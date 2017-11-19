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
package com.github.java_open_nl.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.github.java_open_nl.client.Client; 
 
//This class is used for storing information about a client connection
//It is used from both server and client side
public class SClient {
	private int id, port;
	private String ip;
	
	public long lastTimeMillis; 

	public SClient(String ip, int port, int id){
		this.ip = ip;
		this.port = port;
		this.id = id;
		lastTimeMillis = System.currentTimeMillis();
		
		//Check the current time and the last time that the client send a packet that tells the server that the client is still on
		if(Server.isHosting()) {
			new Thread(new Runnable() {
				public void run() { 					
					while(true) { 
						if(System.currentTimeMillis() - lastTimeMillis > Server.getConnectionLostMillis()) {
							try {
								//Just in case, inform the client (if he is still connected and has more than (Server.clientMaxDelay * 1000)ms ping that the connection is lost
								send("cl#".getBytes());
								//Send the server its self the disconnection packet as the client sent it
								Server.getSocket().send(new DatagramPacket(("d#"+id).getBytes(), ("d#"+id).getBytes().length, InetAddress.getByName("localhost"), Server.getPort()));
							} catch (IOException e) { 
								e.printStackTrace();
							} 
							break; 
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) { 
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	} 

	//Sends data to this specific client and returns the result (failure -> IOException | success -> null)
	public IOException send(byte[] data){
		try {
			DatagramSocket socket = null;
			if(Server.isHosting()) socket = Server.getSocket();
			else if(Client.isConnected()) socket = Client.getSocket();
			 
			String d = new String(data, 0, data.length).trim();

			data = d.getBytes();
			if(socket != null){
				socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(ip), port));
				return null;
			}else{
				return new IOException("SClient error. Could not get the current socket.");
			}
		} catch (IOException e) {
			return e;
		}
	}
	
	//Sends data to this specific client and returns the result (failure -> IOException | success -> null)
	public IOException sendMessage(String data){
		try {
			DatagramSocket socket = null;
			if(Server.isHosting()) socket = Server.getSocket();
			else if(Client.isConnected()) socket = Client.getSocket();
			 
			int myID = (Server.isHosting()) ? -1 : Client.getID();
			String d;
			byte[] finalData;
			d = "m#"+myID+"#" + data; 
			finalData = d.getBytes();
			if(socket != null){
				socket.send(new DatagramPacket(finalData, finalData.length, InetAddress.getByName(ip), port));
				return null;
			}else{
				return new IOException("SClient error. Could not get the current socket.");
			}
		} catch (IOException e) {
			return e;
		}
	}
	
	public int getID() {
		return id;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getIP() {
		return ip;
	}
}
