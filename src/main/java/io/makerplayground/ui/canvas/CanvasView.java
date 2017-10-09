package io.makerplayground.ui.canvas;

import io.makerplayground.project.NodeElement;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.io.IOException;

/**
 *
 */
public class CanvasView extends AnchorPane {
    @FXML private AnchorPane anchorPane;
    @FXML private Pane canvasPane;
    @FXML private ScrollPane scrollPane;
    @FXML private Button addStateBtn;
    @FXML private Button addConditionBtn;

    private final CanvasViewModel canvasViewModel;
    private final BeginSceneView beginSceneView;

    private final SelectionGroup selectionGroup;

    private Line guideLine;

    private NodeElement source; // TODO: leak model into view
    private NodeElement dest;   // TODO: leak model into view

    private boolean flag = false; // false means left, true means right

    public CanvasView(CanvasViewModel canvasViewModel) {
        this.canvasViewModel = canvasViewModel;
        this.selectionGroup = new SelectionGroup();
        setOnMousePressed(event -> {
            selectionGroup.deselect();
        });

        beginSceneView = new BeginSceneView(canvasViewModel.getBeginViewModel());
//        setBottomAnchor(beginSceneView,0.0);
//        setTopAnchor(beginSceneView,0.0);
//        setLeftAnchor(beginSceneView,20.0);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/CanvasView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        addStateBtn.setOnAction((event) -> {
            canvasViewModel.project.addState();
        });
        addConditionBtn.setOnAction((event) -> {
            canvasViewModel.project.addCondition();
        });

        DynamicViewCreator<Pane, SceneViewModel, SceneView> canvasViewCreator =
                new DynamicViewCreator<>(canvasViewModel.getPaneStateViewModel(), canvasPane, sceneViewModel -> {
                    SceneView sceneView = new SceneView(sceneViewModel);
                    addStateConnectionEvent(sceneView);
                    selectionGroup.getSelectable().add(sceneView);
                    sceneView.setOnAction(event -> canvasViewModel.project.removeState(sceneViewModel.getScene()));
                    return sceneView;
                }, new NodeConsumer<Pane, SceneView>() {
                    @Override
                    public void addNode(Pane parent, SceneView node) {
                        parent.getChildren().add(node);
                    }

                    @Override
                    public void removeNode(Pane parent, SceneView node) {
                        parent.getChildren().remove(node);
                    }
                });

        DynamicViewCreator<Pane, ConditionViewModel, ConditionView> conditionViewCreator =
                new DynamicViewCreator<>(canvasViewModel.getConditionViewModel(), canvasPane, conditionViewModel -> {
                    ConditionView conditionView = new ConditionView(conditionViewModel);
                    addConditionConnectionEvent(conditionView);
                    selectionGroup.getSelectable().add(conditionView);
                    conditionView.setOnAction(event -> canvasViewModel.project.removeCondition(conditionViewModel.getCondition()));
                    return conditionView;
                }, new NodeConsumer<Pane, ConditionView>() {
                    @Override
                    public void addNode(Pane parent, ConditionView node) {
                        parent.getChildren().add(node);
                    }

                    @Override
                    public void removeNode(Pane parent, ConditionView node) {
                        parent.getChildren().remove(node);
                    }
                });
        addBeginConnectionEvent(beginSceneView);

        DynamicViewCreator<Pane, LineViewModel, LineView> lineViewCreator =
                new DynamicViewCreator<>(canvasViewModel.getLineViewModel(), canvasPane, viewModel -> {
                    LineView lineView = new LineView(viewModel);
                    lineView.setOnAction(event -> canvasViewModel.project.removeLine(viewModel.getLine()));
                    selectionGroup.getSelectable().add(lineView);
                    return lineView;
                }, new NodeConsumer<Pane, LineView>() {
                    @Override
                    public void addNode(Pane parent, LineView node) {
                        parent.getChildren().add(node);
                    }

                    @Override
                    public void removeNode(Pane parent, LineView node) {
                        parent.getChildren().remove(node);
                    }
                });

        guideLine = new Line();
        guideLine.setVisible(false);
        guideLine.setStrokeWidth(3.25);
        guideLine.setStyle("-fx-stroke: #313644;");
        canvasPane.getChildren().addAll(guideLine,beginSceneView);

        setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.MOVE) {
            }

            guideLine.setVisible(false);

            event.consume();
        });

        setOnDragOver(event -> {
            System.out.println(event.getSceneX() + " " + event.getSceneY());

            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY()-35.0);

            event.consume();
        });
    }

    private void addStateConnectionEvent(SceneView sceneView) {
        sceneView.setOnDesPortDragDetected(event -> {
            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);

            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString("");
            db.setContent(clipboard);

            guideLine.setStartX(event.getSceneX());
            guideLine.setStartY(event.getSceneY()-32.5);
            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());
            guideLine.setVisible(true);

            source = sceneView.getSceneViewModel().getScene();
            flag = true; // right port

            event.consume();
        });
        sceneView.setOnSrcPortDragDetected(event -> {
            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);

            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString("");
            db.setContent(clipboard);

            guideLine.setStartX(event.getSceneX());
            guideLine.setStartY(event.getSceneY()-32.5);
            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());
            guideLine.setVisible(true);

            source = sceneView.getSceneViewModel().getScene();
            flag = false; // left port

            event.consume();
        });
        sceneView.setOnSrcPortDragOver(event -> {
            System.out.println(event.getSceneX() + " " + event.getSceneY());
            if (!flag) {
                return;
            }
            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
                sceneView.setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY()-32.5);
            event.consume();
        });
        sceneView.setOnDesPortDragOver(event -> {
            System.out.println(event.getSceneX() + " " + event.getSceneY());

            if (flag) {
                return;
            }
            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY()-32.5);

            event.consume();
        });
        sceneView.setOnSrcPortDragEntered(event -> {
            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
                // TODO: add visual feedback
            }

            event.consume();
        });
        sceneView.setOnDesPortDragEntered(event -> {
            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
                // TODO: add visual feedback
            }

            event.consume();
        });
        sceneView.setOnSrcPortDragExited(event -> {
            // TODO: remove visual feedback
            sceneView.setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
            event.consume();
        });
        sceneView.setOnDesPortDragExited(event -> {
            // TODO: remove visual feedback

            event.consume();
        });
        sceneView.setOnSrcPortDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                System.out.println("Connect to => " + db.getString());
                success = true;
            }
            canvasViewModel.connectState(source, sceneView.getSceneViewModel().getScene());
            event.setDropCompleted(success);

            event.consume();
        });
        sceneView.setOnDesPortDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                System.out.println("Connect to => " + db.getString());
                success = true;
            }
            canvasViewModel.connectState(sceneView.getSceneViewModel().getScene(), source);
            event.setDropCompleted(success);

            event.consume();
        });
    }

    private void addConditionConnectionEvent(ConditionView conditionView) {
        conditionView.setOnDesPortDragDetected(event -> {
            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);

            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString("");
            db.setContent(clipboard);

            guideLine.setStartX(event.getSceneX());
            guideLine.setStartY(event.getSceneY()-32.5);
            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());
            guideLine.setVisible(true);

            source = conditionView.getSceneViewModel().getCondition();
            flag = true; // right port

            event.consume();
        });

        conditionView.setOnSrcPortDragDetected(event -> {
            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);

            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString("");
            db.setContent(clipboard);

            guideLine.setStartX(event.getSceneX());
            guideLine.setStartY(event.getSceneY()-32.5);
            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());
            guideLine.setVisible(true);

            source = conditionView.getSceneViewModel().getCondition();
            flag = false; // left port

            event.consume();
        });

        conditionView.setOnSrcPortDragOver(event -> {
            System.out.println(event.getSceneX() + " " + event.getSceneY());

            if (flag == false) {
                return;
            }
            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                conditionView.setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
            }

            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY()-32.5);

            event.consume();
        });

        conditionView.setOnDesPortDragOver(event -> {
            System.out.println(event.getSceneX() + " " + event.getSceneY());

            if (flag == true) {
                return;
            }
            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY()-32.5);

            event.consume();
        });

        conditionView.setOnSrcPortDragEntered(event -> {
            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
                // TODO: add visual feedback
            }

            event.consume();
        });

        conditionView.setOnDesPortDragEntered(event -> {
            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
                // TODO: add visual feedback
            }

            event.consume();
        });
        conditionView.setOnSrcPortDragExited(event -> {
            // TODO: remove visual feedback
            conditionView.setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
            event.consume();
        });
        conditionView.setOnDesPortDragExited(event -> {
            // TODO: remove visual feedback

            event.consume();
        });
        conditionView.setOnSrcPortDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                System.out.println("Connect to => " + db.getString());
                success = true;
            }
            canvasViewModel.connectState(source, conditionView.getSceneViewModel().getCondition());
            event.setDropCompleted(success);

            event.consume();
        });

        conditionView.setOnDesPortDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                System.out.println("Connect to => " + db.getString());
                success = true;
            }
            canvasViewModel.connectState(conditionView.getSceneViewModel().getCondition(), source);
            event.setDropCompleted(success);

            event.consume();
        });
    }

    private void addBeginConnectionEvent(BeginSceneView beginSceneView) {
        beginSceneView.setOnDesPortDragDetected(event -> {
            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);

            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString("");
            db.setContent(clipboard);

            guideLine.setStartX(event.getSceneX());
            guideLine.setStartY(event.getSceneY()-32.5);
            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());
            guideLine.setVisible(true);

            source = beginSceneView.getBeginSceneViewModel().getBegin();
            flag = true;

            event.consume();
        });

//        beginSceneView.setOnSrcPortDragOver(event -> {
//            System.out.println(event.getSceneX() + " " + event.getSceneY());
//
//            if (event.getGestureSource() != beginSceneView && event.getDragboard().hasString()) {
//                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//            }
//
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY());
//
//            event.consume();
//        });
//        beginSceneView.setOnSrcPortDragEntered(event -> {
//            if (event.getGestureSource() != beginSceneView && event.getDragboard().hasString()) {
//                // TODO: add visual feedback
//            }
//
//            event.consume();
//        });
//        beginSceneView.setOnSrcPortDragExited(event -> {
//            // TODO: remove visual feedback
//
//            event.consume();
//        });
//        beginSceneView.setOnSrcPortDragDropped(event -> {
//            Dragboard db = event.getDragboard();
//            boolean success = false;
//            if (db.hasString()) {
//                System.out.println("Connect to => " + db.getString());
//                success = true;
//            }
//            canvasViewModel.connectState(source, beginSceneView.getSceneViewModel().getCondition());
//            event.setDropCompleted(success);
//
//            event.consume();
//        });
    }
}
