package com.flowiee.pms.service.system;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface SendMailService {
    boolean sendMail(String subject, String to, String body) throws UnsupportedEncodingException, MessagingException;
}