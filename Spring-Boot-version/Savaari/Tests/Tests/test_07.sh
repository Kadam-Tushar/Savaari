#! /bin/sh
# This test case checks if an invalid cabId can sign in or not.

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_07 ====${NC}"

testPassed="yes"

# reset RideService and Wallet.
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset


# cab 101 signs in at location 10.
resp=$(curl -s "http://localhost:8080/signIn?cabId=-9999&initialPos=10")
if [ "$resp" = "true" ];
then
	echo "Cab -9999 signed in"
	testPaseed="no"
else
	echo "Cab -9999 could not sign in"
fi


if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi