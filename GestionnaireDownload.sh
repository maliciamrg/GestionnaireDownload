#!/bin/bash
# exit if I am already running
# bloquer : kill $(pgrep java)
NOW=$(date +"%F %T")
outfile="/media/kitchen/source_code/GestionnaireDownload/Log/GestionnaireDownload-`date +%Y-%m-%d`.html"
echo "UndoubleScript Launch-"$NOW >> $outfile
exec 9<$0
flock -n 9 || exit 1
cd /media/kitchen/source_code/GestionnaireDownload
sleep 20
java -cp ./lib/*:./bin Main >> $outfile 2>&1 
echo "UndoubleScript Exit---"$NOW >> $outfile
