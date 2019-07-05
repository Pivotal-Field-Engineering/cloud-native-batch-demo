package io.pivotal.dragonstonefinance.tradesloader.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rating {
    private String value;
    private String analyst;
    private Date updateDateTime;
}
