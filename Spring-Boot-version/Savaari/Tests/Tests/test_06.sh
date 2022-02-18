#! /bin/sh
# This test case checks the functionality of numRides.

# We call numRides to a signed-out cab. It should return 0, as per the requirements mentioned. 
# Inadvertently, also checks if sign-in and sign-out work properly.

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_06 ====${NC}"

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


# cab 101 signs out
resp=$(curl -s "http://localhost:8080/signOut?cabId=101")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed out"
else
	echo "Cab 101 could not sign out"
	testPassed="no"
fi

resp=$(curl -s "http://localhost:8080/numRides?cabId=101")
if [ "$resp" = "0" ];
then
	testPassed="yes"
else
	testPassed="no"
fi


if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi


