package pl.kasprowski.stimulus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.EyeFrame;
import pl.kasprowski.buffer.FrameBuffer;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.tools.Calc;
import pl.kasprowski.tools.Graph;

/**
 * Experiment that shows current gazepoint in realtime
 * Gets two coeficients and middlePoint
 * @author pawel
 *
 */
public class ExpVisualizer extends Experiment implements Runnable{
	private static final long serialVersionUID = 1L;
	Logger log = Logger.getLogger(ExpVisualizer.class);

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

	
	
	JButton start = new JButton("Start");
	JTextField login = new JTextField("test");
	JLabel status = new JLabel();
	boolean isReading = false;
	
	double[] coefX;
	double[] coefY;
	double[] middlePoint; //wspó³rzêdne punktu w œrodku

	/**
	 * Konstruktor tylko do testów
	 * @param callback
	 * @param reader
	 */
	public ExpVisualizer(ExperimentCallback callback, Reader reader) {
		this(callback,reader,new double[]{1,0},new double[]{1,0},new double[]{100,100});
	}

	/**
	 * Dostaje wspó³czynniki kalibracji i wspó³rzêdne punktu na œrodku (wg urz¹dzenia)
	 * @param callback
	 * @param reader
	 * @param coefX
	 * @param coefY
	 * @param middlePoint
	 */
	public ExpVisualizer(ExperimentCallback callback, Reader reader, double[] coefX, double[] coefY, double[] middlePoint) {
		super(callback,reader);
		this.coefX = coefX;
		this.coefY = coefY;
		this.middlePoint = middlePoint;
		
		buildGui();
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isReading) stop();
				else start();
				isReading = !isReading;
			}
		});
		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				if(isReading) stop();
				else start();
				isReading = !isReading;
			}
		});


	}
	
	private void buildGui() {
		setLayout(new BorderLayout());
		JToolBar jtb = new JToolBar();
		jtb.add(start);
		jtb.add(login);
		this.add(jtb,BorderLayout.NORTH);
		this.add(new JPanel(),BorderLayout.CENTER);
		this.add(status,BorderLayout.SOUTH);
	}

	private void start() {
		start.setText("Stop");
		buffer = new FrameBuffer();
		reader.start(buffer);
		stimThread = new Thread(this);
		stimThread.start();
	}


	private void stop() {
		reader.stop();
		System.out.println("reader stopped");
		stimThread.interrupt();
		System.out.println("thread interrupted");
		start.setText("Start");
		//buffer.save(login.getText()+".out");
		//System.out.println("buffer saved");
		callback.finished(buffer);
	}

	


	@Override
	public void run() {
		try {
			while(true) {
				Thread.sleep(100);
				repaint();
			}
		} catch (InterruptedException e) {}
	}


	@Override
	public void paint(Graphics g) {
		super.paint(g);
		int size = buffer.getFrames().size();
		//int start = size-10;


		//	System.out.println("Range: "+start+","+(start+10));

		g.setColor(new Color(255,255,255));
		g.fillRect(SCREEN_MINX, SCREEN_MINY, SCREEN_MAXX-SCREEN_MINX, SCREEN_MAXY-SCREEN_MINY);
		g.setColor(new Color(0,0,0));
		g.drawRect(SCREEN_MINX, SCREEN_MINY, SCREEN_MAXX-SCREEN_MINX, SCREEN_MAXY-SCREEN_MINY);

		if(size==0) {
			g.drawString("Empty buffer",10,50);
			return;
		}
		//		if(size<10) {
		//			start=0;
		//		}

		//punkt w srodku			
		double xM = Calc.recalcAB(middlePoint[0], coefX[0], coefX[1]);
		double yM = Calc.recalcAB(middlePoint[1], coefY[0], coefY[1]);
		double xMiddle = Calc.recalc(xM, -screenX, +screenX, SCREEN_MINX, SCREEN_MAXX);
		double yMiddle = Calc.recalc(yM, -screenY, +screenY, SCREEN_MINY, SCREEN_MAXY);
		yMiddle = SCREEN_MAXY - yMiddle; //???
		Graph.circle(g, (int)xMiddle, (int)yMiddle, 10);

		

		//punkty poprzednie
		int frames = (size>=10)?10:size;
		for(int i=0;i<frames;i++) {
			//	System.out.println("Reading "+(i+start));
			EyeFrame f = buffer.getFrames().get(i+size-frames);
			
			//przeliczenie do zakresu -1000,+1000 - 0,0 jesli na srodek
			double xCDeg = Calc.recalcAB(f.eyeX, coefX[0], coefX[1]);
			double yCDeg = Calc.recalcAB(f.eyeY, coefY[0], coefY[1]);
			
			// przeliczenie na wspó³rzêdne na ekranie 
			//TODO wielkoœæ okna ustalona na sztywno... -1000 to lewy brzeg, +1000 to prawy brzeg
			double xScreen = Calc.recalc(xCDeg, -screenX, +screenX, SCREEN_MINX, SCREEN_MAXX);
			double yScreen = Calc.recalc(yCDeg, -screenY, +screenY, SCREEN_MINY, SCREEN_MAXY);

			
			//wyœwietlenie gaze pointa
			
			yScreen = SCREEN_MAXY - yScreen; //???
			Point pScreen = new Point((int)xScreen,(int)yScreen);
			g.drawString(f.eyeX+","+f.eyeY+" ["+(int)xScreen+","+(int)yScreen+"]", 5, (i*20)+50);
			int c = 255-i*20; //kolor
			//System.out.println(c);
			g.setColor(new Color(c,c,c));
			int s = i+1;
			// rzeczywisty gaze point
			Graph.circle(g, pScreen.x, pScreen.y, s);

			//jeœli to ostani punkt - dodatkowo ramka
			if(i==frames-1) {
				//dwa punkty na osiach
				g.fillRect(pScreen.x, getSize().height/2 /*pScreen.y*/, s, s);
				g.fillRect(getSize().height/2 /*pScreen.x*/, pScreen.y,s, s);
				g.drawLine(pScreen.x, getSize().height/2, getSize().height/2 /*pScreen.x*/, pScreen.y);

				Dimension d = getSize();
				g.drawString(f.eyeX+","+f.eyeY+" <"+(int)xCDeg+","+(int)yCDeg+"> ["+(int)xScreen+","+(int)yScreen+"]", d.width/2, d.height/2);
				Graph.frame(g, (int)xScreen, (int)yScreen, 50);
				
			}
		}

//		g.setColor(new Color(255,0,0));
//		EyeFrame f = buffer.getFrames().get(size-1);
//		double xCDeg = Calc.recalcAB(f.eyeX, coefX[0], coefX[1]);
//		double yCDeg = Calc.recalcAB(f.eyeY, coefY[0], coefY[1]);
//		
//		//TODO: okno powinno byc obliczone w katach
//		double xScreen = Calc.recalc(xCDeg, -screenX, +screenX, SCREEN_MINX, SCREEN_MAXX);
//		double yScreen = Calc.recalc(yCDeg, -screenY, +screenY, SCREEN_MAXY, SCREEN_MINY);
//		
//		//yScreen = SCREEN_MAXY-yScreen;
//		Dimension d = getSize();
//		g.drawString(f.eyeX+","+f.eyeY+" <"+(int)xCDeg+","+(int)yCDeg+"> ["+(int)xScreen+","+(int)yScreen+"]", d.width/2, d.height/2);
//		Graph.frame(g, (int)xScreen, (int)yScreen, 50);
		
		
	}

//
//	private final int EYE_MAXX = 7000;
//	private final int EYE_MAXY = 5000;
//	private final int EYE_MINX = -2000;
//	private final int EYE_MINY = 0;
//
//	private Point recalcEye(int ex, int ey) {
//		double dex = (double)ex;
//		double dey = (double)ey;
//
//		double screenDiffX = SCREEN_MAXX - SCREEN_MINX;
//		double eyeDiffX =EYE_MAXX - EYE_MINX;
//		double screenX = (dex-(double)EYE_MINX)*(screenDiffX/eyeDiffX)+SCREEN_MINX;
//
//		double screenDiffY = SCREEN_MAXY - SCREEN_MINY;
//		double eyeDiffY =EYE_MAXY - EYE_MINY;
//		double screenY = (dey-(double)EYE_MINY)*(screenDiffY/eyeDiffY)+SCREEN_MINY;
//
//		Point p = new Point();
//		p.x = (int)screenX;
//		p.y = (int)screenY;
//		return p;
//	}


}
