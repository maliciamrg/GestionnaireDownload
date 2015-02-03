<?php 
die("");
echo "</TABLE>";
echo "</TABLE>";
	
const pathS="/mnt/HD/HD_b2/VideoClubSeries/Serie/";
const pathA="/mnt/HD/HD_a2/VideoClub/Anime/";
//const path="C:\\Users\\Malicia\\Dropbox\\AppProjects\\SerieAutoDownload\\Serie\\";
const pathw="V:\\Serie\\";
const TransmissionStart ="/ffp/start/transmission.sh start";
const TransmissionStop ="/ffp/start/transmission.sh stop";
const NbEncMax = 7;
const NbSeedMax = 7;

$path = array();
$path[]=pathS;
//$path[]=pathA;

$ActionARealiser=array();
$ActionARealiser["null"]=00;
$ActionARealiser["MarquerCommePresent"]=10;
$ActionARealiser["MarquerCommeAbsent"]=20;
$ActionARealiser["Start"]=30;
$ActionARealiser["Stop"]=40;
$ActionARealiser["Add"]=50;
$ActionARealiser["Remove"]=60;
$ActionARealiser["Exclure"]=70;
$ActionARealiser["Copier"]=80;

$TR_STATUS=array();
$TR_STATUS[0]="TR_STATUS_STOPPED";
$TR_STATUS[1]="TR_STATUS_CHECK_WAIT";
$TR_STATUS[2]="TR_STATUS_CHECK";
$TR_STATUS[3]="TR_STATUS_DOWNLOAD_WAIT";
$TR_STATUS[4]="TR_STATUS_DOWNLOAD";
$TR_STATUS[5]="TR_STATUS_SEED_WAIT";
$TR_STATUS[6]="TR_STATUS_SEED";
//die("");
Debut();

Load_include();

$Bibseries = new BibSeries($path);

//shell_exec(TransmissionStop);
//echo "<pre>"; print_r($Bibseries->Stats()); echo "</pre>";
//die("");
//$Bibseries = new BibSeries(path,"The Blacklist");
//$Bibseries = new BibSeries(path,"Stargate Atlantis");
//$Bibseries = new BibSeries(path,"",1);
//$Bibseries = new BibSeries(path,"Real Humans",1);
//$Bibseries = new BibSeries(path,"The Blacklist",1);
//$Bibseries = new BibSeries(path,"Stargate Atlantis",1);
//$Bibseries->SaveBib();
//die("");
//echo "<pre>"; print_r($Bibseries); echo "</pre>";
//$Bibseries->SaveBib();
//$Bibseries = new BibSeries(path,"The Blacklist");
//$Bibseries->serie["The Blacklist"]->episodetab["0102"]->addmagnet(new magnet( "test" , 10 );
//echo "<pre>"; print_r($Bibseries); echo "</pre>";
//echo "<pre>"; print_r($Bibseries->getlistnexttorent()); echo "</pre>";
//echo "<pre>"; print_r($Bibseries->getallhash()); echo "</pre>";

$GLOBALS["ttrans"] ="";
$GLOBALS["ttrans"] .= "<!DOCTYPE html><html><body>";
$GLOBALS["ttrans"] .= "<?php include '/srv/www/pages/BibPerso/krumo/class.krumo.php'; ?>";
$GLOBALS["ttrans"] .= "<title>Visualisation Transmission SeriesAutoDownload</title>";
$GLOBALS["ttrans"] .= "*".str_repeat("-",053)."*". "</br>\n";  
$GLOBALS["ttrans"] .= "|".str_repeat("-",053)."|". "</br>\n";   
$GLOBALS["ttrans"] .= "|".str_repeat("-",016).date("Y-m-d H:i:s").str_repeat("-",016)."|". "</br>\n";
$GLOBALS["ttrans"] .= "|".str_repeat("-",053)."|". "</br>\n";   


$TRPC = ChargementTransmission($Bibseries);
	
AnalyseTransmission($TRPC,$Bibseries);
	
MakeActionTransmission($TRPC,$Bibseries);

