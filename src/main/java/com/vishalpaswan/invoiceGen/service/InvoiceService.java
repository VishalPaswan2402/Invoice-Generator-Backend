package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.InvoiceRequest;
import com.vishalpaswan.invoiceGen.dto.InvoiceResponse;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.inputValidationCheck.GenerateInvNumber;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceRequestMapper;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceResponseMapper;
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
    private final InvoiceResponseMapper invoiceResponseMapper;
    private final InvoiceRequestMapper invoiceRequestMapper;

    // generate invoice number
    public String generateInvoiceNumber(int previousTotalInvoices) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear()).substring(2);
        String month = String.format("%02d", now.getMonthValue());
        String sequence = String.format("%02d", previousTotalInvoices);
        return "INV-" + year + month + sequence;
    }

    // save new invoice
//    @Transactional
    public ResponseEntity<?> saveNewInvoice(InvoiceRequest invoiceRequest, String companyOwnerId) {
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
        int newInvoiceCount = userData.getTotalInvoices() + 1;
//        String newInvoiceNumber = generateInvoiceNumber(newInvoiceCount);
        String newInvoiceNumber = GenerateInvNumber.generateInvoiceNumber(newInvoiceCount);
        int grandTotal = invoiceRequest.getItemsDetails().stream()
                .mapToInt(item -> (int) (item.getQuantity() * item.getRate()))
                .sum();
        int paidAmount = invoiceRequest.getPaidAmount();
        int dueBalance = grandTotal - paidAmount;
        Invoice newInvoice = invoiceRequestMapper.mapToInvoice(invoiceRequest);
        newInvoice.setGrandTotal(grandTotal);
        newInvoice.setDueBalance(dueBalance);
        newInvoice.setCompany(companyDetails);
        newInvoice.getInvoiceDetails().setInvNumber(newInvoiceNumber);
        Invoice savedInvoice = invoiceRepository.save(newInvoice);
        userData.setTotalInvoices(newInvoiceCount);
        userRepository.save(userData);
        InvoiceResponse invoiceResponse = invoiceResponseMapper.mapToResponse(savedInvoice);
        invoiceResponse.setCompanyDetails(new InvoiceResponse.CompanyDetails());
        invoiceResponse.getCompanyDetails().setName(savedInvoice.getCompany().getCompanyName());
        invoiceResponse.getCompanyDetails().setContact(savedInvoice.getCompany().getContact());
        invoiceResponse.getCompanyDetails().setEmail(savedInvoice.getCompany().getEmail());
        invoiceResponse.getCompanyDetails().setAddress(savedInvoice.getCompany().getAddress());
        return new ResponseEntity<>(invoiceResponse, HttpStatus.CREATED);
    }

    // fetch invoice
    public ResponseEntity<?> getInvoiceById(String invoiceId, String userId) {
        Optional<Invoice> toFetchInvoice = invoiceRepository.findById(invoiceId);
        if (toFetchInvoice.isEmpty()) {
            return new ResponseEntity<>("Invoice not found!", HttpStatus.BAD_REQUEST);
        }
        Invoice invoiceDetails = toFetchInvoice.get();
        Companies company = invoiceDetails.getCompany();
        String companyId = invoiceDetails.getCompany().getId();
        Optional<Users> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("Invoice not found!", HttpStatus.BAD_REQUEST);
        }
        boolean isFind = false;
        List<Companies> companiesList = user.get().getCompanies();
        for (Companies companies : companiesList) {
            if (companies.getId().equals(companyId)) {
                isFind = true;
                break;
            }
        }
        if (!isFind) {
            return new ResponseEntity<>("Invoice not found!", HttpStatus.BAD_REQUEST);
        }
        InvoiceResponse invoiceResponse = invoiceResponseMapper.mapToResponse(invoiceDetails);
        invoiceResponse.setCompanyDetails(new InvoiceResponse.CompanyDetails());
        invoiceResponse.getCompanyDetails().setName(company.getCompanyName());
        invoiceResponse.getCompanyDetails().setEmail(company.getEmail());
        invoiceResponse.getCompanyDetails().setAddress(company.getAddress());
        invoiceResponse.getCompanyDetails().setContact(company.getContact());
        return new ResponseEntity<>(invoiceResponse, HttpStatus.OK);
    }

    // fetch latest 20 invoice
    public ResponseEntity<?> getLatestInvoice(String ownerId) {
        Optional<Users> user = userRepository.findById(ownerId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("Create account.", HttpStatus.BAD_REQUEST);
        }
        Users userData = user.get();
        if (userData.getTotalCompany() == 0) {
            return new ResponseEntity<>("Create company account.", HttpStatus.CONFLICT);
        }
        String companyId = userData.getCompanies().getFirst().getId();
        Pageable topTwenty = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        List<Invoice> latestInvoice = invoiceRepository.findByCompanyId(companyId, topTwenty).getContent();
        if (latestInvoice.isEmpty()) {
            return new ResponseEntity<>(latestInvoice, HttpStatus.NO_CONTENT);
        }
        List<InvoiceResponse> responseList = latestInvoice.stream()
                .map(invoiceResponseMapper::mapToResponse)
                .toList();
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    // get all invoice
    public ResponseEntity<?> getAllInvoice(String ownerId, String companyId) {
        Optional<Users> user = userRepository.findById(ownerId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("Create account!", HttpStatus.BAD_REQUEST);
        }
        Users userData = user.get();
        if (userData.getTotalCompany() == 0) {
            return new ResponseEntity<>("Create company account.", HttpStatus.BAD_REQUEST);
        }
        boolean isFind = false;
        List<Companies> companiesList = userData.getCompanies();
        for (Companies companies1 : companiesList) {
            if (companies1.getId().equals(companyId)) {
                isFind = true;
                break;
            }
        }
        if (!isFind) {
            return new ResponseEntity<>("This company not registered.", HttpStatus.BAD_REQUEST);
        }
        List<Invoice> allInvoice = invoiceRepository.findByCompanyId(companyId, Sort.by(Sort.Direction.DESC, "id"));
        if (allInvoice.isEmpty()) {
            return new ResponseEntity<>("No invoice found.", HttpStatus.NO_CONTENT);
        }
        List<InvoiceResponse> responseList = allInvoice.stream()
                .map(invoiceResponseMapper::mapToResponse)
                .toList();
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    // delete invoice by id
    public ResponseEntity<?> deleteInvoiceById(String invoiceId, String ownerId) {
        Optional<Invoice> findInvoice = invoiceRepository.findById(invoiceId);
        if (findInvoice.isEmpty()) {
            return new ResponseEntity<>("Invoice not found.", HttpStatus.NOT_FOUND);
        }
        String invoiceOwnerId = findInvoice.get().getCompany().getOwner().getId();
        if (!invoiceOwnerId.equals(ownerId)) {
            return new ResponseEntity<>("Not allowed to delete!", HttpStatus.UNAUTHORIZED);
        }
        invoiceRepository.deleteById(invoiceId);
        return new ResponseEntity<>("Invoice deleted successfully.", HttpStatus.OK);
    }

    // update or edit invoice
    public ResponseEntity<?> updateInvoice(InvoiceRequest newInvoice, String ownerId, String invoiceId) {
        Optional<Invoice> oldInvoice = invoiceRepository.findById(invoiceId);
        if (oldInvoice.isEmpty()) {
            return new ResponseEntity<>("Invoice not found.", HttpStatus.BAD_REQUEST);
        }
        Invoice oldDetails = oldInvoice.get();
        if (!oldDetails.getCompany().getOwner().getId().equals(ownerId)) {
            return new ResponseEntity<>("Not have permission to delete!", HttpStatus.UNAUTHORIZED);
        }
        String companyId = oldDetails.getCompany().getId();
        String invoiceNumber = oldDetails.getInvoiceDetails().getInvNumber();

        int grandTotal = newInvoice.getItemsDetails().stream()
                .mapToInt(item -> (int) (item.getQuantity() * item.getRate()))
                .sum();
        int paidAmount = newInvoice.getPaidAmount();
        int dueBalance = grandTotal - paidAmount;
        Invoice updatedInvoice = invoiceRequestMapper.mapToInvoice(newInvoice);
        updatedInvoice.setGrandTotal(grandTotal);
        updatedInvoice.setDueBalance(dueBalance);
        updatedInvoice.setCompany(oldDetails.getCompany());
        updatedInvoice.getInvoiceDetails().setInvNumber(oldDetails.getInvoiceDetails().getInvNumber());
        updatedInvoice.setId(oldDetails.getId());
        Invoice savedInvoice = invoiceRepository.save(updatedInvoice);
        return new ResponseEntity<>("Invoice updated successfully.", HttpStatus.OK);
    }

}
