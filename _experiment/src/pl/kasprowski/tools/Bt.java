package pl.kasprowski.tools;


public class Bt {
	public static void printBits(byte number) {
		for(int i=7; i>=0 ; i--) {
			System.out.print(isBitSet(number,i)?1:0);
			if(i%4==0) System.out.print(" ");
		}
		System.out.println();
	}

	private static Boolean isBitSet(byte b, int bit)
	{
		return (b & (1 << bit)) != 0;
	}


	public static void printBits(int number) {
		for(int i=31; i>=0 ; i--) {
			System.out.print(isBitSet(number,i)?1:0);
			if(i%4==0) System.out.print(" ");
		}
		System.out.println();
	}

	private static Boolean isBitSet(int b, int bit)
	{
		return (b & (1 << bit)) != 0;
	}


}
