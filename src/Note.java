/**
 * 
 */

/**
 * @author Dharius
 *
 */

public class Note {
	public final int noteLength; //length of note in Milliseconds
	public final int restLength; //length of rest space (silence) left after the note has finished playing
	public final MidiNote midiNote;
	
	public Note(MidiNote midiNote, int noteLength, int restLength)
	{
		this.midiNote = midiNote;
		this.noteLength = noteLength;
		this.restLength = restLength;
	}
}