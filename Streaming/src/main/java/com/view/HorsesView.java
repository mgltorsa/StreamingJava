package com.view;

import java.io.File;
import java.io.FileInputStream;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class HorsesView {

    @FXML
    private HorseFrame[] frames;

    @FXML
    private ScrollPane scrollpane;

    @FXML
    private VBox vbox;

    public Node getContentPane() {

	return scrollpane;
    }

    public void setContentPane(ScrollPane pane) {
	this.scrollpane = pane;
    }

    public void init() {

    }

    public void setHorses(String[][] horses) {

	setFrames(horses);

    }

   

    private void setFrames(String[][] horses) {
	FXMLLoader loader = new FXMLLoader();

	boolean first = false;
	if (frames == null) {
	    first = true;
	    frames = new HorseFrame[horses.length];
	}
	try {
	    FileInputStream file = new FileInputStream(new File("./views/horseframe.fxml"));
	    for (int i = 0; i < frames.length; i++) {

		if (first) {
		    AnchorPane p = loader.load(file);

		    frames[i] = loader.getController();
		    frames[i].init();
		    frames[i].setImage("./docs/horse.gif");
		    vbox.getChildren().add(p);

		    loader = new FXMLLoader();
		    file = new FileInputStream(new File("./views/horseframe.fxml"));
		}

		if (horses != null) {
		    frames[i].getLblIdHorse().setText(horses[i][0]);
		    frames[i].getLblNameHorse().setText(horses[i][1]);
		    frames[i].getLblSpeed().setText(horses[i][2]);
		    frames[i].getLblDistance().setText(horses[i][3]);
		    frames[i].getLblBet().setText(horses[i][4]);
		    frames[i].setBackground(Color.WHITE);
		    if (horses[i][5].equalsIgnoreCase("OWN")) {
			frames[i].setBackground(Color.DARKSALMON);
		    }
		    double value = Double.parseDouble(horses[i][6].trim());
		    frames[i].getProgress().setProgress(value);
		    
		}
		
//		frames[i].getImg().setX(frames[i].getImg().getX()+10);

	    }

	    file.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
