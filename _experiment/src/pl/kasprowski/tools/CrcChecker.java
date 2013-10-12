package pl.kasprowski.tools;

public class CrcChecker {

	private final int WIDTH16 = 16;
	private final int POLY16 = 0x1021;
	private final int TOPBIT16 = 0x8000;
	private final int[] aTable = new int[256];

	public CrcChecker() {
		// Compute the remainder of each possible dividend.
		for (int dividend = 0; dividend < 256; ++dividend) {
			// Start with the dividend followed by zeros.
			int remainder = dividend << (WIDTH16 - 8);
			// Perform modulo-2 division, a bit at a time.
			for (int bit = 8; bit > 0; --bit) {
				// Try to divide the current data bit.

				if ((remainder & TOPBIT16) != 0) {
					remainder = (remainder << 1) ^ POLY16;
				} else {
					remainder = (remainder << 1);
				}
			}

			// Store the result into the table.
			aTable[dividend] = remainder & 0xFFFF;
		}
	}

	public int calculateCRC(byte[] message, int size) {
		int data;
		int remainder = 0xFFFF;

		// Divide the message by the polynomial, a byte at a time.
		for (int xbyte = 0; xbyte < size; xbyte++) {
			int x = (remainder >> (WIDTH16 - 8));
			data = message[xbyte] ^ x;
			remainder = aTable[data & 0xFF] ^ (remainder << 8);
			remainder = remainder & 0xFFFF;
		}

		// The final remainder is the CRC.
		return remainder;
	}

}
