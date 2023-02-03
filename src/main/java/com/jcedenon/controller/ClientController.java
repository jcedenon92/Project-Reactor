package com.jcedenon.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jcedenon.model.Client;
import com.jcedenon.service.IClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cloudinary.json.JSONObject;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final IClientService service;

    @GetMapping
    public Mono<ResponseEntity<Flux<Client>>> findAll() {
        //return service.findAll(); // Flux<Client>
        Flux<Client> fx = service.findAll();

        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx));
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Client>> findById(@PathVariable("id") String id){
        return service.findById(id)
                .map( e -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                    )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Client>> save(@Valid @RequestBody Client client, final ServerHttpRequest req){
        return service.save(client)
                .map( e -> ResponseEntity
                        .created(URI.create(req.getURI().toString().concat("/").concat(e.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Client>> update(@Valid @PathVariable ("id") String id, @RequestBody Client client){
        client.setId(id);

        Mono<Client> monoBody = Mono.just(client);
        Mono<Client> monoDB = service.findById(id);

        return monoDB.zipWith(monoBody, (db, c) -> {
            db.setId(id);
            db.setFirstName(c.getFirstName());
            db.setLastName(c.getLastName());
            db.setBirthDate(c.getBirthDate());
            db.setUrlPhoto(c.getUrlPhoto());
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

    private Client clientHateoas;
    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel> getHateoas(@PathVariable("id") String id){
        Mono<Link> link1 = linkTo(methodOn(ClientController.class).findById(id)).withSelfRel().toMono();

        //PRACTICA NO RECOMENDADA
        /*return service.findById(id) //Mono<Client>
                .flatMap( d -> {
                    this.clientHateoas = d;
                    return link1;
                })
                .map( lk -> EntityModel.of(clientHateoas, lk));*/

        //PRACTICA INTERMEDIA
        /*return service.findById(id)
                .flatMap( d -> link1.map( lk -> EntityModel.of(d, lk)));*/

        //PRACTICA IDEAL
        return service.findById(id)
                .zipWith(link1, EntityModel::of);
    }

    /////////////////////////////////////////
    @PostMapping("v1/upload/{id}")
    public Mono<ResponseEntity<Client>> uploadV1(@PathVariable ("id") String id, @RequestPart("file") FilePart file)throws Exception{
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dnbpobzmn",
                "api_key", "969631313279787",
                "api_secret", "y15cCcRT2l455ZN7xhR075MNHiU"
        ));

        File f = Files.createTempFile("temp", file.filename()).toFile();

        return file.transferTo(f)
                .then(service.findById(id)
                        .flatMap( c -> {
                            Map response;
                            try {
                                response = cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));
                                JSONObject json = new JSONObject(response);
                                String url = json.getString("url");
                                c.setUrlPhoto(url);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return service.update(c).thenReturn(ResponseEntity.ok().body(c));
                        })
                        .defaultIfEmpty(ResponseEntity.notFound().build())
                );
    }

    @PostMapping("v2/upload/{id}")
    public Mono<ResponseEntity<Client>> uploadV2(@PathVariable("id") String id, @RequestPart("file") FilePart file) throws Exception{
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dnbpobzmn",
                "api_key", "969631313279787",
                "api_secret", "y15cCcRT2l455ZN7xhR075MNHiU"
        ));

        return service.findById(id)
                .flatMap( c -> {
                    try{
                        File f = Files.createTempFile("temp", file.filename()).toFile();
                        file.transferTo(f).block();

                        Map response = cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));
                        JSONObject json = new JSONObject(response);
                        String url = json.getString("url");

                        c.setUrlPhoto(url);

                        return service.update(c).thenReturn(ResponseEntity.ok().body(c));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
