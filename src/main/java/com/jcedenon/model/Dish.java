package com.jcedenon.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Jorge Cedeño
 * @version 1.0l
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "dishes")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dish {

    @Id
    @EqualsAndHashCode.Include
    private String id; //ObjectId | BSON Binary JSON

    @Size(min = 3)
    @Field //(name = "name_x") //NO ES OBLIGATORIO
    private String name;
    @Field
    private Double price;
    @Field
    private Boolean status;

}
