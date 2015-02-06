SeriesAutoDownload
==================


BoucleXmlSeries
---------------

00 14 * * * /ffp/bin/php /srv/www/pages/SerieAutoDownload/BoucleXmlSeries.php >> /srv/www/pages/SerieAutoDowload/Log/BoucleXmlSeries-`date +%Y-%m-%d`.html 2>&1 

GestionTorrents
---------------

15,45 * * * * /ffp/bin/php /srv/www/pages/SerieAutoDownload/GestionTorrents.php >> /srv/www/pages/SerieAutoDowload/Log/GestionTorrents-`date +%Y-%m-%d`.html 2>&1 
