/**
 * 
 */

/**
 * @author Dharius
 *
 */
public class Break extends Section {

	/**
	 * 
	 */
	public Break(MidiNote  key, int tempoBPM) 
	{
		super(key, tempoBPM);
		Break.numberOfBars = 4;
		Break.noteLengthChances = new int[] {
//				1,
				2,
				8,
				32,
				32
				};
		notes = generateRepeatedBars(4, generateSectionNotes());;
//		notes = generateSectionNotes();	
	}
	
	private boolean isValidBreak()
	{
		if (this.notes.length != 0)
			return true;
		else
			return false;
	}

}
