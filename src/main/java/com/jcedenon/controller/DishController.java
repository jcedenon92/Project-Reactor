package com.jcedenon.controller;

import com.jcedenon.model.Dish;
import com.jcedenon.service.IDishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final IDishService service;

    @GetMapping
    public Mono<ResponseEntity<List<Dish>>> findAll() {
        //public Mono<ResponseEntity<Flux<Dish>>> findAll() {
        //return service.findAll(); // Flux<Dish>

        return service.findAll()
                .hasElements()
                .map( status -> {
                    if (status) {
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(service.findAll().collectList().block());
                    } else {
                        return ResponseEntity.noContent().build();
                    }
                });

        /*return service.findAll()
                .collectList()
                .map( list -> {
                    if (list.isEmpty()) {
                        return ResponseEntity.noContent().build();
                    } else {
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(list);
                    }
                });*/


        /*Flux<Dish> fx = service.findAll();
        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx));

         */
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Dish>> findById(@PathVariable("id") String id){
        return service.findById(id)
                .map( e -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Dish>> save(@Valid @RequestBody Dish dish, final ServerHttpRequest req){
        return service.save(dish)
                .map( e -> ResponseEntity
                        .created(URI.create(req.getURI().toString().concat("/").concat(e.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Dish>> update(@Valid @PathVariable ("id") String id, @RequestBody Dish dish){
        dish.setId(id);

        Mono<Dish> monoBody = Mono.just(dish);
        Mono<Dish> monoDB = service.findById(id);

        return monoDB.zipWith(monoBody, (db, b) -> {
            db.setId(id);
            db.setName(b.getName());
            db.setPrice(b.getPrice());
            db.setStatus(b.getStatus());
            return db;
        }).flatMap(service::update)
                .map( e -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable("id") String id){
        return service.findById(id)
                .flatMap(e -> service.deleteById(e.getId())
                    //.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                    .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private Dish dishHateoas;
    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel> getHateoas(@PathVariable("id") String id){
        Mono<Link> link1 = linkTo(methodOn(DishController.class).findById(id)).withSelfRel().toMono();

        //PRACTICA NO RECOMENDADA
        /*return service.findById(id) //Mono<Dish>
                .flatMap( d -> {
                    this.dishHateoas = d;
                    return link1;
                })
                .map( lk -> EntityModel.of(dishHateoas, lk));*/

        //PRACTICA INTERMEDIA
        /*return service.findById(id)
                .flatMap( d -> link1.map( lk -> EntityModel.of(d, lk)));*/

        //PRACTICA IDEAL
        return service.findById(id)
                .zipWith(link1, EntityModel::of);
    }
}
