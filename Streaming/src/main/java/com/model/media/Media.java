package com.model.media;

import java.awt.IllegalComponentStateException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

import com.google.gson.JsonObject;

public class Media {

    private InputStream input;
    private AudioFormat format;
    private byte[] targetData;
    private String srcFile;

    public Media() {

    }

    public Media(String srcFile) {

	this.srcFile = srcFile;
	openFile();
    }

    private void openFile() {
	File file = new File(srcFile);
	if (!file.exists()) {
	    System.out.println("not exists");
	}
	try {
	    input = AudioSystem.getAudioInputStream(file);
	    int frameSize = ((AudioInputStream) input).getFormat().getFrameSize();
	    setTargetData(new byte[frameSize]);
	} catch (UnsupportedAudioFileException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public Media(AudioFormat format) {
	this.format = format;
    }

    public Media(InputStream input) {
	this.input = input;
    }

    public Media(InputStream input, AudioFormat format) {
	this.input = input;
	this.format = format;
    }

    public void setInputStream(InputStream stream) {
	this.input = stream;
    }

    public int getBufferSize() {
	return targetData.length;
    }

    public InputStream getInputStream() {

	if (input == null) {
	    throw new IllegalAccessError("first start read data event");
	}
	return input;
    }

    public void start() {
	if (input == null) {
	    throw new IllegalComponentStateException("First load any input stream");
	}
	format = ((AudioInputStream) input).getFormat();

    }

    public JsonObject getJsonAudioFormat() {
	if (format == null) {
	    throw new IllegalComponentStateException("format was null");
	}
	JsonObject json = new JsonObject();
	json.addProperty("encoding", format.getEncoding().toString());
	json.addProperty("sample-rate", format.getSampleRate());
	json.addProperty("sample-size", format.getSampleSizeInBits());
	json.addProperty("channels", format.getChannels());
	json.addProperty("big-endian", format.isBigEndian());
	json.addProperty("frame-rate", format.getFrameRate());
	json.addProperty("frame-size", format.getFrameSize());
	return json;
    }

    public static AudioFormat parseFormat(JsonObject audioInfo) {
	AudioFormat format = null;

	Encoding encoding = new Encoding(audioInfo.get("encoding").getAsString());
	float sampleRate = audioInfo.get("sample-rate").getAsFloat();
	int sampleSizeInBits = audioInfo.get("sample-size").getAsInt();
	int channels = audioInfo.get("channels").getAsInt();
	boolean bigEndian = audioInfo.get("big-endian").getAsBoolean();
	float frameRate = -1f;

	frameRate = audioInfo.get("frame-rate").getAsFloat();
	int frameSize = audioInfo.get("frame-size").getAsInt();
	format = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);

	return format;

    }

    public AudioFormat getAudioFormat() {
	return format;
    }

    public void setAudioFormat(AudioFormat format) {
	this.format = format;
    }

    public void setTargetData(byte[] targetData) {
	this.targetData = targetData;
    }

    public void reload() {
	openFile();
    }

    public static void main(String[] args) throws Exception {
	Media m = new Media("./music/Van Halen - Jump.wav");
	m.start();
	String s = m.getJsonAudioFormat().toString();
	System.out.println(s);
	SourceDataLine sourceLine = null;

	DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, m.getAudioFormat());
	try {
	    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
	    sourceLine.open(m.getAudioFormat());
	    sourceLine.start();
	    int r = 0;
	    byte[] data = new byte[1024];

	    while (true) {
		r=m.getInputStream().read(data, 0, data.length);
		if(r==-1) {
		    break;
		}
		sourceLine.write(data, 0, data.length);
	    }
	} catch (LineUnavailableException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
