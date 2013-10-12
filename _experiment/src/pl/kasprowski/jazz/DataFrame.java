package pl.kasprowski.jazz;

public class DataFrame {
	long timestamp;
	int[] eye_x = new int[2];
	int[] eye_y = new int[2];
	int pul_i;
	int pul_r;
	int[] mic = new int[16];
	int counter;
	int[] control_1 = new int[2];
	int control_2;
	int control_3;
	int crc;
	boolean crcOK = true;

	public String toString() {
		return 
				counter + "\t" +
				eye_x[0] +"\t" + 
				eye_x[1] +"\t" +
				eye_y[0] +"\t" +
				eye_y[1] +"\t" +
				control_1[0] +"\t" +
				control_1[1] +"\t" +
				control_2 +"\t" +
				control_3 +"\t" +
				(crcOK?"OK":"ERROR");

	}
}
