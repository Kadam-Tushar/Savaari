package com.RideService.RideService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType; 
import javax.persistence.PersistenceContext ;
import java.io.IOException ; 

@RestController
public class RideService{
    CabtbRepository cabtbRep;
    CabAvailableRepository cabAvailableRep;
    CabCommitedRepository cabComittedRep;
    CabGivingRideRepository cabGivingRideRep;
    RidetbRepository ridetbRep;
    DummyRepository dummyRep;
    static final int  attempts = 3 ; 


    static RestTemplate restTemplate = new RestTemplate();
    static String requestUrlcab = "http://cab-service";
    static String requestUrlwallet = "http://wallet-service";
    static String cabPort = "8080";
    static String walletPort = "8082";

    @PersistenceContext
    private EntityManager em ;


    @Autowired
    public RideService(CabtbRepository cabtbRep, CabAvailableRepository cabAvailableRep,
        CabCommitedRepository cabComittedRep, CabGivingRideRepository cabGivingRideRep, RidetbRepository ridetbRep, DummyRepository dummyRep) {
        this.cabtbRep = cabtbRep;
        this.cabAvailableRep = cabAvailableRep;
        this.cabComittedRep = cabComittedRep;
        this.cabGivingRideRep = cabGivingRideRep;
        this.ridetbRep = ridetbRep;
        this.dummyRep = dummyRep;
    }

    @GetMapping("/")
  	public String home(){

		return "Started the RidService";

  	}

    @Transactional
    public Long getCommitRideId(@RequestParam Long cabId){
        if(cabId == null || cabId < 0 ) return -1L;

        CabCommited c = cabComittedRep.findById(cabId);
        
        if(c == null){
            return -1L;
        }
        if(c.rideId < 0) return -1L;
        log("Returning  RideService.getCommitedRideId rideId :"+c.rideId + "for  cabid "+cabId);
        return c.rideId;
        
    }
    public Long givingRideRideId(@RequestParam Long cabId){
        if(cabId == null || cabId < 0) return -1L;
        CabGivingRide cgr = cabGivingRideRep.findById(cabId);
        if(cgr == null){
            return -1L;
        }
        if(cgr.rideId < 0) return -1L;
        log("Returning givingRideRideID rideId :"+cgr.rideId + "for  cabid "+cabId);
        return cgr.rideId;
        
    }
    
    public boolean existsByRideId(Long rideId){
        if(rideId == null || rideId < 0 ) return false ;
        Iterable<CabGivingRide> it =  cabGivingRideRep.findAll();

        log("Exists by Ride ID  rideId :"+rideId +" request");
        for(CabGivingRide cgr : it){
            if(cgr.rideId == rideId){
                log("Exists by Ride ID  rideId :"+cgr.rideId +" Returning true ");
                return true ;
            }
        }
        log("Exists by Ride ID  rideId :"+rideId +" Returning false");
        return false;
    }

    // Not using anymore 
    public CabGivingRide findByRideId(Long rideId){
        if(rideId == null || rideId < 0 ) return null ;
        Iterable<CabGivingRide> it =  cabGivingRideRep.findAll();
        log("Findby rideId :"+rideId +" request");

        for(CabGivingRide cgr : it){
            if(cgr.rideId == rideId){
                return cgr ;
            }
        }
        return null;
    }

    @RequestMapping(path = "/rideEnded", method = RequestMethod.GET)
    @Transactional
    public boolean rideEnded(@RequestParam Long rideId){

        
        if(rideId  == null || rideId < 0 || (!(existsByRideId(rideId)))) return false;
        log("rideEnded : "+rideId +" request");

        //CabGivingRide cgr = findByRideId(rideId);
        //CabGivingRide cgr = em.find(CabGivingRide.class,rideId,LockModeType.PESSIMISTIC_WRITE);

        Iterable<CabGivingRide> it =  cabGivingRideRep.findAll();
        
        ArrayList<CabGivingRide> cabGivingList = new ArrayList<>();

        for(CabGivingRide cgr : it){
            CabGivingRide cg;
            try {
                cg = em.find(CabGivingRide.class,cgr.cabId,LockModeType.PESSIMISTIC_WRITE);
            }
            catch(Throwable e){
                log("lock not able to get maybe better luck next time !");
                return false;
            }
            if(cg != null ){
                cabGivingList.add(cg);
            }
        }
        CabGivingRide cgr = null ;
        for(CabGivingRide cg : cabGivingList){
            if(cg.rideId ==  rideId){
                cgr = cg ; 
                break ; 
            }
        }
        if(cgr == null){
            log("rideEnded : "+rideId +" fail due to no cabid for this rideId");
            return false; 
        }



        Long cabId = cgr.cabId;

        //Cabtb c = cabtbRep.findById(cabId);
        Cabtb c;
        try {
            c = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return false;
        }

        c.location = cgr.destinationLoc ; 
        cabtbRep.saveAndFlush(c);

        cabAvailableRep.saveAndFlush(new CabAvailable(cabId));
        cabGivingRideRep.deleteById(cabId);

        log("rideEnded : "+rideId +" true");
     
        return true ;

    }

