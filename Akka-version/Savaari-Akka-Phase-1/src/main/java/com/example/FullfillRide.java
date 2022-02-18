package pods.cabs;

import java.util.*;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
public class FullfillRide extends AbstractBehavior<FullfillRide.Command> {

    private final ActorContext<Command> context;
    
    RideService.RequestRide requestRide ;
    HashMap<String,Long[]> cacheTable;
    LinkedList<Long[]> top3cabs = new LinkedList<>();
    Long rideId;
    Long[] choosenCabInfo = new Long[2]; 
    ActorRef<RideService.Command> replyTo;
 
    public static Behavior<Command> create(RideService.RequestRide r,HashMap<String,Long[]> cacheTable,Long rideId, ActorRef<RideService.Command> replyTo) {
	    return Behaviors.setup(context -> new FullfillRide(context,r,cacheTable,rideId,replyTo));
	}


    public FullfillRide(ActorContext<Command> context,RideService.RequestRide r , HashMap<String,Long[]> cacheTable,Long rideId, ActorRef<RideService.Command> replyTo){
        super(context);
        this.context = context ;
        this.requestRide = r ;
        this.cacheTable = cacheTable;
        this.rideId = rideId; 
        this.replyTo = replyTo;
        
    }
    
    //=======================================================================================================================

    public interface Command{}
    
    public static final class RequestRideResponse implements Command {
        boolean response;

        public RequestRideResponse(boolean response) {
            this.response = response;
        }
    }
    
    public static final class RideEnded implements Command {

        public RideEnded() {

        }

    }
    public static final class AllocateRide implements Command {
        public AllocateRide() {}
    }

    static class ResponseBalanceWrapped implements Command {
        Wallet.ResponseBalance responseBalance;
        
        public ResponseBalanceWrapped(Wallet.ResponseBalance responseBalance) {
            this.responseBalance = responseBalance;
        }

        @Override
        public boolean equals(Object o) {
            ResponseBalanceWrapped r = (ResponseBalanceWrapped) o;
            return r.responseBalance.balance.equals(responseBalance.balance);
        }

    }

    //=======================================================================================================================

    @Override
	public Receive<FullfillRide.Command> createReceive() {
		ReceiveBuilder<FullfillRide.Command> builder = newReceiveBuilder();
		return builder
                .onMessage(RequestRideResponse.class, this::onRequestRideResponse)
                .onMessage(RideEnded.class, this::onRideEnded)
                .onMessage(AllocateRide.class, this::onAllocateRide)
                .onMessage(ResponseBalanceWrapped.class, this::onResponseBalanceWrapped)
//                .onMessage(Wallet.ResponseBalance.class, this::onResponseBalanceWrapped)
				.build();
	}

    public Behavior<FullfillRide.Command> onAllocateRide(AllocateRide allocateRide){
        pn("Called AllocateRide  ");
        PriorityQueue<Long[]>  pq = new PriorityQueue<>((a,b)->a[1].compareTo(b[1]));

        for(String cabId : cacheTable.keySet()){
            Long[] stateAndLocation = cacheTable.get(cabId);
                if(stateAndLocation[0] ==  0){
                Long[] cabInfo = new Long[2];
                cabInfo[0]=  Long.parseLong(cabId) ;
                cabInfo[1] = Math.abs(requestRide.sourceLoc - stateAndLocation[1]); 
                pq.add(cabInfo);
            }
        }
        int attempts = 3 ;
        
        while(!pq.isEmpty() && attempts > 0){
            Long[] cabInfo = pq.poll();
            if(cabInfo == null){
                break ;
            }
            else{
                top3cabs.add(cabInfo);
            }
            attempts--;
        }
        if(top3cabs.size() ==  0){
            pn("Request Ride  : "+requestRide.custId+" src:"+ requestRide.sourceLoc +"dest: "+requestRide.destinationLoc +  "fails due to no cab avail");
            requestRide.replyTo.tell(new RideService.RideResponse(-1L, "-1", 0L,context.getSelf()));
            replyTo.tell(new RideService.RideResponse(-1L, "-1", 0L,context.getSelf()));

            return Behaviors.stopped();

        }

        Long[] cabInfo = top3cabs.peekFirst();
        Globals.cabs.get(cabInfo[0]+"").tell(new Cab.RequestRide(rideId, requestRide.sourceLoc, requestRide.destinationLoc,context.getSelf()));


        return this;
    }
   
