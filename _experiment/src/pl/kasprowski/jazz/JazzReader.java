package pl.kasprowski.jazz;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import pl.kasprowski.buffer.EyeFrame;
import pl.kasprowski.buffer.IFrameBuffer;
import pl.kasprowski.tools.CrcChecker;

/**
 * Class that builds DataFrames basing on binary data from COM
 * @author pawel
 *
 */
public class JazzReader {

	Logger log = Logger.getLogger(JazzReader.class);
	int warnBatteryLevel = 20;
	int errorBatteryLevel = 10;
	
	boolean searchPktBegin = true;
	static final int ZERO_COUNT = 3;
	static final int FRAME_SIZE = 56;

	int nPktBuf;
	int nDecodingByteOffset;
	//DataFrame packet;

	CrcChecker crcCalc = new CrcChecker();
	/**
	 * binary data from COM - data from current frame
	 */
	byte[] aPktBuf = new byte[FRAME_SIZE];

	/**
	 * list of frames - decode adds frames here and recalculate removes and
	 * sends EyeFrames to buffer
	 */
	public List<DataFrame> inFrames = new ArrayList<DataFrame>();


	IFrameBuffer buffer;
		
	public JazzReader(IFrameBuffer buffer) {
		this.buffer = buffer;
		buffer.getStat().setStart(System.currentTimeMillis());
	}

	/**
	 * Analyses new data from COM and builds frames
	 * Remembers last unfinished frame (aPktBuf) and is able to add new data to it
	 * @param data buffer with new data from COM
	 * @param nBufSize data length
	 */
	public void decode(byte[] data,int nBufSize) {


		int s=0;
		int zeroCounter = ZERO_COUNT;

		aPktBuf[0] = 0;
		aPktBuf[1] = 0;
		aPktBuf[2] = 0;

		//if no remainders to read
		if(searchPktBegin) {
			//search for three zeros in row
			s = 0;
			while ( s < nBufSize && zeroCounter > 0 )	{
				if ( data[s] != 0 ) zeroCounter = ZERO_COUNT;
				else zeroCounter--;
				s++;
			}

			if ( zeroCounter == 0 )	{
				nPktBuf = 0;
				searchPktBegin = false;
				nDecodingByteOffset = s;
			}
		}
		else {
			s=0;
		}

		// start reading from s - it may be a new package or remainder of the previous one
		// (in this case nPktBuf is > 0)
		for ( int i = s ; i < nBufSize ; i++ )
		{
			//we copy bytes to frame buffer as long as the nPktBuf is smaller than
			//the size of the single frame
			if ( nPktBuf < FRAME_SIZE - 3 )	{ //if not just wait for three zeroes
				aPktBuf[nPktBuf+3] = data[i];
			}

			nPktBuf++;

			if ( data[i] != 0 )	{
				zeroCounter = ZERO_COUNT;
			} else { //zero was read
				zeroCounter--;

				//check for three zeros in row condition (new frame start)
				if ( zeroCounter == 0 )	{
					// if m_nPktBuf is smaller than FRAME_SIZE then we've got decoding error
					if ( nPktBuf >= FRAME_SIZE ) {
						// we build a new frame from buffer decoded in aPktBuf
						DataFrame newFrame = decodeFrame(aPktBuf);

						//serializeFrame(aPktBuf, newFrame);
						newFrame.crcOK = checkCRC( aPktBuf , newFrame.crc ) ;
						inFrames.add(newFrame);
						log.trace("Added frame ["+newFrame.counter+"]!");
						if (!newFrame.crcOK ) {
							log.error("CRC ERROR in frame ["+newFrame.counter+"]!");
							buffer.getStat().incCRCErrors();
						}
					}

					//clear frame data pointer
					nPktBuf = 0;

					//clear zeros counter
					zeroCounter = ZERO_COUNT;
				}
			}


		}
		reconstruct();
	}


	boolean checkCRC(byte[] buffer, int myCrc) {
		int bufferCRC = crcCalc.calculateCRC(buffer, 53);
		return (bufferCRC==myCrc);
	}

