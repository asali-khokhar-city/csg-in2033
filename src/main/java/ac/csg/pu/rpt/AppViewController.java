package ac.csg.pu.rpt;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the Reports screen (app-view.fxml).
 *
 * Responsibilities:
 *   - Admin-gates all actions (requires isAdmin() via SessionState)
 *   - Runs report queries on a background thread (Task) to avoid freezing the UI
 *   - Computes the summary bar from real returned data
 *   - Exports to CSV and PDF
 *   - Optional 30-second auto-refresh via a JavaFX Timeline
 *   - Logs every report generation as a clickstream event
 */
public class AppViewController {

    private static final Logger logger = LoggerFactory.getLogger(AppViewController.class);

    private final ReportService reportService = new ReportService();

    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Label statusLabel;
    @FXML private Label summaryLabel;
    @FXML private CheckBox autoRefreshCheckbox;

    @FXML private TableView<ReportRow> resultsTable;
    @FXML private TableColumn<ReportRow, String> col1;
    @FXML private TableColumn<ReportRow, String> col2;
    @FXML private TableColumn<ReportRow, String> col3;
    @FXML private TableColumn<ReportRow, String> col4;

    @FXML private Button generateButton;
    @FXML private Button exportCsvButton;
    @FXML private Button exportPdfButton;

    private final ObservableList<ReportRow> tableData = FXCollections.observableArrayList();
    private Timeline autoRefreshTimeline;

