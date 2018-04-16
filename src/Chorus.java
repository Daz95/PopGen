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
		Chorus.numberOfBars = 4;
		notes = generateSectionNotes();
	}
	
	private boolean isValidChorus()
	{
		if (this.notes.length != 0)
			return true;
		else
			return false;
	}

}
