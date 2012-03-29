package game;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;

public class StartScreenState extends AbstractScreenController {

	private InputManager inputManager;


	public StartScreenState() {
		super();
	}
	
	@Override
	public void initialize(AppStateManager stateManager, Application a) {
		/** init the screen */
		super.initialize(stateManager, a);
		
		inputManager = app.getInputManager();
		inputManager.setCursorVisible(true);
	
		this.inputManager = app.getInputManager();
	}

	@Override
	public void update(float tpf) {
		/** any main loop action happens here */
	}

	@Override
	public void stateAttached(AppStateManager stateManager) {
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
	}

	@Override
	public void bind(Nifty nifty, Screen screen) {
		super.bind(nifty, screen);
	}

	@Override
	public void onEndScreen() {
		stateManager.detach(this);
	}

	@Override
	public void onStartScreen() {
	}
	
	public void startGame(String nextScreen) {
		app.gotoGame();
	}
	
	public void quitGame() {
		app.stop();
	}

}