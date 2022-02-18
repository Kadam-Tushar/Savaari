#! /bin/sh

# This test checks whether a cab in signed-in state can sign-in again or not.

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_08 ====${NC}"

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

# cab 101 tries to sign in again.
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=10")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
	testPassed="no"
else
	echo "Cab 101 could not sign in again" 
fi


if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi