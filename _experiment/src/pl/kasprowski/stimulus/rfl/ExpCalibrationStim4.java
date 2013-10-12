package pl.kasprowski.stimulus.rfl;

import java.awt.Point;

import javax.swing.JOptionPane;

import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExperimentCallback;

/**
 * Stymulacja 4 punktowa
 * @author pawel
 *
 */
public class ExpCalibrationStim4 extends ExpRFL {
	private static final long serialVersionUID = 1L;

	public ExpCalibrationStim4(ExperimentCallback callback, Reader reader) {
		super(callback,reader);

	}
	
	
	
	@Override
	void showStimulus() throws InterruptedException{
		setP(new Point(0,0));
		Thread.sleep(sleep*2);
		if(buffer.getFrames().size()<sleep/2) {
			JOptionPane.showMessageDialog(this, "Not receiving frames!");
			throw new RuntimeException("Not receiving frames!");
		}
	
		int screenX = 900;
		int screenY = 900;

		// 0>E
		wander(new Point(0,0),new Point(screenX,0));
		// E>N
		wander(new Point(screenX,0),new Point(0,-screenY));
		// N>W
		wander(new Point(0,-screenY),new Point(-screenX,0));
		//W>S
		wander(new Point(-screenX,0),new Point(0,screenY));
		//S>0
		wander(new Point(0,screenY),new Point(0,0));
		
		Thread.sleep(sleep*2);

	}

	

}
