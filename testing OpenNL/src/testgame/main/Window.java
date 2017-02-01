package testgame.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import open_nl.client.Client;
import open_nl.server.Server;

public class Window {
	public static JFrame frame;
	static Render renderer;
	static Logic logic;
	static KeyboardInput input;
	
	public static void main(String[] args) {
		frame = new JFrame("Test Game for OpenNL");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		renderer = new Render();
		frame.add(renderer);
		logic = new Logic();
		input = new KeyboardInput(logic);
		frame.addKeyListener(input);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                if(Client.isConnected)
                	Client.disconnect();
                else if(Server.hosting)
                	Server.shutdown("Server is closing by Window exit...");
            }
        });
	} 
}