$GLOBALS["ttrans"] .= "|".str_repeat("-",053)."|". "</br>\n";   
$GLOBALS["ttrans"] .= "|".str_repeat("-",016).date("Y-m-d H:i:s").str_repeat("-",016)."|". "</br>\n";
$GLOBALS["ttrans"] .= "|".str_repeat("-",053)."|". "</br>\n";   
$GLOBALS["ttrans"] .= "*".str_repeat("-",053)."*". "</br>\n";  
$file_name ="/srv/www/pages/SerieAutoDownload/tabletransmission".".php";
file_put_contents($file_name, 	 $GLOBALS["ttrans"] );
$file_name = $path[0]."tabletransmission".".html";
file_put_contents($file_name, 	 $GLOBALS["ttrans"] );

$Bibseries->SaveBib();

MiseAJourStatistiques($path,$Bibseries->Stats());

makeChmod($path,$TRPC);
//******************************************************************//
//******************************************************************//
//*******************Statistique************************************//		
//echo "<pre>"; print_r($path); echo "</pre>";
echo shell_exec("/ffp/bin/php /srv/www/pages/SerieAutoDownload/Statistiques.php \"\" \"".base64_encode(serialize($path))."\" ");
	
//*fin*//
Fin();

//*debut*//
function Debut() {
//	error_reporting(E_ERROR | E_WARNING | E_PARSE | E_NOTICE);
	error_reporting(E_ALL);
	ini_set('display_errors', '1');
	echo "<!DOCTYPE html><html><body>";
	echo "*".str_repeat("-",053)."*". "</br>\n";              
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "|".str_repeat("-",016).date("Y-m-d H:i:s").str_repeat("-",016)."|". "</br>\n";   
	echo "|".str_repeat("-",053)."|". "</br>\n"; 
	echo "*". __FUNCTION__." of ".$_SERVER['SCRIPT_NAME']."*". "</br>\n";	
}

//*initialisation*//
function load_include(){
	echo "-----".__FUNCTION__."-----"."</br>\n"; 
	require_once( 'Serie.class.php' );
	require_once( 'Statistiques.class.php' );
	require_once( 'TransmissionRPC.class.php' );
//	include 'Mail.php';
//	include 'Commun.php';	
	include '/srv/www/pages/BibPerso/krumo/class.krumo.php';
	include '/srv/www/pages/BibPerso/Includefunctionphpcore.php';



}

//*Transmission*//

