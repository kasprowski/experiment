package pl.kasprowski.buffer;

import java.io.Serializable;

public class EyeFrame implements Serializable, Cloneable{
	private static final long serialVersionUID = 1L;

	public int eyeX;
	public int eyeY;
	public int pulI;
	public int pulR;
	public int counter;
	public long timestamp;
	
	@Override
	public String toString() {
		return counter+"\t"+eyeX+"\t"+eyeY+"\t"+pulI+"\t"+pulR;
	}
	
	@Override
	protected EyeFrame clone() throws CloneNotSupportedException {
		return (EyeFrame)super.clone();
	}
}
