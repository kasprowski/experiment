package pl.kasprowski.stimulus.photo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;

import pl.kasprowski.buffer.Timestamp;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.Experiment;
import pl.kasprowski.stimulus.ExperimentCallback;
import pl.kasprowski.tools.Calc;
import pl.kasprowski.tools.Graph;

/**
 * Rozbudowany eksperyment pokazuj¹cy zdjêcia i rejstruj¹cy input od u¿ytkownika
 * Pomiêdzy zdjêciami trzypunktowe kalibracje
 * Uwaga! Obrazki ³adowane z katalogu sdir, potrzebny te¿ plik 'points' aby wykadrowaæ obrazki
 * Na koniec gotowy bufor.
 * @author pawel
 *
 */
public class ExpPhoto extends Experiment {
	private static final long serialVersionUID = 1L;

	Point leftEye = new Point();
	Point rightEye = new Point();
	int eyeDistance = 400;
	int eyeShiftX = 390;
	int eyeShiftY = 200;

	/**
	 * Katalog ze zdjêciami
	 */
	String sdir = "c:/_faces/f2";
	
	class EyePosition {
		String filename;
		Point leftEye = new Point();
		Point rightEye = new Point();
	}


	Map<String,EyePosition> positions = new HashMap<String,EyePosition>();

	
	List<ImageIcon> imgs = new ArrayList<ImageIcon>();
	//List<ExpPhotoResult> results = new ArrayList<ExpPhotoResult>();
	int crImg = 0;

	public ExpPhoto(ExperimentCallback callback, Reader reader) {
		super(callback, reader);
		buildGui();
		loadImgs();
		//buffer = new FrameBuffer();
		buffer = reader.getBuffer();
		buffer.addTimestamp(new Timestamp(new Date().getTime(),"START faces!"));
		try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
		buffer.addTimestamp(new Timestamp(new Date().getTime(),"[STARTIMG] "+imgs.get(crImg).getDescription()));
		reader.start(buffer);
	}

	/**
	 * Zapamiêtujemy wynik rozpoznawania i nazwê obrazka jako timestamp
	 * @param known
	 */
	private void pressed(boolean known) {
		if(inPoint) return;
		inPoint = true;
		String imgName = imgs.get(crImg).getDescription();
		System.out.println("Pressed "+imgName+" >"+known);

		//		//TODO: user name
		//PhotoResultTimestamp result = new PhotoResultTimestamp(imgName,known);
		String result = "[ENDIMG] "+imgName+" <"+(known?"true":"false")+">";
		buffer.addTimestamp(new Timestamp(new Date().getTime(),result));
		showPoint();
	}
	Point p = new Point(0,0);
	boolean inPoint = false;
	Random r = new Random();

