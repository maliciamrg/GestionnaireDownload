<?php 


die("");
	error_reporting(E_ERROR | E_WARNING | E_PARSE | E_NOTICE);
	
const pathS="/mnt/HD/HD_b2/VideoClubSeries/Serie/";
const pathA="/mnt/HD/HD_a2/VideoClub/Anime/";

	echo "<!DOCTYPE html><html><body>";
	echo "*".str_repeat("-",053)."*". "</br>\n";              
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "|".str_repeat("-",016).date("Y-m-d H:i:s").str_repeat("-",016)."|". "</br>\n";   
	echo "|".str_repeat("-",053)."|". "</br>\n";        
		
	ini_set('display_errors', '1');

	sleep(rand(0, 10));
	
$path = array();
$path[]=pathS;
$path[]=pathA;

	$GV = array();
	$GV["ListAction"]="";
	$GV["nbepisodestotal"]=0;
	$GV["nbepisodespresenttotal"]=0;
	$GV["nbepisodesdisponibletotal"]=0;

    $EntrySolo="";
	if (@$_GET['argv1']) {
		$EntrySolo = @$_GET['argv1'];
	}
	if (isset($argv[1])) {
		$EntrySolo = strtolower($argv[1]);
	} 
	echo "-".$EntrySolo."-". "</br>\n";
	
	$LogOn = true;
	if (@$_GET['argv2']) {
		if ($_GET['argv2']=="off") {
			$LogOn = false;
		}
	}
	if (isset($argv[2])) {
		if ($argv[2]=="off") {
			$LogOn = false;
		}
	} 
	echo "-".$LogOn."-". "</br>\n";
	
	echo "*"."StartBoucleXmlSeries"."*". "</br>\n";

//die("");
	include 'Mail.php';
	include 'Commun.php';
	include '/srv/www/pages/BibPerso/Includefunctionphpcore.php';
	require_once( 'Serie.class.php' );
	
        //PhpFusionAddNews($numCategorie,$Subject,$news,$ExtendedNews);
	if($LogOn){
		$LogId = PhpFusionAddLogsCurrent();
	}
	
	Initialisation($path);

	$reper = $path[0];
	foreach(glob("$reper*.error") as $v){ 
		unlink($v);
	}
	foreach(glob("$reper*.log") as $v){ 
		unlink($v);
	}
	sleep(1);
	foreach(glob("$reper*.data") as $v){ 
		unlink($v);
	}
	sleep(1);
	foreach(glob("$reper*.tag") as $v){ 
		unlink($v);
	}
	sleep(1);
	foreach(glob("$reper*.Thread") as $v){ 
		unlink($v);
	}
	sleep(1);
	foreach(glob("$reper*.ThreadOut") as $v){ 
		unlink($v);
	}
	sleep(1);
	
	AddFiles($path[0],"BoucleXmlSeries","","tag");
	
	echo "*".""."*". "</br>\n";
	
	$NbThreadMax = 3;
	
	//******************************************************************//
	//**************SeriesConstitutionThread****************************//
	foreach ($path as $pathele){	
		echo "boucle principal 1=". $pathele."</br>\n";

		$MyDirectory = opendir($pathele) or die('Erreur opendir');
		while($Entry = @readdir($MyDirectory)) {
			//echo "*".$EntrySolo."=".$Entry."*"."</br>\n";
			if(is_dir($pathele.$Entry.DIRECTORY_SEPARATOR)&& $Entry != '.' && $Entry != '..') {
				//echo $EntrySolo."</br>\n";
				if (strtolower($EntrySolo) == strtolower($Entry) || $EntrySolo ==""){
					$ele["pathele"]=$pathele;
					$ele["Entry"]=$Entry;
					$rep[] = $ele;
				}
			}
		}
		closedir($MyDirectory);
	}
	
	asort($rep);
	//die("");	
	foreach ($rep as $Entry) {
		echo "*".$Entry["Entry"]."*". "</br>\n";
		$fileout = $GV["repbase"]."Log".DIRECTORY_SEPARATOR."SeriesConstitutionThread".DIRECTORY_SEPARATOR.preg_replace("/[^a-zA-Z0-9. ]/", "",$Entry["Entry"])."-SeriesConstitutionThread".".log";

	        AddFiles($path[0],$Entry["Entry"],"-SeriesConstitutionThread","tag");
		shell_exec("/ffp/bin/php /srv/www/pages/SerieAutoDownload/SeriesConstitutionThread.php  \"".$Entry["pathele"]."\" \"".$Entry["Entry"]."\" >'{$fileout}' 2>'{$fileout}' &");
	}
	
	($EntrySolo =="")?sleep(60):sleep(5);
	while (CountFiles($path[0],"*SeriesConstitutionThread","tag")>= 1) {
		($EntrySolo =="")?sleep(60):sleep(5);
	};
	//******************************************************************//

	//******************************************************************//
	//*******************SeriesReadThread*******************************//
	echo "boucle principal 2=". $path[0]."</br>\n";
	$files = glob($path[0]."*-Thread".".Thread", GLOB_MARK);
