#! /bin/bash
cd TestCases2
for f in *.sh; do
  echo "---$f-------"

  sh "$f" || break
  echo "------------------------------"
done

cd ../phase2Testcases/1.Totalfare

 bash testMain.sh
 
 echo "---------1----------------"
 
 cd ../2.numofRidesandFare

 bash testMain.sh
 
 echo "---------2----------------"
 
 cd ../3.looping1-EndRideAfterAFewIteration

 bash testMain.sh
 
 echo "---------3----------------"
 
 cd ../4.looping1-EndRideAfterEveryIteration

 bash testMain.sh
 
 echo "---------4----------------"