	/**
	 * Converts binary data from buffer to DataFrame
	 * 
	 * @param dataBuffer binary data
	 * @return data from buffer packed into DataFrame
	 */
	DataFrame decodeFrame(byte[] dataBuffer) {
		DataFrame f = new DataFrame();
		f.timestamp = System.currentTimeMillis();
		buffer.getStat().incFrames();
		int[] szBuf = new int[dataBuffer.length];
		for(int j=0;j<szBuf.length;j++) { 
			szBuf[j] = dataBuffer[j] & 0xFF;
		}

		final int PACKET_SIZE = 28;


		// control_3 must be decoded at the begining as it's value determine the way of decoding
		f.control_3 = szBuf[55];

		for ( int i = 0 ; i < 2 ; i++ )
		{
			int offset = i * PACKET_SIZE;
			//eye movement
			f.eye_y[i] = szBuf[offset+3];
			f.eye_y[i] <<= 4;
			f.eye_y[i] += szBuf[offset+4] >> 4;
			f.eye_x[i] = szBuf[offset+4] & 0x0F;
			f.eye_x[i] <<= 8;
			f.eye_x[i] += szBuf[offset+5];

			//microphone signal
			int mic_offset = offset + 12;
			int mic_sample = i * 8;

			for ( int mic_idx = 0 ; mic_idx < 4 ; mic_idx++ )
			{		
				f.mic[mic_sample] = szBuf[mic_offset+0];
				f.mic[mic_sample] <<= 4;
				f.mic[mic_sample] += szBuf[mic_offset+1] >> 4;

				mic_sample++;

				f.mic[mic_sample] = szBuf[mic_offset+1] & 0x0F;
				f.mic[mic_sample] <<= 8;
				f.mic[mic_sample] += szBuf[mic_offset+2];

				mic_sample++;
				mic_offset += 3;
			}
		}

		//pulsoxymeter
		f.pul_i = szBuf[28];
		f.pul_i <<= 4;
		f.pul_i += szBuf[29] >> 4;
		f.pul_r = szBuf[29] & 0x0F;
		f.pul_r <<= 8;
		f.pul_r += szBuf[30];

		//counter
		f.counter = szBuf[26];
		f.counter <<= 8;
		f.counter += szBuf[27];

		//control bytes
		if ( f.control_3 == 0xFF ) // old protocol
		{
			// duplicate control_1 from old place but leave in upper nibble
			// so it is consistent with new protocols (1 and higher)
			f.control_1[0] = ( szBuf[24] & 0xF0 );
			f.control_1[1] = ( szBuf[24] & 0xF0 );
			f.control_2 = szBuf[52];
		}
		else // new protocol with window move flag, 1 and higher
		{
			f.control_1[0] = szBuf[24];
			f.control_1[1] = szBuf[52];
			f.control_2 = szBuf[25];
		}
		// control_3 decoded at the begining

		//CRC
		f.crc = szBuf[53];
		f.crc <<= 8;
		f.crc += szBuf[54];
		//packet = f;

		return f;
	}

	//
	//	void serializeFrameBinary(byte[] buffer, DataFrame f) {
	//		try {
	//			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("file.out"),true));
	//			for(byte bajt:buffer)
	//				bw.write(bajt);
	//			bw.write("\n");
	//			bw.write(f.crc);
	//			bw.write("\n");
	//			bw.close();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//
	//	}
	//




	//used by reconstruct to preserve state
	DataFrame lastFrame = null;
	int offsetEyeX = 0 ;
	int offsetEyeY = 0;
	int offsetPulI = 0;
	int offsetPulR = 0;

