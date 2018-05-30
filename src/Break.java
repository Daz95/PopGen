/**
 * 
 */

/**
 * @author Dharius
 * Break section
 */
public class Break extends Section {

	public Break(MidiNote  key, int tempoBPM) 
	{
		super(key, tempoBPM);
		Break.numberOfBars = 4;
		Break.numOfRepetitions = 4;
		Break.noteLengthChances = new int[] {
//				1,
				2,
				8,
				32,
				32
				};
		notes = generateRepeatedBars(numOfRepetitions, generateSectionNotes());;
//		notes = generateSectionNotes();	
	}
}
