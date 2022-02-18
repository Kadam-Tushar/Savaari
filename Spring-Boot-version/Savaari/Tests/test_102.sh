#! /bin/sh
# this test case checks whether wallet end points are working properly
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_102 ====${NC}"

testPassed="yes"

resp=$(curl -s "http://localhost:8082/getBalance?custId=201")
if [ "$resp" = "10000" ];
then
    echo "Got Balance 10000 of cust:201"
else
    echo "Got wrong balance for cust=201"
    testPassed="no"
fi


resp=$(curl -s "http://localhost:8082/deductAmount?custId=202&amount=3000")
resp=$(curl -s "http://localhost:8082/getBalance?custId=202")
if [ "$resp" = "7000" ];
then
    echo "Got Balance 7000 of cust:202"
else
    echo "Got wrong balance for cust=202"
    testPassed="no"
fi


resp=$(curl -s "http://localhost:8082/addAmount?custId=203&amount=4000")
resp=$(curl -s "http://localhost:8082/getBalance?custId=203")
if [ "$resp" = "14000" ];
then
    echo "Got Balance 14000 of cust:203"
else
    echo "Got wrong balance for cust=203"
    testPassed="no"
fi
 
 
curl -s http://localhost:8082/reset

resp=$(curl -s "http://localhost:8082/getBalance?custId=201")
if [ "$resp" = "10000" ];
then
    echo "Got Balance 10000 of cust:201"
else
    echo "Got wrong balance for cust=201"
    testPassed="no"
fi


resp=$(curl -s "http://localhost:8082/getBalance?custId=202")
if [ "$resp" = "10000" ];
then
    echo "Got Balance 10000 of cust:202"
else
    echo "Got wrong balance for cust=202"
    testPassed="no"
fi


resp=$(curl -s "http://localhost:8082/getBalance?custId=203")
if [ "$resp" = "10000" ];
then
    echo "Got Balance 10000 of cust:203"
else
    echo "Got wrong balance for cust=203"
    testPassed="no"
fi


if [ "$testPassed" = "yes" ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi
