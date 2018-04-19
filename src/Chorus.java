/**
 * 
 */

/**
 * @author Dharius
 *
 */
public class Chorus extends Section {

	/**
	 * 
	 */
	public Chorus(MidiNote  key, int tempoBPM) {
		super(key, tempoBPM);
		Chorus.numberOfBars = 8;
		Chorus.numOfRepetitions = 4;
		Chorus.noteLengthChances = new int[] {
//				1,
				2,
				16,
				64,
				16
				};
		notes = generateRepeatedBars(numOfRepetitions, generateSectionNotes());;
//		notes = generateSectionNotes();
	}
	
	private boolean isValidChorus()
	{
		if (this.notes.length != 0)
			return true;
		else
			return false;
	}

}
