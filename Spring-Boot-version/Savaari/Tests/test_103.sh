#! /bin/sh
# This test case checks the functionality of signin and sign out


# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_103 ====${NC}"

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

# cab 102 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=102&initialPos=10")
if [ "$resp" = "true" ];
then
	echo "Cab 102 signed in"
else
	echo "Cab 102 could not sign in"
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

# cab 102 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=102&initialPos=10")
if [ "$resp" = "true" ];
then
	echo "Cab 102 signed in"
	testPassed="no"
else
	echo "Cab 102 could not sign in"
fi


# cab 101 signs out
resp=$(curl -s "http://localhost:8080/signOut?cabId=101")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed out"
	testPassed="no"
else
	echo "Cab 101 could not sign out"
fi
#========================================================================================================================================


if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi
