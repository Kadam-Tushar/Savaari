#! /bin/sh
#adding and subtracting balance to a wallet multiple times 


curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "${GREEN}==== Test test_1001_wallet-concurrency ====${NC}"

testPassed="yes"

Before=$(curl -s "http://localhost:8082/getBalance?custId=201")

sleep 2

sh wa1.sh & sh wa2.sh

wait	

After=$(curl -s "http://localhost:8082/getBalance?custId=201")


if [ $After = $Before ];
then
	echo "${YELLOW}Test Passing Status: ${GREEN}$testPassed${NC}"
else
	testPassed="no"
	echo "${YELLOW}Test Passing Status: ${RED}$testPassed${NC}"
fi

