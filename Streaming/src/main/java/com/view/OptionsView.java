package com.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

public class OptionsView {

    public static final String COMMAND_AUDIO_STREAMING = "Audio Streaming";
    public static final String COMMAND_ROAD_STREAMING = "Carrera Live!";
    public static final String COMMAND_UPDATE_STREAMING = "Actualizar datos carrera";
    public static final String COMMAND_BET = "Apostar";

    private String[] ids;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private CheckBox chbAudioStream;

    @FXML
    private CheckBox chbRoadStream;

    @FXML
    private Button btnUpdateRoad;

    @FXML
    private Button btnBet;

    @FXML
    private ChoiceBox<String> chbcHorses;

    @FXML
    private Spinner<Double> spnBet;

    public void init() {
	SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,
		0.5);
	spnBet.setValueFactory(factory);
	spnBet.focusedProperty().addListener(new ChangeListener<Boolean>() {

	    public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean nv) {
		
		if(nv) {
		    return;
		}
		
		if (!spnBet.isEditable()) return;
		    String text = spnBet.getEditor().getText();
		    SpinnerValueFactory<Double> valueFactory = spnBet.getValueFactory();
		    if (valueFactory != null) {
		        StringConverter<Double> converter = valueFactory.getConverter();
		        if (converter != null) {
		            Double value = converter.fromString(text);
		            valueFactory.setValue(value);
		        }
		    }
		
	    }
	});
	spnBet.setEditable(true);
	
	btnUpdateRoad.setText(COMMAND_UPDATE_STREAMING);
	btnBet.setText(COMMAND_BET);
	chbAudioStream.setText(COMMAND_AUDIO_STREAMING);
	chbRoadStream.setText(COMMAND_ROAD_STREAMING);
	chbAudioStream.setSelected(true);

    }

    public Node getContentPane() {
	return contentPane;
    }

    public void setContentPane(AnchorPane optionsPane) {
	this.contentPane = optionsPane;
    }

    public Button[] getButtons() {
	Button[] buttons = new Button[2];
	buttons[0] = btnBet;
	buttons[1] = btnUpdateRoad;
	return buttons;
    }

    public CheckBox[] getCheckBox() {
	CheckBox[] chbs = new CheckBox[2];
	chbs[0] = chbAudioStream;
	chbs[1] = chbRoadStream;

	return chbs;
    }

    public String getActualHorse() {
	int value = chbcHorses.getSelectionModel().getSelectedIndex();
	if (ids == null || value == -1) {
	    return null;
	}
	return ids[value];
    }

    public double getBetCount() {
	return spnBet.getValue();

    }

    public void setSelections(String[][] horses) {

	if (ids == null) {
	    String[] array = new String[horses.length];
	    ids = new String[horses.length];
	    for (int i = 0; i < array.length; i++) {
		ids[i] = horses[i][0];
		array[i] = "Caballo id - " + horses[i][0];
	    }
	    ObservableList<String> list = FXCollections.observableArrayList(array);
	    chbcHorses.setItems(list);
	}
    }

}
