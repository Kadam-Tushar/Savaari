#! /bin/sh
rm -f sh1.txt




#Step 2 : cab signsout
resp=$(curl -s "http://localhost:8080/signOut?cabId=101")
echo $resp >> sh1.txt
if [ "$resp" = "true" ];
then
	echo "Sh1: Cab 101 signed out"
else
	echo "Sh1: Cab 101 could not sign out"
fi