    @RequestMapping(path = "/cabSignsIn", method = RequestMethod.GET)
    @Transactional
    public boolean cabSignsIn(@RequestParam Long cabId, @RequestParam Long initialPos){
        if(cabId == null || cabId < 0 || initialPos == null || initialPos < 0) return false ;

        log("RideService.CabsignIn : "+cabId +" "+initialPos +" req");
        Cabtb c;
        try {
            c = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return false;
        }
        if(c != null){
        
           //Cabtb c = cabtbRep.findById(cabId);
           
           if(!c.state){

               c.state = true;
               c.location=initialPos ; 
               cabtbRep.saveAndFlush(c);

               CabAvailable cav = new CabAvailable(cabId);
               cabAvailableRep.saveAndFlush(cav);
               log("RideService.CabsignIn : "+cabId +" "+initialPos +" true");
               return true;
                   
           }
           else{
            log("RideService.CabsignIn : "+cabId +" "+initialPos +" false becoz c.state = true ");
           }
       }
       log("RideService.CabsignIn : "+cabId +" "+initialPos +" false");
       return false;
    }

    @RequestMapping(path = "/cabSignsOut", method = RequestMethod.GET)
    @Transactional
    public boolean cabSignsOut(@RequestParam Long cabId){
        if(cabId == null || cabId < 0) return false ;

        
        log("RideService.CabSignOut : "+cabId + "request");
        Cabtb c;
        try {
            c = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return false;
        }
        if(c != null){                 //check if cabId is valid
            
           //Cabtb c = cabtbRep.findById(cabId);
           

           if(c.state){                                 //check if cab is in signIn sate
                if(cabAvailableRep.existsById(cabId)){  //check if the cab is in available state
                    cabAvailableRep.deleteById(cabId);

                    c.state = false;
                    c.location=-1L;
                    cabtbRep.saveAndFlush(c);
                    log("RideService.CabSignOut : "+cabId+ " true");
                    return true;
                }               
            }
        }
        log("RideService.CabSignOut : "+cabId+ " false");
        return false;
    }

    @RequestMapping(path = "/requestRide", method = RequestMethod.GET)
    @Transactional
    public String requestRide(@RequestParam Long custId, @RequestParam Long sourceLoc,@RequestParam Long destinationLoc){
            Dummy d = em.find(Dummy.class,true,LockModeType.PESSIMISTIC_WRITE);

            if(custId == null || sourceLoc == null || destinationLoc == null ) return "-1";
            if(custId < 0  || sourceLoc < 0 || destinationLoc < 0 ) return "-1";
        
            Iterable<CabAvailable> it = cabAvailableRep.findAll();
            ArrayList<CabAvailable> cabAvailList = new ArrayList<>();

            log("Request Ride  : "+custId+" src:"+ sourceLoc +"dest: "+destinationLoc +  "request");
            // Locking complete table of CabAvailable 
            for(CabAvailable c: it){
                CabAvailable ca;
                try {
                    ca= em.find(CabAvailable.class,c.cabId,LockModeType.PESSIMISTIC_WRITE);
                }
                catch(Throwable e){
                    log("lock not able to get maybe better luck next time !");
                    return "-1";
                }
                if(ca != null) cabAvailList.add(ca);
            }
            

            PriorityQueue<Long[]>  pq = new PriorityQueue<>((a,b)->a[1].compareTo(b[1]));

            for(CabAvailable c : cabAvailList){
                Long location = cabtbRep.findById(c.cabId).location;

                Long[] cabInfo = new Long[2];
                cabInfo[0]= c.cabId ;
                cabInfo[1] = Math.abs(sourceLoc - location); 
                pq.add(cabInfo);
            }
            Ridetb r = new Ridetb(custId,destinationLoc,sourceLoc);
            r = ridetbRep.saveAndFlush(r);
            
            for(int i=0;i<attempts;++i){
                Long[] cabInfo = pq.poll();
                if(cabInfo == null){
                    log("Request Ride  : "+custId+" src:"+ sourceLoc +"dest: "+destinationLoc +  "fails due to no cab avail");
                    return "-1";
                }
                /*
                send request to cab.reqeustRide 
                If resp == true  
                    call res = wallet.deductAmount 
                    if res  == true :
                         delete cab  from available 
                         add cab to cabGivingRide
                         call cab.rideStarted       
                         r.cabId = cabInfo[0]
                         ridetbRep.saveAndFlush(r);
                         return r.rideId
                    else:
                        call Cab.rideCanceled
                        return -1L
                

                */
                // HTTP Req to cab.requestRide 
                String requestRideUrl = requestUrlcab+":"+cabPort+"/requestRide?cabId="+cabInfo[0]+"&rideId="+r.rideId+"&sourceLoc="+sourceLoc+"&destinationLoc="+destinationLoc;
                boolean response = restTemplate.getForObject(requestRideUrl,Boolean.class);
                if (response){

                    // calling wallet.duductAmount
                    Long fare = (cabInfo[1] + Math.abs(destinationLoc-sourceLoc))*10;
                    String deductAmountUrl = requestUrlwallet+":"+walletPort+"/deductAmount?custId="+r.custId+"&amount="+fare;
                    response = restTemplate.getForObject(deductAmountUrl,Boolean.class);
                    if(response){
                        cabAvailableRep.deleteById(cabInfo[0]);
                        cabGivingRideRep.saveAndFlush(new CabGivingRide(cabInfo[0],r.rideId,destinationLoc,sourceLoc));

                        // call cab.rideStarted 
                        String rideStartedUrl = requestUrlcab+":"+cabPort+"/rideStarted?cabId="+cabInfo[0]+"&rideId="+r.rideId;
                        response = restTemplate.getForObject(rideStartedUrl,Boolean.class);
                        r.cabId = cabInfo[0];
                        ridetbRep.saveAndFlush(r);

                        Cabtb cabt;
                        try {
                            cabt = em.find(Cabtb.class,cabInfo[0],LockModeType.PESSIMISTIC_WRITE);
                        }
                        catch(Throwable e){
                            log("lock not able to get maybe better luck next time !");
                            return "-1";
                        }
                        cabt.location = sourceLoc;
                        cabtbRep.saveAndFlush(cabt);
                        log("Request Ride  : "+custId+" src:"+ sourceLoc +"dest: "+destinationLoc +  "true");
                        return r.rideId+" "+r.cabId+" "+fare;
                    }
                    else{
                        //calling cab.rideCanceld
                        String rideCanceledUrl = requestUrlcab+":"+cabPort+"/rideCanceled?cabId="+cabInfo[0]+"&rideId="+r.rideId;
                        response = restTemplate.getForObject(rideCanceledUrl,Boolean.class);
                        log("Request Ride  : "+custId+" src:"+ sourceLoc +"dest: "+destinationLoc +  "fails due to not enough balance");
                        return "-1";
                    }
                }
                


            }
            log("Request Ride  : "+custId+" src:"+ sourceLoc +"dest: "+destinationLoc +  "fails due to no cab avail from top 3");
        return "-1";
    }

