#! /bin/sh

#force the docker commands that you subsequently within the same terminal to treat the minikube container as the docker daemon
eval $(minikube docker-env)
#================================================================================================================================================================
#creating images
cd ..
cd h2
docker build -t savaari/h2db .
minikube kubectl -- create deployment h2-service --image=savaari/h2db

cd ../Cab
./mvnw -DskipTests clean package
docker build -t savaari/cab_service .
minikube kubectl -- create deployment cab-service --image=savaari/cab_service

cd ../RideService
./mvnw -DskipTests clean package
docker build -t savaari/ride_service .
minikube kubectl -- create deployment ride-service --image=savaari/ride_service

cd ../wallet
./mvnw -DskipTests clean package
docker build -t savaari/wallet_service .
minikube kubectl -- create deployment wallet-service --image=savaari/wallet_service

cd ..

#================================================================================================================================================================
#creating the deployments and services






minikube kubectl -- expose deployment h2-service --type=ClusterIP --port=9092 --name=h2-service
minikube kubectl -- expose deployment cab-service --type=ClusterIP --port=8080 --name=cab-service
minikube kubectl -- expose deployment ride-service --type=ClusterIP --port=8081 --name=ride-service
minikube kubectl -- expose deployment wallet-service --type=ClusterIP --port=8082 --name=wallet-service

minikube kubectl -- expose deployment h2-service --type=LoadBalancer --port=8082 --name=h2-service-e
minikube kubectl -- expose deployment cab-service --type=LoadBalancer --port=8080 --name=cab-service-e
minikube kubectl -- expose deployment ride-service --type=LoadBalancer --port=8081 --name=ride-service-e
minikube kubectl -- expose deployment wallet-service --type=LoadBalancer --port=8082 --name=wallet-service-e

#================================================================================================================================================================
#change --imagePullPolicy: Never
	

