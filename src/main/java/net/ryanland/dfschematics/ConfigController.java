package net.ryanland.dfschematics;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.ryanland.dfschematics.df.value.Scope;
import net.ryanland.dfschematics.df.value.Variable;
import net.ryanland.dfschematics.schematic.TrackedBlock;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static net.ryanland.dfschematics.Controller.schematic;

public class ConfigController implements Initializable {

    static Label configLabel;

    @FXML
    private Label label;
    @FXML
    private TextField schemNameField;
    @FXML
    private TextField authorField;
    @FXML
    private CheckBox removeCheckBox;
    @FXML
    private ChoiceBox<Scope> varScopePicker;
    @FXML
    private ListView<TrackedBlock> trackedBlocks;
    @FXML
    private TextField varNameField;
    @FXML
    private TextField blockField;
    @FXML
    private Button addTrackedBlockButton;
    @FXML
    private Spinner<Double> offsetX;
    @FXML
    private Spinner<Double> offsetY;
    @FXML
    private Spinner<Double> offsetZ;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configLabel = label;
        label.setText(Controller.selectedFile.getName());

        schemNameField.setText(schematic.getName());
        schemNameField.textProperty().addListener((l, oldName, newName) -> schematic.setName(newName));
        authorField.setText(schematic.getAuthor());
        authorField.textProperty().addListener((l, oldAuthor, newAuthor) -> schematic.setAuthor(newAuthor));

        trackedBlocks.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(TrackedBlock item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("%s [%s] (%s) - %s found%s".formatted(
                        item.getVariable().getName(), item.getVariable().getScope().name(),
                        item.getMaterial(), item.getOccurrences(),
                        item.isRemoved() ? " & removed" : ""));
                    setGraphic(item.getIcon());
                }
                setOnContextMenuRequested(event -> {
                    view.getItems().remove(item);
                    usedVariables.remove(item.getVariable());
                    schematic.getTrackedBlocks().remove(item);
                    schematic.read();
                });
            }
        });
        trackedBlocks.setItems(FXCollections.observableArrayList(schematic.getTrackedBlocks().getBlocks()));
        usedVariables = new ArrayList<>();

        varNameField.textProperty().addListener((l, oldName, newName) -> addTrackedBlockButton.setDisable(newName.isBlank() || blockField.getText().isBlank()));
        blockField.textProperty().addListener((l, oldBlock, newBlock) -> addTrackedBlockButton.setDisable(newBlock.isBlank() || varNameField.getText().isBlank()));
        varScopePicker.setItems(FXCollections.observableArrayList(Scope.LINE, Scope.LOCAL, Scope.GAME, Scope.SAVED));
        varScopePicker.setValue(Scope.LOCAL);

        offsetX.setValueFactory(getFactory(0));
        offsetY.setValueFactory(getFactory(1));
        offsetZ.setValueFactory(getFactory(2));
    }

    private SpinnerValueFactory.DoubleSpinnerValueFactory getFactory(int index) {
        SpinnerValueFactory.DoubleSpinnerValueFactory factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 301.0, 0.0, 0.1);
        factory.setConverter(new OffsetConverter(index));
        factory.valueProperty().setValue(schematic.getTrackedBlocks().getOffset()[index]);
        return factory;
    }

    @FXML
    void close(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        Controller.root.setDisable(false);
    }

    private List<Variable> usedVariables = new ArrayList<>();

    @FXML
    void addTrackedBlock() {
        Variable var = new Variable(varNameField.getText(), varScopePicker.getValue());
        if (usedVariables.contains(var)) return;
        usedVariables.add(var);

        TrackedBlock block = new TrackedBlock(var, blockField.getText(), removeCheckBox.isSelected());
        schematic.getTrackedBlocks().add(block);
        schematic.read();
        trackedBlocks.getItems().add(block);
    }

    static class OffsetConverter extends StringConverter<Double> {

        private Double value = 0.0;
        private int index;

        public OffsetConverter(int index) {
            this.index = index;
        }

        @Override
        public String toString(Double object) {
            String str = String.format("%.1f", object).replaceAll(",", ".");
            value = Double.valueOf(str);
            schematic.getTrackedBlocks().setOffset(index, value);
            return str;
        }

        @Override
        public Double fromString(String string) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException e) {
                return value;
            }
        }
    }
}
