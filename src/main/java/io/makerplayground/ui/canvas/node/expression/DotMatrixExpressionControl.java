/*
 * Copyright (c) 2020. The Maker Playground Authors.
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

package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.DotMatrix;
import io.makerplayground.project.expression.DotMatrixExpression;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DotMatrixExpressionControl extends VBox {

    private ReadOnlyObjectWrapper<DotMatrixExpression> expression;

    BooleanProperty mouseDragToHighlight = new SimpleBooleanProperty();

    public DotMatrixExpressionControl(DotMatrixExpression expression) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        initControl();
    }

    private void initControl() {
        this.setSpacing(3);
        this.getChildren().clear();

        GridPane gridPaneDot = new GridPane();
        DotMatrix dotMatrix = expression.get().getDotMatrix();
        for (int i=0; i<dotMatrix.getRow(); i++) {
            for (int j=0; j<dotMatrix.getColumn(); j++) {
                Rectangle rectangle = new Rectangle(12, 12);
                rectangle.setStyle("-fx-stroke: #cccccc; -fx-stroke-width: 1;");
                rectangle.fillProperty().bind(Bindings.when(dotMatrix.getData()[i][j]).then(Color.rgb(255, 0, 0)).otherwise(Color.WHITE));
                int finalI = i;
                int finalJ = j;
                rectangle.setOnMousePressed(event -> {
                    dotMatrix.toggle(finalI, finalJ);
                    mouseDragToHighlight.set(dotMatrix.getData()[finalI][finalJ].get());
                });
                rectangle.setOnMouseReleased(event -> getScene().setCursor(Cursor.DEFAULT));
                rectangle.setOnDragDetected(event -> {
                    startFullDrag();
                    if (mouseDragToHighlight.get()) {
                        getScene().setCursor(Cursor.HAND);
                    } else {
                        getScene().setCursor(Cursor.CLOSED_HAND);
                    }
                });
                rectangle.setOnMouseDragEntered(event -> dotMatrix.set(finalI, finalJ, mouseDragToHighlight.get()));
                rectangle.setOnMouseDragReleased(event -> {
                    getScene().setCursor(Cursor.DEFAULT);
                });
                gridPaneDot.add(rectangle, j, i);
            }
        }

        this.getChildren().add(gridPaneDot);

        GridPane gridPaneControl = new GridPane();
        gridPaneControl.setHgap(5);
        gridPaneControl.setVgap(0);

        gridPaneControl.add(new Label("Column"), 0, 0);

        ImageView addColumnButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/add-expression.png")));
        addColumnButton.setFitHeight(25);
        addColumnButton.setFitWidth(25);
        addColumnButton.setPreserveRatio(true);
        addColumnButton.setOnMouseClicked(event -> {
            dotMatrix.resize(dotMatrix.getRow(), dotMatrix.getColumn() + 8);
            initControl();
        });
        gridPaneControl.add(addColumnButton, 2, 0);

        if (dotMatrix.getColumn() > 8) {
            ImageView removeColumnButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/remove-expression.png")));
            removeColumnButton.setFitHeight(25);
            removeColumnButton.setFitWidth(25);
            removeColumnButton.setPreserveRatio(true);
            removeColumnButton.setOnMouseClicked(event -> {
                dotMatrix.resize(dotMatrix.getRow(), dotMatrix.getColumn() - 8);
                initControl();
            });
            gridPaneControl.add(removeColumnButton, 3, 0);
        }

        gridPaneControl.add(new Label("Row"), 0, 1);

        ImageView addRowButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/add-expression.png")));
        addRowButton.setFitHeight(25);
        addRowButton.setFitWidth(25);
        addRowButton.setPreserveRatio(true);
        addRowButton.setOnMouseClicked(event -> {
            dotMatrix.resize(dotMatrix.getRow() + 8, dotMatrix.getColumn());
            initControl();
        });
        gridPaneControl.add(addRowButton, 2, 1);

        if (dotMatrix.getRow() > 8) {
            ImageView removeRowButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/remove-expression.png")));
            removeRowButton.setFitHeight(25);
            removeRowButton.setFitWidth(25);
            removeRowButton.setPreserveRatio(true);
            removeRowButton.setOnMouseClicked(event -> {
                dotMatrix.resize(dotMatrix.getRow() - 8, dotMatrix.getColumn());
                initControl();
            });
            gridPaneControl.add(removeRowButton, 3, 1);
        }

        this.getChildren().add(gridPaneControl);
    }

    public ReadOnlyObjectWrapper<DotMatrixExpression> expressionProperty() {
        return expression;
    }
}