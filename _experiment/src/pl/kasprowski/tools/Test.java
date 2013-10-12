package pl.kasprowski.tools;



public class Test {
	public static void main(String[] args) throws Exception{
		//	byte x0 = (byte)84;
		//	byte x1 = (byte)204;
		//	byte x2 = (byte)188;
		test4();
	}

	static class Foo implements Cloneable{
		int[] x = new int[2];
		@Override
		protected Foo clone() throws CloneNotSupportedException {
			return (Foo)super.clone();
		}
	}
	
	public static void test5() throws Exception{
		Foo f1 = new Foo();
		f1.x[0] = 2;
		f1.x[1] = 3;
		Foo f2;

		f2 = f1.clone();
		f2.x[0] = 5;
		
		System.out.println(f1.x[0]);//5!
				
	}
	public static void test4() {
		
		int[] x = new int[2];
		x[0]=2;
		x[1]=3;
		int[] y = x;
		y[0]=5;
		System.out.println(x[0]);//5!

		int[] y2 = x.clone();
		y2[0]=8;
		System.out.println(x[0]);//5!

	}
	
	public static void test3() {
		CrcChecker c = new CrcChecker();
		byte[] x = {2,76,4,67};
		int crc = c.calculateCRC(x, x.length);
		System.out.println("crc:"+crc);
		
//		for(int i=0;i<256;i++) {
//			System.out.println(i+"\t"+c.m_aTable[i]);
//		}
	}
	
	public static void test2() {
		int x0 = 84;
		int x1 = 236;
		Bt.printBits(x0);
		x0 = x0<<8;
		Bt.printBits(x0);
		Bt.printBits(x1);

		int x2 = x0+x1;
		Bt.printBits(x2);
	
		x0 += x1;
		Bt.printBits(x0);
	}
	public static void test1() {
		byte bx0 = (byte)84;
		byte bx1 = (byte)236;
		byte bx2 = (byte)189;
//		int x0 = 84;
//		int x1 = 236;
//		int x2 = 189;
		int x0 = bx0 & 0xFF;
		int x1 = bx1 & 0xFF;
		int x2 = bx2 & 0xFF;

		
		Bt.printBits(bx0);
		Bt.printBits(bx1);
		Bt.printBits(bx2);
		System.out.println("=");
		Bt.printBits(x0);
		Bt.printBits(x1);
		Bt.printBits(x2);
System.out.println("===");
		//Bt.printBits(x0);

		int eye_y = x0;
		Bt.printBits(eye_y);
		eye_y <<= 4;
		Bt.printBits(eye_y);
		eye_y += x1 >> 4;
		Bt.printBits(eye_y);
		System.out.println("x");
		int eye_x = x1 & 0x0F;

		Bt.printBits(eye_x);
		eye_x <<= 8;
		Bt.printBits(eye_x);
		eye_x += x2;
		Bt.printBits(eye_x);

		System.out.println(x0);
		System.out.println(x1);
		System.out.println(x2);
		System.out.println(eye_x);
		System.out.println(eye_y);


	}



	//84
	//204
	//188
	//3260
	//1356

	//84
	//-52
	//-68
	//3004
	//1340

	//2


	//84
	//236
	//189
	//3261
	//1358

	//84
	//-20
	//-67
	//3005
	//1342



}

