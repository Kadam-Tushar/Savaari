package com.Cab.Cab;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType; 
import javax.persistence.PersistenceContext ;
import java.util.*;

@RestController
public class Cab{
    CabtbRepository cabtbRep;
    CabAvailableRepository cabAvailableRep;
    CabCommitedRepository cabComittedRep;
    CabGivingRideRepository cabGivingRideRep;
    HashMap<Long,Boolean> interest = new HashMap<>() ; 

    static RestTemplate restTemplate = new RestTemplate();
    static String requestUrlride = "http://ride-service";
    static String ridePort = "8081";
    static String walletPort = "8082";

    @PersistenceContext
    private EntityManager em ;


    @Autowired
    public Cab(CabtbRepository cabtbRep,CabAvailableRepository cabAvailableRep,
        CabCommitedRepository cabComittedRep,CabGivingRideRepository cabGivingRideRep) {
        this.cabtbRep = cabtbRep;
        this.cabAvailableRep = cabAvailableRep;
        this.cabComittedRep = cabComittedRep;
        this.cabGivingRideRep = cabGivingRideRep;
    }

    
    
    @GetMapping("/")
  	public String home(){

		return "Started the Cab";

  	}
    // Check cab id valid function 
   
    public Long getCommitRideId(Long cabId){
        
        if(cabId == null || cabId < 0 ) return -1L;
        
        CabCommited c = cabComittedRep.findById(cabId);
        if(c == null){
            return -1L;
        }
        if(c.rideId < 0) return -1L;

        log("Returning CabService.getCommitedRideId rideId :"+c.rideId + "for  cabid "+cabId);

        return c.rideId;
        
    }
    public Long givingRideRideId(Long cabId){
        
        if(cabId == null || cabId < 0) return -1L;
        CabGivingRide cgr = cabGivingRideRep.findById(cabId);
        if(cgr == null){
            return -1L;
        }

        if(cgr.rideId < 0) return -1L;

        log("Returning givingRideRideID rideId :"+cgr.rideId + "for  cabid "+cabId);

        return cgr.rideId;
        
    }

    public boolean isInterested(Long cabId){
        if(cabId == null) return false;
        if(interest.containsKey(cabId)){
            boolean i = interest.get(cabId);
            interest.put(cabId,!i);
            return i ;
        }
        interest.put(cabId,false);
        return true ;
    }

    @RequestMapping(path = "/resetinterest", method = RequestMethod.GET)
    public boolean resetinterest(){

        interest = new HashMap<>();
        return true;
    }
    
    @Transactional 
    @RequestMapping(path = "/requestRide", method = RequestMethod.GET)
    public boolean requestRide(@RequestParam Long cabId,@RequestParam Long rideId,@RequestParam Long sourceLoc,@RequestParam Long destinationLoc){
                if(cabId == null || rideId == null || sourceLoc == null || destinationLoc == null) return false;
                if(sourceLoc < 0 || destinationLoc < 0 || cabId < 0 || rideId < 0) return false ;        

                
                log("got requestRide with cabId : "+cabId +"  rideId : "+rideId +" (src, dest) : "+sourceLoc+"," + destinationLoc);
                Cabtb ctb;
                CabAvailable ca;
                try{
                    ctb = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
                    ca = em.find(CabAvailable.class,cabId,LockModeType.PESSIMISTIC_WRITE);
                }
                catch(Throwable e){
                    log("lock not able to get maybe better luck next time !");
                    return false;
                }

                if( ctb != null && ca != null  && cabtbRep.existsById(cabId) && cabAvailableRep.existsById(cabId) ){
                    if(isInterested(cabId)){   
                        //updatations remove from available and then add new entry to commited state.

                        cabAvailableRep.deleteById(cabId);
                        cabComittedRep.saveAndFlush(new CabCommited(cabId,rideId,destinationLoc,sourceLoc));                    
                        log("returning true for requestRide "+cabId+" "+rideId);
                        return true ;
                    }
                }

            log("returning false for requestRide "+cabId+" "+rideId);
            return false;
    }


