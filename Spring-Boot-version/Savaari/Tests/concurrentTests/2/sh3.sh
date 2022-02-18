#! /bin/sh
rm -f sh3.txt


#Step 2 : customer 201 requestsride
rideDetails=$(curl -s \
"http://localhost:8081/requestRide?custId=203&sourceLoc=110&destinationLoc=200")
#echo "Sh1 Ride Details:" $rideDetails
rideId=$(echo $rideDetails | cut -d' ' -f 1)
#cabId=$(echo $rideDetails | cut -d' ' -f 2)
#fare=$(echo $rideDetails | cut -d' ' -f 3)
#echo "Sh1: Fare" $fare
echo $rideId >> sh3.txt
if [ "$rideId" != "-1" ];
then
    echo "Sh3: Ride by customer 203 started"
else
    echo "Sh3: Ride to customer 203 denied"
fi
