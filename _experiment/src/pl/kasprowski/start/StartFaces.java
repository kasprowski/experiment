package pl.kasprowski.start;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.buffer.Timestamp;
import pl.kasprowski.jazz.FakeReaderStim;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.Experiment;
import pl.kasprowski.stimulus.ExperimentCallback;
import pl.kasprowski.stimulus.photo.ExpPhoto;
import pl.kasprowski.stimulus.rfl.ExpCalibrationCircle;
import pl.kasprowski.stimulus.rfl.ExpCalibrationStim16;

/**
 * Eksperyment z ogl¹daniem obrazków (twarzy)
 * Zaczyna od kalibracji 4 punktowej, potem rusza z ExpPhoto, na koniec ExpCircle
 * Nie robi ¿adanej kalibracji tylko zapisuje wszystko do bufora
 * @author pawel
 *
 */
public class StartFaces extends JFrame{
	private static final long serialVersionUID = 1L;

	Logger log = Logger.getLogger(StartFaces.class);
	Experiment expPanel;


	public static void main(String[] args) {
		new StartFaces(null);
	}

	Reader reader = new FakeReaderStim();
	
	public StartFaces(Reader rd) {
		
		JPanel cP = (JPanel)this.getContentPane();
		cP.setLayout(new BorderLayout());

		if(rd!=null) reader = rd;
		//reader = new ComReader("COM14");
		//setDefaultCloseOperation(EXIT_ON_CLOSE);

		setSize(1200,720);
		
		setLocationRelativeTo(null);
		setVisible(true);
		
		step1();
		//step2(new FrameBuffer());
	}

	/**
	 * Kalibracja 4 punktowa
	 */
	private void step1() {
		setTitle("Kalibracja");
		expPanel = new ExpCalibrationStim16(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				step2(buffer);
			}},reader);


		getContentPane().add(expPanel);
		repaint();
		setVisible(true);
		
	}	

	/**
	 * Twarze
	 * @param buffer
	 */
	private void step2(IFrameBuffer buffer) {
		buffer.serializeP("calib");

		setTitle("Rozpoznawanie twarzy");
//		setVisible(false);
		getContentPane().remove(expPanel);

		expPanel = new ExpPhoto(new ExperimentCallback() {
		public void finished(IFrameBuffer buffer) {
			step3(buffer);
		}},reader);
		
		getContentPane().add(expPanel);
		repaint();
		setVisible(true);
		
	}

	/**
	 * Circle
	 */
	private void step3(IFrameBuffer buffer) {
		buffer.serializeP("faces");
		buffer.addTimestamp(new Timestamp(new Date().getTime(),"START circle!"));

		setTitle("Kó³ko");
		setVisible(false);
		getContentPane().remove(expPanel);

//		JPanel thanksPanel = new JPanel();
		expPanel = new ExpCalibrationCircle(new ExperimentCallback() {
		public void finished(IFrameBuffer buffer) {
			step4(buffer);
		}},reader);

		getContentPane().add(expPanel);
		repaint();
		setVisible(true);
		
	}	

	/**
	 * Podziêkowanie
	 * @param buffer
	 */
	private void step4(IFrameBuffer buffer) {
		buffer.addTimestamp(new Timestamp(new Date().getTime(),"STOP circle!"));
		buffer.serializeP("circle");


		setTitle("Podziêkowanie");
		setVisible(false);
		getContentPane().remove(expPanel);

		JPanel thanksPanel = new JPanel();
		thanksPanel.add(new JLabel("Dziêkujemy!"));
		
//		expPanel = new ExpCalibrationStim9(new ExperimentCallback() {
//			public void finished(IFrameBuffer buffer) {
//				step2(buffer);
//			}},reader);
//
//
//		expPanel = new ExpCalibrationCircle(new ExperimentCallback() {
//		public void finished(IFrameBuffer buffer) {
//			step4(buffer);
//		}},reader);



		
		getContentPane().add(thanksPanel);
		repaint();
		setVisible(true);
	}
}
