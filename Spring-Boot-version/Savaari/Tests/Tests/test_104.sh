#! /bin/sh
# This test case checks the functionality of alternate test, balance check and wallet


# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_104 ====${NC}"

# reset RideService and Wallet.
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

testPassed="yes"

# cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=0")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi

#========================================================================================================================================

# cust 201 requests ride (should be accepted)
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=20")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $rideId
else
	echo "Customer 201 is not alloted a ride"
	testPassed="no"
fi


# ride completes
resp=$(curl -s "http://localhost:8080/rideEnded?cabId=101&rideId=$rideId")
if [ "$resp" = "true" ];
then
	echo "ride " $rideId "completed"
else
	echo "Could not complete ride"
	testPassed="no"
fi

#========================================================================================================================================
# cust 201 requests ride (should be rejected)
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=20")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $rideId
	testPassed="no"
else
	echo "Customer 201 is not alloted a ride"
fi
#========================================================================================================================================
resp=$(curl -s "http://localhost:8082/deductAmount?custId=201&amount=9800")
if [ "$resp" = "true" ];
then
	echo "All amount deducted"
else
	echo "Could not deduct amount"
	testPassed="no"
fi

#========================================================================================================================================
# cust 201 requests ride (should be cancelled)
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=20")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $rideId
	testPassed="no"
else
	echo "Customer 201 is not alloted a ride"
fi

#========================================================================================================================================

resp=$(curl -s "http://localhost:8082/addAmount?custId=201&amount=9800")
if [ "$resp" = "true" ];
then
	echo "All amount added"
else
	echo "Could not add amount"
	testPassed="no"
fi

#========================================================================================================================================
# cust 201 requests ride (should be rejected)
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=20")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $rideId
	testPassed="no"
else
	echo "Customer 201 is not alloted a ride"
fi
#========================================================================================================================================
# cust 201 requests ride (should be accepted)
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=20")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $rideId
else
	echo "Customer 201 is not alloted a ride"
	testPassed="no"
fi


# ride completes
resp=$(curl -s "http://localhost:8080/rideEnded?cabId=101&rideId=$rideId")
if [ ! "$resp" = "false" ];
then
	echo "ride " $rideId "completed"
else
	echo "Could not complete ride"
	testPassed="no"
fi


#========================================================================================================================================

if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi
