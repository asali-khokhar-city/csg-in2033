package ac.csg.pu.gui.reports;


import ac.csg.pu.gui.SceneHelper;
import ac.csg.pu.rpt.PdfExporter;
import ac.csg.pu.rpt.ReportRow;
import ac.csg.pu.rpt.ReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class ReportsController {

    @FXML private ComboBox<String> reportTypeBox;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private TableView<ReportRow> reportTable;
    @FXML private TableColumn<ReportRow, String> productIdColumn;
    @FXML private TableColumn<ReportRow, String> productNameColumn;
    @FXML private TableColumn<ReportRow, String> unitPriceColumn;
    @FXML private TableColumn<ReportRow, String> purchasePriceColumn;
    @FXML private TableColumn<ReportRow, String> quantityColumn;
    @FXML private TableColumn<ReportRow, String> totalColumn;
    @FXML private Label statusLabel;

    private final ObservableList<ReportRow> currentRows = FXCollections.observableArrayList();
    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        reportTypeBox.getItems().addAll(
                "Sales Report",
                "Campaign Report",
                "Customer Engagement Report"
        );
        productIdColumn.setCellValueFactory(c -> c.getValue().productIdProperty());
        productNameColumn.setCellValueFactory(c -> c.getValue().productNameProperty());
        unitPriceColumn.setCellValueFactory(c -> c.getValue().unitPriceProperty());
        purchasePriceColumn.setCellValueFactory(c -> c.getValue().purchasePriceProperty());
        quantityColumn.setCellValueFactory(c -> c.getValue().quantityProperty());
        totalColumn.setCellValueFactory(c -> c.getValue().totalProperty());
        reportTable.setItems(currentRows);
    }

    private void configureColumns(String type) {
        if ("Sales Report".equals(type)) {
            productIdColumn.setText("Product ID");
            productNameColumn.setText("Product Name");
            unitPriceColumn.setText("Unit Price");
            purchasePriceColumn.setText("Purchase Price");
            quantityColumn.setText("Quantity");
            totalColumn.setText("Total");

            quantityColumn.setVisible(true);
            totalColumn.setVisible(true);
        } else {
            productIdColumn.setText("Category");
            productNameColumn.setText("Metric");
            unitPriceColumn.setText("Value");
            purchasePriceColumn.setText("Period");

            quantityColumn.setVisible(false);
            totalColumn.setVisible(false);
        }
    }

    @FXML
    private void onGenerate() {
        String type = reportTypeBox.getValue();
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();
        if (type == null || from == null || to == null) {
            statusLabel.setText("Please fill all fields.");
            return;
        }
        if (from.isAfter(to)) {
            statusLabel.setText("From date must be before to date.");
            return;
        }
        configureColumns(type);
        List<ReportRow> rows = reportService.generateReport(type, from, to);
        currentRows.setAll(rows);
        statusLabel.setText("Report generated.");
    }

    @FXML
    private void onExport() {
        if (currentRows.isEmpty()) {
            statusLabel.setText("Nothing to export.");
            return;
        }
        try {
            new PdfExporter().export(
                    currentRows,
                    reportTypeBox.getValue(),
                    fromDate.getValue(),
                    toDate.getValue(),
                    new File("report.pdf")
            );
            statusLabel.setText("PDF exported.");
        } catch (Exception e) {
            statusLabel.setText("Export failed.");
        }
    }
    @FXML
    public void onBack() {
        SceneHelper.switchScene("dashboard/admin/promotions.fxml");
    }
}