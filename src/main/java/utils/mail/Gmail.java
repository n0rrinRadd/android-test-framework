package utils.mail;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Store;

import core.Elements;
import data.users.Customer;
import utils.Log;

public class Gmail {

    public static Message getEmailFromCustomerInbox(Customer customer) {

        final int MAX_MESSAGES = 5;

        String user = "testqa@gmail.com";
        String password = "";

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Log.info("");
        Log.info("Searching email for '" + customer.getEmail() + "' ...");
        Log.info("");

        return Elements.Wait(() -> {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", user, password);

            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_ONLY);

            Message tmpResult = null;

            Message messages[] = inbox.getMessages(inbox.getMessageCount() - MAX_MESSAGES, inbox.getMessageCount());

            for (int i = MAX_MESSAGES; i >= 0; i--) {
                String emailRecipient = messages[i].getRecipients(RecipientType.TO)[0].toString();
                Log.info(emailRecipient);

                if (emailRecipient.contains(customer.getEmail())) {
                    Log.info("Email found !");
                    tmpResult = messages[i];
                    break;
                }
            }

            // For log spacing
            Log.info("");

            inbox.close(false);
            store.close();

            return tmpResult;
        });
    }
}
