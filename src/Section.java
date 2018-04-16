import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/*
 * All Sections are restricted to 4x4 time signatures
 * 
 * Section of a song, containing a number of notes to be added to the full song.
 * Parent of sections: Intro, Verse, Chorus and Outro
 */
public class Section {
	Note[] notes;
	static MidiNote key;
	static int tempoBPM;
	static int numberOfBars;
	
	public Section(MidiNote key, int tempoBPM) 
	{
		Section.key = key;
		Section.tempoBPM = tempoBPM;
		
		notes = generateSectionNotes();	
	}
	
	private int getRandomInt(int min, int max)
	{
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}	
	
	protected Note[] generateSectionNotes()
	{
		ArrayList<Note> sectionNotes = new ArrayList<Note>();
		ArrayList<Note> barNotes = generateBarNotes();
		
		for (Note note : barNotes) {
			sectionNotes.add(note);
		}
		
		return sectionNotes.toArray(new Note[sectionNotes.size()]); //Convert ArrayList to Note array (Note[]) and return
	}
	
	private int getRandNoteLength(int timeInBar, int timeAvailable)
	{
		int[] possibleNoteLengths = {
//				timeInBar/64, 
				timeInBar/32,
				timeInBar/16,
				timeInBar/16,
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/4,
				timeInBar/4,
				timeInBar/4,
				timeInBar/4,
				timeInBar/4,
				timeInBar/4
				}; // from 1/16th notes to full note per beat
		
		if(timeAvailable == 0)
			return 0;
		
		int timeChosenPos = getRandomInt(0, possibleNoteLengths.length-1);
		
		while (timeChosenPos >= 0)
		{
			if(possibleNoteLengths[timeChosenPos] > timeAvailable) //if the note is too long to fit in with the remaining time
			{
				if(timeChosenPos == 0)
					return timeAvailable;
				timeChosenPos = getRandomInt(0, timeChosenPos-1); //keep lowering note length by one until it can fit in available time
			}
			else
				return possibleNoteLengths[timeChosenPos]; //Otherwise, it fits, so the note length is valid -> return 
		}
		
		//If we get here, there is no valid note length
		return 0;
	}
	
	private int getRandRestLength(int timeInBar, int timeAvailable)
	{
		int[] possibleRestLengths = {
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, //added multiple 0 rest times to make this more likely
//				timeInBar/64, 
				timeInBar/32,
				timeInBar/16,
				timeInBar/16,
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/8, 
				timeInBar/4,
				timeInBar/4,
				timeInBar/4,
				timeInBar/4,
				timeInBar/4,
				timeInBar/4
				}; // from no rest to full beat rest
		
		if(timeAvailable == 0)
			return 0;
		
		int timeChosenPos = getRandomInt(0, possibleRestLengths.length-1);
		
		while (timeChosenPos >= 0)
		{
			if(possibleRestLengths[timeChosenPos] > timeAvailable) //if the rest is too long to fit in with the remaining time
			{
				if(timeChosenPos == 0)
					return timeAvailable;
				timeChosenPos = getRandomInt(0, timeChosenPos-1); //keep lowering maximum possible rest length by one position until it can fit in available time
			}
			else
				return possibleRestLengths[timeChosenPos]; //Otherwise, it fits, so the rest length is valid -> return 
		}
		
		//If we get here, there is no valid rest length -> return all time remaining
		return timeAvailable;
	}
	
	/*
	 * Returns a list of Notes to fill a 4x4 time signature bar (Including note length, and rest length)
	 * Rest is more likely to be 0ms than anything else
	 * Only certain beat lengths are allowed for each note or rest (e.g. 1/16th, 1/8th, etc.)
	 * Based on temp (BPM) to calculate length of the bar in milliseconds
	 */
	private ArrayList<Note> generateBarNotes()
	{
		float millisecondsPerBeat = 60000 / tempoBPM;
		int initialTimeInBar = (int)(4 * millisecondsPerBeat); //In milliseconds - always assumed to be 4 beats in a bar
		int timeAvailableInBar = initialTimeInBar;
		MidiNote[] notesAllowed = getNotesAllowed(key);
		
		ArrayList<Note> barNotes = new ArrayList<Note>();
		MidiNote currentNote;
		int currentNoteLength;
		int currentRestLength; 
		
		
		int minRand = 0;
		int maxRandNoteChoice = notesAllowed.length-1;
		
		while (timeAvailableInBar > 0)
		{
			currentNote = notesAllowed[getRandomInt(minRand, maxRandNoteChoice)];
			currentNoteLength = getRandNoteLength(initialTimeInBar, timeAvailableInBar);
			timeAvailableInBar -= currentNoteLength;//reduce time available (for new notes) as note is added to bar
			currentRestLength = getRandRestLength(initialTimeInBar, timeAvailableInBar);
			timeAvailableInBar -= currentRestLength;//reduce time available (for new notes) as rest is added to bar
			
			barNotes.add(new Note(currentNote, currentNoteLength, currentRestLength));
		}
		
		return barNotes;
	}
	
