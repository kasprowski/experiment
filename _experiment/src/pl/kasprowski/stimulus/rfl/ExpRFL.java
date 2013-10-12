package pl.kasprowski.stimulus.rfl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.buffer.Timestamp;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.Experiment;
import pl.kasprowski.stimulus.ExperimentCallback;
import pl.kasprowski.tools.Calc;

/**
 * Not stopable stimulus generator
 * Starts with mouse click and shows stimulus
 * Starts reader and stops it in the end
 * Adds timestamps to buffer
 * When finished invokes callback.finished(buffer)
 * @author pawel
 *
 */
public class ExpRFL extends Experiment implements Runnable{
	private static final long serialVersionUID = 1L;

	
	Random r = new Random();
	

	/**
	 * Max angle for screen border in cdeg, (0,0) is in the middle
	 */
	//TODO maybe convert to pixels?
	protected int screenX = 1000;
	protected int screenY = 1000;

	/**
	 * Current RFL on the screen position in pixels, (0,0) is in the middle
	 */
	double x;
	double y;

//	public ExpRFL(ExperimentCallback callback, Reader reader, IFrameBuffer buffer) {
//		super(callback,reader);
//		this.buffer = buffer;
//		
//	}
	public ExpRFL(ExperimentCallback callback, Reader reader) {
		super(callback,reader);
		
		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				if(!isRunning) start();
			}
		});
		
	}

	private void start() {
		stimThread = new Thread(this);
		stimThread.start();
	}


	/**
	 * Main loop for stimulus.
	 * May be overriden
	 */
	@Override
	public void run() {
		reader.start(buffer);

		// w¹tek odœwie¿aj¹cy ekran
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

		boolean error=false;
		isRunning = true;
		try {
			long startTime = System.currentTimeMillis();
			
			showStimulus();
			
			long endTime = System.currentTimeMillis();
			System.out.println("Time = "+(endTime-startTime));

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			error=true;
		}
		isRunning = false;
		repaint();
		reader.stop();
		if( !error) callback.finished(buffer);

	}

	/**
	 * Method is overriden - simple implementation just shows a static
	 * point in the middle of the screen
	 * @throws InterruptedException
	 */
	void showStimulus() throws InterruptedException{
		setP(new Point(0,0));
		Thread.sleep(sleep*2);
	}
	
	
	/**
	 * Adds timestamp with new point (in cdeg -1000,1000) to buffer
	 * Calculates new values for x and y in pixels
	 * @param p
	 */
	protected void setP(Point p) {
		//!!! minus!
		buffer.addTimestamp(new Timestamp(System.currentTimeMillis(), new Point(p.x,-p.y)));
		//Dimension size = getSize();

		//TODO wielkosc okna nie powinna byc na sztywno!
//		x = Calc.recalc(p.x, -screenX, screenX, SCREEN_MINX, SCREEN_MAXX) ;
//		y = Calc.recalc(p.y, -screenY, screenY, SCREEN_MINY, SCREEN_MAXY) ;
		x = Calc.recalc(p.x, -1024, 1024, 0, this.getSize().width) ;
		y = Calc.recalc(p.y, -1024, 1024, 0, this.getSize().height) ;

		repaint();
	}


	int pointSize = 10;
	int pointColor = 255;
	int counterSize = 2;
	/**
	 * Metoda wyœwietlaj¹ca punkt w miejscu (x,y)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		//g.setColor(new Color(255,255,255));
		g.setColor(new Color(0,0,0));
		//int offset = 10;
		//g.fillRect(SCREEN_MINX-offset, SCREEN_MINY-offset, SCREEN_MAXX-SCREEN_MINX+2*offset, SCREEN_MAXY-SCREEN_MINY+2*offset);
		g.fillRect(0, 0, getSize().width, getSize().width);
			
		if(x==0 || y==0) 
			setP(new Point(0,0));
		
		
		if(counterSize--==0) {
			pointSize = r.nextInt(15)+5;
			pointColor = r.nextInt(150)+100;
			counterSize = 4;
		}
		//int size2 = r.nextInt(20)+2;
		if(isRunning) {
			g.setColor(new Color(pointColor,pointColor,pointColor));
		} else
			g.setColor(new Color(0,200,0));
		g.fillRect((int)x-pointSize/2,(int)y-pointSize/2,pointSize, pointSize);
		g.setColor(new Color(0,0,0));
		g.fillRect((int)x,(int)y,1,1);
	
	}

	/**
	 * Should be 1
	 */
	int speed=1;
	/**
	 * Fixation time
	 */
	int sleep=5000/speed;
	/**
	 * Number of point between fixations
	 */
	int skip=10;
	/**
	 * Length of points appearance between fixation points
	 */
	int sskip =10/speed;

	/**
	 * Moves point from one location to another
	 * @param from
	 * @param to
	 * @throws InterruptedException
	 */
	void wander(Point from, Point to) throws InterruptedException {
		//System.out.println("Wandering "+from.x+","+from.y+" "+to.x+","+to.y);
		
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
			//if(finish) System.out.println("F: "+skipx+" "+to.x+" "+cx);
		}
		Thread.sleep(sleep);
		
	}

}
