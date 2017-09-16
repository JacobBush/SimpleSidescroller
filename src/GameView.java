
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

public class GameView extends JPanel {
	
    private static final int HEADER_HEIGHT = 50;
	
	private ViewController vc;
	private Model model;
	
	private Timer animationTimer;
	private float animationLoopTimer = 0.0f;
	//private int leftBlockOnScreen = 0;
	
	public GameView (ViewController vc, Model model) {
		super();
		
		this.vc = vc;
		this.model = model;
		
		setLayout(new BorderLayout());
		add(new GameMenu(),BorderLayout.PAGE_START);
		add(new GameArea(), BorderLayout.CENTER);
		
		model.play();
		startAnimationTimer();
	}
	
	public void paintComponent (Graphics g) {
		super.paintComponent(g);
	}
	
	private void startAnimationTimer() {
		animationTimer = new Timer (1000/vc.getFPS(), new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			handleAnimation(vc.getFPS());
    			repaint();
    			checkGameEnd();
    		}
    	});
		animationTimer.start();
	}
	
	private void handleAnimation (int fps) {
		handleCourseScrollAnimation(fps);
		handleProgressBarAnimation();
	}
	
	private boolean scrollAnimationRunning = false; // miss a scroll frame on stop without this
	private void handleCourseScrollAnimation(int fps) {
		if (model.getGameState() == Course.RUNNING || model.getGameState() == Course.IDLE) {
			scrollAnimationRunning = true;
		}
		if (scrollAnimationRunning) {
			/*if (model.getCourseDisplayBoundary() != leftBlockOnScreen) {
				animationLoopTimer = 0.0f;
				leftBlockOnScreen = model.getCourseDisplayBoundary();
			}*/
			//updateAnimationLoopTimer();
			animationLoopTimer += model.getScrollRate()/fps;
		}
		if (!(model.getGameState() == Course.RUNNING || model.getGameState() == Course.IDLE)) {
			scrollAnimationRunning = false;
		}
	}
	
	private float progress = 0.0f;
	private void handleProgressBarAnimation() {
		float prog = (float) (model.getPlayerLocation().getWidth() / (model.getCourseSize().getWidth() - 1));
		if (prog > progress) {
			progress = prog;
		}
	}
	
	private boolean gameEndPopupDisplayed = false;
	private void checkGameEnd() {
		boolean displayPopup = false;
		String title = "";
		String message = "";
		if (!gameEndPopupDisplayed && model.getGameState() == Course.VICTORY) {
			title = "You have won!";
			message = "Victory!";
			displayPopup = true;
		} else if (!gameEndPopupDisplayed && model.getGameState() == Course.DEFEAT) {
			title = "You have lost!";
			message = "Defeat!";
			displayPopup = true;
		} else {}
		if (displayPopup && !gameEndPopupDisplayed) {
			gameEndPopupDisplayed = true;
			Object[] options = {"Home", "Restart"};
			int answer = JOptionPane.showOptionDialog(vc,
						message,
						title,
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[1]); //default button title
			if (answer == 1) { // restart
				model.restartCourse();
	            resetAnimations();
			} else if (answer == 0) { // home
				model.pause();
				vc.selectView(ViewController.HOME_VIEW);
			} else {/* ??? */}
		}
	}
	
	private void resetAnimations () {
		animationLoopTimer = 0.0f;
		//leftBlockOnScreen = 0;
		progress = 0.0f;
		gameEndPopupDisplayed = false;
	}
	
	public class GameMenu extends JPanel {
		JLabel scoreLabel;
		
    	public GameMenu () {
    		super();
    		setLayout(new BorderLayout());
    		setPreferredSize(new Dimension(0, HEADER_HEIGHT)); // HEADER_HEIGHT pixels high, scale width dynamically
    		setBackground(Color.BLACK);
    				
    		JPanel leftButtons = new JPanel();
    		leftButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
    		leftButtons.setBackground(Color.BLACK);
    		leftButtons.setBorder(new EmptyBorder(5, 5, 5, 5));
    		add(leftButtons, BorderLayout.WEST);
    		
    		JPanel rightButtons = new JPanel();
    		rightButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
    		rightButtons.setBackground(Color.BLACK);
    		rightButtons.setBorder(new EmptyBorder(5, 5, 5, 5));
    		add(rightButtons, BorderLayout.EAST);
    		
    		JPanel centerBar = new JPanel ();
    		centerBar.setBackground(Color.BLACK);
    		centerBar.setBorder(new EmptyBorder(10,20,10,20));
    		centerBar.setLayout(new BorderLayout());
    		add(centerBar, BorderLayout.CENTER);
    		
    		ProgressBar progressBar = new ProgressBar();
    		centerBar.add(progressBar);
    		
    		scoreLabel = new JLabel("0%");
    		progressBar.add(scoreLabel);
    		
    		centerBar.add(progressBar, BorderLayout.CENTER);
    		
    		JButton pauseButton = new JButton("Pause");
    		pauseButton.addActionListener(new ActionListener () {
    			private boolean pause = true;
    			public void actionPerformed(ActionEvent e) {
    				JButton b = (JButton)e.getSource();
    				if (pause) {
    					model.pause();
    					b.setText("Play");
    				} else {
    					model.unpause();
    					b.setText("Pause");
    				}
    	            pause = !pause;
    	        }
    		});
    		pauseButton.setAlignmentX(LEFT_ALIGNMENT);
    		rightButtons.add(pauseButton);
    		
    		rightButtons.add(Box.createRigidArea(new Dimension(10,0)));
    		
    		JButton b = new JButton("Reset");
    		b.addActionListener(new ActionListener () {
    			public void actionPerformed(ActionEvent e) {
    	            model.restartCourse();
    	            resetAnimations();
    	        }
    		});
    		b.setAlignmentX(LEFT_ALIGNMENT);
    		rightButtons.add(b);
    		
    		b = new JButton("Home");
    		b.addActionListener(new ActionListener () {
    			public void actionPerformed(ActionEvent e) {
    	            model.pause();
    	            vc.selectView(ViewController.HOME_VIEW);
    	        }
    		});
    		b.setAlignmentX(LEFT_ALIGNMENT);
    		leftButtons.add(b);        		
    	}
    	public void paintComponent (Graphics g) {
    		super.paintComponent(g);
    		scoreLabel.setText("" + (int)(progress*100) + "%");
    	}
    }
	
	public class ProgressBar extends JPanel {
		
		public ProgressBar() {
			super();
			setBackground(Color.WHITE);
		}		
		
		public void paintComponent (Graphics g) {
    		super.paintComponent(g);
    		g.setColor(Color.RED);
    		int barWidth = (int) Math.round(progress*this.getWidth());
    		g.fillRect(0, 0, barWidth, this.getHeight());
    		g.clearRect(barWidth, 0, this.getWidth(), this.getHeight());
    	}
	}
    
    public class GameArea extends JPanel {
    	public GameArea () {
    		super();
    		
    		InputMap im = getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am =getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RightArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "RightArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LeftArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "LeftArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UpArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "UpArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DownArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "DownArrow");

            am.put("RightArrow", new ArrowAction("RightArrow"));
            am.put("LeftArrow", new ArrowAction("LeftArrow"));
            am.put("UpArrow", new ArrowAction("UpArrow"));
            am.put("DownArrow", new ArrowAction("DownArrow"));
    		
    		
    		setBackground(Color.black);
    		
    	}
    	
    	public class ArrowAction extends AbstractAction {

    	    private String cmd;

    	    public ArrowAction(String cmd) {
    	        this.cmd = cmd;
    	    }

    	    @Override
    	    public void actionPerformed(ActionEvent e) {
    	        if (cmd.equalsIgnoreCase("LeftArrow")) {
    	            model.movePlayerLeft();
    	        } else if (cmd.equalsIgnoreCase("RightArrow")) {
    	        	model.movePlayerRight();
    	        } else if (cmd.equalsIgnoreCase("UpArrow")) {
    	        	model.movePlayerUp();
    	        } else if (cmd.equalsIgnoreCase("DownArrow")) {
    	        	model.movePlayerDown();
    	        }
    	    }
    	}
    	
    	public void paintComponent (Graphics g) {
    		super.paintComponent(g);
    		int[][] course = model.getCourse();
    		Dimension size = model.getCourseSize();
    		//float offset = (float) leftBlockOnScreen + animationLoopTimer;
    		for (int x = 0; x < size.width; x++) {
    			for (int y = 0; y < size.height; y++) {
    				paintUnit(g, x, y, course[x][y]);
    			}
    		}
    	}
    }
    
    private void paintUnit(Graphics g, float x, float y, int spec) {
		int width = vc.getPixelsPerUnit();
		int height = width;
		int offset = (int) (animationLoopTimer * width);
		switch (spec) {
		case Course.EMPTY_TILE:
			g.clearRect((int)(x*width) - offset, (int) (y*height), width, height);
			break;
		case Course.PLAYER_TILE:
			g.setColor(Color.RED);
			g.fillRect((int)(x*width) - offset, (int) (y*height), width, height);
			break;
		case Course.OBSTACLE_TILE:
			g.setColor(Color.GRAY);
			g.fillRect((int)(x*width) - offset, (int) (y*height), width, height);
			break;
		case Course.VICTORY_TILE:
			g.setColor(Color.GREEN);
			g.fillRect((int)(x*width) - offset, (int) (y*height), width, height);
			break;
		default:
			break;
		}
	}
}
