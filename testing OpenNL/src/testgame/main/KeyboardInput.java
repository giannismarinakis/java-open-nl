package testgame.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardInput implements KeyListener{
	Logic logic;
	
	public KeyboardInput(Logic logic){
		this.logic = logic;
	}
	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() != KeyEvent.VK_SPACE){
			//Handle the player movement
			String dir = "";
			
			if(e.getKeyCode() == KeyEvent.VK_D)
				dir = "right";
			else if(e.getKeyCode() == KeyEvent.VK_A)
				dir = "left";
			else if(e.getKeyCode() == KeyEvent.VK_W)
				dir = "up";
			else if(e.getKeyCode() == KeyEvent.VK_S)
				dir = "down";
			else
				return;
			
			logic.move(dir);
		}else{
			//Show the command dialog box
			logic.commandDialog();
		}
	}

	public void keyReleased(KeyEvent e) {
		
	}

	public void keyTyped(KeyEvent e) {
		 
	}

}
