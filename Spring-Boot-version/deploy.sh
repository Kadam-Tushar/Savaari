#! /bin/sh
 

cd Cab
./mvnw -DskipTests clean package
docker build -t cab_service .
docker run --rm --name cab-service -d -p 172.17.0.1:8080:8080 -p 127.0.0.1:8080:8080 -v /path-to-input-file/Input cab_service

cd ../RideService
./mvnw -DskipTests clean package
docker build -t ride_service .
docker run --rm --name ride-service -d -p 172.17.0.1:8081:8081 -p 127.0.0.1:8081:8081 -v /path-to-input-file/Input ride_service

cd ../wallet
./mvnw -DskipTests clean package
docker build -t wallet_service .
docker run --rm --name wallet-service -d -p 172.17.0.1:8082:8082 -p 127.0.0.1:8082:8082 -v /path-to-input-file/Input wallet_service

# sleep before running tests
var=""
echo "Press any key to continue"
read var

# Run Tests
cd ../Tests
for f in ./*; do
  sh "$f" || break
done



#cab-service ride-service wallet-service

