package com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp;

import com.vishalpaswan.invoiceGen.dto.responseDTO.MailQueueItems;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@RequiredArgsConstructor
@Slf4j
@Service
public class MailQueueImpl {
    private final Queue<MailQueueItems> failedMailQueue = new LinkedList<>();
    @Autowired
    private InvoiceMailTemplate sendInvoiceMail;
    private final int maxReTry = 3;

    private void addFailedMailToQueue(MailQueueItems retryMail) {
        log.info("Adding failed mail to queue.");
        failedMailQueue.add(retryMail);
    }

    private void reSendFailedMail() {
        log.info("Checking mail queue for retry. Pending mails: {}", failedMailQueue.size());
        List<MailQueueItems> failedAgain = new ArrayList<>();
        while (!failedMailQueue.isEmpty()) {
            MailQueueItems mailData = failedMailQueue.poll();
            try {
                sendInvoiceMail.sendInvoiceLink(
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

    public void addMailToQueue(MailQueueItems retryMail) {
        addFailedMailToQueue(retryMail);
    }

    @Scheduled(fixedDelay = 900000)
    public void sendFailedMail() {
        reSendFailedMail();
    }

}