    @RequestMapping(path = "/getCabStatus", method = RequestMethod.GET)
    @Transactional
    public String getCabStatus(@RequestParam Long cabId){
        if(cabId == null || cabId < 0) return "Invalid CabId";
        Cabtb c;
        try {
            c = em.find(Cabtb.class,cabId,LockModeType.PESSIMISTIC_WRITE);
        }
        catch(Throwable e){
            log("lock not able to get maybe better luck next time !");
            return "Cannot lock the cabId";
        }
        if(c == null) return "Invalid CabId ";

        String state = null;
        
        //Cabtb c = cabtbRep.findById(cabId);
        
        if(!c.state)
            return "signed-out -1";
        else
        {
                if(cabAvailableRep.existsById(cabId))
                    return "available "+c.location;

                if(cabComittedRep.existsById(cabId))
                    return "commited "+c.location;
        }
        Long rideId;
        CabGivingRide cgv = cabGivingRideRep.findById(cabId);
        rideId = cgv.rideId;
        Ridetb ride = ridetbRep.findById(rideId);
        return "giving-ride "+c.location+" "+ride.custId+" "+ride.destinationLoc;
    }
    
    @RequestMapping(path = "/reset", method = RequestMethod.GET)
    @Transactional
    public void reset(){
        //Sending  Cab.rideEnded requests to all cabs that are currently in giving-ride state

        String rideEndedUrl = "";
        boolean response = false;

        Iterable<CabGivingRide> it = cabGivingRideRep.findAll();

        for(CabGivingRide cgv : it){
            rideEndedUrl = requestUrlcab+":"+cabPort+"/rideEnded?cabId="+cgv.cabId+"&rideId="+cgv.rideId;
            response = restTemplate.getForObject(rideEndedUrl,Boolean.class);
        }



        //Sending Cab.signOut requests  to all cabs that are currently in sign-in state

        String signOutUrl = "";

        Iterable<Cabtb> it1 = cabtbRep.findAll();
      
        for(Cabtb cav : it1){
            if(cav.state){
                signOutUrl = requestUrlcab+":"+cabPort+"/signOut?cabId="+cav.cabId;
                response = restTemplate.getForObject(signOutUrl,Boolean.class);
            }
        }

        //resetting the interest array
        String resetinterest = requestUrlcab+":"+cabPort+"/resetinterest";
        response = restTemplate.getForObject(resetinterest,Boolean.class);

    }
    static void log(Object o){
        System.out.println(o);
    }

}