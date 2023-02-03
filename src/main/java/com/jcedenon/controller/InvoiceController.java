package com.jcedenon.controller;

import com.jcedenon.model.Invoice;
import com.jcedenon.service.IInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final IInvoiceService service;

    @GetMapping
    public Mono<ResponseEntity<List<Invoice>>> findAll() {
        //public Mono<ResponseEntity<Flux<Invoice>>> findAll() {
        //return service.findAll(); // Flux<Invoice>

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


        /*Flux<Invoice> fx = service.findAll();
        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx));

         */
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Invoice>> findById(@PathVariable("id") String id){
        return service.findById(id)
                .map( e -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Invoice>> save(@Valid @RequestBody Invoice invoice, final ServerHttpRequest req){
        return service.save(invoice)
                .map( e -> ResponseEntity
                        .created(URI.create(req.getURI().toString().concat("/").concat(e.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Invoice>> update(@Valid @PathVariable ("id") String id, @RequestBody Invoice invoice){
        invoice.setId(id);

        Mono<Invoice> monoBody = Mono.just(invoice);
        Mono<Invoice> monoDB = service.findById(id);

        return monoDB.zipWith(monoBody, (db, inv) -> {
            db.setId(id);
            db.setClient(inv.getClient());
            db.setDescription(inv.getDescription());
            db.setItems(inv.getItems());
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

    private Invoice invoiceHateoas;
    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel> getHateoas(@PathVariable("id") String id){
        Mono<Link> link1 = linkTo(methodOn(InvoiceController.class).findById(id)).withSelfRel().toMono();

        //PRACTICA NO RECOMENDADA
        /*return service.findById(id) //Mono<Invoice>
                .flatMap( d -> {
                    this.invoiceHateoas = d;
                    return link1;
                })
                .map( lk -> EntityModel.of(invoiceHateoas, lk));*/

        //PRACTICA INTERMEDIA
        /*return service.findById(id)
                .flatMap( d -> link1.map( lk -> EntityModel.of(d, lk)));*/

        //PRACTICA IDEAL
        return service.findById(id)
                .zipWith(link1, EntityModel::of);
    }
}
