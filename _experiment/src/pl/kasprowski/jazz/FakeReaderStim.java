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

public class FakeReaderStim  implements Reader,Runnable  {

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

						int screenX = 1000;
						int screenY = 1000;
				
						setP(new Point(0,0));
						Thread.sleep(sleep*2);
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

	int speed=10;
	int sleep=2010; //5000/speed;
	int skip=10;
	int sskip =50/speed;

	private void wander(Point from, Point to) throws InterruptedException {
	//	System.out.println("Wandering "+from.x+","+from.y+" "+to.x+","+to.y);
		
		int skipx = (to.x-from.x)/skip;
		//if(from.x>=to.x) skipx=-skipx;
		int skipy = (to.y-from.y)/skip;
		//if(from.y>=to.y) skipy=-skipy;
	
		int cx = from.x;
		int cy = from.y;
		boolean finish=false;
		while(!finish) {
			setP(new Point(cx,cy));
			//System.out.println(cx+","+cy);
			Thread.sleep(sskip);
			cx+=skipx;
			cy+=skipy;
			if(skipx>=0	 && cx>to.x) finish=true;
			if(skipx<0	 && cx<to.x) finish=true;
			if(skipy>=0	 && cy>to.y) finish=true;
			if(skipy<0	 && cy<to.y) finish=true;
			//if(finish)	System.out.println("F: "+skipx+" "+to.x+" "+cx);
		}
		Thread.sleep(sleep);
		
	}

	@Override
	public IFrameBuffer getBuffer() {
		return buffer;
	}

}
