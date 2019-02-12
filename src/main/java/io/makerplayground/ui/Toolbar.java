/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.generator.upload.ArduinoUploadTask;
import io.makerplayground.generator.upload.RaspberryPiUploadTask;
import io.makerplayground.generator.upload.UploadResult;
import io.makerplayground.generator.upload.UploadTask;
import io.makerplayground.project.Project;
import io.makerplayground.ui.dialog.DeviceMonitor;
import io.makerplayground.ui.dialog.UploadDialogView;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.Optional;

public class Toolbar extends AnchorPane {

    private final ObjectProperty<Project> project;

    @FXML private MenuItem newMenuItem;
    @FXML private MenuItem openMenuItem;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem exportMenuItem;
    @FXML private MenuItem closeMenuItem;

    @FXML private RadioButton diagramEditorButton;
    @FXML private RadioButton deviceConfigButton;
    @FXML private Label statusLabel;
    @FXML private MenuButton deviceMonitorMenuButton;
    @FXML private Button uploadButton;
    @FXML private Separator separator;
    @FXML private Button uploadStatusButton;

    private UploadTask uploadTask;
    private StringProperty logProperty;
    private ImageView uploadStartImageView;
    private ImageView uploadStopImageView;

    public Toolbar(ObjectProperty<Project> project) {
        this.project = project;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ToolBar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(diagramEditorButton, deviceConfigButton);

        diagramEditorButton.setSelected(true);

        deviceMonitorMenuButton.setOnShowing(this::deviceMonitorMenuShowing);

        initUploadButton();
    }

    public void setOnNewButtonPressed(EventHandler<ActionEvent> event) {
        newMenuItem.setOnAction(event);
    }

    public void setOnLoadButtonPressed(EventHandler<ActionEvent> event) {
        openMenuItem.setOnAction(event);
    }

    public void setOnSaveButtonPressed(EventHandler<ActionEvent> event) {
        saveMenuItem.setOnAction(event);
    }

    public void setOnSaveAsButtonPressed(EventHandler<ActionEvent> event) {
        saveAsMenuItem.setOnAction(event);
    }

    public void setOnExportButtonPressed(EventHandler<ActionEvent> event) {
        exportMenuItem.setOnAction(event);
    }

    public void setOnCloseButtonPressed(EventHandler<ActionEvent> event) {
        closeMenuItem.setOnAction(event);
    }

    public BooleanProperty diagramEditorSelectProperty() {
        return diagramEditorButton.selectedProperty();
    }

    public BooleanProperty deviceConfigSelectProperty() {
        return deviceConfigButton.selectedProperty();
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    private void initUploadButton() {
        uploadStartImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/upload-start.png")));
        uploadStartImageView.setFitWidth(20);
        uploadStartImageView.setFitHeight(20);

        uploadStopImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/upload-stop.png")));
        uploadStopImageView.setFitWidth(20);
        uploadStopImageView.setFitHeight(20);

        uploadButton.setText("Upload");
        uploadButton.setGraphic(uploadStartImageView);
        uploadStatusButton.setVisible(false);

        uploadStatusButton.managedProperty().bind(uploadStatusButton.visibleProperty());
        separator.visibleProperty().bind(uploadStatusButton.visibleProperty());
        separator.managedProperty().bind(separator.visibleProperty());

        uploadButton.setOnAction(event -> {
            if (uploadTask == null || !uploadTask.isRunning()) {
                if (createUploadTask()) {
                    uploadButton.setText("Cancel");
                    uploadButton.setGraphic(uploadStopImageView);
                    uploadStatusButton.setVisible(true);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel upload?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK) {
                    return;
                }
                uploadTask.cancel();
                uploadButton.setText("Upload");
                uploadButton.setGraphic(uploadStartImageView);
                uploadStatusButton.setVisible(false);
            }
        });

        uploadStatusButton.setOnAction(event -> {
            UploadDialogView uploadDialogView = new UploadDialogView(getScene().getWindow(), uploadTask);
            uploadDialogView.progressProperty().bind(uploadTask.progressProperty());
            uploadDialogView.descriptionProperty().bind(uploadTask.messageProperty());
            uploadDialogView.logProperty().bind(logProperty);
            uploadDialogView.show();
        });
    }

    private boolean createUploadTask() {
        StringBuilder log = new StringBuilder();
        logProperty = new SimpleStringProperty();

        // we MUST deep copy the project because user can modified the project in the UI thread while the upload thread
        // access the project
        switch (project.get().getPlatform()) {
            case ARDUINO_AVR8:
            case ARDUINO_ESP32:
            case ARDUINO_ESP8266:
                uploadTask = new ArduinoUploadTask(Project.newInstance(project.get()));
                break;
            case RASPBERRYPI:
                String initialIpValue = "192.168.1.100";
                TextInputDialog textInputDialog = new TextInputDialog(initialIpValue);
                textInputDialog.setTitle("Connect Raspberry Pi");
                textInputDialog.setHeaderText("Connect Raspberry Pi in Network: ");
                textInputDialog.setContentText("IP Address:");
                Optional<String> ip = textInputDialog.showAndWait();
                if (ip.isPresent()) {
                    uploadTask = new RaspberryPiUploadTask(Project.newInstance(project.get()), ip.get());
                } else {
                    return false;
                }
                break;
            default:
                throw new IllegalStateException("No upload method for current platform");
        }
        uploadTask.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (Double.compare(newValue.doubleValue(), 1.0) == 0) {
                uploadStatusButton.setText("Upload done");
            } else {
                uploadStatusButton.setText("Uploading (" + (newValue.doubleValue() * 100.0) + "%)");
            }
        });
        uploadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event1 -> {
            if (uploadTask.getValue() == UploadResult.OK) {
                uploadStatusButton.setText("Upload done");
            } else {
                uploadStatusButton.setText("Upload failed");
            }
            uploadButton.setText("Upload");
            uploadButton.setGraphic(uploadStartImageView);
        });
        uploadTask.logProperty().addListener((observable, oldValue, newValue) -> {
            log.append(newValue);
            logProperty.set(log.toString());
        });

        new Thread(uploadTask).start();
        return true;
    }

    private void deviceMonitorMenuShowing(Event e) {
        MenuButton deviceMonitorButton = (MenuButton) e.getSource();
        deviceMonitorButton.getItems().clear();
        SerialPort[] commPorts = SerialPort.getCommPorts();
        if (commPorts.length > 0) {
            for (SerialPort port : commPorts) {
                MenuItem item = new MenuItem(port.getDescriptivePortName());
                // runLater to make sure that the menuitem is disappeared before open the DeviceMonitor
                item.setOnAction(event -> Platform.runLater(() -> openDeviceMonitor(port.getSystemPortName())));
                deviceMonitorButton.getItems().add(item);
            }
        } else {
            MenuItem item = new MenuItem("No connected serial port found.\nPlease connect the board with computer.");
            item.setDisable(true);
            deviceMonitorButton.getItems().add(item);
        }
    }

    private void openDeviceMonitor(String portName){
        SerialPort port = SerialPort.getCommPort(portName);
        //TODO: capture error in rare case the port is disconnected
        DeviceMonitor deviceMonitor = new DeviceMonitor(port);
        deviceMonitor.showAndWait();
    }
}
