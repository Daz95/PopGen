/**
 * 
 */

/**
 * @author Dharius
 *
 */

public class Note {
	public int noteLength; //length of note in Milliseconds
	public int restLength; //length of rest space (silence) left after the note has finished playing
	public MidiNote midiNote;
	
	public Note(MidiNote midiNote, int noteLength, int restLength)
	{
		this.midiNote = midiNote;
		this.noteLength = noteLength;
		this.restLength = restLength;
	}
}