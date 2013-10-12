package pl.kasprowski.stimulus;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import pl.kasprowski.buffer.EyeFrame;
import pl.kasprowski.buffer.FrameBuffer;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.tools.Calc;
import pl.kasprowski.tools.Graph;


/**
 * Shows gaze point and calibrates to gaze point when user clicks with mouse.
 * 
 * Wersja 1 przelicza dane z urz¹dzenia najpierw na (-1000,+1000)
 * a potem wpasowuje w okno
 * Minus to, ¿e trzeba z góry znaæ wielkoœæ okna - lepiej dzia³a ExpLive2
 * 
 * @author pawel
 *
 */
@Deprecated
public class ExpLive extends Experiment {
	private static final long serialVersionUID = 1L;

	
	
	/**
	 * Screen dimension in pixels
	 */
	private final int SCREEN_MINX = 50;
	private final int SCREEN_MAXX = 950;

	private final int SCREEN_MINY = 20;
	private final int SCREEN_MAXY = 720;
	

	/**
	 * Max angle for screen border in cdeg, (0,0) is in the middle
	 */
	int screenX = 1000;
	int screenY = 1000;


	double[] coefX = new double[2];
	double[] coefY = new double[2];

	boolean isRunning = false;
	
	public ExpLive(ExperimentCallback callback, Reader reader) {
		super(callback, reader);
		coefX = new double[]{1,0};
		coefY = new double[]{1,0};
		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				//Point p = e.getLocationOnScreen();
				if(!isRunning) {
					isRunning = true;
					start();
				}
				Point p = new Point(e.getX(),e.getY());
				setPoint(p);
			}
		});

//		setLayout(new BorderLayout());
//		JButton button = new JButton("stop"); 
//		add(
	}

	
	private void start() {
		buffer = new FrameBuffer();
		reader.start(buffer);
		stimThread = new Thread(this);
		stimThread.start();
	}


//	private void stop() {
//		reader.stop();
//		System.out.println("reader stopped");
//		stimThread.interrupt();
//		System.out.println("thread interrupted");
//		callback.finished(buffer);
//	}

	
	
	@Override
	public void run() {
		try {
			while(true) {
				Thread.sleep(100);
				repaint();
			}
		} catch (InterruptedException e) {}

	}

	
	Point p = new Point(0,0);
	
	/**
	 * Calibrates the current gaze point on mouse click point
	 * @param p
	 */
	private void setPoint(Point p) {
		this.p = p;
		int size = buffer.getFrames().size();
		int nx=0;
		int ny=0;
		int MEDIAN = 41;
		if(size>MEDIAN) {
			//EyeFrame lastFrame = buffer.getFrames().get(size-1);
			double[] lastX = new double[MEDIAN];
			double[] lastY = new double[MEDIAN];
			for(int i=-MEDIAN;i<0;i++) {
			//	System.out.println("B size="+buffer.getFrames().size());
			//	System.out.println("i:"+i+" size:"+size+" :"+(i+MEDIAN)+" s:"+(size+i));
				lastX[i+MEDIAN] = buffer.getFrames().get(size+i).eyeX;
				lastY[i+MEDIAN] = buffer.getFrames().get(size+i).eyeY;
			}
			
//			nx = lastFrame.eyeX;
//			ny = lastFrame.eyeY;

			nx = (int)Calc.countMedian(lastX);
			ny = (int)Calc.countMedian(lastY);
		}
		coefX[1] = p.x-nx;
		coefY[1] = p.y-ny;
		//coefX[1] = nx;
		//coefY[1] = ny;
				
		System.out.println("nx:"+nx+" ny:"+ny+" p.x:"+p.x+" p.y:"+p.y);
		repaint();
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		int rsize = 5;
		g.drawRect(p.x-rsize/2, p.y-rsize/2, rsize, rsize);
		
		int size = buffer.getFrames().size();
		
		if(size==0) {
			g.drawString("Empty buffer",10,50);
			return;
		}
		int FRAME_SIZE = 150;
		int frames = (size>=FRAME_SIZE)?FRAME_SIZE:size;
		g.drawString(""+size, 5, 30);
		g.drawString("X: ["+coefX[0]+","+coefX[1]+"]",5,50);
		g.drawString("Y: ["+coefY[0]+","+coefY[1]+"]",5,70);
		
		int avgEyeX = 0;
		int avgEyeY = 0;
		for(int i=0;i<frames;i++) {
			EyeFrame f = buffer.getFrames().get(i+size-frames);
	
//			double xCDeg = Calc.recalcAB(f.eyeX, coefX[0], coefX[1]);
//			double yCDeg = Calc.recalcAB(f.eyeY, coefY[0], coefY[1]);
////			//TODO: okno powinno byc obliczone w katach
//			double xScreen = Calc.recalc(xCDeg, -screenX, +screenX, SCREEN_MINX, SCREEN_MAXX);
//			double yScreen = Calc.recalc(yCDeg, -screenY, +screenY, SCREEN_MINY, SCREEN_MAXY);
			
			double xScreen = Calc.recalcAB(f.eyeX, coefX[0], coefX[1]);
			double yScreen = Calc.recalcAB(f.eyeY, coefY[0], coefY[1]);
			
			yScreen = SCREEN_MAXY - yScreen; //???

			avgEyeX+=xScreen;
			avgEyeY+=yScreen;

	
			Point pScreen = new Point((int)xScreen,(int)yScreen);
			g.setColor(new Color(0,0,0));
//			g.drawString(f.eyeX+","+f.eyeY+" [CDeg: "+(int)xCDeg+","+(int)yCDeg+"]"+" [abs: "+(int)xScreen+","+(int)yScreen+"]", 5, (i*20)+150);
			//g.drawString(f.eyeX+","+f.eyeY+" [abs: "+(int)xScreen+","+(int)yScreen+"]", 5, (i*20)+150);			
			int c = 255-i*20;
			if(c<0) c=0;
			//System.out.println(c);
			g.setColor(new Color(c,c,c));
			int s = 5;
//			g.fillRect(pScreen.x, getSize().height/2 /*pScreen.y*/, s, s);
//			g.fillRect( getSize().height/2 /*pScreen.x*/, pScreen.y,s, s);
//			g.drawLine(pScreen.x, getSize().height/2, getSize().height/2 /*pScreen.x*/, pScreen.y);
//			g.setColor(new Color(0,0,200));
			//g.fillRect( pScreen.x, pScreen.y,s, s);
			Graph.point(g, pScreen.x, pScreen.y,s);
		}		
		int sizer2 = 50;
		int s = 10;
		avgEyeX/=frames;
		avgEyeY/=frames;
		Point pScreen = new Point(avgEyeX,avgEyeY);
		g.setColor(new Color(0,0,0));
		g.drawRect( avgEyeX-sizer2/2, avgEyeY-sizer2/2,sizer2, sizer2);
		//g.fillOval(pScreen.x, getSize().height/2 /*pScreen.y*/, s, s);
		Graph.circle(g, pScreen.x, getSize().height/2 /*pScreen.y*/, s);
		//g.fillOval( getSize().height/2 /*pScreen.x*/, pScreen.y,s, s);
		Graph.circle(g, getSize().height/2 /*pScreen.x*/, pScreen.y, s);
		g.drawLine(pScreen.x, getSize().height/2, getSize().height/2 /*pScreen.x*/, pScreen.y);
		g.drawLine(pScreen.x, getSize().height/2, pScreen.x, pScreen.y);
		g.drawLine(pScreen.x, pScreen.y, getSize().height/2 /*pScreen.x*/, pScreen.y);
		
	}
	
	
}
