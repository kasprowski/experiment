package pl.kasprowski.stimulus.rfl;

import java.awt.Point;

import javax.swing.JOptionPane;

import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExperimentCallback;

/**
 * Stymulacja 16 punktowa
 * @author pawel
 *
 */
public class ExpCalibrationStim16 extends ExpRFL {
	private static final long serialVersionUID = 1L;

	public ExpCalibrationStim16(ExperimentCallback callback, Reader reader) {
		super(callback,reader);
	}
	
	
	
	
	@Override
	void showStimulus() throws InterruptedException, RuntimeException{
		sleep = 4000;
		setP(p(7,7));
		prevPoint = new Point(7,7);
		Thread.sleep(sleep*2);
		if(buffer.getFrames().size()<sleep/2) {
			JOptionPane.showMessageDialog(this, "Not receiving frames!");
			throw new RuntimeException("Not receiving frames!");
		}
		
		//start 7,7
		go(12,11);
		go(3,15);
		go(9,4);
		go(1,1);
		go(6,12);
		go(14,7);
		go(5,2);
		go(2,5);
		go(8,10);
		go(11,15);
		go(15,3);
		go(10,0);
		go(4,8);
		go(13,13);
		go(0,9);
		go(8,8);
		
		Thread.sleep(sleep*2);
	}
	
	Point prevPoint = new Point(0,0);
	void go(int x, int y) throws InterruptedException, RuntimeException{
		wander(p(prevPoint.x,prevPoint.y),p(x,y));
		prevPoint.x = x;
		prevPoint.y = y;
	}
	Point p(int x,int y) {
		return new Point(x*128-1024+64,y*128-1024+64);
	}

}
