package pl.kasprowski;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pl.kasprowski.jazz.ComReader;
import pl.kasprowski.jazz.FakeReaderStim;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.start.StartCalibratorAndVisualizer;
import pl.kasprowski.start.StartChart;
import pl.kasprowski.start.StartFaces;
import pl.kasprowski.start.StartLive;

public class Go extends JFrame{
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		new Go();
	}
	Reader reader = new FakeReaderStim();

	JLabel info = new JLabel("Fake");
	public Go() {
		this.setLayout(new BorderLayout());
		//this.setLayout(new FlowLayout());
		this.add(inPanel(info),BorderLayout.NORTH);

		JPanel leftPanel = new JPanel();
		add(leftPanel,BorderLayout.WEST);
		//leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setLayout(new FlowLayout());
		
		
		JButton b1 = new JButton("Live");
		leftPanel.add(inPanel(b1));
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new StartLive(reader);
			}
		});

		JButton b2 = new JButton("Chart");
		leftPanel.add(inPanel(b2));
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new StartChart(reader);
			}
		});

		JButton b3 = new JButton("Faces");
		leftPanel.add(inPanel(b3));
		b3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new StartFaces(reader);
			}
		});

		JButton b4 = new JButton("Calibrator");
		leftPanel.add(inPanel(b4));
		b4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new StartCalibratorAndVisualizer(reader);
			}
		});

/////////////////////////
		final JTextField tf = new JTextField();
		this.add(inPanel(tf),BorderLayout.SOUTH);
		tf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
				reader = new ComReader(tf.getText());
				info.setText(tf.getText()+" initialized");
				}catch(RuntimeException ex) {
					info.setText(tf.getText()+" is not correct!");
				}
			}
		});

		//reader = new ComReader("COM6");
		// 9punktowa kalibracja ExpCalibrationStim9, calibrator a potem ExpVisualizer z obliczonymi wspó³czynnikami
		//new StartCalibrator(reader);
		// dfsdfs
		// 4punktowa kalibracja, calibrator, sprawdzenie czy ok, 9punktowa kalibracja, kó³ko (ExpCircle) i koniec
		//new StartExperiment1(reader);

		// 4punktowa kalibracja ExpCalibrationStim4, eksperyment obrazkowy ExpPhoto
		//new StartFaces(reader);

		// startuje ExpLive2 - kalibracja 2 punktowa na bie¿¹co
		//new StartLive(reader);
		//pack();
		setSize(400,300);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	JPanel inPanel(JComponent comp) {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(comp, BorderLayout.NORTH);
		return p;
	}

	void addButton(JPanel leftPanel,Object start) {
		JButton b2 = new JButton("Chart");
		leftPanel.add(inPanel(b2));
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new StartChart(reader);
			}
		});

	}
}
