package com.jcedenon.service.impl;

import com.jcedenon.model.Invoice;
import com.jcedenon.repo.IInvoiceRepo;
import com.jcedenon.repo.IGenericRepo;
import com.jcedenon.service.IInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl extends CRUDImpl<Invoice, String> implements IInvoiceService {

    private final IInvoiceRepo repo;

    @Override
    protected IGenericRepo<Invoice, String> getRepo() {
        return repo;
    }
}
