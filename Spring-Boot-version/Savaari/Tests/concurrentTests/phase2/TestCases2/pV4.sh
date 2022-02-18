#! /bin/sh
# this test case checks whether a customer's request
#  gets rejected even if fewer than 3 cabs are contacted 
#  and all they all are not interested and the remaining 
#  is either not signed in or on ride.
# every test case should begin with these two steps

curl -s http://localhost:8081/reset
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

testPassed="yes"

#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=10")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi

#Step 2 : cab 102 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=102&initialPos=30")
if [ "$resp" = "true" ];
then
	echo "Cab 102 signed in"
else
	echo "Cab 102 could not sign in"
	testPassed="no"
fi


#Step 3 : customer 201 requests a ride
rideDetails=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=100")
rideId1=$(echo $rideDetails | cut -d' ' -f 1)
if [ "$rideId1" != "-1" ];
then
	echo "Ride by customer 201 started"
else
	echo "Ride to customer 201 denied"
		testPassed="no"
fi


#Step 4 : customer 202 requests a ride
rideDetails=$(curl -s "http://localhost:8081/requestRide?custId=202&sourceLoc=10&destinationLoc=110")
rideId2=$(echo $rideDetails | cut -d' ' -f 1)
if [ "$rideId2" != "-1" ];
then
	echo "Ride by customer 202 started"
else
	echo "Ride to customer 202 denied"
		testPassed="no"
fi


#Step 5 : End ride1
resp=$(curl -s "http://localhost:8080/rideEnded?cabId=101&rideId=$rideId1")
echo $resp
if [ "$resp" = "true" ];
then
	echo $rideId1 " has ended"
else
	echo "Could not end" $rideId1
	testPassed="no"
fi


#Step 6 : customer 201 requests a ride
rideId3=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=100&destinationLoc=10")
if [ "$rideId3" != "-1" ];
then
	echo "Ride by customer 201 started"
	     testPassed="no"
else
	echo "Ride to customer 201 denied"
	
fi

#Step 7 : customer 201 requests a ride again
rideId4=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=100&destinationLoc=10")
if [ "$rideId4" != "-1" ];
then
	echo "Ride by customer 201 started"
else
	echo "Ride to customer 201 denied"
		     testPassed="no"

	
fi


echo "Test Passing Status: " $testPassed