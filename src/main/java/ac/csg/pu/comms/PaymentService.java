package ac.csg.pu.comms;

import ac.csg.pu.comms.model.Payment;
import ac.csg.pu.comms.model.Response;

public class PaymentService {
    public static Response process(Payment request) {
        Response response = new Response();
        if (request == null) {
            response.status = 400;
            response.message = "No payment details were sent.";
            return response;
        }
        if (request.amount <= 0) {
            response.status = 400;
            response.message = "The payment amount isn’t valid.";
            return response;
        }
        if (isBlank(request.senderName)) {
            response.status = 400;
            response.message = "Please enter the cardholder name.";
            return response;
        }
        if (isBlank(request.senderCardNumber) || request.senderCardNumber.replaceAll("\\s", "").length() < 8) {
            response.status = 400;
            response.message = "Card number is incorrect.";
            return response;
        }
        if (isBlank(request.senderExpiryDate)) {
            response.status = 400;
            response.message = "Please enter the expiry date.";
            return response;
        }
        if (isBlank(request.senderCVV) || request.senderCVV.length() < 3) {
            response.status = 400;
            response.message = "CVV is invalid.";
            return response;
        }
        if (isBlank(request.receiverName)) {
            response.status = 400;
            response.message = "Missing receiver details.";
            return response;
        }
        response.status = 200;
        response.message = "Payment went through successfully.";
        return response;
    }
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}