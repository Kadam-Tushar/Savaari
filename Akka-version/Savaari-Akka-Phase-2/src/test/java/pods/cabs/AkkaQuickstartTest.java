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
//#definition
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AkkaQuickstartTest {


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
    @Test
    public void test1() {
        //this test case checks the signin fucntionality

        sharder_init();

        resetCabActors();

        EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey, "cab101");
        TestProbe<Cab.NumRideResponse> resetRes = testKit.createTestProbe();

        //cab101.tell(new Cab.Reset(resetRes.ref()));

        Cab.NumRideResponse resp;

        cab101.tell(new Cab.SignIn(100L));
        cab101.tell(new Cab.NumRides(resetRes.ref()));

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            System.out.println("Error in waiting!!!");//TODO: handle exception
        }

        resp = resetRes.receiveMessage();
        pn("printing response"+resp.response);
        assertEquals(0, (long)resp.response);
        

    }

    @Test
    public void test2(){
        // this test case checks if the requestride fucntionality is working properly
        // it also checks whether the fare is calculated correctly

        resetCabActors();

        EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey, "cab101");
        cab101.tell(new Cab.SignIn(0L));
        //Globals.cabs.get("101").tell(new Cab.SignIn(0L));


        EntityRef<RideService.Command> rideService1 = rideSharding.entityRefFor(RideService.TypeKey, "rideService1");
        //TestProbe<RideService.RideResponse> rideResponseProbe = testKit.createTestProbe();

        TestProbe<RideService.RideResponse> rideResponseProbe = testKit.createTestProbe();

        rideService1.tell(new RideService.RequestRide("201", 50L, 100L, rideResponseProbe.ref()));
        //Globals.rideService.get(0L).tell(new RideService.RequestRide("201", 50L, 100L, rideResponseProbe.ref()));
        
        RideService.RideResponse r = rideResponseProbe.receiveMessage();
        //rideResponseProbe.expectMessage(new RideService.RideResponse(0L, "101", 1000L, null));
    
        assertEquals("101", r.cabId);
        assertEquals(1000,(long)r.fare);

    }
    

    @Test
    public void test3(){
        //this test case checks:

        // it checks whether RideService actors on multiple nodes work correctly
        // it checks the isInterested fucntionality is working properly
        // it checks if the cab is in giving-ride state is should not accept a ride

    
        resetCabActors();

        EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey, "cab101");
        cab101.tell(new Cab.SignIn(0L));
        EntityRef<RideService.Command> rideService1 = rideSharding.entityRefFor(RideService.TypeKey, "rideService1");
        TestProbe<RideService.RideResponse> rideResponseProbe = testKit.createTestProbe();
        rideService1.tell(new RideService.RequestRide("201", 50L, 100L, rideResponseProbe.ref()));
        RideService.RideResponse r = rideResponseProbe.receiveMessage();
        Long rideId = r.rideId;

        EntityRef<RideService.Command> rideService6 = rideSharding.entityRefFor(RideService.TypeKey, "rideService6");
        rideService6.tell(new RideService.RequestRide("201", 150L, 250L, rideResponseProbe.ref()));


        // try {
        //     TimeUnit.SECONDS.sleep(10);
        // } catch (Exception e) {
        //     System.out.println("Error in waiting!!!");//TODO: handle exception
        // }

        r = rideResponseProbe.receiveMessage();
        assertEquals(-1, (long)r.rideId);

        //ending the ride
        cab101.tell(new Cab.RideEnded(rideId));

        EntityRef<RideService.Command> rideService2 = rideSharding.entityRefFor(RideService.TypeKey, "rideService2");
        rideService2.tell(new RideService.RequestRide("202", 200L, 300L, rideResponseProbe.ref()));
        
        // try {
        //     TimeUnit.SECONDS.sleep(20);
        // } catch (Exception e) {
        //     System.out.println("Error in waiting!!!");//TODO: handle exception
        // }
        r = rideResponseProbe.receiveMessage();
        
        assertEquals(-1, (long)r.rideId);

        EntityRef<RideService.Command> rideService4 = rideSharding.entityRefFor(RideService.TypeKey, "rideService4");
        rideService4.tell(new RideService.RequestRide("202", 200L, 300L, rideResponseProbe.ref()));
        
        
        // try {
        //     TimeUnit.SECONDS.sleep(10);
        // } catch (Exception e) {
        //     System.out.println("Error in waiting!!!");//TODO: handle exception
        // }
        r = rideResponseProbe.receiveMessage();
        
        
        assertNotEquals(-1, (long)r.rideId);

    }

    


    @Test
    public void test4(){

        // checks when multiple cabs are available the request are assigned in increasing order
        // also checks when a customer asks for multples cabs, the requests should be satisfied

        resetCabActors();

        EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey, "cab101");
        cab101.tell(new Cab.SignIn(0L));
        EntityRef<Cab.Command> cab102 = cabSharding.entityRefFor(Cab.TypeKey, "cab102");
        cab102.tell(new Cab.SignIn(0L));
        EntityRef<Cab.Command> cab103 = cabSharding.entityRefFor(Cab.TypeKey, "cab103");
        cab103.tell(new Cab.SignIn(0L));
        EntityRef<Cab.Command> cab104 = cabSharding.entityRefFor(Cab.TypeKey, "cab104");
        cab104.tell(new Cab.SignIn(0L));

        EntityRef<RideService.Command> rideService1 = rideSharding.entityRefFor(RideService.TypeKey, "rideService1");
        TestProbe<RideService.RideResponse> rideResponseProbe = testKit.createTestProbe();

        //========================================================================================================

        rideService1.tell(new RideService.RequestRide("201", 0L, 50L, rideResponseProbe.ref()));
        RideService.RideResponse r = rideResponseProbe.receiveMessage();

        assertEquals("101", r.cabId);
        //cab101.tell(new Cab.RideEnded(r.rideId));

        //========================================================================================================
        rideService1.tell(new RideService.RequestRide("201", 50L, 100L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertEquals("102", r.cabId);
        //cab102.tell(new Cab.RideEnded(r.rideId));

        //========================================================================================================
        rideService1.tell(new RideService.RequestRide("201", 100L, 150L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertEquals("103", r.cabId);
        //cab103.tell(new Cab.RideEnded(r.rideId));

        //========================================================================================================

        rideService1.tell(new RideService.RequestRide("201", 150L, 200L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();
        
        assertEquals("104", r.cabId);
        //cab104.tell(new Cab.RideEnded(r.rideId));

    }


    @Test
    public void test5(){

        // this test case checks the numRides fucntionality

        resetCabActors();

        EntityRef<Cab.Command> cab101 = cabSharding.entityRefFor(Cab.TypeKey, "cab101");
        cab101.tell(new Cab.SignIn(0L));

        EntityRef<RideService.Command> rideService1 = rideSharding.entityRefFor(RideService.TypeKey, "rideService1");
        TestProbe<RideService.RideResponse> rideResponseProbe = testKit.createTestProbe();

        RideService.RideResponse r;

        //========================================================================================================
        //1

        rideService1.tell(new RideService.RequestRide("201", 0L, 50L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertNotEquals(-1, (long)r.rideId);
        cab101.tell(new Cab.RideEnded(r.rideId));

        rideService1.tell(new RideService.RequestRide("201", 0L, 50L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertEquals(-1, (long)r.rideId);

        //========================================================================================================
        //2

        rideService1.tell(new RideService.RequestRide("201", 0L, 50L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertNotEquals(-1, (long)r.rideId);
        cab101.tell(new Cab.RideEnded(r.rideId));

        rideService1.tell(new RideService.RequestRide("201", 0L, 50L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertEquals(-1, (long)r.rideId);

        //========================================================================================================
        //3

        rideService1.tell(new RideService.RequestRide("201", 0L, 50L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertNotEquals(-1, (long)r.rideId);
        cab101.tell(new Cab.RideEnded(r.rideId));

        rideService1.tell(new RideService.RequestRide("201", 0L, 50L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertEquals(-1, (long)r.rideId);

        //========================================================================================================
        //4

        rideService1.tell(new RideService.RequestRide("201", 0L, 50L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertNotEquals(-1, (long)r.rideId);
        cab101.tell(new Cab.RideEnded(r.rideId));

        rideService1.tell(new RideService.RequestRide("201", 0L, 50L, rideResponseProbe.ref()));
        r = rideResponseProbe.receiveMessage();

        assertEquals(-1, (long)r.rideId);

        //========================================================================================================

        TestProbe<Cab.NumRideResponse> resetRes = testKit.createTestProbe();
        
        cab101.tell(new Cab.NumRides(resetRes.ref()));

        // try {
        //     TimeUnit.SECONDS.sleep(3);
        // } catch (Exception e) {
        //     System.out.println("Error in waiting!!!");//TODO: handle exception
        // }

        Cab.NumRideResponse resp = resetRes.receiveMessage();
        assertEquals(4, (long)resp.response);
    }

static void resetCabActors() {

    TestProbe<Cab.NumRideResponse> resetactor = testKit.createTestProbe();

    for (long value: Globals.cabs){
        EntityRef<Cab.Command> cab = cabSharding.entityRefFor(Cab.TypeKey, "cab"+value);
        cab.tell(new Cab.Reset(resetactor.ref()));
        
        Cab.NumRideResponse res = resetactor.receiveMessage();


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
}