function ChargementTransmission($Bibseries){
	echo "-----".__FUNCTION__."-----"."</br>\n"; 
	$tabletransmission="";
	$ret = array();
	
	
	for ($i = 1; $i <= 10; $i++) {
		try
		{	
			$GVrpc = new TransmissionRPC(); 
		} catch (Exception $e) {
			echo('[ERROR] ' . $e->getMessage() . PHP_EOL . "</br>\n");
			shell_exec(TransmissionStart);
			echo 'Transmission=START'."</br>\n" ;
			sleep(6);
			try
			{	
				$GVrpc = new TransmissionRPC(); 
			} catch (Exception $e) {
				die('[ERROR] ' . $e->getMessage() . PHP_EOL . "</br>\n");
			} 
		} 	
		
		$result = $GVrpc->sstats( );
		print "GET SESSION STATS... [{$result->result}]"."</br>\n";
	
		$GVrpc->set( array( "seed-queue-size" => 5 ));
		$GVrpc->set( array( "seed-queue-enabled" => 0 ));
		$GVrpc->set( array( "download-queue-enabled" => 1 ));
		$GVrpc->set( array( "download_queue_size" => 10 ));
	
		$Tport=$GVrpc->ptest();
		if (!isset($Tport->arguments->port_is_open)){
			echo "port_is_open="."false". "</br>\n";
			shell_exec(TransmissionStop);
			echo 'Transmission=STOP'."</br>\n" ;
			sleep(5);
		} else {
			echo "port_is_open=".$Tport->arguments->port_is_open. "</br>\n";
			break;
		}
	}

	
	$ret["rpc_download_dir"] = $GVrpc->sget()->arguments->download_dir . DIRECTORY_SEPARATOR;
	//$ret["SessionStat"] = $GVrpc->sstats();
	$ret["arrayridstatus"] = $GVrpc->get();
	$ret["TransmissionRpc"] = $GVrpc;
	
	//echo "<pre>"; print_r($ret["arrayridstatus"]->arguments); echo "</pre>";	
	//die("");
	//*Mise a jour des status de la bibliotheque*//;
	//$allhash = array();
	$allhash = $Bibseries->getallhash();	
	//krumo($allhash);
	//die("");
	foreach (@$ret["arrayridstatus"]->arguments->torrents as $t ) 
	{
		if (isset($allhash[strtolower($t->hashString)])){
			// recalcul presence
			$Bibseries->serie[ $allhash[strtolower($t->hashString)]["serie"] ]->episodetab[ $allhash[strtolower($t->hashString)]["numid"] ]->calculpresence( $Bibseries->pathtoxml[ $allhash[strtolower($t->hashString)]["serie"] ], $allhash[strtolower($t->hashString)]["serie"] , $Bibseries->serie[ $allhash[strtolower($t->hashString)]["serie"] ]->episodetab);
			//mise a encours
			$Bibseries->serie[ $allhash[strtolower($t->hashString)]["serie"] ]->episodetab[ $allhash[strtolower($t->hashString)]["numid"] ]->sethashenc( $allhash[strtolower($t->hashString)]["hashString"] );
			//exclusion de allhash
			echo "unset de allhash : series=". $allhash[strtolower($t->hashString)]["serie"]. " hash=".strtolower($t->hashString). "</br>\n";
			unset ($allhash[strtolower($t->hashString)]);
		}
	}
		//les encours restant dans allhash sount des faux encours
	foreach (@$allhash as $ah ) 
	{
		if ($Bibseries->serie[$ah["serie"]]->episodetab[$ah["numid"]]->statusEnc($ah["hashString"]) ) {
			$Bibseries->serie[$ah["serie"]]->episodetab[$ah["numid"]]->sethashVIDE($ah["hashString"]); 
		}
	}
	
	
	
	//*exclusion des hash non gerer par la bibliotheque*//
	$tabletransmission .=    "<TABLE BORDER>";	
	$allhash=$Bibseries->getallhash();
	$count = 0;
	foreach (@$ret["arrayridstatus"]->arguments->torrents as $key => $t ) 
	{
		if (isset($allhash[strtolower($t->hashString)]))
		{
			$ret["arrayridstatus"]->arguments->torrents[$key]->serie = $allhash[strtolower($t->hashString)];
			$ret["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["null"];
			$tabletransmission .=  "<TR>";
			$tabletransmission .=  "<TD>";
			$tabletransmission .=   "name=".$t->name. "</br>\n";
			$tabletransmission .=  "</TD><TD>";
			$tabletransmission .=     (isset($t->isStalled))?"Stalled"."</br>\n":"";
			$tabletransmission .=     (isset($t->doneDate))?date('Y-m-d H:i:s',@$t->doneDate)."</br>\n":"";
			$tabletransmission .=     (isset($t->percentDone))?sprintf('%1$03d',($t->percentDone*100))." % complete"."</br>\n":"";
			$tabletransmission .=     (isset($t->status))? $GLOBALS["TR_STATUS"][$t->status]."</br>\n":"";
			$tabletransmission .=    "</TD><TD>";
						 // krumo(@$t);
			if (isset($t->files)) {
				foreach (@$t->files as $keyf => $f )  {
							// krumo(@$f); 
							// krumo(@$key); 
							// krumo(@$t->fileStats); 
							// krumo(@$t->fileStats[$keyf]); 
					$etatfile= array();
					$by = (isset($f->bytesCompleted))?$f->bytesCompleted:0;
					$le = (isset($f->length))?$f->length:1;
					$na = (isset($f->name))?$f->name:"";
					$etatfile["pourcent"]=sprintf('%1$03d',(($by/$le)*100)." % complete");
					$etatfile["name"]=$na;
					$etatfile["bytesCompleted"]=$by;
					$etatfile["length"]=$le;	
					if (isset($t->fileStats[$keyf]->wanted)) {
						$ret["arrayridstatus"]->arguments->torrents[$key]->serie["etatfiles"]["files-wanted"][] = $etatfile;
					} else {
						$ret["arrayridstatus"]->arguments->torrents[$key]->serie["etatfiles"]["files-unwanted"][] = $etatfile;
					}
				}
			}
			// krumo(@$ret["arrayridstatus"]->arguments->torrents[$key]->serie["etatfiles"]); 
			$tabletransmission .=  '<?php krumo(unserialize(base64_decode(\''.base64_encode(serialize(@$ret["arrayridstatus"]->arguments->torrents[$key]->serie["etatfiles"])).'\'))); ?>';
			// die("");
			//$tabletransmission .=    "****************";
			$tabletransmission .=    "</TD><TD>";
			$tabletransmission .=    "</TR>";
			$count ++;
		} else	{
		
			$filesize = 0;
			$filename ="";
			if (isset($t->files)) {
				foreach ($t->files as $f) {
					if($filesize <  $f->length){
						$filesize =  $f->length;
						$filename = $f->name;
					}
				}	
				$filename=$ret["rpc_download_dir"].$filename;
			}
			
			$tabletransmission .=    "<TR>";
			$tabletransmission .=    "<TD>";
			$tabletransmission .=     "name=".$t->name. "</br>\n";
			//$tabletransmission .=    '<p>'.$t->name.'<a href=\"./BibPerso/stream.php?flux='.urlencode($filename).'" target=\"_blank\">'.basename($filename).'</a></p>';
			$tabletransmission .=    '<p>'.$t->name.'<a href=/BibPerso/stream.php?flux="'.($filename).'" target="_blank">'.basename($filename).'</a></p>';
			$tabletransmission .=    "</TD><TD>";
			$tabletransmission .=     (isset($t->isStalled))?"Stalled"."</br>\n":"";
			$tabletransmission .=     (isset($t->doneDate))?date('Y-m-d H:i:s',@$t->doneDate)."</br>\n":"";
			$tabletransmission .=     (isset($t->percentDone))?sprintf('%1$03d',($t->percentDone*100))." % complete"."</br>\n":"";
			$tabletransmission .=     (isset($t->status))? $GLOBALS["TR_STATUS"][$t->status]."</br>\n":"";
			$tabletransmission .=    "</TD><TD>";
			$tabletransmission .=     '<a href=/SerieAutoDownload/movefilm.php?hash='.$t->hashString.'&mode=film target="_blank">film</a> / <a href=/SerieAutoDownload/movefilm.php?hash='.$t->hashString.'&mode=cam target="_blank">cam</a> / <a href=/SerieAutoDownload/PurgeTorrentsByHash.php?hash='.$t->hashString.' target="_blank">purge</a> ';
			$tabletransmission .=    "</TD><TD>";
			$tabletransmission .=    "</TR>";
			unset ($ret["arrayridstatus"]->arguments->torrents[$key]);
		}
	}
	$tabletransmission .=     "</TABLE>";
	$tabletransmission .=   "nb restant $count". "</br>\n";
	//echo "<pre>"; print_r($allhash); echo "</pre>";
	echo $tabletransmission;
	//die("");
	 $GLOBALS["ttrans"] .=$tabletransmission;
	return $ret;
}
function AnalyseTransmission($TRPC,$Bibseries){
	echo "-----".__FUNCTION__."-----"."</br>\n"; 
	
	$tabletransmission2="";
	
	$nbtorrent_enc = 0;
	$nbtorrent_seed = 0;
	$nbtorrent_Exclure = 0;
	$nbtorrent_Start = 0;
	$nbtorrent_Stop = 0;
	$nbtorrent_Remove = 0;
	$nbtorrent_Copier = 0;
	$nbtorrent_CopierBloquée = 0;
	
	$vingtquatreheure = 86400;
	$douzeheure = 43200;
	$sixheure = 21600;
	$troisheure = 10800;
	$deuxheure = 7200;
	$uneheure = 3600;
		
	$tabletransmission2 .= "<TABLE BORDER>";	
	foreach (@$TRPC["arrayridstatus"]->arguments->torrents as $key => $t ) 
	{
//		echo $t->name." - ".@$t->doneDate." - ".@$t->serie["presence"]." - ".@$t->isStalled." - ".@$t->status. "</br>\n";
    	/*dead download*/
		$tabletransmission2 .=  "<TR>";
		$tabletransmission2 .=  "<TD>";
		$tabletransmission2 .=   "name=".$t->name. "</br>\n";
		$tabletransmission2 .=  "</TD><TD>";
		$tabletransmission2 .=   (isset($t->isStalled))?"Stalled"."</br>\n":"";
		$tabletransmission2 .=   (isset($t->serie["presence"]))?"presence=".$t->serie["presence"]."</br>\n":"";
		$tabletransmission2 .=   (isset($t->doneDate))?date('Y-m-d H:i:s',@$t->doneDate)."</br>\n":"";
		$tabletransmission2 .=   (isset($t->status))? $GLOBALS["TR_STATUS"][$t->status]."</br>\n":"";
		$tabletransmission2 .=  "</TD><TD>";
		
		if (isset($t->isStalled) || @$t->status == 4 ) {
			$Asupp = false;
			if (isset($t->startDate)) {
				if (isset($t->activityDate)) {
					$tpc = time() - $t->activityDate;
					$reste = $uneheure - $tpc;
					$tabletransmission2 .=   "Last activity (ago) =".$tpc." reste ".$reste." s".  "</br>\n";		
					if ($reste < 0 ){
						$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Exclure"];
						$nbtorrent_Exclure +=1;
						$tabletransmission2 .=  " Exclure"."</br>\n"; 
					}
				} else {
					$tpc = time() - $t->addedDate;
					$reste = $deuxheure - $tpc;
					$tabletransmission2 .=   "Added there =".$tpc." reste ".$reste." s". "</br>\n";
					if ($reste < 0 ){
						$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Exclure"];
						$nbtorrent_Exclure +=1;
						$tabletransmission2 .=  " Exclure"."</br>\n"; 
					}
				}
			}
		}

		/*torrent arreter*/
		if (!isset($t->status)) {
			if (isset($t->doneDate)) {
				if (@$t->serie["presence"]==1){
					$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Remove"];
					$nbtorrent_Remove +=1;
					$tabletransmission2 .=  " Remove"."</br>\n";
				}
			} else {
				$nbtorrent_enc = $nbtorrent_enc + 1 ;
				$tabletransmission2 .=  " Encours"."</br>\n";
				$tabletransmission2 .=  '<?php krumo(unserialize(base64_decode(\''.base64_encode(serialize($t->files)).'\'))); ?>';
				//$tabletransmission2 .=  "<pre>".print_r(@$t->files,TRUE)."</pre>";
				if ($nbtorrent_enc > NbEncMax){
					$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Stop"];
					$nbtorrent_Stop +=1;
					$tabletransmission2 .=  " Stop"."</br>\n";
				} else {
					$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Start"];
					$nbtorrent_Start +=1;
					$tabletransmission2 .=  " Start"."</br>\n";
				}
			}
		}
		
		/*gestion seeding*/
		if (isset($t->status)) {
			if (isset($t->doneDate)) {
				$nbtorrent_seed = $nbtorrent_seed + 1;
				$tabletransmission2 .=   " Seed"."</br>\n";
				if ($nbtorrent_seed > NbSeedMax){
					$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Stop"];
					$nbtorrent_Stop +=1;
					$tabletransmission2 .=  " Stop"."</br>\n";
				}
			}
		}
		
		/*torrent en cours de download*/
		if (@$t->status == 3 || @$t->status == 4 ) {
			if ($TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser != $GLOBALS["ActionARealiser"]["Stop"] && 
				$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser != $GLOBALS["ActionARealiser"]["Remove"] && 
				$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser != $GLOBALS["ActionARealiser"]["Exclure"]  
				){
				/*Test de copy en amont*/
				$ret = $Bibseries->serie[ $t->serie["serie"] ]->copyepisode( $t->serie["numid"]  , $TRPC["rpc_download_dir"] , @$t->files , $Bibseries->pathtoxml[ $t->serie["serie"] ] , True );
				$tabletransmission2 .=  '<?php krumo(unserialize(base64_decode(\''.base64_encode(serialize($ret)).'\'))); ?>';			
				if (isset($ret["files-wanted"])){
					$TRPC["arrayridstatus"]->arguments->torrents[$key]->serie["iwant"]["files-wanted"] = $ret["files-wanted"];
				}
				if (isset($ret["files-unwanted"])){
					$TRPC["arrayridstatus"]->arguments->torrents[$key]->serie["iwant"]["files-unwanted"] = $ret["files-unwanted"];
				}


				if ($ret["status"]  || (!is_array(@$t->files)) ) {
					$nbtorrent_enc = $nbtorrent_enc + 1 ;
					$tabletransmission2 .=  " Encours"."</br>\n";
					//$tabletransmission2 .=  "<pre>".print_r($ret,TRUE)."</pre>";
					//$tabletransmission2 .=  "<pre>".print_r(@$t->files,TRUE)."</pre>";
					if ($nbtorrent_enc > NbEncMax){
						$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Stop"];
						$nbtorrent_Stop +=1;
						$tabletransmission2 .=  " Stop"."</br>\n";
					}
				} else { 
					$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Exclure"];
					$nbtorrent_Exclure +=1;
					$tabletransmission2 .=  " Exclure"."</br>\n";
					//$tabletransmission2 .=  "<pre>".print_r($ret,TRUE)."</pre>";
				}
			 }
		}
		
		/*torrent lancer a tord*/
		if (@$t->status == 3 || @$t->status == 4 ){
			if (@$t->serie["presence"]==1){
				$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Remove"];
				$nbtorrent_Remove +=1;
				$tabletransmission2 .=  " Remove"."</br>\n";
			}
		}
		
		/*torrent termin?*/
		if (isset($t->doneDate)) {
			if (@$t->serie["presence"]!=1 && $Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $t->serie["numid"] ]->estencours() ){
				$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Copier"];
				$nbtorrent_Copier +=1;
				$tabletransmission2 .=  " Copier"."</br>\n";
				/*copie*/
				$ret = $Bibseries->serie[ $t->serie["serie"] ]->copyepisode( $t->serie["numid"]  , $TRPC["rpc_download_dir"] , $t->files , $Bibseries->pathtoxml[ $t->serie["serie"] ] );
				$tabletransmission2 .=  '<?php krumo(unserialize(base64_decode(\''.base64_encode(serialize($ret)).'\'))); ?>';
				if (isset($ret["files-wanted"])){
					$TRPC["arrayridstatus"]->arguments->torrents[$key]->serie["iwant"]["files-wanted"] = $ret["files-wanted"];
				}
				if (isset($ret["files-unwanted"])){
					$TRPC["arrayridstatus"]->arguments->torrents[$key]->serie["iwant"]["files-unwanted"] = $ret["files-unwanted"];
				}
				
				if ($ret["status"]) {
					foreach ($ret["copyde"] as $ele) {
						Creationraccourci($ele["ns"],$ele["nsd"],$Bibseries->pathtoxml[$t->serie["serie"]],$ele["dest"]);
						//maj episode reel
						$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $ele["numid"] ]->sethashok( $t->hashString );			
						$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $ele["numid"] ]->setdtcopystock( $t->hashString );
						$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $ele["numid"] ]->setpresence(true);
						//maj episode theorique
						$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $t->serie["numid"] ]->sethashok( $t->hashString );	
					}
				}else{
					if ($ret["faketorrent"]) {
						$tabletransmission2 .=   " faketorrent". "</br>\n";
						$TRPC["arrayridstatus"]->arguments->torrents[$key]->ActionARealiser = $GLOBALS["ActionARealiser"]["Exclure"];
						$nbtorrent_Exclure +=1;
						$tabletransmission2 .=  " Exclure"."</br>\n";	
					} else {
						$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $t->serie["numid"] ]->setdtcopybloque( $t->hashString );
//						$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $t->serie["numid"] ]->sethashok( $t->hashString );	
						$nbtorrent_CopierBloquée +=1;
						$tabletransmission2 .=  ' CopierBloquée <a href=/SerieAutoDownload/PurgeTorrentsByHash.php?hash='.$t->hashString.' target="_blank">purge</a> ';
						//$tabletransmission2 .=  "<pre>".print_r($ret,TRUE)."</pre>";
					}
				}
			}
		}
		$tabletransmission2 .=  "</TR>";
	}
	$tabletransmission2 .=   "</TABLE>";

	$TRPC["arrayridstatus"]->NbALancer = NbEncMax - $nbtorrent_enc;
	
	
	$tabletransmission2 .=   "<TABLE BORDER>";			
	$tabletransmission2 .=  "<TR>";
	$tabletransmission2 .=  "<TD>";
	$tabletransmission2 .=   "enc=$nbtorrent_enc /".NbEncMax."</br>\n";
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=   "seed=$nbtorrent_seed /".NbSeedMax. "</br>\n"; 
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=   "Start=$nbtorrent_Start". "</br>\n";
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=   "Stop=$nbtorrent_Stop". "</br>\n";
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=   "Remove=$nbtorrent_Remove". "</br>\n";	
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=   "Copier=$nbtorrent_Copier". "</br>\n";
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=   "Exclure=$nbtorrent_Exclure". "</br>\n";
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=   "CopierBloquée=$nbtorrent_CopierBloquée". "</br>\n";
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=  "</TD><TD>";
	$tabletransmission2 .=   "A lancer=".$TRPC["arrayridstatus"]->NbALancer."</br>\n";	
	$tabletransmission2 .=  "</TD>";
	$tabletransmission2 .=  "</TR>";
	$tabletransmission2 .=   "</TABLE>";
	echo $tabletransmission2;
	 	 $GLOBALS["ttrans"] .=$tabletransmission2;

	 //die("");
}

