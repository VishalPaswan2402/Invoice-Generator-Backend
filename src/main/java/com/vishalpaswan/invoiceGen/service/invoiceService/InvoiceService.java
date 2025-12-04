package com.vishalpaswan.invoiceGen.service.invoiceService;

import com.vishalpaswan.invoiceGen.dto.requestDTO.InvoiceRequest;
import com.vishalpaswan.invoiceGen.service.invoiceService.invoiceServiceImp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final SaveInvoice saveInvoice;
    private final FetchInvoice fetchInvoice;
    private final Fetch20LatestInvoice fetch20LatestInvoice;
    private final FetchAllInvoice fetchAllInvoice;
    private final DestroyInvoice destroyInvoice;
    private final UpdatingInvoice updatingInvoice;
    private final FetchLazyInvoice fetchLazyInvoice;
    private final SearchInvoice searchInvoice;

    // save new invoice
    public ResponseEntity<?> saveNewInvoice(InvoiceRequest invoiceRequest, String companyOwnerId) {
        return saveInvoice.newInvoice(invoiceRequest, companyOwnerId);
    }

    // fetch invoice
    public ResponseEntity<?> getInvoiceById(String invoiceId, String userId) {
        return fetchInvoice.getInvoice(invoiceId, userId);
    }

    // fetch latest 20 invoice
    public ResponseEntity<?> getLatestInvoice(String ownerId) {
        return fetch20LatestInvoice.latestInvoice(ownerId);
    }

    // get all invoice
    public ResponseEntity<?> getAllInvoice(String ownerId, String companyId) {
        return fetchAllInvoice.allInvoices(ownerId, companyId);
    }

    // delete invoice by id
    public ResponseEntity<?> deleteInvoiceById(String invoiceId, String ownerId) {
        return destroyInvoice.removeInvoice(invoiceId, ownerId);
    }

    // update or edit invoice
    public ResponseEntity<?> updateInvoice(InvoiceRequest newInvoice, String ownerId, String invoiceId) {
        return updatingInvoice.makeUpdate(newInvoice, ownerId, invoiceId);
    }

    // lazy invoice fetch
    public ResponseEntity<?> pagewiseInvoiceFetch(String ownerId, String companyId, int page) {
        return fetchLazyInvoice.getLazyInvoiceFetch(ownerId, companyId, page);
    }

    // search invoice by query
    public ResponseEntity<?> searchFromInvoice(String searchQuery, String ownerId, String companyId) {
        return searchInvoice.searchInvoiceByQuery(searchQuery, ownerId, companyId);
    }

}