    public Behavior<FullfillRide.Command> onRequestRideResponse(RequestRideResponse requestRideResponse) {
        //no going to be used
        Long[] cabInfo =  top3cabs.pollFirst();
        if(!requestRideResponse.response) {
            if(top3cabs.size() == 0){
                pn("Cab"+cabInfo[0]+" not available and no further cabs in top3");

                // tell RideService no rides avail  
                requestRide.replyTo.tell(new RideService.RideResponse(-1L,cabInfo[0]+"",0L,context.getSelf()));
                replyTo.tell(new RideService.RideResponse(-1L,cabInfo[0]+"",0L,context.getSelf()));

                return Behaviors.stopped();
            }
            else{
                Long[] cabInfoNext = top3cabs.peekFirst();
                pn("Cab"+cabInfo[0]+" not available and checking next cab in top3 : cabId :"+cabInfoNext[0]);
                
                pn("rideId : "+rideId+" requestRide.sourceLoc : "+requestRide.sourceLoc+" requestRide.destinationLoc : "+requestRide.destinationLoc);
                Globals.cabs.get(cabInfoNext[0]+"").tell(new Cab.RequestRide(rideId, requestRide.sourceLoc, requestRide.destinationLoc,context.getSelf()));
            }
        }
        else{
            Long toDeduct = (cabInfo[1] + Math.abs(requestRide.destinationLoc-requestRide.sourceLoc))*10;
            pn("Cab "+cabInfo[0]+" Available deducting balance fare:"+toDeduct);
            choosenCabInfo[0] = cabInfo[0];
            choosenCabInfo[1] = cabInfo[1];

            ActorRef<Wallet.ResponseBalance> responseBalanceWrapped = 
                context.messageAdapter(Wallet.ResponseBalance.class, ResponseBalanceWrapped::new );

            Globals.wallets.get(requestRide.custId).tell(new Wallet.DeductBalance(toDeduct, responseBalanceWrapped));
        }
        return this;
    }

    public Behavior<FullfillRide.Command> onResponseBalanceWrapped(ResponseBalanceWrapped responseBalanceWrapped) {
        if(responseBalanceWrapped.responseBalance.balance == -1L){
            pn("Not enough balance for rideId "+rideId);
            Globals.cabs.get(choosenCabInfo[0]+"").tell(new Cab.RideCanceled(rideId));

            requestRide.replyTo.tell(new RideService.RideResponse(-1L,choosenCabInfo[0]+"",0L,context.getSelf()));
            replyTo.tell(new RideService.RideResponse(-1L,choosenCabInfo[0]+"",0L,context.getSelf()));
            return Behaviors.stopped();
        }
        else{
            Globals.cabs.get(choosenCabInfo[0]+"").tell(new Cab.RideStarted(rideId));
            Long fare = (choosenCabInfo[1] + Math.abs(requestRide.destinationLoc-requestRide.sourceLoc))*10;

            requestRide.replyTo.tell(new RideService.RideResponse(rideId, choosenCabInfo[0]+"",fare, context.getSelf()));
            replyTo.tell(new RideService.RideResponse(rideId, choosenCabInfo[0]+"",fare, context.getSelf()));
        }
        return this;
    }

    public Behavior<FullfillRide.Command> onRideEnded(RideEnded rideEnded) {

        // send msg to ride Service that cab is free now and update the cache table
        Random r= new Random();
        Long index = (long)r.nextInt(10);
        //index = 0L ;

        //send signin to some random RideService.SignIn
        Globals.rideService.get(index).tell(new RideService.RideEndedResponse(choosenCabInfo[0]+"", requestRide.destinationLoc));

        return Behaviors.stopped();
    }


    public static void pn(Object o) {
        System.out.println(o);
        //pn("This is a example")
    }
}
