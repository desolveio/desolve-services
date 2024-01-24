#!/bin/sh
echo "[Desolve] Provide your Artifactory password:"
read -r artifactoryPassword
echo "[Desolve] Rebuilding..."

sh rebuildAndRun"$4".sh "$1" "$2" "$3" "$artifactoryPassword"
