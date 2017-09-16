import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.regex.Pattern;

public class OptionsView extends JPanel {
	
	private ViewController vc;
	private Model model;
	
	public OptionsView (ViewController vc, Model model) {
    	super();
    	this.vc = vc;
    	this.model = model;
    	
    	setLayout(new BorderLayout());
		add(new OptionsMenu(),BorderLayout.PAGE_START);
		add(new OptionsArea(), BorderLayout.CENTER);
	}
	
	public void paintComponent (Graphics g) {
		super.paintComponent(g);
	}
	
	public class OptionsMenu extends JPanel {
		public OptionsMenu () {
			super();
			setBackground(Color.BLACK);	    
	    	setPreferredSize(new Dimension(0,vc.HEADER_HEIGHT));
	    	setLayout(new BorderLayout());
	    	
	    	JPanel leftButtons = new JPanel();
    		leftButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
    		leftButtons.setBackground(Color.BLACK);
    		leftButtons.setBorder(new EmptyBorder(5, 5, 5, 5));
    		add(leftButtons, BorderLayout.WEST);
	    	
	    	JButton homeButton = new JButton("Home");
	    	homeButton.addActionListener(new ActionListener () {
				public void actionPerformed(ActionEvent e) {
		            vc.selectView(ViewController.HOME_VIEW);
		        }
			});
	    	leftButtons.add(homeButton);
		}
		
		public void paintComponent (Graphics g) {
			super.paintComponent(g);
		}
	}
	public class OptionsArea extends JPanel {
		public OptionsArea () {
			super();
	    	setBackground(Color.WHITE);
	    	this.setLayout(new GridBagLayout());
	    	GridBagConstraints c = new GridBagConstraints();
	    	c.insets = new Insets(5,5,5,5);
	    	
	    	c.gridy = 0;
	    	JLabel fpsLabel = new JLabel ("Frames Per Second:");
	    	add(fpsLabel,c);
	    	
	    	final int FPS_MIN = 0;
	    	final int FPS_MAX = 60;
	    	final int FPS_INIT = vc.getFPS();    //initial frames per second

	    	JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL,
	    	                                      FPS_MIN, FPS_MAX, FPS_INIT);
	    	//Turn on labels at major tick marks.
	    	framesPerSecond.setMajorTickSpacing(10);
	    	framesPerSecond.setMinorTickSpacing(1);
	    	framesPerSecond.setPaintTicks(true);
	    	framesPerSecond.setPaintLabels(true);
	    	framesPerSecond.setBackground(Color.WHITE);
	    	
	    	Font font = new Font("Serif", Font.ITALIC, 15);
	    	framesPerSecond.setFont(font);
	    	framesPerSecond.addChangeListener(new FPSSliderListener());
	    	add(framesPerSecond,c);
	    	
	    	c.gridy = 1;
	    	JLabel scrollBlocksLabel = new JLabel ("Scroll Rate (Blocks/Milisecond)");
	    	add(scrollBlocksLabel,c);
	    	
	    	
	    	final int BLOCKS_MIN = 0;
	    	final int BLOCKS_MAX = 10000;
	    	final int BLOCKS_INIT = (int) (model.getScrollRate()*1000); 
	    	JSlider blocksPerSecond = new JSlider(JSlider.HORIZONTAL,
	    			BLOCKS_MIN, BLOCKS_MAX, BLOCKS_INIT);
	    	//Turn on labels at major tick marks.
	    	blocksPerSecond.setMajorTickSpacing(2500);
	    	blocksPerSecond.setMinorTickSpacing(1000);
	    	blocksPerSecond.setPaintTicks(true);
	    	blocksPerSecond.setPaintLabels(true);
	    	blocksPerSecond.setBackground(Color.WHITE);
	    	blocksPerSecond.setFont(font);
	    	blocksPerSecond.addChangeListener(new BlockSliderListener());
	    	add(blocksPerSecond,c);
	    	
	    	
		}

    	private class FPSSliderListener implements ChangeListener {
    		public FPSSliderListener() {super();}
    		public void stateChanged(ChangeEvent e) {
    	        JSlider source = (JSlider)e.getSource();
    	        if (!source.getValueIsAdjusting()) {
    	            int fps = (int)source.getValue();
    	            if (fps <= 0) fps = 1;
    	            vc.setFPS(fps);
    	        }    
    	    }
    	}
    	
    	private class BlockSliderListener implements ChangeListener {
    		public BlockSliderListener() {super();}
    		public void stateChanged(ChangeEvent e) {
    	        JSlider source = (JSlider)e.getSource();
    	        if (!source.getValueIsAdjusting()) {
    	            float scrollRate = (int)source.getValue();
    	            model.setScrollRate((float) scrollRate / 1000);
    	        }    
    	    }
    	}
		
		public void paintComponent (Graphics g) {
			super.paintComponent(g);
		}
	}
}