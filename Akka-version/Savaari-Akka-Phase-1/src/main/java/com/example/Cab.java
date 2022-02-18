package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import java.util.*;
public class Cab extends AbstractBehavior<Cab.Command> {

    String cabId;

    /* If the cab is interested
     *  in taking the next ride request
     * */
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

    Long rideId;
    Long sourceLocation;
    Long destinationLocation;
    Long numRides;
  

    ActorRef<FullfillRide.Command> FullfillRideActorRef;
    private final ActorContext<Command> context;

    public static Behavior<Command> create(String cabId) {
	    return Behaviors.setup(context -> new Cab(context,cabId));
	}


    public Cab(ActorContext<Command> context,String cabId){
        super(context);
        this.context = context ;
        this.cabId = cabId;
        this.state = 3;
    }

    //=======================================================================================================================

    public interface Command{}
    
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
    }

    public static final class RideStarted implements Command {
        Long rideId;
        public RideStarted(Long rideId) {
            this.rideId = rideId;
        }
    }

    public static final class RideCanceled implements Command {
        Long rideId;
        public RideCanceled(Long rideId) {
            this.rideId = rideId;
        }
    }

    public static final class RideEnded implements Command {
        Long rideId;
        public RideEnded(Long rideId) {
            this.rideId = rideId;
        }
    }

    public static final class SignIn implements Command {
        Long initialPos;

        public SignIn(Long initialPos) {
            this.initialPos = initialPos;
        }
    }

    public static final class SignOut implements Command {
        public SignOut() {
        }
    }

    public static final class NumRides implements Command {
        ActorRef<NumRideResponse> replyTo;
        public NumRides(ActorRef<NumRideResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static final class Reset implements Command {
        ActorRef<NumRideResponse> replyTo;
        public Reset(ActorRef<NumRideResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }

    //interface CabResponse {}

    public static final class NumRideResponse implements Command {
        Long response;

        public NumRideResponse(Long response) {
            this.response = response;
        }
    }

    //Testing
    //=======================================================================================================================================
    public static final class GetCabStatus implements Command {
        ActorRef<GetCabStatusResponce> replyTo;

        public GetCabStatus(ActorRef<GetCabStatusResponce> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static final class GetCabStatusResponce implements Command {
        String getCabStatusResponce;
        public GetCabStatusResponce(String getCabStatusResponce) {
            this.getCabStatusResponce = getCabStatusResponce;
        }

        @Override
        public boolean equals(Object o) {
            GetCabStatusResponce r = (GetCabStatusResponce) o;
            return r.getCabStatusResponce.equals(this.getCabStatusResponce);

        }
    }
    //=======================================================================================================================================
    //=======================================================================================================================

    @Override
	public Receive<Cab.Command> createReceive() {
		ReceiveBuilder<Cab.Command> builder = newReceiveBuilder();
		return builder
                .onMessage(RequestRide.class, this::onRequestRide)
                .onMessage(RideStarted.class, this::onRideStarted)
                .onMessage(RideCanceled.class, this::onRideCanceled)
                .onMessage(RideEnded.class, this::onRideEnded)
                .onMessage(SignIn.class, this::onSignIn)
                .onMessage(SignOut.class, this::onSignOut)
                .onMessage(Reset.class, this::onReset)
                .onMessage(NumRides.class, this::onNumRides)
                .onMessage(GetCabStatus.class, this::onGetCabStatus)
				.build();
	}

    public Behavior<Cab.Command> onRideStarted(RideStarted rideStarted){
        pn("Got Cab.RideStarted call on cabId:"+cabId+" and location: "+location+" and rideId: "+rideStarted.rideId);

        if(state != 1)
            pn("ERROR: got a Cab.RideStarted on the cab:"+cabId+" which is not in commited state");
        else if(rideId != rideStarted.rideId)
            pn("ERROR: got a Cab.RideStarted on the cab:"+cabId+" with rideId not equal to the rideId for which it went into commited state");
        else {
            state = 2;
            numRides++;
            location = sourceLocation;
            pn("Ride: "+rideStarted.rideId+" Started on cab: "+cabId);
        }

        return this;
    }

    public Behavior<Cab.Command> onRideCanceled(RideCanceled rideCanceled){
        pn("Got Cab.RideCancelled call on cabId:"+cabId+" and location: "+location+" and rideId: "+rideCanceled.rideId);

        if(state != 1)
            pn("ERROR: got a Cab.RideCancelled on the cab:"+cabId+" which is not in commited state");
        else if(rideId != rideCanceled.rideId)
            pn("ERROR: got a Cab.RideCancelled on the cab:"+cabId+" with rideId not equal to the rideId for which it went into commited state");
        else {
            state = 0;
            pn("Ride: "+rideCanceled.rideId+" Cancelled on cab: "+cabId);
        }

        return this;
    }

    public Behavior<Cab.Command> onRideEnded(RideEnded rideEnded){
        pn("Got Cab.RideEnded call on cabId:"+cabId+" and location: "+location+" and rideId: "+rideEnded.rideId);

        if(state != 2)
            pn("ERROR: got a Cab.RideEnded on the cab:"+cabId+" which is not in giving-ride state");
        else if(rideId != rideEnded.rideId)
            pn("ERROR: got a Cab.RideEnded on the cab:"+cabId+" with rideId not equal to the rideId for which it went into giving-ride state");
        else {
            //Send RideService.RideEnded to a random RideService
            location = destinationLocation;
            state = 0;
            pn("Ride: "+rideEnded.rideId+" Ended on cab: "+cabId);
        }

        pn("\nCabId:"+cabId+" is sending RideEnded to "+FullfillRideActorRef+" this FullfillRideActorRef\n");
        FullfillRideActorRef.
            tell(new FullfillRide.RideEnded());
        return this;
    }

    public Behavior<Cab.Command> onRequestRide(RequestRide requestRide){
        // Long rideId;
        // Long sourceLoc;
        // Long destinationLoc;
        // ActorRef<FullfillRide.Command> replyTo;

        pn("Got Cab.RequestRide call on cabId:"+cabId+" and location: "+location+" with rideId: "+requestRide.rideId+" sourceLoc: "+requestRide.sourceLoc+" destinationLoc: "+requestRide.destinationLoc);

        if(state == 0) {
            pn("State is available for cabID: "+cabId);
            if(isInterested) {
                pn("cabId: "+cabId+" is interested in taking the ride");
                isInterested = false;
                state = 1;
                sourceLocation = requestRide.sourceLoc;
                destinationLocation = requestRide.destinationLoc;
                rideId = requestRide.rideId;
                pn("CabId: "+cabId+" is saving FullfillRideActorRef as: "+requestRide.replyTo);
                FullfillRideActorRef = requestRide.replyTo;
                requestRide.replyTo.tell(new FullfillRide.RequestRideResponse(true));
            }
            else {
                pn("cabId: "+cabId+" is NOT interested in taking the ride");
                isInterested = true;
                requestRide.replyTo.tell(new FullfillRide.RequestRideResponse(false));
            }
        }
        else {
            pn("State is NOT available for cabID: "+cabId);
            requestRide.replyTo.tell(new FullfillRide.RequestRideResponse(false));
        }
        return this;
    }

    public Behavior<Cab.Command> onSignIn(SignIn signIn){
        pn("Got Cab.SignIn call on cabId:"+cabId+" and initialPos: "+signIn.initialPos);
        if(state != 3|| signIn.initialPos < 0) {
            pn("Trying to Cab.SignIn already signed in cab with id"+cabId+" or initialPos is negative ");
        }
        else {
            
            Random r= new Random();
            Long index = (long)r.nextInt(10);
            //index = 0L ;

            //send signin to some random RideService.SignIn
            Globals.rideService.get(index).tell(new RideService.CabSignsIn(cabId, signIn.initialPos));


            state = 0;
            location = signIn.initialPos;
            numRides = 0L;
            isInterested = true;
            pn("Cab.SignIn complete on cabId: "+cabId+" with initalPos: "+location);
        }
        return this;
    }

    public Behavior<Cab.Command> onSignOut(SignOut signOut){
        pn("Got Cab.SignOut call on cabId:"+cabId);
        if(state == 3) {
            pn("ERROR: Trying to Cab.SignOut already signed out cab with id"+cabId);
        }
        else if(state != 0){
            pn("ERROR: Trying to Cab.SignOut cab which is not in available state with id"+cabId);
        }
        else {
            
            Random r= new Random();
            Long index = (long)r.nextInt(10);
            //index = 0L ;

            //send signout to some random RideService.SignOut
            Globals.rideService.get(index).tell(new RideService.CabSignsOut(cabId));


            state = 3;
            location = -1L;
            numRides = 0L;
            pn("Cab.SignOut complete on cabId: "+cabId);
        }
        return this;
    }

    public Behavior<Cab.Command> onReset(Reset reset){
        ActorRef<NumRideResponse> client = reset.replyTo;

        pn("Got Cab.Reset call on cabId:"+cabId);

        if(state == 2) {    //giving-ride then do rideEnded
            //Send RideService.RideEnded to a random RideService
            location = destinationLocation;
            state = 0;
            pn("Ride: "+rideId+" Ended on cab: "+cabId);
        }
        if(state == 1) {    //commited then do rideCancelled
            state = 0;
            pn("Ride: "+rideId+" Cancelled on cab: "+cabId);
        }
        if(state == 0) {    //available then do signOut
            //send signout to some random RideService.SignOut
            state = 3;
            location = -1L;
            //numRides = 0;
            pn("Cab.SignOut complete on cabId: "+cabId);
        }

        client.tell(new NumRideResponse(numRides));
        numRides = 0L;

        pn("Reset complete on cabId: "+cabId);
        return this;
    }


    public Behavior<Cab.Command> onNumRides(NumRides numRide){
        ActorRef<NumRideResponse> client = numRide.replyTo;
        pn("Got numRides call on cabId:"+cabId);

        client.tell(new NumRideResponse(numRides));
        return this;
    }

    public static void pn(Object o) {
        System.out.println(o);
        //pn("This is a example")
    }

    //Testing
    //=======================================================================================================================================
    public Behavior<Cab.Command> onGetCabStatus(GetCabStatus getCabStatus){
        ActorRef<GetCabStatusResponce> client = getCabStatus.replyTo;
        pn(getCabStatus.replyTo);
        String getCabStatusResponce = "";

        pn("Got Cab.GetCabStatus call on cabId:"+cabId);

        if(state == 0) {    //available
            getCabStatusResponce = "available "+location;
        }
        else if(state == 1) {    //commited
            getCabStatusResponce = "commited "+location;
        }
        else if(state == 2) {    //giving-ride
            getCabStatusResponce = "giving-ride "+location+" "+rideId+" "+destinationLocation;
        }
        else if(state == 3) {    //signedOut
            getCabStatusResponce = "signed-out -1";
        }

        client.tell(new GetCabStatusResponce(getCabStatusResponce));
        pn("Replying to GetCabStatus on cabId: "+cabId+" with responce: "+getCabStatusResponce);
        return this;
    }

    //=======================================================================================================================================
}
