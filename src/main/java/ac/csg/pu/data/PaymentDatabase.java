package ac.csg.pu.data;

import ac.csg.pu.comms.model.Payment;
import ac.csg.pu.comms.model.Response;

public class PaymentDatabase {
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
    public static void savePayment(Payment request, Response response) {
        String cardNumber = null;
        if (request != null && request.senderCardNumber != null) {
            String cleaned = request.senderCardNumber.replaceAll("\\s", "");
            if (cleaned.length() > 4) {
                cardNumber = cleaned.substring(cleaned.length() - 4);
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
}