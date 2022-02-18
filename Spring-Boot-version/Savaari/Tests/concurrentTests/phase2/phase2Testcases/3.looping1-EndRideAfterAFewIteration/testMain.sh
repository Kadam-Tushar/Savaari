#! /bin/bash
# Color
YELLOW='\033[0;33m'


# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

# Run two test scripts in parallel
bash sh1.sh &
bash sh2.sh & wait

# sh1 creates the output file sh1out, which contains fares
# of all rides given in sh1. Similarly, sh2out.


testPassed="YES"
totalNoOfRides=0

for num in $(cat sh1out sh2out);
do
((totalNoOfRides=totalNoOfRides+num))
done


numRides=$(curl -s "http://localhost:8080/numRides?cabId=101")
echo $numRides

if [ "$numRides" != "$totalNoOfRides" ];
then
	echo "Invalid Number of rides"
	testPassed="no"
else
	echo "Correct number of rides"
fi


echo -e "${YELLOW}Test Passing Status: " $testPassed

