package testgame.network;

import javax.swing.JOptionPane;

import open_gl.common.RPC;
import open_gl.common.RPCMode;
import open_nl.server.SClient; 
import testgame.main.Logic;
import testgame.main.Window;

public class ServerCallbackScript {
	Logic logic;
	
	public ServerCallbackScript(Logic logic){
		this.logic = logic;
	}
	
	//Server callback methods below
	
	void onServerInitialized(){
		System.out.println("Server initialized!");
		String username;
		username = JOptionPane.showInputDialog(Window.frame, "Please enter a username: ");
		 
		logic.sendSpawnPlayer("[SERVER]"+username); 
	}
	
	void onClientConnect(SClient player){
		System.out.println("Player with ID "+player.id+" connected");
		for(NetworkPlayer cplayer : GameDataHolder.players)
			if(cplayer.id != player.id){
				RPC.send(RPCMode.Others, "receiveConnectedPlayer", player.id, cplayer.username, cplayer.x, cplayer.y, cplayer.id); 
				System.out.println("Sending to " + player.id + " to created " + cplayer.id);
			}
	}
	
	void onClientDisconnect(SClient player){
		RPC.send(RPCMode.All, "removePlayer", player.id);
		System.out.println("Player with ID "+player.id+" disconnected");
	}
}
