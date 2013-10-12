package pl.kasprowski.stimulus.rfl;

import java.awt.Point;

import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExperimentCallback;

/**
 * Punkt na œrodku ekranu przez 10 sekund
 * @author pawel
 *
 */
public class ExpStaticPoint extends ExpRFL {
	private static final long serialVersionUID = 1L;

	public ExpStaticPoint(ExperimentCallback callback, Reader reader) {
		super(callback,reader);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		reader.start(buffer);
		isRunning = true;
		try {
			//long startTime = System.currentTimeMillis();
			//			start = new Date().getTime();
			setP(new Point(0,0));
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		isRunning = false;
		repaint();
		reader.stop();
		callback.finished(buffer);

	}

}
