package com.company.ecommercebackend.service;

import com.company.ecommercebackend.model.LocalUser;
import com.company.ecommercebackend.model.VerificationToken;
import com.company.ecommercebackend.exception.EmailFailureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Value("${email.from}")
  private String fromAddress;

  @Value("${app.frontend.url}")
  private String url;

  private JavaMailSender javaMailSender;

  public EmailService(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  private SimpleMailMessage makeMailMessage() {
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    simpleMailMessage.setFrom(fromAddress);
    return simpleMailMessage;
  }

  public void sendVerificationEmail(VerificationToken verificationToken) throws EmailFailureException {
    SimpleMailMessage message = makeMailMessage();
    message.setTo(verificationToken.getUser().getEmail());
    message.setSubject("Verify your email to active your account.");
    message.setText("Please follow the link below to verify your email to active your account.\n" +
        url + "/auth/verify?token=" + verificationToken.getToken());
    try {
      javaMailSender.send(message);
    } catch (MailException ex) {
      throw new EmailFailureException();
    }
  }

  public void sendPasswordResetEmail(LocalUser user,String token) throws EmailFailureException
  {
    SimpleMailMessage mailMessage=makeMailMessage();
    mailMessage.setTo(user.getEmail());
    mailMessage.setSubject("Your password reset request link.");
    mailMessage.setText("You requested a password reset. Please " +
            "enter the link below for reset password:\n"+url+
            "/auth/reset?token="+token);
    try {
      javaMailSender.send(mailMessage);
    }catch (MailException ex)
    {
      throw new EmailFailureException();
    }
  }

}
