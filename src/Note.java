/**
 * @author Dharius
 * A note to be used in a song
 * noteLength - How long the midi note will be played for (in ms)
 * restLength - How long the gap (silence) between the end of this note and the next note will be (in ms)
 * midiNote - The MidiNote integer to be played using the Midi package
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
	
	public void setMidiNote(MidiNote newMidiNote)
	{
		this.midiNote = newMidiNote;
	}
}