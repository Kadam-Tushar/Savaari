//package pods.cabs;
package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;

import java.io.Console;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.*;
public class Main {
  static ArrayList<Long> cabs,custs,balances;
  public static void pn(Object o){
      System.out.println(o);
  }
  
  public static void main(String[] args) {

    // Input of customers and cabId from IDs.txt 

    takeInput();


    if (args.length == 0) {
        pn("Give port no. Can't proceed");
    } 
    else{
        Arrays.stream(args).map(Integer::parseInt).forEach(Main::startup);
    }
      
  }

  private static Behavior<Void> rootBehavior() {
    return Behaviors.setup(context -> {
      return Behaviors.empty();
    });
  }

  private static void startup(int port) {
    // Override the configuration of the port
    // Override the configuration of the port
    Map<String, Object> overrides = new HashMap<>();
    overrides.put("akka.remote.artery.canonical.port", port);
    if (port == 10001) {
    	overrides.put("akka.persistence.journal.plugin", "akka.persistence.journal.leveldb");
	// option above is same as in akka-raghavan-noSharding-persistence-demo.
	// It says  node 10001 will use leveldb to store its journal
    	overrides.put("akka.persistence.journal.proxy.start-target-journal", "on");
	// Default value for this flag is "off" (see application.conf), as all
	// other nodes should use 25251's journal.
    }
    else  {
    	overrides.put("akka.persistence.journal.plugin", "akka.persistence.journal.proxy");
	// Tells other nodes that they should use some other node's journal. 
	// Information about that other node is given application.conf via the
	// flags target-journal-plugin and target-journal-address.
	// Note, this proxy system is ok for testing, but not ok for deployment,
	// as 25251 becomes a single point of failure as far as the journalling goes.
    }
    
    

    Config config = ConfigFactory.parseMap(overrides)
        .withFallback(ConfigFactory.load());

    // Create an Akka system
    ActorSystem<Void> system = ActorSystem.create(rootBehavior(), "ClusterSystem", config);
    
    // Normally nothing should be done in main() after creating the ActorSytem.
    // We are breaking this rule only for demo purposes.
    
   
    final ClusterSharding cabSharding = ClusterSharding.get(system);

    // cab sharding 
    cabSharding.init(
      Entity.of(Cab.TypeKey,
		entityContext ->
		  Cab.create(
		    entityContext.getEntityId(), 
		    PersistenceId.of(
  		     entityContext.getEntityTypeKey().name(), entityContext.getEntityId()
				    )
			)
		)
    );
      /* Notice how we generate a unique peristence-id using a
       * combination of the entity type-key and entity ID. This is a
       * common idiom. Passing the persistence key to .create is
       * mandatory, while passing the entity ID is optional.  Notice
       * how in CounterPersist's constructor the persistence-id is
       * passed to super(); this is mandatory. */

      final ClusterSharding rideServiceSharding = ClusterSharding.get(system);
      rideServiceSharding.init(
              Entity.of(RideService.TypeKey,
                      entityContext ->
                              RideService.create(entityContext.getEntityId())
              )
      );

      if (port == 10001) {

          
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService1");
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService2");
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService3");

      } else if (port == 10002) {

          
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService4");
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService5");
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService6");

      } else if (port == 10003) {

          
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService7");
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService8");
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService9");

      } else if (port == 10004) {
          
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService10");
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService11");
          rideServiceSharding.entityRefFor(RideService.TypeKey, "rideService12");

      }
      
    
  }
  static void takeInput(){

    // Assuming ID's.txt will contain sorted order of cab Ids and custIds
    try{
    Scanner sc = new Scanner(new File("IDs.txt"));
    String line = sc.nextLine();
    line  = sc.nextLine();
    Globals.custs = new ArrayList<>();
    Globals.cabs = new ArrayList<>();
    while(!line.contains("*")){
        Globals.cabs.add(Long.parseLong(line));
        line = sc.nextLine();
    }
    line = sc.nextLine();

    while(!line.contains("*")){
        Globals.custs.add(Long.parseLong(line));
        line = sc.nextLine();
    }
    }
    catch(Exception e){
      //e.printStackTrace();
      System.out.println("ID's  File exception : "+e.getMessage());
    }

  }
  
}
