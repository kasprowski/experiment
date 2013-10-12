package pl.kasprowski.buffer;

import java.io.Serializable;
import java.util.Date;

public class Stat implements Serializable{
	private static final long serialVersionUID = 1L;


	//public final static Stat s = new Stat();
	public void reset() {
		noFrames = 0;
		noEyeFrames = 0;
		noCRCErrors = 0;
		noOffsetChanges = 0;
		noCounterErrors = 0;
	}
	public Stat() {
		reset();
	}
	private long start;
	private long end;
	private int noFrames = 0;
	private int noEyeFrames = 0;
	private int noCRCErrors = 0;
	private int noOffsetChanges = 0;
	private int noCounterErrors = 0;
	
	
	public void incFrames() {
		noFrames++;
	}
	
	public void incEyeFrames() {
		noEyeFrames++;
	}
	public void incCRCErrors() {
		noCRCErrors++;
	}
	public void incOffsetChanges() {
		noOffsetChanges++;
	}
	public void incCounterErrors() {
		noCounterErrors++;
	}

	
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}
	public int getNoFrames() {
		return noFrames;
	}
	public void setNoFrames(int noFrames) {
		this.noFrames = noFrames;
	}
	public int getNoEyeFrames() {
		return noEyeFrames;
	}
	public void setNoEyeFrames(int noEyeFrames) {
		this.noEyeFrames = noEyeFrames;
	}
	public int getNoCRCErrors() {
		return noCRCErrors;
	}
	public void setNoCRCErrors(int noCRCErrors) {
		this.noCRCErrors = noCRCErrors;
	}
	public int getNoOffsetChanges() {
		return noOffsetChanges;
	}
	public void setNoOffsetChanges(int noOffsetChanges) {
		this.noOffsetChanges = noOffsetChanges;
	}
	public int getNoCounterErrors() {
		return noCounterErrors;
	}
	public void setNoCounterErrors(int noCounterErrors) {
		this.noCounterErrors = noCounterErrors;
	}
	@Override
	public String toString() {
		String fps = "0";
		if(end-start<1000) {
			fps = ""+noFrames;
		}
		else
		if(((end-start)/1000)!=0)			
				fps = ""+noFrames/ ((end-start)/1000);
		return
				"======\n"+
				"Start= "+new Date(start)+"\n"+
				"Frames= "+noFrames+"\n"+
				"EyeFrames= "+noEyeFrames+"\n"+
				"CRCErrors= "+noCRCErrors+"\n"+
				"OffsetChanges= "+noOffsetChanges+"\n"+
				"CounterErrors= "+noCounterErrors+"\n"+
				"End= "+new Date(end)+"\n"+
				"Seconds= "+(end-start)/1000+"\n"+
				"FPS= "+ fps  +"\n"+
				"======\n"
				;
		
	}
}
