package io.makerplayground.ui;

import io.makerplayground.generator.Sourcecode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

public class ErrorDialogView extends Dialog{
    @FXML
    private Label descriptionLabel;
    public ErrorDialogView(String error) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ErrorDialogView.fxml"));
        fxmlLoader.setRoot(this.getDialogPane());
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setTitle("  Error");
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        descriptionLabel.setText(error);
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
    }
}