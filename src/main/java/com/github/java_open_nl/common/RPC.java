/* 
	MIT License
	
	Copyright (c) 2017 giannismarinakis
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
*/
package com.github.java_open_nl.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import com.github.java_open_nl.client.Client;
import com.github.java_open_nl.server.SClient;
import com.github.java_open_nl.server.Server;
  

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
	
	private RPC() {}
	
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