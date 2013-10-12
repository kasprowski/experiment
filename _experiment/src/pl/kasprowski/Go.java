package pl.kasprowski;

import pl.kasprowski.jazz.ComReader;
import pl.kasprowski.jazz.FakeReaderStim;
import pl.kasprowski.jazz.Reader;
import pl.kasprowski.start.StartLive;

public class Go {
public static void main(String[] args) {
	
	Reader reader = new FakeReaderStim();
	reader = new ComReader("COM6");
	// 9punktowa kalibracja ExpCalibrationStim9, calibrator a potem ExpVisualizer z obliczonymi wspó³czynnikami
	//new StartCalibrator(reader);
	// dfsdfs
	// 4punktowa kalibracja, calibrator, sprawdzenie czy ok, 9punktowa kalibracja, kó³ko (ExpCircle) i koniec
	//new StartExperiment1(reader);
	
	// 4punktowa kalibracja ExpCalibrationStim4, eksperyment obrazkowy ExpPhoto
	//new StartFaces(reader);
	
	// startuje ExpLive2 - kalibracja 2 punktowa na bie¿¹co
	new StartLive(reader);
}



}
