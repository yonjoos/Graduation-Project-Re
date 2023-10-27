package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "similarity")
public class VectorSimilarity {

    @Id @GeneratedValue
    private Long id;

    private Integer[] vectorA;
    private Integer[] vectorB;

    private double similarity;


    public VectorSimilarity(Integer[] VectorA, Integer[] VectorB){
        this.vectorA = VectorA;
        this.vectorB = VectorB;
    }

}