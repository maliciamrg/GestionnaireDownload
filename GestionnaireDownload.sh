#!/mnt/HD/HD_a2/ffp/bin/bash
cd /srv/www/pages/GestionnaireDownload
/ffp/opt/java/jre/bin/java -cp ./lib/*:./bin Main >> /srv/www/pages/GestionnaireDownload/Log/GestionnaireDownload-`date +%Y-%m-%d`.html 2>&1 &