//	$files = glob(path."*3*-Thread".".Thread", GLOB_MARK);
    foreach ($files as $file) {
		while (CountFiles($path[0],"*SeriesReadThread","tag")>= $NbThreadMax) {
			($EntrySolo =="")?sleep(30):sleep(5);
		};
		$filename= basename($file);
		echo "*".$filename."*". "</br>\n";
		//$fileout = "/dev/null";
		$fileout = $GV["repbase"]."Log".DIRECTORY_SEPARATOR."SeriesReadThread".DIRECTORY_SEPARATOR.$filename."-SeriesReadThread".".log.html";
		shell_exec("/ffp/bin/php /srv/www/pages/SerieAutoDownload/SeriesReadThread.php '{$filename}' >'{$fileout}' 2>'{$fileout}' &");
		sleep(2);
	}
	
	($EntrySolo =="")?sleep(30):sleep(15);
	while (CountFiles($path[0],"*SeriesReadThread","tag")>= 1) {
		($EntrySolo =="")?sleep(30):sleep(5);
	};
	//******************************************************************//
	//******************************************************************//
	//*******************Mise a jour des Xml****************************//		
	echo "boucle principal 3=". $path[0]."</br>\n";
	
	$Bibseries = new BibSeries($path,$EntrySolo);
	
	foreach ($rep as $Entry) {
		echo "*".$Entry["Entry"]."*". "</br>\n";

		//*** recuperation des statistiques de l'entry en cours***//
		$fileStat =$path[0].preg_replace("/[^a-zA-Z0-9. ]/", "",$Entry["Entry"])."data".".data";
		$filestringStat = file_get_contents($fileStat);
		$arrayargvStat = unserialize($filestringStat);
		unlink($fileStat);
		$nbepisodes=$arrayargvStat["nbepisodes"];
		$nbepisodespresent=$arrayargvStat["nbepisodespresent"];
		$nbepisodesdisponible=$arrayargvStat["nbepisodesdisponible"];
		$RepDernieresaison=unserialize($arrayargvStat["#RepDernieresaison"]);
		$NumeroDernierSaison=$arrayargvStat["NumeroDernierSaison"];
				
		//*** recuperation des statistiques de l'entry en cours***//
		$filename=$path[0]."ThreadOut-".preg_replace("/[^a-zA-Z0-9. ]/", "",$Entry["Entry"]).".ThreadOut";
		if (file_exists ($filename)){
		
			$filestring = file_get_contents($filename);
			$filearray = explode("</br>\n", $filestring);
			unlink($filename);

			echo "*".$filename."*". "</br>\n";
				
			foreach ($filearray as $val) {
			
				if ($val==""){continue;}
				//$msg =""."|".$Entry."|".$local."|".path."|".$Entry."|".$typ."|".$ns."|".$ne."|".$Exclusion."|".$ti."|".$num."|".$pod."|".$seedv."|".$magnetv."|"."</br>\n"; 
				$arrayargv = unserialize($val);
					
				////*** entry en cours***//
				//$myEntryfile = OpenEntry($arrayargv["local"],$arrayargv["Entry"]);
				//		
				//if ($NumeroDernierSaison < $arrayargv["NumeroDernierSaison"]){$NumeroDernierSaison = $arrayargv["NumeroDernierSaison"];}
				$tabseedv=unserialize($arrayargv["#TorrentMagnet"]);
				//
				//if ($arrayargv["dt"] <= date("Ymd")) {
				//	if (count($tabseedv)>0){
				//	//if ($arrayargv[13]!="" && $arrayargv[12]>0){
				//		$nbepisodesdisponible ++;
				//		$RepDernieresaison[$arrayargv["ns"]][intval($arrayargv["ne"])]="o";
				//}
				//} else {
				//	$RepDernieresaison[$arrayargv["ns"]][intval($arrayargv["ne"])]="_";
				//}
				//
				//MajEntry($myEntryfile,$arrayargv["local"],$arrayargv["Entry"],$arrayargv["ns"],$arrayargv["ne"],$arrayargv["dt"],$arrayargv["ti"],$arrayargv["num"],$arrayargv["pod"],$NumeroDernierSaison,$tabseedv,"");
				//
				//serie
				if (!isset($Bibseries->serie[$arrayargv["Entry"]])){
					$Bibseries->serie[$arrayargv["Entry"]] = new Serie($arrayargv["Entry"]);
				}
				//echo $arrayargv["ne"]."=='*'".'-'. "</br>\n";
				if ($arrayargv["ne"]=="*"){
						$arrayargv["ne"] = base_convert($arrayargv["ns"]+360, 10, 36);  
				}
				//echo $arrayargv["ne"]."=='*'".'-'. "</br>\n";
				//episode

				$numid=$Bibseries->serie[$arrayargv["Entry"]]->addepisode('',$arrayargv["ti"],$arrayargv["ns"],$arrayargv["ne"],$arrayargv["dt"],$arrayargv["num"]);
				echo $numid.'--->'.count($tabseedv). "</br>\n";
				$Bibseries->serie[$arrayargv["Entry"]]->episodetab[$numid]->setpresence($arrayargv["pod"]);			
				//magnet
				foreach ($tabseedv as $key => $value) {
					if (is_numeric($arrayargv["ne"])){
						$Bibseries->serie[$arrayargv["Entry"]]->episodetab[$numid]->addmagnet( new magnet($value,$key) );
					} else {
						$Bibseries->serie[$arrayargv["Entry"]]->episodetab[$numid]->addmagnet( new magnet($value,$key+100) );
						// recalcul presence
						//$Bibseries->serie[$arrayargv["Entry"]]->episodetab[$numid]->calculpresence( $Bibseries->pathtoxml[$arrayargv["Entry"]] , $arrayargv["Entry"] , $Bibseries->serie[$arrayargv["Entry"]]->episodetab);
						//break;
					}
				}
				//$Bibseries->serie[$arrayargv["Entry"]]->Save($Bibseries->pathtoxml)	;
			}
		}	
			
		////*** traitement stat entry precedante***//
		//MefStatEntry($Entry,$nbepisodes,$nbepisodespresent,$nbepisodesdisponible,$RepDernieresaison);

	}
	
	$Bibseries->SaveBib($EntrySolo);
	

	//******************************************************************//
	//******************************************************************//
	//*******************Statistique************************************//		
	echo shell_exec("/ffp/bin/php /srv/www/pages/SerieAutoDownload/Statistiques.php \"".$EntrySolo."\" \"".base64_encode(serialize($path))."\" ");

	//if ($EntrySolo == ""){
		//PhpFusionAddNews($numCategorie,$Subject,$news,$ExtendedNews);
		$news = 'Tableau des episodes des series <a href=\"/BibPerso/gethtml.php?file='.$path[0].'Series'."-".str_replace("'","\'",$EntrySolo)."-".".html".'" target=\"_blank\">Series.html</a>';
		//$ExtendedNews = $msg;
		if($LogOn){
			PhpFusionAddNews(2,date('Y-m-d H:i:s',time())." ".'Etats des Series',$news,"");//$ExtendedNews);
			PhpFusionDelLogs ($LogId);
		}
	//}

	//SendMail('Series - Main ',$msg);

	//******************************************************************//
	
	echo "*"."EndBoucleXmlSeries"."*". "</br>\n";
	
	DelFiles($path[0],"BoucleXmlSeries","","tag");
    
