package com.model.media;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Player extends Thread {

//    public static Info[] mixerInfo;
//    private static int currentInfo = 0;

    private SourceDataLine sourceLine;
    private InputStream input;
    private AudioFormat format;
    private OutputStream output;
    private boolean listen;

    public Player(AudioFormat format) {
	this.format = format;
	DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, this.format);
	try {
	    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
	    sourceLine.open(format);
	    sourceLine.start();
//	    restVolume(20.0f);
	    float m = ((FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN)).getMaximum();
	    ((FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN)).setValue(m);
	    listen = true;
	} catch (LineUnavailableException e) {
	    e.printStackTrace();
	}
    }

    public Player(Media media, FileOutputStream fileOutputStream) {
	this(media.getAudioFormat());
	input = media.getInputStream();
	output = fileOutputStream;
    }

    @Override
    public void run() {
	byte[] data = new byte[1024];
	int bytesRead = 0;
	while (true) {
	    try {
		bytesRead = input.read(data, 0, data.length);

		if (listen) {

		    if (bytesRead > 0) {
			sourceLine.write(data, 0, data.length);
		    }
		}
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}
    }

    public void restVolume(float f) {
	((FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-f);
    }

    public synchronized void play(byte[] bytes) {

	if (listen) {
	    if (input == null) {
		playRaw(bytes);
	    } else {
		try {
		    output.write(bytes, 0, bytes.length);

		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}

    }

    public synchronized void playRaw(byte[] bytes) {
	sourceLine.write(bytes, 0, bytes.length);
    }

    public void setListen(boolean listen) {
	this.listen = listen;

    }
}
