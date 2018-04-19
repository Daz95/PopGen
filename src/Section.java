import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.lang.Math;

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
	
	int[] possibleNoteLengths;
	int[] possibleRestLengths;
	MidiNote[] notesAllowed;
	
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
		
		
		int timeInBar = getTimeInFullBar(getMiliSecondsPerBeat());
		this.possibleNoteLengths = new int[] {
//				timeInBar/64, //16th
				timeInBar/32, //8th
				timeInBar/16, //quarter
				timeInBar/8,  //half
				timeInBar/4   //full
				}; // from 1/16th notes to full note per beat
		this.possibleRestLengths = new int[] {
				0,
//				timeInBar/64, 
				timeInBar/32,
				timeInBar/16,
				timeInBar/8, 
				timeInBar/4
				}; // from no rest to full beat rest
		
		this.notesAllowed = getNotesAllowed(key);
		
		notes = generateSectionNotes();	
	}
	
	protected void evolve(int rating)
	{
		Note[] currentNotes = this.notes;
		Note[] newNotes = currentNotes;
		float newFitness;
		float currentFitness = 0;
		float fitnessThreshold = (float)0.9;
		float mutationStrength = (float)((10.0 - (float)rating) / 10.0); //The higher the rating, the lower the mutationStrength
		
		//newNotes = mutateSectionByNote(currentNotes, mutationStrength);
		newFitness = 0;
//		int mutationCounter = 0;
		while(newFitness < fitnessThreshold)
		{
			newNotes = mutateSectionByNote(currentNotes, mutationStrength);
			newFitness = calculateComparitiveFitness(this.notes, newNotes, rating);
//			if(mutationCounter <= 1000)
//				newNotes = mutateSectionByNote(currentNotes, mutationStrength * 1.2)
			if(newFitness > currentFitness)
			{
				currentFitness = newFitness;
				currentNotes = newNotes;
			}
			
		}
		
		this.notes = currentNotes;
	}
	
	protected float calculateComparitiveFitness(Note[] previousNotes, Note[] newNotes, int previousRating)
	{
		float fitness;
		float midiNoteNumDiff;
		float noteLengthDiff;
		float restLengthDiff;
		
		AverageNote previousAvgNote = getAvgNote(previousNotes);
		AverageNote newAvgNote = getAvgNote(newNotes);
		
		midiNoteNumDiff = Math.abs(previousAvgNote.midiNoteNum - newAvgNote.midiNoteNum);
		noteLengthDiff = Math.abs(previousAvgNote.noteLength - newAvgNote.noteLength);
		restLengthDiff = Math.abs(previousAvgNote.restLength - newAvgNote.restLength);
		
		fitness = (float)(calculateFitness(previousRating, midiNoteNumDiff, noteLengthDiff, restLengthDiff));
		return fitness;
		
	}
	
	private float calculateFitness(int rating, float noteNumDiff, float noteLengthDiff, float restLengthDiff)
	{
		if(0 > rating || rating > 10)
			return 0; //invalid rating
		
		float totalVariance = (float)(noteNumDiff + noteLengthDiff + restLengthDiff);
		float maxFitness = (float)(totalVariance * 10); //as max rating a song can get is 10/10
		float ratedFitness = (float)(totalVariance * rating);
		
		return (float)(ratedFitness/maxFitness); //returns decimal fitness value (number between 0 and 1)
	}
	
	private AverageNote getAvgNote(Note[] notes)
	{
		float avgNoteLength;
		float avgRestLength;
		float avgMidiNoteNum;
		int sumNoteLength = 0;
		int sumRestLength = 0;
		int sumMidiNoteNums = 0;
		int numOfNotes = notes.length;
		
		for (Note note : notes) 
		{
			sumMidiNoteNums += note.midiNote.midiNoteNumber;
			sumNoteLength += note.noteLength;
			sumRestLength += note.restLength;
		}
		
		avgMidiNoteNum = (float)(sumMidiNoteNums/numOfNotes);
		avgNoteLength = (float)(sumNoteLength/numOfNotes);
		avgRestLength = (float)(sumRestLength/numOfNotes);
		
		return new AverageNote(avgMidiNoteNum, avgNoteLength, avgRestLength);
	}
	
	/*
	 * Mutates a section of notes
	 * Strength variable defines how many notes will be mutated
	 */
	private Note[] mutateSectionByNote(Note[] notes, float strength)
	{
		int randNotePos;
		int timeAvailableBetweenNotes;
		int numNotesToMutate;
		ArrayList<Integer> mutatedNotesPos;
		
		// Strength should be a decimal below 1 or 1
		// A strength of 1 will mutate all notes
		if(strength > 1) 
			strength = 1;
		
		numNotesToMutate = (int)(notes.length * strength);
		mutatedNotesPos = new ArrayList<>();
//		int[] mutatedNotesPos = new int[numNotesToMutate];
		
		
		// Mutate/Replace a number of notes based on strength
		for(int i = 0; i < numNotesToMutate; i++)
		{
			// Keep looking for a random note that hasn't already been mutate
			while (isNoteMutated(mutatedNotesPos, randNotePos = getRandomInt(0, notes.length-1)));
			
				if(randNotePos == 0) 
				{
					// If it is the first note, we have no previous note to compare to -> create random  new note
					notes[randNotePos] = getCompletelyRandomNote();
					mutatedNotesPos.add(randNotePos);
				}
				else
				{
					// Otherwise -> create a new note normally, based on previous note
					timeAvailableBetweenNotes = notes[randNotePos].noteLength + notes[randNotePos].restLength;
					notes[randNotePos] = mutateNote(notes[randNotePos-1], timeAvailableBetweenNotes);	
					mutatedNotesPos.add(randNotePos);
				}
		}
		
		return notes;
	}
	
	private Note mutateNote(Note previousNote, int timeAvailable)
	{
		int timeInBar = getTimeInFullBar(getMiliSecondsPerBeat());
		
		MidiNote newNote = getRandomSmoothedNote(previousNote.midiNote, notesAllowed);
		int newNoteLength = getRandNoteLength(timeInBar, timeAvailable, previousNote.noteLength);
		timeAvailable -= newNoteLength;
		int newRestLength = getRandRestLength(timeInBar, timeAvailable, previousNote.restLength);
		
		return new Note(newNote, newNoteLength, newRestLength);
	}
	
	private Note getCompletelyRandomNote()
	{
		MidiNote midiNote = notesAllowed[getRandomInt(0, notesAllowed.length-1)];
		int noteLength = possibleNoteLengths[getRandomInt(0, possibleNoteLengths.length-1)];
		int restLength = possibleRestLengths[getRandomInt(0, possibleRestLengths.length-1)];
		
		return new Note(midiNote, noteLength, restLength);
	}
	
	/*
	 * Checks that note position chosen has not already been mutated before
	 * Checks if @pos exists in the @mutatedNotesPos array list
	 */
	private boolean isNoteMutated(ArrayList<Integer> mutatedNotesPos, int pos)
	{
		for(int i=0; i < mutatedNotesPos.size(); i++)
		{
			if(mutatedNotesPos.get(i) == pos)
				return true;
		}
		return false;
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
		
		int lengthChosenPos;
		
		int[] smoothedNoteLengthChances = smoothLengthChances(previousNoteLength, possibleNoteLengths, noteLengthChances);
		lengthChosenPos = getRandPosChoice(smoothedNoteLengthChances);
		
		while (lengthChosenPos >= 0)
		{
			if(possibleNoteLengths[lengthChosenPos] > timeAvailable) //if the note is too long to fit in with the remaining time
			{
				if(lengthChosenPos == 0)
					return timeAvailable;
				lengthChosenPos -= 1; //keep lowering note length choice by one position until it can fit in available time
				//timeChosenPos = getRandomInt(0, timeChosenPos-1); //keep lowering note length by one until it can fit in available time
			}
			else
				return possibleNoteLengths[lengthChosenPos]; //Otherwise, it fits, so the note length is valid -> return 
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
	
	private float getMiliSecondsPerBeat()
	{
		return 60000/tempoBPM;
	}
	
	private int getTimeInFullBar(float millisecondsPerBeat)
	{
		 return (int)(4 * millisecondsPerBeat);
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
	private MidiNote getRandomSmoothedNote(MidiNote previousNote, MidiNote[] notesAllowed)
	{
		MidiNote newNote;
		int newNotePos;

		int[] noteChances = new int[notesAllowed.length];
		for (int pos = 0; pos < noteChances.length; pos++)
		{
			if(notesAllowed[pos] == previousNote) 
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
	private int[] smoothLengthChances(int previousLength, int[] possibleLengths, int[] lengthChancesToCopy)
	{
		int[] lengthChances = lengthChancesToCopy.clone();
		
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
