package com.controller;

public class MessageUpdater implements Runnable{

    private String title;
    private String header;
    private String content;
    private Class<?> class1;
    private Controller controller;
    
    
    public MessageUpdater(String title, String header, String content, Class<?> class1, Controller controller) {
	this.title=title;
	this.header=header;
	this.content=content;
	this.class1=class1;
	this.controller=controller;
    }

    public void run() {
	controller.showMessageInMain(title,header,content,class1);
	
    }

}
