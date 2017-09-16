import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import javax.swing.text.MaskFormatter;

public class EditorView extends JLayeredPane {
	
    private static final int HEADER_HEIGHT = 50;
    private static final int SIDEBAR_WIDTH = 120;
	
	private ViewController vc;
	private EditorContent content;
	private Model model;
	
	// draw dragging
	private EditorDragComponent dragComponent;
	private boolean courseDragging;
	private Point dragStartLoc;
	
	// animation
	private boolean repaintRequired = false;
	private Timer animationTimer;
	
	// resizing
	private ArrayList<SelectableTile> selectableTiles;
	
	public EditorView (ViewController vc, Model model) {
    	super();
    	this.vc = vc;
    	this.model = model;
    	model.reloadCourse();
    	dragComponent = null;
    	courseDragging = false;
    	dragStartLoc = null;
    	selectableTiles = new ArrayList<SelectableTile>();
    	
    	content = new EditorContent();
    	content.setBounds(0, 0, vc.getWidth(), vc.getHeight());
    	add(content, JLayeredPane.DEFAULT_LAYER);
    	
    	resizeSelectableTiles();
    	
    	// Mouse Listeners
    	EditorMouseController e = new EditorMouseController (this);
    	addMouseListener(e);
    	addMouseMotionListener(e);    	
    	
    	animationTimer = new Timer (1000/vc.getFPS(), new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			if (repaintRequired) {
        			repaint();
        			repaintRequired = false;
    			}
    		}
    	});
    	animationTimer.start();
	}
	
	void updateComponentSize() {
		content.setBounds(0, 0, vc.getWidth(), vc.getHeight());
		resizeSelectableTiles();
		revalidate();
		repaintRequired=true;
	}
	
	private void resizeSelectableTiles() {
		for (SelectableTile tile: selectableTiles) {
			tile.setPreferredSize(new Dimension(vc.getPixelsPerUnit(),vc.getPixelsPerUnit()));
			tile.setMinimumSize(new Dimension(vc.getPixelsPerUnit(),vc.getPixelsPerUnit()));
			tile.setMaximumSize(new Dimension(vc.getPixelsPerUnit(),vc.getPixelsPerUnit()));
		}
	}
	
	public void paintComponent (Graphics g) {
		super.paintComponent(g);
		
	}
	
	// Mouse Listener Functions
	
	
	public void mousePressed (Point p) {
		
		if (dragComponent != null) return;
		Component c = this.findComponentAt(p);
		SelectableTile tile = (c instanceof SelectableTile ? (SelectableTile)c : null);
		if (tile != null) {
			dragComponent = new EditorDragComponent(p, tile.type);
			add(dragComponent, JLayeredPane.DRAG_LAYER);
			revalidate();
			repaint();
		}
		
		Point tilePosn = content.getGameArea().getTileAtPosn(p);
		if (tilePosn != null) {
			int type = this.model.getTile(tilePosn);
			if (type == Course.PLAYER_TILE || type == Course.OBSTACLE_TILE) {
				this.model.addTileToCourse(tilePosn, Course.EMPTY_TILE);
				dragComponent = new EditorDragComponent(p, type);
				add(dragComponent, JLayeredPane.DRAG_LAYER);
				repaintRequired=true;
			} else {
				courseDragging = true;
				dragStartLoc = p;
			}
		}
	}
	
	public void rightButtonPressed (Point p) {
		if (dragComponent != null) {
			Point tilePosn = content.getGameArea().getTileAtPosn(p);
			if (tilePosn != null) {
				this.model.addTileToCourse(tilePosn, this.dragComponent.type);
			}
		}
	}
	
	public void mouseReleased (Point p) {
		if (dragComponent == null) {
			if (courseDragging) {
				courseDragging = false;				
			}
			return;
		}
		
		Point tilePosn = content.getGameArea().getTileAtPosn(p);
		if (tilePosn != null) {
			//System.out.println("(" + tilePosn.x + ", " + tilePosn.y + ")");
			this.model.addTileToCourse(tilePosn, this.dragComponent.type);
		}
		
		this.remove(dragComponent);
		dragComponent = null;
		this.revalidate();
		repaint();
	}
	
	public void mouseDrag (Point p) {
		if (dragComponent == null) {
			if (courseDragging) {
				content.getGameArea().moveView(dragStartLoc, p);
				dragStartLoc = p;
			}
		} else {
			dragComponent.changeLoc(p);
			repaintRequired=true;
		}
	}
	// classes and stuff
	public class EditorDragComponent extends JPanel {
		public final int type;
		public EditorDragComponent (Point p, int type) {
			super();
			changeLoc(p);
			this.type = type;
			if (type == Course.PLAYER_TILE) {
				this.setBackground(Color.red);
			} else if (type == Course.OBSTACLE_TILE) {
				this.setBackground(Color.gray);
			}
		}
		public void paintComponent (Graphics g) {
			super.paintComponent(g);
		}
		public void changeLoc (Point p) {
			int pix = vc.getPixelsPerUnit();
			setBounds(p.x - pix/2,p.y - pix/2, pix, pix);
		}
	}
	
	public class EditorContent extends JPanel {

		private EditorMenu menu;
		private EditorSidebar sidebar;
		private EditorArea gameArea;
		//private EditorAreaWrapper gameAreaWrapper;
		
		public EditorArea getGameArea() {
			return gameArea;
			//return gameAreaWrapper.getGameArea();
		}
		
		public EditorContent() {
			super();
	    	setLayout(new BorderLayout());
	    	menu = new EditorMenu();
	    	sidebar = new EditorSidebar();
	    	gameArea = new EditorArea();
	    	//gameAreaWrapper = new EditorAreaWrapper();
	    	add(menu, BorderLayout.NORTH);
	    	add(sidebar, BorderLayout.WEST);
	    	add(gameArea, BorderLayout.CENTER);
	    	
	    	
	    	//JScrollPane scrollPane = new JScrollPane(gameArea);
	    	//scrollPane.setViewportView(gameArea);
	    	//add(scrollPane, BorderLayout.CENTER);
	    	//add(gameAreaWrapper, BorderLayout.CENTER);
	    	//System.out.println(gameArea.getSize().width + ", " + gameArea.getSize().height);
		}
		public void paintComponent (Graphics g) {
			super.paintComponent(g);
		}
	}
	
	public class EditorMenu extends JPanel {
    	public EditorMenu () {
    		super();
    		setLayout(new BorderLayout());
    		setPreferredSize(new Dimension(0, HEADER_HEIGHT));
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
    		
    		JPanel centerPanel = new JPanel ();
    		centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    		centerPanel.setBackground(Color.BLACK);
    		centerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    		add(centerPanel, BorderLayout.CENTER);
    		        		
    		JButton b = new JButton("Home");
    		b.addActionListener(new ActionListener () {
    			public void actionPerformed(ActionEvent e) {
    				vc.selectView(ViewController.HOME_VIEW);
    	        }
    		});
    		b.setAlignmentX(LEFT_ALIGNMENT);
    		leftButtons.add(b);
    		
    		JLabel courseLengthLabel = new JLabel ("Course Length: ");
    		courseLengthLabel.setForeground(Color.white);
    		centerPanel.add(courseLengthLabel);
    		
    		String[] courseLengthStrings = { "Short", "Medium", "Long"};
    		JComboBox courseLengthList = new JComboBox(courseLengthStrings);
    		switch(model.getCourseSize().width) {
    		case Course.SHORT_LENGTH:
        		courseLengthList.setSelectedIndex(0);
    			break;
    		case Course.MEDIUM_LENGTH:
        		courseLengthList.setSelectedIndex(1);
    			break;
    		case Course.LONG_LENGTH:
        		courseLengthList.setSelectedIndex(2);
    			break;
    		default:
        		courseLengthList.setSelectedIndex(1);
    			break;
    		}
    		courseLengthList.addActionListener(new ActionListener () {
    			public void actionPerformed(ActionEvent e) {
    				JComboBox cb = (JComboBox)e.getSource();
    		        String courseLength = (String)cb.getSelectedItem();
    		        model.updateCourseLength(courseLength);
    	        }
    		});
    		centerPanel.add(courseLengthList);
    		
    		centerPanel.add(Box.createRigidArea(new Dimension(5,0))); // hspace
    		
    		JLabel courseNameLabel = new JLabel ("Course Name: ");
    		courseNameLabel.setForeground(Color.white);
    		centerPanel.add(courseNameLabel);
    		
    		JTextField courseNameField = new JTextField(10);
    		courseNameField.setText(model.getCourseName());
    		centerPanel.add(courseNameField);
    		
    		centerPanel.add(Box.createRigidArea(new Dimension(5,0))); //hspace
    		
    		JButton saveCourseButton = new JButton ("Save");
    		saveCourseButton.addActionListener(new ActionListener () {
    			public void actionPerformed(ActionEvent e) {
    				String fn = courseNameField.getText();
    				if (validFileName(fn)) {
        				if (model.saveCourse(fn)) {
        					JOptionPane.showMessageDialog(vc,
        						    "Level " + fn + " saved successfully!",
        						    "Saved Successfully",
        						    JOptionPane.INFORMATION_MESSAGE);
        				} else {
        					JOptionPane.showMessageDialog(vc,
        						    "An error occured while trying to save the file.",
        						    "Save Error",
        						    JOptionPane.ERROR_MESSAGE);
        				}
        				
    				} else {
    					JOptionPane.showMessageDialog(vc,
						    "Please specify a valid file name.",
						    "Invalid File Name",
						    JOptionPane.WARNING_MESSAGE);  					
    				}
    	        }
    		});    		
    		centerPanel.add(saveCourseButton);
    		
    		JButton blankCourseButton = new JButton ("Make Blank Course");
    		blankCourseButton.addActionListener(new ActionListener () {
    			public void actionPerformed(ActionEvent e) {
    				model.createBlankCourse();
    				courseLengthList.setSelectedIndex(1);
    				courseNameField.setText("");
    				revalidate();
    				repaint();
    				content.repaint();
    	        }
    		});    		
    		centerPanel.add(Box.createRigidArea(new Dimension(20,0))); //hspace
    		centerPanel.add(blankCourseButton);
    		revalidate();
    	}
    	public boolean validFileName (String fileName) {
    		return fileName != null && !fileName.trim().isEmpty();
    	}
    	public void paintComponent (Graphics g) {
    		super.paintComponent(g);
    	}
    	protected MaskFormatter createFormatter(String s) {
		    MaskFormatter formatter = null;
		    try {
		        formatter = new MaskFormatter(s);
		    } catch (java.text.ParseException exc) {
		        System.err.println("formatter is bad: " + exc.getMessage());
		        System.exit(-1);
		    }
		    return formatter;
		}  	
    }
	public class EditorSidebar extends JPanel {
    	public EditorSidebar () {
    		super();
    		setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
    		setBackground(Color.BLACK);
 
    		setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets (2,2,2,2);
            gbc.weighty = 0;
            
            gbc.gridy = 0;
            JLabel l = new JLabel ("Player:");
    		l.setForeground(Color.white);
    		add(l,gbc);
    		gbc.gridy = 1;
    		add(new SelectableTile(Course.PLAYER_TILE),gbc);
    		
    		gbc.gridy = 2;
    		l = new JLabel ("Obstacle:");
    		l.setForeground(Color.white);
    		add(l,gbc);
    		gbc.gridy = 3;
    		add(new SelectableTile(Course.OBSTACLE_TILE),gbc);
    		
    		gbc.gridy = 4;
            JLabel text = new JLabel("<html><pre>Use the arrows\n or drag the\ncourse with the\nmouse to pan the\ncamera.\n\n"
            		+ "Drag and drop to\nmove player and\nobstacle tiles.\nWhile dragging,\npress the right\nmouse button to\nmake a copy.</pre></html>");
            text.setForeground(Color.WHITE);
            add(text, gbc);
    		
    		gbc.gridy = 5;
    		gbc.weighty = 1;
    		add(Box.createRigidArea(new Dimension(0,0)),gbc);
    	}
    	public void paintComponent (Graphics g) {
    		super.paintComponent(g);
    	}    	
    }
	public class SelectableTile extends JPanel {
		public final int type;
		public SelectableTile (int type) {
			super();
			this.setPreferredSize(new Dimension(vc.getPixelsPerUnit(),vc.getPixelsPerUnit()));
			this.setMinimumSize(new Dimension(vc.getPixelsPerUnit(),vc.getPixelsPerUnit()));
			this.setMaximumSize(new Dimension(vc.getPixelsPerUnit(),vc.getPixelsPerUnit()));
			this.type = type;
			if (type == Course.PLAYER_TILE) {
				this.setBackground(Color.red);
			} else if (type == Course.OBSTACLE_TILE) {
				this.setBackground(Color.gray);
			}
			selectableTiles.add(this); // for resizing
		}
		public void paintComponent (Graphics g) {
			super.paintComponent(g);
		}
	}
	
	public class EditorArea extends JPanel {
		
		private int xPixelOffset;
		private int yPixelOffset;
		private int scrollSpeed;
		
		public EditorArea () {
    		super();
    		setBackground(Color.BLACK);
    		xPixelOffset = 0;
    		yPixelOffset = 0;
    		scrollSpeed = 10;
    		
        	InputMap im = getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am =getActionMap();
            
    		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RightArrow");
            //im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "RightArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LeftArrow");
            //im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "LeftArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UpArrow");
            //im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "UpArrow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DownArrow");
            //im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "DownArrow");

            am.put("RightArrow", new ArrowAction("RightArrow"));
            am.put("LeftArrow", new ArrowAction("LeftArrow"));
            am.put("UpArrow", new ArrowAction("UpArrow"));
            am.put("DownArrow", new ArrowAction("DownArrow"));
    		
    	}
		
		public class ArrowAction extends AbstractAction {

		    private String cmd;

		    public ArrowAction(String cmd) {
		        this.cmd = cmd;
		    }

		    @Override
		    public void actionPerformed(ActionEvent e) {
		        if (cmd.equalsIgnoreCase("LeftArrow")) {
		        	moveViewHorizontal(-scrollSpeed);
					repaint();
		        } else if (cmd.equalsIgnoreCase("RightArrow")) {
		        	moveViewHorizontal(scrollSpeed);
					repaint();
		        } else if (cmd.equalsIgnoreCase("UpArrow")) {
		        	moveViewVertical(-scrollSpeed);
					repaint();
    	        } else if (cmd.equalsIgnoreCase("DownArrow")) {
    	        	moveViewVertical(scrollSpeed);
    				repaint();
    	        }
		    }
		}
		
		public void moveView(Point startLoc, Point endLoc) {
			int xDiff = startLoc.x - endLoc.x;
			int yDiff = startLoc.y - endLoc.y;
			moveViewHorizontal(xDiff);
			moveViewVertical(yDiff);
			repaint();
		}
		
		public void moveViewHorizontal(int pixels) {
			xPixelOffset += pixels;
		}
		public void moveViewVertical(int pixels) {
			yPixelOffset += pixels;
		}
		
		
    	public void paintComponent (Graphics g) {
    		super.paintComponent(g);
    		int[][] course = model.getCourse();
    		Dimension size = model.getCourseSize();
    		for (int x = 0; x < size.width; x++) {
    			for (int y = 0; y < size.height; y++) {
    				paintUnit(g, x, y, course[x][y], xPixelOffset, yPixelOffset);
    			}
    		}
    	}
    	public Point getTileAtPosn (Point p) {
    		// will return a point corresponding to a tile, else null
    		if (p.x < SIDEBAR_WIDTH || p.y < HEADER_HEIGHT) return null; // need to be in course areadd
    		float x = (p.x - this.getX() + xPixelOffset)/(float) vc.getPixelsPerUnit();
    		float y = (p.y - this.getY() + yPixelOffset)/(float) vc.getPixelsPerUnit();
    		if (x >= 0 && y >= 0 && x < model.getCourseSize().width && y < model.getCourseSize().height) {
    			return new Point((int) x, (int) y);
    		} else {
    			return null;
    		}
    	}
    	private void paintUnit(Graphics g, float x, float y, int spec, int xOffset, int yOffset) {
    		// offset should be positve.
    		int width = vc.getPixelsPerUnit();
    		int height = width;
    		switch (spec) {
    		case Course.EMPTY_TILE:
    			g.clearRect((int) (x*width) - xOffset, (int) (y*height) - yOffset, width, height);
    			break;
    		case Course.PLAYER_TILE:
    			g.setColor(Color.RED);
    			g.fillRect((int) (x*width) - xOffset, (int) (y*height) - yOffset, width, height);
    			break;
    		case Course.OBSTACLE_TILE:
    			g.setColor(Color.GRAY);
    			g.fillRect((int) (x*width) - xOffset, (int) (y*height) - yOffset, width, height);
    			break;
    		case Course.VICTORY_TILE:
    			g.setColor(Color.GREEN);
    			g.fillRect((int) (x*width) - xOffset, (int) (y*height) - yOffset, width, height);
    			break;
    		default:
    			break;
    		}
    	}
	}	
}