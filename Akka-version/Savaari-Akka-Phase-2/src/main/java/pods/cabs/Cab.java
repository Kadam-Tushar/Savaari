package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;

import java.util.*;
public class Cab extends EventSourcedBehavior<Cab.Command,Cab.Event,Cab.State>{

   
  

    // Persistance and Sharding  related interfaces 
    public static final EntityTypeKey<Command> TypeKey = EntityTypeKey.create(Cab.Command.class, "CabEntity");

    interface Command extends CborSerializable{}

    interface Event extends CborSerializable{}

    static final class State implements CborSerializable{
        String cabId;

      
        boolean isInterested;
    
        /* 0: Available
         * 1: Commited
         * 2: Giving-Ride
         * 3: SignedOut
         * */
        int state;
    
        //last known position of the cab
        Long location;
    
        /*
         * All following fields stores
         * information about ongoing ride.
         * */
    
        Long rideId ;
        Long sourceLocation;
        Long destinationLocation;
        Long numRides = 0L;

        // Not setting isInterseted , location , numRides , Hope it won't create problem 

        public State(String cabId){
            this.cabId = cabId;
            this.state = 3;
        }





    }

    @Override
    public State emptyState() {
        return new State(this.persistenceId().id().split("\\|")[1]);
    }

    
    public static final class RequestRideEvent implements Event{
        Long rideId;
        Long sourceLoc;
        Long destinationLoc;

        public RequestRideEvent(Long rideId, Long sourceLoc, Long destinationLoc) {
            this.rideId = rideId;
            this.sourceLoc = sourceLoc;
            this.destinationLoc = destinationLoc;
        }
        public RequestRideEvent(){
            super();
        }


    }
    public static final class RequestRideNotInterestedEvent implements Event{
        int dummy = 0 ;
        public RequestRideNotInterestedEvent(){
            super();
        }
        
    }

    public static final class RideEndedEvent implements Event{
        Long rideId;

        public RideEndedEvent(Long rideId) {
            this.rideId = rideId;
        }
        public RideEndedEvent() {
            super();
        }
    }

    public static final class SignInEvent implements Event{
        Long initialPos = 0L;

        public SignInEvent(Long initialPos) {
            this.initialPos = initialPos;
        }

        public SignInEvent() {
            super();
        }
        
    }

    public static final class SignOutEvent implements Event{
        int dummy=0;

        public SignOutEvent() {
        }
    }

    public static final class ResetEvent implements Event{
        int dummy=0;

        public ResetEvent() {
        }
    }
    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(RequestRideEvent.class, Cab::onRequestRideEvent)
                .onEvent(RequestRideNotInterestedEvent.class,Cab::onRequestRideNotInterestedEvent)
                .onEvent(RideEndedEvent.class, Cab::onRideEndedEvent)
                .onEvent(SignInEvent.class, Cab::onSignInEvent)
                .onEvent(SignOutEvent.class, Cab::onSignOutEvent)
                .onEvent(ResetEvent.class, Cab::onResetEvent)
                .build();
    }

public static State onRequestRideEvent(State state, RequestRideEvent event){
    state.isInterested = false;
    state.state = 2;
    state.sourceLocation = event.sourceLoc;
    state.destinationLocation = event.destinationLoc;
    state.rideId = event.rideId;
    state.numRides += 1L;
    state.location = event.sourceLoc;
//   pn("got and after  onRequestRideEvent Cab.state  is   isInterested, state, src,dest,rideId,numRides :  "+state.isInterested + " "+state.state +" "+state.sourceLocation+" "+state.destinationLocation+" "+state.rideId+" "+state.numRides );
    return state; 
}

public static State onRequestRideNotInterestedEvent(State state, RequestRideNotInterestedEvent event){
    state.isInterested  = true ; 
    return state ; 

}

public static State onRideEndedEvent(State state, RideEndedEvent event){
    pn("Got RideEndedEvent  with    cabId:"+state.cabId+" and location: "+state.location+" and rideId: "+event.rideId);

    // if(state.state != 2)
    //         throw new IllegalStateException("ERROR: got a Cab.RideEnded on the cab:"+state.cabId+" which is not in giving-ride state");
    // else if(state.rideId != event.rideId)
    //         throw new IllegalStateException("ERROR: got a Cab.RideEnded on the cab:"+state.cabId+" with rideId not equal to the rideId for which it went into giving-ride state");

    state.state = 0;
    state.location = state.destinationLocation;
    return state;

}

