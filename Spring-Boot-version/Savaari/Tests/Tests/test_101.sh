#! /bin/sh
# This test case checks the functionality of interest fucntion and request ride mainly.
# cab 101 signs in
# cust 201 requests ride (should be accepted)
# ride completes
# cust 202 requests ride (should be rejected)
# cust 201 requests ride (should be accpeted)
# ride completed
# cab 101 signs out
# cab 101 signs in
# cust 201 requests ride (should be accepted)
# ride completes


# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_101 ====${NC}"

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
# cust 202 requests ride (should be rejected)
rideId=$(curl -s "http://localhost:8081/requestRide?custId=202&sourceLoc=10&destinationLoc=20")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 202 alloted a ride number " $rideId
	testPassed="no"
else
	echo "Customer 202 is not alloted a ride"
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

# cab 101 signs out
resp=$(curl -s "http://localhost:8080/signOut?cabId=101")
if [  "$resp" = "true" ];
then
	echo "Cab 101 signed out"
else
	echo "Cab 101 could not sign out"
	testPassed="no"
fi


#========================================================================================================================================
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
