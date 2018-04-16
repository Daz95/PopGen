/**
 * 
 */

/**
 * @author Dharius
 *
 */
public class Intro extends Section {

	/**
	 * 
	 */
	public Intro(MidiNote  key, int tempoBPM) 
	{
		super(key, tempoBPM);
		Intro.numberOfBars = 2;
		notes = generateSectionNotes();	
	}
	
	private boolean isValidIntro()
	{
		if (this.notes.length != 0)
			return true;
		else
			return false;
	}

}
