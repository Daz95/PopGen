/**
 * 
 */

/**
 * @author Dharius
 *
 */
public class Outro extends Section {

	/**
	 * 
	 */
	public Outro(MidiNote  key, int tempoBPM) {
		super(key, tempoBPM);
		Outro.numberOfBars = 2;
		notes = generateSectionNotes();
	}
	
	private boolean isValidOutro()
	{
		if (this.notes.length != 0)
			return true;
		else
			return false;
	}

}