public static State onSignInEvent(State state, SignInEvent event){
    pn("Got SignInEvent  with cabId:"+state.cabId+" and initialPos: "+event.initialPos);
    state.state = 0;
    state.location = event.initialPos;
    state.numRides = 0L;
    state.isInterested = true;
    return state;
}

public static State onSignOutEvent(State state, SignOutEvent event){
    
    state.state = 3;
    state.location = -1L;
    state.numRides = 0L;
    pn("OnSignOutEvent with cabId: "+state.cabId);
    return state;
}

public static State onResetEvent(State state, ResetEvent event){
    pn(" OnResetEvent with  cabId:"+state.cabId);
    state.state = 3;
    state.location = -1L;
    state.numRides = 0L;
        
    return state;
}



    // ActorRef<FullfillRide.Command> FullfillRideActorRef;
    // private final ActorContext<Command> context;

    // public static Behavior<Command> create(String cabId) {
	//     return Behaviors.setup(context -> new Cab(context,cabId));
	// }


    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder().forStateType(State.class)
                .onCommand(RequestRide.class, this::onRequestRide)
                .onCommand(RideEnded.class, this::onRideEnded)
                .onCommand(SignIn.class, this::onSignIn)
                .onCommand(SignOut.class, this::onSignOut)
                .onCommand(Reset.class, this::onReset)
                .onCommand(NumRides.class, this::onNumRides)
                .build();
    }


    
    public static final class RequestRide implements Command {
        Long rideId;
        Long sourceLoc;
        Long destinationLoc;
        ActorRef<FullfillRide.Command> replyTo;

        public RequestRide(Long rideId, Long sourceLoc, Long destinationLoc,
                           ActorRef<FullfillRide.Command> replyTo) {
            this.rideId = rideId;
            this.sourceLoc = sourceLoc;
            this.destinationLoc = destinationLoc;
            this.replyTo = replyTo;
        }

        public  RequestRide(){
            super();
        }
    }

  

    public static final class RideEnded implements Command {
        Long rideId;
        public RideEnded(Long rideId) {
            this.rideId = rideId;
        }
        public RideEnded(){
            super();

        }
    }

    public static final class SignIn implements Command {
        Long initialPos;

        public SignIn(Long initialPos) {
            this.initialPos = initialPos;
        }
        public SignIn(){
            super();

        }
    }

    public static final class SignOut implements Command {
        public SignOut() {
            super();
        }

    }

    public static final class NumRides implements Command {
        ActorRef<NumRideResponse> replyTo;
        public NumRides(ActorRef<NumRideResponse> replyTo) {
            this.replyTo = replyTo;
        }
        public NumRides(){
            super();
        }
    }

    public static final class Reset implements Command {
        ActorRef<NumRideResponse> replyTo;
        public Reset(ActorRef<NumRideResponse> replyTo) {
            this.replyTo = replyTo;
        }
        public Reset(){
            super();
        }
    }

    //interface CabResponse {}

    public static final class NumRideResponse implements Command {
        Long response;

        public NumRideResponse(Long response) {
            this.response = response;
        }
        public NumRideResponse(){
            super();
        }
    }

    public static Behavior<Command> create(String cabId, PersistenceId persistenceId){
        pn(" In Cab.create() cabId: " + cabId);
        return Behaviors.setup(
                ctx -> new Cab(ctx, persistenceId)
        );
    }

    public Cab(ActorContext<Command> context, PersistenceId persistenceId) {
        super(persistenceId);
    }

    public Effect<Event, State> onRideEnded(State state, RideEnded rideEnded){
        if(state.rideId != rideEnded.rideId){
            pn("ERROR: got a Cab.RideEnded on the cab:"+state.cabId+" with rideId not equal to the rideId for which it went into giving-ride state");
            return Effect().none();
        }
        else{
            if(state.state != 2){
                pn("ERROR: got a Cab.RideEnded on the cab:"+state.cabId+" which is not in giving-ride state");
                return Effect().none();
            }
        }
        return Effect()
                .persist(new RideEndedEvent(rideEnded.rideId));
    }

    public Effect<Event, State> onRequestRide(State state, RequestRide requestRide){
        pn("got RequestRideEvent on cab"+state.cabId+" isInterested: "+state.isInterested+", state: "+state.state+", src,dest,rideId : "+requestRide.sourceLoc+" "+requestRide.destinationLoc+" "+requestRide.rideId);
        if(state.state==0){
            if(state.isInterested){
                final Long loc = state.location;
                return Effect().persist(new RequestRideEvent(requestRide.rideId, requestRide.sourceLoc, requestRide.destinationLoc))
                .thenRun(newState -> requestRide.replyTo.tell(
                        new FullfillRide.RequestRideResponse(true, loc)
                        )
                );
            }

            else{
                return Effect().persist(new RequestRideNotInterestedEvent())
                .thenRun(newState -> requestRide.replyTo.tell(
                        new FullfillRide.RequestRideResponse(false, -1L)
                        )
                );

            }
        }
        else{
            return Effect().none().thenRun(
                    newState -> requestRide.replyTo.tell(new FullfillRide.RequestRideResponse(false, -1L))
            );
        }
    }

    public Effect<Event, State> onSignIn(State state, SignIn signIn){
        
        if(state.state != 3|| signIn.initialPos < 0) {
            pn("Trying to Cab.SignIn already signed in cab with id"+state.cabId+" or initialPos is negative ");
            return Effect().none();
        }
        else {
            return Effect().persist(
                new SignInEvent(signIn.initialPos)
            );
        }
    }

    public Effect<Event, State> onSignOut(State state, SignOut signOut){
        pn("Got Cab.SignOut call on cabId:"+state.cabId);
        if(state.state == 3) {
            pn("ERROR: Trying to Cab.SignOut already signed out cab with id"+state.cabId);

            return Effect().none();

        }
        else if(state.state != 0){
            pn("ERROR: Trying to Cab.SignOut cab which is not in available state with id"+state.cabId);
            return Effect().none();

        }
        
        return Effect().persist(
                new SignOutEvent()
        );
        
        
    }

    public Effect<Event, State> onReset(State state,Reset reset){
      
        final Long num = state.numRides;
        pn("Before Reseting of cab : "+state.cabId+"  state was : "+state.state);
        pn("Got Cab.Reset call on cabId:"+state.cabId);
        return Effect()
                .persist(new ResetEvent())
                .thenRun(newState -> reset.replyTo.tell(new NumRideResponse(num)));
        
        
        
    }


    public Effect<Event, State> onNumRides(State state, NumRides numRides){
        pn("Got num rides on cab"+state.cabId);
        return Effect()
                .none()
                .thenRun(newState-> numRides.replyTo.tell(new NumRideResponse(newState.numRides)));
    }

    public static void pn(Object o) {
        System.out.println(" ");
        System.out.println("______________________"+o+"______________________");
        System.out.println(" ");
        System.out.flush();
        //pn("This is a example")
    }

    //Testing
    //=======================================================================================================================================
    // public Behavior<Cab.Command> onGetCabStatus(GetCabStatus getCabStatus){
    //     ActorRef<GetCabStatusResponce> client = getCabStatus.replyTo;
    //     pn(getCabStatus.replyTo);
    //     String getCabStatusResponce = "";

    //     pn("Got Cab.GetCabStatus call on cabId:"+cabId);

    //     if(state == 0) {    //available
    //         getCabStatusResponce = "available "+location;
    //     }
    //     else if(state == 1) {    //commited
    //         getCabStatusResponce = "commited "+location;
    //     }
    //     else if(state == 2) {    //giving-ride
    //         getCabStatusResponce = "giving-ride "+location+" "+rideId+" "+destinationLocation;
    //     }
    //     else if(state == 3) {    //signedOut
    //         getCabStatusResponce = "signed-out -1";
    //     }

    //     client.tell(new GetCabStatusResponce(getCabStatusResponce));
    //     pn("Replying to GetCabStatus on cabId: "+cabId+" with responce: "+getCabStatusResponce);
    //     return this;
    // }

    //=======================================================================================================================================
}
