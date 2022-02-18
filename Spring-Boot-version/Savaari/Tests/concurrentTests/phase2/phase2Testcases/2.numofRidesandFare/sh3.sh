rm -f sh3out
#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=100")
if [ "$resp" = "true" ];
then
    echo "Cab 101 signed in"
else
    echo "Cab 101 could not sign in"
fi

# Step 2: customer 203 requests a cab
rideDetails=$(curl -s "http://localhost:8081/requestRide?custId=203&sourceLoc=190&destinationLoc=200")
rideId=$(echo $rideDetails | cut -d' ' -f 1)
cabId=$(echo $rideDetails | cut -d' ' -f 2)
fare=$(echo $rideDetails | cut -d' ' -f 3)

if [ "$rideId" != "-1" ];
then
    echo "Ride by customer 203 started"
    
else
    fare=0
    echo "Ride to customer 203 denied"
fi

echo $fare >> sh3out