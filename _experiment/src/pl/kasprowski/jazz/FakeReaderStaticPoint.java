package pl.kasprowski.jazz;

import java.awt.Point;
import java.util.Random;

import pl.kasprowski.buffer.EyeFrame;
import pl.kasprowski.buffer.IFrameBuffer;

/**
 * Pretends reading from device
 * Instead of reading from COM (like COM reader) it generates points and adds
 * it to the buffer according to order set in another thread.
 * Two separate threads:
 * readThread - adds new values (basing on x and y) to buffer in endless loop
 * anonymous thread created in run of readThread - changes value of x and y
 * 
 * Produkuje punkt o wspó³rzêdnych (100,100)
 * @author pawel
 *
 */
public class FakeReaderStaticPoint implements Reader,Runnable {

	// czas milisekundach pojawiania siê ramek
	static int sleep = 1;
	
	// œrednie wartoœci X i Y
	int midX = 2500;
	int midY = 2500;
	
	//noise
	int noise = 100;
	
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
				
				eyeFrame.eyeX = x + r.nextInt(noise)-noise/2;
				eyeFrame.eyeY = y  + r.nextInt(noise)-noise/2;

				Thread.sleep(sleep); // delay for new frames 1kHz

				eyeFrame.eyeY = -eyeFrame.eyeY; //minus!!!
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
		x = p.x + midX;
		y = p.y + midY;
		//if we want to add info about stimulus (it should be done by Exp.. class)
		//buffer.addTimestamp(new Timestamp(System.currentTimeMillis(), p));
	}
	@Override
	public IFrameBuffer getBuffer() {
		return buffer;
	}

}
