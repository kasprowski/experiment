package pl.kasprowski.tools;

import java.awt.Graphics;

public class Graph {
	public static void circle(Graphics g,int x, int y, int r) {
		g.drawOval(x-r/2, y-r/2, r, r);
	}

	public static void ellipse(Graphics g,int x, int y, int r1, int r2) {
		g.fillOval(x-r1/2, y-r2/2, r1, r2);
	}

	public static void point(Graphics g,int x, int y, int s) {
		g.fillRect(x-s/2, y-s/2, s, s);
	}

	public static void frame(Graphics g,int x, int y, int s) {
		g.drawRect(x-s/2, y-s/2, s, s);
	}

}
