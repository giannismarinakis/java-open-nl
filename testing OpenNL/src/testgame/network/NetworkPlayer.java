package testgame.network;

import java.awt.Color;
import java.awt.Graphics2D;

import open_nl.client.Client;
import open_nl.server.Server;

public class NetworkPlayer {
	public int x, y, id, speed = 10;
	public String username;
	
	public NetworkPlayer(String username, int x, int y, int id){
		this.username = username;
		this.x = x;
		this.y = y;
		this.id = id;
	}
	
	public void draw(Graphics2D g2d){
		if(id == Client.id || (Server.hosting && id == -1))
			g2d.setColor(Color.GREEN);
		else
			g2d.setColor(Color.RED);
		g2d.drawString(this.username, this.x, this.y - 10);
		g2d.setColor(Color.WHITE);
		g2d.drawRect(this.x, this.y, 50, 50);
	}
}
