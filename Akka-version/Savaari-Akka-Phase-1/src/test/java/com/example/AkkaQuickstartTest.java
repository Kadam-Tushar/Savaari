package pods.cabs;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
// import org.junit.ClassRule;
// import org.junit.jupiter.api.MethodOrderer;
// import org.junit.jupiter.api.Order;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.TestMethodOrder;

import org.junit.*;
import org.junit.Assert;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import java.time.Duration.*;
import org.junit.runners.MethodSorters;
import java.util.concurrent.TimeUnit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
//#definition
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AkkaQuickstartTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();
//#definition

    //#test
    

    /*

    Syntax for name of test method : test{x}_{your_actor} where x: test number as test will execute in ascending order 

    */
    @Test
    public void test1_MainActor() {
        TestProbe<Main.Started> testProbe = testKit.createTestProbe();
        ActorRef<Main.Started> MainActor = testKit.spawn(   Main.create(testProbe.getRef())   , "Main");
        Assert.assertEquals("Done",testProbe.receiveMessage().msg);

    }

    @Test
    public void test2_wallet(){

        /*
1           Checking functionality of getBalance ,addBalanace , deductBalance, reset of wallet actor.
         */
        RESETALL();

        TestProbe<Wallet.ResponseBalance> balanceProbe = testKit.createTestProbe();

        Globals.wallets.get("201").tell(new Wallet.GetBalance(balanceProbe.ref()));
        balanceProbe.expectMessage(new Wallet.ResponseBalance(10000L));

        Globals.wallets.get("202").tell(new Wallet.AddBalance(100L));
        Globals.wallets.get("202").tell(new Wallet.GetBalance(balanceProbe.ref()));
        balanceProbe.expectMessage(new Wallet.ResponseBalance(10100L));

        Globals.wallets.get("203").tell(new Wallet.DeductBalance(1000L,balanceProbe.ref()));
        balanceProbe.expectMessage(new Wallet.ResponseBalance(9000L));

        Globals.wallets.get("202").tell(new Wallet.Reset(balanceProbe.ref()));
        balanceProbe.expectMessage(new Wallet.ResponseBalance(10000L));

        Globals.wallets.get("203").tell(new Wallet.Reset(balanceProbe.ref()));
        balanceProbe.expectMessage(new Wallet.ResponseBalance(10000L));

    }

    @Test
    public void test3_Cab() {
        
        /*
1           Checking functionality of signIN and SignOut and getCabStatus
         */
        RESETALL();

        TestProbe<Cab.GetCabStatusResponce> cabProbe = testKit.createTestProbe();
        
        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("signed-out -1"));

        Globals.cabs.get("101").tell(new Cab.SignIn(100L));
        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("available 100"));

        Globals.cabs.get("101").tell(new Cab.SignOut());
        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("signed-out -1"));


    }

    @Test
    public void test4_Cab() {
        /*
            Checking functionality of requestRide using internal method getCAbStatus


        */

        RESETALL();

        TestProbe<Cab.GetCabStatusResponce> cabProbe = testKit.createTestProbe();
        
        Globals.cabs.get("101").tell(new Cab.SignIn(0L));
        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("available 0"));


        TestProbe<RideService.RideResponse> rideResponseProbe = testKit.createTestProbe();

        Globals.rideService.get(0L).tell(new RideService.RequestRide("201", 50L, 100L, rideResponseProbe.ref()));
        //rideResponseProbe.expectMessage(new RideService.RideResponse(0L, "101", 1000L, null));
    
        RideService.RideResponse r = rideResponseProbe.receiveMessage();
        assertEquals("101", r.cabId);
        assertEquals(1000,(long)r.fare);

        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("giving-ride 50 "+r.rideId+" 100"));

        TestProbe<RideService.Started> probe = testKit.createTestProbe();
        Globals.rideService.get(0L).tell(new RideService.Reset(probe.ref()));
        probe.expectMessage(new RideService.Started("Done"));


        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("signed-out -1"));
    }

    @Test
    public void test5_extra_functionality(){
        /*
            Implemented extra functionality : RideService Actors will know when some cab goes into giving ride state.
            Checking that functionality in this test case.
        */
        RESETALL();

        TestProbe<Cab.GetCabStatusResponce> cabProbe = testKit.createTestProbe();
        TestProbe<RideService.Started> rideStartedActor = testKit.createTestProbe();
        TestProbe<RideService.RideResponse> rideResponseProbe = testKit.createTestProbe();
        
        
        Globals.cabs.get("101").tell(new Cab.SignIn(0L));
        //Need to send Get Cab status for SignIn. This is a trict that works because we wait for reply of GetCabStatusResponce and hence CabSign ins also done
        //But, it is not guaranteed that the cache table is also updated
        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("available 0"));



        Globals.rideService.get(0L).tell(new RideService.GetCabStatus("101", rideStartedActor.ref()));
        rideStartedActor.expectMessage(new RideService.Started("available"));
        System.out.println("\nPASSED 1\n");

        Globals.rideService.get(0L).tell(new RideService.RequestRide("201", 50L, 100L, rideResponseProbe.ref()));
    
        RideService.RideResponse r = rideResponseProbe.receiveMessage();
        assertEquals("101", r.cabId);
        assertEquals(1000,(long)r.fare);
        System.out.println("\nPASSED 2\n");
        
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            System.out.println("Error in waiting!!!");//TODO: handle exception
        }

        Globals.rideService.get(1L).tell(new RideService.GetCabStatus("101", rideStartedActor.ref()));
        rideStartedActor.expectMessage(new RideService.Started("giving-ride"));
        System.out.println("\nPASSED 3\n");


    }

    @Test
    public void test6_Pub() {
        // Public test case provided by sir
        RESETALL();

        ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
        TestProbe<Cab.GetCabStatusResponce> cabProbe = testKit.createTestProbe();

        cab101.tell(new Cab.SignIn(10L));
        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("available 10"));

        ActorRef<RideService.Command> rideService = Globals.rideService.get(0L);
            // If we are going to raise multiple requests in this script,
            // better to send them to different RideService actors to achieve
            // load balancing.

        TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

        rideService.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));

        RideService.RideResponse resp = probe.receiveMessage();
            // Blocks and waits for a response message.
            // There is also an option to block for a bounded period of time
            // and give up after timeout.
        assertNotEquals((long)resp.rideId, -1);
        cab101.tell(new Cab.RideEnded(resp.rideId));
    }

    @Test
    public void test7() {

        /*
        Showing that when requestRide is called on multipler rideSerivice actors then only one of them returns true erven if their
        cacheTables are not consistent and only one cab is available.

        */

        RESETALL();

        TestProbe<Cab.GetCabStatusResponce> cabProbe = testKit.createTestProbe();

        Globals.cabs.get("101").tell(new Cab.SignIn(5L));
        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("available 5"));

        //=================================================================================================

        TestProbe<RideService.RideResponse> probe_0 = testKit.createTestProbe();
        TestProbe<RideService.RideResponse> probe_1 = testKit.createTestProbe();

        Globals.rideService.get(0L).tell(new RideService.RequestRide("201", 1L, 5L, probe_0.ref()));
        Globals.rideService.get(1L).tell(new RideService.RequestRide("202", 1L, 5L, probe_1.ref()));


        RideService.RideResponse resp0 = probe_0.receiveMessage();
        RideService.RideResponse resp1 = probe_1.receiveMessage();
            // Blocks and waits for a response message.
            // There is also an option to block for a bounded period of time
            // and give up after timeout.

        int dummy = 0;
        if((long)resp0.rideId == -1 && (long)resp1.rideId == -1)
            dummy = -1;
        if((long)resp0.rideId != -1 && (long)resp1.rideId != -1)
            dummy = -1;

        assertEquals(dummy, 0);
        //=================================================================================================
      
    }

    @Test
    public void test8() {

        /*
            Test case to show that messages are propagated to all RideService actors on a call to cab.RideEnded 

        */

        RESETALL();

        TestProbe<RideService.Started> rideStartedActor = testKit.createTestProbe();
        TestProbe<Cab.GetCabStatusResponce> cabProbe = testKit.createTestProbe();

        Globals.cabs.get("101").tell(new Cab.SignIn(5L));
        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("available 5"));

        //=================================================================================================

        TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();
        

        Globals.rideService.get(0L).tell(new RideService.RequestRide("201", 1L, 5L, probe.ref()));


        RideService.RideResponse resp = probe.receiveMessage();

        
        assertNotEquals((long)resp.rideId, -1);
        //=================================================================================================
        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("giving-ride 1 "+resp.rideId+" 5"));
        //=================================================================================================

        Globals.cabs.get(resp.cabId).tell(new Cab.RideEnded(resp.rideId));

        //=================================================================================================

        Globals.cabs.get("101").tell(new Cab.GetCabStatus(cabProbe.ref()));
        cabProbe.expectMessage(new Cab.GetCabStatusResponce("available 5"));
        //=================================================================================================

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            System.out.println("Error in waiting!!!");//TODO: handle exception
        }

        Globals.rideService.get(1L).tell(new RideService.GetCabStatus("101", rideStartedActor.ref()));
        rideStartedActor.expectMessage(new RideService.Started("available"));

        
        
    }
    



    public void RESETALL() {
        //Resetting the wallets
        TestProbe<Wallet.ResponseBalance> resetwalletprobe = testKit.createTestProbe();
        for(String custId : Globals.wallets.keySet()) {
            Globals.wallets.get(custId).tell(new Wallet.Reset(resetwalletprobe.ref()));
            resetwalletprobe.expectMessage(new Wallet.ResponseBalance(10000L));
        }

        //Resetting the cabs
        TestProbe<RideService.Started> probe = testKit.createTestProbe();
        Globals.rideService.get(0L).tell(new RideService.Reset(probe.ref()));
        probe.expectMessage(new RideService.Started("Done"));


    }
}
