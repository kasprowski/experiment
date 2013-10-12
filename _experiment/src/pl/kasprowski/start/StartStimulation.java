package pl.kasprowski.start;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.jazz.ComReader;
import pl.kasprowski.jazz.FakeReaderStaticPoint;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExperimentCallback;
import pl.kasprowski.stimulus.chart.ExpCharts;

/**
 * Startuje wybran¹ jedn¹ stymulacjê.
 * Aktualnie ExpCharts - wykres na ¿ywo
 * @author pawel
 *
 */
public class StartStimulation extends JFrame{
	private static final long serialVersionUID = 1L;

	Logger log = Logger.getLogger(StartStimulation.class);
	Reader reader = new FakeReaderStaticPoint();

	public static void main(String[] args) {
		new StartStimulation();
	}

	public StartStimulation() {
		JPanel cp = (JPanel)this.getContentPane();
		cp.setLayout(new BorderLayout());
//		reader = new ComReader("COM14");

		cp.add(new ExpCharts(new ExperimentCallback() {
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
