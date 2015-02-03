<?php 

/*
	$argv[1]=$Entry
*/

	error_reporting(E_ERROR | E_WARNING | E_PARSE | E_NOTICE);
	
const path="/mnt/HD/HD_b2/VideoClubSeries/Serie/";

	echo "<!DOCTYPE html><html><body>";
	
	echo "*".str_repeat("-",053)."*". "</br>\n";              
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "|".str_repeat("-",016).date("Y-m-d H:i:s").str_repeat("-",016)."|". "</br>\n";   
	echo "|".str_repeat("-",053)."|". "</br>\n";        
		
	ini_set('display_errors', '1');

	sleep(rand(0, 10));

	$GV = array();

	echo "*"."StartSeriesReadThread"."*". "</br>\n";

	include 'Mail.php';
	include 'Commun.php';

	Initialisation();

	if (isset($argv[1])){
		echo "*".$argv[1]."*". "</br>\n";
		$filename=path.$argv[1];
		$filestring = file_get_contents($filename);
		$filearray = explode("</br>\n", $filestring);
		unlink($filename);
		$EntryGlobal=$argv[1];
	} else {
		if (@$_GET['Entry'] && @$_GET['ns'] && @$_GET['ne']) {
			$temp["Entry"]=$_GET['Entry'];
			$temp["ns"]=$_GET['ns'];
			$temp["ne"]=$_GET['ne'];
			$temp["ti"]="";
			$temp["dt"]="";
			$temp["repSerie"]=path;
			$temp["Exclusion"]="";
			$temp["typ"]="";
			$temp["num"]="";
			$temp["pod"]="";
			$temp["local"]="";
			$filearray[1] = serialize($temp);
			$EntryGlobal=$_GET['Entry'];
		} else {
			die("pas de parametre pour le lancement");
		}
	}
	
	AddFiles(path,$EntryGlobal,"SeriesReadThread","tag");
	
	echo "<TABLE BORDER>";	
	foreach ($filearray as $val) {
		if ($val==""){continue;}
		//$msg = $ns."|".$ne."|".$dt."|".$ti."|".$num."|".$pod."|".$local."|".path."|".$Entry."|".$typ."|".$Exclusion."|"."</br>\n"; 
		$arrayargv = unserialize($val);
		//print_r($arrayargv);

		echo "<TR><TD>".FormatNom($arrayargv["Entry"],"",$arrayargv["ns"],$arrayargv["ne"],$arrayargv["ti"])."</br>\n";
		$TorrentMagnet=array();
		
		if ($arrayargv["pod"] !="1" ) {
			echo $arrayargv["Entry"]." S".$arrayargv["ns"]."E".$arrayargv["ne"]." ".$arrayargv["dt"]." <= ".date("Ymd")."</br>\n";
			if ($arrayargv["dt"] <= date("Ymd") ) {
			
				sleep(1);
				$arrRet1=array();
				$arrRet1 = get_magnet_kat($arrayargv["repSerie"],$arrayargv["Entry"],$arrayargv["typ"],$arrayargv["ns"],$arrayargv["ne"],$arrayargv["Exclusion"]);
				//print_r($arrRet);
				//$TorrentMagnet[$arrRet[0][1]]=$arrRet[0][0];
				//$TorrentMagnet[$arrRet[1][1]]=$arrRet[1][0];
				//$TorrentMagnet[$arrRet[2][1]]=$arrRet[2][0];
				//print_r($TorrentMagnet);
				
				sleep(1);
				$arrRet2=array();
		//**//		$arrRet2 = get_magnet_tpb($arrayargv["repSerie"],$arrayargv["Entry"],$arrayargv["typ"],$arrayargv["ns"],$arrayargv["ne"],$arrayargv["Exclusion"]);
				//$TorrentMagnet[$arrRet[0][1]]=$arrRet[0][0];
				//$TorrentMagnet[$arrRet[1][1]]=$arrRet[1][0];
				//$TorrentMagnet[$arrRet[2][1]]=$arrRet[2][0];

				$TorrentMagnet = $arrRet1 + $arrRet2;
				//$TorrentMagnet = array_merge($arrRet1,$arrRet2);
				
				krsort($TorrentMagnet);
				//reset($TorrentMagnet);
				
				echo "nb torrent = ".count($TorrentMagnet)."</br>\n";
				//echo "<pre>";print_r($TorrentMagnet);echo "</pre>";
				//echo "</br>\n";			
			}
		}
		//echo count($TorrentMagnet)."</br>\n";
		//if (count($TorrentMagnet)>0){
		//	die("");
		//}
		
		//$seedv = key($TorrentMagnet);
		//$magnetv = current($TorrentMagnet);
		//if ($magnetv=="null"){
		//	$magnetv="";
		//}
		//echo "<TR><TD>".$seedv."</TD><TD>".$magnetv. "</br>\n</TD></TR>";
		$filename=path."ThreadOut-".preg_replace("/[^a-zA-Z0-9. ]/", "",$arrayargv["Entry"]).".ThreadOut";
		//$msg =""."|".$Entry."|".$local."|".path."|".$Entry."|".$typ."|".$ns."|".$ne."|".$Exclusion."|".$ti."|".$num."|".$pod."|".$seedv."|".$magnetv."|"."</br>\n"; 
		$msg = constitutionmessage($arrayargv["ns"],$arrayargv["ne"],$arrayargv["dt"],$arrayargv["ti"],$arrayargv["num"],$arrayargv["pod"],$arrayargv["local"],$arrayargv["repSerie"],$arrayargv["Entry"],$arrayargv["typ"],$arrayargv["Exclusion"],"","","",array(""),"",$TorrentMagnet); 
		file_put_contents($filename,$msg,FILE_APPEND);
		echo "Ecriture de :".$arrayargv["Entry"].$arrayargv["ns"].$arrayargv["ne"].$arrayargv["ti"]. "</br>\n";
		//MajEntry($myEntryfile,$arrayargv[2],$arrayargv[4],$arrayargv[6],$arrayargv[7],$arrayargv[9],$arrayargv[10],$arrayargv[11],$NumeroDernierSaison,$arrayargv[12],$arrayargv[13]);
		echo "</TD></TR>";
	}
	echo "</TABLE>";
	
	echo "*"."EndSeriesReadThread"."*". "</br>\n";
	
	DelFiles(path,$EntryGlobal,"SeriesReadThread","tag");
	
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "|".str_repeat("-",016).date("Y-m-d H:i:s").str_repeat("-",016)."|". "</br>\n";  
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "*".str_repeat("*",053)."*". "</br>\n";    
	
	echo "</body></html> ";
	
	function Initialisation(){
		echo "Initialisation". "</br>\n";
		global $GV;

		$GV["repbase"] = str_replace('/',DIRECTORY_SEPARATOR , realpath( dirname(__FILE__) ). DIRECTORY_SEPARATOR);
		
		$Paramxmlbody = ChargerParam($GV["repbase"]."param.xml");
		$GV["BibliothequeSerie"]=$Paramxmlbody->getElementsByTagName('BibliothequeSerie')->item(0)->nodeValue;
		$GV["BibliothequeSerieW"]=$Paramxmlbody->getElementsByTagName('BibliothequeSerieWindows')->item(0)->nodeValue;
		
	/*	path=$GV["repbase"]."tmp".DIRECTORY_SEPARATOR."Serie".DIRECTORY_SEPARATOR;
		if (!file_exists (path)){
			SMBMap($GV["repbase"],$GV["repbase"]."tmp".DIRECTORY_SEPARATOR,$GV["BibliothequeSerie"]);
			if (!file_exists (path)){
				if (isset($Paramxmlbody->getElementsByTagName('repSerie')->item(0)->nodeValue)){
					if(file_exists ($Paramxmlbody->getElementsByTagName('repSerie')->item(0)->nodeValue)){
						path = $Paramxmlbody->getElementsByTagName('repSerie')->item(0)->nodeValue;
					}
				}
			}
		}		
		path= path;*/
		if (!file_exists (path)){die("répertoire inaccessible ". path. "</br>\n");}
		echo "repSerie=".path. "</br>\n";
		
	}			
	
	function get_magnet_kat($GV_repSerie,$Entry,$typ,$nsaison,$nepisode,$Exclusion="") {
		echo "-----get_magnet_kat-----"."</br>\n";
		$ret=array();
		$delta = 0;
		if ($typ=="Anime"){$delta = 100;}
		//$arrayEntry = preg_split("~[s,'._()0123456789 ]+~", strtolower($Entry));
		$thisnom=$Entry;
		$thisnom=preg_replace("~\(([^\[]+)\)~","",strtolower($thisnom));
		$thisnom=preg_replace("~\[([^\[]+)\]~","",strtolower($thisnom));
		$arrayEntry=preg_split("~[s,'._() ]+~",strtolower($thisnom));
		$arrayExclusion  = preg_split("~[;,'._()0123456789 ]+~",strtolower($Exclusion));
		$arrayExclusion  = preg_split("~[;,'._()0123456789 ]+~",strtolower($Exclusion));
		$seed1 = 0;
		$seed2 = 0;
		$seed3 = 0;
		$seed4 = 0;
		$magnet1="null";
		$magnet2="null";
		$magnet3="null";
		$magnet4="null";
		$taille1=0;
		$taille2=0;
		$taille3=0;
		$taille4=0;		
		
		$rch = urlencode(str_replace("S00E","",FormatNom(preg_replace("/[^a-zA-Z0-9. ]/", "",$Entry),"",$nsaison,$nepisode,"","")));
		$url = 'http://kickass.to/usearch/'.$rch.'/?field=seeders&sorder=desc';
		//$url =  urlencode($url);
		//$url = 'http://kickass.to/usearch/'.str_replace("S00E","",FormatNom($Entry,"",$nsaison,$nepisode,"","").'/?rss=1');
		echo "url = ".$url."</br>\n";
		
		$html = get_data_html($url);
		//echo "html".$html."</br>\n";
		$doc = new DOMDocument();
		@$doc->loadHTML($html);
		
		file_put_contents($GV_repSerie.$Entry.DIRECTORY_SEPARATOR.str_replace("S00E","",FormatNom($Entry,"",$nsaison,$nepisode,"",""))."-kat".".html",$html);
		
		//echo "==>"."	".$rch."-".strpos($html,"	".$rch)."<=="."</br>\n";
		
		$notfound = "Nothing found!";
		if (strpos($html,$notfound)>0 || $html=="" || !$html || strpos($html,"	".$rch)==0 ){
		//	echo "html notfound"."</br>\n";
		//	$ret[0] ="vide";
		//	$ret[0] =array("",0);
		//	$ret[1] =array("",0);
		//	$ret[2] =array("",0);
			return $ret;
		}
			
		$xpath = new DOMXPath($doc);
		$tags = $xpath->query('//table[@class="data"]//tr[@id]');
		foreach ($tags as $tag) {
			$magnetEp="";
			$NomTorrent="";
			$tailleEp="";
			$seedEp="";
			foreach($tag->childNodes as $node){
				if ($node->nodeName=="td") {
		//			if ($node->getAttribute("class") == "font12px torrentnameCell"){
						foreach($node->childNodes as $node2){
							if ($node2->nodeName=="div") {
								if ($node2->getAttribute("class") == "iaconbox floatright"){
									foreach($node2->childNodes as $node3){
										if ($node3->nodeName=="a") {
											if ($node3->getAttribute("title") == "Torrent magnet link"){
												$magnetEp = $node3->getAttribute("href");
												break;
											}
										}
									
									}
								}
								if ($node2->getAttribute("class") == "torrentname"){
//echo strtolower($node2->nodeValue)."-----".strtolower(FormatNom("","",$nsaison,$nepisode,"","")). "</br>\n" ;
									$NomTorrent = decoupenom($node2->nodeValue,$nsaison,$nepisode);
								}
								if ($magnetEp<>"" and $NomTorrent<>"") {break;}
							}
						}
		//			}
					if ($node->getAttribute("class") == "nobr center"){
						$tailleEp = $node->nodeValue;
					}
					if ($node->getAttribute("class") == "green center"){
						$seedEp = $node->nodeValue;
					}
					if ($tailleEp<>"" and $seedEp<>"" and $magnetEp<>"" and $NomTorrent<>"") {break;}
				}
			}
			
			//echo "$seedEp - $NomTorrent - $tailleEp - $magnetEp"."</br>\n";
			// controle et comptage
			if ($seedEp > 0) {
				// controle du nom du torent
				$ctrlnom = true;
				foreach ($arrayEntry as $mot){
					if (strlen ($mot)>1){
						if (!stripos(" ".$NomTorrent,$mot)){
							//echo $NomTorrent."-".$mot."</br>\n";
							$ctrlnom = false;
						}
					}
				}
				foreach ($arrayExclusion as $mot){
					if (strlen ($mot)>1){
						if (stripos(" ".$NomTorrent,$mot)){
							//echo $NomTorrent."-".$mot."</br>\n";
							$ctrlnom = false;
						}
					}
				}
				if ($ctrlnom) {
					$arrayTEp = preg_split("~ ~", $tailleEp);
					$ntailleEp = doubleval($arrayTEp[0]);
					$ftailleEp = $arrayTEp[1];
					//echo "tanet-".$ntailleEp."</br>\n";
					//echo "tafacteur-".$ftailleEp."</br>\n";
					$f=0;
					if ($ftailleEp =="MB") { $f = 1;}
					if ($ftailleEp =="GB") { $f = 1000;}
					//echo "f-".$f."</br>\n";
					$tailleEp = $f * $ntailleEp;
					//echo "tailleEp-".$tailleEp."</br>\n";
	/*				if ($tailleEp > (225 - $delta) and $tailleEp < (375 - $delta) ) { 
						//echo "1"."</br>\n";
						if ($seed1 < $seedEp/$tailleEp)  {
							$seed1 = $seedEp/$tailleEp;
							$magnet1 = $magnetEp;
							$taille1 = $tailleEp;
							//echo "1:".$seed1."-".$magnet1."</br>\n";
						}
					}
					if ($tailleEp > (600 - $delta) and $tailleEp < (850 - $delta) ) { 
						//echo "2"."</br>\n";
						if ($seed2 < $seedEp/$tailleEp)  {
							$seed2 = $seedEp/$tailleEp;
							$magnet2 = $magnetEp;
							$taille2 = $tailleEp;
							//echo "2:".$seed2."-".$magnet2."</br>\n";
						}
					}
					if ($tailleEp > (375 - $delta) and $tailleEp < (600 - $delta)) { 
						//echo "3"."</br>\n";
						if ($seed3 < $seedEp/$tailleEp)  {
							$seed3 = $seedEp/$tailleEp;
							$magnet3 = $magnetEp;
							$taille3 = $tailleEp;
							//echo "3:".$seed3."-".$magnet3."</br>\n";
						}
					}
					if ($tailleEp > (850 - $delta) ) { 
						//echo "4"."</br>\n";
						if ($seed4 < $seedEp/$tailleEp)  {
							$seed4 = $seedEp/$tailleEp;
							$magnet4 = $magnetEp;
							$taille4 = $tailleEp;
							//echo "4:".$seed4."-".$magnet4."</br>\n";
						}
					}
					*/
					$ret[round($seedEp/$tailleEp*100)+1] =$magnetEp;
				}
			}
			//echo "<TD></TD><TD>".$seedEp."</TD><TD>".$tailleEp."</br>\n</TD><TD></TD>";
		}
		//echo "Kat ".$Entry." S".$nsaison." E".$nepisode."</br>\n";
		//echo "<TD>";
		//print_r($ret);
		//echo"</TD>"."</TR>";
		//echo "<TD></TD><TD>1</TD><TD>".$seed1."</TD><TD>".$taille1."</br>\n</TD><TD></TD><TD>";
		//echo "<TD></TD><TD>2</TD><TD>".$seed2."</TD><TD>".$taille2."</br>\n</TD><TD></TD><TD>";
		//echo "<TD></TD><TD>3</TD><TD>".$seed3."</TD><TD>".$taille3."</br>\n</TD><TD></TD><TD>";
		//echo "<TD></TD><TD>4</TD><TD>".$seed4."</TD><TD>".$taille4."</br>\n</TD><TD></TD><TD>"."</TR>";
		//if ($magnet1 <> "" ){return array($magnet1,$seed1);}	
		//if ($magnet2 <> "" ){return array($magnet2,$seed2);}	
		//if ($magnet3 <> "" ){return array($magnet3,$seed3);}	
//		$ret[0] =array($magnet1,round($seed1*100)+1);
//		$ret[1] =array($magnet2,round($seed2*100)+1);
//		$ret[2] =array($magnet3,round($seed3*100)+1);
		return $ret;
		//return array("","");
	}
	
	function get_magnet_tpb($GV_repSerie,$Entry,$typ,$nsaison,$nepisode,$Exclusion="") {
		echo "-----get_magnet_tpb-----"."</br>\n";
		$ret=array();
		$delta = 0;
		if ($typ=="Anime"){$delta = 100;}
		$arrayEntry = preg_split("~[s,'._()0123456789 ]+~",strtolower($Entry));
		$arrayExclusion  = preg_split("~[;,'._()0123456789 ]+~",strtolower($Exclusion));
		$seed1 = 0;
		$seed2 = 0;
		$seed3 = 0;
		$seed4 = 0;
		$magnet1="null";
		$magnet2="null";
		$magnet3="null";
		$magnet4="null";
		$taille1=0;
		$taille2=0;
		$taille3=0;
		$taille4=0;	
		
		$url = 'http://thepiratebay.se/search/'.urlencode(str_replace("S00E","",FormatNom($Entry,"",$nsaison,$nepisode,"",""))).'/0/3/205/';
		echo "url = ".$url."</br>\n";
		
		$html = get_data_html($url);
		//echo "html".$html."</br>\n";
		$doc = new DOMDocument();
		@$doc->loadHTML($html);
		
		file_put_contents($GV_repSerie.$Entry.DIRECTORY_SEPARATOR.str_replace("S00E","",FormatNom($Entry,"",$nsaison,$nepisode,"",""))."-tpb".".html",$html);

		$notfound = "No hits. Try adding an asterisk in you search phrase.";
		if (strpos($html,$notfound)>0 || $html=="" || !$html){
		//	echo "html notfound"."</br>\n";
		//	$ret[0] =array("",0);
		//	$ret[1] =array("",0);
		//	$ret[2] =array("",0);
			return $ret;
		}
			
		$xpath = new DOMXPath($doc);
		$tags = $xpath->query('//table[@id="searchResult"]//tr');
		foreach ($tags as $tag) {
			$magnetEp="";
			$NomTorrent="";
			$tailleEp="";
			$seedEp="";
			$ntd=0;
			foreach($tag->childNodes as $node){
				//echo $node->nodeName ."*".$node->nodeValue."</br>\n";
				if ($node->nodeName=="td") {				
					$ntd +=1;
					//echo "+".$ntd."+"."</br>\n";
					if ($ntd==2){
						foreach($node->childNodes as $node2){
							//echo $node2->nodeName ."*".$node2->nodeValue."</br>\n";
							if ($node2->nodeName=="font") {
								if ($node2->getAttribute("class") == "detDesc"){
									$tailleEp = sch($node2->nodeValue,"","Size ",", ULed");
								}
							}
							if ($node2->nodeName=="div") {
								if ($node2->getAttribute("class") == "detName"){
									foreach($node2->childNodes as $node3){
										if ($node3->nodeName=="a") {
											if ($node3->getAttribute("class") == "detLink"){
												$arr = preg_split("~/~",$node3->getAttribute("href"));
												//print_r($arr);
												$NomTorrent = decoupenom($arr[3],$nsaison,$nepisode);
											}
										}
									}
								}
							}
							if ($node2->nodeName=="a") {
								if ($node2->getAttribute("title") == "Download this torrent using magnet"){
									$magnetEp = $node2->getAttribute("href");
								}
							}
						}
					}
					if ($ntd==3){
						$seedEp = $node->nodeValue;
					}
				}
			}
			
			//echo "$seedEp - $NomTorrent - $tailleEp - $magnetEp"."</br>\n";
			// controle et comptage
			if ($seedEp > 0) {
				// controle du nom du torent
				//print_r($arrayEntry);
				$ctrlnom = true;
				foreach ($arrayEntry as $mot){
					if (strlen ($mot)>1){
						//echo $NomTorrent ."-".$mot."-". stripos($NomTorrent,$mot)."</br>\n";
						if (!stripos(" ".$NomTorrent,$mot)){
							$ctrlnom = false;
						}
					}
				}
				foreach ($arrayExclusion as $mot){
					if (strlen ($mot)>1){
						if (stripos(" ".$NomTorrent,$mot)){
							$ctrlnom = false;
						}
					}
				}
				if ($ctrlnom) {
					$arrayTEp = preg_split("~ ~", str_replace("\xc2\xa0"," ",$tailleEp));
					//echo $tailleEp."</br>\n";
					//print_r($arrayTEp);
					$ntailleEp = doubleval($arrayTEp[0]);
					$ftailleEp = $arrayTEp[1];
					//echo "tanet-".$ntailleEp."</br>\n";
					//echo "tafacteur-".$ftailleEp."</br>\n";
					$f=0;
					if ($ftailleEp =="MiB") { $f = 1;}
					if ($ftailleEp =="GiB") { $f = 1000;}
					//echo "f-".$f."</br>\n";
					$tailleEp = $f * $ntailleEp;
					//echo "tailleEp-".$tailleEp."</br>\n";
		/*			if ($tailleEp > (225 - $delta) and $tailleEp < (375 - $delta) ) { 
						//echo "1"."</br>\n";
						if ($seed1 < $seedEp/$tailleEp)  {
							$seed1 = $seedEp/$tailleEp;
							$magnet1 = $magnetEp;
							$taille1 = $tailleEp;
							//echo "1:".$seed1."-".$magnet1."</br>\n";
						}
					}
					if ($tailleEp > (600 - $delta) and $tailleEp < (850 - $delta) ) { 
						//echo "2"."</br>\n";
						if ($seed2 < $seedEp/$tailleEp)  {
							$seed2 = $seedEp/$tailleEp;
							$magnet2 = $magnetEp;
							$taille2 = $tailleEp;
							//echo "2:".$seed2."-".$magnet2."</br>\n";
						}
					}
					if ($tailleEp > (375 - $delta) and $tailleEp < (600 - $delta)) { 
						//echo "3"."</br>\n";
						if ($seed3 < $seedEp/$tailleEp)  {
							$seed3 = $seedEp/$tailleEp;
							$magnet3 = $magnetEp;
							$taille3 = $tailleEp;
							//echo "3:".$seed3."-".$magnet3."</br>\n";
						}
					}
					if ($tailleEp > (850 - $delta) ) { 
						//echo "4"."</br>\n";
						if ($seed4 < $seedEp/$tailleEp)  {
							$seed4 = $seedEp/$tailleEp;
							$magnet4 = $magnetEp;
							$taille4 = $tailleEp;
							//echo "4:".$seed4."-".$magnet4."</br>\n";
						}
					}
					*/
					$ret[round($seedEp/$tailleEp*100)+1] =$magnetEp;
				}
			}
		}
		echo "Tpb ".$Entry." S".$nsaison." E".$nepisode."</br>\n";
	/*	echo "<TD></TD><TD>1</TD><TD>".$seed1."</TD><TD>".$taille1."</br>\n</TD><TD></TD><TD>";
		echo "<TD></TD><TD>2</TD><TD>".$seed2."</TD><TD>".$taille2."</br>\n</TD><TD></TD><TD>";
		echo "<TD></TD><TD>3</TD><TD>".$seed3."</TD><TD>".$taille3."</br>\n</TD><TD></TD><TD>";
		echo "<TD></TD><TD>4</TD><TD>".$seed4."</TD><TD>".$taille4."</br>\n</TD><TD></TD><TD>"."</TR>";
	*/	//if ($magnet1 <> "" ){return array($magnet1,$seed1);}	
		//if ($magnet2 <> "" ){return array($magnet2,$seed2);}	
		//if ($magnet3 <> "" ){return array($magnet3,$seed3);}	
	/*	$ret[0] =array($magnet1,round($seed1*100)+1);
		$ret[1] =array($magnet2,round($seed2*100)+1);
		$ret[2] =array($magnet3,round($seed3*100)+1);
	*/	return $ret;
		//return array("","");
	}
	
	function decoupenom($NomTorrent,$nsaison,$nepisode) {	
		$NomTorrentRet = $NomTorrent;

		$NomTorrent = sch(strtolower($NomTorrentRet),"","", strtolower(FormatNom("","",$nsaison,$nepisode,"",""))) ;
		if ($NomTorrentRet == $NomTorrent) {
			$NomTorrent = sch(strtolower($NomTorrentRet),"","", strtolower($nsaison."E".$nepisode)) ;
		}
		if ($NomTorrentRet == $NomTorrent) {
			$NomTorrent = sch(strtolower($NomTorrentRet),"","", strtolower(sprintf('%1$01d',$nsaison).$nepisode)) ;
		}
		//Si on arrive pas a decouper alors on prend pas
		if ($NomTorrentRet == $NomTorrent) {
			echo "non decoupable=".$NomTorrentRet."</br>\n";
			$NomTorrent = "" ;
		}
		return trim($NomTorrent);
	}

?>