function Creationraccourci($ns,$dernieresaison,$pathtoxml,$FichierEpisode) {	
	global $GV;
	
	$Add="";
	$AddNew="";

	if ($ns >= $dernieresaison) {
		$Add="-Last-";
		$AddNew='<img src=\"/phpCore/images/star.png\" alt=\"\" width=\"16\" height=\"16\" />';
	}

	// echo "creation raccourci";
	$content = "[InternetShortcut]"."\n";
	$content .= "URL=\"".pathw.str_replace($pathtoxml,"",$FichierEpisode)."\""."\n";
	$content .= "IconIndex=45"."\n";
	$content .= "IconFile=C:\Windows\System32\shell32.dll"."\n";
	$fp = fopen($pathtoxml.$Add.basename($FichierEpisode)." - Raccourci.url","wb");
	fwrite($fp,$content);
	fclose($fp);

	//PhpFusionAddNews($numCategorie,$Subject,$news,$ExtendedNews);
	$news = "Liens vers le fichier ==> ".'<p>'.$AddNew.'<a href=\"/BibPerso/stream.php?flux='.urlencode($FichierEpisode).'" target=\"_blank\">'.basename($FichierEpisode).'</a></p>';
	$ExtendedNews = 'Les Statistiques des series ==> <a href=\"/BibPerso/gethtml.php?file='.$pathtoxml.'statistique.html" target=\"_blank\">statistique.html</a>';
	PhpFusionAddNews(9,basename($FichierEpisode),$news,$ExtendedNews);
}

