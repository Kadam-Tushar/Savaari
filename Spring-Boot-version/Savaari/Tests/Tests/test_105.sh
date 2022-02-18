#! /bin/sh
# This test case checks the functionality of getting 2 rides for 1 custormer


# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_105 ====${NC}"

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
	echo "${RED}Cab 101 could not sign in${NC}"
	testPassed="no"
fi

#========================================================================================================================================

# cab 102 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=102&initialPos=0")
if [ "$resp" = "true" ];
then
	echo "Cab 102 signed in"
else
	echo "${RED}Cab 102 could not sign in${NC}"
	testPassed="no"
fi

#========================================================================================================================================

# customer 201 sends a request for ride
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=14&destinationLoc=10")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $rideId
else
	echo "${RED}Customer 201 is not alloted a ride${NC}"
	testPassed="no"
fi

#========================================================================================================================================

# customer 201 again sends a request for ride. 
# This request must be accepted.
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=14")
if [ ! "$rideId" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $rideId
else
	echo "${RED}Customer 201 is not alloted a ride${NC}"
	testPassed="no"
fi
#========================================================================================================================================


if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi
