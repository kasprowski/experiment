package pl.kasprowski.stimulus.chart;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import pl.kasprowski.buffer.EyeFrame;
import pl.kasprowski.buffer.IFrameBuffer;

public class LiveChart extends JComponent{
	int x = 0;
	int y = 0;

	int rangeMin = 0;
	int rangeMax = 5000;
	int divider = 10;


	int width;
	int height;
	String axis;
	IFrameBuffer buffer;

	public LiveChart(IFrameBuffer buffer,String axis) {
		this.buffer = buffer;
		this.axis = axis;
		this.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				recalibrate();
			}
		});
	}

	/**
	 * ustawia oœ poziom¹ tak jak aktualny sygna³
	 */
	void recalibrate() {
		int size = buffer.getFrames().size();
		if(size==0) return;
		EyeFrame f = buffer.getFrames().get(size-1);
		int v = 0;
		if(axis.equals("X"))
			v = f.eyeX;
		else
			v= -f.eyeY;
		rangeMin = v - 2500;
		rangeMax = v + 2500;
		
		System.out.println("v="+v+" min="+rangeMin+" max="+rangeMax);
		
	}

	@Override
	public void paint(Graphics g) {
		width = getSize().width;
		height = getSize().height;

		super.paint(g);
		g.setColor(new Color(255,255,255));
		g.fillRect(x,y,width,height);
		g.setColor(new Color(0,0,0));
		g.drawRect(x,y,width,height);

		g.setColor(new Color(200,200,200));
		for(int i=0; i<width; i+=1000/divider)
			g.drawLine(i, 0, i, height);


		g.setColor(new Color(0,0,0));

		int size = buffer.getFrames().size();
		int elements = width;
		//punkty poprzednie
		int skok = divider; //(int)((double)elements/(double)divider);
		int start = size - elements*divider;
		if(start<=0) start = 0;

		//g.drawString("["+size+"] "+buffer.hashCode(), 500, 50);
		int lastX=0;
		int lastY=0;

		double scale = (double)height/(double)(rangeMax-rangeMin);
		//		System.out.println(scale);

		int x = 0;
		for(int i=start;i<size;i+=skok) {
			//	System.out.println("Reading "+(i+start));
			EyeFrame f = buffer.getFrames().get(i);

			int signal = 0;
			if(axis.equals("X"))
				signal = f.eyeX;
			else
				signal = -f.eyeY;

			signal = (int)((double)(signal-rangeMin)*scale);

			g.drawLine(lastX, lastY,x, signal);

			if(size-i<skok) //dla ostatniego 
				g.drawString("["+f.eyeX+","+f.eyeY+"] <"+signal+">", 5, /*((i-frames+2)*20)+*/50);
			lastX = x;
			lastY = signal;
			x++;
			//g.drawString("["+i+","+f.eyeY+"]", 500, (i*20)+50);
		}

		g.setColor(new Color(220,220,220));
		g.drawLine(0,height/2, width, height/2);
		g.setColor(new Color(0,0,0));
		g.drawString("["+rangeMin+" "+rangeMax+"] "+(rangeMax+rangeMin)/2, 10, 20);

//		g.fillRect(10, 10, 10, 10);
//		g.fillRect(30, 10, 10, 10);
//		g.drawString(""+divider, 50, 10);
	}

}
