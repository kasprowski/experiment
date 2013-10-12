package pl.kasprowski.jazz;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JazzReaderTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCheckCRC() {
		fail("Not yet implemented");
	}

	@Test
	public void testDecodeFrame() {
		int x = 5 + 4;
		assertEquals("Nie jest osiem!", 8, x);
	}

	@Test
	public void testReconstruct() {
		fail("Not yet implemented");
	}

}
