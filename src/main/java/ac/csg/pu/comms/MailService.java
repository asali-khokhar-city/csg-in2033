package ac.csg.pu.comms;

import ac.csg.pu.comms.model.Mail;
import ac.csg.pu.comms.model.Response;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String EMAIL = "csg.in2033@gmail.com";
    // REPLACE WITH REAL PASSWORD
    private static final String APP_PASSWORD = "EMAIL APP PASSWORD";

    public static Response process(Mail mail) {
        Response res = new Response();

        try {
            sendMail(mail);
            res.status = 200;
            res.message = "Email sent successfully";
        } catch (Exception e) {
            res.status = 500;
            res.message = "Failed to send email: " + e.getMessage();
        }

        return res;
    }

    private static void sendMail(Mail mail) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(EMAIL));

        for (String receiver : mail.receivers) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
        }

        message.setSubject(mail.subject);
        message.setText(mail.body);

        Transport.send(message);
    }
}