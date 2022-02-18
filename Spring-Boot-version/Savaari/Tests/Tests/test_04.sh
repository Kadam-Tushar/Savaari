#! /bin/sh
# This test case checks the functionality of requestRide

# This test checks whether an invalid customerId can book a cab or not.

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_04 ====${NC}"

testPassed="yes"

# reset RideService and Wallet.
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset


# cab 101 signs in at location 10.
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=10")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi

# customer -1000 sends a request. Since this is non-negative and will not be present, it should fail.
resp=$(curl -s "http://localhost:8081/requestRide?custId=-1000&sourceLoc=10&destinationLoc=14")
if [ "$resp" = "-1" ];
then
	echo "Customer -1000 is not alloted a ride"
else
	echo "Customer -1000 alloted a ride number " $resp
	testPassed="no"
fi

if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi