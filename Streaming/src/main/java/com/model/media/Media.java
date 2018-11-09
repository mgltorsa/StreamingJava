package com.model.media;

import java.awt.IllegalComponentStateException;
import java.io.File;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

import com.google.gson.JsonObject;

public class Media {

    private volatile InputStream input;
    private AudioFormat format;
    private byte[] targetData;
    private String srcFile;

    public Media() {

    }

    public Media(String srcFile) {
	this.srcFile = srcFile;
    }

    private void openFile() throws IllegalComponentStateException{
	File file = new File(srcFile);
	if (!file.exists()) {
	   throw new IllegalComponentStateException("src file path doesn't exist");
	}
	loadInputStream(file);

    }

    private void loadInputStream(File file) {

	try {
	    input = AudioSystem.getAudioInputStream(file);
	    int frameSize = ((AudioInputStream) input).getFormat().getSampleSizeInBits() / 8;
	    setTargetData(new byte[frameSize]);
	} catch (Exception e) {
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
	setInputStream(input);
	setAudioFormat(format);
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

    public void start() throws IllegalComponentStateException {
	openFile();

	if (input == null) {
	    throw new IllegalComponentStateException("First load any input stream");
	}
	setAudioFormat(((AudioInputStream) input).getFormat());
	//TODO
//	setAudioFormat(createDefaultFormat());

    }

    public AudioFormat createDefaultFormat() {
	float sampleRate = 16000f;
	int sampleSizeInBits = 16;
	int channels = 2;
	boolean bigEndian = false;
	boolean signed = true;
	format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	return format;
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
	float sampleRate = format.getSampleRate();
	int sizeInBytes = format.getSampleSizeInBits() / 8;
	setTargetData(new byte[(int) (sampleRate * sizeInBytes) / 2]);

    }

    public void setTargetData(byte[] targetData) {
	this.targetData = targetData;
    }

    public void reload() {
	openFile();
    }

    public static void main(String[] args) throws Exception {

	new Thread(new Runnable() {

	    public void run() {
		try {
		    Media m = new Media("./music/Van Halen Jump.wav");
		    m.start();
		    String s = m.getJsonAudioFormat().toString();
		    System.out.println(s);

		    DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, m.getAudioFormat());

		    SourceDataLine sourceLine = null;
		    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
		    sourceLine.open(m.getAudioFormat());
		    sourceLine.start();
		    int r = 0;
		    byte[] data = new byte[1024];

		    while (true) {
			r = m.getInputStream().read(data, 0, data.length);
			if (r == -1) {
			    break;
			}

			sourceLine.write(data, 0, data.length);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

	    }
	}).start();

//	new Thread(new Runnable() {
//
//	    public void run() {
//		Media m = new Microphone();
//		m.start();
//
//		DataLine.Info s2 = new DataLine.Info(SourceDataLine.class, m.getAudioFormat());
//
//		SourceDataLine sourceLine = null;
//		DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, m.getAudioFormat());
//
//		try {
//		    sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
//		    sourceLine.open(m.getAudioFormat());
//		    sourceLine.start();
//		    int r = 0;
//		    byte[] data = new byte[1024];
//
//		    while (true) {
//			r = m.getInputStream().read(data, 0, data.length);
//			if (r == -1) {
//			    break;
//			}
//			sourceLine.write(data, 0, data.length);
//		    }
//		} catch (Exception e) {
//
//		}
//	    }
//	}).start();

    }

}
