package com.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainView {

    private Stage stage;

    private HorsesView horsesView;
    private OptionsView optionsView;
    
    @FXML
    private Label lblRoadDistance;

    @FXML
    private Label lblRoadState;

    @FXML
    private AnchorPane contentPane;
    
    @FXML
    private ScrollPane roadView;
    
    @FXML
    private CheckBox chbConnected;

    public void initialize(AnchorPane contentPane, String name, Stage stage) {
	this.stage = stage;
	this.stage.setTitle(name);
	this.stage.setResizable(false);
	this.contentPane = contentPane;
	chbConnected.setDisable(true);
	initViews();
	createScene();
    }

    private void initViews() {
	FXMLLoader loader = new FXMLLoader();

	try {
	    FileInputStream inputview = new FileInputStream(new File("./views/horsesview.fxml"));
	    ScrollPane pane = loader.load(inputview);
	    pane.setLayoutX(14);
	    pane.setLayoutY(14);
	    horsesView = loader.getController();	    
	    horsesView.init();
	    horsesView.setContentPane(pane);

	    this.contentPane.getChildren().set(0, horsesView.getContentPane());

	    inputview = new FileInputStream(new File("./views/optionsview.fxml"));
	    loader = new FXMLLoader();
	    AnchorPane optionsPane = loader.load(inputview);
	    optionsPane.setLayoutX(14);
	    optionsPane.setLayoutY(391);
	    optionsView = loader.getController();
	    optionsView.init();
	    optionsView.setContentPane(optionsPane);
	    this.contentPane.getChildren().set(1, optionsView.getContentPane());
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }
    

    private void createScene() {
	Scene scene = new Scene(contentPane);
	stage.setScene(scene);
	stage.show();

    }

    public OptionsView getOptionsView() {
	return optionsView;

    }

    public void showMessage(String title, String header, String content, Class<?> class1) {

	AlertType type = AlertType.NONE;
	if (class1.getClass().equals(Exception.class)) {
	    type = AlertType.ERROR;
	}else if(class1.getClass().equals(String.class)) {
	    type = AlertType.INFORMATION;
	}
	Alert alert = new Alert(type,content, ButtonType.OK);
	alert.setTitle(title);
	alert.setHeaderText(header);
	alert.setContentText(content);
	
	alert.show();
	

    }

    public Label getDistanceLabel() {
	return this.lblRoadDistance;
	
    }
    
    public Label getStateLabel() {
	return this.lblRoadState;
    }
    
    public CheckBox getConnectionChb() {
	return this.chbConnected;
    }

    public HorsesView getHorsesView() {
	return horsesView;
    }

}
