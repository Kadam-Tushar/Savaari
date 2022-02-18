package com.wallet.wallet;

import javax.persistence.*;

import javax.annotation.processing.Generated;

@Entity
 public class Customer{

    @Id
    // @GeneratedValue(
    //     strategy = GenerationType.SEQUENCE,generator = "mysqgen"
    // )
    // @SequenceGenerator(
    //     name = "mysqgen", initialValue = 1, allocationSize  = 1
    // )
    
    Long custId ;

    Long balanace ;
    public Customer(Long custId, Long balance){
        this.custId = custId;
        this.balanace = balance;
    }

    Customer(){}

    public String toString(){
        return "custID : "+custId + " Balanace : "+balanace;
    }
 }