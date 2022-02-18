#! /bin/sh

curl -s "http://localhost:8081/reset";
curl -s "http://localhost:8082/reset";

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_1004_====${NC}"

testPassed="true";

# cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=0")
if [ "$resp" = "-1" ];
then
    testPassed=false;
    echo "Cab 101 could not sign in."
else
    echo "Cab 101 signed in."
fi

#cab 102 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=102&initialPos=100")
if [ "$resp" = "-1" ];
then
    testPassed=false;
    echo "Cab 102 could not sign in."
else
    echo "Cab 102 signed in."
fi

sh sh1.sh & sh sh2.sh;

wait;

cab101Details=$(curl -s "http://localhost:8081/getCabStatus?cabId=101")
cab102Details=$(curl -s "http://localhost:8081/getCabStatus?cabId=102")

cab101State=$(echo $cab101Details | cut -d' ' -f 1)
cab102State=$(echo $cab102Details | cut -d' ' -f 1)

if [ "$cab101State" != "giving-ride" ]
then
    echo "Cab 101 is in invalid state: $cab101State"
    testPassed=false;
fi

if [ "$cab102State" != "giving-ride" ]
then
    echo "Cab 201 is in invalid state: $cab101State"
    testPassed=false;
fi

cab101CustId=$(echo $cab101Details | cut -d' ' -f 3)
cab102CustId=$(echo $cab102Details | cut -d' ' -f 3)

cust201GotRide="false"
cust202GotRide="false"

if [ "$cab101CustId" = "201" ]
then
    cust201GotRide="true"
elif [ "$cab101CustId" = "202" ]
then
    cust202GotRide="true"
fi

if [ "$cab102CustId" = "201" ]
then
    cust201GotRide="true"
elif [ "$cab102CustId" = "202" ]
then
    cust202GotRide="true"
fi

if [ "$cust201GotRide" != "true" ]
then
    echo "Customer 201 did not get any ride!"
    testPassed="false"
fi

if [ "$cust202GotRide" != "true" ]
then
    echo "Customer 202 did not get any ride!"
    testPassed="false"
fi

if [ "$testPassed" != "true" ]
then
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
fi
