#!/mnt/HD/HD_a2/ffp/bin/bash
# exit if I am already running
NOW=$(date +"%F %T")
outfile="/srv/www/pages/GestionnaireDownload/Log/GestionnaireDownload-`date +%Y-%m-%d`.html"
echo "UndoubleScript Launch-"$NOW >> $outfile
exec 200<$0
flock -n 200 || exit 1
cd /srv/www/pages/GestionnaireDownload
sleep 20
/ffp/opt/java/jre/bin/java -cp ./lib/*:./bin Main >> $outfile 2>&1 
echo "UndoubleScript Exit---"$NOW >> $outfile