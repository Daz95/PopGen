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
		this.tempoBPM = 60;
		this.key = MidiNote.Bflat;
		this.channel = initMidiChannel(acousticGuitarSteel, 1);
		
		this.songStructure = new Structure(key, tempoBPM);
		
		ArrayList<Note> notesList = new ArrayList<Note>();
		for (Section section : songStructure.sections) {
			for (Note note : section.notes) {
				notesList.add(note);
			}
		}
		
		Note[] notes = notesList.toArray(new Note[notesList.size()]);
		
//		Note[] notes = {
//				Note.Csharp, Note.E, Note.Csharp, 
//				Note.Csharp, Note.E, Note.Csharp, 
//				Note.Csharp, Note.E, Note.Csharp, 
//				Note.Dsharp, Note.Csharp, Note.B
//				};
//		int[][] noteLengths = {
//				{100, 150}, {100, 150}, {100, 100},
//				{100, 150}, {100, 150}, {100, 100},
//				{100, 150}, {100, 150}, {100, 100},
//				{100, 150}, {100, 100}, {100, 100},
//				};
		
		for (Note note : notes) {
			this.play(note.midiNote);
			
			Thread.sleep(note.noteLength);
			this.silence();
			Thread.sleep(note.restLength);
		}
		
//		for(int i=0; i < notes.length; i++)
//		{
//			this.play(notes[i]);
//			
//			try {
//				Thread.sleep(noteLengths[i][0]*2);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			this.silence();
//			try {
//				Thread.sleep(noteLengths[i][1]*2);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
//		this.play(Note.C);;//On channel 0, play note number 60 with velocity 100 
//        try { Thread.sleep(100); // wait time in milliseconds to control duration
//        } catch( InterruptedException e ) { }
//        this.silence();//turn of the note
	}
	
	//
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
//		try {
//			//int barTime = 40;
//			generateTone(22050, (float)(22050/261.626));
//			loopSound(5);
////			generateTone(22050, (float)(0));
////			loopSound(15);
////			generateTone(22050, (float)(22050/261.626));
////			loopSound(5);
////			generateTone(22050, (float)(0));
////			loopSound(5);
//		} catch (LineUnavailableException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	private void playSong() {
//		int songSampleRate = 22050;
//		float currentFrequency;
//		float currentFramesPerWavelength;
//		
//		for (Section section: songStructure.sections) {
//			for(Note note: section.notes) {
//				currentFrequency = note.frequency;
//				currentFramesPerWavelength = songSampleRate / currentFrequency;
//				try {
//					generateTone(songSampleRate, currentFramesPerWavelength);
//					//loopSound();
//				} catch (LineUnavailableException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		
//	}
//	
//    /** Loops the current Clip until a commence false is passed. */
//    public void loopSound(int noteLength) {
//        clip.setFramePosition(0);
//        //clip.start();
//        clip.loop(noteLength);
//        //clip.stop();
//    }
//    
//    public void setVolume() {
//    	final FloatControl control = (FloatControl)
//            clip.getControl( FloatControl.Type.MASTER_GAIN );
//    	control.setValue(control.getMaximum());
//    }
//	
//	public void generateTone(float sampleRate, float framesPerWavelength)
//	        throws LineUnavailableException {
//	        if ( clip!=null ) {
//	            clip.stop();
//	            clip.close();
//	        } else {
//	            clip = AudioSystem.getClip();
//	        }
//	        //boolean addHarmonic = harmonic.isSelected();
//
//	        int intSR = (int)sampleRate;
//	        int intFPW = (int)framesPerWavelength;
//
//	        // oddly, the sound does not loop well for less than
//	        // around 5 or so, wavelengths
//	        int wavelengths = 20;
//	        byte[] buf = new byte[2*intFPW*wavelengths];
//	        AudioFormat audioFormat = new AudioFormat(
//	            sampleRate,
//	            8,  // sample size in bits
//	            2,  // channels
//	            true,  // signed
//	            false  // bigendian
//	            );
//
//	        int maxVol = 127;
//	        for(int i=0; i<intFPW*wavelengths; i++){
//	            double angle = ( (float)(i*2) / ((float)intFPW) ) * (Math.PI);
//	            buf[i*2]=getByteValue(angle);
////	            if(addHarmonic) {
////	                buf[(i*2)+1]=getByteValue(2*angle);
////	            } else {
//	                buf[(i*2)+1] = buf[i*2];
////	            }
//	        }
//
//	        try {
//	            byte[] b = buf;
//	            AudioInputStream audioStream = new AudioInputStream(
//	                new ByteArrayInputStream(b),
//	                audioFormat,
//	                buf.length/2 );
//
//	            clip.open( audioStream );
//	        } catch(Exception e) {
//	            e.printStackTrace();
//	        }
//	    }
//
//    /** Provides the byte value for this point in the sinusoidal wave. */
//    private static byte getByteValue(double angle) {
//        int maxVol = 127;
//        return (new Integer(
//            (int)Math.round(
//            Math.sin(angle)*maxVol))).
//            byteValue();
//    }
//    
//    public static void main(String args[])
//    {
//    	
//    	//new Song();
//    }
//
//}
