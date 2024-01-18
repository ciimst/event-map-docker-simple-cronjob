REGISRTY=docker.io/imst
TAG=1.4.30
MINIKUBE_DRIVER=docker


while getopts r:t: option
do 
    case "${option}"
        in
        r)REGISRTY=${OPTARG};;
        t)TAG=${OPTARG};;
    esac
done

# (REGISRTY ve TAG kontrol)

echo "REGISRTY : $REGISRTY"
echo "TAG   : $TAG"
echo "MINIKUBE_DRIVER = $MINIKUBE_DRIVER"




echo "BUILDING Dockerfiles"

echo "BUILDING DockerfileCronjob"
docker build -t event_map_cronjob:$TAG -f DockerfileCronjob  .


docker tag event_map_cronjob:$TAG $REGISRTY/event_map_cronjob:$TAG


echo "PUSHING event_map_cronjob to $REGISRTY"
docker push $REGISRTY/event_map_cronjob:$TAG

docker image rm $REGISRTY/event_map_cronjob:$TAG






