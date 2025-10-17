package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.InvoiceResponse;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CompaniesRepository companiesRepository;
    private final InvoiceResponse invoiceResponse;

    public String generateInvoiceNumber(int previousTotalInvoices) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear()).substring(2);
        String month = String.format("%02d", now.getMonthValue());
        String sequence = String.format("%02d", previousTotalInvoices);
        return "INV-" + year + month + sequence;
    }

    // save invoice
//    @Transactional
    public ResponseEntity<?> saveNewInvoice(Invoice invoice, String companyOwnerId) {
        Optional<Users> companyOwner = userRepository.findById(companyOwnerId);
        if (companyOwner.isEmpty()) {
            return new ResponseEntity<>("Please create your account.", HttpStatus.BAD_REQUEST);
        }
        Optional<Companies> companyData = companiesRepository.findByOwnerId(companyOwnerId);
        if (companyData.isEmpty()) {
            return new ResponseEntity<>("Please add your company data.", HttpStatus.BAD_REQUEST);
        }
        Companies companyDetails = companyData.get();
        Users userData = companyOwner.get();
//        int newInvoiceCount = userData.getTotalInvoices() + 1;
//        String newInvoiceNumber = generateInvoiceNumber(newInvoiceCount);
        invoice.setCompanyId(companyDetails.getId());
        invoice.setOwnerId(companyOwnerId);
//        invoice.getInvoiceDetails().setInvNumber(newInvoiceNumber);
        Invoice savedInvoice = invoiceRepository.save(invoice);
//        userData.setTotalInvoices(newInvoiceCount);
        userRepository.save(userData);
        return new ResponseEntity<>(savedInvoice, HttpStatus.CREATED);
    }

    // fetch invoice
    public ResponseEntity<?> getInvoiceById(String invoiceId, String ownerId) {
        Optional<Companies> companyData = companiesRepository.findByOwnerId(ownerId);
        if (companyData.isEmpty()) {
            return new ResponseEntity<>("Company data is not available!", HttpStatus.BAD_REQUEST);
        }
        Optional<Invoice> invoice = invoiceRepository.findById(invoiceId);
        if (invoice.isEmpty()) {
            return new ResponseEntity<>("Invoice not found!", HttpStatus.BAD_REQUEST);
        }
        Companies companyDetails = companyData.get();
        Invoice invoiceDetails = invoice.get();
        if (!companyDetails.getId().equals(invoiceDetails.getCompanyId())) {
            return new ResponseEntity<>("Invoice not found!", HttpStatus.BAD_REQUEST);
        }
        InvoiceResponse response = new InvoiceResponse(companyData.get(), invoice.get());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // fetch latest 20 invoice
    public ResponseEntity<?> getLatestInvoice(String ownerId) {
        Optional<Companies> companyDetails = companiesRepository.findByOwnerId(ownerId);
        if (companyDetails.isEmpty()) {
            return new ResponseEntity<>("Create company.", HttpStatus.BAD_REQUEST);
        }
        String companyId = companyDetails.get().getId();
        Pageable topTwenty = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        List<Invoice> latestInvoice = invoiceRepository.findByCompanyId(companyId, topTwenty).getContent();
        if (latestInvoice.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(latestInvoice, HttpStatus.OK);
    }

    // get all invoice
    public ResponseEntity<?> getAllInvoice(String ownerId) {
        Optional<Companies> companyData = companiesRepository.findByOwnerId(ownerId);
        if (companyData.isEmpty()) {
            return new ResponseEntity<>("Company new company!", HttpStatus.BAD_REQUEST);
        }
        List<Invoice> allInvoice = invoiceRepository.findByCompanyId(companyData.get().getId(), Sort.by(Sort.Direction.DESC, "id"));
        if (allInvoice.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(allInvoice, HttpStatus.OK);
    }

    // delete invoice by id
    public ResponseEntity<?> deleteInvoiceById(String invoiceId, String ownerId) {
        Optional<Invoice> findInvoice = invoiceRepository.findById(invoiceId);
        if (findInvoice.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!findInvoice.get().getOwnerId().equals(ownerId)) {
            return new ResponseEntity<>("Not allowed to delete!", HttpStatus.UNAUTHORIZED);
        }
        invoiceRepository.deleteById(invoiceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // update or edit invoice
    public ResponseEntity<?> updateInvoice(Invoice newInvoice, String ownerId, String invoiceId) {
        Optional<Invoice> oldInvoice = invoiceRepository.findById(invoiceId);
        if (oldInvoice.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Invoice oldDetails = oldInvoice.get();
        if (!oldDetails.getOwnerId().equals(ownerId)) {
            return new ResponseEntity<>("Not have permission to delete!", HttpStatus.UNAUTHORIZED);
        }
        String companyId = oldDetails.getCompanyId();
        String invoiceNumber = oldDetails.getInvoiceDetails().getInvNumber();
        newInvoice.setId(invoiceId);
        newInvoice.setOwnerId(ownerId);
        newInvoice.setCompanyId(companyId);
        newInvoice.getInvoiceDetails().setInvNumber(invoiceNumber);
        Invoice updateInvoice = invoiceRepository.save(newInvoice);
        return new ResponseEntity<>(updateInvoice, HttpStatus.OK);
    }

}
