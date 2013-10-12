package pl.kasprowski.stimulus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.EyeFrame;
import pl.kasprowski.buffer.FrameBuffer;
import pl.kasprowski.buffer.Timestamp;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.tools.Calc;
import pl.kasprowski.tools.Graph;
import pl.kasprowski.tools.Statistics;


/**
 * Shows gaze point and calibrates to gaze point when user clicks with mouse.
 * 
 * Wersja 2 nie przelicza w ogole na (-1000,+1000)
 * Przelicza bezpoœrednio dane z urz¹dzenia na wspó³rzêdne ekranowe
 * 
 * @author pawel
 *
 */
public class ExpLive2 extends Experiment {
	private static final long serialVersionUID = 1L;
	Logger log = Logger.getLogger(ExpLive2.class);
	double[] coefX = new double[2];
	double[] coefY = new double[2];

	boolean isRunning = false;
	//boolean isRecording = false;
	
	public ExpLive2(ExperimentCallback callback, Reader reader) {
		super(callback, reader);
		coefX = new double[]{1,0};
		coefY = new double[]{1,0};
		setLayout(new BorderLayout());
		add(getToolbar(),BorderLayout.NORTH);
		
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
				else{
					Point p = new Point(e.getX(),e.getY());
					//setPoint(p);
					startFixating(p);
				}
			}
		});
		
	}

	private JToolBar getToolbar() {
		JToolBar toolbar = new JToolBar();
		
		JButton btSave = new JButton("Save");
		btSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
				String fileName = "live"+sdf.format(new Date())+".ser";
				buffer.serialize(fileName);
			}
		});
		toolbar.add(btSave);

		JButton btReset = new JButton("Reset");
		btReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				screenPoints = new ArrayList<Point>();
				eyePoints = new ArrayList<double[]>();
				deviations = new ArrayList<double[]>();
				coefX = new double[]{1,0};
				coefY = new double[]{1,0};
			}
		});
		toolbar.add(btReset);

		JButton btClear = new JButton("Clear buffer");
		btClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buffer.clear();
			}
		});
		toolbar.add(btClear);

		return toolbar;

	}

	private void start() {
		buffer = new FrameBuffer();
		reader.start(buffer);
		stimThread = new Thread(this);
		stimThread.start();
	}



	/**
	 * Endless loop refreshing the screen and guarding fixation time
	 */
	@Override
	public void run() {
		try {
			while(true) {
				Thread.sleep(100);
				long timestamp = new Date().getTime();
				if(isFixating && timeToFixate<timestamp) {
					isFixating = false;
					//point(0,0) means no fixation after that point
					buffer.addTimestamp(new Timestamp(new Date().getTime(),new Point(0,0)));
					calcCoefs();
				}

				repaint();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	boolean isFixating = false;
	long timeToFixate = 0;
	//Point p = new Point(0,0);
	List<Point> screenPoints = new ArrayList<Point>();
	List<double[]> eyePoints = new ArrayList<double[]>();
	List<double[]> deviations = new ArrayList<double[]>();

	/**
	 * After mause is clicked - starts timer for fixation
	 * @param p
	 */
	private void startFixating(Point p) {
		screenPoints.add(p);
		buffer.addTimestamp(new Timestamp(new Date().getTime(),p));
		timeToFixate = new Date().getTime() + 3000;
		isFixating = true;
	}

	double[] lastX;
	double[] lastY;
	int MEDIAN = 1500; //no of last frames to take

	/**
	 * Calibrates the current gaze point on mouse click point
	 * @param p
	 */
	private void calcCoefs() {
		//this.p = p;
		Point p = screenPoints.get(screenPoints.size()-1);
		int size = buffer.getFrames().size();
		MEDIAN = 1000; //no of last frames to take
		if(size<MEDIAN) {
			log.error("Should not happen!");
			MEDIAN = size;
		}
		//EyeFrame lastFrame = buffer.getFrames().get(size-1);
		lastX = new double[MEDIAN];
		lastY = new double[MEDIAN];
		double sumX = 0;
		double sumY = 0;
		for(int i=-MEDIAN;i<0;i++) {
			//	System.out.println("B size="+buffer.getFrames().size());
			//	System.out.println("i:"+i+" size:"+size+" :"+(i+MEDIAN)+" s:"+(size+i));
			lastX[i+MEDIAN] = buffer.getFrames().get(size+i).eyeX;
			lastY[i+MEDIAN] = buffer.getFrames().get(size+i).eyeY;
			sumX += buffer.getFrames().get(size+i).eyeX;
			sumY += buffer.getFrames().get(size+i).eyeY;
		}
		double[] newEyePointAvg = new double[2]; 
		newEyePointAvg[0] = (int)sumX/MEDIAN;
		newEyePointAvg[1] = (int)sumY/MEDIAN;
		
		double[] dev = new double[2];
		dev[0] = new Statistics(lastX).getStdDev();
		dev[1] = new Statistics(lastY).getStdDev();
		deviations.add(dev);
		double[] newEyePoint = new double[2]; 
		newEyePoint[0] = (int)Calc.countMedian(lastX);
		newEyePoint[1] = (int)Calc.countMedian(lastY);
		eyePoints.add(newEyePoint);

		System.out.println("================");
		System.out.println("New screen point: "+p.x+","+p.y);
		System.out.println("New eye point AVG: "+newEyePointAvg[0]+","+newEyePointAvg[1]);
		System.out.println("New eye point: "+newEyePoint[0]+","+newEyePoint[1]);
		System.out.println("Standard deviation: "+dev[0]+","+dev[1]);

		if(screenPoints.size()<2) { // only shift for one point
			coefX[1] = p.x-newEyePoint[0];
			coefY[1] = p.y-newEyePoint[1];
		}
		else { // we have two points - calc both coefs
			Point sp0 = screenPoints.get(screenPoints.size()-2);
			double[] ep0 = eyePoints.get(eyePoints.size()-2);
			System.out.println("Previous screen point: "+sp0.x+","+sp0.y);
			System.out.println("Previous eye point: "+ep0[0]+","+ep0[1]);
			
			coefX = Calc.calcCoef(ep0[0], newEyePoint[0], sp0.x, p.x);
			coefY = Calc.calcCoef(ep0[1], newEyePoint[1], sp0.y, p.y);

			//coefY[0] = -1; 
		}

		System.out.println("coefX = "+coefX[0]+","+coefX[1]);
		System.out.println("coefY = "+coefY[0]+","+coefY[1]);

		//coefX[1] = nx;
		//coefY[1] = ny;

		//		System.out.println("nx:"+nx+" ny:"+ny+" p.x:"+p.x+" p.y:"+p.y);
		repaint();
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		//		int rsize = 5; // tu rysowa³o punkt w ostanim klinkniêtym gazepoincie
		//		g.drawRect(p.x-rsize/2, p.y-rsize/2, rsize, rsize);

		if(isFixating) {
			long toEnd = timeToFixate - new Date().getTime();
			if(toEnd>0) {
				Point p = screenPoints.get(screenPoints.size()-1);
				int rsize = 5;
				g.drawString(""+(toEnd/100),p.x-rsize/2, p.y-rsize/2);
			}
		}
		else {
			if(screenPoints.size()>1) {
				Point p1 = screenPoints.get(screenPoints.size()-1);
				Point p2 = screenPoints.get(screenPoints.size()-2);
				double[] e1 = eyePoints.get(eyePoints.size()-1);
				double[] e2 = eyePoints.get(eyePoints.size()-2);
				double[] dev1 = deviations.get(deviations.size()-1);
				double[] dev2 = deviations.get(deviations.size()-2);
				//g.drawRect( p1.x, p1.y, Math.abs(p2.x-p1.x), Math.abs(p2.y-p1.y));
				g.drawLine( p1.x, p1.y, p2.x, p2.y);
				g.setColor(new Color(0,255,0));
				Graph.ellipse(g, p1.x, p1.y, (int)dev1[0]*10, (int)dev1[1]*10);
				Graph.ellipse(g, p2.x, p2.y, (int)dev2[0]*10, (int)dev2[1]*10);
				g.setColor(new Color(0,0,0));
				g.drawString("["+p1.x+","+p1.y+"] - "+e1[0]+","+e1[1],	p1.x, p1.y);
				g.drawString("["+p2.x+","+p2.y+"] - "+e2[0]+","+e2[1],	p2.x, p2.y);
				g.setColor(new Color(0,25,0));
				for(int i=0;i<MEDIAN;i++) {
					double xScreen = Calc.recalcAB(lastX[i], coefX[0], coefX[1]);
					double yScreen = Calc.recalcAB(lastY[i], coefY[0], coefY[1]);
					Point pScreen = new Point((int)xScreen,(int)yScreen);
					Graph.point(g, pScreen.x, pScreen.y,3);
				}
				g.setColor(new Color(0,0,0));
			}
		
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

			//			yScreen = SCREEN_MAXY - yScreen; //???

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
		
		
		EyeFrame f = buffer.getFrames().get(buffer.getFrames().size()-1);
		g.drawString(f.eyeX+","+f.eyeY, getSize().height/2, getSize().width/2);
		
		}
	}


}
