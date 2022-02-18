
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

# Run three test scripts in parallel
sh sh1.sh & sh sh2.sh & sh sh3.sh & wait

# sh1 creates the output file sh1out, which contains fares
# of all rides given in sh1. Similarly, sh2out and sh3out.


testPassed="YES"
initBalance=10000
cid=201

#Checks balance amount for each customer
for fare in $(cat sh1out sh2out sh3out);
do
  balance=$(curl -s "http://localhost:8082/getBalance?custId=$cid")
  ((expBalance = initBalance - fare))

  if [ "$balance" != "$expBalance" ];
  then
	echo "Invalid balance amount for the customer $cid!"
			testPassed="NO"
  else
		echo "Valid balance amount for the customer $cid!"
  fi
  ((cid=cid+1))

done

numRides=$(curl -s "http://localhost:8080/numRides?cabId=101")
echo $numRides

if [ "$numRides" != "1" ];
then
	echo "Invalid Number of rides"
	testPassed="NO"
else
	echo "Correct number of ride"
fi


echo "Test Passing Status: " $testPassed

