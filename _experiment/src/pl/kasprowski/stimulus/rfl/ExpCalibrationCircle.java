package pl.kasprowski.stimulus.rfl;

import java.awt.Point;

import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExperimentCallback;

/**
 * Punkt krêci elipsê na ekranie
 * @author pawel
 *
 */
public class ExpCalibrationCircle extends ExpRFL {
	private static final long serialVersionUID = 1L;

	public ExpCalibrationCircle(ExperimentCallback callback, Reader reader) {
		super(callback,reader);
		sleep = 500;
	}
	
	
	
	@Override
	void showStimulus() throws InterruptedException{
		//int screenX = this.screenX/2;
		//int screenY = this.screenY/2;
		sskip = 100;
		int R = screenX/2;
		for(double alfa=0;alfa<=2*Math.PI;alfa+=Math.PI/32) {
			int cx = (int)(R * Math.cos(alfa));
			int cy = (int)(R * Math.sin(alfa));
			setP(new Point(cx,cy));
			Thread.sleep(sskip);
		}
		for(double alfa=2*Math.PI;alfa>=0;alfa-=Math.PI/32) {
			int cx = (int)(R * Math.cos(alfa));
			int cy = (int)(R * Math.sin(alfa));
			setP(new Point(cx,cy));
			Thread.sleep(sskip);
		}

		for(double alfa=0;alfa<=2*Math.PI;alfa+=Math.PI/32) {
			int cx = (int)(R * Math.cos(alfa));
			int cy = (int)(R * Math.sin(alfa));
			setP(new Point(cx,cy));
			Thread.sleep(sskip);
		}
		for(double alfa=2*Math.PI;alfa>=0;alfa-=Math.PI/32) {
			int cx = (int)(R * Math.cos(alfa));
			int cy = (int)(R * Math.sin(alfa));
			setP(new Point(cx,cy));
			Thread.sleep(sskip);
		}

		
		Thread.sleep(sleep*2);

	}

	
//	void wander(Point from, Point to) throws InterruptedException {
//		System.out.println("Wandering "+from.x+","+from.y+" "+to.x+","+to.y);
//		
//		int skipx = (to.x-from.x)/skip;
//		//if(from.x>=to.x) skipx=-skipx;
//		int skipy = (to.y-from.y)/skip;
//		//if(from.y>=to.y) skipy=-skipy;
//	
//		int cx = from.x;
//		int cy = from.y;
//		boolean finish=false;
//		while(!finish) {
//			setP(new Point(cx,cy));
//			System.out.println(cx+","+cy);
//			Thread.sleep(sskip*4);
//			cx+=skipx;
//			//cy+=skipy;
//	//		cy = (int)(screenY * Math.sqrt(1-(cx*cx)/(screenX*screenX)));
//			cy = cx - screenY;
//			System.out.println(cx+","+cy);
//			if(skipx>=0	 && cx>to.x) finish=true;
//			if(skipx<0	 && cx<to.x) finish=true;
//			if(skipy>=0	 && cy>to.y) finish=true;
//			if(skipy<0	 && cy<to.y) finish=true;
//			//if(finish) System.out.println("F: "+skipx+" "+to.x+" "+cx);
//		}
//		Thread.sleep(sleep);
//		
//	}

}
