package com.itc.commons.core.services.impl;

import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.jcr.RepositoryException;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
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
import com.itc.commons.core.services.MailService;

@Component(service = MailService.class, immediate = true)
@Designate(ocd = MailServiceImpl.MailServiceConfig.class)
public class MailServiceImpl implements MailService  {
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
  public void sendEmail(String userOrGroupName, ResourceResolver resourceResolver, String message, String subject, boolean isGroup) throws MessagingException {
    Session session = getSession();
    MimeMessage mimeMessage;


    try {
      List<String> userEmail = getEmail(userOrGroupName, isGroup, resourceResolver);

      if (userEmail != null  &&  !userEmail.isEmpty()) {
        System.out.println(userEmail);
        mimeMessage = getMimeMessage(session, fromAddress, userEmail);
        System.out.println("mime: " + Arrays.toString(mimeMessage.getAllRecipients()));
        LOGGER.info("MimeMessage constructed successfully.");

        mimeMessage.setSubject(subject);
        LOGGER.debug("Subject set on email: {}", subject);

        mimeMessage.setContent(message, "text/html");
        LOGGER.debug("Email content set as HTML.");

        sendMessage(session, mimeMessage);
        LOGGER.info("Email sent");
      }
      else {
        throw new MessagingException("No email associated with user");
      }
    } catch (MessagingException | UnsupportedEncodingException | RepositoryException e) {
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

  private void sendMessage(Session session, MimeMessage msg) throws MessagingException {
    try {
      Transport transport = session.getTransport();
      transport.connect(smtpHost, smtpUsername, smtpPassword);
      transport.sendMessage(msg, msg.getAllRecipients());
      LOGGER.info("Message sent successfully");
    } catch (MessagingException e) {
      throw new MessagingException("There was an exception in sending the message : " + e.getMessage());
    }
  }

  private MimeMessage getMimeMessage(Session session, String fromAddress, List<String> userEmail) throws UnsupportedEncodingException, MessagingException {
    LOGGER.info("Creating MimeMessage with session and from address: {}", fromAddress);
    MimeMessage mimeMessage = new MimeMessage(session);

    InternetAddress from = new InternetAddress(fromAddress, "ITC-ASSET-UPLOAD");
    mimeMessage.setFrom(from);
    LOGGER.debug("From address set: {}", from.toString());

    for (String toAddress : userEmail) {
      mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
      LOGGER.debug("Added TO recipient: {}", toAddress);
    }

    LOGGER.info("MimeMessage prepared with TO recipients.");

    return mimeMessage;
  }

  private List<String> getEmail(String userOrGroupName, boolean isGroup, ResourceResolver resourceResolver) throws RepositoryException {
    List<String> emails;

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

      Authorizable auth = userManager.getAuthorizable(userOrGroupName);

      if (isGroup) {
        emails = emailsFromGroup(auth, resourceResolver);
      }
      else {
        emails = emailFromUser(auth, resourceResolver);
      }

    } catch (RepositoryException e) {
      throw new RepositoryException("Error fetching email for " + userOrGroupName + ", " + e.getMessage());
    }
    return emails;
  }

  private List<String> emailsFromGroup(Authorizable auth, ResourceResolver resourceResolver) throws RepositoryException {

    List<String> emails = new ArrayList<>();

    if (auth != null && auth.isGroup()) {
      Group group = (Group) auth;
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
      throw new RepositoryException("Group not found");
    }
    return emails;
  }

  private List<String> emailFromUser(Authorizable auth, ResourceResolver resourceResolver) throws RepositoryException {

    List<String> email = new ArrayList<>();
    User user = (User) auth;

    String userId = null;
    if (user != null) {
      userId = user.getID();
    }

    LOGGER.info("memberId: {}", userId);
    String profilePath = user.getPath() + "/profile";
    Resource userRes = resourceResolver.getResource(profilePath);
    LOGGER.info("email profile path: {}", profilePath);
    if (userRes != null && userRes.getValueMap().containsKey("email")) {
      String userEmail = userRes.getValueMap().get("email", String.class);
      email.add(userEmail);
      LOGGER.info("email from profile node: {}", email);
    }
    else {
      throw new RepositoryException("UserResource for user is null");
    }
    return email;
  }

  @ObjectClassDefinition(name = "ITC Asset Approval Email Configs", description = "ITC Asset Approval Email Configs") public @interface MailServiceConfig {

    @AttributeDefinition(name = "From Address", description = "From address")
    String fromAddress();

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
