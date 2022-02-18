#! /bin/sh
# This test case checks the functionality of Cab.

# This test checks whether a Cab can sign-out, before it is signed-in.

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_05 ====${NC}"

testPassed="yes"

# reset RideService and Wallet.
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

# cab 101 signs out
resp=$(curl -s "http://localhost:8080/signOut?cabId=101")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed out"
	testPassed="no"
else
	echo "Cab 101 could not sign out"
fi

if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi