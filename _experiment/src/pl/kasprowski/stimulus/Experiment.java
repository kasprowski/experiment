package pl.kasprowski.stimulus;

import java.awt.Graphics;

import javax.swing.JPanel;

import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.buffer.FrameBuffer;
import pl.kasprowski.jazz.Reader;

/**
 * Shows some information in JPanel and is able to add timestamps to buffer.
 * Creates buffer and adds timestamps to the buffer.
 * It may start and stop reader that reads information and adds data to the buffer.
 * When work is finished reports about it using callback
 * @author pawel
 *
 */
public abstract class Experiment extends JPanel implements Runnable{
	private static final long serialVersionUID = 1L;

	protected ExperimentCallback callback;
	protected Reader reader;
	protected Thread stimThread;
	protected boolean isRunning = false;
	protected IFrameBuffer buffer = new FrameBuffer();

	public Experiment(ExperimentCallback callback, Reader reader) {
		this.callback = callback;
		this.reader = reader;
	}
	
	/**
	 * Starts stimulus (+reader)
	 */
//	private void start() {
//		reader.start(buffer);
//		stimThread = new Thread(this);
//		stimThread.start();
//	}

	@Override
	public abstract void run();
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}
}
