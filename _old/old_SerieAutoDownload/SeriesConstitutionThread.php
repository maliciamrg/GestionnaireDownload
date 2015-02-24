<?php 

/*
	$argv[2]=$Entry
*/
	const pathS="/mnt/HD/HD_b2/VideoClubSeries/Serie/";

	error_reporting(E_ERROR | E_WARNING | E_PARSE | E_NOTICE);

	echo "<!DOCTYPE html><html><body>";
	
	echo "*".str_repeat("-",053)."*". "</br>\n";              
	echo "|".str_repeat("-",053)."|". "</br>\n";              
	echo "|".str_repeat("-",016).date("Y-m-d H:i:s").str_repeat("-",016)."|". "</br>\n";   
	echo "|".str_repeat("-",053)."|". "</br>\n";        
		
	ini_set('display_errors', '1');

	sleep(rand(0, 10));

	$GV = array();
	$GV["ListAction"]="";
	$GV["nbepisodestotal"]=0;
	$GV["nbepisodespresenttotal"]=0;
	$GV["nbepisodesdisponibletotal"]=0;

	echo "*"."StartSeriesConstitutionThread"."*". "</br>\n";

	include 'Mail.php';
	include 'Commun.php';

	Initialisation($argv[1]);

	echo "*".$argv[2]."*". "</br>\n";
	
	$e="";
	$g="";
	$file =utf8_encode($argv[1].$argv[2].DIRECTORY_SEPARATOR.$argv[2].".xml");
	if(file_exists($file)){
		$urlt = simplexml_load_file($file);
		if ($urlt->Serie->epguides != ""){
			$g=(string) $urlt->Serie->epguides;
		}
		if ($urlt->Serie->Exclusion != ""){
			$e=(string) $urlt->Serie->Exclusion;
		} 
	}  
	
	$gverified = (string) get_url_serie($argv[2] , $g);
	
	Cree_fichier_base_Serie($argv[1],$argv[2],$argv[1].$argv[2].DIRECTORY_SEPARATOR,$gverified,$e);

	echo "*"."EndSeriesConstitutionThread"."*". "</br>\n";
	
	DelFiles(pathS,$argv[2],"-SeriesConstitutionThread","tag");
	
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
	
