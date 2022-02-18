package com.RideService.RideService;

import javax.persistence.*;

import javax.annotation.processing.Generated;

@Entity
 public class Cabtb{

    @Id
    // @GeneratedValue(
    //     strategy = GenerationType.SEQUENCE,generator = "mysqgen"
    // )
    // @SequenceGenerator(
    //     name = "mysqgen", initialValue = 1, allocationSize  = 1
    // )
    
    Long cabId;
    Long location ;

    boolean state= false;
    
    Cabtb(Long cabId, Long location){
        this.cabId = cabId;
        this.location = location ;
    }


    Cabtb(){}

    public String toString(){
        return "cabId : "+cabId + " state : "+state + " location : "+location;
    }
 }