package com.controller;

import java.io.File;
import java.io.FileInputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.connection.ITCPListener;
import com.model.webclient.Client;
import com.view.MainView;
import com.view.OptionsView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Controller extends Application implements ITCPListener {

    public static final int QUERIES_PROXY_PORT = 6668;
    public static final int MICRO_PROXY_PORT = 6666;
    public static final int AUDIO_PROXY_PORT = 6667;
    protected static final String QUERY_ROAD_STATUS = "query=road-status";

    private MainView main;
    private Client client;
    private boolean connected;

    public Controller() {
	connected = false;
	this.client = new Client();
	client.addListener(this);
	connectClient();

    }

    @Override
    public void start(Stage stage) throws Exception {
	FileInputStream file = new FileInputStream(new File("./views/mainview.fxml"));
	FXMLLoader loader = new FXMLLoader();
	AnchorPane contentPane = loader.load(file);
	main = loader.getController();
	main.initialize(contentPane, "Horses Road", stage);
	stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	    public void handle(WindowEvent event) {
		System.exit(0);
	    }
	});

	addEventHandlers();

	if (connected) {
	    main.getConnectionChb().setSelected(true);
	    main.getConnectionChb().setDisable(true);
	}

    }

    private void addEventHandlers() {

	EventHandler<ActionEvent> eventHandler = createActionEventHandler();

	Button[] buttons = main.getOptionsView().getButtons();
	CheckBox[] cbxs = main.getOptionsView().getCheckBox();

	for (Button button : buttons) {
	    button.setOnAction(eventHandler);
	}

	for (CheckBox cbx : cbxs) {
	    cbx.setOnAction(eventHandler);
	}

    }

    private EventHandler<ActionEvent> createActionEventHandler() {
	EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {

	    public void handle(ActionEvent event) {
		Object src = event.getSource();
		if (src instanceof CheckBox) {
		    CheckBox newSrc = (CheckBox) src;
		    String srcTxt = newSrc.getText();
		    if (srcTxt.equalsIgnoreCase(OptionsView.COMMAND_AUDIO_STREAMING)) {
			listenAudioStreaming(newSrc.isSelected());
		    } else if (srcTxt.equalsIgnoreCase(OptionsView.COMMAND_ROAD_STREAMING)) {
			listenRoadStreaming(newSrc.isSelected());
		    }
		} else if (src instanceof Button) {
		    Button newSrc = (Button) event.getSource();
		    String srcTxt = newSrc.getText();
		    if (srcTxt.equalsIgnoreCase(OptionsView.COMMAND_BET)) {
			String horse = main.getOptionsView().getActualHorse();
			double bet = main.getOptionsView().getBetCount();
			bet(horse, bet);
		    } else if (srcTxt.equalsIgnoreCase(OptionsView.COMMAND_UPDATE_STREAMING)) {
			if (!connected) {
			    connectClient();
			}
			realizeQueryClient(QUERY_ROAD_STATUS);
		    }
		}

	    }
	};
	return eventHandler;
    }

    public void realizeQueryClient(String query) {
	if (connected) {
	    try {
		this.client.executeQuery(query);
	    } catch (Exception e) {
		updateGUI(new MessageUpdater("ERROR", "Servicio en puerto", e.getMessage(), Exception.class, this));
	    }
	} else {
	    showMessageInMain("INFORMATION", "Conexión", "Presione actualizar para reintentar conexión", String.class);

	}
    }

    private void connectClient() {
	try {
	    client.start();
	    
	    connected = true;
	    main.getConnectionChb().setSelected(true);
	    main.getConnectionChb().setDisable(true);

	} catch (Exception e) {

	}
    }

    public void listenAudioStreaming(boolean listen) {
	if (connected) {
	    try {
		client.setListenOnPort(AUDIO_PROXY_PORT, listen);
	    } catch (Exception e) {
		showMessageInMain("ERROR", "Servicio en puerto", e.getMessage(), Exception.class);
	    }
	} else {
	    showMessageInMain("INFORMATION", "Conexión", "Presione actualizar para reintentar conexión", String.class);

	}
    }

    public void listenRoadStreaming(boolean listen) {
	client.setRoadStreamingListen(listen);
    }

    public void bet(String horse, double bet) {
	try {
	    this.client.bet(horse, bet);
	} catch (Exception e) {
	    showMessage("ERROR", "Bet error", e.getMessage(), Exception.class);
	}
    }

    public void onInputMessageData(String data, ITCPListener callback) {
	try {
	    JsonObject json = (JsonObject) new JsonParser().parse(data);
	    boolean status = json.get("status").getAsString().contains("200 OK");

	    if (!status) {
		showMessage("STATUS", "ANOTHER STATUS", json.get("info").getAsString(), String.class);
	    }

	    String requestType = json.get("request-type").getAsString();
	    if (requestType.equals("query")) {

		String query = json.get("query").getAsString();
		if (query.equals("road-status")) {
		    setRoadStatusOnGUI(json.get("road-status").getAsJsonObject());

		}
	    } else if (requestType.equals("bet")) {
		String response = json.get("bet-response").getAsString();
		showMessage("Información", "Información apuesta", response, String.class);
	    }
	} catch (Exception e) {

	    showMessage("ERROR", "Json Error", e.getMessage(), Exception.class);
	}

    }

    private void setRoadStatusOnGUI(JsonObject json) {
	double distance = json.get("distance").getAsDouble();
	String state = json.get("estado").getAsString();

	int qHorses = json.get("cantidad-caballos").getAsInt();
	final String[][] horses = new String[qHorses][7];

	int id = 1;
	int name = 0;
	int speed = 2;
	int dis = 3;
	int bet = 4;

	for (int i = 1; i <= horses.length; i++) {
	    String line = json.get(i + "").getAsString();

	    String[] infoLine = line.split(" - ");

	    horses[i - 1][5] = "";
	    if (infoLine.length > 5) {
		horses[i - 1][5] = "OWN";
	    }
	    horses[i - 1][0] = infoLine[id];
	    horses[i - 1][1] = infoLine[name];
	    horses[i - 1][2] = infoLine[speed];
	    horses[i - 1][3] = infoLine[dis];
	    horses[i - 1][4] = infoLine[bet];

	    double runningDistance = Double.parseDouble(infoLine[dis].trim().replace(',', '.'));
	    horses[i - 1][6] = (runningDistance / distance) + "";
	}

	// TODO
	updateGUI(new HorsesUpdater(distance, state, horses, this));

    }

    private void showMessage(String title, String header, String content, Class<?> class1) {

	updateGUI(new MessageUpdater(title, header, content, class1, this));
    }

    private void updateGUI(Runnable runnable) {

	Platform.runLater(runnable);
    }

    public static void main(String[] args) {
	launch(args);
    }

    public void showMessageInMain(String title, String header, String content, Class<?> class1) {
	main.showMessage(title, header, content, class1);

    }

    public void setDistanceLabel(String text) {
	main.getDistanceLabel().setText(text);

    }

    public void setRoadState(String state) {
	main.getStateLabel().setText(state);
    }

    public void setHorsesView(String[][] horses) {
	main.getHorsesView().setHorses(horses);
	main.getOptionsView().setSelections(horses);

    }

}
