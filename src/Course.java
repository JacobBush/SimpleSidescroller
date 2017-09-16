import java.awt.Dimension;
import java.util.*;
import java.io.*;
import javax.swing.Timer;
import java.awt.event.*;
import java.awt.Point;

public class Course {
	
	public static final int EMPTY_TILE = 0;
	public static final int PLAYER_TILE = 1;
	public static final int OBSTACLE_TILE = 2;
	public static final int VICTORY_TILE = 3;
	
	public static final int RUNNING = 0;
	public static final int VICTORY = 1;
	public static final int DEFEAT = 2;
	public static final int IDLE = 3;
	public static final int PAUSED = 4;
	
	public static final int SHORT_LENGTH = 30;
	public static final int MEDIUM_LENGTH = 70;
	public static final int LONG_LENGTH = 110;
	
	public static final int DEFAULT_HEIGHT = 10;
	
    private int[][] course;
    private Dimension dimensions;
    private Dimension playerLoc;
    
    private int leftBlock;
    
    private final String defaultSaveDirectory;
    private final String defaultSaveFileName;
    
    private String saveDirectory;
    private String saveFileName;
    
	private Timer scrollTimer;
	
	private int gameState = IDLE;
	public int getGameState () {return gameState;}
    
    // constructors
    public Course() {
    	this("saves/","sample_level");
    }
    
    public Course(String defaultSaveDirectory) {
    	this(defaultSaveDirectory,"sample_level");
    }
    
    public Course(String defaultSaveDirectory, String defaultSaveFileName) {
    	dimensions = new Dimension(0,0);
    	course = new int[0][0];
    	playerLoc = null;
    	this.defaultSaveDirectory = defaultSaveDirectory;
    	this.defaultSaveFileName = defaultSaveFileName;
    	this.saveDirectory = "";
    	this.saveFileName = "";
    	leftBlock = 0;
    }
    
    // getters
    
    public Dimension getPlayerLocation () {
    	return playerLoc;
    }
    
    public int[][] getCourse () {
    	return course;
    }
    public Dimension getDimensions() {
    	return dimensions;
    }
    
    public String getCourseName () {
    	return saveFileName;
    }
    
    // loaders
    public void loadDefaultCourse () {
    	loadCourse (defaultSaveFileName, defaultSaveDirectory);
    }
    
    public void loadCourse (String fileName) {
    	loadCourse (fileName, defaultSaveDirectory);
    }
    
    private ArrayList<Integer> parseInts(String s) {
    	ArrayList<Integer> ints = new ArrayList<Integer>();
    	int p1 = 0;
    	int p2 = 0;
    	while (p1 != s.length()) {
    		if (s.charAt(p1) == ',') {
    			String number = s.substring(p2,p1);
    			ints.add(Integer.valueOf(number));
    			p2 = p1 + 1;
    		} else if (s.charAt(p1) == ' ') {
    			if (p1 == p2) {
    				p2++;
    			} else {
    				String number = s.substring(p2,p1);
        			ints.add(Integer.valueOf(number));
        			p2 = p1 + 1;
    			}
    		}
    		p1++;
    	}
    	if (p1 != p2) {
    		String number = s.substring(p2,p1);
			ints.add(Integer.valueOf(number));
    	}
    	return ints;
    }
	
