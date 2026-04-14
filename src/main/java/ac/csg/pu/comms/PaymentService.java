package ac.csg.pu.comms;

import ac.csg.pu.comms.model.Payment;
import ac.csg.pu.comms.model.Response;
import ac.csg.pu.data.PaymentDatabase;

public class PaymentService {
    public static Response process(Payment request) {
        Response response = new Response();
        if (request == null) {
            response.status = 400;
            response.message = "No payment details were sent.";
            PaymentDatabase.savePayment(null, response);
            return response;
        }
        if (request.amount <= 0) {
            response.status = 400;
            response.message = "The payment amount isn’t valid.";
            PaymentDatabase.savePayment(request, response);
            return response;
        }
        if (isBlank(request.senderName)) {
            response.status = 400;
            response.message = "Please enter the cardholder name.";
            PaymentDatabase.savePayment(request, response);
            return response;
        }
        if (isBlank(request.senderCardNumber) || request.senderCardNumber.replaceAll("\\s", "").length() < 8) {
            response.status = 400;
            response.message = "Card number is incorrect.";
            PaymentDatabase.savePayment(request, response);
            return response;
        }
        if (isBlank(request.senderExpiryDate)) {
            response.status = 400;
            response.message = "Please enter the expiry date.";
            PaymentDatabase.savePayment(request, response);
            return response;
        }
        if (isBlank(request.senderCVV) || request.senderCVV.length() < 3) {
            response.status = 400;
            response.message = "CVV is invalid.";
            PaymentDatabase.savePayment(request, response);
            return response;
        }
        if (isBlank(request.receiverName)) {
            response.status = 400;
            response.message = "Missing receiver details.";
            PaymentDatabase.savePayment(request, response);
            return response;
        }
        response.status = 200;
        response.message = "Payment went through successfully.";
        PaymentDatabase.savePayment(request, response);
        return response;
    }
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}