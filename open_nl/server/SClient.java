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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import open_nl.client.Client;

//This class is used for storing information about a client connection
//It is used from both server and client side
public class SClient {
	public int id, port;
	public String ip;

	public SClient(String ip, int port, int id){
		this.ip = ip;
		this.port = port;
		this.id = id;
	}

	//Sends data to this specific client and returns the result (failure -> IOException | success -> null)
	public IOException send(byte[] data){
		try {
			DatagramSocket socket = null;
			if(Server.hosting) socket = Server.getSocket();
			else if(Client.isConnected) socket = Client.getSocket();

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
}
