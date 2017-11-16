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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import open_nl.client.Client;
import open_nl.server.Server;

public class ONLSettings {
	public static void onJFrameClose_Disconnect(JFrame frame) {
		if(frame != null) {
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					if(Server.isHosting() || Client.isConnected()) {
						if(Server.isHosting()) {
							Server.shutdown("");
						}else {
							Client.disconnect();
						}
					}
				}
			});
		}else {
			System.out.println("Can't add Window Event on a null JFrame.");
		}
	}
}