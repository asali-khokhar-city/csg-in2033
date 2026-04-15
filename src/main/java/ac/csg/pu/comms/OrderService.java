package ac.csg.pu.comms;

import ac.csg.pu.comms.model.Response;
import ac.csg.pu.ord.OrderDatabase;

public class OrderService {
    public static Response process(int id) {
        Response response = new Response();

        ac.csg.pu.ord.Order order = OrderDatabase.getOrder(id);

        if (order == null) {
            response.status = 404;
            response.message = "Order not found";
            return response;
        }

        response.status = 200;
        response.message = order.getStatus().name();
        return response;
    }
}
