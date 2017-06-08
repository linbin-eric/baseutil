package com.jfireframework.baseutil;

import java.security.Security;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender
{
    static
    {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    }
    
    private String        host;
    private String        port;
    private String        username;
    private String        password;
    private Authenticator loginAuth;
    private Properties    props;
    
    public void init()
    {
        props = new Properties();
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.store.protocol", "smtp");
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.auth", "true");
        loginAuth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(username, password);
            }
        };
    }
    
    /**
     * 发送邮件进行处理，根据处理结果返回数字 0是成功，1是地址错误，2是发送失败
     * 
     * @param to
     * @param subject
     * @param content
     * @return
     * @throws AddressException
     * @throws MessagingException
     */
    public void sendSimpleText(String to, String subject, String content) throws AddressException, MessagingException
    {
        Session session = Session.getInstance(props, loginAuth);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(username));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        msg.setSubject(subject);
        msg.setText(content);
        Transport.send(msg);
    }
    
    public String getHost()
    {
        return host;
    }
    
    public void setHost(String host)
    {
        this.host = host;
    }
    
    public String getPort()
    {
        return port;
    }
    
    public void setPort(String port)
    {
        this.port = port;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
}
