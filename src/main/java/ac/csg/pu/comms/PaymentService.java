package ac.csg.pu.comms;

import ac.csg.pu.comms.model.Payment;
import ac.csg.pu.comms.model.Response;
import ac.csg.pu.data.DatabaseUtility;

public class PaymentService {
    private static final DatabaseUtility db = new DatabaseUtility("payments.db");
    static {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS simulated_payments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                sender_name TEXT,
                sender_card_number TEXT,
                sender_expiry_date TEXT,
                sender_email TEXT,
                receiver_name TEXT,
                status INTEGER NOT NULL,
                message TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
    }
    public static Response process(Payment request) {
        Response response = new Response();
        if (request == null) {
            response.status = 400;
            response.message = "No payment details were sent.";
            savePayment(null, response);
            return response;
        }
        if (request.amount <= 0) {
            response.status = 400;
            response.message = "The payment amount isn’t valid.";
            savePayment(request, response);
            return response;
        }
        if (isBlank(request.senderName)) {
            response.status = 400;
            response.message = "Please enter the cardholder name.";
            savePayment(request, response);
            return response;
        }
        if (isBlank(request.senderCardNumber) || request.senderCardNumber.replaceAll("\\s", "").length() < 8) {
            response.status = 400;
            response.message = "Card number is incorrect.";
            savePayment(request, response);
            return response;
        }
        if (isBlank(request.senderExpiryDate)) {
            response.status = 400;
            response.message = "Please enter the expiry date.";
            savePayment(request, response);
            return response;
        }
        if (isBlank(request.senderCVV) || request.senderCVV.length() < 3) {
            response.status = 400;
            response.message = "CVV is invalid.";
            savePayment(request, response);
            return response;
        }
        if (isBlank(request.receiverName)) {
            response.status = 400;
            response.message = "Missing receiver details.";
            savePayment(request, response);
            return response;
        }
        response.status = 200;
        response.message = "Payment went through successfully.";
        savePayment(request, response);
        return response;
    }
    private static void savePayment(Payment request, Response response) {
        String cardNumber = null;
        if (request != null && request.senderCardNumber != null) {
            String cleaned = request.senderCardNumber.replaceAll("\\s", "");
            if (cleaned.length() > 4) {
                cardNumber = cleaned.substring(cleaned.length() - 4); // save only last 4 digits
            } else {
                cardNumber = cleaned;
            }
        }
        db.executeUpdate(""" 
            INSERT INTO simulated_payments (amount, sender_name, sender_card_number, sender_expiry_date, sender_email, receiver_name, status, message)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
                request != null ? request.amount : 0.0,
                request != null ? request.senderName : null,
                cardNumber,
                request != null ? request.senderExpiryDate : null,
                request != null ? request.senderEmail : null,
                request != null ? request.receiverName : null,
                response.status,
                response.message
        );
    }
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}