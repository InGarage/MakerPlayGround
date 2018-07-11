package io.makerplayground.ui.dialog.devicepane;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/7/2017 AD.
 */
public class DeviceSelectorView extends Dialog<Map<GenericDevice, Integer>> {
    @FXML private DialogPane deviceSelectorPane;
    //@FXML private FlowPane mcuPane;
    @FXML private FlowPane outputPane;
    @FXML private FlowPane inputPane;
    @FXML private FlowPane connectivityPane;

    private ObservableList<ControlAddDevicePane> outputDevice;
    private ObservableList<ControlAddDevicePane> inputDevice;
    private ObservableList<ControlAddDevicePane> connectivityDevice;

    public DeviceSelectorView() {
        this.inputDevice = FXCollections.observableArrayList();
        this.outputDevice = FXCollections.observableArrayList();
        this.connectivityDevice = FXCollections.observableArrayList();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/devicepane/DeviceSelectorView.fxml"));
        fxmlLoader.setRoot(this.getDialogPane());
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        initView();
    }

    private void initView() {
        setTitle(" Device Library");
        getDialogPane().setExpanded(true);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);

        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericOutputDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            outputPane.getChildren().add(controlDevicePane);
            this.outputDevice.add(controlDevicePane);
        }
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericInputDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            inputPane.getChildren().add(controlDevicePane);
            this.inputDevice.add(controlDevicePane);
        }
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericConnectivityDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            connectivityPane.getChildren().add(controlDevicePane);
            this.connectivityDevice.add(controlDevicePane);
        }
        ButtonType importButtonType = new ButtonType("Import", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(importButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == importButtonType) {
                Map<GenericDevice, Integer> deviceToBeAdded = new HashMap<>();
                for (ControlAddDevicePane d : outputDevice) {
                    deviceToBeAdded.put(d.getGenericDevice(), d.getCount());
                }
                for (ControlAddDevicePane d : inputDevice) {
                    deviceToBeAdded.put(d.getGenericDevice(), d.getCount());
                }
                for (ControlAddDevicePane d : connectivityDevice) {
                    deviceToBeAdded.put(d.getGenericDevice(), d.getCount());
                }
                return deviceToBeAdded;
            }
            return null;
        });
    }
}