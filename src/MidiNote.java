public enum MidiNote 
{		
	G(55), Gsharp(56), Aflat(56), A(57), Asharp(58), Bflat(58), B(59), Cflat(60), C(60), Csharp(61), Dflat(61), D(62), Dsharp(63), Eflat(63), E(64), F(65), Fsharp(66), Gflat(66);
	
	public final int midiNoteNumber;
	
	MidiNote(int midiNoteNumber) {
	    this.midiNoteNumber = midiNoteNumber;
	}

}