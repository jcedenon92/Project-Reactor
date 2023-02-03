package com.jcedenon.service.impl;

import com.jcedenon.model.Client;
import com.jcedenon.repo.IClientRepo;
import com.jcedenon.repo.IGenericRepo;
import com.jcedenon.service.IClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl extends CRUDImpl<Client, String> implements IClientService {

    private final IClientRepo repo;

    @Override
    protected IGenericRepo<Client, String> getRepo() {
        return repo;
    }
}
