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

package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.term.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ChipField extends VBox {
    @FXML private ScrollPane scrollPane;
    @FXML private HBox mainPane;
    @FXML private Rectangle cursor;

    @FXML private GridPane chipSelectorPane;
    @FXML private StackPane numberChip;
    @FXML private StackPane valueChip;
    @FXML private StackPane plusChip;
    @FXML private StackPane multiplyChip;
    @FXML private StackPane minusChip;
    @FXML private StackPane divideChip;
    @FXML private StackPane openParenthesisChip;
    @FXML private StackPane closeParenthesisChip;

    private final List<ProjectValue> projectValues;
    private final ReadOnlyObjectWrapper<CustomNumberExpression> expressionProperty;

    private final BooleanProperty chipFieldFocus = new SimpleBooleanProperty();

    private static final Insets CHIP_FIT_INSETS = new Insets(0, 0, 0, -10);
    private static final int EXPAND_BUFFER = 15;

    public ChipField(CustomNumberExpression expression, List<ProjectValue> projectValues) {
        this.projectValues = projectValues;
        this.expressionProperty = new ReadOnlyObjectWrapper<>(expression);
        initView();
        initEvent();
    }

    private void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/ChipField.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // initialize chip based on expression
        List<Term> listTerm = expressionProperty.get().getTerms();
        for (int i=0; i<listTerm.size(); i++) {
            addChipUI(listTerm.get(i), i);
        }

        // hide the cursor until the ChipField or it's children (chip in the ChipField) received focus
        cursor.setVisible(false);
        cursor.setManaged(false);

        // hide the chipSelectorPane until the ChipField or it's children (chip in the ChipField) received focus
        chipSelectorPane.setVisible(false);
        chipSelectorPane.setManaged(false);

        updateViewLayout();
        updateHilight();
    }

    private void initEvent() {
        scrollPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                int currentCursorPosition = mainPane.getChildren().indexOf(cursor);
                if (currentCursorPosition > 0) {
                    Node removeNode = mainPane.getChildren().get(currentCursorPosition - 1);
                    if (removeNode instanceof Text) {
                        mainPane.getChildren().remove(removeNode);
                    } else {
                        removeChip((Chip) removeNode);
                    }
                }
            } else if (event.getCode() == KeyCode.DELETE) {
                int currentCursorPosition = mainPane.getChildren().indexOf(cursor);
                if (currentCursorPosition < mainPane.getChildren().size() - 1) {
                    Node removeNode = mainPane.getChildren().get(currentCursorPosition + 1);
                    if (removeNode instanceof Text) {
                        mainPane.getChildren().remove(removeNode);
                    } else {
                        removeChip((Chip) removeNode);
                    }
                }
            } else if (event.getCode() == KeyCode.LEFT) {
                int currentCursorPosition = mainPane.getChildren().indexOf(cursor);
                if (currentCursorPosition > 0) {
                    mainPane.getChildren().remove(cursor);
                    mainPane.getChildren().add(currentCursorPosition - 1, cursor);
                    updateViewLayout();
                    repositionScrollpane(cursor);
                }
            } else if (event.getCode() == KeyCode.RIGHT) {
                int currentCursorPosition = mainPane.getChildren().indexOf(cursor);
                if (currentCursorPosition < mainPane.getChildren().size() - 1) {
                    mainPane.getChildren().remove(cursor);
                    mainPane.getChildren().add(currentCursorPosition + 1, cursor);
                    updateViewLayout();
                    repositionScrollpane(cursor);
                }
            } else if (event.getCode() == KeyCode.ENTER)  {
                transformTextToChip();
                updateViewLayout();
                updateExpression();
            } else {
                mainPane.getChildren().add(mainPane.getChildren().indexOf(cursor), new Text(event.getText()));
                updateViewLayout();
            }
        });

        scrollPane.addEventHandler(KeyEvent.ANY, Event::consume);

        // add chip when the icons in the chipSelectorPane is pressed
        numberChip.setOnMousePressed(event -> addChip(new NumberWithUnitTerm(NumberWithUnit.ZERO)));
        valueChip.setOnMousePressed(event -> addChip(new ValueTerm(null)));
        plusChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.PLUS)));
        multiplyChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.MULTIPLY)));
        minusChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.MINUS)));
        divideChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.DIVIDE)));
        openParenthesisChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.OPEN_PARENTHESIS)));
        closeParenthesisChip.setOnMousePressed(event -> addChip(new OperatorTerm(Operator.CLOSE_PARENTHESIS)));

        // update chipFieldFocus property by comparing scene's focus owner node to the ChipField(scrollPane) and it's
        // children because for unknown reason the focused property of the node itself doesn't update after we click
        // at the other node
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.focusOwnerProperty().addListener((observable1, oldFocusOwner, newFocusOwner) -> {
                    Queue<Node> queue = new ArrayDeque<>();
                    queue.add(scrollPane);
                    while (!queue.isEmpty()) {
                        Node node = queue.remove();
                        if (node == newFocusOwner) {
                            chipFieldFocus.set(true);
                            return;
                        } else if (node instanceof Parent) {
                            queue.addAll(((Parent) node).getChildrenUnmodifiable());
                        }
                    }
                    chipFieldFocus.set(false);
                });
            }
        });

        // blink the cursor when the ChipField or it's children (chip in the ChipField) received focus
        BooleanProperty cursorVisible = new SimpleBooleanProperty();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), evt -> cursorVisible.set(false)),
                new KeyFrame(Duration.seconds( 1), evt -> cursorVisible.set(true)));
        timeline.setCycleCount(Animation.INDEFINITE);
        chipFieldFocus.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timeline.play();
            } else {
                timeline.stop();
            }
            transformTextToChip();
            updateViewLayout();
            updateExpression();
            updateChipfieldWidth();
        });

        // automatically expand the chipfield based on its content
        mainPane.layoutBoundsProperty().addListener(observable -> Platform.runLater(this::updateChipfieldWidth));

        cursor.visibleProperty().bind(chipFieldFocus.and(cursorVisible));
        cursor.managedProperty().bind(chipFieldFocus);

        chipSelectorPane.visibleProperty().bind(chipFieldFocus);
        chipSelectorPane.managedProperty().bind(chipFieldFocus);
    }

    private void updateChipfieldWidth() {
        double contentWidth = mainPane.getLayoutBounds().getWidth();
        double chipSelectorWidth = chipSelectorPane.getLayoutBounds().getWidth();

        double newWidth;
        if (chipFieldFocus.get() && contentWidth < chipSelectorWidth) {
            newWidth = chipSelectorWidth;
        } else {
            if (contentWidth > 250) {
                newWidth = 250;
            } else if (contentWidth > 75) {
                newWidth = contentWidth + EXPAND_BUFFER;
            } else {
                newWidth = 75;
            }
        }

        scrollPane.setMinWidth(newWidth);
        scrollPane.setPrefWidth(newWidth);
        scrollPane.setMaxWidth(newWidth);
    }

    private void repositionScrollpane(Node node) {
        // ensure that the ChipField has been redrawn completely otherwise bounds receive from various method call below
        // will be the old bound and the calculation will be wrong
        getParent().layout();

        double chipMinX = node.getBoundsInParent().getMinX();
        double chipMaxX = node.getBoundsInParent().getMaxX();
        double contentWidth = mainPane.getLayoutBounds().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double extraWidth = contentWidth - viewportWidth;
        if (extraWidth > 0) {
            if (chipMinX < scrollPane.getHvalue() * extraWidth) {
                scrollPane.setHvalue(chipMinX / extraWidth);
            } else if (chipMaxX > scrollPane.getHvalue() * extraWidth + viewportWidth) {
                scrollPane.setHvalue((chipMaxX - viewportWidth) / extraWidth);
            }
        } else {
            scrollPane.setHvalue(0);
        }
    }

    private void addChip(Term t) {
        Chip c = addChipUI(t, mainPane.getChildren().indexOf(cursor));
        updateExpression();
        repositionScrollpane(c);
    }

    private Chip addChipUI(Term t, int index) {
        Chip chip;
        if (t instanceof NumberWithUnitTerm) {
            chip = new NumberWithUnitChip(((NumberWithUnitTerm) t).getValue());
            ((NumberWithUnitChip) chip).valueProperty().addListener((observable, oldValue, newValue) -> updateExpression());
        } else if (t instanceof OperatorTerm) {
            chip = new OperatorChip(((OperatorTerm) t).getValue());
            ((OperatorChip) chip).valueProperty().addListener((observable, oldValue, newValue) -> updateExpression());
        } else if (t instanceof ValueTerm) {
            chip = new ProjectValueChip(((ValueTerm) t).getValue(), projectValues);
            ((ProjectValueChip) chip).valueProperty().addListener((observable, oldValue, newValue) -> updateExpression());
        } else {
            throw new IllegalStateException();
        }

        chip.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            mainPane.getChildren().remove(cursor);
            int chipPosition = mainPane.getChildren().indexOf(chip);
            mainPane.getChildren().add(chipPosition+1, cursor);
            transformTextToChip();
            updateViewLayout();
            repositionScrollpane(chip);
        });

        mainPane.getChildren().add(index, chip);

        return chip;
    }

    private void removeChip(Chip chip) {
        mainPane.getChildren().remove(chip);
        updateExpression();
    }

    private void updateExpression() {
        if (mainPane.getChildren().stream().noneMatch(node -> node instanceof Text)) {
            expressionProperty.set(new CustomNumberExpression(mainPane.getChildren().stream()
                    .filter(node -> node instanceof Chip).map(node -> ((Chip) node).getTerm()).collect(Collectors.toList())));
        } else {
            expressionProperty.set(CustomNumberExpression.INVALID);
        }
        transformTextToChip();
        updateViewLayout();
        updateHilight();
    }

    private void transformTextToChip() {
        List<Node> nodes = mainPane.getChildren();
        Set<String> operator = Operator.getArithmeticOperator().stream().map(Operator::getDisplayString).collect(Collectors.toSet());

        int i = nodes.size()-1;
        while (i >= 0) {
            if (nodes.get(i) instanceof Text) {
                int endIndex = i;
                // decrement i until we found any kind of chip or a operator
                while ((i >= 0) && (nodes.get(i) instanceof Text) && !operator.contains(((Text) nodes.get(i)).getText())) {
                    i--;
                }

                // process block of texts from position i+1 to endIndex (inclusive) and replace them with a chip
                List<Node> textNode = nodes.subList(i+1, endIndex+1);
                if (!textNode.isEmpty()) {
                    String text = textNode.stream().map(t -> ((Text) t).getText()).collect(Collectors.joining()).trim();
                    Term t = null;
                    if (text.matches("-?\\d+\\.?\\d*")) {
                        t = new NumberWithUnitTerm(NumberWithUnit.of(Double.parseDouble(text), Unit.NOT_SPECIFIED));
                    } else if (text.contains("'s ")) {
                        for (ProjectValue projectValue : projectValues) {
                            String s = projectValue.getDevice().getName() + "'s " + projectValue.getValue().getName();
                            if (text.equals(s)) {
                                t = new ValueTerm(projectValue);
                                break;
                            }
                        }
                    }
                    textNode.clear();
                    if (t != null) {
                        addChipUI(t, i+1);
                    }
                }

                // process text at position i (in case that the first loop terminate because of an operator)
                if ((i >= 0) && nodes.get(i) instanceof Text) {
                    String operatorString = ((Text) nodes.get(i)).getText();
                    OperatorTerm operatorTerm = new OperatorTerm(Operator.fromDisplayString(operatorString));
                    nodes.remove(i);
                    addChipUI(operatorTerm, i);
                }
            }
            i--;
        }
    }

    private void updateViewLayout() {
        List<Node> nodes = mainPane.getChildren();
        if (nodes.isEmpty()) {
            return;
        }

        HBox.setMargin(nodes.get(0), Insets.EMPTY);
        for (int i=1; i<nodes.size(); i++) {
            Node previousNode = nodes.get(i - 1);
            Node currentNode = nodes.get(i);
            if (previousNode == cursor) {
                HBox.setMargin(currentNode, Insets.EMPTY);
            } else if (currentNode == cursor) {
                // do nothing
            } else if (previousNode instanceof Text || currentNode instanceof Text) {
                HBox.setMargin(currentNode, Insets.EMPTY);
            } else if ((previousNode instanceof NumberWithUnitChip || previousNode instanceof ProjectValueChip
                    || isOperatorChip(previousNode, OperatorType.RIGHT_UNARY))
                    && (currentNode instanceof NumberWithUnitChip || currentNode instanceof ProjectValueChip
                    || isOperatorChip(currentNode, OperatorType.LEFT_UNARY))) {
                HBox.setMargin(currentNode, Insets.EMPTY);
            } else if (isOperatorChip(previousNode, OperatorType.BINARY)
                    && isOperatorChip(currentNode, OperatorType.BINARY)) {
                HBox.setMargin(currentNode, Insets.EMPTY);
            } else if (isOperatorChip(previousNode, OperatorType.LEFT_UNARY)
                    && isOperatorChip(currentNode, OperatorType.RIGHT_UNARY)) {
                HBox.setMargin(currentNode, Insets.EMPTY);
            } else {
                HBox.setMargin(currentNode, CHIP_FIT_INSETS);
            }
        }
    }

    private boolean isOperatorChip(Node node, OperatorType operator) {
        return (node instanceof OperatorChip) && (((OperatorChip) node).getTerm().getValue().getType() == operator);
    }

    private void updateHilight() {
        if (!expressionProperty.get().isValid()) {
            scrollPane.setStyle("-fx-effect: dropshadow(gaussian, #ff0000, 5.0 , 0.5, 0.0 , 0.0);");
        } else {
            scrollPane.setStyle("-fx-effect: null;");
        }
    }

    public CustomNumberExpression getExpression() {
        return expressionProperty.get();
    }

    public ReadOnlyObjectProperty<CustomNumberExpression> expressionProperty() {
        return expressionProperty.getReadOnlyProperty();
    }
}
