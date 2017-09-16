import java.awt.event.*;
import javax.swing.*;

// Finer mouse control is needed for the editor,
// so it is seperated from the ViewController

public class EditorMouseController extends MouseAdapter {
	EditorView view;
	public EditorMouseController(EditorView view) {
		super();
		this.view = view;
	}
	public void mouseClicked(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			view.mousePressed(e.getPoint());
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			view.rightButtonPressed(e.getPoint());
		}
	}
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			view.mouseReleased(e.getPoint());
		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}
	public void mouseDragged(MouseEvent e) {
		view.mouseDrag(e.getPoint());
	}
	public void mouseMoved(MouseEvent e) {
	}
	
}