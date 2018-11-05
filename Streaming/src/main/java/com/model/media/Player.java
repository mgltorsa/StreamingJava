package com.model.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Player extends Thread {

    private SourceDataLine sourceLine;
    private OutputStream output;
    private InputStream input;

    public Player(AudioFormat format) {
	DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
	try {
	    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
	    sourceLine.open(format);
	    sourceLine.start();
	} catch (LineUnavailableException e) {
	    e.printStackTrace();
	}
    }

    public Player(Media media, OutputStream stream) {

	this(media.getAudioFormat());
	output = stream;
	input = media.getInputStream();

    }

    @Override
    public void run() {

	byte[] bytes = new byte[1024];
	try {
	    sleep(500);
	    int bytesReaded = 0;
	    while (true) {
		if (bytesReaded != -1) {
		    bytesReaded = input.read(bytes, 0, bytes.length);
		    sourceLine.write(bytes, 0, bytes.length);
		}
	    }
	} catch (Exception e) {

	}
    }

    public synchronized void play(byte[] bytes) {
	try {
	    output.write(bytes, 0, bytes.length);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public synchronized void playRaw(byte[] bytes) {
	sourceLine.write(bytes, 0, bytes.length);
    }
}
