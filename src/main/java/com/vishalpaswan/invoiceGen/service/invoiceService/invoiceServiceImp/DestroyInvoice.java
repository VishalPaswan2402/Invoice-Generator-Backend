package com.vishalpaswan.invoiceGen.service.invoiceService.invoiceServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestroyInvoice {
    private final InvoiceRepository invoiceRepository;

    private ResponseEntity<?> deleteInvoiceById(String invoiceId, String ownerId) {
        try {
            if (ownerId == null || ownerId.isBlank() || invoiceId == null || invoiceId.isBlank()) {
                log.warn("Invalid request: ownerId or invoiceId is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID and Invoice ID are required.");
            }
            Optional<Invoice> findInvoice = invoiceRepository.findById(invoiceId);
            if (findInvoice.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Invoice not found.");
            }

            String invoiceOwnerId = findInvoice.get().getCompany().getOwner().getId();
            if (!invoiceOwnerId.equals(ownerId)) {
                return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Not allowed to delete!");
            }

            invoiceRepository.deleteById(invoiceId);
            log.info("Invoice with ID {} deleted successfully by owner {}", invoiceId, ownerId);
            return ResponseBuilder.success(HttpStatus.OK, "Invoice deleted successfully.", null);

        } catch (DataAccessException ex) {
            log.error("Database error while deleting invoice {} for owner {}: {}", invoiceId, ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while deleting invoice.");

        } catch (Exception e) {
            log.error("Unexpected error while deleting invoice {} for owner {}: {}", invoiceId, ownerId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while deleting the invoice. Please try again later.");
        }
    }

    public ResponseEntity<?> removeInvoice(String invoiceId, String ownerId) {
        return deleteInvoiceById(invoiceId, ownerId);
    }
    
}
