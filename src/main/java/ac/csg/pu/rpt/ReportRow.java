package ac.csg.pu.rpt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReportRow {

    private final StringProperty category;
    private final StringProperty metric;
    private final StringProperty value;
    private final StringProperty period;

    public ReportRow(String category, String metric, String value, String period) {
        this.category = new SimpleStringProperty(category);
        this.metric = new SimpleStringProperty(metric);
        this.value = new SimpleStringProperty(value);
        this.period = new SimpleStringProperty(period);
    }

    public String getCategory() {return category.get();}

    public void setCategory(String category) {this.category.set(category);}

    public StringProperty categoryProperty() {return category;}

    public String getMetric() {return metric.get();}

    public void setMetric(String metric) {this.metric.set(metric);}

    public StringProperty metricProperty() {return metric;}

    public String getValue() {return value.get();}

    public void setValue(String value) {this.value.set(value);}

    public StringProperty valueProperty() {return value;}

    public String getPeriod() {return period.get();}

    public void setPeriod(String period) {this.period.set(period);}

    public StringProperty periodProperty() {return period;}
}