	private void showPoint(){	
		//pokazuje punkt na œrodku a potem w losowym miejscu obok

		buffer.addTimestamp(new Timestamp(new Date().getTime(),"[STARTPOINT]"));


		repaint();
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					Thread.sleep(2000); //œrodek
					// nowy punkt zawsze w odleg³oœci rombu 900
//					int newX = r.nextInt(1800)-900;
//					int newY = 900 - Math.abs(newX);
//					newY = newY * (r.nextInt(2)*2-1); //-1 lub 1

					int newX = 700 * (r.nextInt(2)*2-1);
					int newY = 700 * (r.nextInt(2)*2-1);
					p = new Point(newX,newY);
					buffer.addTimestamp(new Timestamp(new Date().getTime(),p));
					repaint();
					Thread.sleep(2000); //bok
					p = new Point(0,0);
					buffer.addTimestamp(new Timestamp(new Date().getTime(),p));
					repaint();
					Thread.sleep(2000); //œrodek
					newX = 700 * (r.nextInt(2)*2-1);
					newY = 700 * (r.nextInt(2)*2-1);
					p = new Point(newX,newY);
					buffer.addTimestamp(new Timestamp(new Date().getTime(),p));
					repaint();
					Thread.sleep(2000); //bok2

				
				
				} catch (InterruptedException e) {e.printStackTrace();}
				inPoint = false;
				buffer.addTimestamp(new Timestamp(new Date().getTime(),"[ENDPOINT]"));
				p = new Point(0,0);
				nextImg();
			}});
		thread.start();
	}




	private void nextImg() {
		if(crImg<imgs.size()-1) {
			crImg++;
			buffer.addTimestamp(new Timestamp(new Date().getTime(),"[STARTIMG] "+imgs.get(crImg).getDescription()));
			repaint();
		}
		else {
			reader.stop();
			callback.finished(buffer);
		}
	}

	//	public void prevImg() {
	//		if(crImg>0)
	//			crImg--;
	//		repaint();
	//	}

	private void buildGui() {
		setLayout(new BorderLayout());
		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e){}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				int button = e.getButton();
				pressed(button == MouseEvent.BUTTON1);
				//				if(button == MouseEvent.BUTTON1)
				//					nextImg();
				//				if(button == MouseEvent.BUTTON3)
				//					prevImg();
			}
		});
	}

	private void loadImgs() {
		
		File dir = new File(sdir);
		for(String fname:dir.list()) {
			ImageIcon img = new ImageIcon(sdir+"/"+fname);
			img.setDescription(fname);
			imgs.add(img);
		}
	
		// 100 razy wyci¹ga obrazek ze œrodka i wstawia na pocz¹tku
		Random r = new Random();
		for(int i=0;i<100;i++) {
			int x = r.nextInt(imgs.size());
			ImageIcon img = imgs.get(x);
			imgs.remove(x);
			imgs.add(0,img);
		}
		
		
		
		File file = new File("points");
		try {
			if (file.exists() && file.isFile()) {
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String line;
				while ((line = br.readLine()) != null) {
					EyePosition ep = new EyePosition();
					String[] x = line.split("\\t");
					System.out.println(x[0]+"..."+x[1]+"..."+x[2]);
					ep.filename = x[0];
					String[] l = x[1].split(",");
					System.out.println(l[0]+"..."+l[1]);
					ep.leftEye.x = Integer.parseInt(l[0]);
					ep.leftEye.y = Integer.parseInt(l[1]);
					String[] rr = x[2].split(",");
					ep.rightEye.x = Integer.parseInt(rr[0]);
					ep.rightEye.y = Integer.parseInt(rr[1]);
					positions.put(ep.filename, ep);
				}
				br.close();
			}
		}catch (IOException ioe) {ioe.printStackTrace();}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if(inPoint) {
			double x = Calc.recalc(p.x, -1000, 1000, 0, this.getSize().width) ;
			double y = Calc.recalc(p.y, -1000, 1000, 0, this.getSize().height) ;
			//g.fillRect((int)x,(int)y,1,1);
			g.setColor(new Color(0,0,0));
			Graph.point(g, (int)x, (int)y, 20);
			g.setColor(new Color(255,255,255));
			Graph.point(g, (int)x, (int)y, 2);
			
		}
		else{

			ImageIcon image = imgs.get(crImg);
			
			//image.paintIcon(this, g, 0, 0);
				
			leftEye = positions.get(image.getDescription()).leftEye;
			rightEye = positions.get(image.getDescription()).rightEye;
			
			//g.fillOval(leftEye.x-5, leftEye.y-5, 10, 10);
			//g.fillOval(rightEye.x-5, rightEye.y-5, 10, 10);
			int h = image.getIconHeight();
			int w = image.getIconWidth();
			
			int wEye = Math.abs(rightEye.x - leftEye.x);

			double zoom = (double)eyeDistance/(double)wEye;
			System.out.println("wEye="+wEye+" zoom="+zoom);
			BufferedImage img =  new BufferedImage(image.getIconWidth(), image.getIconHeight(), BufferedImage.TYPE_INT_RGB);
			image.paintIcon(null, img.getGraphics(), 0, 0);

			int shiftX = eyeShiftX - (int)(leftEye.x*zoom); 
			int shiftY = eyeShiftY - (int)(leftEye.y*zoom);
			g.drawImage(img, shiftX, shiftY, (int)(w*zoom), (int)(h*zoom), null);

			
			///---
//			//		image.paintIcon(this, g, 0, 0);
//
//			int h = image.getIconHeight();
//			int w = image.getIconWidth();
//			//		double ratio = (double)w/(double)h;
//			//		System.out.println("ratio "+h+","+w+" = "+ratio + " width="+(int)(this.getSize().width*ratio));
//
//			int[] xy = calcSize(w,h,this.getSize().width,this.getSize().height);
//			//		
//			BufferedImage img =  new BufferedImage(image.getIconWidth(), image.getIconHeight(), BufferedImage.TYPE_INT_RGB);
//			image.paintIcon(null, img.getGraphics(), 0, 0);
//			//g.drawImage(img, 0, 0, (int)(this.getSize().width*ratio), this.getSize().height, null);
//			int shiftX = (this.getSize().width - xy[0])/2; 
//			g.drawImage(img, shiftX, 0, xy[0], xy[1], null);
		}

	}

	private int[] calcSize(int ox, int oy, int nx, int ny) {
		int[] xy = new int[2];
		double r = (double)ny/(double)oy;
		//	System.out.println("ox: "+ox+" oy: "+oy+" r:"+r);
		//		if(oy>ny) {
		//			ry = ny/oy;
		//		}
		xy[0] = (int)(ox*r);
		xy[1] = (int)(oy*r);
		return xy;
	}
}
