package pods.cabs;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.*;
import org.junit.Assert;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import java.time.Duration.*;
import org.junit.runners.MethodSorters;
import java.util.concurrent.TimeUnit;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Join;
import akka.persistence.typed.PersistenceId;
import pods.cabs.Globals;
import pods.cabs.RideService;
import java.util.*;
import java.io.*;


import java.time.Duration;
import com.typesafe.config.ConfigFactory;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.runners.MethodSorters;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.io.Console;

import akka.actor.ProviderSelection;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;


//#definition
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AkkaPrivateTestCases2 {


    static ClusterSharding cabSharding;
    static ClusterSharding rideSharding;

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource(ActorTestKit.create("ClusterSystem")) ;

//#definition

    //#test
    public static void sharder_init() {
        takeInput();
        
        cabSharding = ClusterSharding.get(testKit.system());
        cabSharding.init(Entity.of(Cab.TypeKey,
                entityContext -> Cab.create(entityContext.getEntityId(),
                        PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()
                        ))));

        rideSharding = ClusterSharding.get(testKit.system());
        rideSharding.init(Entity.of(RideService.TypeKey,
                entityContext -> RideService.create(entityContext.getEntityId())
        ));
    }


    /*

    Syntax for name of test method : test{x}_{your_actor} where x: test number as test will execute in ascending order 

    */
	// @Test
	// public void testCase1() throws InterruptedException {
	// 	// This Testcase checks whether a ride request from invalid customer is rejected or not.
	// 	// Also checks whether a ride request for a cab on givingride is rejected or not.
	// 	sharder_init();
		
	// 	System.out.println("### Test Case 1 Started ###");

	// 	//reset all Cabs
	// 	resetCabActors();

	// 	//ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
	// 	EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey,"cab101");
	// 	cab101.tell(new Cab.SignIn(10L));

	// 	EntityRef<RideService.Command> rideService = rideSharding.entityRefFor(RideService.TypeKey,
	// 			getRideServiceId(7));    //#Change#.

	// 	TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();
	// 	RideService.RideResponse resp = null;

	// 	// Delay
	// 	TimeUnit.SECONDS.sleep(1);

	// 	//Assuming 207 is an Invalid CustomerId.
	// 	//  Ride request should be denied
	// 	// rideService.tell(new RideService.RequestRide("207", 10L, 100L, probe.ref()));
	// 	// resp = probe.receiveMessage();

	// 	// if (resp.rideId != -1)
	// 	// 	System.out.println("XXXX Ride request from Customer 207 is accepted - Ride ID : " + resp.rideId);
	// 	// else
	// 	// 	System.out.println(" $$$$ Ride request from Customer 207 is Denied ! ");

	// 	// assertTrue((long)resp.rideId == -1);


	// 	// Ride request should be accepted.
	// 	rideService.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
	// 	resp = probe.receiveMessage();

	// 	if (resp.rideId != -1)
	// 		System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
	// 	else
	// 		System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

	// 	assertTrue((long)resp.rideId != -1);

	// 	// Delay
	// 	TimeUnit.SECONDS.sleep(1);


	// 	EntityRef<RideService.Command> rideService1 = rideSharding.entityRefFor(RideService.TypeKey,
	// 			getRideServiceId(8));    //#Change#.

	// 	rideService1.tell(new RideService.RequestRide("202", 10L, 100L, probe.ref()));
	// 	resp = probe.receiveMessage();

	// 	System.out.println(" ");
	// 	System.out.println("Checkpoint for persistence !");
	// 	System.out.println(" ");
	// 	// Console cnsl = System.console();
	// 	// if (cnsl == null) {
	// 	// 	System.out.println(
	// 	// 			"No console available");
	// 	// 	return;
	// 	// }

	// 	// String str = cnsl.readLine();
	// 	// Enter any string (or just Enter) to satisfy the readLine above
	// 	// Delay
	// 	TimeUnit.SECONDS.sleep(50);


	// 	if (resp.rideId != -1)
	// 		System.out.println("XXXX Ride request from Customer 202 is accepted - Ride ID : " + resp.rideId);
	// 	else
	// 		System.out.println("$$$$ Ride request from Customer 202 is Denied ! ");

	// 	assertTrue(resp.rideId == -1);
	// 	// Request should be denied as the cab101 is in givingride status


	// }


	// @Test
	// public void testCase2() throws InterruptedException {
	// 	// This Testcase checks the number of rides for cabs in different status.
	// 	//Similar to Testcase 3 in Phase1

	// 	System.out.println("### Test Case 2 Started ###");

	// 	//reset all Cabs
	// 	resetCabActors();


	// 	TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
	// 	EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey,"cab101");

	// 	// number of rides for signed out cab.
	// 	cab101.tell(new Cab.NumRides(Cprobe.ref()));
	// 	Cab.NumRideResponse Cresp = Cprobe.receiveMessage();

	// 	System.out.println("### Number of Rides for Cab 101 in SignedOut Stage : " + Cresp.response);
	// 	assertTrue((long)Cresp.response == 0); // need to ensure variable name numRides

	// 	// Cab101 signs In
	// 	cab101.tell(new Cab.SignIn(10L));

	// 	// Checks the number of rides for signed in cab.
	// 	// Though Cab.SignIn has no response, the numRides should return 0,
	// 	// since for Signed Out/just Signed In Cab, numRides is 0.

	// 	cab101.tell(new Cab.NumRides(Cprobe.ref()));
	// 	Cresp = Cprobe.receiveMessage();
	// 	System.out.println("### Number of Rides for Cab 101 in just SignedIn Stage : " + Cresp.response);
	// 	assertTrue((long)Cresp.response == 0);

	// 	// Request For a ride


	// 	EntityRef<RideService.Command> rideService = rideSharding.entityRefFor(RideService.TypeKey,
	// 			getRideServiceId(9));    //#Change#.



	// 	TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

	// 	rideService.tell(new RideService.RequestRide("201", 0L, 100L, probe.ref()));
	// 	RideService.RideResponse resp = probe.receiveMessage(Duration.ofSeconds(5));

	// 	if (resp.rideId != -1)
	// 		System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
	// 	else
	// 		System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

	// 	assertTrue((long)resp.rideId != -1);

	// 	// Checks for the Number of Rides for the cab in givingride status---
	// 	cab101.tell(new Cab.NumRides(Cprobe.ref()));
	// 	Cresp = Cprobe.receiveMessage();
	// 	System.out.println("### Number of Rides for Cab 101 in giving-ride status : " + Cresp.response);
	// 	assertTrue(Cresp.response == 1);

	// 	cab101.tell(new Cab.RideEnded(resp.rideId));

	// }

	// @Test
	// public void testCase3() throws InterruptedException {
	// 	// This Testcase checks whether a disinterested cab is allocated for a ride or
	// 	// not .
	// 	//Similar to Testcase 4 in Phase1

	// 	System.out.println("### Test Case 3 Started ###");

	// 	//reset all Cabs
	// 	resetCabActors();


	// 	TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();

	// 	EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey,"cab101");

	// 	// Cab101 signs In
	// 	cab101.tell(new Cab.SignIn(10L));

	// 	// Checks the number of rides for signed in cab.

	// 	cab101.tell(new Cab.NumRides(Cprobe.ref()));
	// 	Cab.NumRideResponse Cresp = Cprobe.receiveMessage();
	// 	assertTrue((long)Cresp.response == 0);

	// 	// Request For a ride and should be allowed with one request itself as
	// 	// Cab.NumRides had response

	// 	EntityRef<RideService.Command> rideService = rideSharding.entityRefFor(RideService.TypeKey,
	// 			getRideServiceId(10));    //#Change#.

	// 	EntityRef<RideService.Command> rideService1 = rideSharding.entityRefFor(RideService.TypeKey,
	// 			getRideServiceId(11));    //#Change#.


	// 	TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

	// 	rideService.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
	// 	RideService.RideResponse resp = probe.receiveMessage();

	// 	if (resp.rideId != -1)
	// 		System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
	// 	else
	// 		System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

	// 	assertTrue((long)resp.rideId != -1);
	// 	cab101.tell(new Cab.RideEnded(resp.rideId));

	// 	// Delay
	// 	TimeUnit.SECONDS.sleep(1);

	// 	// Again requests for a cab101 that should be rejected due to disinterest.
	// 	// But this may happen since rideEnded has no response and
	// 	// the internal caches are not updated with available status of Cab101.
	// 	rideService1.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
	// 	resp = probe.receiveMessage();

	// 	if (resp.rideId != -1)
	// 		System.out.println("XXXX Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
	// 	else
	// 		System.out.println("$$$$ Ride request from Customer 201 is Denied ! ");

	// 	assertTrue((long)resp.rideId == -1);

	// }

	@Test
	public void testCase4() throws InterruptedException {
		sharder_init();
		// This Testcase checks the following scenario.
		// 4 cabs signed in, 4 ride requests are accepted.
		// Last cab in the sorted order of cabid is ended.
		// So all these 4 cabs are either givingride or disinterested for next ride.
		// Therefore 5th request should be denied
		//   and 6th request should be allowed.
		// The 6th request should be allocated with last cab.

		//Similar to Testcase 3 in Phase1

		System.out.println("### Test Case 4 Started ###");

		//reset all Cabs
		resetCabActors();

		TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();

		EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey,"cab101");
		EntityRef<Cab.Command> cab102 = cabSharding.entityRefFor(Cab.TypeKey,"cab102");
		EntityRef<Cab.Command> cab103 = cabSharding.entityRefFor(Cab.TypeKey,"cab103");
		EntityRef<Cab.Command> cab104 = cabSharding.entityRefFor(Cab.TypeKey,"cab104");


		// Cabs sign In
		cab101.tell(new Cab.SignIn(10L));
		cab102.tell(new Cab.SignIn(10L));
		cab103.tell(new Cab.SignIn(10L));
		cab104.tell(new Cab.SignIn(10L));

		cab101.tell(new Cab.NumRides(Cprobe.ref()));
		Cab.NumRideResponse Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 0);

		cab102.tell(new Cab.NumRides(Cprobe.ref()));
		Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 0);

		cab103.tell(new Cab.NumRides(Cprobe.ref()));
		Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 0);

		cab104.tell(new Cab.NumRides(Cprobe.ref()));
		Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 0);

		System.out.println("### Cab 101, 102, 103 and 104 are Signed In");

		// Request For a ride and should be allowed with one request itself as
		// Cab.NumRides had response

		EntityRef<RideService.Command> rideService1 = rideSharding.entityRefFor(RideService.TypeKey,
				getRideServiceId(1));    //#Change#.

		EntityRef<RideService.Command> rideService2 = rideSharding.entityRefFor(RideService.TypeKey,
				getRideServiceId(2));    //#Change#.

		EntityRef<RideService.Command> rideService3 = rideSharding.entityRefFor(RideService.TypeKey,
				getRideServiceId(3));    //#Change#.

		EntityRef<RideService.Command> rideService4 = rideSharding.entityRefFor(RideService.TypeKey,
				getRideServiceId(4));    //#Change#.

		EntityRef<RideService.Command> rideService5 = rideSharding.entityRefFor(RideService.TypeKey,
				getRideServiceId(5));    //#Change#.
		EntityRef<RideService.Command> rideService6 = rideSharding.entityRefFor(RideService.TypeKey,
				getRideServiceId(11));    //#Change#.

		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

		rideService1.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
		RideService.RideResponse resp1 = probe.receiveMessage();

		if (resp1.rideId != -1)
			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp1.rideId);
		else
			System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

		assertTrue((long)resp1.rideId != -1);

		rideService2.tell(new RideService.RequestRide("202", 10L, 110L, probe.ref()));
		RideService.RideResponse resp2 = probe.receiveMessage();
		if (resp2.rideId != -1)
			System.out.println("$$$$ Ride request from Customer 202 is accepted - Ride ID : " + resp2.rideId);
		else
			System.out.println("XXXX Ride request from Customer 203 is Denied ! ");

		assertTrue((long)resp2.rideId != -1);

		rideService3.tell(new RideService.RequestRide("203", 10L, 120L, probe.ref()));
		RideService.RideResponse resp3 = probe.receiveMessage();
		if (resp3.rideId != -1)
			System.out.println("$$$$ Ride request from Customer 203 is accepted - Ride ID : " + resp3.rideId);
		else
			System.out.println("XXXX Ride request from Customer 203 is Denied ! ");

		assertTrue((long)resp3.rideId != -1);


		rideService4.tell(new RideService.RequestRide("203", 100L, 10L, probe.ref()));
		RideService.RideResponse resp4 = probe.receiveMessage();

		if (resp4.rideId != -1)
			System.out.println("$$$$ Ride request from Customer 203 is accepted - Ride ID : " + resp4.rideId);
		else
			System.out.println("XXXX Ride request from Customer 203 is Denied ! ");

		assertTrue((long)resp4.rideId != -1);

		TimeUnit.SECONDS.sleep(10);

		cab104.tell(new Cab.RideEnded(resp4.rideId));

		//All cabs are in giving ride or disinterested mode now. So new request should not be accepted.
		rideService5.tell(new RideService.RequestRide("203", 100L, 10L, probe.ref()));
		RideService.RideResponse resp5 = probe.receiveMessage();

		if (resp5.rideId != -1)
			System.out.println("XXXX Ride request from Customer 203 is accepted - Ride ID : " + resp5.rideId);
		else
			System.out.println("$$$$ Ride request from Customer 203 is Denied ! ");

		assertTrue((long)resp5.rideId == -1);

		//One cab should be allocated

		rideService6.tell(new RideService.RequestRide("203", 100L, 10L, probe.ref()));
		RideService.RideResponse resp6 = probe.receiveMessage();

		System.out.println(" ");
		System.out.println("Checkpoint for persistence !");
		System.out.println(" ");
		// Console cnsl = System.console();
		// if (cnsl == null) {
		// 	System.out.println(
		// 			"No console available");
		// 	return;
		// }

		// String str = cnsl.readLine();
		// Enter any string (or just Enter) to satisfy the readLine above
		// Delay
		TimeUnit.SECONDS.sleep(50);

		if (resp6.rideId != -1)
			System.out.println("$$$$ Ride request from Customer 203 is accepted - Ride ID : " + resp6.rideId);
		else
			System.out.println("XXXX Ride request from Customer 203 is Denied ! ");

		assertTrue((long)resp6.rideId != -1);

		//Last request should be allocated for Cab104
		cab104.tell(new Cab.NumRides(Cprobe.ref()));
		Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 2);
	}



	// @Test
	// public void testCase5() throws InterruptedException {
	// 	// This Testcase do the stress test.
	// 	// Also verifies the number of rides after a series of ride requests/end.

	// 	//Similar to Testcase 9 in Phase1

	// 	int allotCount = 0, denialCount = 0;
	// 	System.out.println("### Test Case 5 Started ###");

	// 	//reset all Cabs
	// 	resetCabActors();


	// 	TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
	// 	EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey,"cab101");

	// 	cab101.tell(new Cab.SignIn(3L));

	// 	cab101.tell(new Cab.NumRides(Cprobe.ref()));
	// 	Cab.NumRideResponse Cresp = Cprobe.receiveMessage();
	// 	assertTrue((long)Cresp.response == 0);

	// 	System.out.println("### Cab 101 is  Signed In");

	// 	// Request For a ride and should be allowed with one request itself as
	// 	// Cab.NumRides had response
	// 	TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();
	// 	Long step = 3L;
	// 	int noOfSteps = 200;
	// 	for (int i = 0; i <= noOfSteps; i++) {


	// 		EntityRef<RideService.Command> rideService = rideSharding.entityRefFor(RideService.TypeKey,
	// 				getRideServiceId(i%12+1));    //#Change#.



	// 		rideService.tell(new RideService.RequestRide("201", 0L, step, probe.ref()));
	// 		RideService.RideResponse resp = probe.receiveMessage();

	// 		if (resp.rideId != -1) {
	// 			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
	// 			cab101.tell(new Cab.RideEnded(resp.rideId));
	// 			System.out.println("$$$$ Ride Has ended");
	// 			allotCount++;
	// 		} else {
	// 			System.out.println("$$$$ Ride request from Customer 201 is Denied ! ");
	// 			denialCount++;
	// 		}
	// 	}

	// 	System.out.println("#### Total Rides : " + allotCount + " Denial of rides : " + denialCount);

	// 	cab101.tell(new Cab.NumRides(Cprobe.ref()));
	// 	Cab.NumRideResponse Cresp1 = Cprobe.receiveMessage();
	// 	System.out.println("### Number of rides for the Cab 101 : " + Cresp1.response);
	// 	assertTrue((long)Cresp1.response == (long)allotCount);

	// }

	static void resetCabActors() {

        TestProbe<Cab.NumRideResponse> resetactor = testKit.createTestProbe();

        for (long value: Globals.cabs){
            EntityRef<Cab.Command> cab = cabSharding.entityRefFor(Cab.TypeKey, "cab"+value);
            cab.tell(new Cab.Reset(resetactor.ref()));
            
            Cab.NumRideResponse res = resetactor.receiveMessage(Duration.ofSeconds(7));


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

  public static void pn(Object o) {
    System.out.println(" ");
    System.out.println("______________________"+o+"______________________");
    System.out.println(" ");
    System.out.flush();
    //pn("This is a example")
  }

  public String getRideServiceId(int n) {
	  return "rideService1"+n;
  }


}
// */
