#! /bin/sh
rm -f sh1out

# Color
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color


noOfIteration=70
onGoingRideId=-1
noOfRide=0

#Step 1 : cab 101 signs in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=100")
if [ "$resp" = "true" ];
then
    echo -e "${GREEN}1: Cab 101 signed in${NC}"
else
    echo "1: Cab 101 could not sign in"
fi

# Step 2: customer 201 requests a cab
for ((i=1;i<=noOfIteration;i++));
do
    #End previous ride
    if [ "$onGoingRideId" != "-1" ] && [ $i != "1" ];
	then
		resp=$(curl -s "http://localhost:8080/rideEnded?cabId=101&rideId=$onGoingRideId")
		if [ "$resp" = "true" ];
		then
		  echo -e "${GREEN}1: " $onGoingRideId " has ended" at $i"th iteration ${NC}"
		  onGoingRideId=-1
		else
		  echo -e "${RED}1: Could not end${NC}" $onGoingRideId
		  testPassed="no"
        fi
	fi

	rideDetails=$(curl -s "http://localhost:8081/requestRide?custId=201&sourceLoc=100&destinationLoc=105")
	rideId=$(echo $rideDetails | cut -d' ' -f 1)

	if [ "$rideId" != "-1" ];
	then
	    onGoingRideId=$rideId
		((noOfRide=noOfRide+1))
		echo -e "${GREEN}sh1: Request $i >>   Ride by customer 201 started${NC}"
		
	else
		echo "sh1: Request $i >>   Ride to customer 201 denied"
	fi
	
done

echo $noOfRide >> sh1out

