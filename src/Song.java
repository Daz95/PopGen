import java.util.ArrayList;
import javax.sound.midi.*;

public class Song {
	
    
    // http://music.stackexchange.com/q/22133/1941
    // http://www.midi.org/techspecs/gm1sound.php
    //Instrument list can be viewed at: https://www.midi.org/specifications/item/gm-level-1-sound-set
    private static final int acousticGuitarSteel = 26;
    private final MidiChannel channel;
    int tempoBPM;
    MidiNote key;
	Structure songStructure;
	Note[] allNotes;
	
	
	/*
	 * Creates a song including multiple sections of notes for one midi instrument (In this case, a steel acoustic guitar)
	 */
	public Song() throws MidiUnavailableException, InterruptedException {
		this.tempoBPM = 120;
		this.key = MidiNote.Bflat;
		this.channel = initMidiChannel(acousticGuitarSteel, 1);
		
		this.songStructure = new Structure(key, tempoBPM);
		this.allNotes = getAllNotes();
		playNotes(this.allNotes);
//		playKeyNotes(key);
	}
	
	/*
	 * Combines all notes from all sections in the song into one Note array
	 * Returns Note array of all section notes
	 */
	private Note[] getAllNotes()
	{
		ArrayList<Note> notesList = new ArrayList<Note>();
		for (Section section : this.songStructure.sections) {
			for (Note note : section.notes) {
				notesList.add(note);
			}
		}
		
		return notesList.toArray(new Note[notesList.size()]);
	}
	
	/*
	 * Evolves current song and plays new evolved output
	 */
	public void evolveAndPlay(int rating) throws InterruptedException
	{
		this.songStructure.evolve(rating);
		this.allNotes = getAllNotes();
		playNotes(this.allNotes);
	}

	
	/*
	 * Initialise the midi channel to be used for playing midi notes
	 */
	private static MidiChannel initMidiChannel(int instrument, int channelNo) throws MidiUnavailableException {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        MidiChannel channel = synth.getChannels()[channelNo];
        // MIDI instruments are traditionally numbered from 1,
        // but the javax.midi API numbers them from 0
        channel.programChange(instrument - 1);
        return channel;
    }
	
	/*
	 * Plays all notes in the "notes" array out loud
	 */
	public void playNotes(Note[] notes) throws InterruptedException
	{
		for (Note note : notes) {
			this.play(note.midiNote);
			
			Thread.sleep(note.noteLength);
			this.silence();
			Thread.sleep(note.restLength);
		}
	}
	
