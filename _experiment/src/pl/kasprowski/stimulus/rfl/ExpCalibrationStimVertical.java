package pl.kasprowski.stimulus.rfl;

import java.awt.Point;

import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExperimentCallback;

/**
 * Tylko skoki góra - dó³
 * @author pawel
 *
 */
public class ExpCalibrationStimVertical extends ExpRFL {
	private static final long serialVersionUID = 1L;

	public ExpCalibrationStimVertical(ExperimentCallback callback, Reader reader) {
		super(callback,reader);
	}
	
	
	@Override
	public void run() {
		reader.start(buffer);

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						repaint();
						Thread.sleep(100);
					}
				} catch (InterruptedException e) { }
			}
		});
		t.start();

		isRunning = true;
		try {
			long startTime = System.currentTimeMillis();
			setP(new Point(0,0));
			Thread.sleep(sleep*2);
			// 0>S
			wander(new Point(0,0),new Point(0,screenY));
			// S>0
			wander(new Point(0,screenY),new Point(0,0));
			// 0>N
			wander(new Point(0,0),new Point(0,-screenY));
			// N>0
			wander(new Point(0,-screenY),new Point(0,0));
			
			Thread.sleep(sleep*2);
			long endTime = System.currentTimeMillis();
			System.out.println("Time = "+(endTime-startTime));

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		isRunning = false;
		repaint();
		reader.stop();
		callback.finished(buffer);



	}
	
	
	
	

}
