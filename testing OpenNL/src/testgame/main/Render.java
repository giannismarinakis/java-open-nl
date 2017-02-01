package testgame.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import open_nl.client.Client;
import open_nl.server.Server;
import testgame.network.GameDataHolder;
import testgame.network.NetworkPlayer;

public class Render extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;

	public Render(){
		this.setBackground(Color.blue.darker().darker());
		Timer timer = new Timer(10, this);
		timer.start();
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g; 
		if(Server.hosting || Client.isConnected){
			String who = Server.hosting == false ? "Client on "+Client.serverIP+":" + Client.serverPort : "Server on port " + Server.port;
			g2d.setColor(Color.white);
			g2d.drawString("You are: " + who, 10, 20);
		}
		for(NetworkPlayer player : GameDataHolder.players){
			player.draw(g2d);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		repaint();
	} 
}
