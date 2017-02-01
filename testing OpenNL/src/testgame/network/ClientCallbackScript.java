package testgame.network;

import javax.swing.JOptionPane;

import open_gl.common.RPC;
import open_gl.common.Sender;
import open_nl.client.Client;
import open_nl.server.Server;
import testgame.main.Logic;
import testgame.main.Window;

public class ClientCallbackScript {
	Logic logic;	
	
	public ClientCallbackScript(Logic logic){
		this.logic = logic;
	}
	
	//Client callback methods below
	
	void onConnectedToServer(){
		System.out.println("Connected to the server.");
		String username;
		username = JOptionPane.showInputDialog(Window.frame, "Please enter a username: ");
		 
		logic.sendSpawnPlayer(username);
	}
	
	void onFailedToConnect(){
		System.out.println("Failed to connect to the server.");
	}
	
	void onDisconnectedFromServer(String disconnectionMessage){
		System.out.println("Disconnected. Message: " + disconnectionMessage);
		RPC.removeAllscripts();
		GameDataHolder.players.clear();
	}
	
	//Callback methods for RPCs
	void receiveConnectedPlayer(Sender sender, int idRequested, String username, int x, int y, int id){
		if(Client.id == idRequested && !Server.hosting)
			GameDataHolder.players.add(new NetworkPlayer(username, x, y, id));
	}
}
