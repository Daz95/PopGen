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
		Verse.numberOfBars = 8;
		notes = generateSectionNotes();
	}
	
	private boolean isValidVerse()
	{
		if (this.notes.length != 0)
			return true;
		else
			return false;
	}

}
