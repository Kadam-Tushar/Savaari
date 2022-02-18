rm -f sh2out

#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=100")
if [ "$resp" = "true" ];
then
    echo "Sh2: Cab 101 signed in"
else
    echo "Sh2: Cab 101 could not sign in"
fi

# Step 2: customer 202 requests a cab
rideDetails=$(curl -s \
"http://localhost:8081/requestRide?custId=202&sourceLoc=90&destinationLoc=200")
echo "Sh2 Ride Details:" $rideDetails 
rideId=$(echo $rideDetails | cut -d' ' -f 1)
cabId=$(echo $rideDetails | cut -d' ' -f 2)
fare=$(echo $rideDetails | cut -d' ' -f 3)
echo "Sh2: Fare" $fare
if [ "$rideId" != "-1" ];
then
    echo "Sh2: Ride by customer 202 started"
    echo $fare >> sh2out
else
    echo "Sh2: Ride to customer 202 denied"
    echo "0" >> sh2out
fi
