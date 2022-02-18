#! /bin/sh
for i in {0..10};
do
	#echo "Shell 2:" $i
	resp=$(curl -s "http://localhost:8082/deductAmount?custId=201&amount=100")
done
