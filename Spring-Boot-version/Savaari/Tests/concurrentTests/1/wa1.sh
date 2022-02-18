for i in {1..10};
do
	resp=$(curl -s "http://localhost:8082/addAmount?custId=201&amount=99")
done
