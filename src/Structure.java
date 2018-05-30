import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Dharius
 * Creates a valid song structure populating all sections from the chosen structure
 */
public class Structure {
	Section[] sections;
	
	public Structure(MidiNote key, int tempoBPM) 
	{
		sections = selectRandStructure(key, tempoBPM);
	}
	
	/*
	 * Used to select and create a random structure of song sections 
	 * Based on variations of the ABABCBB song structure
	 */
	private Section[] selectRandStructure(MidiNote key, int tempoBPM)
	{
		Intro intro = new Intro(key, tempoBPM);
		Verse verse1 = new Verse(key, tempoBPM);
		Chorus chorus = new Chorus(key, tempoBPM);
		Verse verse2 = (Verse)verse1.mutate(verse1, (float)0.1); // change verse 2 to vary slightly (remaining 90% the same as verse1)
		Break break1 = new Break(key, tempoBPM);
		Outro outro = new Outro(key, tempoBPM);
		Section[][] sections = {
				{intro, verse1, chorus, verse1, chorus, break1, chorus, chorus, intro},
				{intro, verse1, chorus, verse1, chorus, break1, chorus, chorus, outro},
				{intro, verse1, chorus, verse1, chorus, break1, chorus, intro},
				{intro, verse1, chorus, verse1, chorus, break1, chorus, outro},
				{intro, verse1, chorus, verse2, chorus, break1, chorus, chorus, intro},
				{intro, verse1, chorus, verse2, chorus, break1, chorus, chorus, outro},
				{intro, verse1, chorus, verse1, chorus, break1, chorus, chorus},
				{intro, verse1, chorus, verse2, chorus, break1, chorus, chorus},
				{intro, verse1, chorus, verse2, chorus, break1, chorus, intro},
				{intro, verse1, chorus, verse2, chorus, break1, chorus, outro},
				};
		// Choose a random section structure
		return sections[getRandSectionPos(sections.length-1)];
	}
	
	/*
	 * Evolves the Sections in the structure based on the user's rating
	 */
	public void evolve(int rating)
	{
		for (Section section : sections) {
			section.GAEvolve(500, 200, rating);
		}
	}
	
	/*
	 * Returns a random number between 0 and max
	 */
	private int getRandSectionPos(int max)
	{
		return ThreadLocalRandom.current().nextInt(0, max + 1);
	}

}
