package for_testing;
  

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import open_nl.client.Client;
import open_nl.common.RPC;
import open_nl.common.RPCMode;
import open_nl.common.Sender;
import open_nl.server.SClient; 

public class TestClient extends JFrame{
	static JFrame frame;
	static ServerPanelC panel;
	int a = 0;
	public static void main(String[] args){ 
		frame = new JFrame("Client");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(480, 240);
		frame.setLocationRelativeTo(null);
		panel = new ServerPanelC();
		frame.add(panel);
		frame.addKeyListener(panel); 
		frame.setVisible(true);  
	}
}

class ServerPanelC extends JPanel implements ActionListener, KeyListener{
	private String inputData = "", log = "";
	
	public ServerPanelC(){ 
		Client.connectTo(this, "localhost", 7312);
		Timer timer = new Timer(10, this);
		timer.start(); 
		RPC.enableRPCfor(this);
	}
	 
	public void paint(Graphics g) { 
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		if(Client.isConnected)
			g2d.drawString("Connected players: " + Client.connectedClients.size(), 10, 20); 
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
		if(command.startsWith("send:")){
			//send:localhost:7312:hey
			String[] sp = command.replace("\n", "").split(":");
			String ip = sp[1];
			int port = Integer.parseInt(sp[2]);
			byte[] data = sp[3].getBytes();
			
			Client.sendDataTo(ip, port, data);
		}else if(command.equals("disconnect")){
			Client.disconnect();
		}else if(command.equals("rpc")){
			RPC.send(RPCMode.Others, "test", 7312);
		}
	} 
	
	//Callback methods
	
	void runMe(Sender sender){
		System.out.println("Hey "+ " - " + sender.getClient().port);
	}
	
	void test(Sender sender, String ep){
		System.out.println("[Client] epitheto: " + ep);
	}
	
	void onConnectedToServer(){
		log += "Connected to the server!!\n";
	}
	
	void onFailedToConnect(){
		log += "Failed to connect to the specified server.\n";
	}
	
	void onDisconnectedFromServer(){
		log += "Disconnected..!\n";
	}
	
	void onDisconnectedFromServer(String disconnectionMessage){
		log += "Disconnected from server: " + disconnectionMessage;
	} 
}