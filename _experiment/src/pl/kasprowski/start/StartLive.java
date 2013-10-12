package pl.kasprowski.start;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.jazz.FakeReaderStaticPoint;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExpLive2;
import pl.kasprowski.stimulus.ExperimentCallback;

/**
 * Startuje eksperyment ExpLive2
 * @author pawel
 *
 */
public class StartLive extends JFrame{
	private static final long serialVersionUID = 1L;

	Logger log = Logger.getLogger(StartCalibrator.class);
	Reader reader = new FakeReaderStaticPoint();

	public static void main(String[] args) {
		new StartLive(null);
	}
	public StartLive(Reader rd) {
		JPanel cp = (JPanel)this.getContentPane();
		cp.setLayout(new BorderLayout());
		
		if(rd!=null) reader = rd;
//		reader = new ComReader("COM6");
		
		cp.add(new ExpLive2(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				System.out.println("OK");
			}
		},reader));
		//setSize(700,700);
		setSize(1000,780);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
}
