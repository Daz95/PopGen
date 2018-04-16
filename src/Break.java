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
		Break.numberOfBars = 2;
		notes = generateSectionNotes();	
	}
	
	private boolean isValidBreak()
	{
		if (this.notes.length != 0)
			return true;
		else
			return false;
	}

}
