package pl.kasprowski.tools;

import java.awt.Point;
import java.util.Arrays;

public class Calc {
	/**
	 * Przelicza punkt x do nowego ukadu wspólrzednych
	 * @param x
	 * @param omin
	 * @param omax
	 * @param nmin
	 * @param nmax
	 * @return
	 */
	public static double recalc(double x, double omin, double omax, double nmin, double nmax) {
		double[] coef = new double[2];
		coef[0] = (nmax-nmin)/(omax-omin);
		coef[1] = nmin-omin*coef[0];
		double result = x * coef[0] + coef[1] ; 
		return  result;
	}
	
	public static double[] calcCoef(double omin, double omax, double nmin, double nmax) {
		//System.out.println("Calculating coef: ["+omin+","+omax+"] ["+nmin+","+nmax+"]");
		double[] coef = new double[2];
		coef[0] = (nmax-nmin)/(omax-omin);
		System.out.println("COEF[0]="+coef[0]);
		if(coef[0]>2) coef[0]=2; //TODO Na sztywno ograniczam z góry coef[0]!
		if(coef[0]<-2) coef[0]=-2; //TODO Na sztywno ograniczam z góry coef[0]!
		coef[1] = nmin-omin*coef[0];
		System.out.println("Calculating coef: ["+omin+","+omax+"] ["+nmin+","+nmax+"] > "+coef[0]+","+coef[1]);
		return  coef;
	}
	

	
	public static Point recalcPoint(Point p, double omin, double omax, double nmin, double nmax) {
		Point np = new Point();
		double nx = recalc(p.x, omin, omax, nmin, nmax);
		double ny = recalc(p.y, omin, omax, nmin, nmax);
		np.x = (int)nx;
		np.y = (int)ny;
		return np;
	}
	

	public static Point recalcAB(Point p, double a, double b) {
		double x = recalcAB(p.x, a, b);
		double y = recalcAB(p.y, a, b);
		return new Point((int)x,(int)y);
	}

	public static double recalcAB(double x, double a, double b) {
		return x * a + b;
	}

	
	public static double countMedian(double[] x) {
		int size = x.length;
		double median = 0;
		Arrays.sort(x);
		median = x[size/2];
		return median;
	}
	
	public static double[] recalcAB(double[] y, double a, double b) {
		double[] ny = new double[y.length];
		for(int i=0;i<y.length;i++) {
			ny[i] = y[i]*a + b ;
		}
		return ny;

	}
	/**
	 * Filtr medianowy na podany sygna³
	 * @param x
	 * @return
	 */
	public static double[] buildMedian(double[] x,int len) {
		int size = x.length;
		double[] median = new double[size];
		for(int i=len/2;i<size-len/2;i++) {
			double[] t = new double[len];
			for(int j=-len/2;j<=len/2;j++) {
				t[j+len/2] = x[i+j];
			}
			Arrays.sort(t);
			median[i] = t[len/2];
		}
		//uœrednienie pocz¹tkowych
		for(int i=0;i<len/2;i++) median[i] = median[len/2];
		//uœrednienie koñcowych
		for(int i=size-len/2;i<size;i++) median[i]=median[size-len/2-1];
		return median;
	}

}
