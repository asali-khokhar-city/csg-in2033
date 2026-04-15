package ac.csg.pu.gui.dashboard.admin;

import ac.csg.pu.gui.SceneHelper;
import ac.csg.pu.prm.Promotion;
import ac.csg.pu.prm.PromotionDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PromotionsController {
    private final static Logger logger = LoggerFactory.getLogger(PromotionsController.class);

    @FXML private TableView<Promotion> promotionsTable;
    @FXML private TableColumn<Promotion, String> colName;
    @FXML private TableColumn<Promotion, String> colStart;
    @FXML private TableColumn<Promotion, String> colEnd;
    @FXML private TableColumn<Promotion, Boolean> colActive;
    @FXML private Label errorLabel;

    private ObservableList<Promotion> promotions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadPromotions();
        setupTableColumns();

        ContextMenu rowMenu = new ContextMenu();

        // Edit menu item
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(event -> {
            Promotion selected = promotionsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openEditPromotionDialogue(selected);
            }
        });

        // Delete menu item
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            Promotion selected = promotionsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                boolean confirmed = showDeleteConfirmation(selected);
                if (confirmed) {
                    deletePromotion(selected);
                }
            }
        });

        rowMenu.getItems().addAll(editItem, deleteItem);

        // Attach to table rows
        promotionsTable.setRowFactory(tv -> {
            TableRow<Promotion> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    promotionsTable.getSelectionModel().select(row.getIndex());
                    rowMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName())
        );
        colStart.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStartDate().toString())
        );
        colEnd.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEndDate().toString())
        );
        colActive.setCellValueFactory(data ->
                new SimpleBooleanProperty(data.getValue().isCurrentlyActive())
        );

        colActive.setCellFactory(CheckBoxTableCell.forTableColumn(colActive));

        colActive.setCellFactory(tc -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                // Listen to checkbox toggles
                checkBox.setOnAction(event -> {
                    Promotion p = getTableView().getItems().get(getIndex());
                    boolean newActive = checkBox.isSelected();
                    PromotionDatabase.setPromotionActive(p.getId(), newActive);
                    p.setActive(newActive); // update model
                    updateItem(p.isCurrentlyActive(), false); // refresh checkbox
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Promotion p = getTableView().getItems().get(getIndex());

                    // Checkbox state is determined by isCurrentlyActive()
                    checkBox.setSelected(p.isCurrentlyActive());

                    // Disable checkbox if out of date
                    boolean outOfDate = p.getEndDate().isBefore(LocalDate.now());
                    checkBox.setDisable(outOfDate);

                    // Tooltip for clarity
                    if (outOfDate) {
                        checkBox.setTooltip(new Tooltip("Promotion has expired and cannot be reactivated"));
                    } else {
                        checkBox.setTooltip(null);
                    }

                    setGraphic(checkBox);
                }
            }
        });

        // TODO: Actions column with Edit/Delete/Terminate buttons
    }

    private void loadPromotions() {
        //List<Promotion> activePromotions = PromotionDatabase.getActivePromotions();
        //logger.info("Active promotions list loaded: {}", activePromotions);
        //promotions.setAll(activePromotions);
        List<Promotion> allPromotions = PromotionDatabase.getAllPromotions();
        logger.info("All promotions list loaded: {}", allPromotions);
        promotions.setAll(allPromotions);
        promotionsTable.setItems(promotions);

    }

    @FXML
    private void onAddPromotion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("promotion-edit-popup.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add Promotion");
            PromotionEditController controller = loader.getController();
            controller.setup(null, this, stage);
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to open add promotion dialog:", e);
        }
    }

    @FXML
    void onRefresh() {
        loadPromotions();
    }

    private void openEditPromotionDialogue(Promotion promotion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("promotion-edit-popup.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edit Promotion");
            PromotionEditController controller = loader.getController();
            controller.setup(promotion, this, stage);
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to open edit dialog:", e);
        }
    }

    private boolean showDeleteConfirmation(Promotion promotion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Promotion");
        alert.setHeaderText(
                "Are you sure you want to delete " + promotion.getName() + "? This action cannot be undone."
        );
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void deletePromotion(Promotion promotion) {
        // Remove from database
        PromotionDatabase.deletePromotion(promotion.getId());
        // Refresh table
        promotionsTable.getItems().remove(promotion);
    }
    @FXML
    private void onReportsClick() {
        System.out.println("Reports button clicked");
        SceneHelper.switchScene("reports.fxml");
    }
}