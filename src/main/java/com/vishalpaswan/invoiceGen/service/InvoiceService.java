package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    // save invoice
    public ResponseEntity<?> saveNewInvoice(Invoice invoice){
        Invoice savedInvoice= invoiceRepository.save(invoice);
        return new ResponseEntity<>(savedInvoice,HttpStatus.CREATED);
    }

    // fetch invoice
    public ResponseEntity<?> getInvoiceById(String id){
        Optional<Invoice> invoice= invoiceRepository.findById(id);
        if(invoice.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(invoice,HttpStatus.OK);
    }

    // fetch latest 20 invoice
    public ResponseEntity<?> getLatestInvoice(){
        Pageable topTwenty= PageRequest.of(0,20,Sort.by(Sort.Direction.DESC,"id"));
        List<Invoice> latestInvoice= invoiceRepository.findAll(topTwenty).getContent();
        if(latestInvoice.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(latestInvoice,HttpStatus.OK);
    }

    // get all invoice
    public ResponseEntity<?> getAllInvoice(){
        List<Invoice> allInvoice= invoiceRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        if(allInvoice.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(allInvoice,HttpStatus.OK);
    }

    // delete invoice by id
    public ResponseEntity<?> deleteInvoiceById(String id){
        Optional<Invoice> findInvoice= invoiceRepository.findById(id);
        if(findInvoice.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        invoiceRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // update or edit invoice
    public ResponseEntity<?> updateInvoice(@RequestBody Invoice newInvoice,String id){
        Optional<Invoice> oldInvoice=invoiceRepository.findById(id);
        if(oldInvoice.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
//        System.out.println(oldInvoice);
        System.out.println("NEW Invoice");
        System.out.println(newInvoice);
        newInvoice.setId(id);
        Invoice updateInvoice=invoiceRepository.save(newInvoice);
        System.out.println("updated Invoice "+updateInvoice);
        return new ResponseEntity<>(updateInvoice,HttpStatus.OK);
    }

}
