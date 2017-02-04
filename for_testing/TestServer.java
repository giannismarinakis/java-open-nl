package for_testing; 

import javax.swing.JFrame;  

public class TestServer extends JFrame{
	static JFrame frame;
	static ServerPanel panel;
	public static void main(String[] args){ 
		frame = new JFrame("Sersver");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(480, 240);
		frame.setLocationRelativeTo(null);
		panel = new ServerPanel();
		frame.add(panel);
		frame.addKeyListener(panel); 
		frame.setVisible(true);  
	}  
}