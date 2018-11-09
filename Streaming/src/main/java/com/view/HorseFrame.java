package com.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class HorseFrame {
    
    
    @FXML
    private AnchorPane contentPane;
    
    @FXML
    private ImageView imgHorse;

    @FXML
    private Label lblHorseName;

    @FXML
    private Label lblSpeed;

    @FXML
    private Label lblDistance;

    @FXML
    private ProgressBar pgbHorse;

    @FXML
    private Label lblBet;

    @FXML
    private Label lblIdHorse;

    public void init() {
	
	pgbHorse.setStyle("-fx-accent: burlywood");
//	pgbHorse.setBackground(new Background(new BackgroundFill(Color.BURLYWOOD, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void setImage(String path) {
	FileInputStream file;
	try {
	    file = new FileInputStream(new File(path));
	    Image img = new Image(file, imgHorse.getFitWidth(), imgHorse.getFitHeight(), imgHorse.isPreserveRatio(),
		    imgHorse.isSmooth());
	    imgHorse.setImage(img);
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public Label getLblSpeed() {
	return lblSpeed;
    }

    public Label getLblDistance() {
	return lblDistance;
    }

    public Label getLblBet() {
	return lblBet;
    }

    public Label getLblIdHorse() {
	return lblIdHorse;
    }

    public Label getLblNameHorse() {
	return lblHorseName;
    }

    public void setBackground(Color color) {
	BackgroundFill b = new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY);
	contentPane.setBackground(new Background(b));
	
    }

    public ProgressBar getProgress() {
	return pgbHorse;
    }

    public ImageView getImg() {

	return imgHorse;
    }

}
