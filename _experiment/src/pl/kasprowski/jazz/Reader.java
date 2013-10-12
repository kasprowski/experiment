package pl.kasprowski.jazz;

import pl.kasprowski.buffer.IFrameBuffer;

/**
 * Reader when started adds frames to buffer
 * @author pawel
 *
 */
public interface Reader {
	public void start(IFrameBuffer buffer);
	public void stop();
	public void setPortName(String portName);
	public IFrameBuffer getBuffer();
}
