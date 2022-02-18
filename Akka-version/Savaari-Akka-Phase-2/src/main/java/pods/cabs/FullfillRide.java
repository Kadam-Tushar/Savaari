package pods.cabs;

import java.time.LocalDate;
import java.util.*;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;

import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.persistence.typed.PersistenceId;

public class FullfillRide extends AbstractBehavior<FullfillRide.Command> {

    private final ActorContext<Command> context;
    
    RideService.RequestRide requestRide ;
    
    Long rideId;
    int cabIndex = 0 ; 
    
    static ClusterSharding sharding;
 
    public static Behavior<Command> create(RideService.RequestRide r ,Long rideId) {
        pn("_____________________________In Fullfillreide.create() rideId: " + rideId+"_________________");
	    return Behaviors.setup(context -> new FullfillRide(context,r,rideId));
	}


    public FullfillRide(ActorContext<Command> context,RideService.RequestRide r,Long rideId ){
        super(context);
        this.context = context ;
        this.requestRide = r ;
        this.rideId = rideId; 
    // Sharding init code 

        sharding = ClusterSharding.get(context.getSystem());
        sharding.init(
            Entity.of(Cab.TypeKey, entityContext -> Cab.create(entityContext.getEntityId(), PersistenceId.of(
                    entityContext.getEntityTypeKey().name(), entityContext.getEntityId()
            )))
    );



    }
    
    
    //=======================================================================================================================

    public interface Command extends CborSerializable{}
    
    public static final class RequestRideResponse implements Command {
        boolean response;
        Long location = 0L ;
        public RequestRideResponse(boolean response,Long location) {
            this.response = response;
            
            this.location = location;
        }
        public RequestRideResponse(){
            super();
        }
    }
    
    public static final class RideEnded implements Command {

        public RideEnded() {
            super();

        }

    }
    public static final class AllocateRide implements Command {
        public AllocateRide() {
            super();
        }
    }


    

    //=======================================================================================================================

    @Override
	public Receive<FullfillRide.Command> createReceive() {
		ReceiveBuilder<FullfillRide.Command> builder = newReceiveBuilder();
		return builder
                .onMessage(RequestRideResponse.class, this::onRequestRideResponse)
                .onMessage(AllocateRide.class, this::onAllocateRide)
                .build();
	}

    public Behavior<FullfillRide.Command> onAllocateRide(AllocateRide allocateRide){

        
        
        cabIndex = 0;
        pn(Globals.cabs);
        EntityRef<Cab.Command> cab = sharding.entityRefFor(
            Cab.TypeKey, "cab"+Globals.cabs.get(cabIndex)+"");
        cab.tell(new Cab.RequestRide(rideId, requestRide.sourceLoc, requestRide.destinationLoc, context.getSelf()));


        return this ;

    }

    

    public Behavior<Command> onRequestRideResponse(RequestRideResponse r){
        if (!r.response){
            cabIndex++;
            if( cabIndex < Globals.cabs.size() ) {
                EntityRef<Cab.Command> cab = sharding.entityRefFor(Cab.TypeKey, "cab"+Globals.cabs.get(cabIndex)+"");
                cab.tell(new Cab.RequestRide(rideId, requestRide.sourceLoc, requestRide.destinationLoc, context.getSelf()));
            }

            else {
                System.out.println("Context: "+context.getSelf());
                System.out.println("Replyto: "+requestRide.replyTo);
                pn("from fullfillride actor sending negative reply for rideId: "+rideId);
                requestRide.replyTo.tell
                    (new RideService.RideResponse(-1L, 0+"", 0L, context.getSelf()));
            }
        }

        else { 
            //calc fare
            Long fare = (Math.abs(r.location-requestRide.sourceLoc) + Math.abs(requestRide.destinationLoc-requestRide.sourceLoc))*10;
            pn("from fullfillride actor sending positive reply for rideId: "+rideId);
            requestRide.replyTo.tell(new RideService.RideResponse
                        (rideId, Globals.cabs.get(cabIndex)+"",fare, context.getSelf()));
        }
        return this ;
    }



    public static void pn(Object o) {
        System.out.println(" ");
        System.out.println("______________________"+o+"______________________");
        System.out.println(" ");
        System.out.flush();
        //pn("This is a example")
    }
}
