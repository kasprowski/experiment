package pl.kasprowski.buffer;

import java.io.Serializable;
import java.util.Date;

public class Timestamp implements Serializable,Cloneable{
	private static final long serialVersionUID = 1L;

	private long timestamp;
	private Object data;
	
	public Timestamp(long timastamp, Object data) {
		this.timestamp = timastamp;
		this.data = data;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Object getData() {
		return data;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String ret = "["+new Date(timestamp)+"] "+getData()+" ("+getData().getClass().getName()+")";
		return ret;
	}
	
	@Override
	protected Timestamp clone() throws CloneNotSupportedException {
		return (Timestamp)super.clone();
	}

}
