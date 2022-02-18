package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import jdk.javadoc.internal.tool.Start;

import java.util.*;
public class RideService extends AbstractBehavior<RideService.Command> {

    HashMap<String,Long[]> cacheTable = new HashMap<>();
    // cacheTable - to store state of cabId  Key : cabId  value : Long[2], 0: state of cab and 1:location of cab 
    /* 0: Available
     * 1: Commited
     * 2: Giving-Ride
     * 3: SignedOut
     * */

    HashMap<String,RequestRide> fullFillActorList = new HashMap<>();

    ActorRef<FullfillRide.Command> FullfillRideActorRef;
    public final ActorContext<Command> context;


    public static Behavior<Command> create() {
	    return Behaviors.setup(context -> new RideService(context));
	}

    public RideService(ActorContext<Command> context){
        super(context);
        this.context = context; 
    }

    //=======================================================================================================================

    public interface Command{}
    
    public static final class CabSignsIn implements Command {
        String cabId;
        Long initialPos;

        

        public CabSignsIn(String cabId,Long initialPos) {
           this.cabId = cabId;
           this.initialPos = initialPos;

        }
    }
    public static final class CabSignsOut implements Command {
        String cabId;
        public CabSignsOut(String cabId) {
           this.cabId = cabId;
        }
    }

    public static final class RequestRide implements Command {
        String custId;
        Long sourceLoc;
        Long destinationLoc;
        ActorRef<RideService.RideResponse> replyTo;

        public RequestRide(String custId,Long sourceLoc, Long destinationLoc,
                                            ActorRef<RideService.RideResponse> replyTo) {
            this.custId = custId;
            this.sourceLoc = sourceLoc;
            this.destinationLoc = destinationLoc;
            this.replyTo = replyTo;
        }
    }

    public static final class RideResponse implements Command {
        Long rideId,fare;
        String cabId;
        ActorRef<FullfillRide.Command> replyTo;
        
        public RideResponse(Long rideId, String cabId,Long fare,
                                    ActorRef<FullfillRide.Command> replyTo) {
            this.rideId = rideId;
            this.cabId = cabId;
            this.fare = fare ; 
            this.replyTo = replyTo;
        }

        @Override
        public boolean equals(Object o) {
            RideResponse r = (RideResponse) o;
            
            if(r.cabId.equals(this.cabId) && r.fare.equals(this.fare))
                return true;
            return false;
        }
    }

    public static final class UpdateCacheTable implements Command {
        String cabId;
        Long state; 
        Long position;
        Long time;
        public UpdateCacheTable(String cabId,Long state, Long position, Long time) {
            this.cabId = cabId;
            this.state = state;
            this.position = position;
            this.time = time;
        }
    }

    public static final class RideEndedResponse implements Command {
        String cabIdAvailable; 
        Long position;
        public RideEndedResponse(String cabIdAvailable,Long position) {
            this.cabIdAvailable= cabIdAvailable;
            this.position = position;
        }
    }


 
    public static class Started implements Command{
        public final String msg;
        public Started(String msg){
            this.msg = msg ; 
        }

