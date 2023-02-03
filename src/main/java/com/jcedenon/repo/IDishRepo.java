package com.jcedenon.repo;

import com.jcedenon.model.Dish;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IDishRepo extends IGenericRepo<Dish, String> {
}
