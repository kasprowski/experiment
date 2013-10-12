package pl.kasprowski.buffer;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Podstawowa klasa przechowuj¹ca wyniki badania, czyli odczyty z eyetracker i informacje wrzucane podczas eksperymentu
 * (np. zmiana po³o¿enia punktu, zmiana obrazu itp) 
 * Obiekt mo¿na serializowaæ.
 * @author pawel
 *
 */
public class FrameBuffer implements IFrameBuffer, Serializable{
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(FrameBuffer.class);

	/**
	 * Lista frames to wszystkie odczytane ramki (to co przysz³o z eyetrackera)
	 */
	private List<EyeFrame> frames = new ArrayList<EyeFrame>();

	/**
	 * Lista timestamps to dowolne obiekty wrzucone podczas eksperymentu (np informacja o punkcie stymulacji albo "tu zaczyna siê kolejny etap"
	 * 
	 */
	private List<Timestamp> timestamps = new ArrayList<Timestamp>();
	/**
	 * Stat to zwyk³y rekord na statystyki pomiarów (ile b³edów itp) 
	 */
	Stat stat = new Stat();
	/**
	 * Dodane po to, ¿eby podczas serializacji obiektu nie da³o siê dorzucaæ nowych ramek
	 */
	boolean serializing = false;
	private String name;


	public Stat getStat() {
		return stat;
	}


	//
	//	public void setStat(Stat stat) {
	//		this.stat = stat;
	//	}
	//

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	/**
	 * Dodawanie nowej ramki - zwykle przez jak¹œ klasê pl.kasprowski.jazz.XXXReader
	 */
	@Override
	public void addFrame(EyeFrame frame) {
		if(!serializing)
			frames.add(frame);
	}

	@Override
	public List<EyeFrame> getFrames() {
		return frames;
	}

