package io.makerplayground.ui.canvas.node.expressioncontrol;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;

/**
 * Created by USER on 12-Jul-17.
 */
public class SpinnerWithUnit extends HBox {
    private final Spinner<Double> spinner;
    private ComboBox<Unit> comboBox;
    private final ObjectProperty<NumberWithUnit> numberWithUnit;
    private final ObservableList<Constraint> constraintList;

//    public SpinnerWithUnit(double min, double max, double value,) {
//        this(min, max, value, Unit.NOT_SPECIFIED);
//    }

    public SpinnerWithUnit(double min, double max, double value, Unit unit, ObservableList<Unit> unitList) {
        spinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max));
        spinner.setEditable(true);
        spinner.getValueFactory().setValue(value);

        comboBox = new ComboBox<>(unitList);
        comboBox.getSelectionModel().select(unit);
        if (unit == Unit.NOT_SPECIFIED) {
            comboBox.setVisible(false);
            comboBox.setManaged(false);
        }

        setSpacing(2);

        numberWithUnit = new SimpleObjectProperty<>(new NumberWithUnit(value,unit));
        numberWithUnit.addListener((observable, oldValue, newValue) -> {
            spinner.getValueFactory().setValue(newValue.getValue());
            comboBox.getSelectionModel().select(newValue.getUnit());
        });
        constraintList = FXCollections.observableArrayList();

        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                numberWithUnit.set(new NumberWithUnit(oldValue, numberWithUnit.get().getUnit()));
            } else {
                numberWithUnit.set(new NumberWithUnit(newValue, numberWithUnit.get().getUnit()));
            }
        });
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            numberWithUnit.set(new NumberWithUnit(numberWithUnit.get().getValue(), newValue));
        });
        getChildren().addAll(spinner, comboBox);
    }

    public NumberWithUnit getValue() {
        return numberWithUnit.get();
    }

    public ObjectProperty<NumberWithUnit> valueProperty() {
        return numberWithUnit;
    }

    public void setValue(NumberWithUnit numberWithUnit) {
        this.numberWithUnit.set(numberWithUnit);
    }

    public void setUnit(ObservableList<Unit> unitList) {
        comboBox.setItems(unitList);
    }

    public ObservableList<Constraint> getSpinnerConstraint() {
        return constraintList;
    }

    public static class Constraint {
        private final double min;
        private final double max;
        private final Unit unit;

        public Constraint(double min, double max, Unit unit) {
            this.min = min;
            this.max = max;
            this.unit = unit;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public Unit getUnit() {
            return unit;
        }
    }
}