/*
 * An average of all notes in a given song
 * midiNoteNum is the average MidiNote integer used in the song (in float form) - can then be used to find the nearest note or to compare average notes
 * noteLength is the average note length used for all notes in the song
 * restLength is the average rest length used for all notes in the song
 */
public class AverageNote {
	float midiNoteNum;
	float noteLength;
	float restLength;

	public AverageNote(float midiNoteNum, float noteLength, float restLength) {
		this.midiNoteNum = midiNoteNum;
		this.noteLength = noteLength;
		this.restLength = restLength;
	}

}
