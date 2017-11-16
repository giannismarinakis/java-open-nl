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
package open_nl.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import open_nl.client.Client;
import open_nl.server.SClient;
import open_nl.server.Server; 

//This class is a combination of the Remote procedure call (RPC) and Remote method invocation (RMI)
//More here: 
//	RPC: https://en.wikipedia.org/wiki/Remote_procedure_call
//	RMI: https://en.wikipedia.org/wiki/Distributed_object_communication
//The client packs the parameters and the method name and sends the data to the server. 
//Then the server receives the incoming data from the client, and processes them based on the RPCMode.
//RPCModes--------
//	**All -> Everyone executes the method locally.

//	**Server -> Server side: The server executes the method locally, and it does not send the RPC call to any of it's clients.

//	**Others -> Server side: The server executes the method locally if the RPC call it not sent by it's self, and sends the RPC call to all the clients
//			 -> Client side: The client executes the method locally if the RPC call it not sent by it's self.

public class RPC {
	public static int groupID = 0;
	
	private static ArrayList<CallbackScript> callbackScripts = new ArrayList<CallbackScript>();
	
	//Enables the object passed to receive RPC calls
	public static void enableRPCfor(Object... caller){
		for (Object object : caller) {
			for(CallbackScript script : callbackScripts)
				if(script.scriptObject == object) return;
			
			callbackScripts.add(new CallbackScript(object)); 		
		} 
	}
	
	//Clears the array of the stored scripts 
	public static void removeAllscripts(){
		callbackScripts.clear();
	}
	
	public static ArrayList<CallbackScript> getCallbackScripts(){
		return callbackScripts;
	}
	
	//Disables the object passed from receiving RPC calls
	public static void removeRPCfor(Object caller){
		CallbackScript toRemove = null;
		for(CallbackScript cScript : callbackScripts){
			if(cScript.scriptObject == caller){
				toRemove = cScript;
				break;
			}
		}
		callbackScripts.remove(toRemove);
	}
	
	//Sends the RPC with RPCMode
	public static void send(RPCMode mode, String methodName, Object... arguments){
		sendRPC(mode, 0, null, null, methodName, arguments);
	} 
	
	//Sends the RPC with RPCMode and Group ID
	public static void send(RPCMode mode, int groupID, String methodName, Object... arguments){
		sendRPC(mode, groupID, null, null, methodName, arguments);
	}
	
	//Sends the RPC with Sender
	public static void send(Sender sender, String methodName, Object... arguments){
		sendRPC(null, 0, sender, null, methodName, arguments);
	} 
	
	//Sends the RPC with SClient
	public static void send(SClient client, String methodName, Object... arguments){
		sendRPC(null, 0, null, client, methodName, arguments);
	} 
	
	@SuppressWarnings("resource")
	private static void sendRPC(RPCMode mode, int groupID, Sender sender, SClient client, String methodName, Object... arguments) {
		DatagramSocket socket;
		String args = "";
		//Converts the passed arguments to String
		for (Object object : arguments) {
			//Get the code of the argument (String -> 1, Integer -> 2, etc...)
			int typecode = getTypeCode(object.getClass());
			String str = object.toString(); 
			args += str.length() + "." + typecode + str; 
		}
		//Get the current ID, -1 for Server.
		int id = Client.isConnected() ? Client.getID() : -1;
	
		//The final data for the RPC
		String data = "";
		
		if(sender == null && client == null) {
			data = "rpc"+id+"#" + mode + "#"+groupID+"@" + methodName + "@" + arguments.length + "@" + args;
		}else {
			int i_d;
			
			if(sender != null)
				i_d = sender.isServer ? -1 : sender.getClient().getID();
			else 
				i_d = client.getID();
			
			data = "rpc"+id+"#id." + i_d + "#"+groupID+"@" + methodName + "@" + arguments.length + "@" + args;
		}
		
		byte[] bdata = data.getBytes();
		
		socket = Server.isHosting() ? Server.getSocket() : Client.getSocket();
		try {
			socket.send(new DatagramPacket(bdata, bdata.length, Server.isHosting() ? InetAddress.getByName("localhost") : InetAddress.getByName(Client.getServerIP()), Server.isHosting() ? Server.getPort() : Client.getServerPort()));
		} catch (IOException e) { 
			e.printStackTrace();
		} 		
	}
	
	//Converts a variable type to a code 
	private static int getTypeCode(Class<? extends Object> c){
		if(c == Character.class)
			return 0;
		else if(c == String.class)
			return 1;
		else if(c == Integer.class)
			return 2;
		else if(c == Byte.class)
			return 3;
		else if(c == Short.class)
			return 4;
		else if(c == Boolean.class)
			return 5;
		else if(c == Double.class)
			return 6;
		else if(c == Long.class)
			return 7;
		else if(c == Float.class)
			return 8;
		else //Object.class
			return 9;
	}
}