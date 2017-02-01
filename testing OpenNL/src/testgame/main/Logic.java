package testgame.main;

import java.util.Random;

import javax.swing.JOptionPane;

import open_gl.common.RPC;
import open_gl.common.RPCMode;
import open_nl.client.Client;
import open_nl.server.Server;
import testgame.network.ClientCallbackScript;
import testgame.network.CommonRPCcalls;
import testgame.network.GameDataHolder;
import testgame.network.NetworkPlayer;
import testgame.network.ServerCallbackScript;

public class Logic {
	public NetworkPlayer myPlayer;
	ClientCallbackScript clientCS;
	ServerCallbackScript serverCS;
	CommonRPCcalls common;
	
	public void commandDialog(){
		String command =  JOptionPane.showInputDialog(Window.frame, "Enter a command:"); 
		if(command != null){
			if(command.startsWith("connect")){	//Connect to a server command 
				//Example:connect localhost 7312
				String[] sp = command.split(" ");
				String ip = sp[1];
				int port = Integer.parseInt(sp[2]);
				clientCS = new ClientCallbackScript(this);
				common = new CommonRPCcalls(this);
				//'Tell' to OpenNL which scripts are going to receive RPC calls
				RPC.enableRPCfor(clientCS, common);
				//All the client's callback methods will be placed in the 'clientCS' object.
				Client.connectTo(clientCS, ip, port);
			}else if(command.startsWith("server")){	//Start hosting a server on a specific port
				//Example:server 7777
				String[] sp = command.split(" ");
				int port = Integer.parseInt(sp[1]);
				serverCS = new ServerCallbackScript(this);
				common = new CommonRPCcalls(this);
				//'Tell' to OpenNL which scripts are going to receive RPC calls
				RPC.enableRPCfor(common);
				
				Server.initialize(serverCS, port);
			}else if(command.equals("stop")){
				Server.shutdown("Server is closing by command...");
				RPC.removeAllscripts();
				GameDataHolder.players.clear();
			}else if(command.equals("disconnect")){ 
				Client.disconnect();
			}
		}
	}
	
	public void sendSpawnPlayer(String username){
		System.out.println("Spawning my player object.");
		
		Random random = new Random();
		int x = random.nextInt(Window.frame.getWidth() - 50);
		int y = random.nextInt(Window.frame.getHeight() - 50);
		
		RPC.send(RPCMode.All, "spawnPlayer", username, x, y, Server.hosting ? -1 : Client.id);
	}
	
	public void move(String dir){
		if(Client.isConnected || Server.hosting){
			if(dir == "right"){
				myPlayer.x += myPlayer.speed;
			}else if(dir == "left"){
				myPlayer.x -= myPlayer.speed;
			}else if(dir == "up"){
				myPlayer.y -= myPlayer.speed;
			}else if(dir == "down"){
				myPlayer.y += myPlayer.speed;
			}
			RPC.send(RPCMode.Others, "updatePlayerPosition", myPlayer.x, myPlayer.y, myPlayer.id);
		}
	}
}
