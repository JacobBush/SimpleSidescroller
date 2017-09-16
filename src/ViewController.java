
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class ViewController extends JFrame implements Observer {

    public Model model;

    // constants
    private static final String PROGRAM_TITLE = "Sidescroller Game - Jacob Bush 2017";
    private static final Dimension WIN_MIN_DIM = new Dimension(128, 128);
    private static final Dimension WIN_START_DIM = new Dimension(800, 450);
    public static final int HEADER_HEIGHT = 50;
    
    // semi-constants
    private static int PIXELS_PER_UNIT = 31;
    public int getPixelsPerUnit() {return PIXELS_PER_UNIT;}
    private static int FPS = 30;
    public int getFPS() {return FPS;}
    public void setFPS(int fps) {FPS = fps;}
    
    
    // view select constants
    public static final int HOME_VIEW = 0;
    public static final int GAME_VIEW = 1;
    public static final int EDITOR_VIEW = 2;
    public static final int OPTIONS_VIEW = 3;
    
    // Need to dynamically resize editor view
    // Not made general since only single view needs this functionality
    private EditorView editorViewHandle;    
    
    private int currentView = -1;
    /**
     * Create a new View.
     */
    public ViewController(Model model) {
        // Set up the window.
        this.setTitle(PROGRAM_TITLE);
        this.setMinimumSize(WIN_MIN_DIM);
        this.setSize(WIN_START_DIM);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Hook up this observer so that it will be notified when the model
        // changes.
        this.model = model;
        model.addObserver(this);
        setVisible(true);
        selectView(HOME_VIEW);
        resizeBlocks();
        
        addComponentListener(new ComponentAdapter() {  
                public void componentResized(ComponentEvent evt) {
                	resizeBlocks();
                	if (editorViewHandle != null) {
                		editorViewHandle.updateComponentSize();
                	}
                }
        });
    }
    
    private void resizeBlocks () {
    	PIXELS_PER_UNIT = (this.getHeight() - this.HEADER_HEIGHT) / (10 + 2); // 10 blocks per height
    }    
    
    public void selectView (int view) {
    	getContentPane().removeAll();
    	currentView = view;
    	editorViewHandle = null;
    	switch (view) {
    	case HOME_VIEW:
    		add(new HomeView(this));
    		break;
    	case GAME_VIEW:
    		add(new GameView(this, model));
    		break;
    	case EDITOR_VIEW:
    		editorViewHandle = new EditorView(this,model);
    		add(editorViewHandle);
    		break;
    	case OPTIONS_VIEW:
    		add(new OptionsView(this, model));
    		break;
    	default:
    		break;
    	}
    	revalidate();
    	repaint();
    }
    /**
     * Update with data from the model.
     */
    public void update(Object observable) {
    	update(observable,"Model changed");
    }
    public void update(Object observable, String message) {
        repaint();
    }
}