//	SMBRelease($GV["repbase"]."tmp".DIRECTORY_SEPARATOR);
		
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "|".str_repeat("-",016).date("Y-m-d H:i:s").str_repeat("-",016)."|". "</br>\n";  
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "*".str_repeat("*",053)."*". "</br>\n";    
	
	echo "</body></html> ";
	
	function Initialisation($path){
		echo "Initialisation". "</br>\n";
		global $GV;

		$GV["repbase"] = str_replace('/',DIRECTORY_SEPARATOR , realpath( dirname(__FILE__) ). DIRECTORY_SEPARATOR);
		$Paramxmlbody = ChargerParam($GV["repbase"]."param.xml");
		
		$GV["BibliothequeSerie"]=$Paramxmlbody->getElementsByTagName('BibliothequeSerie')->item(0)->nodeValue;
		$GV["BibliothequeSerieW"]=$Paramxmlbody->getElementsByTagName('BibliothequeSerieWindows')->item(0)->nodeValue;
		
		//path=$GV["repbase"]."tmp".DIRECTORY_SEPARATOR."Serie".DIRECTORY_SEPARATOR;
    	SMBRelease($GV["repbase"]."tmp".DIRECTORY_SEPARATOR);
//		SMBMap($GV["repbase"],$GV["repbase"]."tmp".DIRECTORY_SEPARATOR,$GV["BibliothequeSerie"]);
//		if (!file_exists (path)){
//			if (!file_exists (path)){
//				if (isset($Paramxmlbody->getElementsByTagName('repSerie')->item(0)->nodeValue)){
//					if(file_exists ($Paramxmlbody->getElementsByTagName('repSerie')->item(0)->nodeValue)){
//						path = $Paramxmlbody->getElementsByTagName('repSerie')->item(0)->nodeValue;
//					}
//				}
//			}
//		}
		//path= path;
		if (!file_exists ($path[0])){die("répertoire inaccessible ". $path[0]. "</br>\n");}
		echo "repSerie=".$path[0]. "</br>\n";
		
		$GV["ListAction"] ="";
	}	
		