	/**
	 * Zapis do pliku tekstowego - bez timestampów i ju¿ raczej nie u¿ywam
	 */
	@Deprecated
	@Override
	public void save(String fileName) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName),false));
			for(EyeFrame frame: frames) {
				//bw.write(frame.eye_x[0]+"\t"+frame.eye_x[1]+"\t"+frame.eye_y[0]+"\t"+frame.eye_y[1]+"\t"+(frame.crcOK?"OK":"ERROR"));
				bw.write(frame.toString());
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//	public void saveToTSV(String fileName) {
	//		try {
	//			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName),false));
	//			for(EyeFrame frame: frames) {
	//				//bw.write(frame.eye_x[0]+"\t"+frame.eye_x[1]+"\t"+frame.eye_y[0]+"\t"+frame.eye_y[1]+"\t"+(frame.crcOK?"OK":"ERROR"));
	//				//bw.write(frame.counter+"\t"+frame.eye_x[0]+"\t"+frame.eye_y[0]);
	//				//bw.write(frame.toString());
	//				bw.write("\n");
	//			}
	//			bw.close();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//
	//	}

	@Override
	public void addTimestamp(Timestamp timestamp) {
		timestamps.add(timestamp);

	}

	//	public List<Timestamp> getTimestamps() {
	//		return timestamps;
	//	}

	/**
	 * Zwraca ostatni obiekt zapisany przed podanym momentem w milisekundach
	 * Mo¿e to byæ np punkt, gdzie znajduje siê stymulacja
	 */
	@Override
	public Object findObjectForTimestamp(long stamp) {
		return findTimestamp(stamp).getData();
	}

	@Override
	public Timestamp getLastTimestamp() {
		int size = timestamps.size()-1;
		if(size<0) return null; 
		return timestamps.get(size);
	}

	/**
	 * Zwraca ostatni obiekt timestamp zapisany przed podanym momentem w milisekundach
	 * Mo¿e to byæ np punkt, gdzie znajduje siê stymulacja
	 */
	@Override
	public Timestamp findTimestamp(long stamp) {
		//log.debug("Timestamp: "+stamp);
		if(timestamps.size()==0) return null;
		Timestamp o = timestamps.get(0);
		for(Timestamp timestamp:timestamps) {
			//log.debug("current: "+timestamp.getTimestamp());
			if(timestamp.getTimestamp()<stamp) {
				o = timestamp;
				//	log.debug("Setting as new!");
			}
		}
		return o;
	}

	/**
	 * Zapis ramek do pliku - razem z odpowiadaj¹cymi timestampami
	 * @param fileName
	 */
	public void saveInTable(String fileName) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName),false));
			for(EyeFrame frame: frames) {
				bw.write(frame.counter+"\t"+frame.timestamp+"\t"+frame.eyeX+"\t"+frame.eyeY+"\t");
				Timestamp t = findTimestamp(frame.timestamp);
				bw.write(t.getTimestamp()+"\t"+((Point)t.getData()).x+"\t"+((Point)t.getData()).y);
				//bw.write(frame.counter+"\t"+frame.eye_x[0]+"\t"+frame.eye_y[0]);
				//bw.write(frame.toString());
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}


	/**
	 * Zapisanie ca³ego obiektu do pliku binarnego z domyœln¹ nazw¹
	 */
	public void serialize() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
		String fileName = "eye"+sdf.format(new Date())+".ser";
		serialize(fileName);

		//		log.debug("Saved in file: "+fileName);

	}

	/**
	 * Zapisanie ca³ego obiektu do pliku binarnego z domyœln¹ nazw¹ ale o podanym prefiksie
	 */
	@Override
	public void serializeP(String prefix) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
		String fileName = prefix+"_"+sdf.format(new Date())+".ser";
		serialize(fileName);
	}

	/**
	 * Zapisanie ca³ego obiektu do pliku binarnego o podanej nazwie
	 */
	public void serialize(String fileName) {
		serializing = true;
		BufferNormalizer.normalize(this);
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
			out.writeObject(this);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		serializing = false;
	}

	/**
	 * Odczyt z pliku o podanej nazwie ca³ego bufora
	 * @param fileName
	 * @return pe³ny bufor lub null
	 */
	public static IFrameBuffer deserialize(String fileName) {
		Object o=null;
		try{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			o = in.readObject();
			in.close();
		}catch(IOException ex) {ex.printStackTrace();}
		catch(ClassNotFoundException ex) {ex.printStackTrace();}
		if(o instanceof IFrameBuffer)
			return (FrameBuffer)o;
		else return null;
	}



	/**
	 * Redukuje liczbê ramek o divider 
	 * Uœrednia ramki z podanego zakresu i zapisuje jako jedn¹ ramkê  
	 * @param divider o ile zminiejszyæ liczbê ramek
	 */
	public void reduceFrequencyByDiv(int divider) {
		List<EyeFrame> newFrames = new ArrayList<EyeFrame>();

		for(int i=0;i<frames.size()-divider;i+=divider) {
			EyeFrame newFrame = new EyeFrame();
			newFrame.timestamp = frames.get(i).timestamp;
			for(int j=i;j<i+divider-1;j++) {
				newFrame.eyeX += frames.get(j).eyeX;
				newFrame.eyeY += frames.get(j).eyeY;
				newFrame.pulI += frames.get(j).pulI;
				newFrame.pulR += frames.get(j).pulR;
			}
			newFrame.eyeX /= divider;
			newFrame.eyeY /= divider;
			newFrame.pulI /= divider;
			newFrame.pulR /= divider;

			newFrames.add(newFrame);
		}
		frames = newFrames;
	}

	@Override
	public void clear() {
		frames = new ArrayList<EyeFrame>();
		timestamps = new ArrayList<Timestamp>();

	}

	/**
	 * Wycina z bufora ramki i timestampy z zakresu (start,end)
	 */
	public IFrameBuffer getBufferWithFramesBetween(long start, long end) {
		log.debug("start="+new Date(start)+" end="+new Date(end));
		IFrameBuffer newBuffer = new FrameBuffer();
		serializing = true;
		try{
			int fNo = 0;
			for(EyeFrame frame: frames) {
				//log.debug("frame: "+frame.timestamp );
				if(frame.timestamp>start && (frame.timestamp<end || end==-1)) {
					newBuffer.addFrame(frame.clone());
					//log.debug("Adding frame");
					fNo++;
				}
			}
			int tNo = 0;
			for(Timestamp tm: timestamps) {
				//log.debug("frame: "+frame.timestamp );
				if(tm.getTimestamp()>start && (tm.getTimestamp()<end || end==-1)) {
					newBuffer.addTimestamp(tm.clone());
					//log.debug("Adding timestamp");
					tNo++;
				}
			}
			Stat stat = new Stat();
			stat.setStart(newBuffer.getFrames().get(0).timestamp);
			stat.setEnd(newBuffer.getFrames().get(newBuffer.getFrames().size()-1).timestamp);
			stat.setNoFrames(newBuffer.getFrames().size());
			//TODO pozosta³e parametry!
			((FrameBuffer)newBuffer).stat = stat;

			log.debug("Added "+fNo+" frames and "+tNo+" timestamps");
		}catch(CloneNotSupportedException ex) {
			System.err.println("Clone not supported?");
		}
		serializing = false;
		return newBuffer;
	}

	@Override
	public List<Timestamp> getTimestamps() {
		return timestamps;
	}

	
	/**
	 * Zwraca timestamp zawieraj¹cy obiekt o podanej wartoœci
	 */
	@Override
	public Timestamp findTimestamp(Object o) {
		long after = 0;
		return findTimestamp(o,after);
	}

	/**
	 * Zwraca timestamp zawieraj¹cy obiekt o podanej wartoœci po podanym w ms momencie czasu
	 */
	public Timestamp findTimestamp(Object o, long after) {
		System.out.println("Searching for: "+o);
		if(log==null) System.out.println("NULL!!");
		log.debug("Searching for: "+o);
		for(Timestamp tm:timestamps) {
			//		log.debug("found: "+tm.getData()+" "+new Date(tm.getTimestamp()));
			if(tm.getTimestamp()>after && tm.getData().equals(o)) {
				//			log.debug("EQUALS! "+new Date(tm.getTimestamp()));
				return tm;
			}
		}
		return null;
	}


	/**
	 * Zwraca timestamp zawieraj¹cy String o podanej wartoœci
	 */
	@Override
	public Timestamp findStringTimestamp(String s) {
		long after = 0;
		return findStringTimestamp(s,after);
	}

	/**
	 * Zwraca timestamp zawieraj¹cy String o podanej wartoœci po podanym momencie
	 */
	public Timestamp findStringTimestamp(String s, long after) {
		log.debug("Searching for String: "+s);
		for(Timestamp tm:timestamps) {
			//		log.debug("found: "+tm.getData()+" "+new Date(tm.getTimestamp()));
			if(tm.getData() instanceof String) { 
				String data = (String)tm.getData();
				if(tm.getTimestamp()>after && data.startsWith(s)) {
					//			log.debug("EQUALS! "+new Date(tm.getTimestamp()));
					return tm;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return name+"("+getFrames().size()+")";
	}


	/**
	 * Zwraca tablicê wszystkich odczytów osi X ze wszystkich ramek
	 */
	public double[] getX() {
		int size = getFrames().size();
		double[] x = new double[size];
		for(int i=0;i<size;i++) {
			x[i] = getFrames().get(i).eyeX;
		}
		return x;
	}

	/**
	 * Zwraca tablicê wszystkich odczytów osi Y ze wszystkich ramek
	 */
	public double[] getY() {
		int size = getFrames().size();
		double[] y = new double[size];
		for(int i=0;i<size;i++) {
			y[i] = getFrames().get(i).eyeY;
		}
		return y;
	}


	/**
	 * Dodaje na pocz¹tku pliku podany Timestamp
	 */
	@Override
	public void addTimestampAtStart(Timestamp timestamp) {
		timestamps.add(0,timestamp);
	}

}