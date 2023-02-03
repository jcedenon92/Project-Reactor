package com.jcedenon.handler;

import com.jcedenon.model.Dish;
import com.jcedenon.service.IDishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@RequiredArgsConstructor
public class DishHandler {

    private final IDishService service;

    public Mono<ServerResponse> findAll(ServerRequest req){

        /*return service.findAll()
                .hasElements()
                .flatMap( status -> {
                    if(status){
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(service.findAll()));
                    }else{
                        return ServerResponse.noContent().build();
                    }
                });*/

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Dish.class);
    }

    public Mono<ServerResponse> findById(ServerRequest req){
        String id = req.pathVariable("id");
        return service.findById(id)
                .flatMap( dish -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(dish))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest req){
        Mono<Dish> monoDish = req.bodyToMono(Dish.class);

        return monoDish
                .flatMap(service::save)
                .flatMap( dish -> ServerResponse
                        .created(URI.create(req.uri().toString().concat("/").concat(dish.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(dish))
                );
    }

    public Mono<ServerResponse> update(ServerRequest req){
        String id = req.pathVariable("id");

        Mono<Dish> monoDish = req.bodyToMono(Dish.class);
        Mono<Dish> monoDB = service.findById(id);

        return monoDB
                .zipWith(monoDish, (db, di) -> {
                    db.setId(id);
                    db.setName(di.getName());
                    db.setPrice(di.getPrice());
                    db.setStatus(di.getStatus());
                    return db;
                })
                .flatMap(service::update)
                .flatMap( dish -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(dish))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest req){
        String id = req.pathVariable("id");

        return service.findById(id)
                .flatMap( dish -> service.deleteById(dish.getId())
                        .then(ServerResponse.noContent().build())
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
