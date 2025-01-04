package com.flowiee.pms.schedule;

import com.flowiee.pms.entity.system.MailMedia;
import com.flowiee.pms.entity.system.MailStatus;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.repository.system.MailMediaRepository;
import com.flowiee.pms.repository.system.MailStatusRepository;
import com.flowiee.pms.service.system.SendMailService;
import com.flowiee.pms.utils.constants.ScheduleTask;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MailNotificationScheduleExecutor extends ScheduleExecutor {
    private final MailStatusRepository mailStatusRepository;
    private final MailMediaRepository mailMediaRepository;
    private final SendMailService sendMailService;

    @Scheduled(cron = "*/15 * * * * ?")
    @Override
    public void execute() throws AppException {
        super.init(ScheduleTask.MailNotification);
    }

    @Override
    public void doProcesses() throws AppException {
        int emailSentQty = 0;
        List<MailMedia> emailReadyToSendList = mailMediaRepository.getEmailReadyToSend();
        for (MailMedia mailMedia : emailReadyToSendList) {
            String[] emailDestinationArray = mailMedia.getDestination().split(MailMedia.EMAIL_ADDRESS_SPERATOR);
            String errorMsg = null;
            String sendStatus = "success";
            try {
                Set<String> lvRecipients = new HashSet<>();
                for (String emailDestination : emailDestinationArray) {
                    lvRecipients.add(emailDestination.trim());
                }
                for (String lvRecipient : lvRecipients) {
                    sendMailService.sendMail(mailMedia.getSubject(), lvRecipient, mailMedia.getMessage(), mailMedia.getAttachment());
                    emailSentQty += 1;
                }
            } catch (Throwable ex) {
                logger.error("An error occurred while send an email: " + ex.getMessage(), ex);
                errorMsg = ex.getMessage();
                sendStatus = "error";
            } finally {
                mailStatusRepository.save(MailStatus.builder()
                        .refId(mailMedia.getId())
                        .deliveryTime(errorMsg == null ? LocalDateTime.now() : null)
                        .errorMsg(errorMsg)
                        .status(sendStatus)
                        .build());
            }
        }
        if (emailSentQty > 0) {
            logger.info(emailSentQty + (emailSentQty == 1 ? " email has been sent." : " emails have been sent."));
        }
    }
}