package com.itc.commons.core.services.impl;

import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.jcr.RepositoryException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.itc.commons.core.listener.AssetAcceptRejectListener;
import com.itc.commons.core.services.MailService;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = MailService.class, immediate = true)
@Designate(ocd = MailServiceImpl.MailServiceConfig.class)
public class MailServiceImpl implements MailService {
  private String fromAddress;
  private String smtpHost;
  private String smtpUsername;
  private String smtpPassword;
  private int smtpPort;


  private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

  @Activate
  @Modified
  protected void activate(MailServiceConfig mailServiceConfig) {
    fromAddress = "tarunpratap3135@gmail.com";
    smtpHost = "smtp.gmail.com";
    smtpUsername = "tarunpratap3135@gmail.com";
    smtpPassword = "oukbpgdddewbyywu";
    smtpPort = 587;
  }


  @Override
  public void sendEmail(String groupName, ResourceResolver resourceResolver, String message, String subject) throws MessagingException {
    Session session = getSession();
    MimeMessage mimeMessage;

    try {
      List<String> userEmail = getEmailsFromGroup(groupName,resourceResolver);
        if (userEmail != null  &&  !userEmail.isEmpty()) {
          mimeMessage = getMimeMessage(session, fromAddress, userEmail);
          LOGGER.info("MimeMessage constructed successfully.");

          mimeMessage.setSubject(subject);
          LOGGER.debug("Subject set on email: {}", subject);

          mimeMessage.setContent(message, "text/html");
          LOGGER.debug("Email content set as HTML.");

          sendMessage(session, mimeMessage);
          LOGGER.info("Email sent");
        }
        else {
          throw new MessagingException("Group has either no user or associated user has no email");
        }
    } catch (MessagingException | UnsupportedEncodingException e) {
      throw new MessagingException("There was an exception in sending the message : " + e.getMessage());
    }
  }

  private Session getSession() {
    Properties props = System.getProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.port", smtpPort);
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.ssl", "false");
    return Session.getDefaultInstance(props);
  }

  private void sendMessage(Session session, MimeMessage msg) {
    boolean status;
    try {
      Transport transport = session.getTransport();
      transport.connect(smtpHost, smtpUsername, smtpPassword);
      transport.sendMessage(msg, msg.getAllRecipients());
      status = true;
    } catch (MessagingException e) {
      LOGGER.error("There was an exception in sending the message : {}", e.getMessage(), e);
      status = false;
    }
    LOGGER.info("send message status '{}'",status);
  }

  private MimeMessage getMimeMessage(Session session, String fromAddress, List<String> userEmail) throws UnsupportedEncodingException, MessagingException {
    LOGGER.info("Creating MimeMessage with session and from address: {}", fromAddress);
    MimeMessage mimeMessage = new MimeMessage(session);

    InternetAddress from = new InternetAddress(fromAddress, "ITC-Content-Publish");
    mimeMessage.setFrom(from);
    LOGGER.debug("From address set: {}", from.toString());

    for (String toAddress : userEmail) {
      mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
      LOGGER.debug("Added TO recipient: {}", toAddress);
    }


    LOGGER.info("MimeMessage prepared with TO and CC recipients.");

    return mimeMessage;
  }

  private List<String> getEmailsFromGroup(String groupName, ResourceResolver resourceResolver) {
    List<String> emails = new ArrayList<>();

    try {
      UserManager userManager;
      if (resourceResolver == null) {
        LOGGER.warn("ResourceResolver is null");
        return null;
      }

      userManager = resourceResolver.adaptTo(UserManager.class);

      if (userManager == null) {
        LOGGER.warn("UserManager is null");
        return null;
      }

      Authorizable groupAuth = userManager.getAuthorizable(groupName);
      if (groupAuth != null && groupAuth.isGroup()) {
        Group group = (Group) groupAuth;
        Iterator<Authorizable> members = group.getMembers();

        while (members.hasNext()) {
          Authorizable member = members.next();
          String memberId = member.getID();
          LOGGER.info("memberId: {}", memberId);
          String profilePath = member.getPath() + "/profile";
          Resource userRes = resourceResolver.getResource(profilePath);
          LOGGER.info("email profile path: {}", profilePath);
          if (userRes != null && userRes.getValueMap().containsKey("email")) {
            String email = userRes.getValueMap().get("email", String.class);
            emails.add(email);
            LOGGER.info("email from profile node: {}", email);
          }
        }
      }
      else {
        LOGGER.warn("Group '{}' not found or is not a group", groupName);
      }

    } catch (RepositoryException e) {
      LOGGER.error("Error fetching group emails for '{}': {}", groupName, e.getMessage(), e);
    }
    return emails;
  }


  @ObjectClassDefinition(name = "ITC Corporate Email Configs", description = "ITC Corporate Email Configs") public @interface MailServiceConfig {

    @AttributeDefinition(name = "From Address", description = "From address")
    String fromAddress();

    @AttributeDefinition(name = "To Address", description = "To address")
    String toAddress();

    @AttributeDefinition(name = "CC Addresses", description = "CC addresses")
    String[] ccAddresses();

    @AttributeDefinition(name = "SMTP Host", description = "SMTP Host")
    String smtpHost();

    @AttributeDefinition(name = "SMTP Username", description = "SMTP username")
    String smtpUserName();

    @AttributeDefinition(name = "SMTP Password", description = "SMTP password")
    String smtpPassword();

    @AttributeDefinition(name = "SMTP Port Number", description = "SMTP Port Number")
    int smtpPort();
  }

}
