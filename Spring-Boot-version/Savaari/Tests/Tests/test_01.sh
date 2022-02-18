#! /bin/sh
# This test case checks the functionality of requestRide

# first 4 cabs will sign in with location 0, 10, 20, 30.
# Then customer makes a request for a ride with a source location 14 
# which should be satisfied by cab at location 10.
# then second customer makes a request for a ride with a source location 26.
# then third customer makes a request for a ride with a source location 14
# which should be satisfied by 

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_02 ====${NC}"

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

# cab 103 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=103&initialPos=20")
if [ "$resp" = "true" ];
then
	echo "Cab 103 signed in"
else
	echo "Cab 103 could not sign in"
	testPassed="no"
fi

# cab 104 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=104&initialPos=30")
if [ "$resp" = "true" ];
then
	echo "Cab 104 signed in"
else
	echo "Cab 104 could not sign in"
	testPassed="no"
fi

# customer 201 sends a request with source location 14
resp=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=14&destinationLoc=34")
if [ ! "$resp" = "-1" ];
then
	echo "Customer 201 alloted a ride number " $resp
else
	echo "Customer 201 is not alloted a ride"
	testPassed="no"
fi

# customer 202 sends a request with source location 26
resp=$(curl -s "http://localhost:8081/requestRide?custId=202&sourceLoc=26&destinationLoc=34")
if [ ! "$resp" = "-1" ];
then
	echo "Customer 202 alloted a ride number " $resp
else
	echo "Customer 202 is not alloted a ride"
	testPassed="no"
fi

# customer 203 sends a request with source location 14
resp=$(curl -s "http://localhost:8081/requestRide?custId=203&sourceLoc=14&destinationLoc=34")
if [ ! "$resp" = "-1" ];
then
	echo "Customer 203 alloted a ride number " $resp
else
	echo "Customer 203 is not alloted a ride"
	testPassed="no"
fi

# cab 102 must be alloted to customer 201
resp=$(curl -s "http://localhost:8081/getCabStatus?cabId=102")
if [ "$resp" = "giving-ride 14 201 34" ];
then
	echo "Customer 201 got a correct cab"
else
	echo "Customer 201 got an incorrect cab"
	testPassed="no"
fi

# cab 104 must be alloted to customer 202
resp=$(curl -s "http://localhost:8081/getCabStatus?cabId=104")
if [ "$resp" = "giving-ride 26 202 34" ];
then
	echo "Customer 202 got a correct cab"
else
	echo "Customer 202 got an incorrect cab"
	testPassed="no"
fi

# cab 103 must be alloted to customer 203
resp=$(curl -s "http://localhost:8081/getCabStatus?cabId=103")
if [ "$resp" = "giving-ride 14 203 34" ];
then
	echo "Customer 203 got a correct cab"
else
	echo "Customer 203 got an incorrect cab"
	testPassed="no"
fi

if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi
