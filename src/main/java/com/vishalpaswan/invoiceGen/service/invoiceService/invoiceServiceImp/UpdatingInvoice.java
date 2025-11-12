package com.vishalpaswan.invoiceGen.service.invoiceService.invoiceServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.InvoiceRequest;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceRequestMapper;
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
public class UpdatingInvoice {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceRequestMapper invoiceRequestMapper;

    private ResponseEntity<?> updateInvoice(InvoiceRequest newInvoice, String ownerId, String invoiceId) {
        try {
            if (ownerId == null || ownerId.isBlank() || invoiceId == null || invoiceId.isBlank() || newInvoice == null) {
                log.warn("Invalid request: ownerId or invoiceId or newInvoice is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID , Invoice ID and New Invoice are required.");
            }
            Optional<Invoice> oldInvoice = invoiceRepository.findById(invoiceId);
            if (oldInvoice.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Invoice not found.");
            }

            if (newInvoice.getItemsDetails() == null || newInvoice.getItemsDetails().isEmpty()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "No items found in updated invoice.");
            }

            Invoice oldDetails = oldInvoice.get();
            if (!oldDetails.getCompany().getOwner().getId().equals(ownerId)) {
                return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Not authorized to update this invoice!");
            }

            // Recalculate totals
            int grandTotal = newInvoice.getItemsDetails().stream()
                    .mapToInt(item -> (int) (item.getQuantity() * item.getRate()))
                    .sum();
            int paidAmount = newInvoice.getPaidAmount();
            int dueBalance = grandTotal - paidAmount;

            // Map and update fields
            Invoice updatedInvoice = invoiceRequestMapper.mapToInvoice(newInvoice);
            updatedInvoice.setGrandTotal(grandTotal);
            updatedInvoice.setDueBalance(dueBalance);
            updatedInvoice.setDueClear(dueBalance == 0 || newInvoice.isDueClear());
            updatedInvoice.setCompany(oldDetails.getCompany());
            updatedInvoice.getInvoiceDetails().setInvNumber(oldDetails.getInvoiceDetails().getInvNumber());
            updatedInvoice.setId(oldDetails.getId());

            invoiceRepository.save(updatedInvoice);

            log.info("Invoice {} successfully updated by owner {}", invoiceId, ownerId);
            return ResponseBuilder.success(HttpStatus.OK, "Invoice updated successfully.", null);

        } catch (DataAccessException ex) {
            log.error("Database error while updating invoice {} for owner {}: {}", invoiceId, ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while updating invoice.");

        } catch (Exception e) {
            log.error("Unexpected error while updating invoice {} for owner {}: {}", invoiceId, ownerId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while updating invoice. Please try again later.");
        }
    }

    public ResponseEntity<?> makeUpdate(InvoiceRequest newInvoice, String ownerId, String invoiceId) {
        return updateInvoice(newInvoice, ownerId, invoiceId);
    }
    
}
