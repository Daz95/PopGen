/**
 * 
 */

/**
 * @author Dharius
 * Outro section
 */
public class Outro extends Section {

	public Outro(MidiNote  key, int tempoBPM) 
	{
		super(key, tempoBPM);
		Outro.numberOfBars = 8;
		Outro.numOfRepetitions = 2;
		Outro.noteLengthChances = new int[] {
//				1,
				2,
				8,
				32,
				16
				};
		notes = generateRepeatedBars(numOfRepetitions, generateSectionNotes());;
//		notes = generateSectionNotes();
	}
}