function MakeActionTransmission($TRPC,$Bibseries){
	echo "-----".__FUNCTION__."-----"."</br>\n"; 
$tabletransmission3 =   "";		


$tabletransmission3 .=   "<TABLE BORDER>";			
	//$TRPC["arrayridstatus"]->NbALancer =0;
	// lancement particulier
	if (@$_GET['argv1'] && @$_GET['argv2']) {	
	//echo $_GET['argv1']."-"."</br>\n"; 
	//echo $_GET['argv2']."-"."</br>\n"; 
	//echo "<pre>"; print_r($Bibseries->serie[ $_GET['argv1']]); echo "</pre>";
		$NextT = $Bibseries->serie[ $_GET['argv1'] ]->episodetab[ $_GET['argv2'] ]->getlistnexttorent($_GET['argv1']);
		 $tabletransmission3 .= lancement($TRPC,$Bibseries,$NextT);
	}
	
	//lancement suite a gestion
	while ($TRPC["arrayridstatus"]->NbALancer > 0) {
		$l = $Bibseries->getlistnexttorent();
		//echo "<pre>"; print_r($l); echo "</pre>";
		//$tabletransmission3 .= count($l)."</br>\n";
		$NextT = current($l);
		//echo "<pre>"; print_r($NextT); echo "</pre>";
		if ($NextT["magnetlink"]==""){break;}
		 $tabletransmission3 .= lancement($TRPC,$Bibseries,$NextT);	
	}
	
	$tabletransmission3 .=   "</TABLE>";
	echo $tabletransmission3;
	$GLOBALS["ttrans"] .=$tabletransmission3;
	
	foreach (@$TRPC["arrayridstatus"]->arguments->torrents as $key => $t ) 
	{
		//gestion des fichier voulu ou pas
		if ( @isset($t->serie["iwant"]["files-unwanted"]) || @isset($t->serie["iwant"]["files-wanted"]) ){
			//krumo ($TRPC["TransmissionRpc"]->get( $t->id ));
			//krumo ($TRPC["arrayridstatus"]->arguments->torrents[$key]->serie);
			$ret = $TRPC["TransmissionRpc"]->set( $t->id, $t->serie["iwant"]);
			//krumo ($TRPC["TransmissionRpc"]->get( $t->id ));
			
		}
		
		switch($t->ActionARealiser) {
			case $GLOBALS["ActionARealiser"]["null"]:
				break;
			case $GLOBALS["ActionARealiser"]["MarquerCommePresent"]:
				break;
			case $GLOBALS["ActionARealiser"]["MarquerCommeAbsent"]:
				break;
			case $GLOBALS["ActionARealiser"]["Start"]:
				$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $t->serie["numid"] ]->sethashenc( $t->hashString );
				$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $t->serie["numid"] ]->setdtrelance( $t->hashString );
				$TRPC["TransmissionRpc"]->start($t->id);
				break;
			case $GLOBALS["ActionARealiser"]["Stop"]:
				$TRPC["TransmissionRpc"]->stop($t->id);
				break;
			case $GLOBALS["ActionARealiser"]["Add"]:
				break;
			case $GLOBALS["ActionARealiser"]["Exclure"]:
				$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $t->serie["numid"] ]->sethashko( $t->hashString );
				$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $t->serie["numid"] ]->setdtpurge( $t->hashString );
				$TRPC["TransmissionRpc"]->remove($t->id,true);
				break;
			case $GLOBALS["ActionARealiser"]["Remove"]:
				$Bibseries->serie[ $t->serie["serie"] ]->episodetab[ $t->serie["numid"] ]->setdtpurge( $t->hashString );
				$TRPC["TransmissionRpc"]->remove($t->id,true);
				break;
		}
	}

}	

