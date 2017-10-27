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
package open_nl.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method; 
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import open_nl.common.CallbackScript;
import open_nl.common.RPC;
import open_nl.common.Sender;
import open_nl.server.SClient; 

public class Client { 
	public static int receive_buffer = 2048;
	public static int serverPort;
	public static String serverIP;
	public static int id;
	public static int connectTimeout = 8000;
	public static boolean isConnected = false;
	//This arraylist will be filled only if the server has enabled the option 
	//that informs the clients about all the other clients (syncClients).
	public static ArrayList<SClient> connectedClients = new ArrayList<SClient>();
	
	private static DatagramSocket socket;
	private static Thread thread;
	private static long startTimeMillis;
	private static Class<?> callerClass;
	private static Object callerObject; 
	private static String serverDisconnectMessage = "";
	private static ArrayList<Object> objsValues;
	
	//Connects to a specific server
	public static void connectTo(Object callbackScript, String ip, int port){
		//Get the name of the class that called the connectTo method
		String callerClassName = callbackScript.getClass().getName();

		try {
			//Store the class and the class's object, so later we can call Client's callback methods
			//like onConnectedToServer etc...
			Client.callerClass = Class.forName(callerClassName); 
			Client.callerObject = callbackScript; 
		} catch (ClassNotFoundException e) { 
			System.out.println("Client error. Could not get caller class. Error: " + e.getMessage());
			return;
		}
		//Initialize the Client's socket first
		initializeClientSocket();
		
		Client.serverIP = ip;
		Client.serverPort = port; 
		
		//The 'c' tells the server that we are going to connect.
		send("c".getBytes());
		//First, attempt to connect to the server
		connect();
	}
	
	//Disconnects the client locally and remotely
	public static void disconnect(){
		//The 'd#' tells the server that we are going to disconnect.
		send(("d#"+id).getBytes());
		Client.socket.close();
		Client.isConnected = false; 
		Client.serverIP = "";
		Client.serverPort = 0;
		//Call the Client's callback method onDisconnectedFromServer
		callCallerMethod("onDisconnectedFromServer", new Object[] {serverDisconnectMessage}, String.class);
	} 
	
	//Sends data to a specific socket
	public static void sendDataTo(String ip, int port, byte[] data){ 
		if(socket == null){
			System.out.println("Client error. Socket is not initialized.");
			return;
		}
		//Socket is OK, so send the data.
		try {
			InetAddress parsedIP = InetAddress.getByName(ip);
			send(data, parsedIP, port, false);
		} catch (UnknownHostException e) { 
			System.out.println("Client error. Could not parse IP address.");
			e.printStackTrace();
		}
	}
	
	//Sends data to a specific socket
	public static void sendDataTo(String ip, int port, DatagramPacket packet){ 
		if(socket == null){
			System.out.println("Client error. Socket is not initialized.");
			return;
		}
		//Socket is OK, so send the data.
		try {
			InetAddress parsedIP = InetAddress.getByName(ip);
			send(packet.getData(), parsedIP, port, false);
		} catch (UnknownHostException e) { 
			System.out.println("Client error. Could not parse IP address.");
			e.printStackTrace();
		}
	} 
	
	//Get the Client's socket
	public static DatagramSocket getSocket(){
		return Client.socket;
	}
	
	private static void connect(){
		//The following thread runs for "connectTimeout" milliseconds and handles the 
		//callback method that runs when the client failed to connect to the server.
		startTimeMillis = System.currentTimeMillis();
		new Thread(new Runnable(){
			public void run(){
				long lastTime = System.currentTimeMillis();
				long count = 500;
				//Run a second thread that will wait for the response of the server
				new Thread(new Runnable() {  
					public void run() {
						DatagramPacket packet = new DatagramPacket(new byte[Client.receive_buffer], Client.receive_buffer);
						//Wait till we receive the success connect packet from the server.
						try {
							Client.socket.receive(packet);
							String data = new String(packet.getData(), 0, packet.getData().length).trim();
							analyzeData(data);
						} catch (IOException e) {  }
					}
				}).start();

				//Send every 500 milliseconds a connect request packet 
				while(System.currentTimeMillis() - startTimeMillis <= connectTimeout){ 
					if(System.currentTimeMillis() - lastTime > count){
						send("c".getBytes()); 
						count += 500; 
					}
					if(Client.isConnected)	break;								
				} 

				//If the client fails to connect after (connectTimeout / 500) tries, then it 
				//calls the "onFailedToConnect" method (if available)
				if(!Client.isConnected){
					callCallerMethod("onFailedToConnect", null);
					Client.socket.close();
				}else{
					//Start listening for incoming packets
					thread = new Thread(() -> startListening());  
					thread.start();
				}
			}
		}).start();	
	}
	
