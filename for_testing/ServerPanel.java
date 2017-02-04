package for_testing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.sun.javafx.scene.control.skin.IntegerFieldSkin;
import com.sun.nio.file.ExtendedWatchEventModifier;

import open_nl.common.RPC;
import open_nl.common.RPCMode;
import open_nl.common.Sender;
import open_nl.server.SClient;
import open_nl.server.Server;

public class ServerPanel extends JPanel implements ActionListener, KeyListener{
 	private static final long serialVersionUID = 1L;
	private String log = ""; 
	Echo echo;
	
	public ServerPanel(){
		Server.syncClients = true;
		Server.setReceiveBufferSize(1024);
		Server.initialize(this, 7312);
		Timer timer = new Timer(10, this);
		timer.start(); 
		echo = new Echo(5);
		RPC.enableRPCfor(echo, this); 
	}
	 
	public void paint(Graphics g) { 
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawString("Press SPACE key for the command line.", 10, 20);
		g2d.drawString("Is server online? " + Server.hosting, 300, 20);
		g2d.drawString("Command log: ", 10, 40);
		int i = 0; 
		for(String command : log.split("\n")){
			g2d.drawString(command, 10, 60 + (20 * i));
			i++;
		}
	}
 
	public void keyPressed(KeyEvent e) {
		 
	}
 
	public void keyReleased(KeyEvent e) {
		 if(e.getKeyCode() == KeyEvent.VK_SPACE){
			String cm = JOptionPane.showInputDialog(null, "Enter a command:");
			log += "/>" + cm + "\n"; 
			proccessCommand(cm);
		 }
	}
 
	public void keyTyped(KeyEvent e) {
		 
	}
 
	public void actionPerformed(ActionEvent arg0) {
		repaint();
	}
	
	void proccessCommand(String command){
		if(command.equals("stop")){
			Server.shutdown("Restarting...");
		}else if(command.startsWith("send:")){
			String[] sp = command.replace("\n", "").split(":");
			Server.csend(Integer.parseInt(sp[1]), sp[2]);
		}else if(command.equals("rpc")){ 
			RPC.send(RPCMode.All, "test", "Hello motherfucker");
		}
	}
	
	void onServerInitialized(){
		log += "Server successfully started!\n"; 
	}
	
	void onClientConnect(SClient client){
		log += "A player has been connected from "+client.ip+":"+client.port+" with ID->"+client.id+"\n";
	}
	
	void onClientDisconnect(SClient client){
		log += "A player has been disconnected. Data: "+client.ip+":"+client.port+" with ID->"+client.id+"\n";
	}
	
	void test(Sender sender, String ep){
		System.out.println("[Server] epitheto: " + ep);
	}
}