package pl.kasprowski.buffer;

import java.util.List;

import org.apache.log4j.Logger;

public class BufferNormalizer {
	static Logger log = Logger.getLogger(BufferNormalizer.class);
	IFrameBuffer buffer;

	
	/**
	 * Poprawia timestampy w próbkach.
	 * Problem: próbki przychodz¹ z jazza grupami. Ca³a grupa dostaje ten sam timestamp (moment przyjœcia)
	 * Czyli dla ramki mamy nie rzeczywisty moment w którym j¹ zmierzono ale moment w którym przysz³a
	 * Czyli np czasy moga wygl¹dac tak: (6,10,10,10,10,14,14,14,14,18,18,18,18)
	 * Metoda poni¿ej poprawia to w ten sposób: (6,7,8,9,10,11,12,13,14,15,...)
	 * rozci¹gaj¹c grupê na ca³¹ dziurê przed ni¹
	 */
	public static void normalize(IFrameBuffer buffer) {
		List<EyeFrame> frames = buffer.getFrames();

		int end = frames.size();
		int start = 0;
		//start = 5000;
		//end = 7000;
		long oldTimestamp = frames.get(0).timestamp;
		long olderTimestamp = frames.get(0).timestamp;
		int counter = 0;
log.debug(end);
		for(int i=1;i<end;i++) {
			long newTimestamp = frames.get(i).timestamp;
			if(newTimestamp>oldTimestamp+5) {

				//pierwszy blok - przeliczam w ty³ od timestampu bloku - zak³adam interval 1ms
				if(oldTimestamp == olderTimestamp) {
					double interval = 1;
			//	log.debug("FIRST "+olderTimestamp+"\t"+oldTimestamp+"\t"+newTimestamp+"\t"+counter+"\t"+(oldTimestamp-olderTimestamp)+"\t"+interval);
					for(int j=0;j<counter;j++) {
						long change = olderTimestamp - (long)(interval*(counter-j));
			//	log.debug("zmiana: ["+(j)+"]\t"+frames.get(j).timestamp+" > "+change);
						frames.get(start+j).timestamp = change;
					}
				}
				else { // kolejne bloki - rozrzucam proporcjonalnie w przestrzeni pomiêdzy olderTimestamp a oldTimestamp
					double interval = 0;
					if(counter>0 )
						interval = (double)(oldTimestamp-olderTimestamp)/(double)counter;
			//	log.debug(olderTimestamp+"\t"+oldTimestamp+"\t"+newTimestamp+"\t"+counter+"\t"+(oldTimestamp-olderTimestamp)+"\t"+interval);

					for(int j=0;j<counter;j++) {
						long changed = olderTimestamp + (long)(interval*j);
			//	log.debug("zmiana: ["+(start+j)+"]\t"+frames.get(start+j).timestamp+" > "+changed);
						frames.get(start+j).timestamp = changed;
					}
				}
				olderTimestamp = oldTimestamp;
				oldTimestamp = newTimestamp;
				start = i;
				counter=0;
			}
			counter++;
		}
		//ostatni blok
		double interval = 0;
		if(counter>0 )
			interval = (double)(oldTimestamp-olderTimestamp)/(double)counter;
		//log.debug("LAST "+olderTimestamp+"\t"+oldTimestamp+"\t"+"\t"+counter+"\t"+(oldTimestamp-olderTimestamp)+"\t"+interval);
		for(int j=0;j<counter;j++) {
			long changed = olderTimestamp + (long)(interval*j);
		//log.debug("zmiana: ["+(start+j)+"]\t"+frames.get(start+j).timestamp+" > "+changed);
			frames.get(start+j).timestamp = changed;
		}
		
		
		

		log.debug("==========================");
		for(int i=0;i<end;i++) {
			long newTimestamp = frames.get(i).timestamp;
			//if(newTimestamp-oldTimestamp>2)	log.debug("["+i+"] "+newTimestamp+"\t"+(newTimestamp-oldTimestamp));
			oldTimestamp = newTimestamp;
		}

	}

}
