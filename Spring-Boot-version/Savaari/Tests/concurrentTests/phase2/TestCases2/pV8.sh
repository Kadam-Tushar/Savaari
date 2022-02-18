#! /bin/sh
# this test case checks whether a customer's request
#   for long ride get processed after  sufficient amount
#   is added to Wallet
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

testPassed="yes"

#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=0")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab 101 could not sign in"
	testPassed="no"
fi

#Step 2 : Customer 201 deducts amount from his/her wallet
resp=$(curl -s "http://localhost:8082/deductAmount?custId=201&amount=-2000")
if [ "$resp" = "true" ];
then
	echo "Amount is deducted from Customer's wallet"
		testPassed="no"
else
	echo "Amount cannot be deducted from Customer's wallet"
fi


#Step 3 : customer 201 requests a ride
rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=10&destinationLoc=1100")
if [ "$rideId" != "-1" ];
then
	echo "Ride by customer 201 started"
			testPassed="no"

else
	echo "Ride to customer 201 denied"
fi



#Step 4 : Get balance amount in Customer 201's wallet
resp=$(curl -s "http://localhost:8082/getBalance?custId=201")
echo $resp
if [ "$resp" != "10000" ];
then
	echo "Invalid Balance Amount"
    testPassed="no"

else
	echo "Correct balance amount"
	
fi

echo "Test Passing Status: " $testPassed