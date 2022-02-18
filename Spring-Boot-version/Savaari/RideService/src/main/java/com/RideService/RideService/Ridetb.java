package com.RideService.RideService;

import javax.persistence.*;

import javax.annotation.processing.Generated;


@Entity
public class Ridetb{
    @Id
  
    @GeneratedValue(
         strategy = GenerationType.SEQUENCE,generator = "mysqgen"
    )
    @SequenceGenerator(
        name = "mysqgen", initialValue = 1, allocationSize  = 1
    )
    Long rideId ;

    Long custId,destinationLoc,sourceLoc;

    Long cabId = -1L;
    Ridetb(Long custId, Long destinationLoc,Long sourceLoc){
        this.custId = custId;
        this.destinationLoc = destinationLoc;
        this.sourceLoc = sourceLoc ;

    }

    Ridetb(){}
}