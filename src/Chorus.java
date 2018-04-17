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
		Chorus.noteLengthChances = new int[] {
//				1,
				2,
				16,
				64,
				16
				};
		notes = generateRepeatedBars(4, generateSectionNotes());;
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
