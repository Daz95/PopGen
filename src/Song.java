//import java.io.ByteArrayInputStream;
//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.Clip;
//import javax.sound.sampled.FloatControl;
//import javax.sound.sampled.LineUnavailableException;
//import javax.swing.JApplet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.omg.PortableInterceptor.IORInterceptor;

public class Song extends JApplet {
	
    
    // http://music.stackexchange.com/q/22133/1941
    // http://www.midi.org/techspecs/gm1sound.php
    //Instrument list can be viewed at: https://www.midi.org/specifications/item/gm-level-1-sound-set
    private static final int acousticGuitarSteel = 26;
    private final MidiChannel channel;
    int tempoBPM;
    MidiNote key;
	Structure songStructure;
	
	
	/*
	 * Creates a song including multiple sections of notes for one midi instrument (In this case, a steel acoustic guitar)
	 */
	public Song() throws MidiUnavailableException, InterruptedException {
		this.tempoBPM = 120;
		this.key = MidiNote.G;
		this.channel = initMidiChannel(acousticGuitarSteel, 1);
		
		this.songStructure = new Structure(key, tempoBPM);
		
		
		ArrayList<Note> notesList = new ArrayList<Note>();
		for (Section section : songStructure.sections) {
			for (Note note : section.notes) {
				notesList.add(note);
			}
		}
		
		Note[] notes = notesList.toArray(new Note[notesList.size()]);
		playNotes(notes);
//		playKeyNotes(key);
	}

	
	private static MidiChannel initMidiChannel(int instrument, int channelNo) throws MidiUnavailableException {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        MidiChannel channel = synth.getChannels()[channelNo];
        // MIDI instruments are traditionally numbered from 1,
        // but the javax.midi API numbers them from 0
        channel.programChange(instrument - 1);
        //channel.setChannelPressure(10);  // optional vibrato
        return channel;
    }
	
	public void playNotes(Note[] notes) throws InterruptedException
	{
		for (Note note : notes) {
			this.play(note.midiNote);
			
			Thread.sleep(note.noteLength);
			this.silence();
			Thread.sleep(note.restLength);
		}
	}
	
	public void playKeyNotes(MidiNote key) throws InterruptedException
	{
		MidiNote[] notesInKey = getNotesAllowed(key);
		
		for (MidiNote note : notesInKey) {
			this.play(note);
			Thread.sleep(500);
			this.silence();
		}
	}
	
	/*
	 * *REMOVE WHEN COMPLETE*
	 * Used for debugging - checking that key notes are correct
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
	
	public void play(MidiNote note) {
        this.channel.noteOn(note.midiNoteNumber, 127); 
    }

    public void silence() {
        this.channel.allNotesOff();
    }
		
	
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					new Song();
				} catch (MidiUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
    }
		
}