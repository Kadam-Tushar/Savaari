#! /bin/sh
# This test case checks the functionality of requestRide

# This test asserts that the cab driver must be interested in every 
# alternative ride request

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_10 ====${NC}"

testPassed="yes"

# reset RideService and Wallet.
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset


# cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=0")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "${RED}Cab 101 could not sign in${NC}"
	testPassed="no"
fi

# customer 201 sends a request for ride
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=14&destinationLoc=10")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $rideId
else
	echo "${RED}Customer 201 is not alloted a ride${NC}"
	testPassed="no"
fi

# previous ride gets completed
resp=$(curl -s "http://localhost:8080/rideEnded?cabId=101&rideId=$rideId")
if [ "$resp" = "true" ];
then
    echo "$rideId has ended"
else
    echo "${RED}Could not end $rideId${NC}"
    testPassed="no"
fi

# customer 201 again sends a request for ride
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=14")
if [ "$rideId" = "-1" ];
then
	echo "Customer 201 is not alloted a ride" 
else
	echo "${RED}Customer 201 alloted a ride${NC}"
	testPassed="no"
fi

# customer 201 yet again sends a request for ride. 
# This time it should be allotted a cab 101
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=14")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $rideId
else
	echo "${RED}Customer 201 is not alloted a ride${NC}"
	testPassed="no"
fi

if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi
