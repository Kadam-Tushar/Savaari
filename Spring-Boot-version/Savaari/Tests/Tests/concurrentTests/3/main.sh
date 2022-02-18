#! /bin/sh
# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

balanceBefore=$(curl -s "http://localhost:8082/getBalance?custId=201")

sleep 5

echo "Balance Before:" $balanceBefore

bash wa1.sh & bash wa2.sh & bash wa3.sh & bash wa4.sh

wait

balanceAfter=$(curl -s "http://localhost:8082/getBalance?custId=201")

echo "Balance After:" $balanceAfter

