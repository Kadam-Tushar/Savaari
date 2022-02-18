#! /bin/sh
# this test case checks whether a customer's request
# gets rejected if 3 cabs(available in the ascending 
# order of nearness to customer) are not interested the riderequest.
# 1 cab which is intersted for a ride, is kept at far
#   to become 4th from the customer.
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

testPassed="yes"

#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=70")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi

#Step 2 : cab 102 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=102&initialPos=80")
if [ "$resp" = "true" ];
then
	echo "Cab 102 signed in"
else
	echo "Cab 102 could not sign in"
	testPassed="no"
fi

#Step 3 : cab 103 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=103&initialPos=90")
if [ "$resp" = "true" ];
then
	echo "Cab 103 signed in"
else
	echo "Cab 103 could not sign in"
	testPassed="no"
fi



#Step 4 : customer 201 requests a ride
rideDetails=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=100")
rideId1=$(echo $rideDetails | cut -d' ' -f 1)

if [ "$rideId1" != "-1" ];
then
	echo "Ride by customer 201 started"
else
	echo "Ride to customer 201 denied"
		testPassed="no"
fi


#Step 5 : customer 202 requests a ride
rideDetails=$(curl -s "http://localhost:8081/requestRide?custId=202&sourceLoc=10&destinationLoc=110")
rideId2=$(echo $rideDetails | cut -d' ' -f 1)
if [ "$rideId2" != "-1" ];
then
	echo "Ride by customer 202 started"
else
	echo "Ride to customer 202 denied"
		testPassed="no"
fi


#Step 6 : customer 203 requests a ride
rideDetails=$(curl -s "http://localhost:8081/requestRide?custId=203&sourceLoc=10&destinationLoc=120")
rideId3=$(echo $rideDetails | cut -d' ' -f 1)
if [ "$rideId3" != "-1" ];
then
	echo "Ride by customer 203 started"
else
	echo "Ride to customer 203 denied"
		testPassed="no"
fi

#Step 7 : End ride1
resp=$(curl -s "http://localhost:8080/rideEnded?cabId=101&rideId=$rideId1")
if [ "$resp" = "true" ];
then
	echo $rideId1 " has ended"
else
	echo "Could not end" $rideId1
	testPassed="no"
fi


#Step 8 : End ride2
resp=$(curl -s "http://localhost:8080/rideEnded?cabId=102&rideId=$rideId2")
if [ "$resp" = "true" ];
then
	echo $rideId2 " has ended"
else
	echo "Could not end" $rideId2
	testPassed="no"
fi

#Step 9 : End ride3
resp=$(curl -s "http://localhost:8080/rideEnded?cabId=103&rideId=$rideId3")
if [ "$resp" = "true" ];
then
	echo $rideId3 " has ended"
else
	echo "Could not end" $rideId3
	testPassed="no"
fi

#Step 10 : cab 104 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=104&initialPos=0")
if [ "$resp" = "true" ];
then
	echo "Cab 104 signed in"
else
	echo "Cab 104 could not sign in"
	testPassed="no"
fi

#Step 11 : customer 201 requests a ride
rideId1=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=100&destinationLoc=10")
if [ "$rideId1" != "-1" ];
then
	echo "Ride by customer 201 started"
	     testPassed="no"
else
	echo "Ride to customer 201 denied"
	
fi

echo "Test Passing Status: " $testPassed