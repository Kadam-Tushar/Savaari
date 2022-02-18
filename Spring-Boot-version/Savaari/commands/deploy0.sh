#! /bin/sh
 
input_path="/home/resonater/iisc/Sem2/209/Project/Input"

cd h2
docker build -t savaari/h2db .
docker run --rm --name h2db -d -p 9092:9092 savaari/h2db

cd ../Cab
./mvnw -DskipTests clean package
docker build -t savaari/cab_service .
docker run --rm --name cab-service -d -p 172.17.0.1:8080:8080 -p 127.0.0.1:8080:8080 savaari/cab_service

cd ../RideService
./mvnw -DskipTests clean package
docker build -t savaari/ride_service .
# docker run --rm --name savaari/ride-service -d -p 172.17.0.1:8081:8081 -p 127.0.0.1:8081:8081 ride_service
docker run --rm --name ride-service -d -p 172.17.0.1:8081:8081 --net=host savaari/ride_service

cd ../wallet
./mvnw -DskipTests clean package
docker build -t savaari/wallet_service .
docker run --rm --name wallet-service -d -p 172.17.0.1:8082:8082 -p 127.0.0.1:8082:8082 savaari/wallet_service

# sleep before running tests
var=""
echo "Press any key to continue"
read var

# Run Tests
cd ../Tests
for f in ./*; do
  sh "$f" || break
done

cd concurrentTests

for f in ./*; do
	cd $f ;
	./main.sh;
	cd .. ;
done

cd ../..
#cab-service ride-service wallet-service


#================================================================================================================================================================
#creating images
cd h2
docker build -t savaari/h2db .
cd ../Cab
docker build -t savaari/cab_service .
cd ../RideService
docker build -t savaari/ride_service .
cd ../wallet
docker build -t savaari/wallet_service .

#================================================================================================================================================================
#deleting the deployments and services
minikube kubectl delete service cab-service
minikube kubectl delete service ride-service
minikube kubectl delete service h2-service
minikube kubectl delete service wallet-service

minikube kubectl delete service cab-service-e
minikube kubectl delete service ride-service-e
minikube kubectl delete service wallet-service-e
minikube kubectl delete service h2-service-e

minikube kubectl delete deployment wallet-service
minikube kubectl delete deployment ride-service
minikube kubectl delete deployment cab-service
minikube kubectl delete deployment h2-service

#================================================================================================================================================================
#creating the deployments and services

minikube kubectl -- create deployment h2-service --image=savaari/h2db
minikube kubectl -- create deployment cab-service --image=savaari/cab_service
minikube kubectl -- create deployment ride-service --image=savaari/ride_service
minikube kubectl -- create deployment wallet-service --image=savaari/wallet_service

minikube kubectl -- expose deployment h2-service --type=ClusterIP --port=9092 --name=h2-service
minikube kubectl -- expose deployment cab-service --type=ClusterIP --port=8080 --name=cab-service
minikube kubectl -- expose deployment ride-service --type=ClusterIP --port=8081 --name=ride-service
minikube kubectl -- expose deployment wallet-service --type=ClusterIP --port=8082 --name=wallet-service

minikube kubectl -- expose deployment cab-service --type=LoadBalancer --port=8080 --name=cab-service-e
minikube kubectl -- expose deployment ride-service --type=LoadBalancer --port=8081 --name=ride-service-e
minikube kubectl -- expose deployment wallet-service --type=LoadBalancer --port=8082 --name=wallet-service-e
minikube kubectl -- expose deployment h2-service --type=LoadBalancer --port=8082 --name=h2-service-e

cd h2
kubectl apply -f spec.yaml
cd ../Cab
kubectl apply -f spec.yaml
cd ../RideService
kubectl apply -f spec.yaml
cd ../wallet
kubectl apply -f spec.yaml	
#================================================================================================================================================================
#deleting the images
docker image rm savaari/wallet_service:latest savaari/ride_service:latest savaari/cab_service:latest savaari/h2db:latest

#================================================================================================================================================================
#Scaling
kubectl scale deployment ride-service --replicas=4

#autoscale
kubectl autoscale deployment hello-java --max=4 --min=1
#================================================================================================================================================================
#Port forwarding

ssh -L 8080:$IP1:8080 lelouch
ssh -L 8081:$IP2:8081 lelouch
ssh -L 8082:$IP3:8082 lelouch

#================================================================================================================================================================
