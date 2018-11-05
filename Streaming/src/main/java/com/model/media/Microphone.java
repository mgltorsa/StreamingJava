package com.model.media;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class Microphone extends Media {

    public Microphone(AudioFormat format) {
	super(format);
    }

    public Microphone() {
	super();
    }

    public void start() {
	try {
	    if (getAudioFormat() == null) {
		setAudioFormat(createDefaultAudioFormat());
	    }
	    DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
	    TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);	    
	    targetLine.open(getAudioFormat());
	    targetLine.start();
	    setTargetData(new byte[targetLine.getBufferSize()/5]);
	    setInputStream(new AudioInputStream(targetLine));
	} catch (Exception e) {

	}
    }

    private AudioFormat createDefaultAudioFormat() {
	float sampleRate = 44100.0f;
	int sampleSizeInBits = 16;
	int channels = 2;
	float frameRate =44100.0f;
	int frameSize = 4;
	boolean bigEndian = false;
	AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
	return format;
    }

}
