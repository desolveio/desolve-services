#!/bin/sh
echo "[Desolve] Provide your Artifactory password:"
read -r artifactoryPassword
echo "[Desolve] Rebuilding..."

sh rebuildAndRunVolume.sh artifacts "$1" 50550 "$artifactoryPassword"
sh rebuildAndRun.sh workers "$1" 50500 "$artifactoryPassword"
sh rebuildAndRun.sh repository "$1" 80 "$artifactoryPassword"

echo "[Desolve] Rebuilt all."
