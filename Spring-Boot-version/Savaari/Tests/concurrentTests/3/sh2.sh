#! /bin/sh
rm -f sh2.txt

#Step 2 : cab signsout
resp=$(curl -s "http://localhost:8080/signOut?cabId=101")
echo $resp >> sh2.txt
if [ "$resp" = "true" ];
then
	echo "Sh2: Cab 101 signed out"
else
	echo "Sh2: Cab 101 could not sign out"
fi