	/*
	public void playKeyNotes(MidiNote key) throws InterruptedException
	{
		MidiNote[] notesInKey = getNotesAllowed(key);
		
		for (MidiNote note : notesInKey) {
			this.play(note);
			Thread.sleep(500);
			this.silence();
		}
	}
	*/
	/*
	 * *REMOVE WHEN COMPLETE*
	 * Used for debugging - checking that key notes are correct
	 */
	/*
	private MidiNote[] getNotesAllowed(MidiNote key)
	{
		switch (key){
			case C:
				return new MidiNote[] {MidiNote.C, MidiNote.D, MidiNote.E, MidiNote.F, MidiNote.G, MidiNote.A, MidiNote.B, MidiNote.Chi};
			case G:
				return new MidiNote[] {MidiNote.G, MidiNote.A, MidiNote.B, MidiNote.C, MidiNote.D, MidiNote.E, MidiNote.Fsharp, MidiNote.Ghi};
			case D:
				return new MidiNote[] {MidiNote.D, MidiNote.E, MidiNote.Fsharp, MidiNote.G, MidiNote.A, MidiNote.B, MidiNote.Csharp, MidiNote.Dhi};
			case A:
				return new MidiNote[] {MidiNote.A, MidiNote.B, MidiNote.Csharp, MidiNote.D, MidiNote.E, MidiNote.Fsharp, MidiNote.Gsharp, MidiNote.Ahi};
			case E:
				return new MidiNote[] {MidiNote.E, MidiNote.Fsharp, MidiNote.Gsharp, MidiNote.A, MidiNote.B, MidiNote.Csharp, MidiNote.Dsharp, MidiNote.Ehi};
			case B:
				return new MidiNote[] {MidiNote.B, MidiNote.Csharp, MidiNote.Dsharp, MidiNote.E, MidiNote.Fsharp, MidiNote.Gsharp, MidiNote.Asharp, MidiNote.Bhi};
			case F:
				return new MidiNote[] {MidiNote.F, MidiNote.G, MidiNote.A, MidiNote.Bflat, MidiNote.C, MidiNote.D, MidiNote.E, MidiNote.Fhi};
			case Bflat:
				return new MidiNote[] {MidiNote.Bflat, MidiNote.C, MidiNote.D, MidiNote.Eflat, MidiNote.F, MidiNote.G, MidiNote.A, MidiNote.BflatHi};
			case Eflat:
				return new MidiNote[] {MidiNote.Eflat, MidiNote.F, MidiNote.G, MidiNote.Aflat, MidiNote.Bflat, MidiNote.C, MidiNote.D, MidiNote.EflatHi};
			case Aflat:
				return new MidiNote[] {MidiNote.Aflat, MidiNote.Bflat, MidiNote.C, MidiNote.Dflat, MidiNote.Eflat, MidiNote.F, MidiNote.G, MidiNote.AflatHi};
			case Dflat:
				return new MidiNote[] {MidiNote.Dflat, MidiNote.Eflat, MidiNote.F, MidiNote.Gflat, MidiNote.Aflat, MidiNote.Bflat, MidiNote.C, MidiNote.DflatHi};
			case Gflat:
				return new MidiNote[] {MidiNote.Gflat, MidiNote.Aflat, MidiNote.Bflat, MidiNote.Cflat, MidiNote.Dflat, MidiNote.Eflat, MidiNote.F, MidiNote.GflatHi};
				
			
			//Alternative note name cases
			case Cflat: // same as note B
				return new MidiNote[] {MidiNote.Cflat, MidiNote.Csharp, MidiNote.Dsharp, MidiNote.E, MidiNote.Fsharp, MidiNote.Gsharp, MidiNote.Asharp, MidiNote.Bhi}; 
			case Asharp: // same as note Bflat
				return new MidiNote[] {MidiNote.Asharp, MidiNote.C, MidiNote.D, MidiNote.Eflat, MidiNote.F, MidiNote.G, MidiNote.A, MidiNote.BflatHi};
			case Dsharp: // same as note Eflat
				return new MidiNote[] {MidiNote.Dsharp, MidiNote.F, MidiNote.G, MidiNote.Aflat, MidiNote.Bflat, MidiNote.C, MidiNote.D, MidiNote.EflatHi};
			case Gsharp: // same as note Aflat
				return new MidiNote[] {MidiNote.Gsharp, MidiNote.Bflat, MidiNote.C, MidiNote.Dflat, MidiNote.Eflat, MidiNote.F, MidiNote.G, MidiNote.AflatHi};
			case Csharp: // same as note Dflat
				return new MidiNote[] {MidiNote.Csharp, MidiNote.Eflat, MidiNote.F, MidiNote.Gflat, MidiNote.Aflat, MidiNote.Bflat, MidiNote.C, MidiNote.DflatHi};
			case Fsharp: // same as note Gflat
				return new MidiNote[] {MidiNote.Fsharp, MidiNote.Aflat, MidiNote.Bflat, MidiNote.Cflat, MidiNote.Dflat, MidiNote.Eflat, MidiNote.F, MidiNote.GflatHi};
				
			default: // set default key to G
				key = MidiNote.G;
				return new MidiNote[] {MidiNote.G, MidiNote.A, MidiNote.B, MidiNote.C, MidiNote.D, MidiNote.E, MidiNote.Fsharp, MidiNote.Ghi};
		}
	}
	*/
	
	/*
	 * Plays a single midi note
	 */
	public void play(MidiNote note) {
        this.channel.noteOn(note.midiNoteNumber, 127); 
    }

	/*
	 * Turns off any currently playing midi note
	 */
    public void silence() {
        this.channel.allNotesOff();
    }		
}