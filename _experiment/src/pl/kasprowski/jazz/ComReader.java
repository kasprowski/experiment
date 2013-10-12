package pl.kasprowski.jazz;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.IFrameBuffer;

/**
 * Fills given FrameBuffer with frames read from given port
 * 
 * @author pawel@kasprowski.pl
 *
 */
public class ComReader implements Reader, Runnable, SerialPortEventListener {
	
	Logger log = Logger.getLogger(ComReader.class);
	
	InputStream inputStream;
	SerialPort serialPort;
	Thread readThread;
	JazzReader jazzReader;;
	private IFrameBuffer buffer;
	String portName;

	//public ComReader() {}
	public ComReader(String port) {
		setPortName(port);
	}
	/**
	 * Opens given COM port and initializes serialPort and inputStream objects
	 */
	@Override
	public void setPortName(String portName) {
		this.portName = portName;
		CommPortIdentifier portId = null;
		

		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = portList.nextElement();
			System.out.println(portId.getName());
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals(portName)) {
					break;
				}
			}
		}
		//portId points the required port
		try {
			serialPort = (SerialPort) portId.open("JazzApp", 2000);
			inputStream = serialPort.getInputStream();
			serialPort.notifyOnDataAvailable(true);
			serialPort.setSerialPortParams(300000,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(portName+" initialization error! " + e.getMessage());
		}

	}
	
	

	
	/**
	 * starts reading from COM and storing frames to buffer
	 */
	@Override
	public void start(IFrameBuffer buffer) {
		if(portName==null)
			throw new RuntimeException("PortName not set in ComReader! Use setPortName() before start.");
		
		this.buffer = buffer;
		this.jazzReader = new JazzReader(buffer);
		
		readThread = new Thread(this);
		readThread.start();
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			e.printStackTrace();
			throw new RuntimeException("Too many listeners!");
		}

	}


	/**
	 * Deactivates reading
	 */
	@Override
	public void stop() {
		
		serialPort.removeEventListener();
		readThread.interrupt();
		buffer.getStat().setEnd(System.currentTimeMillis());
		System.out.println(buffer.getStat());
		log.debug(buffer.getStat());
	}

	/**
	 * nop
	 */
	public void run() {
	}

	private boolean firstRead = true;
	/**
	 * Invoked when something comes from COM
	 * Sends chunk of data read to JazzReader
	 */
	public void serialEvent(SerialPortEvent event) {
		switch(event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[16*1024];
			int numBytes = 0;
			try {
				while (inputStream.available() > 0) {
					numBytes = inputStream.read(readBuffer);
					if(!firstRead)
						jazzReader.decode(readBuffer,numBytes);
					firstRead = false;
				}
			} catch (IOException e) {System.out.println(e);}
			break;
		}
	}
	@Override
	public IFrameBuffer getBuffer() {
		return buffer;
	}



}
