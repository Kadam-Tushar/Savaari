#! /bin/sh

# This test checks whether the getBalance works correctly or not.

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_09 ====${NC}"

testPassed="yes"

# reset RideService and Wallet.
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset


# Step 0: cab 101 signs in at location 100.
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=100")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi


# Step 1: Get the balance of the customer 201.

balance_old=$(curl -s "http://localhost:8082/getBalance?custId=201")
echo "The balance before taking the ride:" $balance_old

# Step 2: Take a ride from location 100 (cab's location) to 200.

rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=100&destinationLoc=200")
if [ "$rideId" != "-1" ];
then
    echo "Ride by customer 201 started"
else
    echo "Ride to customer 201 denied"
    testPassed="no"
fi

#Step 3: Check if the balance of customer 201 has been deducted appropriately.
balance_new=$(curl -s "http://localhost:8082/getBalance?custId=201")
echo "The balance after taking the ride:" $balance_new

## Incomplete - have to check old and new balance.



