package io.pivotal.dragonstonefinance.ratingsapi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rating {
    @Id
    private String symbol;
    private String value;
    private String analyst;
    private Date updateDateTime;
}
