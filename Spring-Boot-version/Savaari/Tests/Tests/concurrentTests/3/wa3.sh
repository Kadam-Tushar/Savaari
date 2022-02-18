#! /bin/sh
for i in {0..10};
do
	#echo "Shell 1:" $i
	resp=$(curl -s "http://localhost:8082/addAmount?custId=201&amount=100")
done