/*		$path=$GV["repbase"]."tmp".DIRECTORY_SEPARATOR."Serie".DIRECTORY_SEPARATOR;
		if (!file_exists ($path)){
			SMBMap($GV["repbase"],$GV["repbase"]."tmp".DIRECTORY_SEPARATOR,$GV["BibliothequeSerie"]);
			if (!file_exists ($path)){

				if (isset($Paramxmlbody->getElementsByTagName('repSerie')->item(0)->nodeValue)){
					if(file_exists ($Paramxmlbody->getElementsByTagName('repSerie')->item(0)->nodeValue)){
						$path = $Paramxmlbody->getElementsByTagName('repSerie')->item(0)->nodeValue;
					}
				}
			}
		}
		$path= $path;*/
		if (!file_exists ($path)){die("répertoire inaccessible ". $path. "</br>\n");}
		echo "repSerie=".$path. "</br>\n";
		
		$GV["ListAction"] ="";
	}	
		
	function get_url_serie($nom_serie , $urldansxml="") {
		//$SiteSe = get_data_html('http://kickass.to/usearch/'.$nom_serie.'/');
		//return "http://kickass.to".sch($SiteSe,'<h2>','<href="','">'); 
		$com = trim(strtolower(preg_replace("/[^a-zA-Z0-9]/", "", $nom_serie)));
		$url ="http://epguides.com/".$com.'/';
		$commthe=trim(preg_replace("/ the/",""," ".$com));
		$url2 ="http://epguides.com/".$commthe.'/';
		$url3 ="http://epguides.com/".preg_replace("/([0-9]{4})/",'_$1',$commthe).'/';
		//		$url4=$url3;
		echo $urldansxml. "</br>\n";
		if (valide_url($urldansxml)) { return $urldansxml ;}
		echo $url. "</br>\n";
		if (valide_url($url)) { return $url ;}
		echo $url2. "</br>\n";
		if (valide_url($url2)) { return $url2 ;}
		echo $url3. "</br>\n";
		if (valide_url($url3)) { return $url3 ;}
//		if (valide_url($url4)) { return $url4 ;}
		
		return $url;
		
	}
	
	function valide_url($url) {		
		$html = get_data_html($url);
		$badr = "Bad Request";
		$notfound = "The page cannot be found";
		//echo $url."-".(strpos("#".$html,$notfound)>0)."-".(strpos("#".$html,$badr)>0 ). "</br>\n";
		return ((strpos("#".$html,$notfound)>0) || (strpos("#".$html,$badr)>0 ) || $html=="" || !$html)?false:true; 
	}

	function Cree_fichier_base_Serie($path,$Entry,$local,$url,$Exclusion="") {
		echo "-----Cree_fichier_base_Serie-----"."</br>\n";
		echo "url = ".$url."</br>\n";
		global $GV;
	
		$typ = "";
		$nbepisodes = 0;
		$nbepisodespresent = 0;
		$nbepisodesdisponible=0;
		$RepDernieresaison=array();
		$NumeroDernierSaison=0;
		
		$nsp = "";

		foreach(glob("$local*.html") as $v){ 
			unlink($v);
		}
		sleep(2);
		
		//Creates XML string and XML document using the DOM 
		$myEntryfile = OpenEntry($local,$Entry);
		
		//recuperation de la structure des saison episodes
		$html = get_data_html($url);
		
		$doc = new DOMDocument();
		@$doc->loadHTML($html);
		file_put_contents($path.$Entry.DIRECTORY_SEPARATOR.$Entry.".html",$html);
		
		$badr = "Bad Request";
		$notfound = "The page cannot be found";
		if (strpos($html,$notfound)==0 && $html<>"" && $html){
			
			if (strpos($html,$badr)>0 ){
				$RepDernieresaison = $badr;
			}

			$text = trim(sch($doc->textContent,"","• ","Back toTOP"));
			$text = preg_replace("#\[Recap\]|\[Trailer\]|Season [1-9]#", "",trim($text));
			$ar = preg_split("~[ ]+|\r|\n|\t~", $text);

			//print_r ($ar);
			//echo "</br>\n";
			$tags = array();
			//$seedv = array();
			//$magnetv = "";
			$n=0;
			$i=1;
			foreach ($ar as $a){
					//if ($ns =="01" && $ne == "01") {
					//	echo "$i  $n  $a  $ti"."</br>\n";
					//}
				if (trim($a) == ""){
					if ($n > 0) {
						//echo "$ns $ne $dt $ti"."</br>\n";
						$tags[$i]["ep"]=$ep;
						$tags[$i]["ns"]=$ns;
						$tags[$i]["ne"]=$ne;
						$tags[$i]["dt"]=$dt;
						$tags[$i]["ti"]=$ti;
						$i +=1; 
						$n=0;
					}
					$ep="";
					$ns="";
					$ne="";
					$dt="";
					$ti="";
				} else {
					$n +=1;
					if ($n > 5){
						$n = 5;
						$a = " ".$a;
					}
					// controle data
					
					switch ($n) {
						case 1:
							if (abs(trim($i)-trim($a))>2){
								$n -=1;
							} else {
								$ep = trim($a);
							}
							break;
						case 2:
							$ex = preg_split("~[-]~", $a);
							$ns = sprintf('%1$02d',$ex[0]);
							$ne = sprintf('%1$02d',$ex[1]);
							break;
						case 3:
							$ex = preg_split("~[/]~", $a);
							if (count($ex) > 2 ){
								$n +=1;
							} else {
								break;
							}
						case 4:
							$exi = preg_replace("~[/]~"," ", $a);
							$dt = date('Ymd',  strtotime($exi)+86400 ); 
							break;
						case 5:
							$ti = $ti.$a;
							break;
					}
				}				
			}
			if ($n > 0) {
				//echo "$ns $ne $dt $ti"."</br>\n";
				$tags[$i]["ep"]=$ep;
				$tags[$i]["ns"]=$ns;
				$tags[$i]["ne"]=$ne;
				$tags[$i]["dt"]=$dt;
				$tags[$i]["ti"]=$ti;
			}			

			//print_r ($tags);
			$xpath = new DOMXpath($doc);
			$hrefs = $xpath->query('//div[@id="eplist"]//pre//a');

			foreach ($tags as $tag) {
					$ep =$tag["ep"];
					$ns =$tag["ns"];
					$ne =$tag["ne"];
					$ti =preg_replace("/[^a-zA-Z0-9. ]/", "",$tag["ti"]);
					$dt =$tag["dt"];
					$num = 0;
					foreach ($hrefs as $href) {	
						if( strpos("-".$tag["ti"],preg_replace("#\[Recap\]|\[Trailer\]|Season [1-9]#", "",trim($href->textContent))) > 0){
							$num = sch($href->getAttribute("href"),"","episodes/","");
							break;
						}
					}
				//if ($tag->getAttribute("title")<>""){
				//	$ep = $tag->getAttribute("title");
				//	$ns = sprintf('%1$02d',trim(sch($ep,"","season "," episode")));
				//	$ne = sprintf('%1$02d',trim(sch($ep,"","episode","")));
				//	$ti = preg_replace("/[^a-zA-Z0-9]/", "",trim($tag->textContent));
				//	$num = sch($tag->getAttribute("href"),"","episodes/","");
				// 	$seedv = array();
				//	$magnetv = "";
					echo $ep."-".$ns."-".$ne."-".$nsp."</br>\n";
					//chaque saison
					if ($ns<>$nsp){
						$RepDernieresaison[$ns]["n"] = $ns.":";
						$msg = constitutionmessage($ns,"*","","",0,0,$local,$path,$Entry,$typ,$Exclusion);							
						file_put_contents(pathS.preg_replace("/[^a-zA-Z0-9. ]/", "",$Entry)." Saison S".$ns."-Thread".".Thread",$msg,FILE_APPEND);
						echo "Ecriture de :".$Entry.$ns."*"."".  "</br>\n";
						$nsp=$ns;
					}
					//chaque episode
					if ($ne>0 and $num>0){
					
						if ($tag["dt"] <= date("Ymd")) {
							$nbepisodes++;
						}
						
						$pod=Is_Present_on_Drive($Entry,$local,$ns,$ne);
						if ($NumeroDernierSaison < $ns){$NumeroDernierSaison = $ns;}
						if ($pod=="1"){
							$nbepisodespresent++;
							$RepDernieresaison[$ns][intval($ne)]="x";
							//MajEntry($myEntryfile,$local,$Entry,$ns,$ne,$dt,$ti,$num,$pod,$NumeroDernierSaison,array(""),"");
							$msg = constitutionmessage($ns,$ne,$dt,$ti,$ep,$pod,$local,$path,$Entry,$typ,$Exclusion);	
						} else {
							$RepDernieresaison[$ns][intval($ne)]="-";
							$msg = constitutionmessage($ns,$ne,$dt,$ti,$ep,$pod,$local,$path,$Entry,$typ,$Exclusion);							
						}
						//Print_r ($msg);
						file_put_contents(pathS.preg_replace("/[^a-zA-Z0-9. ]/", "",$Entry)." Saison S".$ns."-Thread".".Thread",$msg,FILE_APPEND);
						echo "Ecriture de :".$Entry.$ns.$ne.$ti. "</br>\n";
					}
					//$ep="";
					$ep="";
					$ns="";
					//$ab="";
					$ne="";
					$ti="";
					$num="";
					$dt="";
					//$seedv="";
					//$magnetv="";
				//}
				
			}
					
		}		
		
		if (strpos($html,$badr)>0 ){echo $badr."</br>\n";}
		if (strpos($html,$notfound)>0 ){
			echo $notfound."</br>\n";
			AddFiles(pathS,"",$Entry."_notfound","error");
		}
		$msg = constitutionmessage("","","","","","","","","","","",$nbepisodes,$nbepisodespresent,$nbepisodesdisponible,$RepDernieresaison,$NumeroDernierSaison);
		file_put_contents(pathS.preg_replace("/[^a-zA-Z0-9. ]/", "",$Entry)."data".".data",$msg);
	}
	
				

?>