	/*
	 * Returns all notes in the scale of the Song's Key note input
	 */
	private MidiNote[] getNotesAllowed(MidiNote key)
	{
		
		switch (key){
			case C:
				return new MidiNote[] {MidiNote.D, MidiNote.E, MidiNote.F, MidiNote.G, MidiNote.A, MidiNote.B, MidiNote.C};
			case G:
				return new MidiNote[] {MidiNote.A, MidiNote.B, MidiNote.C, MidiNote.D, MidiNote.E, MidiNote.Fsharp, MidiNote.G};
			case D:
				return new MidiNote[] {MidiNote.E, MidiNote.Fsharp, MidiNote.G, MidiNote.A, MidiNote.B, MidiNote.Csharp, MidiNote.D};
			case A:
				return new MidiNote[] {MidiNote.B, MidiNote.Csharp, MidiNote.D, MidiNote.E, MidiNote.Fsharp, MidiNote.Gsharp, MidiNote.A};
			case E:
				return new MidiNote[] {MidiNote.Fsharp, MidiNote.Gsharp, MidiNote.A, MidiNote.B, MidiNote.Csharp, MidiNote.Dsharp, MidiNote.E};
			case B:
				return new MidiNote[] {MidiNote.Csharp, MidiNote.Dsharp, MidiNote.E, MidiNote.Fsharp, MidiNote.Gsharp, MidiNote.Asharp, MidiNote.B};
			case F:
				return new MidiNote[] {MidiNote.G, MidiNote.A, MidiNote.Bflat, MidiNote.C, MidiNote.D, MidiNote.E, MidiNote.F};
			case Bflat:
				return new MidiNote[] {MidiNote.C, MidiNote.D, MidiNote.Eflat, MidiNote.F, MidiNote.G, MidiNote.A, MidiNote.Bflat};
			case Eflat:
				return new MidiNote[] {MidiNote.F, MidiNote.G, MidiNote.Aflat, MidiNote.Bflat, MidiNote.C, MidiNote.D, MidiNote.Eflat};
			case Aflat:
				return new MidiNote[] {MidiNote.Bflat, MidiNote.C, MidiNote.Dflat, MidiNote.Eflat, MidiNote.F, MidiNote.G, MidiNote.Aflat};
			case Dflat:
				return new MidiNote[] {MidiNote.Eflat, MidiNote.F, MidiNote.Gflat, MidiNote.Aflat, MidiNote.Bflat, MidiNote.C, MidiNote.Dflat};
			case Gflat:
				return new MidiNote[] {MidiNote.Aflat, MidiNote.Bflat, MidiNote.Cflat, MidiNote.Dflat, MidiNote.Eflat, MidiNote.F, MidiNote.Gflat};
				
			
			//Alternative note name cases
			case Cflat: // same as note B
				return new MidiNote[] {MidiNote.Csharp, MidiNote.Dsharp, MidiNote.E, MidiNote.Fsharp, MidiNote.Gsharp, MidiNote.Asharp, MidiNote.B}; 
			case Asharp: // same as note Bflat
				return new MidiNote[] {MidiNote.C, MidiNote.D, MidiNote.Eflat, MidiNote.F, MidiNote.G, MidiNote.A, MidiNote.Bflat};
			case Dsharp: // same as note Eflat
				return new MidiNote[] {MidiNote.F, MidiNote.G, MidiNote.Aflat, MidiNote.Bflat, MidiNote.C, MidiNote.D, MidiNote.Eflat};
			case Gsharp: // same as note Aflat
				return new MidiNote[] {MidiNote.Bflat, MidiNote.C, MidiNote.Dflat, MidiNote.Eflat, MidiNote.F, MidiNote.G, MidiNote.Aflat};
			case Csharp: // same as note Dflat
				return new MidiNote[] {MidiNote.Eflat, MidiNote.F, MidiNote.Gflat, MidiNote.Aflat, MidiNote.Bflat, MidiNote.C, MidiNote.Dflat};
			case Fsharp: // same as note Gflat
				return new MidiNote[] {MidiNote.Aflat, MidiNote.Bflat, MidiNote.Cflat, MidiNote.Dflat, MidiNote.Eflat, MidiNote.F, MidiNote.Gflat};
				
			default: // set default key to G
				key = MidiNote.G;
				return new MidiNote[] {MidiNote.A, MidiNote.B, MidiNote.C, MidiNote.D, MidiNote.E, MidiNote.Fsharp, MidiNote.G};
		}
	}

}
