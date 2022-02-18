rm -f sh1out

#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=100")
if [ "$resp" = "true" ];
then
    echo "Sh1: Cab 101 signed in"
else
    echo "Sh1: Cab 101 could not sign in"
fi
	
# Step 2: customer 201 requests a cab
rideDetails=$(curl -s \
"http://localhost:8081/requestRide?custId=201&sourceLoc=110&destinationLoc=200")
echo "Sh1 Ride Details:" $rideDetails
rideId=$(echo $rideDetails | cut -d' ' -f 1)
cabId=$(echo $rideDetails | cut -d' ' -f 2)
fare=$(echo $rideDetails | cut -d' ' -f 3)
#echo "Sh1: Fare" $fare
if [ "$rideId" != "-1" ];
then
    echo "Sh1: Ride by customer 201 started"
    echo $fare >> sh1out
else
    echo "Sh1: Ride to customer 201 denied"
    echo "0" >> sh1out
fi
