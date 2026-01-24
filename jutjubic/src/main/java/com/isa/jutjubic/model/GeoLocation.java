package com.isa.jutjubic.model;
import jakarta.persistence.Embeddable;
import lombok.*;


@Embeddable      // lokacija je vrednost, ne entitet
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter @Setter
public class GeoLocation {

    private String city;
    private String country;
    private double latitude; //geo sirina
    private double longitude; //geo duzina
}