        @Override
        public boolean equals(Object o) {
            Started s = (Started) o;

            return s.msg.equals(this.msg);
        }

    }
    public static final class Reset implements Command {
        ActorRef<Started> replyTo;
        public Reset(ActorRef<Started> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static final class ResetPropogate implements Command {
        public ResetPropogate() {}
    }

    public static final class NumRideResponceWrapped implements Command {
        Cab.NumRideResponse n;

        public NumRideResponceWrapped(Cab.NumRideResponse n) {
            this.n = n;
        }
    }

    public static final class GetCabStatus implements Command {
        String cabId;
        ActorRef<Started> replyTo;
        public GetCabStatus(String cabId,ActorRef<Started> replyTo) {
            this.cabId = cabId;
            this.replyTo= replyTo;
        }
    }
    //interface RideServiceResponse {}


    //Testing
    //=======================================================================================================================================
    
    //=======================================================================================================================================
    //=======================================================================================================================

    @Override
	public Receive<RideService.Command> createReceive() {
		ReceiveBuilder<RideService.Command> builder = newReceiveBuilder();
		return builder
                .onMessage(RequestRide.class, this::onRequestRide)
                .onMessage(CabSignsIn.class, this::onCabSignsIn)
                .onMessage(CabSignsOut.class, this::onCabSignsOut)
                .onMessage(RideResponse.class, this::onRideResponse)
                .onMessage(UpdateCacheTable.class, this::onUpdateCacheTable)
                .onMessage(Reset.class, this::onReset)
                .onMessage(ResetPropogate.class, this::onResetPropogate)
                .onMessage(NumRideResponceWrapped.class, this::onNumRideResponceWrapped)
                .onMessage(RideEndedResponse.class, this::onRideEndedResponse)
                .onMessage(GetCabStatus.class, this::onGetCabStatus)
                .build();
	}
    public Behavior<RideService.Command> onRequestRide(RequestRide requestRide){
        // Long rideId;
        // Long sourceLoc;
        // Long destinationLoc;
        // ActorRef<FullfillRide.Command> replyTo;

        //checks to verify valid parameters 
        pn("Got RequestRide on: "+context.getSelf().path().name());

        pn("Got RideService.RequestRide call with custId:"+requestRide.custId+"  source: "+requestRide.sourceLoc+" dest: "+requestRide.destinationLoc+" sourceLoc: "+requestRide.sourceLoc+" destinationLoc: "+requestRide.destinationLoc);
        if(!Globals.wallets.containsKey(requestRide.custId) || requestRide.sourceLoc <0 || requestRide.destinationLoc < 0){
            pn("Invalid Parameters to RideService.RequestRide");
            return this;
        }
        Long rideId = Globals.rideIdCounter.getAndIncrement();
        pn("RideId Generated : "+rideId);

        ActorRef<FullfillRide.Command> fullFillRideRef =  getContext().spawn(FullfillRide.create(requestRide,cacheTable,rideId,context.getSelf()),"FullFillRide_"+rideId);
        pn("Generated the FULLFILLRIDEACTOR");

        fullFillActorList.put("FullFillRide_"+rideId,requestRide);
        pn("\nThe FULLFILLACTORLIST is as follows\n"+fullFillActorList);

        fullFillRideRef.tell(new FullfillRide.AllocateRide());
        
        return this;
    }

    public Behavior<RideService.Command> onCabSignsIn(CabSignsIn cabSignsIn){
        long time = Globals.timeStamp.getAndIncrement();

        // Adding (cabId,state,location) - > (cabId,3(signout),-1)
        pn("Got RideService.CabSignsIn call on cabId:"+cabSignsIn.cabId +" initialPos:"+cabSignsIn.initialPos);
        //make cab available with initialPos
      
        cacheTable.put(cabSignsIn.cabId,new Long[]{0L,cabSignsIn.initialPos,time});
        pn("Added in the cache table: cabId:"+cabSignsIn.cabId +" initialPos:"+cabSignsIn.initialPos);
        // insert code for propagating changes in other RideService Actors
        for(Long i=0L; i<10L; i++) {
            if(i == Long.parseLong(context.getSelf().path().name().split("_")[1]))
                continue; 
            Globals.rideService.get(i).tell(new RideService.UpdateCacheTable(cabSignsIn.cabId,0L,cabSignsIn.initialPos,time));
        }

        return this ;

    }
    public Behavior<RideService.Command> onRideEndedResponse(RideEndedResponse rideEndedResponse) {
        long time = Globals.timeStamp.getAndIncrement();

        cacheTable.put(rideEndedResponse.cabIdAvailable+"",new Long[]{0L,rideEndedResponse.position,time});

        //propagate this msg to all nodes
        for(Long i=0L; i<10L; i++) {
            if(i == Long.parseLong(context.getSelf().path().name().split("_")[1]))
                continue;
            pn("Sending onRideEndedResponse for CabSignOut for: RideService"+i);
            Globals.rideService.get(i).tell(new RideService.UpdateCacheTable(rideEndedResponse.cabIdAvailable,0L,rideEndedResponse.position,time));
        }
        return this ;
    }

    public Behavior<RideService.Command> onUpdateCacheTable(UpdateCacheTable updateCacheTable) {

        if(cacheTable.get(updateCacheTable.cabId)[2] < updateCacheTable.time)
            cacheTable.put(updateCacheTable.cabId+"",new Long[]{updateCacheTable.state, updateCacheTable.position, updateCacheTable.time});

        pn("Got update cache table for: "+context.getSelf().path().name());
        return this;
    }

    public Behavior<RideService.Command> onCabSignsOut(CabSignsOut cabSignsOut){
        long time = Globals.timeStamp.getAndIncrement();

        pn("Got RideService.SignOut call on cabId:"+cabSignsOut.cabId);
        //make cab sign out and location = -1 
        cacheTable.put(cabSignsOut.cabId,new Long[]{3L,-1L,time});

        // insert code for propagating changes in other RideService Actors
        for(Long i=0L; i<10L; i++) {
            if(i == Long.parseLong(context.getSelf().path().name().split("_")[1]))
                continue; 
            pn("Sending updatecachetable for CabSignOut for: RideService"+i);
            Globals.rideService.get(i).tell(new RideService.UpdateCacheTable(cabSignsOut.cabId,3L,-1L,time));
            
        }

        return this ;
    }
    public Behavior<RideService.Command> onRideResponse(RideResponse rideResponse){
        long time = Globals.timeStamp.getAndIncrement();

        pn("Got RideResponse on: "+context.getSelf().path().name());

        String fullFillActorName = rideResponse.replyTo.path().name();
        pn("THIS is the FULLFILLACTORNAME: "+fullFillActorName);
        if(rideResponse.rideId == -1){
            pn("Got -1 for requestRide from "+fullFillActorName);
            return this;
        }
        pn("RideStarted with rideId:"+rideResponse.rideId+" cabId: "+rideResponse.cabId +" fare: "
                                                        +rideResponse.fare +" with" +fullFillActorName);
                                                
        pn("\nThe FULLFILLACTORLIST is as follows\n"+fullFillActorList);
        Long sourceLoc = fullFillActorList.get(fullFillActorName).sourceLoc;

        cacheTable.put(rideResponse.cabId+"",new Long[]{2L,sourceLoc,time});

        // propagate this changes to all RideService actors 
        for(Long i=0L; i<10L; i++) {
            if(i == Long.parseLong(context.getSelf().path().name().split("_")[1]))
                continue; 
            pn("Sending onRideResponse for CabSignOut for: RideService"+i);
            Globals.rideService.get(i).tell(new RideService.UpdateCacheTable(rideResponse.cabId,2L,sourceLoc,time));
        }

        // send response to testProbe actor too

       return this ;
    }
    

    public Behavior<RideService.Command> onReset(Reset reset){
        long time = Globals.timeStamp.getAndIncrement();
        ActorRef<Started> client = reset.replyTo;

        ActorRef<Cab.NumRideResponse> numRideResponceWrapped = 
                context.messageAdapter(Cab.NumRideResponse.class, NumRideResponceWrapped::new );

        for(String cabId : Globals.cabs.keySet()) {

            Globals.cabs.get(cabId).tell(new Cab.Reset(numRideResponceWrapped));

        }

        for(String x : Globals.cabs.keySet()){
            cacheTable.put(x,new Long[]{3L,-1L,time});
        }

        for(Long i=0L; i<10L; i++) {
            if(i == Long.parseLong(context.getSelf().path().name().split("_")[1]))
                continue; 
            Globals.rideService.get(i).tell(new RideService.ResetPropogate());
        }
        pn("Reset complete on RideService: "+Long.parseLong(context.getSelf().path().name().split("_")[1]));

        reset.replyTo.tell(new Started("Done"));

        return this;
    }

    public Behavior<RideService.Command> onResetPropogate(ResetPropogate resetPropogate) {
        long time = Globals.timeStamp.getAndIncrement();
        for(String x : Globals.cabs.keySet()){
            cacheTable.put(x,new Long[]{3L,-1L,time});
        }

        pn("ResetPropogate complete on RideService: "+Long.parseLong(context.getSelf().path().name().split("_")[1]));
        return this;
    }

    public Behavior<RideService.Command> onNumRideResponceWrapped(NumRideResponceWrapped n) {
        return this;
    }

    public Behavior<RideService.Command> onGetCabStatus(GetCabStatus g) {
        //Return the status of the cabId inside g
        //i.e.
        //"available", "commited", "giving-ride", "signed-out"
        pn("Message getCabStatus recieved for "+g.cabId);
        pn("Cache Table: "+cacheTable);
        if((long)cacheTable.get(g.cabId)[0] == 0) {
            g.replyTo.tell(new Started("available"));
            pn("Sending available for RideService.GetVabStatus: "+g.cabId);
        }
        else if(cacheTable.get(g.cabId)[0] == 1) {
            g.replyTo.tell(new Started("commited"));
            pn("Sending commited for RideService.GetVabStatus: "+g.cabId);
        }
        else if(cacheTable.get(g.cabId)[0] == 2) {
            g.replyTo.tell(new Started("giving-ride"));
            pn("Sending giving-ride for RideService.GetVabStatus: "+g.cabId);
        }
        else if(cacheTable.get(g.cabId)[0] == 3) {
            g.replyTo.tell(new Started("signed-out"));
            pn("Sending signed-out for RideService.GetVabStatus: "+g.cabId);
        }

        return this;
    }

    static void pn(Object o){
        System.out.println(o);
    }


    //Testing
    //=======================================================================================================================================
    

    //=======================================================================================================================================
}
