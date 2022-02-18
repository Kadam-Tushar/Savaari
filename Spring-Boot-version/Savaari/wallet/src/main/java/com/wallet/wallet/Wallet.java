package com.wallet.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.PersistenceContext ;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

@RestController
public class Wallet{
    CustomerRepository cusrep;

    @PersistenceContext
    private EntityManager em ;
    Long intialBalance = 0L;

    @Autowired
    public Wallet(CustomerRepository cusrep) {
        this.cusrep = cusrep;
    }

    @GetMapping("/")
  	public String home(){

		return "Started the Wallet";

  	}

    @RequestMapping(path = "/getBalance", method = RequestMethod.GET)

	public Long getBalance(@RequestParam Long custId){
        // Do this atomically - Remaining 
        if(custId == null || custId < 0 ) return -1L;

        Customer c = cusrep.findById(custId);
        if(c == null) return -1L ; 

        Long balance =  c.balanace;
        return balance;

  	}
    
    
    @RequestMapping(path = "/deductAmount", method = RequestMethod.GET)
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean deductAmount(@RequestParam Long custId,@RequestParam Long amount){
        // Do this atomically - Remaining 
        if(custId == null || amount == null) return false;
        if(custId < 0 || amount < 0 ) return false ;

        //Customer c = cusrep.findById(custId);
        Customer c = em.find(Customer.class,custId,LockModeType.PESSIMISTIC_WRITE);
        if(c == null){
            return false;
        }
        Long balance =  c.balanace;
        if(balance >= amount){
            c.balanace -= amount;
            try {
                cusrep.saveAndFlush(c);
            } catch (Throwable e) {
                e.printStackTrace();
                //TODO: handle exception
            }
            
            return true ;
        }
        return false ;
    }

    
    @RequestMapping(path = "/addAmount", method = RequestMethod.GET)
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean addAmount(@RequestParam Long custId,@RequestParam Long amount){
        // Do this atomically - Remaining 
        if(custId == null || custId < 0 || amount == null || amount < 0) return false ;
        
        //Customer c = cusrep.findById(custId);
        Customer c = em.find(Customer.class,custId,LockModeType.PESSIMISTIC_WRITE);
        if(c == null){
            return false;
        }

        Long balance =  c.balanace;
        c.balanace += amount;
        
        try {
            cusrep.saveAndFlush(c);    
        } catch (Throwable e) {
            e.printStackTrace();
            //TODO: handle exception
        }
        
        return true;
    }

    @GetMapping("/reset")
	public void reset(){

        intialBalance = MyCommandLineRunner.balances.get(0);

        Iterable<Customer> it = cusrep.findAll();

        for(Customer cus : it){
            cus.balanace = intialBalance;
            cusrep.saveAndFlush(cus);
        }
  	}
	
}