	public void loadCourse (String filename, String directory) {
		saveDirectory = directory;
		saveFileName = filename;
        gameState = IDLE;

		leftBlock = 0;
		String fileName = directory + filename + ".txt";
        String l = null;
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            while((l = br.readLine()) != null) {
            	if (l.charAt(0) == '#') {
            		// specifies a comment
            	} else if (l.charAt(0) == 'd') { 
            		// specifies course dimensions
            		List<Integer> ints = parseInts(l.substring(1));
            		if (ints.size() == 2) {
            			dimensions = new Dimension (ints.get(0), ints.get(1));
            			course = new int[LONG_LENGTH][dimensions.height];
            		} else {
            			System.out.println("Dimension line in save file must be a pair of integers");
            			break;
            		}
            	} else if (l.charAt(0) == 'p') {
            		// specifies player starting location
            		List<Integer> ints = parseInts(l.substring(1));
            		if (ints.size() == 2) {
            			int x = ints.get(0);
            			int y = ints.get(1);
            			if (x < 0 || x >= dimensions.width || y < 0 || y >= dimensions.height) {
                			System.out.println("Player coordiantes must be >= 0 and <= the specified course dimensions.");
                			break;            				
            			}
            			loadPlayer(x,y);
            		} else {
            			System.out.println("Player line in save file must be a pair of integers");
            			break;
            		}
            	} else if (l.charAt(0) == 'o') {
            		// specifies an obstacle
            		List<Integer> ints = parseInts(l.substring(1));
            		if (ints.size() == 4) {
            			int x1 = ints.get(0);
            			int y1 = ints.get(1);
            			int x2 = ints.get(2);
            			int y2 = ints.get(3);
            			if (x1 < 0 || x1 >= dimensions.width || y1 < 0 || y1 >= dimensions.height
            					|| x2 < 0 || x2 >= dimensions.width || y2 < 0 || y2 >= dimensions.height) {
                			System.out.println("Obstacles coordiantes must be >= 0 and <= the specified course dimensions.");
                			break;            				
            			}
            			loadObstacle(x1, y1, x2, y2);
            		} else {
            			System.out.println("Obstacle line in save file must be 4 integers");
            			break;
            		}
            	} else {
            		System.out.println("Invalid line annotation in save file: " + l.charAt(0));
            		break;
            	} 
            }  
            setEndRowToVictory();
            if (br != null) br.close();
            if (fr != null) fr.close();
        }
        catch(FileNotFoundException ex) {
        	createBlankCourse();
        }
        catch(IOException ex) {System.out.println("Error reading file '"+ fileName + "'");}
	}
	
	public void reloadCourse() {
		loadCourse (saveFileName, saveDirectory);
	}
	
	public void createBlankCourse() {
		this.dimensions = new Dimension(this.MEDIUM_LENGTH, this.DEFAULT_HEIGHT);
		this.course = new int[this.dimensions.width][this.dimensions.height];
		this.setEndRowToVictory();
		this.loadPlayer(2, 5);
		this.saveDirectory = this.defaultSaveDirectory;
		this.saveFileName = this.defaultSaveFileName;
		//this.saveCourse(this.defaultSaveDirectory, this.defaultSaveFileName);
	}
	
	private void loadPlayer(int x, int y) {
		course[x][y] = PLAYER_TILE;
		playerLoc = new Dimension(x,y);
	}
	
	private void loadObstacle (int topLeftX, int topLeftY, int botRightX, int botRightY) {
		for (int i = topLeftX; i <= botRightX; i++) {
			for (int j = topLeftY; j <= botRightY; j++) {
				course[i][j] = OBSTACLE_TILE;
			}
		}
	}
	private void setEndRowToVictory() {
		for (int y = 0; y < dimensions.height; y++) {
				course[dimensions.width-1][y] = VICTORY_TILE;
		}
	}
	private void removeEndRowVictory() {
		for (int y = 0; y < dimensions.height; y++) {
			course[dimensions.width-1][y] = EMPTY_TILE;
		}
	}
	
	
	// savers
	
	public boolean saveCourse () {
		return this.saveCourse(this.defaultSaveFileName);
	}
	
	public boolean saveCourse (String fileName) {
		return this.saveCourse(this.defaultSaveDirectory, fileName);
	}
	
	public boolean saveCourse (String directory, String fileName) {
		if (fileName != "") {
			String saveFile = directory + fileName + ".txt";
			String content = getSaveFileContent();
			FileWriter fw = null;
            BufferedWriter bw = null;
            try {
    			fw = new FileWriter(saveFile);
    			bw = new BufferedWriter(fw);
    			bw.write(content);
    		} catch (IOException e) {
    			e.printStackTrace();
    			return false;
    		} finally {
    			try {
    				if (bw != null)	bw.close();
    				if (fw != null) fw.close();
    				return true;
    			} catch (IOException ex) {
    				ex.printStackTrace();
    				return false;
    			}
    		}   
		} else {
			return false;
		}
	}
	
	private String getSaveFileContent() {		
		String content = "";
		content += "d " + dimensions.width + ", " + dimensions.height + "\n";
		content += "p " + playerLoc.width + ", " + playerLoc.height + "\n";
		for (int i = 0; i < dimensions.width; i++) {
			for (int j = 0; j < dimensions.height; j++) {
				if (this.course[i][j] == OBSTACLE_TILE) {
					content += "o " + i + " " + j + " " + i + " " + j + "\n";
				}
			}
		}
		return content;
	}
	
	// Course Scrolling
	public void clockTick () {
		if (gameState == IDLE) {
			gameState = RUNNING;
		}
		if (gameState == RUNNING) {
			leftBlock = leftBlock + 1;
			if (playerLoc.width <= leftBlock) endGame(DEFEAT);	
		} else {
			// do nothing - clockTick called in invalid state
		}
	}
	
	public int getCourseDisplayBoundary() {
		return leftBlock;
	}
	
	public void endGame (int gameState) {
		if (this.gameState == RUNNING) {
			this.gameState = gameState;
		} else {
			System.out.println("Course.endGame called with invalid gameState");
		}
	}
	
	public void setGameState (int state) {
		this.gameState = state;
	}
	// Interaction
	public void movePlayerLeft () {movePlayerToTile(playerLoc.width - 1,playerLoc.height);}
	public void movePlayerRight () {movePlayerToTile(playerLoc.width + 1,playerLoc.height);}
	public void movePlayerUp () {movePlayerToTile(playerLoc.width,playerLoc.height - 1);}
	public void movePlayerDown () {movePlayerToTile(playerLoc.width,playerLoc.height + 1);}
	
	private void movePlayerToTile(int x, int y) {
		if (gameState != RUNNING && gameState != IDLE) return;
		if (validTile(x, y)) {
			switch(course[x][y]) {
			case (EMPTY_TILE):
				course[playerLoc.width][playerLoc.height] = EMPTY_TILE;
				course[x][y] = PLAYER_TILE;
				playerLoc = new Dimension(x,y);
				if (x <= leftBlock) {
					endGame(DEFEAT);
				}
			case (PLAYER_TILE):
				break;
			case (OBSTACLE_TILE):
				course[playerLoc.width][playerLoc.height] = EMPTY_TILE;
				course[x][y] = PLAYER_TILE;
				playerLoc = new Dimension(x,y);
				endGame(DEFEAT);
				break;
			case (VICTORY_TILE):
				course[playerLoc.width][playerLoc.height] = EMPTY_TILE;
				course[x][y] = PLAYER_TILE;
				playerLoc = new Dimension(x,y);
				endGame(VICTORY);
				break;
			default:
				System.out.println("Unknown tile type in Course.movePlayerToTile");
				break;
			}
		}
	}
	
	private boolean validTile(int x, int y) {
		if (x < leftBlock) {endGame(DEFEAT); return false;}
		if (x >= 0 && x < dimensions.width && y >= 0 && y < dimensions.height) return true;
		else return false;
	}
	private boolean validTile(Dimension d) {
		return validTile(d.width,d.height);
	}
	
	// Editing
	
	public void addTile(Point p, int type) {
		if (course[p.x][p.y] == VICTORY_TILE) return; // victory tiles shouldnt be edited. (They aren't saved)
		if (type == PLAYER_TILE) {
			if (playerLoc != null) course[playerLoc.width][playerLoc.height] = EMPTY_TILE;
			loadPlayer(p.x,p.y);
		} else {
			course[p.x][p.y] = type;
		}
	}
	
	public void updateCourseLength(String courseLength) {
		//private int[][] course;
	    //private Dimension dimensions;
		removeEndRowVictory();
		switch(courseLength) {
		case("Short"):
			this.dimensions.width = SHORT_LENGTH;
			break;
		case("Medium"):
			this.dimensions.width = MEDIUM_LENGTH;
			break;
		case("Long"):
			this.dimensions.width = LONG_LENGTH;
			break;
		default:
			break;
		}
		setEndRowToVictory();
	}
	
	
	// Utility
	
	@Override
	public String toString() {
		return "PlayerLoc: (" + playerLoc.width + ", " + playerLoc.height + ")";
	}
}
