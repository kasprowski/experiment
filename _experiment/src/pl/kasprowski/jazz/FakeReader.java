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
 * @author pawel
 *
 */
public class FakeReader implements Reader,Runnable {

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
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					while(true) {
//						int sleep=200;	
//						setP(new Point(1,1));
//						Thread.sleep(sleep);
//						setP(new Point(1,0));
//						Thread.sleep(sleep);
//						setP(new Point(1,1));
//						Thread.sleep(sleep);
//						setP(new Point(1,2));
//						Thread.sleep(sleep);
//						setP(new Point(1,1));
//						Thread.sleep(sleep);
//						setP(new Point(0,1));
//						Thread.sleep(sleep);
//						setP(new Point(1,1));
//						Thread.sleep(sleep);
//						setP(new Point(2,1));
//						Thread.sleep(sleep);
//						setP(new Point(1,1));
//						Thread.sleep(sleep);
			
						int screenX = 200;
						int screenY = 200;
				
						int speed=10;
						int sleep=3000/speed;
						int skip=200;
						int sskip =50/speed;


						//			start = new Date().getTime();
						setP(new Point(0,0));
						Thread.sleep(sleep*2);
						//do lewej 0 -> W
						for(int i=0;i>=-screenX;i-=skip) {
							setP(new Point(i,0));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//do rogu E -> NW
						for(int i=0;i>=-screenY;i-=skip) {
							setP(new Point(-screenX,i));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//gora NW->NE
						for(int i=-screenX;i<=screenX;i+=skip) {
							setP(new Point(i,-screenY));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//prawo NE->SE
						for(int i=-screenY;i<=screenY;i+=skip) {
							setP(new Point(screenX,i));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//dol SE->S
						for(int i=screenX;i>=0;i-=skip) {
							setP(new Point(i,screenY));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//dol S -> srodek
						for(int i=screenY;i>=0;i-=skip) {
							setP(new Point(0,i));
							Thread.sleep(sskip);
						}

						Thread.sleep(sleep*2);

						//do prawej 0->E 
						for(int i=0;i<=screenX;i+=skip) {
							setP(new Point(i,0));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//do E->NE
						for(int i=0;i>=-screenY;i-=skip) {
							setP(new Point(screenX,i));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//NE->NW
						for(int i=screenX;i>=-screenX;i-=skip) {
							setP(new Point(i,-screenY));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//NW->SW
						for(int i=-screenY;i<=screenY;i+=skip) {
							setP(new Point(-screenX,i));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//SW->S
						for(int i=-screenX;i<=0;i+=skip) {
							setP(new Point(i,screenY));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep);
						//S->0
						for(int i=screenY;i>=0;i-=skip) {
							setP(new Point(0,i));
							Thread.sleep(sskip);
						}
						Thread.sleep(sleep*2);
					
					}
				} catch (InterruptedException e) {	}

			}
		});
		thread.start();
		try {
			while(true) {
				EyeFrame eyeFrame = new EyeFrame();
				eyeFrame.timestamp = System.currentTimeMillis();
				eyeFrame.counter = counter++;
				eyeFrame.eyeX = x + r.nextInt(30)-15;
				eyeFrame.eyeY = y  + r.nextInt(30)-15;

				Thread.sleep(1); // delay for new frames 1kHz

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
