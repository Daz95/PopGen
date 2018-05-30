/*
 * MidiNote enumerator
 * To map MidiNote integers to actual note names
 */
public enum MidiNote 
{		
	G(55), Gsharp(56), Aflat(56), A(57), Asharp(58), Bflat(58), B(59), Cflat(59), C(60), 
	Csharp(61), Dflat(61), D(62), Dsharp(63), Eflat(63), E(64), F(65), Fsharp(66), Gflat(66),
	
	Ghi(67), GsharpHi(68), AflatHi(68), Ahi(69), AsharpHi(70), BflatHi(70), Bhi(71), CflatHi(71), Chi(72),
	CsharpHi(73), DflatHi(73), Dhi(74), DsharpHi(75), EflatHi(75), Ehi(76), Fhi(77), FsharpHi(78), GflatHi(78);
	
	public final int midiNoteNumber;
	
	MidiNote(int midiNoteNumber) {
	    this.midiNoteNumber = midiNoteNumber;
	}

}