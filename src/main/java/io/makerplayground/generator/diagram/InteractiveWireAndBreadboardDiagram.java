package io.makerplayground.generator.diagram;

import io.makerplayground.device.actual.IntegratedActualDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.canvas.node.usersetting.InteractiveDevicePropertyWindow;
import io.makerplayground.ui.explorer.InteractiveModel;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import org.controlsfx.control.PopOver;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InteractiveWireAndBreadboardDiagram extends WireAndBreadboardDiagram {

    private static final int BUTTON_OFFSET = 10;
    private static final int BUTTON_SIZE = 20;

    private final Project project;
    private final InteractiveModel interactiveModel;

    private InteractiveDevicePropertyWindow devicePropertyWindow;

    public InteractiveWireAndBreadboardDiagram(Project project, InteractiveModel interactiveModel) {
        super(project);
        this.project = project;
        this.interactiveModel = interactiveModel;
        initInteractiveControl(controllerPosition, deviceTopLeftPos);
    }

    private double getDeviceWidth(ProjectDevice projectDevice) {
        return projectDevice.getActualDevice().getWidth();
    }

    private double getDeviceHeight(ProjectDevice projectDevice) {
        return projectDevice.getActualDevice().getHeight();
    }

    private void initInteractiveControl(Point2D controllerPosition, Map<ProjectDevice, Point2D> devicePosition) {
        // draw configuration button for the controller
        List<ProjectDevice> integratedDevice = project.getDevice().stream()
                .filter(device -> device.getActualDevice() instanceof IntegratedActualDevice)
                .collect(Collectors.toUnmodifiableList());
        if (!integratedDevice.isEmpty()) {
            Point2D buttonPos = controllerPosition.add(project.getController().getWidth() + BUTTON_OFFSET, -BUTTON_OFFSET-BUTTON_SIZE);

            Button button = new Button("+");
            button.disableProperty().bind(interactiveModel.initializeProperty().not());
            button.setOnAction(event -> showDevicePropertyWindow(button, integratedDevice));
            button.setLayoutX(buttonPos.getX());
            button.setLayoutY(buttonPos.getY());
            getChildren().add(button);
        }

        // draw configuration button for each device
        for (ProjectDevice projectDevice : devicePosition.keySet()) {
            Point2D topLeftPos = devicePosition.get(projectDevice);
            Point2D buttonPos = topLeftPos.add(getDeviceWidth(projectDevice) + BUTTON_OFFSET, -BUTTON_OFFSET-BUTTON_SIZE);

            Button button = new Button("+");
            button.disableProperty().bind(interactiveModel.initializeProperty().not());
            button.setOnAction(event -> showDevicePropertyWindow(button, Collections.singletonList(projectDevice)));
            button.setLayoutX(buttonPos.getX());
            button.setLayoutY(buttonPos.getY());
            getChildren().add(button);
        }
    }

    private void showDevicePropertyWindow(Button button, List<ProjectDevice> deviceList) {
        if (devicePropertyWindow == null) {
            devicePropertyWindow = new InteractiveDevicePropertyWindow(deviceList, interactiveModel, project);
            devicePropertyWindow.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
            devicePropertyWindow.setOnSendActionButtonPressed(interactiveModel::sendCommand);
            devicePropertyWindow.setOnHidden(event1 -> devicePropertyWindow = null);
            devicePropertyWindow.show(button);
        }
    }
}
