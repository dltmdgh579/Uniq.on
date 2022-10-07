#!/bin/bash

echo "YOUR_PASSWORD" | docker login -u "YOUR_ID" --password-stdin

cd /home/ubuntu/uniq.on/
docker-compose down
docker rmi "YOUR_ID"/private:"FRONT_IMAGE_NAME"
docker rmi "YOUR_ID"/private:"BACK_IMAGE_NAME"
docker-compose up -d
# sudo bash init-letsencrypt.sh

docker logout
