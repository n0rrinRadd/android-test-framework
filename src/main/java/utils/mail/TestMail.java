package utils.mail;

import com.sun.mail.smtp.SMTPMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import utils.Log;
import utils.StringConverter;

public abstract class testMail {

    Message mEmail;
    Session mSession;
    List<MimeBodyPart> screenshotAttachments;
    String mTitle;
    MimeBodyPart mMessage;
    InternetAddress mRecipient;
    InternetAddress mCC;

    protected Session sessionConstructor(String email, String password) {
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "true");
        props.put("mail.store.protocol", "pop3");
        props.put("mail.transport.protocol", "smtp");

        Session session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        session.setDebug(false);
        return session;
    }

    protected List<MimeBodyPart> imageAttachmentConstructor(List<File> images) {
        List<MimeBodyPart> imageAttachments = new ArrayList<>();
        int imageNumber = 0;

        try {
            for (File image : images) {
                MimeBodyPart imageAttachment = new MimeBodyPart();
                imageAttachment.setHeader("Content-ID", "<image" + imageNumber++ + ">");
                imageAttachment.setDisposition(MimeBodyPart.INLINE);
                imageAttachment.attachFile(image);
                imageAttachments.add(imageAttachment);
            }

        } catch (Exception exception) {
            Log.error("ERROR: Unable to Attach Screenshots to Email");
        }

        return imageAttachments;
    }

    protected MimeBodyPart emailMessageConstructor(String content) {
        MimeBodyPart emailMessage = new MimeBodyPart();

        try {
            emailMessage.setContent(StringConverter.toHtml(content), "text/html");

        } catch (Exception exception) {
            Log.error("ERROR: Unable to Attach Stack Trace to Email");
        }

        return emailMessage;
    }

    protected void prepareEmail() {
        try {
            MimeMultipart emailAttachments = new MimeMultipart();

            mEmail = new SMTPMessage(mSession);
            mEmail.setFrom(new InternetAddress("testqa@gmail.com"));
            mEmail.addRecipient(Message.RecipientType.TO, mRecipient);

            if (mCC != null) {
                mEmail.addRecipient(Message.RecipientType.CC, mCC);
            }

            mEmail.setSubject(mTitle);
            emailAttachments.addBodyPart(mMessage);

            for (MimeBodyPart image : screenshotAttachments) {
                emailAttachments.addBodyPart(image);
            }

            mEmail.setContent(emailAttachments);

            Log.success("Success: Email Prepared to Send");

        } catch (Exception exception) {
            Log.error("Failure: Unable to Assemble Email");
        }
    }

    public void send() {
        try {
            Transport.send(mEmail);
            Log.success("Success: Email Was Sent Successfully");

        } catch (Exception exception) {
            Log.error("ERROR: Email Failed to Send");
        }
    }
}