	/**
	 * Takes inFrames and for every frame builds two EyeFrames which are sent to buffer
	 * and then removes the processed frame from inFrames.
	 * Recalculates offsets if necessary.
	 * At the end inFrames should be empty - but it may still have some frames (max 2) 
	 * if it is necessary to recalculate offset and we need to wait for next frames 
	 */
	void reconstruct() 	{
		final int RECONSTRUCT_MIN = 256;
		final int RECONSTRUCT_MAX = 3839;

		int newOffsetEyeX = offsetEyeX;
		int newOffsetEyeY = offsetEyeY;
		int newOffsetPulI = offsetPulI;
		int newOffsetPulR = offsetPulR;

		log.trace("Reconstructing. inFrames size= "+inFrames.size());
		
		//the first read frame is not analyzed
		if(lastFrame==null && inFrames.size()>0) {
			lastFrame = inFrames.get(0);
			inFrames.remove(0);
		}
		int nPrev,nVal,nNext1,nNext2;
		
		// for every frame in buffer
		while(inFrames.size()>0) {
			DataFrame currentFrame = inFrames.get(0);
			
			// if frame is not OK - use the previous one as current
			if(!currentFrame.crcOK) {
				log.error("Wrong frame ["+currentFrame.counter+"] replaced with the previous one");
				currentFrame = lastFrame;
				//TODO: what about reconstruction?
			}
			if(currentFrame.control_2<warnBatteryLevel) {
				System.err.println("Battery level: "+currentFrame.control_2+"!!!");
				if(currentFrame.control_2<errorBatteryLevel) {
					JOptionPane.showMessageDialog(null, "Battery level: "+currentFrame.control_2+"!!!");
					throw new RuntimeException("Battery level: "+currentFrame.control_2+"!!!");
				}
			}
			EyeFrame eyeFrame0 = new EyeFrame();
			EyeFrame eyeFrame1 = new EyeFrame();
			eyeFrame0.counter = currentFrame.counter;
			eyeFrame1.counter = currentFrame.counter;
			eyeFrame0.timestamp = currentFrame.timestamp;
			if(inFrames.size()>1) // if next frame available
				eyeFrame1.timestamp = currentFrame.timestamp +  (inFrames.get(1).timestamp-currentFrame.timestamp)/2; 
			else
				eyeFrame1.timestamp = currentFrame.timestamp;
			//---------------------------------------------------------
			// eye movement horizontal

			//element X[0]
			nVal = currentFrame.eye_x[0];
			eyeFrame0.eyeX = currentFrame.eye_x[0] + newOffsetEyeX;
			if ( ( ( currentFrame.control_3 == 0xFF ) && ( nVal < RECONSTRUCT_MIN || nVal > RECONSTRUCT_MAX ) ) 
					|| ( ( currentFrame.control_3 != 0xFF ) && ( (currentFrame.control_1[0] & 0x40 ))!=0 ) ) {
				//check if the next frame is available
				if(inFrames.size()<=1)	
					return;

				nPrev = lastFrame.eye_x[1];
				nNext1 = currentFrame.eye_x[1];
				nNext2 = inFrames.get(1).eye_x[0];
				newOffsetEyeX -= offsetDiff( nPrev , nVal , nNext1 , nNext2 );
				log.trace("New offset for X0 = "+newOffsetEyeX);
				buffer.getStat().incOffsetChanges();
			}

			//element x[1]
			nVal = currentFrame.eye_x[1];
			eyeFrame1.eyeX = currentFrame.eye_x[1] + newOffsetEyeX; 
			if ( ( ( currentFrame.control_3 == 0xFF ) && ( nVal < RECONSTRUCT_MIN || nVal > RECONSTRUCT_MAX ) ) 
					|| ( ( currentFrame.control_3 != 0xFF ) && ( (currentFrame.control_1[1] & 0x40 ))!=0 ) ) {

				//check if the next frame is available
				if(inFrames.size()<=1)
					return;

				nPrev = currentFrame.eye_x[0];
				nNext1 = inFrames.get(1).eye_x[0];
				nNext2 = inFrames.get(1).eye_x[1];
				//stCurr.eye_x[1] += newOffsetEyeX;
				newOffsetEyeX -= offsetDiff( nPrev , nVal , nNext1 , nNext2 );
				log.trace("New offset for X1 = "+newOffsetEyeX);
				buffer.getStat().incOffsetChanges();
			}


			//---------------------------------------------------------
			// eye movement vertical				
			nVal = currentFrame.eye_y[0];
			eyeFrame0.eyeY = currentFrame.eye_y[0] + newOffsetEyeY;
			if ( ( ( currentFrame.control_3 == 0xFF ) && ( nVal < RECONSTRUCT_MIN || nVal > RECONSTRUCT_MAX ) ) || 
					( ( currentFrame.control_3 != 0xFF ) && ( (currentFrame.control_1[0] & 0x80)!=0 ) ) )	{
				if(inFrames.size()<=1)	
					return;
				nPrev = lastFrame.eye_y[1];
				nNext1 = currentFrame.eye_y[1];
				nNext2 = inFrames.get(1).eye_y[0];
				newOffsetEyeY -= offsetDiff( nPrev , nVal , nNext1 , nNext2 );
				log.trace("New offset for Y0 = "+newOffsetEyeY);
				buffer.getStat().incOffsetChanges();
			}


			//y[1]
			nVal = currentFrame.eye_y[1];
			eyeFrame1.eyeY = currentFrame.eye_y[1] + newOffsetEyeY;
			if ( ( ( currentFrame.control_3 == 0xFF ) && ( nVal < RECONSTRUCT_MIN || nVal > RECONSTRUCT_MAX ) ) || 
					( ( currentFrame.control_3 != 0xFF ) && ( (currentFrame.control_1[1] & 0x80)!=0 ) ) )	{
				if(inFrames.size()<=1)	
					return;
				nPrev = currentFrame.eye_y[0];
				nNext1 = inFrames.get(1).eye_y[0];
				nNext2 = inFrames.get(1).eye_y[1];
				newOffsetEyeY -= offsetDiff( nPrev , nVal , nNext1 , nNext2 );
				log.trace("New offset for Y1 = "+newOffsetEyeY);
				buffer.getStat().incOffsetChanges();
			}

			//---------------------------------------------------------
			// pulsoxymeter I
			nVal = currentFrame.pul_i;
			eyeFrame0.pulI = currentFrame.pul_i + newOffsetPulI;
			eyeFrame1.pulI = currentFrame.pul_i + newOffsetPulI;
			
			if ( ( ( currentFrame.control_3 == 0xFF ) && ( nVal < RECONSTRUCT_MIN || nVal > RECONSTRUCT_MAX ) ) || 
					( ( currentFrame.control_3 != 0xFF ) && ( (currentFrame.control_1[1] & 0x08)!=0 ) ) )
			{
				if(inFrames.size()<=2)	
					return;
				nPrev = lastFrame.pul_i;
				nNext1 = inFrames.get(1).pul_i;
				nNext2 = inFrames.get(2).pul_i;
				newOffsetPulI -= offsetDiff( nPrev , nVal , nNext1 , nNext2 );
				log.trace("New offset for PI = "+newOffsetPulI);
				buffer.getStat().incOffsetChanges();
			}

			//-----------------------------------------------------
			//pulsoxymeter R
			nVal = currentFrame.pul_r;
			eyeFrame0.pulR = currentFrame.pul_r + newOffsetPulR;
			eyeFrame1.pulR = currentFrame.pul_r + newOffsetPulR;

			if ( ( ( currentFrame.control_3 == 0xFF ) && ( nVal < RECONSTRUCT_MIN || nVal > RECONSTRUCT_MAX ) ) || 
				( ( currentFrame.control_3 != 0xFF ) && ( (currentFrame.control_1[1] & 0x04)!=0 ) ) )	{

				if(inFrames.size()<=2)	
					return;
				nPrev = lastFrame.pul_r;
				nNext1 = inFrames.get(1).pul_r;
				nNext2 = inFrames.get(2).pul_r;
				newOffsetPulR -= offsetDiff( nPrev , nVal , nNext1 , nNext2 );
				log.trace("New offset for PR = "+newOffsetPulR);
				buffer.getStat().incOffsetChanges();
			}

			//store current frame as the previous one
			lastFrame = currentFrame;

			buffer.addFrame(eyeFrame0);
			buffer.getStat().incEyeFrames();
			
			buffer.addFrame(eyeFrame1);
			buffer.getStat().incEyeFrames();
			
			
			
			offsetEyeX = newOffsetEyeX;
			offsetEyeY = newOffsetEyeY;
			offsetPulI = newOffsetPulI;
			offsetPulR = newOffsetPulR;
			
			//remove already processed frame
			inFrames.remove(0);
			log.trace("Added two EyeFrames: "+
			"["+eyeFrame0.eyeX+","+eyeFrame0.eyeY+"]"+
			"["+eyeFrame1.eyeX+","+eyeFrame1.eyeY+"]");
		}


	}

	/**
	 * Calculates a new offset using extrapolation with two previous and two subsequent values
	 * 
	 * @param nPrev
	 * @param nVal
	 * @param nNext1
	 * @param nNext2
	 * @return
	 */
	int offsetDiff(int nPrev,int nVal,int nNext1,int nNext2)
	{
		int p1 = nVal + ( nVal - nPrev );
		int p2 = nNext1 - ( nNext2 - nNext1 );
		int mov1 = nNext1 - p1;
		int mov2 = p2 - nVal;
		return ( mov1 + mov2 ) / 2;
	}

}



