import java.util.concurrent.ThreadLocalRandom;

/**
 * 
 */

/**
 * @author Dharius
 *
 */
public class Structure {
	Section[] sections;
	
	/**
	 * 
	 */
	public Structure(MidiNote key, int tempoBPM) 
	{
		sections = selectRandStructure(key, tempoBPM);
	}
	
	private Section[] selectRandStructure(MidiNote key, int tempoBPM)
	{
		Intro intro = new Intro(key, tempoBPM);
		Verse verse1 = new Verse(key, tempoBPM);
		Chorus chorus = new Chorus(key, tempoBPM);
		Verse verse2 = new Verse(key, tempoBPM);
		Break break1 = new Break(key, tempoBPM);
		Outro outro = new Outro(key, tempoBPM);
		Section[][] sections = {
				{intro, verse1, chorus, verse2, break1, chorus, chorus, intro},
				{intro, verse1, chorus, verse2, break1, chorus, chorus, outro},
				{intro, verse1, chorus, verse2, break1, chorus, intro},
				{intro, verse1, chorus, verse2, break1, chorus, outro},
				};
		
		return sections[getRandSectionPos(sections.length-1)];
	}
	
	public void evolve(int rating)
	{
		for (Section section : sections) {
			section.evolve(rating);
		}
	}
	
	private int getRandSectionPos(int max)
	{
		return ThreadLocalRandom.current().nextInt(0, max + 1);
	}

}
