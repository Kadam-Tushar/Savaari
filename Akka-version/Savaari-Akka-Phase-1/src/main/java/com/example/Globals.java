package pods.cabs;
import java.util.*;
import akka.actor.typed.ActorRef;


import java.util.concurrent.atomic.*;


public class Globals{
    public static  HashMap<String,ActorRef<Wallet.Command>> wallets = new HashMap<>();

    public static  HashMap<String,ActorRef<Cab.Command>> cabs = new HashMap<>();
    public static  HashMap<Long,ActorRef<RideService.Command>> rideService = new HashMap<>();
    
    public static AtomicLong rideIdCounter = new AtomicLong();
    //public static  HashMap<String,ActorRef<RideService.Command>> RideService = new HashMap<>();

    public static AtomicLong timeStamp = new AtomicLong();

}