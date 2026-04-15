module ac.csg.pu {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    // Logging
    requires org.slf4j;
    // SQL
    requires java.sql;
    requires io.javalin;
    requires okhttp3;
    requires com.fasterxml.jackson.databind;

    opens ac.csg.pu.gui.auth to javafx.graphics, javafx.fxml;
    opens ac.csg.pu.gui.dashboard.admin to javafx.graphics, javafx.fxml;
    opens ac.csg.pu.gui to javafx.fxml;

    exports ac.csg.pu.main;
    exports ac.csg.pu.gui.dashboard.admin;
    exports ac.csg.pu.gui;
    exports ac.csg.pu.prm;
    exports ac.csg.pu.sales;
    exports ac.csg.pu.comms.model;
    exports ac.csg.pu.comms;
    exports ac.csg.pu.ord;
    exports ac.csg.pu.gui.dashboard.commercial;
    opens ac.csg.pu.gui.dashboard.commercial to javafx.fxml, javafx.graphics;
}
