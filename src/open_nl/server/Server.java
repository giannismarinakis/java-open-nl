/* 
	Copyright (C) 2017  Giannis Marinakis
	
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	LICENSE file for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package open_nl.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import open_nl.common.CallbackScript;
import open_nl.common.RPC;
import open_nl.common.Sender; 

public class Server{ 
	public static int receive_buffer = 1024;
	public static int port;
	public static boolean hosting = false;
	//When a client connects / disconnects it informs all the clients, if true
	public static boolean syncClients = false;
	
	private static DatagramSocket socket;
	private static Thread thread;	
	private static Class<?> callerClass;
	private static Object callerObject;
	private static ArrayList<Object> objsValues;
	 
	
	public static void initialize(Object caller, int port){  
		//Get the name of the class that called the initialize method
		String callerClassName = caller.getClass().getName();
		
		try {
			//Store the class and the class's object, so later we can call Server's callback methods
			//like onClientConnect etc...
			Server.callerClass = Class.forName(callerClassName); 
			Server.callerObject = caller; 
		} catch (ClassNotFoundException e) { 
			System.out.println("Server error. Could not get caller class. Error: " + e.getMessage());
			return;
		}
		 
		Server.port = port;
		//Start the thread that listens for incoming data
		thread = new Thread(() -> startCommunication());  
		thread.start();
	} 
	
	//Shuts down the server and sends a disconnection message to all clients, if any
	public static void shutdown(String message){
		for(SClient client : ServerStorage.clients){
			//'ss' means 'server shutdown' and it disconnects all the currently connected clients
			client.send(("ss#" + message).getBytes());
		}
		Server.hosting = false;
		System.out.println("Server is no longer running.");
	}
	
	//Sends data to a specific client via the ID parameter and returns the result
	public static String csend(int clientID, String data){
		SClient client = null;
		data = "sm" + data;
		byte[] d = data.getBytes();
		if((client = clientExists(clientID)) != null){
			IOException exception = client.send(d);
			if(exception == null) return ""; 
			return "Server error. Could not send data to the specified client. Error: " + exception.getMessage();
		}else{ 
			return "Server error. Could not find client with that ID.";
		}
	}
	
	//Sends data to all the clients except the selected one (if any)
	public static void sendToAll(String data, SClient except){ 
		for(SClient client : ServerStorage.clients){
			if(client != except){
				client.send(data.getBytes());
			}
		}
	} 
	
	//Sets the receive buffer size
	public static void setReceiveBufferSize(int size){
		Server.receive_buffer = size;
	}
	
	//Gets the server's socket
	public static DatagramSocket getSocket(){
		return Server.socket;
	}
	
	//Starts listening for incoming data on a specific port
	private static void startCommunication(){		  
		try{
			Server.socket = new DatagramSocket(Server.port);
			Server.hosting = true;
			callCallerMethod("onServerInitialized", null);
			
			while(Server.hosting){
				//Create the packet so we can receive data
				DatagramPacket packet = new DatagramPacket(new byte[Server.receive_buffer], Server.receive_buffer);
				
				//Wait till we receive data and fill the packet with the incoming data
				socket.receive(packet); 
				
				String data = new String(packet.getData(), 0, packet.getData().length).trim();
				
				analyze(data, packet);
			}
			//Close the receiving/sending data for this socket
			Server.socket.close();
		}catch(IOException e){
			Server.hosting = false;
			System.out.println("Error occurred on the server: " + e.getMessage());
		}
	}	
	
	//Analyze the incoming data
	private static void analyze(String data, DatagramPacket packet){
		//'c' is sent when the client connects to the server.
		//The server will store the client to its storage.
		if(data.equals("c")){ 
			SClient client = new SClient(packet.getAddress().toString().replace("/", ""), packet.getPort(), ++ServerStorage.clientIDCounter);
			ServerStorage.clients.add(client);
			//Return to the client that he connected successfully.
			client.send(("c#"+client.id).getBytes()); 
			//Call the Server's callback method onClientConnect
			callCallerMethod("onClientConnect", new Object[] {client}, SClient.class); 
			//Inform all the clients about the new connection 
			if(Server.syncClients){ 
				String s = "";
				
				for(SClient cl : ServerStorage.clients)  
					s += cl.id + "@" + cl.ip + "@" + cl.port + "%";
				
				s = "cc#@" + s;
				//Send the connect data to all the clients
				for(SClient cl : ServerStorage.clients)  
					cl.send(s.getBytes());
				
			}
		}else if(data.startsWith("d#")){	//The client is about to disconnect, so remove him.
			SClient client = null;
			
			//Get the client object
			for (SClient cl: ServerStorage.clients) {
				if(cl.id == Integer.parseInt(data.split("#")[1]))
				{
					client = cl;
					break;
				}
			}
			
			if(client != null){
				//Call the Server's callback method, onClientDisconnect
				callCallerMethod("onClientDisconnect", new Object[]{client}, SClient.class);
				//Remove the client from the clients array
				ServerStorage.clients.remove(client);
				
				//Inform all the clients about the new disconnection 
				if(Server.syncClients){ 
					String s = "";
					for(SClient cl : ServerStorage.clients) 
						s += cl.id + "@" + cl.ip + "@" + cl.port + "%";
					 
					s = "cd#@" + s;
					
					//Send the disconnect data to all the clients
					for(SClient cl : ServerStorage.clients)
						cl.send(s.getBytes()); 
				}
			}
		}else if(data.startsWith("rpc")){
			objsValues = new ArrayList<Object>();
			String unchangedData = data;
			data = data.substring(3);
			//Get the client id 
			int i = data.indexOf('#');
			int clientID = Integer.parseInt(data.substring(0, i));
			data = data.substring(i + 1);
			String to = "", methodName = "";
			ArrayList<String> parametersValues = new ArrayList<String>(); 
			SClient client = null;
			
			@SuppressWarnings("rawtypes")
			ArrayList<Class> classes = new ArrayList<Class>();
			//Example data: Server@runMe@3@1.11.21.3
			  
			//Initialize the Sender object that will be passed to the first parameter of the calling method
			Sender sender;
			if(clientID != -1){ 
				sender = new Sender(clientExists(clientID));
				sender.isClient = true;
			}else{
				sender = new Sender(null);
				sender.isServer = true;
			}

			//Add the default parameter that every rpc method needs to have (Sender)
			objsValues.add(sender);
			classes.add(Sender.class);
			
			//Get the RPCMode (to) and the method to be called(methodName)
			for(int b = 0; b < 2; b++){
				i = data.indexOf('@');
				if(b == 0)
					to = data.substring(0, i);
				if(b == 1)
					methodName = data.substring(0, i);
				data = data.substring(i + 1);
			}
			//Get the client that we wont send data to
			if(to.equals("Others"))
				client = clientExists(clientID);
			
			//Broadcast the message to the clients
			//If the RPC call is for the Server, don't broadcast the RPC
			if(to.equals("All") || to.equals("Others") && !to.equals("Server"))
				sendToAll(unchangedData, client);
			
			//If the RPC has to go to Server or All then execute locally the RPC call on the server 
			if(to.equals("Server") || to.equals("All") || to.equals("Others")){
				i = data.indexOf('@'); 
				
				//Get the length of the parameters array
				int paramsLength = Integer.parseInt(data.substring(0, i));
				data = data.substring(i + i);
				for(int b = 0; b < paramsLength; b++){
					i = data.indexOf('.'); 
					//Get the length of the parameter's value
					int len = Integer.parseInt(data.substring(0, i));
					//Get what type the parameter is (String, float, etc...)
					int typecode = Integer.parseInt(data.substring(i+1, i+2));
					//Get and save to the classes ArrayList the type of the current parameter
					classes.add(getClassFromType(typecode, data, i, len));
					//Save the value of the current parameter to the ArrayList
					parametersValues.add(data.substring(i+2, i + len + 2));
					//Cut the data for the current parameter, so we can get the next one
					data = data.substring(i + len + 2);
				} 
				
				//Don't execute the method if the Server sent the RPC and it is broadcasted to Others
				if(clientID != -1 || !to.equals("Others"))
					callCallerMethodRPC(methodName, objsValues.toArray(), classes.toArray(new Class[0]));
			} 
		} 
	}
	
	//Gets the type of a parameter based on a code that is generated from the RPC class
	private static Class<?> getClassFromType(int c, String data, int i, int len){
		String dString = data.substring(i+2, i + len + 2);
		if(c == 0){
			objsValues.add(dString.charAt(0));
			return char.class;
		}else if(c == 1){
			objsValues.add(dString);
			return String.class;
		}else if(c == 2){
			objsValues.add(Integer.parseInt(dString));
			return int.class;
		}else if(c == 3){
			objsValues.add(Byte.parseByte(dString));
			return byte.class;
		}else if(c == 4){
			objsValues.add(Short.parseShort(dString));
			return short.class;
		}else if(c == 5){
			objsValues.add(Boolean.parseBoolean(dString));
			return boolean.class;
		}else if(c == 6){
			objsValues.add(Double.parseDouble(dString));
			return double.class;
		}else if(c == 7){
			objsValues.add(Long.parseLong(dString));
			return long.class;
		}else if(c == 8){
			objsValues.add(Float.parseFloat(dString));
			return float.class;
		}else{ //Object.class
			objsValues.add((Object)dString);
			return Object.class;
		}
	}
	 
	//Loop through all the objects that have atleast one RPC callback method
	//and call the specified one, if exists.
	private static void callCallerMethodRPC(String methodName, Object[] parametersValues, Class<?>... parameters){
		for(CallbackScript cs : RPC.callbackScripts){
			try {
				Method method = cs.scriptClass.getDeclaredMethod(methodName, parameters);
				method.setAccessible(true);
				method.invoke(cs.scriptObject, parametersValues);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) { }  
		}
	}
	
	//Calls the specified method to the class that used the initialize() method, if the method exists!
	//Acts like a callback, like, calls the onServerInitialized, onPlayerConnected methods etc..
	private static void callCallerMethod(String methodName, Object[] parametersValues, Class<?>... parameters){
		try {
			Method method = Server.callerClass.getDeclaredMethod(methodName, parameters);
			method.setAccessible(true);
			method.invoke(Server.callerObject, parametersValues);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) { }  
	}
	
	//Checks if a specific client exists via the given ID
	//If the client exists, it returns the SClient object, else, null.
	private static SClient clientExists(int clientID){
		for(SClient c : ServerStorage.clients){
			if(c.id == clientID){
				return c;
			}
		}
		return null;
	}
}
