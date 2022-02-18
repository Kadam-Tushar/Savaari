#! /bin/sh
rideId="-1"
cnt=10
while [ "$rideId" = "-1" ];
do
    rideId=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=50&destinationLoc=100")
    echo "[sh1] rideId: $rideId"
    cnt=`expr $cnt - 1`
    if [ "$cnt" -le "0" ];
    then
        break;
    fi
done

echo "Customer 201 got the ride!"
