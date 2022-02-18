# Run Tests

cd ../Tests
for f in ./*; do
  sh "$f" || break
done
echo ""
echo ""
echo "-----------------------------------------------------------------Running concurrentTests tests now-----------------------------------------------------------------"
echo ""
echo ""
cd concurrentTests

for f in ./*; do
	cd $f ;
	./main.sh;
	cd .. ;
done

cd ../..
