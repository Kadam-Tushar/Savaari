package com.RideService.RideService;

import javax.persistence.*;

import javax.annotation.processing.Generated;


@Entity
public class CabAvailable{
    @Id
  
    Long cabId ;
    CabAvailable(Long cabId){
        this.cabId = cabId;
    }

    CabAvailable(){}
}