function lancement($TRPC,$Bibseries,$NextT){
$tabletransmission4 = "";
	if ($NextT["magnetlink"]){
		//echo "<pre>"; print_r($NextT); echo "</pre>";
		$result =  $TRPC["TransmissionRpc"]->add_file($NextT["magnetlink"] ,"" );
		//echo "<pre>"; print_r($result); echo "</pre>";
	$tabletransmission4 .=  "<TR>";
	$tabletransmission4 .=  "<TD>";
	$tabletransmission4 .=   " $NextT[serie] $NextT[numid] $NextT[titre] "."</br>\n";
		if (@isset($result->arguments->torrent_added) || @isset($result->arguments->torrent_duplicate) ) {
			echo "<pre>"; print_r($result); echo "</pre>";
			$Bibseries->serie[ $NextT["serie"] ]->episodetab[ $NextT["numid"] ]->sethashenc( $NextT["hashString"]);
			$Bibseries->serie[ $NextT["serie"] ]->episodetab[ $NextT["numid"] ]->setdtajout( $NextT["hashString"]);
	$tabletransmission4 .=  "</TD><TD>";
			if (!@isset($result->arguments->torrent_duplicate)){
	$tabletransmission4 .=   " Ok "."</br>\n";
			} else {
	$tabletransmission4 .=   " Dup "."</br>\n";
			}
		} else {
	$tabletransmission4 .=  "</TD><TD>";
	$tabletransmission4 .=   " Ko "."</br>\n";
			$Bibseries->serie[ $NextT["serie"] ]->episodetab[ $NextT["numid"] ]->sethashKo( $NextT["hashString"]);
          }
	$tabletransmission4 .=  "</TR>";
	if (!@isset($result->arguments->torrent_duplicate)){
		$TRPC["arrayridstatus"]->NbALancer-- ;
	}
return $tabletransmission4;
	}
}
		
