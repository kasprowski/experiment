package pl.kasprowski.start;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.calibration.CalibratorLevels;
import pl.kasprowski.exceptions.CalibrationException;
import pl.kasprowski.jazz.ComReader;
import pl.kasprowski.jazz.FakeReaderStim;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.ExpVisualizer;
import pl.kasprowski.stimulus.Experiment;
import pl.kasprowski.stimulus.ExperimentCallback;
import pl.kasprowski.stimulus.rfl.ExpCalibrationStim4;
import pl.kasprowski.stimulus.rfl.ExpCalibrationStim9;

/**
 * Najpierw pokazuje 4 punktow¹ kalibracjê, potem sprawdza czy Y OK i 9 punktow¹
 * na koniec ExpVisualizer - pokazuje gaze point
 * Podobne do StartCalibrator 
 * @author pawel
 *
 */

public class StartWithVertCheck extends JFrame{
	private static final long serialVersionUID = 1L;

	Logger log = Logger.getLogger(StartWithVertCheck.class);

	Experiment expPanel;


	public static void main(String[] args) {
		new StartWithVertCheck();
	}

	Reader reader = new FakeReaderStim();
	
	public StartWithVertCheck() {
		JPanel cP = (JPanel)this.getContentPane();
		cP.setLayout(new BorderLayout());

		
		//reader = new ComReader("COM14");

		expPanel = new ExpCalibrationStim4(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				showCalibration(buffer);
			}},reader);

		getContentPane().add(expPanel);

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setSize(1000,780);
		//pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	
	private void showCalibration(IFrameBuffer buffer) {

		buffer.serializeP("eye4");

		CalibratorLevels c = new CalibratorLevels(buffer);
		
		try{
		if(!c.calculate()) {
			JOptionPane.showMessageDialog(this, "Quality failure!");
		}
		}catch(CalibrationException ex) {
			System.err.println(ex.getMessage());
			JOptionPane.showMessageDialog(this, ex.getMessage());
		}
		
		
		
		setVisible(false);
		getContentPane().remove(expPanel);
		
		expPanel = new ExpCalibrationStim9(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				calibrate(buffer);
			}},reader);
		getContentPane().add(expPanel);
		repaint();
		setVisible(true);
	
	}

	
	private void calibrate(IFrameBuffer buffer) {
		//System.out.println("Frames: "+ buffer.getFrames().size());
		//System.out.println("Timestamps: "+ buffer.getTimestamps().size());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {e.printStackTrace();}

		buffer.serializeP("eye");
		
		
//		for(Timestamp t:buffer.getTimestamps()) {
//			Point p = (Point)t.getData();
//			System.out.println(p.x+"\t"+p.y);
//		}
		//CalibratorMinmax c = new CalibratorMinmax(buffer);
		CalibratorLevels c = new CalibratorLevels(buffer);
		c.calculate();
		
		log.debug("X Coeficients: "+c.getCoefX()[0]+","+c.getCoefX()[1]);
		log.debug("Y Coeficients: "+c.getCoefY()[0]+","+c.getCoefY()[1]);
		log.debug("Middle point: "+c.getMiddlePoint()[0]+","+c.getMiddlePoint()[1]);
		
		
		setVisible(false);
		getContentPane().remove(expPanel);
		
		expPanel = new ExpVisualizer(new ExperimentCallback() {
			public void finished(IFrameBuffer buffer) {
				analyze(buffer);
			}},	reader, 
			c.getCoefX(), c.getCoefY(),
			c.getMiddlePoint());
		getContentPane().add(expPanel);
		repaint();
		setVisible(true);


	}	
	private void analyze(IFrameBuffer buffer) {
		//buffer = c.calibrate();
		System.out.println("TODO: analiza");
	}


}
