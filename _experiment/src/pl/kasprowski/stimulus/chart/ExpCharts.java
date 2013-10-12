package pl.kasprowski.stimulus.chart;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pl.kasprowski.jazz.Reader;
import pl.kasprowski.stimulus.Experiment;
import pl.kasprowski.stimulus.ExperimentCallback;

public class ExpCharts extends Experiment{

	JButton start = new JButton("Start");
	JTextField login = new JTextField("test");
	JLabel status = new JLabel();
	JSpinner divider = new JSpinner();
	LiveChart xChart;
	LiveChart yChart;
	boolean isReading = false;


	public ExpCharts(ExperimentCallback callback, Reader reader) {
		super(callback, reader);
		System.out.println(buffer.hashCode());
		xChart = new LiveChart(buffer,"X");
		yChart = new LiveChart(buffer,"Y");
		buildGui();

		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isReading) stop();
				else start();
				isReading = !isReading;
			}
		});

		SpinnerModel model =
		        new SpinnerNumberModel(1, //initial value
		                               0, //min
		                               100, //max
		                               5);                //step
		divider.setModel(model);
		divider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				xChart.divider = (Integer)divider.getModel().getValue();
				yChart.divider = (Integer)divider.getModel().getValue();
			}
		});
		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				if(isReading) stop();
				else start();
				isReading = !isReading;
			}
		});


	}

	@Override
	public void run() {
		try {
			while(true) {
				Thread.sleep(100);
				repaint();
			}
		} catch (InterruptedException e) {}
	}


//	@Override
//	public void paint(Graphics g) {
//		super.paint(g);
//		int size = buffer.getFrames().size();
//		int frames = (size>=10)?10:size;
//		for(int i=0;i<frames;i++) {
//			//	System.out.println("Reading "+(i+start));
//			EyeFrame f = buffer.getFrames().get(i+size-frames);
//			g.drawString("["+f.eyeX+","+f.eyeY+"]"+buffer.hashCode(), 5, (i*20)+50);
//		}
//
//		//xChart.repaint();
//	}
	//		Dimension winSize = getSize();
	//		Dimension xPanelSize = new Dimension();
	//		
	//		
	//		//int start = size-10;
	//		//punkty poprzednie
	//
	//		
	//		
	//		//	System.out.println("Range: "+start+","+(start+10));
	////		Dimension d = getSize();
	////		g.setColor(new Color(255,255,255));
	////		g.fillRect(0,0,d.width,d.height);
	////		g.setColor(new Color(0,0,0));
	////		g.drawRect(10,10,d.width-20,d.height-20);
	//	}	


	private void buildGui() {
		setLayout(new BorderLayout());
		JToolBar jtb = new JToolBar();
		jtb.add(start);
		jtb.add(login);
		jtb.add(divider);
		this.add(jtb,BorderLayout.NORTH);
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(2,1));
		centerPanel.add(xChart);
		centerPanel.add(yChart);
		this.add(centerPanel,BorderLayout.CENTER);
		this.add(status,BorderLayout.SOUTH);
	}

	private void start() {
		start.setText("Stop");
		//buffer = new FrameBuffer();
		reader.start(buffer);
		stimThread = new Thread(this);
		stimThread.start();
	}


	private void stop() {
		reader.stop();
		System.out.println("reader stopped");
		stimThread.interrupt();
		System.out.println("thread interrupted");
		start.setText("Start");
		//buffer.save(login.getText()+".out");
		//System.out.println("buffer saved");
		callback.finished(buffer);
	}


}
