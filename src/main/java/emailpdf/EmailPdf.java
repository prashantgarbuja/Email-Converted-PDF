package emailpdf;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailPdf {
	private static Session session;
	
	//Initiate properties of email.
	public static void init() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("demoproject0077", "createapassword");
            }
        });
        session.setDebug(true); //trace the execution of session.
    }
	
	public static void sendMail(String pdf_file,String email) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("demoproject0077"));
        message.setRecipients(Message.RecipientType.TO,
        InternetAddress.parse(email)); //email of recepient
        message.setSubject("Advice of Credit"); //Subject of email.
        
        Multipart multipart = new MimeMultipart();

        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText("Provide content of email body here"); //Email Body

        MimeBodyPart attachmentBodyPart= new MimeBodyPart();
        DataSource source = new FileDataSource(pdf_file); //Source of file to send as attachment.
        attachmentBodyPart.setDataHandler(new DataHandler(source));
        attachmentBodyPart.setFileName("Advice of Credit.pdf"); //File name appear in attachment.
        
        multipart.addBodyPart(textBodyPart);  // add the text part
        multipart.addBodyPart(attachmentBodyPart); // add the attachment part
        
        message.setContent(multipart); //combine text and attachment and set it on message.
        
        try {
        Transport.send(message); //Send the message.
        System.out.println("Success");
        } finally {
        	 File file = new File(pdf_file); 
        	file.delete(); //Delete file after send has been successful.
        }
      }
	}