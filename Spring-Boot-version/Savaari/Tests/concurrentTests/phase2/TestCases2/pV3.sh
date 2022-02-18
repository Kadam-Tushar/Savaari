#! /bin/sh
# this test case checks whether a customer's request
# for long ride get processed  due to wrong distance calculation
# even when sufficient amount is not available in Wallet.
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

testPassed="yes"

#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=900")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi


#Step 2 : customer 201 requests a ride from 1000 to 0.
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=1000&destinationLoc=0")
if [ "$rideId" != "-1" ];
then
	echo "Ride by customer 201 started"
			testPassed="no"
else
	echo "Ride to customer 201 denied"
fi

#Step 3 : Checks wallet balance for the Customer 201.
balance=$(curl -s "http://localhost:8082/getBalance?custId=201")
if [ "$balance" != "10000" ];
then
	echo "Wrong balance amount for the customer 201"
			testPassed="no"
else
	echo "Correct Balance amount for the customer 201"
fi

#Step 4 : customer 201 requests a ride from 1000 to 0.
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=100&destinationLoc=0")
if [ "$rideId" != "-1" ];
then
	echo "Ride by customer 201 started"
			testPassed="no"
else
	echo "Ride to customer 201 denied"
fi

echo "Test Passing Status: " $testPassed