///*
package pods.cabs;
//package com.example;
/*import pods.cabs.Cab;
import pods.cabs.Globals;
import pods.cabs.Main;
import pods.cabs.RideService;
import pods.cabs.RideService.CabSignsIn;
import pods.cabs.Wallet;
import pods.cabs.models.CabStatus;
import pods.cabs.utils.InitFileReader;
import pods.cabs.utils.Logger;
*/

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import jdk.internal.vm.annotation.ReservedStackAccess;

import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
//#definition
public class AkkaPrivateTestCases {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();
//#definition


 /*@Test
    public void testMain() throws InterruptedException{

		testCase0();
		// testCase1();
		// testCase2();		//deplay problem
		// testCase3();
		// testCase4();
		 testCase5();		//null ptr at FullfillRide.java:144
		 testCase6();		//null ptr at FullfillRide.java:144
		// testCase7();		//deplay problem
		// testCase8();
		// testCase9();
		// testCase10();
}*/

@Test
    public void testCase0() {
        TestProbe<Main.Started> testProbe = testKit.createTestProbe();
        ActorRef<Main.Started> MainActor = testKit.spawn(   Main.create(testProbe.getRef())   , "Main");
        assertEquals("Done",testProbe.receiveMessage().msg);

    }

 @Test
    public void testCase1() throws InterruptedException{
		//This Testcase checks whether a ride request for a cab on givingride is rejected or not.
						
        System.out.println("### Test Case 1 Started ###");
		RESETALL();
        // TestProbe<Main.Started> testProbe = testKit.createTestProbe();
        // ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main1");
		//  MainAct.tell(new Main.Started(false));
        // assertEquals(true, testProbe.receiveMessage().response);

		
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
		//cab101.tell(new Cab.SignIn(10));
		cab101.tell(new Cab.SignIn(10L));
		//ActorRef<RideService.Command> rideService = Globals.rideService[0];
		ActorRef<RideService.Command> rideService = Globals.rideService.get(0L);

		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();
		RideService.RideResponse resp = null;

		//Delay
		TimeUnit.SECONDS.sleep(1);

		//System.out.println("$$$$ Customer 201 is requesting Ride $$$$");
		//rideService.tell(new RideService.RequestRide("201", 10, 100, probe.ref()));
		rideService.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
		resp = probe.receiveMessage();

		if(resp.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
		else
			System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

		//assertTrue(resp.rideId != -1); 
		assertTrue((long)resp.rideId != -1); 

		//Delay
		TimeUnit.SECONDS.sleep(1);

		//ActorRef<RideService.Command> rideService1 = Globals.rideService[1];
		ActorRef<RideService.Command> rideService1 = Globals.rideService.get(1L);
		//rideService1.tell(new RideService.RequestRide("202", 10, 100, probe.ref()));
		rideService1.tell(new RideService.RequestRide("202", 10L, 100L, probe.ref()));
		resp = probe.receiveMessage();

		//if(resp.rideId !=-1)
		if((long)resp.rideId !=-1)
			System.out.println("XXXX Ride request from Customer 202 is accepted - Ride ID : " + resp.rideId);
		else
			System.out.println("$$$$ Ride request from Customer 202 is Denied ! ");

		//assertTrue(resp.rideId == -1);
		assertTrue((long)resp.rideId == -1);
		//Request should be denied as the cab101 is in givingride status

		cab101.tell(new Cab.RideEnded(resp.rideId));

	}



	@Test
	public void testCase2() throws InterruptedException{
		//This Testcase verifies that a riderequest is never allowed if the balance amount in the customer wallet is insufficient.
		//Also this test will add amount to wallet, 
		//               checks the balance in customer wallet
		//               and ensures a previously denied riderequest is allowed due to sufficient balance.
		
			
        System.out.println("### Test Case 2 Started ###");
		RESETALL();
		// TestProbe<Main.Started> testProbe = testKit.createTestProbe();
		// ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main2");
		// MainAct.tell(new Main.Started(false));
		// assertEquals(true, testProbe.receiveMessage().response);



		//TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");


		//Cab101 signs In
		cab101.tell(new Cab.SignIn(10L));

		//Delay
		TimeUnit.SECONDS.sleep(1);

		//ActorRef<RideService.Command> rideService = Globals.rideService[0];
		ActorRef<RideService.Command> rideService = Globals.rideService.get(0L);
		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

		//Ride request should not be satisfied due to insufficient balance
		 rideService.tell(new RideService.RequestRide("201", 0L, 2000L, probe.ref()));
		 RideService.RideResponse resp = probe.receiveMessage(Duration.ofSeconds(5));

 		 if((long)resp.rideId !=-1)
			System.out.println("XXXX Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
		 else
			System.out.println("$$$$ Ride request from Customer 201 is Denied ! ");


		assertTrue((long)resp.rideId == -1);
		
                    // Add sufficient balance for a particular long ride.  		
		ActorRef<Wallet.Command> cust201 = Globals.wallets.get("201");
		cust201.tell(new Wallet.AddBalance(30000L));
		
					//Checks for the balance in customer wallet   ---- may fail sometimes since Addbalance have no response.
		TestProbe<Wallet.ResponseBalance> Wprobe = testKit.createTestProbe();
		//cust201.tell(new Wallet.GetBalance(1L,Wprobe.ref()));
		cust201.tell(new Wallet.GetBalance(Wprobe.ref()));
		Wallet.ResponseBalance wresp = Wprobe.receiveMessage(Duration.ofSeconds(10));
		//System.out.println("### Balance for the Customer 201 : " + wresp.bal);
		System.out.println("### Balance for the Customer 201 : " + wresp.balance);
		//assertTrue(wresp.bal == 40000L);     //need to ensure variable name balance
		assertTrue(wresp.balance == 40000L);
		
		
		  // Two requests are made to ensure cab allocation.
		  //The last request in the previous loop may be denied due to disinterest.


		//Delay
		//TimeUnit.SECONDS.sleep(1);

		//Ride request should not be satisfied due to insufficient balance
		rideService.tell(new RideService.RequestRide("201", 1L, 2000L, probe.ref()));
		resp = probe.receiveMessage();

		if((long)resp.rideId !=-1)
			System.out.println("XXXX Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
		else
			System.out.println("$$$$ Ride request from Customer 201 is Denied ! ");

		assertTrue((long)resp.rideId == -1);    //ride should not be allowed.
		
		rideService.tell(new RideService.RequestRide("201", 1L, 2000L, probe.ref()));
		resp = probe.receiveMessage();

		if((long)resp.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
		else
			System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

		assertTrue((long)resp.rideId != -1);    //ride should be allowed.
		
		cab101.tell(new Cab.RideEnded(resp.rideId));

	}


	@Test
  
    public void testCase3() throws InterruptedException{
		//This Testcase checks the number of rides for cabs in different status.
		
		
		System.out.println("### Test Case 3 Started ###");
		RESETALL();
		// TestProbe<Main.Started> testProbe = testKit.createTestProbe();
		// ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main3");
		// MainAct.tell(new Main.Started(false));
		// assertEquals(true, testProbe.receiveMessage().response);


		//TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
		
		//number of rides for signed out cab.
		cab101.tell(new Cab.NumRides(Cprobe.ref()));
		Cab.NumRideResponse Cresp = Cprobe.receiveMessage();

		System.out.println("### Number of Rides for Cab 101 in SignedOut Stage : " + Cresp.response);
		//assertTrue((long)Cresp.numRides == 0);     //need to ensure variable name numRides
		assertTrue((long)Cresp.response == 0);

		//Cab101 signs In
		cab101.tell(new Cab.SignIn(10L));
		
		//Checks the number of rides for signed in cab. 
		//Though Cab.SignIn has no response, the numRides should return 0, 
		    //since for Signed Out/just Signed In  Cab,  numRides is 0. 
		
		cab101.tell(new Cab.NumRides(Cprobe.ref()));
		Cresp = Cprobe.receiveMessage();
		System.out.println("### Number of Rides for Cab 101 in just SignedIn Stage : " + Cresp.response);
		//assertTrue(Cresp.numRides == 0);
		assertTrue((long)Cresp.response == 0);     
				
		
		//Request For a ride 	   
		//Single request is enough
		// ActorRef<RideService.Command> rideService = Globals.rideService[0];
		ActorRef<RideService.Command> rideService = Globals.rideService.get(0L);
		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

		//Ride request should be accepted
		rideService.tell(new RideService.RequestRide("201", 0L, 100L, probe.ref()));
		RideService.RideResponse resp = probe.receiveMessage(Duration.ofSeconds(5));

		if((long)resp.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
		else
			System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

       	 assertTrue((long)resp.rideId != -1);
		
		//Checks for the Number of Rides for the cab in givingride status--- 
		cab101.tell(new Cab.NumRides(Cprobe.ref()));
		Cresp = Cprobe.receiveMessage();
		System.out.println("### Number of Rides for Cab 101 in giving-ride status : " + Cresp.response);
		assertTrue(Cresp.response == 1);

		
		cab101.tell(new Cab.RideEnded(resp.rideId));

	}


	@Test
    public void testCase4() throws InterruptedException{
		//This Testcase checks whether a disinterested cab is allocated for a ride or not .
		
		
		System.out.println("### Test Case 4 Started ###");
		RESETALL();
		// TestProbe<Main.Started> testProbe = testKit.createTestProbe();
		// ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main4");
		// MainAct.tell(new Main.Started(false));
		// assertEquals(true, testProbe.receiveMessage().response);
		
		TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
				
		//Cab101 signs In
		cab101.tell(new Cab.SignIn(10L));
		
		//Checks the number of rides for signed in cab. 
		
		cab101.tell(new Cab.NumRides(Cprobe.ref()));
		Cab.NumRideResponse Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 0);     
				
		//Request For a ride and should be allowed with one request itself as Cab.NumRides had response
		//ActorRef<RideService.Command> rideService = Globals.rideService[0];
		ActorRef<RideService.Command> rideService = Globals.rideService.get(0L);
		//ActorRef<RideService.Command> rideService1 = Globals.rideService[1];
		ActorRef<RideService.Command> rideService1 = Globals.rideService.get(1L);
		
		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

		 rideService.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
		 RideService.RideResponse resp = probe.receiveMessage();

		if((long)resp.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
		else
			System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

		assertTrue((long)resp.rideId != -1);
		cab101.tell(new Cab.RideEnded(resp.rideId));

		//Delay
		TimeUnit.SECONDS.sleep(1);

		 //Again requests for a cab101 that should be rejected due to disinterest. 
		 //But this may happen since rideEnded has no response and 
		     //the internal caches are not updated with available status of Cab101.  
		 rideService1.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
		 resp = probe.receiveMessage();

		if((long)resp.rideId !=-1)
			System.out.println("XXXX Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
		else
			System.out.println("$$$$ Ride request from Customer 201 is Denied ! ");

		assertTrue((long)resp.rideId == -1);
		 
	}


	@Test
    public void testCase5() throws InterruptedException{
		//This Testcase checks the following scenario.
		// 4 cabs signed in, 3 ride requests are accepted and ended. 
		// So all these 3 cabs are disinterested for next ride.
		//4th request is far away from the available, interested cab.
		// So the 4th ride request should not be allowed.
		
		
		System.out.println("### Test Case 5 Started ###");
		RESETALL();
		// TestProbe<Main.Started> testProbe = testKit.createTestProbe();
		// ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main5");
		// MainAct.tell(new Main.Started(false));
		// assertEquals(true, testProbe.receiveMessage().response);
		
		TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
		ActorRef<Cab.Command> cab102 = Globals.cabs.get("102");
		ActorRef<Cab.Command> cab103 = Globals.cabs.get("103");
		ActorRef<Cab.Command> cab104 = Globals.cabs.get("104");
				
				
		//Cabs sign In
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

		//Request For a ride and should be allowed with one request itself as Cab.NumRides had response
		ActorRef<RideService.Command> rideService1 = Globals.rideService.get(0L);
		ActorRef<RideService.Command> rideService2 = Globals.rideService.get(1L);
        ActorRef<RideService.Command> rideService3 = Globals.rideService.get(2L);
		ActorRef<RideService.Command> rideService4 = Globals.rideService.get(3L);
		
		
		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

		 rideService1.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
		 RideService.RideResponse resp1 = probe.receiveMessage();

		 if((long)resp1.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp1.rideId);
		 else
			System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

		 assertTrue((long)resp1.rideId != -1);
		 
		 rideService2.tell(new RideService.RequestRide("202", 10L, 110L, probe.ref()));
		 RideService.RideResponse resp2 = probe.receiveMessage();
		 if((long)resp2.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 202 is accepted - Ride ID : " + resp2.rideId);
		 else
			System.out.println("XXXX Ride request from Customer 202 is Denied ! ");

		 assertTrue((long)resp2.rideId != -1);
		 
		 rideService3.tell(new RideService.RequestRide("203", 10L, 120L, probe.ref()));
		 RideService.RideResponse resp3 = probe.receiveMessage();
		if((long)resp3.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 203 is accepted - Ride ID : " + resp3.rideId);
		else
			System.out.println("XXXX Ride request from Customer 203 is Denied ! ");

		 assertTrue((long)resp3.rideId != -1);
		 

		 // Now the cabs are known either as disinterested 
		 //   or not available (due to delay in internal cache update).
		//Delay
		TimeUnit.SECONDS.sleep(5);

		// cab101.tell(new Cab.RideEnded(resp1.rideId));
		// cab102.tell(new Cab.RideEnded(resp2.rideId));
		// cab103.tell(new Cab.RideEnded(resp3.rideId));

		TimeUnit.SECONDS.sleep(5);

		 rideService4.tell(new RideService.RequestRide("201", 100L, 10L, probe.ref()));
		 RideService.RideResponse resp4 = probe.receiveMessage();

		 //Cab 101,102 and 103 (3 cabs) rejects the requests
		if((long)resp4.rideId !=-1)
			System.out.println("XXXX Ride request from Customer 201 is accepted - Ride ID : " + resp4.rideId);
		else
			System.out.println("$$$$ Ride request from Customer 201 is Denied ! ");
       	assertTrue((long)resp4.rideId != -1);


	}




	@Test
    public void testCase6() throws InterruptedException{
		//This Testcase checks the following scenario.
		// 4 cabs signed in, 3 ride requests are accepted and ended. 
		// So all these 3 cabs are disinterested for next ride.
		//4th request for the available, interested cab should be allowed.
		
		
		System.out.println("### Test Case 6 Started ###");
		RESETALL();
		// TestProbe<Main.Started> testProbe = testKit.createTestProbe();
		// ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main6");
		// MainAct.tell(new Main.Started(false));
		// assertEquals(true, testProbe.receiveMessage().response);
		
		TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
		ActorRef<Cab.Command> cab102 = Globals.cabs.get("102");
		ActorRef<Cab.Command> cab103 = Globals.cabs.get("103");
		ActorRef<Cab.Command> cab104 = Globals.cabs.get("104");
				
				
		//Cabs sign In
		cab101.tell(new Cab.SignIn(10L));
		cab102.tell(new Cab.SignIn(10L));
		cab103.tell(new Cab.SignIn(10L));
		cab104.tell(new Cab.SignIn(105L));
		
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

		//Request For a ride and should be allowed with one request itself as Cab.NumRides had response
		ActorRef<RideService.Command> rideService1 = Globals.rideService.get(0L);
		ActorRef<RideService.Command> rideService2 = Globals.rideService.get(1L);
        ActorRef<RideService.Command> rideService3 = Globals.rideService.get(2L);
		ActorRef<RideService.Command> rideService4 = Globals.rideService.get(3L);
		
		
		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

		 rideService1.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
		 RideService.RideResponse resp1 = probe.receiveMessage();

		if((long)resp1.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp1.rideId);
		else
			System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

		assertTrue((long)resp1.rideId != -1);

		rideService2.tell(new RideService.RequestRide("202", 10L, 110L, probe.ref()));
		RideService.RideResponse resp2 = probe.receiveMessage();
		if((long)resp2.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 202 is accepted - Ride ID : " + resp2.rideId);
		else
			System.out.println("XXXX Ride request from Customer 203 is Denied ! ");

		assertTrue((long)resp2.rideId != -1);

		rideService3.tell(new RideService.RequestRide("203", 10L, 120L, probe.ref()));
		RideService.RideResponse resp3 = probe.receiveMessage();
		if((long)resp3.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 203 is accepted - Ride ID : " + resp3.rideId);
		else
			System.out.println("XXXX Ride request from Customer 203 is Denied ! ");

		assertTrue((long)resp3.rideId != -1);


		// Now the cabs are known either as disinterested
		//   or not available (due to delay in internal cache update).
		//Delay
		TimeUnit.SECONDS.sleep(1);
		 rideService4.tell(new RideService.RequestRide("203", 100L, 10L, probe.ref()));
		 RideService.RideResponse resp4 = probe.receiveMessage();
		 //cab 104 should be allocated for ride
       	 assertTrue((long)resp4.rideId != -1);	
		 
	}


	@Test
    public void testCase7() throws InterruptedException{
		//This Testcase checks the following scenario.
		// 2 cabs signed in, 2 ride requests are accepted and ended. 
		// So these 2 cabs are disinterested for next ride.
		//3rd request for the available, disinterested cabs are rejected.
		// So even less than 3 cabs are available and rejected the request,
		 // ...customer request should be rejected.
		
		
		System.out.println("### Test Case 7 Started ###");
		RESETALL();
		// TestProbe<Main.Started> testProbe = testKit.createTestProbe();
		// ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main7");
		// MainAct.tell(new Main.Started(false));
		// assertEquals(true, testProbe.receiveMessage().response);
		
		TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
		ActorRef<Cab.Command> cab102 = Globals.cabs.get("102");
		
				
		//Cabs sign In
		cab101.tell(new Cab.SignIn(10L));
		cab102.tell(new Cab.SignIn(10L));
		
		cab101.tell(new Cab.NumRides(Cprobe.ref()));
		Cab.NumRideResponse Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 0); 
		
		cab102.tell(new Cab.NumRides(Cprobe.ref()));
		Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 0);

		System.out.println("### Cab 101 and 102 are Signed In");

		//Request For a ride and should be allowed with one request itself as Cab.NumRides had response
		ActorRef<RideService.Command> rideService1 = Globals.rideService.get(0L);
		ActorRef<RideService.Command> rideService2 = Globals.rideService.get(1L);
		ActorRef<RideService.Command> rideService3 = Globals.rideService.get(2L);
		
		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

		rideService1.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
		RideService.RideResponse resp1 = probe.receiveMessage();

		if((long)resp1.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp1.rideId);
		else
			System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

		assertTrue((long)resp1.rideId != -1);

		rideService2.tell(new RideService.RequestRide("202", 10L, 110L, probe.ref()));
		RideService.RideResponse resp2 = probe.receiveMessage();
		if((long)resp2.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 202 is accepted - Ride ID : " + resp2.rideId);
		else
			System.out.println("XXXX Ride request from Customer 203 is Denied ! ");

		assertTrue((long)resp2.rideId != -1);

  		 cab101.tell(new Cab.RideEnded(resp1.rideId));
  		 cab102.tell(new Cab.RideEnded(resp2.rideId));

		 // Now the cabs are known either as disinterested 
		 //   or not available (due to delay in internal cache update).
		 
		 rideService3.tell(new RideService.RequestRide("203", 100L, 10L, probe.ref()));
		 RideService.RideResponse resp3 = probe.receiveMessage();
		//ride should be denied.
		if((long)resp3.rideId !=-1)
			System.out.println("XXXX Ride request from Customer 201 is accepted - Ride ID : " + resp3.rideId);
		else
			System.out.println("$$$$ Ride request from Customer 201 is Denied ! ");
		assertTrue((long)resp3.rideId == -1);
	}




	@Test
    public void testCase8() throws InterruptedException{
		//This Testcase checks the following scenario.
		// 1 cabs signed in, 1 ride requests are accepted and ended. 
		// Afterwards 2nd ride request should be accepted.
		// This test case verifies whether a ride request is denied
		// .. due to internal cache update delay, even when the cab is available.
		
		
		System.out.println("### Test Case 8 Started ###");
		RESETALL();
		// TestProbe<Main.Started> testProbe = testKit.createTestProbe();
		// ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main8");
		// MainAct.tell(new Main.Started(false));
		// assertEquals(true, testProbe.receiveMessage().response);

		TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");	
				
		//Cabs sign In
		cab101.tell(new Cab.SignIn(10L));
		
		
		cab101.tell(new Cab.NumRides(Cprobe.ref()));
		Cab.NumRideResponse Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 0); 
		
		//Request For a ride and should be allowed with one request itself as Cab.NumRides had response
		ActorRef<RideService.Command> rideService1 = Globals.rideService.get(0L);
		ActorRef<RideService.Command> rideService9 = Globals.rideService.get(9L); //different actor for the second request
		
		
		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();

		 rideService1.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
		 RideService.RideResponse resp1 = probe.receiveMessage();
       	 assertTrue((long)resp1.rideId != -1);	
		 	 
  		 cab101.tell(new Cab.RideEnded(resp1.rideId));
  		

		//request should be denied as the cab101 is disinterested
		//But denial may happen due to no-response request before
		// .. or internal cache update delay. Then the cab is shown as givingride.
		 rideService9.tell(new RideService.RequestRide("202", 10L, 100L, probe.ref()));
		 RideService.RideResponse resp2 = probe.receiveMessage();
       	 assertTrue((long)resp2.rideId == -1);	

         // ride should be allocated.
		 rideService9.tell(new RideService.RequestRide("202", 10L, 100L, probe.ref()));
		 resp2 = probe.receiveMessage();
       	 assertTrue((long)resp2.rideId != -1);	

	}

	@Test
	public void testCase9() throws InterruptedException{
		//This Testcase do the stress test.
        // Also verifies the consistency of the balance in customer wallet
		// ..after a series of ride requests.
        int allotCount=0,denialCount=0;
		System.out.println("### Test Case 9 Started ###");
		RESETALL();
		// TestProbe<Main.Started> testProbe = testKit.createTestProbe();
		// ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main9");
		// MainAct.tell(new Main.Started(false));
		// assertEquals(true, testProbe.receiveMessage().response);

		TestProbe<Cab.NumRideResponse> Cprobe = testKit.createTestProbe();
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");


		//Cabs sign In
		cab101.tell(new Cab.SignIn(3L));

		cab101.tell(new Cab.NumRides(Cprobe.ref()));
		Cab.NumRideResponse Cresp = Cprobe.receiveMessage();
		assertTrue((long)Cresp.response == 0);

		System.out.println("### Cab 101 is  Signed In");

		//Request For a ride and should be allowed with one request itself as Cab.NumRides had response
		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();
		int step=3, noOfSteps=200;
		for(int i=0;i<noOfSteps;i++)
		{
			ActorRef<RideService.Command> rideService = Globals.rideService.get(i%10L);

			rideService.tell(new RideService.RequestRide("201", 0L, (long)step, probe.ref()));
			RideService.RideResponse resp = probe.receiveMessage();

			if((long)resp.rideId !=-1) {
				System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
				cab101.tell(new Cab.RideEnded(resp.rideId));
				System.out.println("$$$$ Ride Has ended");
				allotCount++;
			}
			else {
				System.out.println("$$$$ Ride request from Customer 201 is Denied ! ");
				denialCount++;
			}
		}

		System.out.println("#### Total Rides : " + allotCount + " Denial of rides : " + denialCount);

		ActorRef<Wallet.Command> cust201 = Globals.wallets.get("201");
		TestProbe<Wallet.ResponseBalance> Wprobe = testKit.createTestProbe();
		//cust201.tell(new Wallet.GetBalance(1,Wprobe.ref()));
		cust201.tell(new Wallet.GetBalance(Wprobe.ref()));
		Wallet.ResponseBalance wresp = Wprobe.receiveMessage(Duration.ofSeconds(10));
		System.out.println("### Balance for the Customer 201 : " + wresp.balance);
		System.out.println("### Expected Balance  : " + (10000- allotCount*step*2*10));
		assertTrue((long)wresp.balance == (10000- allotCount*step*2*10) );     //need to ensure variable name balance
	}

	@Test
	public void testCase10() throws InterruptedException{
		//This Testcase checks whether a ride request for a cab on givingride is rejected or not.


		System.out.println("### Test Case 10 Started ###");
		RESETALL();
		// TestProbe<Main.Started> testProbe = testKit.createTestProbe();
		// ActorRef<Main.Started> MainAct = testKit.spawn(Main.create(testProbe.getRef()), "main10");
		// MainAct.tell(new Main.Started(false));
		// assertEquals(true, testProbe.receiveMessage().response);


		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
		cab101.tell(new Cab.SignIn(10L));

		ActorRef<Cab.Command> cab102= Globals.cabs.get("102");
		cab102.tell(new Cab.SignIn(10L));

		ActorRef<RideService.Command> rideService1 = Globals.rideService.get(0L);
		ActorRef<RideService.Command> rideService2 = Globals.rideService.get(1L);

		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();
		TestProbe<RideService.RideResponse> probe2 = testKit.createTestProbe();

		RideService.RideResponse resp=null;

		//Delay
		TimeUnit.SECONDS.sleep(1);

		//System.out.println("$$$$ Customer 201 is requesting Ride $$$$");
		rideService1.tell(new RideService.RequestRide("201", 10L, 100L, probe.ref()));
		resp = probe.receiveMessage();

		if((long)resp.rideId !=-1)
			System.out.println("$$$$ Ride request from Customer 201 is accepted - Ride ID : " + resp.rideId);
		else
			System.out.println("XXXX Ride request from Customer 201 is Denied ! ");

		assertTrue((long)resp.rideId != -1);

		//Delay
//		TimeUnit.SECONDS.sleep(1);


		rideService2.tell(new RideService.RequestRide("202", 10L, 100L, probe.ref()));
		resp = probe.receiveMessage();

		if((long)resp.rideId !=-1)
			System.out.println("XXXX Ride request from Customer 202 is accepted - Ride ID : " + resp.rideId);
		else
			System.out.println("$$$$ Ride request from Customer 202 is Denied ! ");

		assertTrue((long)resp.rideId != -1);


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
//*/