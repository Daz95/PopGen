/**
 * @author Dharius
 * Introduction section
 */
public class Intro extends Section {

	public Intro(MidiNote  key, int tempoBPM) 
	{
		super(key, tempoBPM);
		Intro.numberOfBars = 8;
		Intro.numOfRepetitions = 2;
		Intro.noteLengthChances = new int[] {
//				1,
				2,
				8,
				32,
				16
				};
		
		notes = generateRepeatedBars(numOfRepetitions, generateSectionNotes());
	}
}