function MiseAJourStatistiques($path,$Statstab) {
	//echo "<pre>"; print_r($Statstab); echo "</pre>";
	$Statistiques = new Statistiques($path);
	$Statistiques->addcompteur(date('Y-m-d H:i:s',time()),$Statstab["nbeppresent"],$Statstab["nbepencours"],$Statstab["nbepadown"],$Statstab["nbepabsent"],$Statstab["nbepavenir"]);
	//echo "<pre>"; print_r($Statistiques); echo "</pre>";
	$file_name = $path[0]."statistique".".html";
	file_put_contents($file_name, $Statistiques->Output());
	//$file_name = $path[1]."statistique".".html";
	file_put_contents($file_name, $Statistiques->Output());
}

//*droit sur les repertoire**//
function makeChmod($path,$TRPC){
	exec ("find '".$path[0]."' -exec chmod 0777 {} +"); 
	//exec ("find '".$path[1]."' -exec chmod 0777 {} +"); 
	exec ("find '".$TRPC["rpc_download_dir"]."' -exec chmod 0777 {} +"); 
	
}

//*fin*//
function Fin() {
	echo "*". __FUNCTION__." of ".$_SERVER['SCRIPT_NAME']."*". "</br>\n";	
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "|".str_repeat("-",016).date("Y-m-d H:i:s").str_repeat("-",016)."|". "</br>\n";  
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "*".str_repeat("*",053)."*". "</br>\n";   
	echo "</body></html> ";	
}

?>
