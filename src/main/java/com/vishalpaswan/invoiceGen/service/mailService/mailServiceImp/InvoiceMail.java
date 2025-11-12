package com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.MailQueueItems;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceMail {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CompaniesRepository companiesRepository;
    private final MailQueueImpl mailQueueImp;
    private final InvoiceMailTemplate sendInvoiceMail;
    private final String backendBaseUrl = "http://localhost:8080";

    private ResponseEntity<?> sendInvoiceLink(String ownerId, String invoiceId) {
        String compEmail = null;
        String compName = null;
        String invUrl = null;
        String invEmail = null;
        try {
            if (ownerId == null || ownerId.isBlank() || invoiceId == null || invoiceId.isBlank()) {
                log.warn("Invalid request: ownerId , receiverEmail or invoiceId is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID , Receiver Email or Invoice ID is required.");
            }

            Optional<Invoice> fetchInvoice = invoiceRepository.findById(invoiceId);
            if (fetchInvoice.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Invoice not found.");
            }

            Optional<Users> findOwner = userRepository.findById(ownerId);
            if (findOwner.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Account not found. Please create your account first.");
            }

            Invoice invoice = fetchInvoice.get();
            String companyId = invoice.getCompany().getId();
            Optional<Companies> fetchCompany = companiesRepository.findById(companyId);

            if (fetchCompany.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Company data is missing. Add your company data.");
            }

            String companyOwnerId = fetchCompany.get().getOwner().getId();

            if (!companyOwnerId.equals(ownerId)) {
                return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Not authorized to perform this operation.");
            }

            Companies company = fetchCompany.get();
            String invoiceUrl = backendBaseUrl + "/public/invoice-gen/api/" + invoiceId + "/view";

            compEmail = company.getEmail();
            compName = company.getCompanyName();
            invUrl = invoiceUrl;
            invEmail = invoice.getBillingDetails().getEmail();

            sendInvoiceMail.sendInvoiceLink(company.getEmail(), invoice.getBillingDetails().getEmail(), invoiceUrl, company.getCompanyName());
            return ResponseBuilder.success(HttpStatus.OK, "Invoice mail send successfully.", null);

        } catch (MailException ex) {
            log.error("Mail sending failed, adding to retry queue: {}", ex.getMessage());
            MailQueueItems mailQueueItems = new MailQueueItems(compEmail, invEmail, compName, invUrl, 0);
            mailQueueImp.addMailToQueue(mailQueueItems);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error while sending main. Please try again.");
        } catch (DataAccessException ex) {
            log.error("Database error while fetching invoice {} for owner {}: {}", invoiceId, ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while fetching invoice.");
        } catch (Exception e) {
            log.error("Unexpected error while fetching invoice {} for owner {}: {}", invoiceId, ownerId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while updating invoice. Please try again later.");
        }
    }

    public ResponseEntity<?> sendLink(String ownerId, String invoiceId) {
        return sendInvoiceLink(ownerId, invoiceId);
    }
}