	//Start listening for incoming packets
	private static void startListening(){ 
		try{   
			while(Client.isConnected){  
				//Create the packet so we can receive data
				DatagramPacket packet = new DatagramPacket(new byte[Client.receive_buffer], Client.receive_buffer);
				//Wait till we receive data and fill the packet with the incoming data
				Client.socket.receive(packet);
				
				byte[] bd = packet.getData();
				String data = new String(bd, 0, bd.length).trim();
				
				analyzeData(data); 
			}
			//Close the receiving/sending data for this socket 
			Client.socket.close();
		}catch(IOException e){
			String errorMessage = e.getMessage();
			if(!errorMessage.equals("socket closed")){
				System.out.println("Error occurred on the client: " + e.getMessage()); 
			}
		} 
	}
	
	private static void analyzeData(String data){
		//'c' code, tells the client that he connected successfully.
		if(data.startsWith("c#")){ 
			Client.isConnected = true; 
			Client.id = Integer.parseInt(data.split("#")[1]);
			callCallerMethod("onConnectedToServer", null);
		}else if(data.startsWith("ss#")){	//Server is going to be disconnected.
			serverDisconnectMessage = data.split("#")[1];
			disconnect();
		}else if(data.startsWith("cc#@") || data.startsWith("cd#@")){	
			//A client has been connected or disconnected, so refresh the clients list
			refreshClients(data.replace("cc#@", "").replace("cd#@", ""));
		}else if(data.startsWith("rpc")){ //Received RPC call
			objsValues = new ArrayList<Object>();
			data = data.substring(3);
			//Get the client id
			int i = data.indexOf('#');
			int clientID = Integer.parseInt(data.substring(0, i));
			data = data.substring(i + 1);
			String methodName = "";
			ArrayList<String> parametersValues = new ArrayList<String>(); 
			
			@SuppressWarnings("rawtypes")
			ArrayList<Class> classes = new ArrayList<Class>();
			//Example data: rpc1#All@test@1@18.1Hello world
			
			//Initialize the Sender object that will be passed to the first parameter of the calling method
			Sender sender;
			if(clientID != -1){ 
				sender = new Sender(getClient(clientID));
				sender.isClient = true;
			}else{
				sender = new Sender(null);
				sender.isServer = true;
			}
			//Add the default parameter that every RPC method NEEDS to have (Sender)
			objsValues.add(sender);
			classes.add(Sender.class);
			
			//Skip the RPCMode (to, i -> 0) and get the method to be called(methodName)
			for(int b = 0; b < 2; b++){
				i = data.indexOf('@');
				if(b == 1)
					methodName = data.substring(0, i);
				data = data.substring(i + 1);
			}

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
			//Call the method
			callCallerMethodRPC(methodName, objsValues.toArray(), classes.toArray(new Class[0]));
		}else if(data.startsWith("m#")){	//Received message direct message from SClient.send() method.
			//example: m#1#hello
			data = data.substring(2);
			int i = data.indexOf('#');
			int senderID = Integer.parseInt(data.substring(0, i));
			data = data.substring(i+1, data.length());
			Sender sender;
			if(senderID != -1){ 
				sender = new Sender(getClient(senderID));
				sender.isClient = true;
			}else{
				sender = new Sender(null);
				sender.isServer = true;
			}
			callCallerMethod("onMessageReceived", new Object[]{sender, data}, Sender.class, String.class);
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
	
	//Refreshes the client list based on the given data
	private static void refreshClients(String data){ 
		Client.connectedClients.clear();
		String[] sp0 = data.split("%");
		for(String str : sp0){
			String[] sp1 = str.split("@");
			SClient client = new SClient(sp1[1].replace("/", ""), Integer.parseInt(sp1[2]), Integer.parseInt(sp1[0]));
			Client.connectedClients.add(client); 
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
	//Acts like a callback, like, calls the onConnectedToServer, onDisconnectedFromServer methods etc..
	private static void callCallerMethod(String methodName, Object[] parametersValues, Class<?>... parameters){
		try {
			
			Method method = Client.callerClass.getDeclaredMethod(methodName, parameters);
			
			method.setAccessible(true);
			method.invoke(Client.callerObject, parametersValues);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) { }  
	} 
	
	//Send data to a specific socket
	private static void send(byte[] data, InetAddress ip, int port, boolean connectPacket){
		try {
			if(!connectPacket)
				data = (".@" + new String(data, 0, data.length).trim()).getBytes();
			
			socket.send(new DatagramPacket(data, data.length, ip, port)); 
		} catch (IOException e) { 
			System.out.println("Client error. Could not send the datagram packet: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	//Gets and returns a client by ID
	private static SClient getClient(int clientID){
		for(SClient cl : connectedClients){
			if(cl.id == clientID)
				return cl;			
		}
		return null;
	}
	
	//Send data to the server
	private static void send(byte[] data){
		try {
			socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(Client.serverIP), Client.serverPort));
		} catch (IOException e) { 
			System.out.println("Client error. Could not send the datagram packet: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	//Initializes the Client's socket
	private static void initializeClientSocket(){ 
		try {
			Client.socket = new DatagramSocket();
		} catch (SocketException e) { 
			System.out.println("Error while initializing client's socket: " + e.getMessage()); 
		}
	}
}
