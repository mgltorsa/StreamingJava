package com.model.media;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

public class Player extends Thread {

//    public static Info[] mixerInfo;
//    private static int currentInfo = 0;

    private SourceDataLine sourceLine;
    private InputStream input;
    private AudioFormat format;
    private OutputStream output;

    public Player(AudioFormat format) {
	this.format = format;
	DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
	try {
	    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
	    sourceLine.open(format);
	    sourceLine.start();
	    restVolume(20.0f);
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
	int bytesReaded = 0;
	while (true) {
	    try {
		bytesReaded = input.read(data, 0, data.length);
		if (bytesReaded > 0) {
		    sourceLine.write(data, 0, data.length);

		}
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}
    }

    private void restVolume(float f) {
	((FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-f);
    }

    public synchronized void play(byte[] bytes) {

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

    private void playAudio() {
	byte[] buffer = new byte[10000];
	try {
	    int count;
	    while ((count = input.read(buffer, 0, buffer.length)) != -1) {
		if (count > 0) {
		    sourceLine.write(buffer, 0, count);
		}
	    }
	} catch (Exception e) {
	    // TODO: handle exception
	}

    }

    public synchronized void playRaw(byte[] bytes) {
	sourceLine.write(bytes, 0, bytes.length);
    }

    public static void main(String[] args) {
	Info[] infos = AudioSystem.getMixerInfo();
	for (Info inf : infos) {
	    System.out.println(inf);
	}
    }
}
