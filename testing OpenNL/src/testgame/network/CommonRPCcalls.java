package testgame.network;

import open_gl.common.Sender;
import open_nl.client.Client;
import open_nl.server.Server;
import testgame.main.Logic;

public class CommonRPCcalls {
	Logic logic;
	
	public CommonRPCcalls(Logic logic){
		this.logic = logic;
	}
	
	void spawnPlayer(Sender sender, String username, int x, int y, int id){ 
		NetworkPlayer player = new NetworkPlayer(username, x, y, id);
		if(!GameDataHolder.players.contains(player)){
			if(Client.id == id || (Server.hosting && id == -1))
				logic.myPlayer = player;
			GameDataHolder.players.add(player);
		}
	}
	
	void removePlayer(Sender sender, int id){
		NetworkPlayer player = null;
		for(NetworkPlayer p : GameDataHolder.players){
			if(p.id == id){
				player = p;
				break;
			}
		}
		GameDataHolder.players.remove(player);
	}
	
	void updatePlayerPosition(Sender sender, int x, int y, int id){
		for(NetworkPlayer player : GameDataHolder.players){
			if(player.id == id){
				player.x = x;
				player.y = y;
				break;
			}
		}
	}
}
