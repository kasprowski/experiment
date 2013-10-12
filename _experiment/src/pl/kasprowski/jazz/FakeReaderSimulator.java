package pl.kasprowski.jazz;

import java.awt.Point;
import java.util.Random;

import pl.kasprowski.buffer.EyeFrame;
import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.buffer.Timestamp;

/**
 * Pretends reading from device
 * Instead of reading from COM (like COM reader) it generates points and adds
 * it to the buffer according to order set in another thread.
 * Two separate threads:
 * readThread - adds new values (basing on x and y) to buffer in endless loop
 * anonymous thread created in run of readThread - changes value of x and y
 * 
 * @author pawel
 *
 */
public class FakeReaderSimulator implements Reader,Runnable {

	IFrameBuffer buffer;
	Thread readThread;
	int counter = 0;
	int x = 0;
	int y = 0;
	Random r = new Random();



	@Override
	public void start(IFrameBuffer buffer) {
		this.buffer = buffer;
		readThread = new Thread(this);
		readThread.start();

	}

	@Override
	public void stop() {
		readThread.interrupt();
	}

	@Override
	public void setPortName(String portName) {
		//nop
	}


	@Override
	public void run() {
//		Thread thread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try{
//					while(true) {
//						Thread.sleep(sleep*2);
//					}
//				} catch (InterruptedException e) {	}
//
//			}
//		});
//		thread.start();
		setP(new Point(0,0));
		try {
			while(true) {
				EyeFrame eyeFrame = new EyeFrame();
				eyeFrame.timestamp = System.currentTimeMillis();
				eyeFrame.counter = counter++;
				
				Timestamp t = buffer.getLastTimestamp();
				if(t!=null) {
					Point p = (Point)t.getData();
					x=p.x;
					y=p.y;
				}
				int noise = 50;
				eyeFrame.eyeX = x + r.nextInt(noise)-noise/2;
				eyeFrame.eyeY = y  + r.nextInt(noise)-noise/2;

				Thread.sleep(1); // delay for new frames 1kHz

				//eyeFrame.eyeY = -eyeFrame.eyeY; //minus!!!
				buffer.addFrame(eyeFrame);


			}
		} catch (InterruptedException e) {

		}
	}

	/**
	 * Changes value of the current point - new frames will have this value
	 * 
	 * @param p
	 */
	public void setP(Point p) {
		x = p.x+100;
		y = p.y+100;
		//if we want to add info about stimulus (it should be done by Exp.. class)
		//buffer.addTimestamp(new Timestamp(System.currentTimeMillis(), p));
	}
	@Override
	public IFrameBuffer getBuffer() {
		return buffer;
	}

}
