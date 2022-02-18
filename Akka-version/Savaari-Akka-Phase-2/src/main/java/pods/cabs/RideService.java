package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import jdk.javadoc.internal.tool.Start;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;

import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.persistence.typed.PersistenceId;

import java.util.*;
public class RideService extends AbstractBehavior<RideService.Command> {

    public static final EntityTypeKey<Command> TypeKey = EntityTypeKey.create(RideService.Command.class, "RideServiceEntity");


    public final ActorContext<Command> context;
    String rideServiceId  ;
    Long fullFillRideId = 0L ; 
    ClusterSharding sharding ;
    ClusterSharding counterSharding;
    RequestRide req ; 

    public RideService(ActorContext<Command> context,String rideServiceId){
        super(context);

        this.context = context; 
        this.rideServiceId = rideServiceId ;
        sharding = ClusterSharding.get(context.getSystem()) ;
        counterSharding = ClusterSharding.get(context.getSystem()) ;
        


    }
   
    public static Behavior<Command> create(String rideServiceId) {
        pn("_____________________________In RideService.create() rideServiceId: " + rideServiceId+"_________________");
        return Behaviors.setup(
                ctx -> new RideService(ctx,rideServiceId)
        );
    }

    public static final class RideResponse implements Command {
        Long rideId,fare;
        String cabId;
        
        public RideResponse(Long rideId, String cabId,Long fare,
                                    ActorRef<FullfillRide.Command> replyTo) {
            this.rideId = rideId;
            this.cabId = cabId;
            this.fare = fare ; 
            
        }
        public RideResponse(){
            super();
        }

        @Override
        public boolean equals(Object o) {
            RideResponse r = (RideResponse) o;
            
            if(r.cabId.equals(this.cabId) && r.fare.equals(this.fare))
                return true;
            return false;
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
        public RequestRide(){
            super();
        }
    }


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestRide.class, this::onRequestRide)
                .onMessage(Counter.CounterValue.class, this::onCounterValue)
                .build();
    }

    interface Command extends CborSerializable{}
    

    public Behavior<Command> onRequestRide(RequestRide requestRide){
       req= requestRide;
        pn("got requestRide for "+requestRide.custId+" "+requestRide.sourceLoc+" "+requestRide.destinationLoc);
        
        counterSharding.init(
                Entity.of(Counter.TypeKey, entityContext -> Counter.create(entityContext.getEntityId(), PersistenceId.of(
                        entityContext.getEntityTypeKey().name(), entityContext.getEntityId()
                )))
        );
        EntityRef<Counter.Command> counterRef = counterSharding.entityRefFor(Counter.TypeKey, "RideIdCounter");
        counterRef.tell(new Counter.GetAndIncrement(context.getSelf()));

       
    return this;
    }

    public Behavior<Command> onCounterValue(Counter.CounterValue counterValue){
        sharding.init(
                Entity.of(Cab.TypeKey, entityContext -> Cab.create(entityContext.getEntityId(), PersistenceId.of(
                        entityContext.getEntityTypeKey().name(), entityContext.getEntityId()
                )))
        );

        pn("In onCounterValue to create fullfillride actor for rideId: "+counterValue.value);

        ActorRef<FullfillRide.Command> fullFillRideRef = getContext().spawn(FullfillRide.create(req,counterValue.value),
        "fullfillRideActor_"+rideServiceId+"_"+(fullFillRideId++));

        fullFillRideRef.tell(new FullfillRide.AllocateRide());

        

        return this;
    }

    public static void pn(Object o) {
        System.out.println(" ");
        System.out.println("______________________"+o+"______________________");
        System.out.println(" ");
        System.out.flush();
        //pn("This is a example")
    }

}
