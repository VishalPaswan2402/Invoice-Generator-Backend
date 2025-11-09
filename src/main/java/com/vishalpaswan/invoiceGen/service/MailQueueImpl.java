package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.responseDTO.MailQueueItems;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
@Service
public class MailQueueImpl {
    private final Queue<MailQueueItems> failedMailQueue = new LinkedList<>();
    @Autowired
    private SendEmailService sendEmailService;
    private final int maxReTry = 3;

    public void addFailedMailToQueue(MailQueueItems retryMail) {
        log.info("Adding failed mail to queue.");
        failedMailQueue.add(retryMail);
    }

    @Scheduled(fixedDelay = 300000)
    public void reSendFailedMail() {
        log.info("Checking mail queue for retry. Pending mails: {}", failedMailQueue.size());
        List<MailQueueItems> failedAgain = new ArrayList<>();
        while (!failedMailQueue.isEmpty()) {
            MailQueueItems mailData = failedMailQueue.poll();
            try {
                sendEmailService.sendInvoiceLinkMail(
                        mailData.getSender(),
                        mailData.getReceiver(),
                        mailData.getInvoiceUrl(),
                        mailData.getCompanyName()
                );
                log.info("Mail re-sent successfully to {}", mailData.getReceiver());
            } catch (Exception ex) {
                mailData.setMaxReTry(mailData.getMaxReTry() + 1);
                if (mailData.getMaxReTry() < maxReTry) {
                    failedAgain.add(mailData);
                    log.warn("Retry {} failed for {}. Re-adding to queue.", mailData.getMaxReTry(), mailData.getReceiver());
                } else {
                    log.error("Mail to {} failed after {} retries. Dropping from queue.", mailData.getReceiver(), mailData.getMaxReTry());
                }
            }
        }
        failedMailQueue.addAll(failedAgain);
    }

}
