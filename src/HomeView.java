import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.util.regex.Pattern;
import java.awt.geom.AffineTransform;

public class HomeView extends JPanel {
	
    private static final int HEADER_HEIGHT = 50;
    private static final int DEFAULT_ANIMATION_SCALE = 30;
    private static final int DEFAULT_MIN_ANIMATION_LOCATION = 25;
    
    
	private ViewController vc;
	
	private Point animationLocation;
	private double animationRotation;
	private double animationScale;
	private int rotationDirection;
	
	private double animationRotationsPerSecond;
	private double animationTranslationSpeed;
	
	public HomeView (ViewController vc) {
    	super();
    	
    	this.vc = vc;
    	
    	animationLocation = new Point(DEFAULT_MIN_ANIMATION_LOCATION,DEFAULT_MIN_ANIMATION_LOCATION);
    	animationRotation = 0.0d;
    	animationScale = 1.0d;
    	animationRotationsPerSecond = 0.5d;
    	animationTranslationSpeed = 150.0d;
    	rotationDirection = 1;
    	
    	setLayout(new GridBagLayout());
    	//setBorder(new MatteBorder(50, 50, 50, 50, Color.BLACK));
    	setBackground(Color.WHITE);
    	
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.insets = new Insets(5,5,5,5);
    	
    	gc.gridy = 0;
    	gc.gridx = 0;
    	File[] files =  finder(Model.defaultSaveDirectory);
    	String[] fileNames = new String[files.length];
    	for (int i = 0; i < files.length; i++) {
    		File file = files[i];
    		fileNames[i] = getFileName(file.toString());
    	}
    	if (files.length > 0)
    		vc.model.setCourse(fileNames[0]);
    	else
    		vc.model.setCourse(null);
    	
    	JLabel courseSelectLabel = new JLabel ("Course:");
		courseSelectLabel.setForeground(Color.BLACK);
		add(courseSelectLabel,gc);
		
		gc.gridx = 1;
		if (fileNames.length == 0) {
			fileNames = new String[1];
		}
		JComboBox courseSelectList = new JComboBox(fileNames);
    	courseSelectList.setSelectedIndex(0);
    	courseSelectList.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
		        String selectedCourse = (String)cb.getSelectedItem();
		        vc.model.setCourse(selectedCourse);
	        }
		});
		add(courseSelectList,gc);
    	
		gc.gridy = 1;
		gc.gridwidth = 2;
		gc.gridx = 0;
    	JButton b = new JButton("Play");
		b.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
	            vc.selectView(ViewController.GAME_VIEW);
	        }
		});
		b.setAlignmentX(CENTER_ALIGNMENT);
		add(b,gc);
		
		//add(Box.createRigidArea(new Dimension(0,5)), gc);
		gc.gridy = 2;
		b = new JButton("Edit Level");
		b.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
	            vc.selectView(ViewController.EDITOR_VIEW);
	        }
		});
		b.setAlignmentX(CENTER_ALIGNMENT);
		add(b,gc);
		
		gc.gridy = 3;
		b = new JButton("Options");
		b.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
	            vc.selectView(ViewController.OPTIONS_VIEW);
	        }
		});
		b.setAlignmentX(CENTER_ALIGNMENT);
		add(b,gc);
		
		Timer animationTimer = new Timer (1000/vc.getFPS(), new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			handleBorderAnimation(vc.getFPS());
    			repaint();
    		}
    	});
		animationTimer.start();
	}
	
	public File[] finder(String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() { 
                 public boolean accept(File dir, String filename)
                      {return filename.endsWith(".txt");}
        } );

    }
	
	public String getFileName (String filePath) {
		String[] parts = filePath.split(Pattern.quote("/"));
		filePath = parts[parts.length-1];
		parts = filePath.split(Pattern.quote("\\"));
		filePath = parts[parts.length-1];
		parts = filePath.split(Pattern.quote(".txt"));
		return parts[0];
	}	
	
	private int currentX = 1;
	private int currentY = 0;
	public void handleBorderAnimation (int frameRate) {
		animationRotation += 2 * Math.PI * rotationDirection *  animationRotationsPerSecond / frameRate;
		while (animationRotation >= 2 * Math.PI) animationRotation -= 2 * Math.PI;
		while (animationRotation <= -2 * Math.PI) animationRotation += 2 * Math.PI;
		
		int screenWidth = this.getWidth();
		int screenHeight = this.getHeight();
		animationLocation.x += (int)(currentX * animationTranslationSpeed / frameRate);
		animationLocation.y += (int)(currentY * animationTranslationSpeed / frameRate);
		
		if (currentX == 1 && screenWidth - animationLocation.x < DEFAULT_MIN_ANIMATION_LOCATION) {
			currentX = 0;
			animationLocation.x = screenWidth - DEFAULT_MIN_ANIMATION_LOCATION;
			currentY = 1;
			rotationDirection = -rotationDirection;
		}
		if (currentY == 1 && screenHeight - animationLocation.y < DEFAULT_MIN_ANIMATION_LOCATION) {
			currentY = 0;
			animationLocation.y = screenHeight - DEFAULT_MIN_ANIMATION_LOCATION;
			currentX = -1;
			rotationDirection = -rotationDirection;
		}
		if (currentX == -1 && animationLocation.x < DEFAULT_MIN_ANIMATION_LOCATION) {
			currentX = 0;
			animationLocation.x = DEFAULT_MIN_ANIMATION_LOCATION;
			currentY = -1;
			rotationDirection = -rotationDirection;
		}
		if (currentY == -1 && animationLocation.y < DEFAULT_MIN_ANIMATION_LOCATION) {
			currentY = 0;
			animationLocation.y = DEFAULT_MIN_ANIMATION_LOCATION;
			currentX = 1;
			rotationDirection = -rotationDirection;
		}
		
	}
	
	public void paintAnimationComponent(Graphics2D g2) {
		AffineTransform saveXform = g2.getTransform();
		g2.setColor(Color.RED);
		g2.translate(animationLocation.x, animationLocation.y);
		g2.rotate(animationRotation);
		int width = (int) (30*animationScale);
		int height = (int) (30*animationScale);
		g2.fill(new Rectangle(-width/2, -height/2, width, height));
		g2.setColor(Color.BLACK);
		g2.fill(new Rectangle(-width/3, -height/3, 2*width/3, 2*height/3));
		g2.setTransform(saveXform);
	}
	
	public void paintComponent (Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.BLACK);
		g2.fill(new Rectangle(0,0,this.getWidth(),50));
		g2.fill(new Rectangle(0,this.getHeight()-50,this.getWidth() ,50));
		g2.fill(new Rectangle(0,0,50 ,this.getHeight()));
		g2.fill(new Rectangle(this.getWidth()-50,0,50 ,this.getHeight()));
		paintAnimationComponent(g2);
	}
}