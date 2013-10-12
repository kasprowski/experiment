package pl.kasprowski.start;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.calibration.CalibratorRegression;
import pl.kasprowski.jazz.ComReader;
import pl.kasprowski.jazz.FakeReaderStim;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExpVisualizer;
import pl.kasprowski.stimulus.Experiment;
import pl.kasprowski.stimulus.ExperimentCallback;
import pl.kasprowski.stimulus.rfl.ExpCalibrationStim9;

/**
 * Pokazuje 9 punktow¹ kalibracjê (ExpCalibrationStim9) a potem ekran z gazepointem (ExpVisualizer)
 * Czyli dwa eksperymenty jeden po drugim
 * @author pawel
 *
 */
public class StartCalibrator extends JFrame{
	private static final long serialVersionUID = 1L;

	Logger log = Logger.getLogger(StartCalibrator.class);

	Experiment expPanel;


	public static void main(String[] args) {
		new StartCalibrator(null);
	}

	Reader reader = new FakeReaderStim();
	
	public StartCalibrator(Reader rd) {
		JPanel cP = (JPanel)this.getContentPane();
		cP.setLayout(new BorderLayout());

		
		//TODO Tu wpisaæ w³aœciwy numer portu
		//reader = new ComReader("COM14");
		if(rd!=null) this.reader = rd;
		
		cP.add(showCalibrator(reader));

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setSize(1000,780);
		//pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}


	/**
	 * Uruchamia kalibrator, jak siê kalibrator skoñczy to uruchomi siê metoda calibrate(buffer)
	 * @param reader
	 * @return
	 */
	private JPanel showCalibrator(Reader reader) {
		expPanel = new ExpCalibrationStim9(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				calibrate(buffer);
			}},reader);
		return expPanel;

	}


	/**
	 * Wylicza wspó³czynniki na podstawie danych
	 * @param buffer
	 */
	private void calibrate(IFrameBuffer buffer) {
		//System.out.println("Frames: "+ buffer.getFrames().size());
		//System.out.println("Timestamps: "+ buffer.getTimestamps().size());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {e.printStackTrace();}

		//zapis na dysk
		buffer.serialize();
		

		//CalibratorMinmax c = new CalibratorMinmax(buffer);
		//CalibratorLevels c = new CalibratorLevels(buffer);
		CalibratorRegression c = new CalibratorRegression(buffer);

		c.calculate();
		
		log.debug("X Coeficients: "+c.getCoefX()[0]+","+c.getCoefX()[1]);
		log.debug("Y Coeficients: "+c.getCoefY()[0]+","+c.getCoefY()[1]);
		
		
		setVisible(false);
		getContentPane().remove(expPanel);

		// uruchamia wizualizer, podaj¹c mu wyliczone wspó³czynniki
		expPanel = new ExpVisualizer(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				analyze(buffer);
			}},	reader, 
			c.getCoefX(), c.getCoefY(),new double[]{0,0});
		getContentPane().add(expPanel);
		repaint();
		setVisible(true);


	}	
	
	/**
	 * Tu nic ju¿ nie ma - ale móg³by byc kolejny Experiment
	 * @param buffer
	 */
	private void analyze(IFrameBuffer buffer) {
		//buffer = c.calibrate();
		System.out.println("TODO: analiza");
	}



}
