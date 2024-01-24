#!/bin/sh
cd /opt/desolve/"$1" || mkdir /opt/desolve/"$1"
wget --user=admin --password=$3 https://artifactory.scala.gg/artifactory/gradle-release/io/desolve/services/"$1"/"$2"/"$1"-"$2".jar

mv "$1"-"$2".jar "$1".jar

docker build -t "$1":latest .
