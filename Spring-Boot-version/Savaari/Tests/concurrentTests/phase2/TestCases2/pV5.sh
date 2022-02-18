#! /bin/sh
# this test case checks Invalid signin and SignOut

# every test case should begin with these two steps
curl -s http://localhost:8081/reset
curl -s http://localhost:8081/reset
curl -s http://localhost:8082/reset

testPassed="yes"

#Step 1 :  sign in of Invalid Cab
resp=$(curl -s "http://localhost:8080/signIn?cabId=9101&initialPos=900")
if [ "$resp" = "true" ];
then
	echo "Cab 9101 signed in"
	testPassed="no"
else
	echo "Cab 9101 could not sign in"
fi

#Step 2 :  sign Out of Cab which is already Signed Out
resp=$(curl -s "http://localhost:8080/signOut?cabId=101")
if [ "$resp" = "true" ];
then
	echo "Cab 101 is signed Out"
	testPassed="no"
else
	echo "Cab 101 could not sign Out"
fi


#Step 3 :  sign in 101
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=900")
if [ "$resp" = "true" ];
then
	echo "Cab 101 signed in"
else
	echo "Cab C101 could not sign in"
		testPassed="no"
fi


#Step 4 :  sign in of Cab which is already Signed in
resp=$(curl -s "http://localhost:8080/signIn?cabId=101&initialPos=100")
if [ "$resp" = "true" ];
then
	echo "Cab 101 is signed In"
	testPassed="no"
else
	echo "Cab 101 could not sign In"
fi

echo "Test Passing Status: " $testPassed