    @RequestMapping(path = "/rideStarted", method = RequestMethod.GET)
    @Transactional
    public boolean rideStarted(@RequestParam Long cabId,@RequestParam Long rideId){
        
        //    if (checkCabId(cabId) && commitRideId(cabId)  ==  rideId){
        //            //change state giving-ride of cab 
        //            return true ;
        //    }
        if(cabId == null || rideId == null || cabId < 0 || rideId < 0) return false ;

        Cabtb cabtb;
        CabCommited cc;
        try {
            cabtb = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
            cc = em.find(CabCommited.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return false;
        }

        log("rideStarted : "+cabId +" "+rideId +" request");
        if(cabId != null && cabtb!= null && (getCommitRideId(cabId) == rideId) && (cabId >= 0) && (rideId >= 0)){
            // removing from cabcommited state and adding to ride giving state
            // also updating last location of cabtb as source location from cab commited entry
            
            cabComittedRep.deleteById(cabId);
            //updating last location of cabtb
            cabtb.numRides++;
            cabtb.location = cc.sourceLoc;
            cabtbRep.saveAndFlush(cabtb);

            //Adding new entry in Giving Ride 
            cabGivingRideRep.saveAndFlush(new CabGivingRide(cabId,rideId,cc.destinationLoc,cc.sourceLoc));

            log("rideStarted : "+cabId +" "+rideId +" true ");
            return true ;
        }

        log("rideStarted : "+cabId +" "+rideId +" fail");
            return false ;
     }

    @RequestMapping(path = "/rideCanceled", method = RequestMethod.GET)
    @Transactional
    public boolean rideCanceled(@RequestParam Long cabId,@RequestParam Long rideId){
        // if (checkCabId(cabId) && commitRideId(cabId)  ==  rideId){
        //         //change state availabe-ride of cab 
        //         return true ;
        // }
        if(cabId == null || rideId == null || cabId < 0 || rideId < 0) return false ;
        log("rideCanceld  : "+cabId +" "+rideId +" request");
        
        Cabtb cabtb;
        CabCommited cc;
        try {
            cabtb = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
            cc = em.find(CabCommited.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return false;
        }

        if(cabtb != null  && cabtbRep.existsById(cabId) && (getCommitRideId(cabId) == rideId)){
            cabComittedRep.deleteById(cabId);
            cabAvailableRep.saveAndFlush(new CabAvailable(cabId));
            log("rideCanceld  : "+cabId +" "+rideId +" true");
            return true ;
        }

        log("rideCanceld  : "+cabId +" "+rideId +" false");
        return false;
    }

    @RequestMapping(path = "/rideEnded", method = RequestMethod.GET)
    @Transactional
    public boolean rideEnded(@RequestParam Long cabId,@RequestParam Long rideId){
        // if (checkCabId(cabId) && givingrideRideId(cabId)  ==  rideId){
        //         //change state availalbe   of cab and delete from giving ride
        //         // Call RideSerivce.rideEnded
        //         return true ;
        // }
        // update last location

        
        if(rideId == null || cabId == null) return false;
        if(rideId < 0 || cabId < 0) return false ;
        log("rideEnded : "+cabId +" "+rideId +" request");
        
        Cabtb c;
        CabCommited cc;
        try {
            c = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
            cc = em.find(CabCommited.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return false;
        }

        if(cabtbRep.existsById(cabId) && (givingRideRideId(cabId) == rideId)){

                // Updating destination as  Last location 
                CabGivingRide cgr = null;
                try {
                    cgr  =  em.find(CabGivingRide.class,cabId,LockModeType.PESSIMISTIC_WRITE);
                }
                catch(Throwable e){
                    log("lock not able to get maybe better luck next time !");
                    return false;
                }

                c.location = cgr.destinationLoc ; 
                cabtbRep.saveAndFlush(c);


                cabAvailableRep.saveAndFlush(new CabAvailable(cabId));
                cabGivingRideRep.deleteById(cabId);
                
                String rideEndedUrl = requestUrlride+":"+ridePort+"/rideEnded?rideId="+rideId;
                boolean response = restTemplate.getForObject(rideEndedUrl,Boolean.class);
                // Remaining ! Calling RidSerivce.rideEnded
                log("rideEnded : "+cabId +" "+rideId +" "+response);
                return response;
        }

        log("rideEnded : "+cabId +" "+rideId +" false" );
        return false;
    }

    @RequestMapping(path = "/signIn", method = RequestMethod.GET)
    @Transactional
    public boolean signIn(@RequestParam Long cabId,@RequestParam Long initialPos){
        // Check CabIdValid && checkSignOut(cabId) 
        // Add query 
         //Call RiderSerivice.cabSignIn consider results of cabSignIn too 
         // update last location and state too 
        if(cabId == null || initialPos == null || cabId < 0 || initialPos < 0) return false ;

        log("Cab.signIn : "+cabId +" "+initialPos +" req");
        Cabtb c=null;
        try {
            c= em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return false;
        }
        if(c != null && (initialPos >= 0)){
             // Chaning state in cabtb
            
            if(!c.state){
             
                //send request to RideService.cabSignsIn
                //if respose is true
                String cabSignsInUrl = requestUrlride+":"+ridePort+"/cabSignsIn?cabId="+cabId+"&initialPos="+initialPos;
                boolean response = restTemplate.getForObject(cabSignsInUrl,Boolean.class);
                if(response){
                    c.state = true;
                    c.location=initialPos ; 
                    c.numRides = 0L;
                    cabtbRep.saveAndFlush(c);

                    CabAvailable cav = new CabAvailable(cabId);
                    cabAvailableRep.saveAndFlush(cav);
                    log("Cab.signIn : "+cabId +" "+initialPos +" true");
                    return true;
                }     
            }
            else{
                log("Cab.signIn : "+cabId +" "+initialPos +" false becoz rideService.signIn returned false" );
            }
        }
        log("Cab.signIn : "+cabId +" "+initialPos +" false");
        return false;
    }


    @RequestMapping(path = "/signOut", method = RequestMethod.GET)
    @Transactional
    public boolean signOut(@RequestParam Long cabId){

        if(cabId == null || cabId < 0) return false ; 

        log("Cab.signOut : "+cabId +" request");
        Cabtb c = null;
        try {
            c = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return false;
        }
        if(c != null){  
           if(c.state){
            
                //send request to RideService.cabSignsIn
                //if respose is true
                String cabSignsOutUrl = requestUrlride+":"+ridePort+"/cabSignsOut?cabId="+cabId;
                boolean response = restTemplate.getForObject(cabSignsOutUrl,Boolean.class);
                if(response)
                {
                    cabAvailableRep.deleteById(cabId);
                    if(interest.containsKey(cabId)) interest.remove(cabId);
                    c.state = false;
                    c.numRides = 0L;
                    c.location = -1L;
                    cabtbRep.saveAndFlush(c);
                    log("Cab.signOut : "+cabId +" true");
                    return true;
                }      
            }
        }
        log("Cab.signOut : "+cabId +" false");
        return false;
    }

    @RequestMapping(path = "/numRides", method = RequestMethod.GET)
    @Transactional
    public Long numRides(@RequestParam Long cabId){
            // if cabId Invalid : return -1 
            /* if{signOut } return 0 
                // return  number of rides
            }
            */
        log("Cab.numRides : cabId = "+cabId);
        if(cabId == null || cabId < 0) return -1L; 
        
        Cabtb c;
        try {
            c = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return -1L;
        }
        if(c != null){
            
            log("Cab.numRides : numRides "+c.numRides);
            return c.numRides;
        }
        
        return -1L;
        
    }
    static void log(Object o ){
        System.out.println(o);
    }

}