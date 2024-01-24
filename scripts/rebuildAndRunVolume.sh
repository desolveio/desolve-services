#!/bin/sh
sh updateAndRebuild.sh "$1" "$2" "$4"

docker rm --force "$1"
docker run --detach  --name "$1" --volume "$1" -p "$3":8080 -it "$1":latest

echo "[Desolve] Now running $1 on port $3."
