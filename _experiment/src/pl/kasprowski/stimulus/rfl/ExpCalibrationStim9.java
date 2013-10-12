package pl.kasprowski.stimulus.rfl;

import java.awt.Point;

import javax.swing.JOptionPane;

import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExperimentCallback;

/**
 * Stymulacja 9 punktowa
 * @author pawel
 *
 */
public class ExpCalibrationStim9 extends ExpRFL {
	private static final long serialVersionUID = 1L;

	public ExpCalibrationStim9(ExperimentCallback callback, Reader reader) {
		super(callback,reader);
	}
	
	
	
	
	@Override
	void showStimulus() throws InterruptedException, RuntimeException{
		setP(new Point(0,0));
		Thread.sleep(sleep*2);
		if(buffer.getFrames().size()<sleep/2) {
			JOptionPane.showMessageDialog(this, "Not receiving frames!");
			throw new RuntimeException("Not receiving frames!");
		}
		
		int screenX = 900;
		int screenY = 900;
		//0->E
		wander(new Point(0,0),new Point(screenX,0));
	
		
		//do rogu E -> NE
		wander(new Point(screenX,0),new Point(screenX,-screenY));
		//gora NE->NW
		wander(new Point(screenX,-screenY),new Point(-screenX,-screenY));
		//NW->0
		wander(new Point(-screenX,-screenY),new Point(0,0));
		//0->S
		wander(new Point(0,0),new Point(0,screenY));
		//S->E
		wander(new Point(0,screenY),new Point(-screenX,0));
		//E->N
		wander(new Point(-screenX,0),new Point(0,-screenY));
		//N->SE
		wander(new Point(0,-screenY),new Point(screenX,screenY));
		//SE->SW
		wander(new Point(screenX,screenY),new Point(-screenX,screenY));
		//SW->0
		wander(new Point(-screenX,screenY),new Point(0,0));
		Thread.sleep(sleep*2);
	}
	

}
