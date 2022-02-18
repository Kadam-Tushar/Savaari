#! /bin/sh
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

#totalBalanceBefore=$(curl -s "http://localhost:8082/totalCurrentBalance")

# Run two test scripts in parallel
#sh sh1.sh > /dev/null & sh sh2.sh > /dev/null
sh sh1.sh &
sh sh2.sh
# sh1 creates the output file sh1out, which contains fares
# of all rides given in sh1. Similarly, sh2out.

wait

totalFare=0
for i in $(cat sh1out sh2out);
do
  totalFare=$(expr $totalFare + $i)
done
# totalFare contains the sum cost of all rides

# Now check if the current total balance
# in all wallets is equal to 
# original total balance in all wallets (which is a constant)
# MINUS totalFare.
# Print “Test Passing Status: yes” if yes,
# else print “Test Passing Status: no”.
# You need to fill the code for the check above

#totalBalanceAfter=$(curl -s "http://localhost:8082/totalCurrentBalance")

#result=$(expr $totalBalanceAfter + $totalFare - $totalBalanceBefore)

resp1=$(curl -s "http://localhost:8082/getBalance?custId=201")
resp2=$(curl -s "http://localhost:8082/getBalance?custId=202")
resp3=$(curl -s "http://localhost:8082/getBalance?custId=203")
echo "After balace:" $(expr $resp1 + $resp2 + $resp3)
echo "Total fare:" $totalFare
#echo "Total Balance Before:" $totalBalanceBefore

#echo "Total Fare:" $totalFare

#echo "Total Balance Afer:" $totalBalanceAfter
