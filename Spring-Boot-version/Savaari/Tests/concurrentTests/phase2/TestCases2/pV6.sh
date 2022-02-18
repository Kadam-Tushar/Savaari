#! /bin/sh
# this test case checks Number of rides for
#  invalid, signed-in and riding, signed-in but not riding, 
#  signed out cabs

# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

testPassed="yes"

#Step 1 :  Numrides for of Invalid Cab
resp=$(curl -s "http://localhost:8080/numRides?cabId=9101")
if [ "$resp" != "-1" ];
then
	echo "Invalid Number of ride for an Invalid cab"
	testPassed="no"
else
	echo "-1 for Invalid cab"
fi

#Step 2 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=0")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi

#Step 3 : cab 102 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=102&initialPos=100")
if [ "$resp" = "true" ];
then
	echo "Cab 102 signed in"
else
	echo "Cab 102 could not sign in"
	testPassed="no"
fi


#Step 4 : customer 201 requests a ride
rideId1=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=100")
if [ "$rideId1" != "-1" ];
then
	echo "Ride by customer 201 started"
else
	echo "Ride to customer 201 denied"
		testPassed="no"
fi

#Step 4.1
resp=$(curl -s "http://localhost:8080/signOut?cabId=101")
if [ "$resp" = "true" ];
then
	echo "Cab 101 is signed Out"
	testPassed="no"
else
	echo "Cab 101 could not sign Out"
fi


#Step 5 :  Numrides for of Cab 101
resp=$(curl -s "http://localhost:8080/numRides?cabId=101")
echo $resp
if [ "$resp" != "1" ];
then
	echo "Invalid Number of ride"
	testPassed="no"
else
	echo "Correct number of ride"
fi

#Step 6 :  Numrides for of Cab 102
resp=$(curl -s "http://localhost:8080/numRides?cabId=102")
if [ "$resp" != "0" ];
then
	echo "Invalid Number of ride"
	testPassed="no"
else
	echo "Correct number of ride"
fi

#Step 7 :  Numrides for of Cab 103 ---in Signed OUT state
resp=$(curl -s "http://localhost:8080/numRides?cabId=103")
if [ "$resp" != "0" ];
then
	echo "Invalid Number of ride"
	testPassed="no"
else
	echo "Correct number of ride"
fi

echo "Test Passing Status: " $testPassed