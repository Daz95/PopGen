/**
 * 
 */

/**
 * @author Dharius
 *
 */
public class Verse extends Section {

	/**
	 * 
	 */
	public Verse(MidiNote  key, int tempoBPM) {
		super(key, tempoBPM);
//		Verse.numberOfBars = 8;
		Verse.numberOfBars = 16;
		Verse.noteLengthChances = new int[] {
//				1,
				2,
				8,
				32,
				16
				};
		notes = generateRepeatedBars(2, generateSectionNotes());;
//		notes = generateSectionNotes();
	}
	
	private boolean isValidVerse()
	{
		if (this.notes.length != 0)
			return true;
		else
			return false;
	}

}
