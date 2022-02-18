#! /bin/sh

curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_1003_====${NC}"

testPassed="yes"
#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=100")
if [ "$resp" = "true" ];
then
    echo "Cab 101 signed in"
else
    echo "Cab 101 could not sign in"
fi

sh sh1.sh &
sh sh2.sh

wait 

sh1txt=$(cat sh1.txt)
sh2txt=$(cat sh2.txt)
if [ "$sh1txt" = "true" ];
then
	if [ "$sh2txt" = "true" ];
	then
		testPassed="no"
		echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
	else
		echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
	fi
else
	if [ "$sh2txt" = "true" ];
	then
		echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
	else
		testPassed="no"
		echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
	fi
fi

