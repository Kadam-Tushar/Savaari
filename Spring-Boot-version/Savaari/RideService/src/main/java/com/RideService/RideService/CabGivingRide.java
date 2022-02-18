package com.RideService.RideService;

import javax.persistence.*;

import javax.annotation.processing.Generated;


@Entity
public class CabGivingRide{
    @Id
    Long cabId;
    Long rideId,destinationLoc,sourceLoc;
    CabGivingRide(Long cabId,Long rideId,Long destinationLoc,Long sourceLoc){
        this.cabId = cabId;
        this.rideId = rideId ;
        this.destinationLoc = destinationLoc;
        this.sourceLoc = sourceLoc ;

    }
    CabGivingRide(){}

}