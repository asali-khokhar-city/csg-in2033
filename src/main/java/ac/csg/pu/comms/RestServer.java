package ac.csg.pu.comms;

import ac.csg.pu.comms.model.Mail;
import ac.csg.pu.comms.model.Order;
import ac.csg.pu.comms.model.Payment;
import ac.csg.pu.comms.model.Response;
import ac.csg.pu.ord.OrderDatabase;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServer {

    private static Javalin app;
    private final static Logger logger = LoggerFactory.getLogger(RestServer.class);

    public static void start(int port) {
        if (app != null) {
            logger.info("Server has already been initialised. Cannot be started again.");
            return;
        }

        app = Javalin.create().start(port);

        stopOnShutdown();

        // Health endpoint
        app.get("/health", ctx -> ctx.result("OK"));

        // Payment endpoint
        app.post("/pay", ctx -> {
            Payment request = ctx.bodyAsClass(Payment.class);
            Response response = PaymentService.process(request);
            ctx.json(response);
        });

        // Mail endpoint
        app.post("/mail", ctx -> {
            Mail request = ctx.bodyAsClass(Mail.class);
            Response response = MailService.process(request);
           ctx.json(response);
        });

        // Order endpoint
        app.get("/order/track/{orderId}", ctx -> {
            int orderId = Integer.parseInt(ctx.pathParam("orderId"));
            Response response = OrderService.process(orderId);
            ctx.json(response);
        });
    }

    public static void stop() {
        if (app != null) app.stop();
        logger.info("App has been stopped.");
    }

    public static void stopOnShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(
                RestServer::stop, "Shutdown-thread"));
    }
}