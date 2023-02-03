package com.jcedenon.service.impl;

import com.jcedenon.model.Dish;
import com.jcedenon.repo.IDishRepo;
import com.jcedenon.repo.IGenericRepo;
import com.jcedenon.service.IDishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DishServiceImpl extends CRUDImpl<Dish, String> implements IDishService {

    private final IDishRepo repo;

    @Override
    protected IGenericRepo<Dish, String> getRepo() {
        return repo;
    }
}
