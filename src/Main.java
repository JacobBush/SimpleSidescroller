import javax.swing.*;

public class Main {
    public static void main(String[] args) {
    	SwingUtilities.invokeLater(new Runnable() {
    		@Override
    		public void run() {
    			runProgram();
    		}
    	});        
    }
    
    private static void runProgram () {
    	Model model = new Model();
    	ViewController view = new ViewController(model);
    }
}
