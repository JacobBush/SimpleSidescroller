
import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import java.io.*;
import java.awt.event.*;
import javax.swing.Timer;

public class Model {
	// model data
	public static String defaultSaveDirectory = "saves/";
	public static String defaultCourse = "sample_level";
	private Course currentCourse;
	
	private Timer courseScrollTimer;
	
	// Setable constant
	private static float scrollRate = 2.5f; // Blocks per second - rate for calling Course.clockTick();
	public static void setScrollRate(float s) {
		scrollRate = s;
		// will need to reset timer
	}
	public static float getScrollRate() {return scrollRate;}
	
    /** The observers that are watching this model for changes. */
    private List<Observer> observers;
    
    public Model() {
        observers = new ArrayList();
        currentCourse = new Course(defaultSaveDirectory, defaultCourse);
    }
	
    // toString for usability
	@Override
	public String toString() {
		return currentCourse.toString();
	}
	
	// loading
	public void loadDefaultCourse () {
		currentCourse.loadDefaultCourse();
		notifyObservers("Default Course loaded: " + defaultSaveDirectory + defaultCourse);
	}
	private void loadCourse (String fileName) {
		currentCourse.loadCourse(fileName);
		notifyObservers("Course loaded :" + defaultSaveDirectory + fileName);
	}
	private void loadCourse (String fileName, String directory) {
		currentCourse.loadCourse(fileName,directory);
		notifyObservers("Course loaded :" + directory + fileName);
	}
	public void reloadCourse() {
		currentCourse.reloadCourse();
		notifyObservers("Course Reloaded");
	}
	
	public void restartCourse () {
		stop();
		reloadCourse();
		notifyObservers("Course Restarted");
		start();
	}
	
	public void setCourse (String c) {
		if (c == null) {
			defaultCourse = null;
		} else {
			defaultCourse = c;
			loadCourse (c);
		}
	}
	
	public void createBlankCourse () {
		this.currentCourse.createBlankCourse();
	}
	// getters
	public int[][] getCourse() {
		return currentCourse.getCourse();
	}
	public Dimension getCourseSize() {
		return currentCourse.getDimensions();
	}
	public String getCourseName () {
    	return currentCourse.getCourseName();
    }
	
	public int getGameState() {
		return currentCourse.getGameState();
	}
	
	public int getCourseDisplayBoundary() {
		return currentCourse.getCourseDisplayBoundary();
	}
	
	public Dimension getPlayerLocation () {
		return currentCourse.getPlayerLocation();
	}
	
    // course scrolling
    public void play () {
    	restartCourse();
    }
    
    public void pause( ) {
    	if (currentCourse.getGameState() == Course.RUNNING ||
				currentCourse.getGameState() == Course.IDLE) {
        	this.courseScrollTimer.stop();
        	currentCourse.setGameState(Course.PAUSED);
    	}
    }
    public void unpause( ) {
    	this.courseScrollTimer.start();
    	currentCourse.setGameState(Course.RUNNING);
    }
    
    private void setCourseScrollTimer () {
    	if (this.courseScrollTimer != null) courseScrollTimer.stop();
    	this.courseScrollTimer = new Timer((int)(1000/scrollRate), new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			if (currentCourse.getGameState() == Course.RUNNING ||
    				currentCourse.getGameState() == Course.IDLE) {
    				currentCourse.clockTick();
        			notifyObservers("CourseScrolledLeft");
    			} else { // victory or defeat
    				stop();
    				notifyObservers("GameEnd"+currentCourse.getGameState());
    			}
    		}
    	});
    }
	
    public void start () {
    	startScrolling();
    }
    public void stop () {
    	stopScrolling();
    }
    
	private void startScrolling () {
		setCourseScrollTimer();
    	this.courseScrollTimer.start();
				
	}
	private void stopScrolling () {
		if (courseScrollTimer != null) {
	    	this.courseScrollTimer.stop();
	    	this.courseScrollTimer = null;			
		}
	}
	
	// Player movement
	public void movePlayerLeft () {currentCourse.movePlayerLeft();notifyObservers("PlayerMovedLeft");}
	public void movePlayerRight () {currentCourse.movePlayerRight();notifyObservers("PlayerMovedRight");}
	public void movePlayerUp () {currentCourse.movePlayerUp();notifyObservers("PlayerMovedUp");}
	public void movePlayerDown () {currentCourse.movePlayerDown();notifyObservers("PlayerMovedDown");}
	
	// Course Editing
	public void addTileToCourse(Point p, int type) {
		this.currentCourse.addTile(p, type);
	}
	
	public int getTile(Point p) {
		return this.getCourse()[p.x][p.y];
	}
	
	public boolean saveCourse () {
		return this.currentCourse.saveCourse();
	}
	
	public boolean saveCourse (String fileName) {
		return this.currentCourse.saveCourse(fileName);
	}
	
	public boolean saveCourse (String directory, String fileName) {
		return this.currentCourse.saveCourse(directory, fileName);
	}
	
	public void updateCourseLength(String courseLength) {
		this.currentCourse.updateCourseLength(courseLength);
		notifyObservers();
	}
		
    /**
     * Add an observer to be notified when this model changes.
     */
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    /**
     * Remove an observer from this model.
     */
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    // Notify all observers that the model has changed.
    
    public void notifyObservers(String message) {
    	for (Observer observer: observers) {
            observer.update(this,message);
        }
    }
     
    public void notifyObservers() {
        notifyObservers("Model changed");
    }

    
}
