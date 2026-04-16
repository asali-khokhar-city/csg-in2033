package ac.csg.pu.comms;

import ac.csg.pu.comms.model.Mail;
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
            System.out.println("REST SERVER START CALLED ON PORT " + port);
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
            ctx.html(OrderService.render(orderId));
        });

        app.put("/order/{orderId}/status", ctx -> {
            int orderId = Integer.parseInt(ctx.pathParam("orderId"));

            UpdateStatusRequest request = ctx.bodyAsClass(UpdateStatusRequest.class);

            if (request == null || request.status == null || request.status.isBlank()) {
                ctx.status(400).result("Missing status");
                return;
            }

            boolean updated = OrderDatabase.updateOrderStatus(orderId, request.status);

            if (updated) {
                ctx.result("Order status updated");
            } else {
                ctx.status(404).result("Order not found");
            }
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
    public static class UpdateStatusRequest {
        public String status;
    }
}