/*	function MefStatEntry($path,$Entry,$nbepisodes,$nbepisodespresent,$nbepisodesdisponible,$RepDernieresaison){
		echo "MefStat=>".$Entry."</br>\n";
		global $GV;

		$repre = Miseenforme($RepDernieresaison);
		
		if ($nbepisodes==0){
			$nb ="0";
		}else {
			$nb=($nbepisodespresent*100)/$nbepisodes;
		}
		$GV_ListAction = "<TR><TD><A HREF=\"http://home.daisy-street.fr/SerieAutoDownload/BoucleXmlSeries.php?argv1=".$Entry."\">$Entry</A></TD><TD>".$nbepisodes."</TD><TD>".$nbepisodespresent."</TD><TD>". number_format($nb,2)."%"."</TD><TD></TD><TD>".$nbepisodesdisponible. "</TD><TD>".$repre. "</TD></TR>" ;
		$GV["ListAction"] .= $GV_ListAction;
		$GV["nbepisodestotal"] = $GV["nbepisodestotal"] + $nbepisodes;
		$GV["nbepisodespresenttotal"] = $GV["nbepisodespresenttotal"] + $nbepisodespresent;
		$GV["nbepisodesdisponibletotal"] = $GV["nbepisodesdisponibletotal"] +  $nbepisodesdisponible;
//		}
		file_put_contents($path[0].$Entry.DIRECTORY_SEPARATOR.$Entry.".html","<TABLE BORDER>".$GV_ListAction."</TABLE>",FILE_APPEND);
	}	*/
			

?>
