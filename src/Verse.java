/**
 * 
 */

/**
 * @author Dharius
 * Verse section
 */
public class Verse extends Section {

	public Verse(MidiNote  key, int tempoBPM) {
		super(key, tempoBPM);
//		Verse.numberOfBars = 8;
		Verse.numberOfBars = 16;
		Verse.numOfRepetitions = 2;
		Verse.noteLengthChances = new int[] {
//				1,
				2,
				8,
				32,
				16
				};
		notes = generateRepeatedBars(numOfRepetitions, generateSectionNotes());;
//		notes = generateSectionNotes();
	}
	
	/*
	 * Clones verse1 into a new Verse object
	 */
	public Verse cloneVerse(Verse verse1)
	{
		Verse verse2 = new Verse(key, tempoBPM);
		verse2.notes = verse1.notes;
		verse2.barNotes= verse1.barNotes;
		
		return verse2;
	}
}
