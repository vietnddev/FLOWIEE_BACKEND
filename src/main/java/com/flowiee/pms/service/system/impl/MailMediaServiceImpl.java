package com.flowiee.pms.service.system.impl;

import com.flowiee.pms.config.StartUp;
import com.flowiee.pms.config.TemplateSendEmail;
import com.flowiee.pms.entity.system.MailMedia;
import com.flowiee.pms.repository.system.MailMediaRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.system.MailMediaService;
import com.flowiee.pms.utils.SendMailUtils;
import com.flowiee.pms.utils.constants.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MailMediaServiceImpl extends BaseService implements MailMediaService {
    private final MailMediaRepository mvMailMediaRepository;

    @Override
    public void send(String pDestination, String pSubject, String pMessage) {
        this.send(pDestination, pSubject, pMessage, MailMedia.P_MEDIUM);
    }

    @Override
    public void send(String pDestination, String pSubject, String pMessage, int pPriority) {
        this.send(pDestination, pSubject, pMessage, "vi", true, MailMedia.P_MEDIUM);
    }

    @Override
    public void send(String pDestination, String pSubject, String pMessage, String pLanguage, boolean pIsHtml, int pPriority) {
        String[] emailDestinationArray = pDestination.split(MailMedia.EMAIL_ADDRESS_SPERATOR);
        Set<String> lvRecipients = new HashSet<>();
        for (String emailDestination : emailDestinationArray) {
            lvRecipients.add(emailDestination.trim());
        }
        for (String lvRecipient : lvRecipients) {
            mvMailMediaRepository.save(MailMedia.builder()
                    .destination(lvRecipient)
                    .subject(pSubject)
                    .language(pLanguage)
                    .message(pMessage)
                    .isHtml(pIsHtml)
                    .priority(pPriority)
                    .build());
        }
    }

    @Override
    public void send(NotificationType pNotificationType, Map<String, Object> pNotificationParameter) {
        TemplateSendEmail.Template lvTemplate = StartUp.mvGeneralEmailTemplateMap.get(pNotificationType);
        String lvDestination = pNotificationParameter.get(pNotificationType.name()).toString();
        String lvSubject = SendMailUtils.replaceTemplateParameter(lvTemplate.getSubject(), pNotificationParameter);
        String lvContent = SendMailUtils.replaceTemplateParameter(lvTemplate.getTemplateContent(), pNotificationParameter);
        this.send(lvDestination, lvSubject, lvContent);
    }
}