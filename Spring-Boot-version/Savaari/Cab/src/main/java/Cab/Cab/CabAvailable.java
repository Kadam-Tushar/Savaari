package com.Cab.Cab;

import javax.persistence.*;

import javax.annotation.processing.Generated;


@Entity
public class CabAvailable{
    @Id
  
    Long cabId ;
    CabAvailable(Long id){
        this.cabId = id;
    }


    CabAvailable(){}

}