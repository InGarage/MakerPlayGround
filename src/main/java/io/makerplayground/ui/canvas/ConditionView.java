package io.makerplayground.ui.canvas;

import io.makerplayground.ui.canvas.event.InteractiveNodeEvent;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewCreatorBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;

/**
 * Created by USER on 05-Jul-17.
 */
public class ConditionView extends InteractiveNode {
    private VBox root = new VBox();
    @FXML private Circle inPort;
    @FXML private Circle outPort;
    @FXML private HBox deviceIconHBox;
    @FXML private Button removeConditionBtn;
    @FXML private Button addInputButton;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane stackPane;

    private final ConditionViewModel conditionViewModel;
    private InputDeviceSelector inputDeviceSelector = null;

    public ConditionView(ConditionViewModel conditionViewModel, InteractivePane interactivePane) {
        super(interactivePane);
        this.conditionViewModel = conditionViewModel;
        initView();
        initEvent();
    }

    private void initView() {
        // initialize view from FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ConditionView2.fxml"));
        fxmlLoader.setRoot(root);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().add(root);

        // dynamically create device configuration icons
        DynamicViewCreator<HBox, SceneDeviceIconViewModel, ConditionDeviceIconView> dynamicViewCreator =
                new DynamicViewCreatorBuilder<HBox, SceneDeviceIconViewModel, ConditionDeviceIconView>()
                        .setParent(deviceIconHBox)
                        .setModelLoader(conditionViewModel.getDynamicViewModelCreator())
                        .setViewFactory(conditionDeviceIconViewModel -> {
                            ConditionDeviceIconView conditionDeviceIconView = new ConditionDeviceIconView(conditionDeviceIconViewModel);
                            conditionDeviceIconView.setOnRemove(event -> conditionViewModel.removeConditionDevice(conditionDeviceIconViewModel.getProjectDevice()));
                            return conditionDeviceIconView;
                        })
                        .setNodeAdder((parent, node) -> parent.getChildren().add(parent.getChildren().size() - 1, node))
                        .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                        .createDynamicViewCreator();

        // bind condition's location to the model
        translateXProperty().bindBidirectional(conditionViewModel.xProperty());
        translateYProperty().bindBidirectional(conditionViewModel.yProperty());

        // show add output device button when there are devices left to be added
        addInputButton.visibleProperty().bind(conditionViewModel.hasDeviceToAddProperty());

        // show remove button when select
        removeConditionBtn.visibleProperty().bind(selectedProperty());
    }

    private void initEvent() {
        // allow the condition to move by dragging the scrollPane
        makeMovable(scrollPane);

        // show device selector dialog to add device to this condition
        addInputButton.setOnAction(e -> {
            if (inputDeviceSelector != null) {
                inputDeviceSelector.hide();
            }
            InputDeviceSelector inputDeviceSel = new InputDeviceSelector(conditionViewModel);
            inputDeviceSel.show(addInputButton,0);
            inputDeviceSelector = inputDeviceSel;
        });

        // remove condition when press the remove button
        removeConditionBtn.setOnAction(event -> fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.REMOVED
                , null, null, 0, 0)));

        // TODO: refactor into InteractiveNode
        // allow node to connect with other node
        outPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , conditionViewModel.getCondition(), null
                    , getBoundsInParent().getMinX() + (outPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                    + (outPort.getBoundsInLocal().getWidth() / 2)
                    , getBoundsInParent().getMinY() + (stackPane.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                    + outPort.getBoundsInParent().getMinY() + (outPort.getBoundsInLocal().getHeight() / 2)));
        });
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null && !conditionViewModel.hasConnectionFrom(interactivePane.getSourceNode())) {
                showHighlight(true);
            }
        });
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHighlight(false));
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null) {
                showHighlight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , interactivePane.getSourceNode(), conditionViewModel.getCondition()
                        , getBoundsInParent().getMinX() + (inPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                        + (inPort.getBoundsInLocal().getWidth() / 2)
                        , getBoundsInParent().getMinY() + (stackPane.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                        + inPort.getBoundsInParent().getMinY() + (inPort.getBoundsInLocal().getHeight() / 2)));
            }
        });

        inPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , null, conditionViewModel.getCondition()
                    , getBoundsInParent().getMinX() + (inPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                    + (inPort.getBoundsInLocal().getWidth() / 2)
                    , getBoundsInParent().getMinY() + (stackPane.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                    + inPort.getBoundsInParent().getMinY() + (inPort.getBoundsInLocal().getHeight() / 2)));
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null && !conditionViewModel.hasConnectionTo(interactivePane.getDestNode())) {
                showHighlight(true);
            }
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHighlight(false));
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null) {
                showHighlight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , conditionViewModel.getCondition(), interactivePane.getDestNode()
                        , getBoundsInParent().getMinX() + (outPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                        + (outPort.getBoundsInLocal().getWidth() / 2)
                        , getBoundsInParent().getMinY() + (stackPane.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                        + outPort.getBoundsInParent().getMinY() + (outPort.getBoundsInLocal().getHeight() / 2)));
            }
        });
    }

    public ConditionViewModel getConditionViewModel() {
        return conditionViewModel;
    }
}
