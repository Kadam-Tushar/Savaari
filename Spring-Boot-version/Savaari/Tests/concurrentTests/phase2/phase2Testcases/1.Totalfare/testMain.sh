#! /bin/bash
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

# Run two test scripts in parallel
sh sh1.sh &
sh sh2.sh & wait
# sh1 creates the output file sh1out, which contains fares
# of all rides given in sh1. Similarly, sh2out.


totalFare=0
totalBalance=0
testPassed="YES"
((initBalance=10000*2))
for i in $(cat sh1out sh2out);
do
  totalFare=$(expr $totalFare + $i)
done

#Checks wallet balance for the Customers 201 & 202.
balance=$(curl -s "http://localhost:8082/getBalance?custId=201")
((totalBalance=totalBalance+balance))

balance=$(curl -s "http://localhost:8082/getBalance?custId=202")
((totalBalance=totalBalance+balance))

((expBalance=initBalance-totalFare))

if [ "$totalBalance" != "$expBalance" ];
then
	echo "Inconsistent balance amounts!"
			testPassed="NO"
else
		echo "Possibly valid balance amounts!"
fi


echo "Test Passing Status: " $testPassed
echo $totalFare $totalBalance $initBalance

