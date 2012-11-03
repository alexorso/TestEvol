package org.testevol.controller;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/account")
public class RequestAccountController {

	@Value("#{testEvolProperties.email}")
	String email;
	@Value("#{testEvolProperties.passwd}")
	String password;

	@RequestMapping(method = RequestMethod.GET)
	public 	String requestAccount(@RequestParam("username") String username) {
		if(username == null || username.trim().length() == 0){
			return "redirect:/login";
		}

		request("Testevol Access Request", username);
		
		return "redirect:/login?accessRequested=true";
	}

	@RequestMapping(value="tool", method = RequestMethod.GET)
	public 	String requestTool(@RequestParam("username") String username) {
		if(username == null || username.trim().length() == 0){
			return "redirect:/login";
		}

		request("Testevol Tool Request", username);
		
		return "redirect:/login?toolRequested=true";
	}
	
	private void request(String messageBody, String username){
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email, password);
			}
		  });
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse("leandro.shp@gmail.com"));
			message.setSubject(messageBody);
			message.setText(username+" is requesting access to Testevol.");
 
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

}