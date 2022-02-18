eval $(minikube docker-env)
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


sleep 10
#================================================================================================================================================================
#deleting the images
docker image rm savaari/wallet_service:latest savaari/ride_service:latest savaari/cab_service:latest savaari/h2db:latest
