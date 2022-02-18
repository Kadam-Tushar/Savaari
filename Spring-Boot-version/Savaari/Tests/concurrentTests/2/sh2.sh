#! /bin/sh
rm -f sh2.txt


#Step 2 : customer 201 requestsride
rideDetails=$(curl -s \
"http://localhost:8081/requestRide?custId=202&sourceLoc=110&destinationLoc=200")
#echo "Sh1 Ride Details:" $rideDetails
rideId=$(echo $rideDetails | cut -d' ' -f 1)
#cabId=$(echo $rideDetails | cut -d' ' -f 2)
#fare=$(echo $rideDetails | cut -d' ' -f 3)
#echo "Sh1: Fare" $fare
echo $rideId >> sh2.txt
if [ "$rideId" != "-1" ];
then
    echo "Sh2: Ride by customer 202 started"
else
    echo "Sh2: Ride to customer 202 denied"
fi
