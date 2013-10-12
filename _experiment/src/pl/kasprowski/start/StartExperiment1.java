package pl.kasprowski.start;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.calibration.CalibratorLevels;
import pl.kasprowski.calibration.CalibratorRegression;
import pl.kasprowski.exceptions.CalibrationException;
import pl.kasprowski.jazz.ComReader;
import pl.kasprowski.jazz.FakeReaderSimulator;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.Experiment;
import pl.kasprowski.stimulus.ExperimentCallback;
import pl.kasprowski.stimulus.rfl.ExpCalibrationCircle;
import pl.kasprowski.stimulus.rfl.ExpCalibrationStim4;
import pl.kasprowski.stimulus.rfl.ExpCalibrationStim9;

/**
 * Jeden pe³ny eksperyment:
 * step1 - kalibracja 4 punktowa
 * step2 - kalibracja 9 punktowa
 * step3 - kóleczko (raczej elipsa)
 * step4 - zadanie wizualne - TODO
 * Ka¿dy eksperyment zapisuje siê w osobnym pliku
 * @author pawel
 *
 */
public class StartExperiment1 extends JFrame{
	private static final long serialVersionUID = 1L;

	Logger log = Logger.getLogger(StartExperiment1.class);

	Experiment expPanel;


	public static void main(String[] args) {
		new StartExperiment1(null);
	}

	Reader reader = new FakeReaderSimulator();
	
	public StartExperiment1(Reader rd) {
		JPanel cP = (JPanel)this.getContentPane();
		cP.setLayout(new BorderLayout());

		if(rd!=null) reader = rd; 
		
		//reader = new ComReader("COM6");

		
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1000,780);
		//pack();
		setLocationRelativeTo(null);

		//step 1 - prosta kalibracja wg naro¿ników i œrodka
		step1();

	}

	/**
	 * Prosta kalibracja
	 */
	private void step1() {
		setTitle("Kalibracja nr 1");
		expPanel = new ExpCalibrationStim4(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				step2(buffer);
			}},reader);


		getContentPane().add(expPanel);
		repaint();
		setVisible(true);
		
	}	
	/**
	 * Analiza wyników kalibracji prostej
	 * Jeœli OK to kalibracja rozbudowana
	 * @param buffer
	 */
	private void step2(IFrameBuffer buffer) {

		//zapis
		buffer.serializeP("simple");

		//obliczenie coefs TYLKO dla naro¿ników!
//		CalibratorLevels c = new CalibratorLevels(buffer);
		CalibratorRegression c = new CalibratorRegression(buffer);
		try{
		if(!c.calculate()) {
			//kalibracja prosta nie posz³a - powtórzyæ!
//			JOptionPane.showMessageDialog(this, "Quality failure!");
//			setVisible(false);
//			getContentPane().remove(expPanel);
//			step1();
//			return;
		}
		}catch(CalibrationException ex) {
			System.err.println(ex.getMessage());
			JOptionPane.showMessageDialog(this, ex.getMessage());
		}

		
		
		
		//step2 - dok³adniejsza kalibracja
		setTitle("Kalibracja nr 2");
		setVisible(false);
		getContentPane().remove(expPanel);
		expPanel = new ExpCalibrationStim9(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				step3(buffer);
			}},reader);
		getContentPane().add(expPanel);
		repaint();
		setVisible(true);
	}

	
	/**
	 * Analiza wyników kalibracji rozbudowanej
	 * Jeœli OK - kó³ka w prawo i w lewo
	 * @param buffer
	 */
	private void step3(IFrameBuffer buffer) {
		//System.out.println("Frames: "+ buffer.getFrames().size());
		//System.out.println("Timestamps: "+ buffer.getTimestamps().size());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {e.printStackTrace();}
		//buffer.saveWithTimestamps("buffer.out");

		buffer.serializeP("ext");

		//CalibratorMinmax c = new CalibratorMinmax(buffer);
		CalibratorLevels c = new CalibratorLevels(buffer);
		if(!c.calculate())
			log.error("Error in calibration");
		log.debug("X Coeficients: "+c.getCoefX()[0]+","+c.getCoefX()[1]);
		log.debug("Y Coeficients: "+c.getCoefY()[0]+","+c.getCoefY()[1]);
		log.debug("Middle point: "+c.getMiddlePoint()[0]+","+c.getMiddlePoint()[1]);
		
		
		//step3: Kalibracja - kó³ka
		setTitle("Kalibracja nr 3 - kó³ko");
		setVisible(false);
		getContentPane().remove(expPanel);
		expPanel = new ExpCalibrationCircle(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				//buffer.serialize();
				step4(buffer);
			}},reader);

		
		getContentPane().add(expPanel);
		repaint();
		setVisible(true);


	}	
	
	
	/**
	 * Zadanie wizualne - wyszukanie liczby i klikniêcie / lub PIN 
	 * @param buffer
	 */
	private void step4(IFrameBuffer buffer) {
		buffer.serializeP("circle");
		System.out.println("TODO: zadanie wizualne");
		step1();
		
		
	}



}
