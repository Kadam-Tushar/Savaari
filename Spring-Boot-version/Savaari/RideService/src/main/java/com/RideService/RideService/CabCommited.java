package com.RideService.RideService;

import javax.persistence.*;

import javax.annotation.processing.Generated;


@Entity
public class CabCommited{
    @Id
    Long cabId;
    Long rideId,destinationLoc,sourceLoc;

    CabCommited(Long cabId, Long rideId, Long destinationLoc,Long sourceLoc){
        this.cabId = cabId;
        this.rideId = rideId ;
        this.destinationLoc = destinationLoc; 
        this.sourceLoc = sourceLoc ;
    }

    CabCommited(){}

    

}