#! /bin/sh
# this test case checks whether a customer's request
# gets rejected due to insufficient balance or not 
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

testPassed="yes"

#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=0")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi

#Step 2 : customer 201 requests a ride
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=1100")
if [ "$rideId" != "-1" ];
then
	echo "Ride by customer 201 started"
	testPassed="no"
else
	echo "Ride to customer 201 denied"
fi


echo "Test Passing Status: " $testPassed