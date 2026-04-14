package ac.csg.pu.main;

import ac.csg.pu.comms.RestServer;
import ac.csg.pu.gui.AppView;

/**
 * Main class to start the JavaFX application.
 * This separate launcher class helps resolve the "JavaFX runtime components are missing" error
 * by ensuring proper initialization of the JavaFX runtime.
 */
public class Main {
    public static void main(String[] args) {
        AppView.main(args);
        RestServer.start(8090);
    }
}