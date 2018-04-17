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
	static int[] noteLengthChances;
	
	public Section(MidiNote key, int tempoBPM) 
	{
		Section.key = key;
		Section.tempoBPM = tempoBPM;
		Section.noteLengthChances = new int[] {
//				1,
				2,
				8,
				32,
				16
				};
		
		notes = generateSectionNotes();	
	}
	
	protected Note[] generateRepeatedBars(int numberOfRepeatedBars, Note[] barNotes)
	{
		Note[] repeatedBarNotes = new Note[barNotes.length * numberOfRepeatedBars];
		
		int pos = 0;
		for(int i = 0; i < numberOfRepeatedBars; i++)
		{
			for (Note note : barNotes) {
				repeatedBarNotes[pos] = note;
				pos++;
			}
		}
		
		return repeatedBarNotes;
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
	
	private int getRandNoteLength(int timeInBar, int timeAvailable, int previousNoteLength)
	{
		if(timeAvailable == 0)
			return 0;
		
		int timeChosenPos;
		
		int[] possibleNoteLengths = {
//				timeInBar/64, 
				timeInBar/32,
				timeInBar/16,
				timeInBar/8, 
				timeInBar/4
				}; // from 1/16th notes to full note per beat
		
		int[] smoothedNoteLengthChances = smoothLengthChances(previousNoteLength, possibleNoteLengths, noteLengthChances);
		timeChosenPos = getRandPosChoice(smoothedNoteLengthChances);
		
		while (timeChosenPos >= 0)
		{
			if(possibleNoteLengths[timeChosenPos] > timeAvailable) //if the note is too long to fit in with the remaining time
			{
				if(timeChosenPos == 0)
					return timeAvailable;
				timeChosenPos -= 1; //keep lowering note length choice by one position until it can fit in available time
				//timeChosenPos = getRandomInt(0, timeChosenPos-1); //keep lowering note length by one until it can fit in available time
			}
			else
				return possibleNoteLengths[timeChosenPos]; //Otherwise, it fits, so the note length is valid -> return 
		}
		
		//If we get here, there is no valid note length
		return 0;
	}
	
	private int getRandRestLength(int timeInBar, int timeAvailable, int previousRestLength)
	{
		if(timeAvailable == 0)
			return 0;
		
		int timeChosenPos;
		int[] chanceForLengths = {
				32,
//				1,
				2,
				8,
				32,
				16
				};
		
		int[] possibleRestLengths = {
				0,
//				timeInBar/64, 
				timeInBar/32,
				timeInBar/16,
				timeInBar/8, 
				timeInBar/4
				}; // from no rest to full beat rest
		
		chanceForLengths = smoothLengthChances(previousRestLength, possibleRestLengths, chanceForLengths);
		
		timeChosenPos = getRandPosChoice(chanceForLengths);
		while (timeChosenPos >= 0)
		{
			if(possibleRestLengths[timeChosenPos] > timeAvailable) //if the rest is too long to fit in with the remaining time
			{
				if(timeChosenPos == 0)
					return timeAvailable;
				timeChosenPos -= 1; //keep lowering maximum possible rest length by one position until it can fit in available time
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
		int currentNoteLength = 0;
		int currentRestLength = 0; 
		
		
		int minRand = 0;
		int maxRandNoteChoice = notesAllowed.length-1;
		
		currentNote = notesAllowed[getRandomInt(minRand, maxRandNoteChoice)];
		
		while (timeAvailableInBar > 0)
		{
			currentNote = getRandomSmoothedNote(currentNote, notesAllowed);
			currentNoteLength = getRandNoteLength(initialTimeInBar, timeAvailableInBar, currentNoteLength);
			timeAvailableInBar -= currentNoteLength;//reduce time available (for new notes) as note is added to bar
			currentRestLength = getRandRestLength(initialTimeInBar, timeAvailableInBar, currentRestLength);
			timeAvailableInBar -= currentRestLength;//reduce time available (for new notes) as rest is added to bar
			
			barNotes.add(new Note(currentNote, currentNoteLength, currentRestLength));
		}
		
		return barNotes;
	}
	
	/*
	 * Selects a smoothed note:
	 * by increasing the chance of selecting one closer to the previous note
	 * making it more difficult to have jumps in notes
	 */
	private MidiNote getRandomSmoothedNote(MidiNote previouseNote, MidiNote[] notesAllowed)
	{
		MidiNote newNote;
		int newNotePos;

		int[] noteChances = new int[notesAllowed.length];
		for (int pos = 0; pos < noteChances.length; pos++)
		{
			if(notesAllowed[pos] == previouseNote) 
			{
				//So total chance will be 3 for the previous note to be selected again
				noteChances[pos] += 2; 
				
				//So total chance will be 2 for a note one position away from the previous note
				noteChances[getCircularArrayPos(notesAllowed.length-1, pos+1)] += 1;
				noteChances[getCircularArrayPos(notesAllowed.length-1, pos-1)] += 1;
				
				//So total chance will be 2 for a note two positions away from the previous note
				noteChances[getCircularArrayPos(notesAllowed.length-1, pos+2)] += 1; 
				noteChances[getCircularArrayPos(notesAllowed.length-1, pos-2)] += 1;
			}
			
			//Add 1 chance to every note
			noteChances[pos] += 1;
		}
		
		newNotePos = getRandPosChoice(noteChances);
		newNote = notesAllowed[newNotePos];
		return newNote;
	}
	
	/*
	 * Smooths length choice probabilities
	 * To make previous length and other similar lengths more likely
	 */
	private int[] smoothLengthChances(int previousLength, int[] possibleLengths, int[] lengthChances)
	{
		for (int pos = 0; pos < lengthChances.length; pos++)
		{
			if(possibleLengths[pos] == previousLength) 
			{
				//Increase the chance of the same length being chosen by 6 times
				lengthChances[pos] *= 6; 
				
				//Increase the chance of lengths 1 position away by 4 times
				lengthChances[getCircularArrayPos(possibleLengths.length-1, pos+1)] *= 4;
				lengthChances[getCircularArrayPos(possibleLengths.length-1, pos-1)] *= 4;
				
				//Increase the chance of lengths 2 positions away by 2 times
				lengthChances[getCircularArrayPos(possibleLengths.length-1, pos+2)] *= 2; 
				lengthChances[getCircularArrayPos(possibleLengths.length-1, pos-2)] *= 2;
			}
		}
		
		return lengthChances;
	}
	
	
	/*
	 * Applies chances to a cumulative array to return an actual choice position
	 * Based on randomly based on probability from chances input 
	 */
	private int getRandPosChoice(int[] chances)
	{
		int chosenPos = 0;
		int cumulativePos;
		int sumChance = 0;
		
		int[] cumulativeChance = new int[chances.length];
		for(int i = 0; i < cumulativeChance.length; i++)
		{
			sumChance += chances[i];
			cumulativeChance[i] = sumChance; 
		}
		
		cumulativePos = getRandomInt(0, sumChance);
		for(int i = 0; i < cumulativeChance.length; i++)
		{
			if(i == 0)
			{
				if(0 <= cumulativePos && cumulativePos <= cumulativeChance[i])
					chosenPos = i;
			}
			else
			{
				if(cumulativeChance[i-1] < cumulativePos && cumulativePos <= cumulativeChance[i])
					chosenPos = i;
			}
		}
		
		return chosenPos;
	}
	
	private int getCircularArrayPos(int arraySize, int nextPos)
	{
		if(nextPos < 0)
			return (arraySize+1) + nextPos;
		if(nextPos > arraySize)
			return nextPos - (arraySize+1);
		return nextPos; 
	}
	
	/*
	 * Returns all notes in the scale of the Song's Key note input
	 */
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

}