    @FXML
    public void initialize() {
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Sales Report (UC38)",
                "Promotion Report (UC37)",
                "Click / Traffic Report (UC36)"
        ));

        col1.setCellValueFactory(data -> data.getValue().categoryProperty());
        col2.setCellValueFactory(data -> data.getValue().metricProperty());
        col3.setCellValueFactory(data -> data.getValue().valueProperty());
        col4.setCellValueFactory(data -> data.getValue().periodProperty());

        resultsTable.setItems(tableData);

        // Default date range: last 30 days
        toDate.setValue(LocalDate.now());
        fromDate.setValue(LocalDate.now().minusDays(30));

        // Admin gate — disable all actions if user is not admin

    }

    @FXML
    protected void onGenerateReport() {

        String selected = reportTypeCombo.getValue();
        LocalDate from  = fromDate.getValue();
        LocalDate to    = toDate.getValue();

        if (selected == null) {
            showError("Please select a report type.");
            return;
        }
        if (from == null || to == null) {
            showError("Please select a date range.");
            return;
        }
        if (from.isAfter(to)) {
            showError("'From' date must be before 'To' date.");
            return;
        }

        showSuccess("Generating report...");
        generateButton.setDisable(true);

        // Run the DB query on a background thread so the UI stays responsive
        Task<List<ReportRow>> task = new Task<>() {
            @Override
            protected List<ReportRow> call() {
                if (selected.startsWith("Sales")) {
                    return reportService.generateSalesReport(from, to);
                } else if (selected.startsWith("Promotion")) {
                    return reportService.generatePromotionReport(from, to);
                } else {
                    return reportService.generateClickReport(from, to);
                }
            }
        };

        task.setOnSucceeded(event -> {
            List<ReportRow> rows = task.getValue();
            tableData.setAll(rows);
            updateSummaryBar(rows, selected);
            showSuccess("Report generated: " + rows.size() + " rows.");
            generateButton.setDisable(false);

            // Log the event asynchronously

            logger.debug("Report generated for type '{}': {} rows", selected, rows.size());

            // Start auto-refresh if checked
            if (autoRefreshCheckbox != null && autoRefreshCheckbox.isSelected()) {
                startAutoRefresh(selected, from, to);
            }
        });

        task.setOnFailed(event -> {
            showError("Report failed: " + task.getException().getMessage());
            generateButton.setDisable(false);
            logger.error("Report generation failed", task.getException());
        });

        new Thread(task, "rpt-query-thread").start();
    }

    @FXML
    protected void onExportCSV() {
        if (tableData.isEmpty()) {
            showError("No report data to export. Generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as CSV");
        fileChooser.setInitialFileName("report.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(resultsTable.getScene().getWindow());
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Category,Metric,Value,Period\n");
            for (ReportRow row : tableData) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        row.getCategory(), row.getMetric(),
                        row.getValue(), row.getPeriod()));
            }
            showSuccess("Exported to: " + file.getName());
            logger.debug("CSV exported to: {}", file.getAbsolutePath());
        } catch (IOException e) {
            showError("Failed to export CSV: " + e.getMessage());
            logger.error("CSV export failed", e);
        }
    }

    @FXML
    protected void onExportPDF() {
        if (tableData.isEmpty()) {
            showError("No report data to export. Generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as PDF");
        fileChooser.setInitialFileName("report.pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(resultsTable.getScene().getWindow());
        if (file == null) return;

        try {
            String title = reportTypeCombo.getValue() != null
                    ? reportTypeCombo.getValue() : "Report";
            new PdfExporter().export(
                    java.util.List.copyOf(tableData),
                    title,
                    fromDate.getValue(),
                    toDate.getValue(),
                    file);
            showSuccess("PDF exported to: " + file.getName());
            logger.debug("PDF exported to: {}", file.getAbsolutePath());
        } catch (IOException e) {
            showError("Failed to export PDF: " + e.getMessage());
            logger.error("PDF export failed", e);
        }
    }

    @FXML
    protected void onClear() {
        stopAutoRefresh();
        tableData.clear();
        reportTypeCombo.setValue(null);
        fromDate.setValue(LocalDate.now().minusDays(30));
        toDate.setValue(LocalDate.now());
        statusLabel.setText("");
        summaryLabel.setText("-");
        if (autoRefreshCheckbox != null) autoRefreshCheckbox.setSelected(false);
    }

    // ── auto-refresh ──────────────────────────────────────────────────────────

    private void startAutoRefresh(String selected, LocalDate from, LocalDate to) {
        stopAutoRefresh();
        autoRefreshTimeline = new Timeline(new KeyFrame(
                Duration.seconds(30),
                e -> onGenerateReport()
        ));
        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        autoRefreshTimeline.play();
        logger.debug("Auto-refresh started (30s interval)");
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            autoRefreshTimeline = null;
        }
    }

    // ── summary bar ───────────────────────────────────────────────────────────

    /**
     * Extracts key values from the returned rows and builds the summary bar text.
     * This replaces the old hardcoded strings with values derived from real data.
     */
    private void updateSummaryBar(List<ReportRow> rows, String reportType) {
        if (rows.isEmpty()) {
            summaryLabel.setText("No data for selected period.");
            return;
        }

        if (reportType.startsWith("Sales")) {
            String revenue = findValue(rows, "Total Revenue",      "£0.00");
            String vat     = findValue(rows, "VAT Collected (20%)", "£0.00");
            String txns    = findValue(rows, "Total Transactions",  "0");
            summaryLabel.setText("Total Revenue: " + revenue
                    + "  |  Transactions: " + txns
                    + "  |  VAT: " + vat);

        } else if (reportType.startsWith("Promotion")) {
            String disc = findValue(rows, "Total Discounts Granted", "£0.00");
            String conv = findValue(rows, "Conversion Rate",          "0.0%");
            String roi  = findValue(rows, "Engagement ROI",           "0.0%");
            summaryLabel.setText("Discounts Granted: " + disc
                    + "  |  Conversion Rate: " + conv
                    + "  |  ROI: " + roi);

        } else {
            String clicks = findValue(rows, "Total Clicks",       "0");
            String users  = findValue(rows, "Unique Users",        "0");
            String avg    = findValue(rows, "Avg. Clicks per User", "0.00");
            summaryLabel.setText("Total Clicks: " + clicks
                    + "  |  Unique Users: " + users
                    + "  |  Avg per User: " + avg);
        }
    }

    /** Finds the Value column for a row matching the given Metric name. */
    private String findValue(List<ReportRow> rows, String metricName, String fallback) {
        return rows.stream()
                .filter(r -> metricName.equals(r.getMetric()))
                .map(ReportRow::getValue)
                .findFirst()
                .orElse(fallback);
    }

    // ── status helpers ────────────────────────────────────────────────────────

    private void showSuccess(String message) {
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText(message);
    }

    private void showError(String message) {
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText(message);
    }
}
