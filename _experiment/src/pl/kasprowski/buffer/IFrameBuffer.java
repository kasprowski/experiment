package pl.kasprowski.buffer;

import java.util.List;


public interface IFrameBuffer {
	public void addFrame(EyeFrame frame);
	public void addTimestamp(Timestamp timestamp);
	public List<EyeFrame> getFrames();
	public List<Timestamp> getTimestamps();
	public void save(String fileName);
	public Object findObjectForTimestamp(long timestamp);
	public Timestamp findTimestamp(long timestamp);
	public Timestamp getLastTimestamp();
	public Timestamp findTimestamp(Object o, long after);
	public Timestamp findTimestamp(Object o);
	public Timestamp findStringTimestamp(String s, long after);
	public Timestamp findStringTimestamp(String s);

	//public void saveWithTimestamps(String fileName);
	public Stat getStat();
	public void serialize(String fileName);
	public void serialize();
	public void serializeP(String prefix);
	
	
	public double[] getX();
	public double[] getY();
	
	
	public void clear();
	
	public IFrameBuffer getBufferWithFramesBetween(long start, long end);
	
	public void setName(String name);
	public String getName();
	
	/**
	 * Dodanie timestampu na pocz¹tek!
	 * @param timestamp
	 */
	public void addTimestampAtStart(Timestamp timestamp);
}
