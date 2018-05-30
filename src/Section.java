import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
	Note[] barNotes;
	static MidiNote key;
	static int tempoBPM;
	static int numberOfBars;
	static int[] noteLengthChances;
	static int numOfRepetitions;
	
	int[] possibleNoteLengths;
	int[] possibleRestLengths;
	MidiNote[] notesAllowed;
	
	public Section(MidiNote key, int tempoBPM) 
	{
		Section.key = key;
		Section.tempoBPM = tempoBPM;
		Section.numOfRepetitions = 4;
		Section.noteLengthChances = new int[] {
//				1,
				2,
				8,
				32,
				16
				};
		
		
		int timeInBar = getTimeInFullBar(getMilliSecondsPerBeat());
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
	
	/*
	 * Evolves the current using a genetic algorithm
	 */
	protected void GAEvolve(int numOfGenerations, int populationSize, int rating)
	{
		float mutationStrength;
		float[] popCompFitnesses;
		Section candidate;
		Section[] population = new Section[populationSize];
		Section[] bestGenCandidates;
		
		// Generate initial population
		popCompFitnesses = new float[populationSize];
		for(int candidateNo = 0; candidateNo < populationSize; candidateNo++)
		{
			candidate = cloneSection(this);
			
			// 70% of the population size to be populated with variations of the original section
			// Where the mutation strength is set specifically based on the user's rating of the song
			// Leaving 30% of the initial population completely random
			if(candidateNo < (populationSize/10) * 7) 
			{
				//The higher the rating, the lower the mutationStrength
				mutationStrength = (float)(((float)10.0 - (float)rating) / (float)10.0);
				candidate = mutate(candidate, mutationStrength);
				population[candidateNo] = candidate;
			}
			// For the rest of the population, use a random mutation strength
			else
			{
				//Get a random mutation strength between 0 and 1 (to 1dp)
				mutationStrength = (float)((float)getRandomInt(1, 10)/(float)10.0);
				candidate = mutate(candidate, mutationStrength);
				population[candidateNo] = candidate;
			}
			popCompFitnesses[candidateNo] = calculateComparativeFitness(this.barNotes, candidate.barNotes, rating);
		}
		
		//Genetically evolve using a random mutation strength
		for(int generationNo = 0; generationNo < numOfGenerations; generationNo++ )
		{
			bestGenCandidates = getRankedSelections(popCompFitnesses, populationSize, populationSize/100, population);
			
			// Generate another 1% of a full population from evolved versions of that candidate (including exact original candidate)
			int candidateNo = 0;
			for (Section bestCandidate : bestGenCandidates) {
				population[candidateNo] = bestCandidate;
				candidateNo++;
				for(int i = 1; i < (int)(populationSize/100); i++)
				{
					mutationStrength = (float)(getRandomInt(0, 10)/(float)10);
					candidate = cloneSection(bestCandidate);
					candidate = mutate(candidate, mutationStrength);
					population[candidateNo] = candidate;
					candidateNo++;
				}
			}
		}
		
		bestGenCandidates = getRankedSelections(popCompFitnesses, populationSize, populationSize/100, population);
		this.notes = bestGenCandidates[bestGenCandidates.length-1].notes;
	}
	
	/*
	 * Gets the best candidates from a generation's population
	 * Using ranked selection
	 */
	private Section[] getRankedSelections(float[] popCompFitnesses, int populationSize, int selectionSize, Section[] population)
	{
		Section[] selectedCandidates = new Section[selectionSize];
		int[] rankedProbabilities = new int[populationSize];
		
		for(int i = 0; i < populationSize; i++)
		{
			rankedProbabilities[i] = i++;
		}
		
		//Get selected population
		for(int i = 0; i < selectionSize; i++)
		{
			//returns position of randomly chosen candidate after determining ordering the positions of each candidate according to their rank
			selectedCandidates[i] = population[candidateRankPositions(popCompFitnesses)[getRandPosChoice(rankedProbabilities)]]; 
		}
		
		return selectedCandidates;
		
	}
	
	
	/*
	 * Returns population positions in order of the candidates' fitness values
	 * So the first element in the returned array is the position of the worst candidate
	 */
	private int[] candidateRankPositions(float[] popCompFitnesses)
	{
		float[] popFitnesses = popCompFitnesses.clone();
		int[] rankPositions = new int[popFitnesses.length];
		Map<Float, Integer> candidatePosMap = new HashMap<Float, Integer>();
		for (int i = 0; i < popFitnesses.length; i++) {
			candidatePosMap.put(popFitnesses[i], i);
        }
		Arrays.sort(popFitnesses);
		
		for (int i = 0; i < popFitnesses.length; i++)
		{
			rankPositions[i] = candidatePosMap.get(popFitnesses[i]);
		}
		
		return rankPositions;
	}
	
	/*
	 * Creates a new clone of section1
	 */
	public Section cloneSection(Section section1)
	{
		Section section2 = new Section(key, tempoBPM);
		section2.barNotes = cloneNotes(section1.barNotes);
		section2.notes = cloneNotes(section1.notes);
		return section2;
	}
	
	/*
	protected void evolve(int rating, float mutationStrength)
	{
		Note[] currentNotes = this.barNotes;
		Note[] newNotes = currentNotes;
		float newFitness;
		float currentFitness = 0;
		float fitnessThreshold = (float)0.999;
		
		//newNotes = mutateSectionByNote(currentNotes, mutationStrength);
		newFitness = 0;
//		int mutationCounter = 0;
		while(newFitness < fitnessThreshold)
		{
			//Choose mutation method
			//true will call "muteSectionByNote" function for mutation. false will call mutateSectionReplaceLengths
			if(getRandBool()) 
			{
				newNotes = mutateSectionByNote(currentNotes, mutationStrength);
			}
			else
			{
				newNotes = mutateSectionReplaceLengths(currentNotes, mutationStrength);
			}
			
			newFitness = calculateComparativeFitness(this.notes, newNotes, rating);
//			if(mutationCounter <= 1000)
//				newNotes = mutateSectionByNote(currentNotes, mutationStrength * 1.2)
			if(newFitness > currentFitness)
			{
				currentFitness = newFitness;
				currentNotes = newNotes;
			}
			
		}
		
		this.notes = generateRepeatedBars(numOfRepetitions, currentNotes);
	}
	*/
	
	/*
	 * Mutates a number of notes in a bar (Returns full section with repeated bars)
	 * Based on the mutation strength
	 */
	protected Section mutate(Section candidate, float mutationStrength)
	{
		Section mutatedCandidate = candidate;
		
		//Choose mutation method
		//true will call "muteSectionByNote" function for mutation. 
		//false will call mutateSectionReplaceLengths
		if(getRandBool()) 
			mutatedCandidate.barNotes = mutateSectionByNote(mutatedCandidate.notes, mutationStrength);
		else
			mutatedCandidate.barNotes = mutateSectionReplaceLengths(mutatedCandidate.notes, mutationStrength);
		
		mutatedCandidate.notes = generateRepeatedBars(mutatedCandidate.numOfRepetitions, mutatedCandidate.barNotes);
		
		return mutatedCandidate;
	}
	
	/*
	 * Returns a random boolean value
	 */
	private boolean getRandBool()
	{
		return ThreadLocalRandom.current().nextBoolean();
	}
	
	/*
	 * Returns a comparative fitness 
	 * Based on the newNotes' similarity/variance to/from the previousNotes (and the previousNotes' rating)
	 */
	protected float calculateComparativeFitness(Note[] previousNotes, Note[] newNotes, int previousRating)
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
		
		fitness = (calculateFitness(previousRating, midiNoteNumDiff, noteLengthDiff, restLengthDiff));
		return fitness;
	}
	
	/*
	 * Calculates fitness
	 * Based on variance of note values compared to the rating
	 * Where the best variance would be none if the rating was 10 (maximum fitness)
	 */
	private float calculateFitness(int rating, float noteNumDiff, float noteLengthDiff, float restLengthDiff)
	{
		if(0 > rating || rating > 10)
			return 0; //invalid rating
		
		float totalVariance = (float)(noteNumDiff + noteLengthDiff + restLengthDiff);
		float maxFitness = (float)(totalVariance * (float)10.0); //as max rating a song can get is 10/10
		float ratedFitness = (float)(totalVariance * rating);
		
		if(maxFitness == 0)
			return 1; //No difference, therefore best fitness
		else
			return (float)(ratedFitness/maxFitness); //returns decimal fitness value (number between 0 and 1)
	}
	
	/*
	 * Get an average of every aspect of a the section's notes
	 * Includes:
	 * Average of note pitch
	 * Average of note length
	 * Average of rest length
	 */
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
	 * Mutates a section of notes, by replacing all aspects of each note
	 * Including:
	 * Midi note
	 * Note length
	 * Rest length
	 * 
	 * Where "strength" variable defines how many notes will be mutated
	 */
	private Note[] mutateSectionReplaceLengths(Note[] notes, float strength)
	{
		int randNotePos;
		int numNotesToMutate;
		ArrayList<Integer> mutatedNotesPos;
		MidiNote newNote;
		
		Note[] newNotes = cloneNotes(notes);
		
		
		if(strength > 1);
			strength = 1;
		
		numNotesToMutate = (int)(newNotes.length * strength);
		mutatedNotesPos = new ArrayList<>();
		
		if(numNotesToMutate == 0)
			numNotesToMutate = 1;
		
		for(int i = 0; i < numNotesToMutate; i++)
		{
			while (isNoteMutated(mutatedNotesPos, randNotePos = getRandomInt(0, newNotes.length-1)));
				
				if(randNotePos == 0) 
				{
					// If it is the first note, we have no previous note to compare to -> create random  new note
					newNote = getCompletelyRandomNote().midiNote;
					newNotes[randNotePos].setMidiNote(newNote);
					mutatedNotesPos.add(randNotePos);
				}
				else
				{
					// Otherwise -> create a new note normally, based on previous note
					newNote = getRandomSmoothedNote(newNotes[randNotePos-1].midiNote, notesAllowed);
					newNotes[randNotePos].setMidiNote(newNote);
					mutatedNotesPos.add(randNotePos);
				}
		}
		
		return mutateAllLengths(newNotes);
	}
	
	/*
	 * Creates a new copy of array of notes from the "notes" parameter
	 */
	private Note[] cloneNotes(Note[] notes) 
	{
		Note[] newNotes = new Note[notes.length];
		Note cloneNote;
		for (int i = 0; i < notes.length; i++)
		{
			cloneNote = getCompletelyRandomNote();
			cloneNote.setMidiNote(notes[i].midiNote);
			cloneNote.noteLength = notes[i].noteLength;
			cloneNote.restLength = notes[i].restLength;
			newNotes[i] = cloneNote; 
		}
		return newNotes;
	}
	
	/*
	 * Mutates a section of notes
	 * Where "strength" variable defines how many notes will be mutated
	 */
	private Note[] mutateSectionByNote(Note[] notes, float strength)
	{
		Note[] newNotes = cloneNotes(notes);;
		int randNotePos;
		int timeAvailableBetweenNotes;
		int numNotesToMutate;
		ArrayList<Integer> mutatedNotesPos;
		
		// Strength should be a decimal below 1 or 1
		// A strength of 1 will mutate all notes
		if(strength > 1) 
			strength = 1;
		
		numNotesToMutate = (int)(newNotes.length * strength);
		mutatedNotesPos = new ArrayList<>();
//		int[] mutatedNotesPos = new int[numNotesToMutate];
		
		if(numNotesToMutate == 0)
			numNotesToMutate = 1;
		
		// Mutate/Replace a number of notes based on strength
		for(int i = 0; i < numNotesToMutate; i++)
		{
			// Keep looking for a random note that hasn't already been mutate
			while (isNoteMutated(mutatedNotesPos, randNotePos = getRandomInt(0, newNotes.length-1)));
			
				if(randNotePos == 0) 
				{
					// If it is the first note, we have no previous note to compare to -> create random  new note
					newNotes[randNotePos] = getCompletelyRandomNote();
					mutatedNotesPos.add(randNotePos);
				}
				else
				{
					// Otherwise -> create a new note normally, based on previous note
					timeAvailableBetweenNotes = newNotes[randNotePos].noteLength + newNotes[randNotePos].restLength;
					newNotes[randNotePos] = mutateNote(newNotes[randNotePos-1], timeAvailableBetweenNotes);	
					mutatedNotesPos.add(randNotePos);
				}
		}
		
		return newNotes;
	}
	
	/*
	 * Mutate all aspects of the note, whilst considering the previousNote in the section
	 */
	private Note mutateNote(Note previousNote, int timeAvailable)
	{
		int timeInBar = getTimeInFullBar(getMilliSecondsPerBeat());
		
		MidiNote newNote = getRandomSmoothedNote(previousNote.midiNote, notesAllowed);
		int newNoteLength = getRandNoteLength(timeInBar, timeAvailable, previousNote.noteLength);
		timeAvailable -= newNoteLength;
		int newRestLength = getRandRestLength(timeInBar, timeAvailable, previousNote.restLength);
		
		return new Note(newNote, newNoteLength, newRestLength);
	}
	
	
	/*
	 * Mutate the note length and rest length of every note in the section
	 */
	private Note[] mutateAllLengths(Note[] notes)
	{
		Note[] newNotes = notes.clone();
		float millisecondsPerBeat = getMilliSecondsPerBeat();
		int initialTimeInBar = getTimeInFullBar(millisecondsPerBeat); //In milliseconds - always assumed to be 4 beats in a bar
		int timeAvailableInBar = initialTimeInBar;
		
		int currentNoteLength = getCompletelyRandomNote().noteLength;
		int currentRestLength = getCompletelyRandomNote().restLength;
		
		ArrayList<Integer> newNoteLengths = new ArrayList<>();
		ArrayList<Integer> newRestLengths = new ArrayList<>();
		
		while(timeAvailableInBar > 0)
		{
				currentNoteLength = getRandNoteLength(initialTimeInBar, timeAvailableInBar, currentNoteLength);
				timeAvailableInBar -= currentNoteLength;
				currentRestLength = getRandRestLength(initialTimeInBar, timeAvailableInBar, currentRestLength);
				timeAvailableInBar -= currentRestLength;
				newNoteLengths.add(currentNoteLength);
				newRestLengths.add(currentRestLength);
		}
		
		//Copies and returns only new notes that were mutated 
		//-> dropping end notes that may not fit into the bar anymore with the new note/rest lengths
		//Or if there is space for new notes create new notes to fill it
		return assignNewLengths(newNotes, newNoteLengths, newRestLengths);
	}
	
	/*
	 * Assigns mutated note lengths and rest to the section notes
	 */
	private Note[] assignNewLengths(Note[] notes, ArrayList<Integer> newNoteLengths, ArrayList<Integer> newRestLengths)
	{
		Note[] newNotes = notes.clone();
		int numOfNewLengths = newNoteLengths.size();
		Note[] arrayCopy = new Note[numOfNewLengths];
		
		int numOfExtraNotes = 0;
		if(newNotes.length < numOfNewLengths) //there are more lengths than before -> generate extra MidiNotes to go with them
		{			
			for(int i=0; i < newNotes.length; i++)
			{
				arrayCopy[i] = newNotes[i];
				arrayCopy[i].noteLength = newNoteLengths.get(i);
				arrayCopy[i].restLength = newRestLengths.get(i);
			}
			
			numOfExtraNotes = newNotes.length - numOfExtraNotes;
			for(int i = newNotes.length; i < numOfNewLengths; i++)
			{
				arrayCopy[i] = new Note(
						getRandomSmoothedNote(arrayCopy[i-1].midiNote, notesAllowed), 
						newNoteLengths.get(i), 
						newRestLengths.get(i)
						);
			}
		}
		else // there are the same (or less) number of lengths than before -> remove extra notes
		{
			for(int i=0; i < numOfNewLengths; i++)
			{
				arrayCopy[i] = newNotes[i];
				arrayCopy[i].noteLength = newNoteLengths.get(i);
				arrayCopy[i].restLength = newRestLengths.get(i);
			}
		}
		
		return arrayCopy;
	}
	
	/*
	 * Creates and returns a note generated completely randomly 
	 * (no use of defined probabilities)
	 */
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
	
	/*
	 * Copies bar notes multiple times
	 * Returns array of repeated bar notes
	 */
	protected Note[] generateRepeatedBars(int numberOfRepeatedBars, Note[] barNotes)
	{
		Note[] repeatedBarNotes = new Note[barNotes.length * numberOfRepeatedBars];
		this.barNotes = barNotes;
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
	
	/*
	 * Returns a random integer between the "mix" and "max"
	 */
	private int getRandomInt(int min, int max)
	{
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}	
	
	/*
	 * Generates all notes in the section
	 */
	protected Note[] generateSectionNotes()
	{
		ArrayList<Note> sectionNotes = new ArrayList<Note>();
		ArrayList<Note> barNotes = generateBarNotes();
		
		for (Note note : barNotes) {
			sectionNotes.add(note);
		}
		
		return sectionNotes.toArray(new Note[sectionNotes.size()]); //Convert ArrayList to Note array (Note[]) and return
	}
	
	/*
	 * Gets a random note length
	 * Based on the time it has available
	 * With probabilities for the note length being chosen, based on the previousNoteLength
	 */
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
	
	/*
	 * Gets a random rest length
	 * Based on the time it has available
	 * With probabilities for the rest length being chosen, based on the previousRestLength
	 */
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
	
	/*
	 * Returns the number of millisections per beat for the given song tempo
	 */
	private float getMilliSecondsPerBeat()
	{
		return 60000/tempoBPM;
	}
	
	/*
	 * Returns the number of milliseconds in a full bar (for the given song tempo)
	 */
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
		float millisecondsPerBeat = getMilliSecondsPerBeat();
		int initialTimeInBar = getTimeInFullBar(millisecondsPerBeat); //In milliseconds - always assumed to be 4 beats in a bar
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
	
	/*
	 * Returns the position of an item in an array as if looping through circular positions.
	 * Allows searching for an item from a certain "distance" away
	 */
	private int getCircularArrayPos(int arraySize, int nextPos)
	{
		if(nextPos < 0)
			return (arraySize+1) + nextPos;
		if(nextPos > arraySize)
			return nextPos - (arraySize+1);
		return nextPos; 
	}
	
	/*
	 * Returns all midi notes in the scale of the Song's Key note input
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
