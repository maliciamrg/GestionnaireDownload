<?php
/*
 * Created on 30 oct. 2013
 *
 * To change the template for this generated file go to
 * Window - Preferences - PHPeclipse - PHP - Code Templates
 */
 
 require_once( 'TransmissionRPC.class.php' );
 require_once( 'Serie.class.php' );
	
 function get_data_html($url,$post="",$NiveauxImbrication=0){
 
//  $cookie_file_path = "./cookiejar.txt"; // Please set your Cookie File path
//  $fp = fopen("$cookie_file_path","w") or die("<BR><B>Unable to open cookie file $mycookiefile for write!<BR>");
//  fclose($fp);

  try {
	  //sleep(2);
	  $ch = curl_init($url);

	  $useragent="Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.1";
	  curl_setopt($ch, CURLOPT_URL, $url);
	  curl_setopt($ch, CURLOPT_USERAGENT, $useragent);
	  //curl_setopt($ch, CURLOPT_HEADER, true);
	  curl_setopt($ch, CURLOPT_ENCODING , "");
	  curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	 // curl_setopt($ch, CURLOPT_MAXREDIRS, 5);
	  curl_setopt($ch, CURLOPT_FOLLOWLOCATION,true);
      //curl_setopt($ch, CURLOPT_POST, TRUE);
	  //curl_setopt($ch, CURLOPT_POSTFIELDS, $post);

	  curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 5);
	  curl_setopt($ch, CURLOPT_TIMEOUT, 10);
	  //curl_setopt($ch, CURLOPT_COOKIEFILE, $cookie_file_path);
	  //curl_setopt($ch, CURLOPT_COOKIEJAR, $cookie_file_path);
	  
	  $data=curl_exec($ch);
//	  if (FALSE === $data && curl_errno($ch)<>52)throw new Exception(curl_error($ch), curl_errno($ch));
	  if (FALSE === $data){
		echo "NiveauxImbrication".$NiveauxImbrication." ".curl_errno($ch)." ".curl_error($ch)."</br>\n";
		sleep(2);
		if ($NiveauxImbrication < 10 and curl_errno($ch) <> 7 ) {
			$data = get_data_html($url,$post,++$NiveauxImbrication);
		}
	  }
	  curl_close($ch);
	  return $data;
	  
  } catch(Exception $e) {
  	
  	echo sprintf('Curl failed with error #%d: %s',$e->getCode(), $e->getMessage());
  	trigger_error(sprintf(
	'Curl failed with error #%d: %s',
	$e->getCode(), $e->getMessage()),
	E_USER_ERROR);
	
  }

}
function FormatRepertoire($Repertoire,$Ns) {
 	if ($Ns=="") {
 		return $Repertoire;
 	} else {
 		return $Repertoire."Season".$Ns.DIRECTORY_SEPARATOR;
 	}
}

function FormatNom($NomSerie, $Repertoire,$Ns,$Ne,$TitreEp,$Ext='*') {
	if ($Ne=="*") {
		$SE = "";
		if ($Ns=="") {
			$SE = "";
		} else {
			$SE = "season ".intval($Ns);
		}	
	} else {
		$SE = "";
		if ($Ns=="") {
			$SE = ""."E".$Ne;
		} else {
			$SE = "S".$Ns."E".$Ne;
		}
	}
	
	if ($TitreEp=="*") {
		$ti = "";
	} else {
		if ($TitreEp!="") {
			$ti =".".$TitreEp;
		} else {
			$ti="";
		}
	}

 	if ($NomSerie !="") {
 	 $NomSerie .= ".";
 	} 
 	if ($Ext !="") {
 	 $Ext = ".".$Ext;
 	} 
	
  	return $Repertoire.$NomSerie.$SE.$ti.$Ext;
}
function SMBMap($repbase,$reptmp,$repserie) {
	return;
	echo "SMBMap in"."</br>\n";
	echo " Mount ".$repserie."</br>\n";
	echo " en ".$reptmp."</br>\n";
	echo " Retour en ".$repbase."</br>\n";
	//@chdir('/shares/NasBox/Documents/Home/Cuisine/Script Bash/');
	//echo shell_exec('ls');
	@chdir('/lib/modules/2.6.24.4/kernel/fs/cifs/');
	echo shell_exec('insmod cifs.ko'). "</br>\n";
	echo shell_exec('mount -t cifs '.$repserie.' '.$reptmp.' -o user=guest'). "</br>\n";
//	echo shell_exec('bash SerieAutoDownloadMount.sh'). "</br>\n";
    chdir ($repbase);
	echo "SMBMap out"."</br>\n";
}
function SMBRelease($reptmp) {
	return;
	echo "SMBRelease in"."</br>\n";
	echo " umount ".$reptmp."</br>\n";
    echo exec('umount '.$reptmp);
	echo "SMBRelease out"."</br>\n";
}

function print_r_xml($arr,$first=true) {
  $output = "";
  if ($first) $output .= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<data>\n";
  foreach($arr as $key => $val) {
    if (is_numeric($key)) $key = "arr_".$key; // <0 is not allowed
    switch (gettype($val)) {
      case "array":
        $output .= "<".htmlspecialchars($key)." type='array' size='".count($val)."'>".
          print_r_xml($val,false)."</".htmlspecialchars($key).">\n"; break;
      case "boolean":
        $output .= "<".htmlspecialchars($key)." type='bool'>".($val?"true":"false").
          "</".htmlspecialchars($key).">\n"; break;
      case "integer":
        $output .= "<".htmlspecialchars($key)." type='integer'>".
          htmlspecialchars($val)."</".htmlspecialchars($key).">\n"; break;
      case "double":
        $output .= "<".htmlspecialchars($key)." type='double'>".
          htmlspecialchars($val)."</".htmlspecialchars($key).">\n"; break;
      case "string":
        $output .= "<".htmlspecialchars($key)." type='string' size='".strlen($val)."'>".
          htmlspecialchars($val)."</".htmlspecialchars($key).">\n"; break;
      default:
        $output .= "<".htmlspecialchars($key)." type='unknown'>".gettype($val).
          "</".htmlspecialchars($key).">\n"; break;
    }
  }
  if ($first) $output .= "</data>\n";
  echo $output;
}
function printr ( $object , $name = '' ) {

    print ( '\'' . $name . '\' : ' ) ;

    if ( is_array ( $object ) ) {
        print ( '<pre>' )  ;
        print_r ( $object ) ;
        print ( '</pre>' ) ;
    } else {
        var_dump ( $object ) ;
    }

}

 function remove_node($myTransmissionxmlfile="" , &$node) {
     $pnode = $node->parentNode;
	 remove_children($node);
     $pnode->removeChild($node);
	 // if ($myTransmissionxmlfile <>""){
		// $myTransmissionxmlfile["Transmissionxml"]->save($myTransmissionxmlfile["myTransmissionxmlfile"]); 
	// }
 }
 
 function remove_nodebyhash($myTransmissionxmlfile , $hash) {
	$torenthash = $myTransmissionxmlfile["Transmissionxmlbody"]->getElementsByTagName('hash');
	foreach ($torenthash as $torenti) {
		if($torenti->nodeValue == $hash){
			$torentgererauto = $torenti->parentNode;
			remove_node($myTransmissionxmlfile,$torentgererauto);
			break;
		}
	}
 }
 
 function remove_children(&$node) {
     while ($node->firstChild) {
         while ($node->firstChild->firstChild) {
             remove_children($node->firstChild);
         }
 
         $node->removeChild($node->firstChild);
     }
 }
 
function open_transmission($debug=false){
	//global $GV;
	if($debug){ echo "open_transmission". "</br>\n";}
	$ret = array();
	
	for ($i = 1; $i <= 10; $i++) {

		try
		{	
			$GVrpc = new TransmissionRPC(); 
		} catch (Exception $e) {
			echo('[ERROR] ' . $e->getMessage() . PHP_EOL . "</br>\n");
			//$ret = get_data_html("http://192.168.1.100/fpkmgr/index.php?ACTION=ExecScript&ScriptFolder=Transmission&ScriptName=Configure&Params=START");
			shell_exec("/ffp/start/transmission.sh start");
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
		if($debug){ print "GET SESSION STATS... [{$result->result}]"."</br>\n";}
	
		//$arg("seed-queue-size")=10;
		$GVrpc->set( array( "seed-queue-size" => 5 ));
		$GVrpc->set( array( "seed-queue-enabled" => 0 ));
		$GVrpc->set( array( "download-queue-enabled" => 1 ));
		$GVrpc->set( array( "download_queue_size" => 10 ));
		if (test_transmission($GVrpc)){
			break;
		}

	}
	
	$ret["rpc_download_dir"] = $GVrpc->sget()->arguments->download_dir . DIRECTORY_SEPARATOR;
	$ret["SessionStat"] = $GVrpc->sstats();
	$ret["arrayridstatus"] = $GVrpc->get();
	return $ret;
}
		
function test_transmission($GVrpc,$debug=false){
	if($debug){ echo "test_transmission". "</br>\n";}	
	$relance=false;
	
	$ret=$GVrpc->ptest();
	//print_r($ret);
	if (!isset($ret->arguments->port_is_open)){
		$relance=true;
		if($debug){ echo "port_is_open="."false". "</br>\n";}
	} else {
		if($debug){ echo "port_is_open=".$ret->arguments->port_is_open. "</br>\n";}
	}
	//echo $relance;
		
	try {	
		$result = $GVrpc->sstats( );
	} catch (Exception $e) {
		echo('[ERROR] ' . $e->getMessage() . PHP_EOL . "</br>\n");
	}

	//print_r ($result);
	//die("");
/*	echo "secondsActive=".$result->arguments->current_stats->secondsActive. "</br>\n";
*/	
/*	if (!isset($ret->arguments->downloadSpeed)){
		echo "downloadSpeed="."0"."</br>\n";
	} else {
		echo "downloadSpeed=".$ret->arguments->downloadSpeed."</br>\n";
	}
*/
/*	if (!isset($ret->arguments->downloadSpeed) && isset($result->arguments->current_stats->downloadedBytes )){
		if ($result->arguments->current_stats->secondsActive > 600) {
			$relance=true;
		}
	}
*/

/*	if (!isset($result->arguments->current_stats->downloadedBytes )){
		echo "downloadBytes="."0". "</br>\n";		
	} else {
		echo "downloadBytes=".$result->arguments->current_stats->downloadedBytes. "</br>\n";
	}
*/	//print_r($result);
	
/*	if (!$relance) {
		if ($result->arguments->current_stats->secondsActive > 300 && !isset($result->arguments->current_stats->downloadedBytes)){
		//if ($result->arguments->current_stats->secondsActive > 6){
				$relance=true;
				
				$GV["pathtorrent"] = calc_pathtorrent();
				
				StockageTorrentTerminee();
				
				$ids = array();
				try{
					$arrayridstatus = $GV["rpc"]->get();

					//print_r($arrayridstatus);
					foreach ($arrayridstatus->arguments->torrents as $t) {
						$ArrayTX=GetTransmissionxmlbyHash($GV["Transmissionxml"],$t->hashString);
						if ($ArrayTX["id"]<>0){
							//$result=$GV["rpc"]->remove($t->id,true);
							//removeFromRpc($t->id,true,$t->hashString);
							echo "remove=".$t->id."/".$ArrayTX["id"]."=>".$ArrayTX["name"]." = ".$result->result."</br>\n";
						}
					}
				} catch(Exception $e){
					echo('[ERROR] ' . $e->getMessage() . PHP_EOL . "</br>\n");
				}
				sleep(2);
				//$arrayridstatus = $GV["rpc"]->get();
				//print_r($arrayridstatus);
				//die("");
		}
	}*/
	//die("");
	//echo $relance;

	
	if ($relance) {
		//$ret = get_data_html("http://192.168.1.100/fpkmgr/index.php?ACTION=ExecScript&ScriptFolder=Transmission&ScriptName=Configure&Params=STOP");
		shell_exec("/ffp/start/transmission.sh stop");
		echo 'Transmission=STOP'."</br>\n" ;
		sleep(5);
		return false;
	}
	return true;
}

function RpcRestart(){
		shell_exec("/ffp/start/transmission.sh stop");
		echo 'Transmission=STOP'."</br>\n" ;
		sleep(5);
		shell_exec("/ffp/start/transmission.sh start");
		echo 'Transmission=START'."</br>\n" ;
		sleep(5);
}
		
function removeFromRpc($key,$bool,$hashString){
	global $GV;
	
	$ArrayTX=array();
	$ArrayTX=GetTransmissionxmlbyHash($GV["Transmissionxml"],$hashString);
	if ($ArrayTX["id"]!=0){
		MarquerCommeAbsent($ArrayTX["xmlseriefile"],$ArrayTX["numero"]);

	//try {	
		$GV["rpc"]->remove($key,$bool);
	//} catch (Exception $e) {
	//	echo('[ERROR] ' . $e->getMessage() . PHP_EOL . "</br>\n");
	//	open_transmission();
	//	$GV["rpc"]->remove($key,$bool);
	//}

	MajExclusion($GV["Exclusion"],$hashString);
	}

}

function calc_pathtorrent(){ 
	global $GV;
	$pathtorrent =  str_replace('/',DIRECTORY_SEPARATOR , $GV["OpenT"]["rpc_download_dir"]. DIRECTORY_SEPARATOR);
	if (isset($GV["Paramxmlbody"]->getElementsByTagName('repTrans')->item(0)->nodeValue)){
		if(file_exists ($GV["Paramxmlbody"]->getElementsByTagName('repTrans')->item(0)->nodeValue)){
			$pathtorrent = $GV["Paramxmlbody"]->getElementsByTagName('repTrans')->item(0)->nodeValue;
		}
	}
	echo "pathtorrent=".$pathtorrent. "</br>\n";
	return $pathtorrent;
}

function ChargerParam($myParamfile){ 	
	$createdefault = false;
	$Paramxml = new DomDocument('1.0'); 
	if(file_exists($myParamfile)){
		$Paramxml->preserveWhiteSpace = false;
		$Paramxml->formatOutput = true; 
		$Paramxml->load($myParamfile);
		if (isset($Paramxml->documentElement)) {
			$Paramxmlbody = $Paramxml->documentElement;
		} else {
			$Paramxmlbody = $Paramxml->appendChild($Paramxml->createElement('body')); 
			$createdefault = true;
		}
	}else{
		$Paramxml->formatOutput = true; 
		$Paramxmlbody = $Paramxml->appendChild($Paramxml->createElement('body'));
		$createdefault = true;
	}	
	if ($createdefault) {	
		echo "Default Xml Cr?e". "</br>\n";
		$repTrans = $Paramxmlbody->appendChild($Paramxml->createElement('repTrans')); 
		$repTrans->appendChild($Paramxml->createTextNode("\\\\MYBOOKWORLD\\Public\\WWW\\TR-Downloads\\"));
		$repSerie = $Paramxmlbody->appendChild($Paramxml->createElement('repSerie')); 
		$repSerie->appendChild($Paramxml->createTextNode("\\\\DLINK-125B62\\VideoClub\\Serie\\"));
		$BibliothequeSerieWindows = $Paramxmlbody->appendChild($Paramxml->createElement('BibliothequeSerieWindows')); 
		$BibliothequeSerieWindows->appendChild($Paramxml->createTextNode("v:"));
		$BibliothequeSerie = $Paramxmlbody->appendChild($Paramxml->createElement('BibliothequeSerie')); 
		$BibliothequeSerie->appendChild($Paramxml->createTextNode("//192.168.1.104/VideoClub"));	
		$NbMaxDowxml = $Paramxmlbody->appendChild($Paramxml->createElement('NbMaxDow')); 
		$NbMaxDowxml->appendChild($Paramxml->createTextNode("5"));
		$NbMaxSeedxml = $Paramxmlbody->appendChild($Paramxml->createElement('NbMaxSeed')); 
		$NbMaxSeedxml->appendChild($Paramxml->createTextNode("10"));
		$PoorTorrentxml = $Paramxmlbody->appendChild($Paramxml->createElement('PoorTorrent')); 
		$SeederLimtxml = $PoorTorrentxml->appendChild($Paramxml->createElement('PoorSeederLimt')); 
		$SeederLimtxml->appendChild($Paramxml->createTextNode("20"));
		$PoorTorrentNbMaxDowxml = $PoorTorrentxml->appendChild($Paramxml->createElement('PoorNbMaxDow')); 
		$PoorTorrentNbMaxDowxml->appendChild($Paramxml->createTextNode("2"));
		$Paramxml->save($myParamfile); 
	}
	return $Paramxmlbody;
}

function ChargerTransmissionxml($rep,$file,$debug=false){
	if($debug){ echo "ChargerTransmissionxml". "</br>\n";}

	if($debug){ echo "rep&file=".$rep.$file. "</br>\n";}
	 
	$myTransmissionxmlfile = array();
	$myTransmissionxmlfile["myTransmissionxmlfile"] =$rep.$file;
	$myTransmissionxmlfile["Transmissionxml"]=new DomDocument('1.0');
	
	$createdefault = false;
	if(file_exists($myTransmissionxmlfile["myTransmissionxmlfile"])){
//		echo "file_exists". "</br>\n";
		$myTransmissionxmlfile["Transmissionxml"]->preserveWhiteSpace = false;
		$myTransmissionxmlfile["Transmissionxml"]->formatOutput = true; 
		$myTransmissionxmlfile["Transmissionxml"]->load($myTransmissionxmlfile["myTransmissionxmlfile"]);
		if (isset($myTransmissionxmlfile["Transmissionxml"]->documentElement)) {
			$myTransmissionxmlfile["Transmissionxmlbody"] = $myTransmissionxmlfile["Transmissionxml"]->documentElement;
		} else {
			$myTransmissionxmlfile["Transmissionxmlbody"] = $myTransmissionxmlfile["Transmissionxml"]->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('body')); 
			$createdefault = true;
		}
	}else{
		$myTransmissionxmlfile["Transmissionxml"]->formatOutput = true; 
		$myTransmissionxmlfile["Transmissionxmlbody"] = $myTransmissionxmlfile["Transmissionxml"]->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('body')); 
		$createdefault = true;
	}	
	if ($createdefault) {
		if($debug){ echo "creation Default Xml". "</br>\n";}
		$urlrep = $myTransmissionxmlfile["Transmissionxmlbody"]->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('repertoire')); 
		$urlrep->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($rep));
		$myTransmissionxmlfile["Transmissionxml"]->save($myTransmissionxmlfile["myTransmissionxmlfile"]); 
	}
	
	return $myTransmissionxmlfile;
}

Function GetTransmissionxmlbyHash($myTransmissionxmlfile,$hash){
	$torenthash = $myTransmissionxmlfile["Transmissionxmlbody"]->getElementsByTagName('hash');
	$Retour = TXvide();
	foreach ($torenthash as $torenti) {
		if($torenti->nodeValue == $hash){
			$torentgererauto = $torenti->parentNode;
			if (isset($torentgererauto->getElementsByTagName('id')->item(0)->nodeValue)) {$Retour["id"]=$torentgererauto->getElementsByTagName('id')->item(0)->nodeValue;}
			if (isset($torentgererauto->getElementsByTagName('name')->item(0)->nodeValue)) {$Retour["name"]=$torentgererauto->getElementsByTagName('name')->item(0)->nodeValue;}
			if (isset($torentgererauto->getElementsByTagName('hash')->item(0)->nodeValue)) {$Retour["hash"]=$torentgererauto->getElementsByTagName('hash')->item(0)->nodeValue;}
			if (isset($torentgererauto->getElementsByTagName('ns')->item(0)->nodeValue)) {$Retour["ns"]=$torentgererauto->getElementsByTagName('ns')->item(0)->nodeValue;}
			if (isset($torentgererauto->getElementsByTagName('Rename')->item(0)->nodeValue)) {$Retour["Rename"]=$torentgererauto->getElementsByTagName('Rename')->item(0)->nodeValue;}
			if (isset($torentgererauto->getElementsByTagName('Stockage')->item(0)->nodeValue)) {$Retour["Stockage"]=$torentgererauto->getElementsByTagName('Stockage')->item(0)->nodeValue;}
			if (isset($torentgererauto->getElementsByTagName('xmlseriefile')->item(0)->nodeValue)) {$Retour["xmlseriefile"]=$torentgererauto->getElementsByTagName('xmlseriefile')->item(0)->nodeValue;}
			if (isset($torentgererauto->getElementsByTagName('entry')->item(0)->nodeValue)) {$Retour["entry"]=$torentgererauto->getElementsByTagName('entry')->item(0)->nodeValue;}
			if (isset($torentgererauto->getElementsByTagName('numero')->item(0)->nodeValue)) {$Retour["numero"]=$torentgererauto->getElementsByTagName('numero')->item(0)->nodeValue;}
			if (isset($torentgererauto->getElementsByTagName('ActionARealiser')->item(0)->nodeValue)) {$Retour["ActionARealiser"]=$torentgererauto->getElementsByTagName('ActionARealiser')->item(0)->nodeValue;}
			break;
		}
	}
	return $Retour;
}

Function CompleteTransmissionxmlbyHash($hash){
	global $GV;
	$GV["HashTorrent"]=OpenHashTorrent($GV["ScriptPHP"]["repertoire"]["repSerie"]);
	$ret="";
	$torenthash = $GV["Transmissionxml"]["Transmissionxmlbody"]->getElementsByTagName('hash');
	$Listtorenthash = $GV["HashTorrent"]["HashTorrentbody"]->getElementsByTagName('hash');
	
	echo "--------------------"."</br>\n";
	foreach ($hash as $key => $val) {

		$Atraiter = True ;
		echo "$key == $val". "</br>\n";
		foreach ($torenthash as $torenti) {

			$a=$torenti->nodeValue;
			echo "$key == $a". "</br>\n";

			if ($key == $torenti->nodeValue) {
				$Atraiter = false ;
			}
		}
		
		echo "=========="."</br>\n";
		
		if ($Atraiter){
			foreach ($Listtorenthash as $torenti) {

				$a=$torenti->nodeValue;
				echo "$key == $a". "</br>\n";

				if ($key == $torenti->nodeValue) {
					$torentgererauto = $torenti->parentNode;
					
					$EntryETnumero=$torentgererauto->getElementsByTagName('Entry')->item(0)->nodeValue."|".$torentgererauto->getElementsByTagName('numero')->item(0)->nodeValue;
					
					$name = FormatNom(preg_replace("/[^a-zA-Z0-9. ]/", "", $torentgererauto->getElementsByTagName('Entry')->item(0)->nodeValue),"",$torentgererauto->getElementsByTagName('ns')->item(0)->nodeValue,$torentgererauto->getElementsByTagName('ne')->item(0)->nodeValue,"");
					
					echo "Ajout dans xml=".$torenti->nodeValue." aka ".$name. "</br>\n";
					
					AddTransmission($EntryETnumero,true,$val->id,$name,$key);
				}
			}
		}
	}
	//die("");
}

Function PurgeTransmissionxmlbyHash($hash){
	global $GV;
//	$ret="";
	$torenthash = $GV["Transmissionxml"]["Transmissionxmlbody"]->getElementsByTagName('hash');
	foreach ($torenthash as $torenti) {
		if(@$hash[$torenti->nodeValue] == 0){
			
			$torentgererauto = $torenti->parentNode;
			echo "purge du xml=".$torenti->nodeValue." aka ".$torentgererauto->getElementsByTagName('name')->item(0)->nodeValue. "</br>\n";
//			AddListAction($GV["ObjStat"],$torenti->nodeValue,"Purge Transmission xml",$torentgererauto->getElementsByTagName('name')->item(0)->nodeValue);
			remove_node($GV["Transmissionxml"],$torentgererauto);
			
			//$entry = $torentgererauto->getElementsByTagName('entry')->item(0)->nodeValue;
			//$fileout = $GV["repbase"]."Log".DIRECTORY_SEPARATOR."BoucleXmlSeries-".$entry.".log";
			//shell_exec("php /srv/www/pages/SerieAutoDownload/BoucleXmlSeries.php '{$entry}' >'{$fileout}' 2>'{$fileout}' &");

		}
	}
	$GV["Transmissionxml"]["Transmissionxml"]->save($GV["Transmissionxml"]["myTransmissionxmlfile"]); 
//	return $ret;
}

Function SaveTransmissionxml($myTransmissionxmlfile,$arrayTX){

	remove_nodebyhash($myTransmissionxmlfile,$arrayTX["hash"]);
	
	$down = $myTransmissionxmlfile["Transmissionxmlbody"]->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('download')); 
	$id = $down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('id')); 
	$id->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["id"]));
	$name = $down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('name')); 
	$name->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["name"]));	
	$hash = $down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('hash')); 
	$hash->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["hash"]));	
	$ns = $down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('ns')); 
	$ns->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["ns"]));	
	$Rename =$down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('Rename')); 
	$Rename->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["Rename"]));	
	$Stockage = $down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('Stockage')); 
	$Stockage->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["Stockage"]));
	$xmlseriefile = $down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('xmlseriefile')); 
	$xmlseriefile->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["xmlseriefile"]));	
	$entry = $down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('entry')); 
	$entry->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["entry"]));	
	$numero =$down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('numero')); 
	$numero->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["numero"]));	
	$date =$down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('date')); 
	$date->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["date"]));
	$ActionARealiser =$down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('ActionARealiser')); 
	$ActionARealiser->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode($arrayTX["ActionARealiser"]));
	$Transmission =$down->appendChild($myTransmissionxmlfile["Transmissionxml"]->createElement('Transmission')); 
	$Transmission->appendChild($myTransmissionxmlfile["Transmissionxml"]->createTextNode(serialize($arrayTX["Transmission"])));
	
	// $myTransmissionxmlfile["Transmissionxml"]->save($myTransmissionxmlfile["myTransmissionxmlfile"]); 
}

Function TXvide(){
	$retour = array();
	$retour["id"]=0;
	$retour["name"]="";
	$retour["hash"]="";
	$retour["ns"]="";
	$retour["Rename"]="";
	$retour["Stockage"]=0;
	$retour["xmlseriefile"]="";
	$retour["entry"]="";
	$retour["numero"]="";
	$retour["date"]="";
	$retour["ActionARealiser"]="";
	$retour["Transmission"]="";
	return $retour;
}
function isvideo($ext){
	if(strrpos("-mkvmpgmp4aviwmvdivx",$ext)>0){
		return true;
	} else {
		return false;
	}
}
function isvideoourar($ext){
	if(isvideo($ext)){
		return true;
	} else {
		if(strrpos("-rar",$ext)>0){
			return true;
		} else {
			return false;
		}
	}
}

function GetTitre($entry,$ns,$ne){
	global $GV;
	$myEntryfile=OpenEntry($GV["ScriptPHP"]["repertoire"]["repSerie"].$entry.DIRECTORY_SEPARATOR,$entry);
	$season = $myEntryfile["Entryxml"]->getElementsByTagName('season');
	foreach ($season as $seasoni) {
		if ($seasoni->getElementsByTagName('valeur')->item(0)->nodeValue == $ns){
			$episode = $seasoni->getElementsByTagName('episode');
			foreach ($episode as $episodei) {
				if ($episodei->getElementsByTagName('valeur')->item(0)->nodeValue == $ne){
					return $episodei->getElementsByTagName('titre')->item(0)->nodeValue;
				}
			}		
		}
	}
}

function deplacerresultat($tor,$src,$rena,$ArrayTX){
	global $GV;

	$retarray["status"]=false;
	$first = true;
	
	//echo "$tor,$src,$dest,$rena". "</br>\n";
	$filecount = 0;
	$filesizemin = 0;
	$filesizetot = 0;
	foreach ($tor as $f) {
		$filecount = $filecount + 1 ;
		$filesizetot= $filesizetot + $f->length;
	}	
	$filesizemin = ($filesizetot / $filecount) * 0.9;
	
	$retarray["faketorrent"] = true;	

	$namemax="";
	foreach ($tor as $f) {
		$ext = pathinfo($f->name, PATHINFO_EXTENSION);
		$rena = "";
		echo "name=".$f->name."</br>\n";
		echo "length=".$f->length."</br>\n";
		if (isvideoourar($ext)){

			$retarray["faketorrent"] = false;
			
			$namemax = $f->name;
			$flength= $f->length;	

			if ($flength > $filesizemin){
				$ne = substr(sch(basename(strtolower($namemax)),"",strtolower("S".$ArrayTX["ns"]."E"),""),0,2);
				if (!is_numeric($ne)){
					$ne = substr(sch(basename(strtolower($namemax)),"",strtolower(substr($ArrayTX["ns"]."E",1,3)),""),0,2);
					if (!is_numeric($ne)){
						$ne = substr(sch(basename(strtolower($namemax)),"",strtolower($ArrayTX["ns"]),""),0,2);
						if (!is_numeric($ne)){
							$ne = substr(sch(basename(strtolower($namemax)),"",strtolower(sprintf('%1$01d',$ArrayTX["ns"])),""),0,2);
							if (!is_numeric($ne)){
								$ne = substr(sch(basename(strtolower($namemax)),"",strtolower(sprintf('%1$01d',$ArrayTX["ns"])),""),1,2);							
							}
						}
					}
				}
				if (is_numeric($ne)){
					
					if ($first){
						$retarray["status"] = true;
						$first = false;
					}
					
					$dest = $GV["ScriptPHP"]["repertoire"]["repSerie"].FormatRepertoire($ArrayTX["entry"].DIRECTORY_SEPARATOR,$ArrayTX["ns"]);	
					if (!@mkdir($dest)) {};
					chmod($dest,0777);
					$rena = FormatNom($ArrayTX["entry"],"",$ArrayTX["ns"],$ne,GetTitre($ArrayTX["entry"],$ArrayTX["ns"],$ne),"");
					
					$s=str_replace('/',DIRECTORY_SEPARATOR ,$src.$namemax);
					$d=str_replace('/',DIRECTORY_SEPARATOR ,$dest.preg_replace("/[^a-zA-Z0-9. ]/", "", $rena).".".$ext);	

					if ($ext=="rar"){
						$repunrar =$GV["pathtorrent"]."Unrar-".rand(0, 10000).DIRECTORY_SEPARATOR;
						sleep(5);
						mkdir ($repunrar);
						@chdir($repunrar);
						// if (is_dir($repunrar)) {
							// echo "exist = $repunrar";
						// }
						// if (is_file ($s)) {
							// echo "exist = $s";
						// }
						$SpecialAction="/ffp/bin/nohup unrar e -y -ad \"$s\" \"$repunrar\" 0>/dev/null >>'".$GV["repbase"]."Log".DIRECTORY_SEPARATOR.$rena."-Unrar".".Log'";
						// print $SpecialAction."</br>\n";
						$result=`$SpecialAction`;
						chdir ($GV["repbase"]);
						
						$ret = ScanDire($repunrar,"");
				
						$ntor = array();
						$count =0 ;
						foreach ($ret as $filename) {
							$ntor[$count]->name = $filename;
							$ntor[$count]->length = filesize($filename);
							$count=$count+1;
						}
				
						$ret2 = deplacerresultat($ntor,"","",$ArrayTX);
						$retarray["status"] =  ($retarray["status"] && $ret2["status"]);
				
						deleteDir($repunrar);
						sleep(5);				
						
					} else {
				
						echo "================================"."</br>\n";
						echo "copy "."</br>\n";
						echo $s."</br>\n";
						echo $d."</br>\n";
						
						$retarray["status"] = ($retarray["status"] && copy($s,$d));

						if ($retarray["status"]){
							Creationraccourci($ArrayTX["xmlseriefile"],$ArrayTX["numero"],$d);
						}
						
					}
				}
			}
		}		
	}
	echo "retarray=>";
	print_r ($retarray);	
	echo "</br>\n";
	return $retarray;
	
}		

/*		
	echo "$filesizemin <==> $filesizetot". "</br>\n";
	echo $ArrayTX["entry"];
	
	if ($ArrayTX["entry"]=="How I Met Your Mother"){
		die("");
	}
	return false;
	
	if ($filesizemin>0 && $filesizemin > ($filesizetot*0.9) ){
		echo "$filesizemin -> $namemax ". "</br>\n";
		$ext = pathinfo($namemax, PATHINFO_EXTENSION);
		
		$s=str_replace('/',DIRECTORY_SEPARATOR ,$src.$namemax);
		//$renaclean=preg_replace("/[^a-zA-Z0-9. ]/", "", $rena);
		$d=str_replace('/',DIRECTORY_SEPARATOR ,$dest.preg_replace("/[^a-zA-Z0-9. ]/", "", $rena).".".$ext);
		
		echo "</TD><TD>"."+Deplacement ".$s."</TD><TD>".$d;
		
		if ($ext=="rar"){
				
			$s=str_replace('/',DIRECTORY_SEPARATOR ,$src.$namemax);
			
			$repunrar =$GV["pathtorrent"]."Unrar".DIRECTORY_SEPARATOR;
			if (is_dir($repunrar)) {
				deleteDir($repunrar);
			}
			sleep(5);
			mkdir ($repunrar);
			@chdir($repunrar);
			$SpecialAction="nohup unrar e -y -ad \"$s\" 0</dev/null >'".$GV["repbase"]."Log".DIRECTORY_SEPARATOR."Unrar".$rena.".Log'";
			//print $SpecialAction."</br>\n";
			$result=`$SpecialAction`;
			chdir ($GV["repbase"]);
			
			$filesizemin =0;
			$namemax="";
			
			//$dh  = opendir($repunrar);
			//while (false !== ($filename = readdir($dh))) {
			$ret = ScanDire($repunrar,"");
			
			foreach ($ret as $filename) {
				$fname=$filename;
				$flength= filesize($fname);
				echo "-".$fname . " / " . $flength."</br>\n";
				if ($flength > $filesizemin){
					$ext = pathinfo($fname, PATHINFO_EXTENSION);
					if (isvideo($ext)){
						$filesizemin=$flength;
						$namemax=$fname;
					}
				}
			}
			
			
			$s=str_replace('/',DIRECTORY_SEPARATOR ,$namemax);
			$ext = pathinfo($namemax, PATHINFO_EXTENSION);
			$d=str_replace('/',DIRECTORY_SEPARATOR ,$dest.preg_replace("/[^a-zA-Z0-9. ]/", "", $rena).".".$ext);
			echo "copy ".$s." ==> ".$d."</br>\n";
			
			//die("");
			
			$array[] = copy($s,$d);
			
			deleteDir($repunrar);
			sleep(5);
			
		} else {
		

			echo "copy ".$s." ==> ".$d."</br>\n";
			$array[] = copy($s,$d);
			
		}
		
		$array[] = $s;
		$array[] = $d;
		
		if ($array[0]){
			Creationraccourci($ArrayTX["xmlseriefile"],$ArrayTX["numero"],$array[2]);
		}
		return false;
		return $array;

	}
	return false;
}
*/
	
function sch($Str,$dep="",$deb,$fin="") {
//echo "-".$dep."-".$deb."-".$fin."-";
	if  ($dep == "" ) {
		$cSe = 0;
	} else {
		$cSe = strpos($Str,$dep,1);
	}
//	echo "*".$cSe."*";
	if  ($dep <> "" && $cSe == "") {
		return "";
	} else {
//		$cSe =$cSe + strlen($dep);
	if  ($deb == "" ) {
		$dSe = 0;
	} else {
		$dSe = strpos($Str,$deb,$cSe+1);
	}
//echo "*".$dSe."*";
		if ($dSe == "" && $dSe != "0") {
			return "";
		} else {
			$dSe = $dSe + strlen($deb);
		}
		if ($fin<>""){
			$d2Se = strpos($Str,$fin,$dSe);
		} else {
			$d2Se = "";
		}
//echo "*".$d2Se."*";
		if ($d2Se != "") {
			return substr($Str,$dSe,$d2Se-$dSe);
		} else {
			return substr($Str,$dSe);
		}

	}
}

function Miseenforme($arr) {
	$out ="<TABLE BORDER>";
	foreach ($arr as $s) {
		if ($s["n"]!="00:" || count($s)!=1) {
			$out .="<tr><td>";
			$i=0;
			$out .=$s["n"];
			unset ($s["n"]);
			do {
				$i++;
				if ($i % 100 == 0) {
					$out .= "</td></tr><tr><td>..:";
				}
				if (isset($s[$i])) {
					$out .=$s[$i];
					unset ($s[$i]);
				} else {
					$out .="-";
				}
			} while (count($s)> 0);
			$out .= "</td></tr>";
		}
	}
	$out .="</table>";//</table></body>";
	return $out;
}

function Is_Present_on_Drive($Entry,$local,$ns,$ne) {
	$file=FormatNom(preg_replace("/[^a-zA-Z0-9. ]/", "", $Entry),FormatRepertoire($local,$ns),$ns,"*".$ne."*","*");
	$list=glob($file);
	foreach ($list as $filename) {
    	$ext = pathinfo($filename, PATHINFO_EXTENSION);
    	if (isvideo($ext)) {return true;}
	}
	return false;
}

function echochildnodes($nodes,$txt) {
	foreach($nodes->childNodes as $node){
		echo "<TR><TD>";
		echo $txt."-".$node->nodeName."</TD><TD>".$node->nodeValue."</TD><TD>".$node->Attribute."</br>\n";
		echo "</TD></TR>";
		echochildnodes($node,$txt."-".$node->nodeName);
	}
}

function OpenStatistique($rep){
	$myStatistiquefile = array();
	$myStatistiquefile["myStatistiquefile"] =$rep."Statistique.xml";
	$myStatistiquefile["Statistiquexml"]=new DomDocument('1.0');
	
	$createdefault = false;
	if(file_exists($myStatistiquefile["myStatistiquefile"])){
//		echo "file_exists". "</br>\n";
		$myStatistiquefile["Statistiquexml"]->preserveWhiteSpace = false;
		$myStatistiquefile["Statistiquexml"]->formatOutput = true; 
		$myStatistiquefile["Statistiquexml"]->load($myStatistiquefile["myStatistiquefile"]);
		if (isset($myStatistiquefile["Statistiquexml"]->documentElement)) {
			$myStatistiquefile["Statistiquexmlbody"] = $myStatistiquefile["Statistiquexml"]->documentElement;
		} else {
			$myStatistiquefile["Statistiquexmlbody"] = $myStatistiquefile["Statistiquexml"]->appendChild($myStatistiquefile["Statistiquexml"]->createElement('body')); 
			$createdefault = true;
		}
	}else{
		$myStatistiquefile["Statistiquexml"]->formatOutput = true; 
		$myStatistiquefile["Statistiquexmlbody"] = $myStatistiquefile["Statistiquexml"]->appendChild($myStatistiquefile["Statistiquexml"]->createElement('body')); 
		$createdefault = true;
	}	
	if ($createdefault) {
		echo  "creation Default Xml". "</br>\n";
		$urlrep = $myStatistiquefile["Statistiquexmlbody"]->appendChild($myStatistiquefile["Statistiquexml"]->createElement('repertoire')); 
		$urlrep->appendChild($myStatistiquefile["Statistiquexml"]->createTextNode($rep));
		$myStatistiquefile["Statistiquexml"]->save($myStatistiquefile["myStatistiquefile"]); 
	}
	return $myStatistiquefile;
}

function AddStatistique($rep,$dt,$nbeppresent,$nbepabsent,$nbepencours,$nbepadown){

	$ObjStat= OpenStatistique($rep);
	
	$Statxml =  $ObjStat["Statistiquexmlbody"]->appendChild($ObjStat["Statistiquexml"]->createElement('Stat')); 
	$Datexml = $Statxml->appendChild($ObjStat["Statistiquexml"]->createElement('date')); 
	$Datexml->appendChild($ObjStat["Statistiquexml"]->createTextNode($dt));
	$nbeppresentxml = $Statxml->appendChild($ObjStat["Statistiquexml"]->createElement('nbeppresent')); 
	$nbeppresentxml->appendChild($ObjStat["Statistiquexml"]->createTextNode($nbeppresent));
	$nbepabsentxml = $Statxml->appendChild($ObjStat["Statistiquexml"]->createElement('nbepabsent')); 
	$nbepabsentxml->appendChild($ObjStat["Statistiquexml"]->createTextNode($nbepabsent));
	$nbepadownxml = $Statxml->appendChild($ObjStat["Statistiquexml"]->createElement('nbepadown')); 
	$nbepadownxml->appendChild($ObjStat["Statistiquexml"]->createTextNode($nbepadown));
	$nbepencoursxml = $Statxml->appendChild($ObjStat["Statistiquexml"]->createElement('nbepencours')); 
	$nbepencoursxml->appendChild($ObjStat["Statistiquexml"]->createTextNode($nbepencours));
	

	$ObjStat["Statistiquexml"]->save($ObjStat["myStatistiquefile"]); 

}

function VisualisationStatistique($rep){

	$ObjStat= OpenStatistique($rep);
	
	$arr=array();
	$stat = $ObjStat["Statistiquexml"]->getElementsByTagName('Stat');
	foreach ($stat as $st) {
		$dt = $st->getElementsByTagName('date')->item(0)->nodeValue;
		$dtc = preg_split("/[: -]/",$dt);
		//$dt;
		//echo date('Y-m-d H:i:s',strtotime($dtc))."-".date('Y-m-d H:i:s',strtotime("-1 month",strtotime($st->getElementsByTagName('date')->item(0)->nodeValue))). "</br>\n";
		//$dtc = date('Y-m-d H:i:s',strtotime("-1 month",strtotime($st->getElementsByTagName('date')->item(0)->nodeValue)));
		$arr[$dt][0]=$dtc[0].",".($dtc[1]-1).",".$dtc[2].",".$dtc[3].",".$dtc[4].",".$dtc[5];
		//echo $arr[$dt][0]."</br>\n";
		$arr[$dt][1]=$st->getElementsByTagName('nbeppresent')->item(0)->nodeValue;
		$arr[$dt][2]=$st->getElementsByTagName('nbepabsent')->item(0)->nodeValue;
		$arr[$dt][3]=$st->getElementsByTagName('nbepadown')->item(0)->nodeValue+$st->getElementsByTagName('nbepencours')->item(0)->nodeValue;
		$arr[$dt][4]=$st->getElementsByTagName('nbepencours')->item(0)->nodeValue;
	}
	

	//data
	$data="";
	foreach ($arr as $st) {
		if ($data!=""){$data .=",";}
		$data .= "[new Date(".$st[0]."),".$st[1].",".$st[2].",".$st[3].",".$st[4]."]";
	}


	//head
	/*		[new Date(2008, 0, 1), 11,10,10,1],
		[new Date(2008, 0, 1), 11,10,10,1],
		  [new Date(2008, 0, 1), 11,10,10,1],
		  [new Date(2008, 0, 1), 11,10,10,1],*/
	$head = '
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
	  
		var data = new google.visualization.DataTable();
		data.addColumn(\'datetime\', \'Date\');
		data.addColumn(\'number\', \'nbeppresent\');
		data.addColumn(\'number\', \'nbepabsent\');
		data.addColumn(\'number\', \'nbepadown\');
		data.addColumn(\'number\', \'nbepencours\');

		data.addRows([[new Date(2013,11,04,23,20,00),1466,297,129,0],[new Date(2013,11,04,23,27,32),1467,297,133,5],[new Date(2013,11,04,23,44,38),1469,320,316,3],[new Date(2013,11,05,02,38,03),1475,320,309,2],[new Date(2013,11,05,04,43,00),1480,320,305,3],[new Date(2013,11,05,06,47,47),1486,320,298,2],[new Date(2013,11,05,08,37,45),1491,320,294,3],[new Date(2013,11,05,10,39,36),1497,320,287,2],[new Date(2013,11,05,12,38,16),1502,320,283,3],[new Date(2013,11,05,14,37,44),1507,320,278,3],[new Date(2013,11,05,16,39,05),1513,320,271,2],[new Date(2013,11,05,18,37,47),1518,320,267,3],[new Date(2013,11,05,20,40,47),1525,320,259,2],[new Date(2013,11,05,22,37,00),1531,320,253,2],[new Date(2013,11,06,00,55,41),1534,319,249,0],[new Date(2013,11,06,02,31,37),1539,319,245,1],[new Date(2013,11,06,02,36,18),1539,319,250,6],[new Date(2013,11,06,02,38,23),1539,319,250,6],[new Date(2013,11,06,04,37,13),1544,319,239,0],[new Date(2013,11,06,06,36,04),1548,319,236,1],[new Date(2013,11,06,08,30,20),1548,319,241,6],[new Date(2013,11,06,10,34,36),1552,319,233,2],[new Date(2013,11,06,12,37,52),1557,319,228,2],[new Date(2013,11,06,14,33,15),1560,319,225,2],[new Date(2013,11,06,16,30,13),1560,319,225,2],[new Date(2013,11,06,18,30,05),1560,319,225,2],[new Date(2013,11,06,20,30,06),1560,319,225,2],[new Date(2013,11,06,20,42,43),1560,319,225,2],[new Date(2013,11,06,21,05,23),1560,319,225,2],[new Date(2013,11,06,23,10,59),1560,319,225,2],[new Date(2013,11,06,23,32,42),1560,319,225,2],[new Date(2013,11,07,00,30,00),1558,320,224,0],[new Date(2013,11,07,02,30,31),1560,320,233,11],[new Date(2013,11,07,10,55,25),1565,320,223,6],[new Date(2013,11,07,16,31,51),1571,320,216,5],[new Date(2013,11,07,18,33,32),1574,320,218,10],[new Date(2013,11,07,20,34,57),1577,320,215,10],[new Date(2013,11,07,22,33,22),1579,320,214,11],[new Date(2013,11,08,00,30,00),1579,320,216,13],[new Date(2013,11,08,02,35,11),1582,320,210,10],[new Date(2013,11,08,04,31,57),1583,320,208,9],[new Date(2013,11,08,06,30,00),1583,320,212,13],[new Date(2013,11,08,00,31,59),1584,320,210,12],[new Date(2013,11,08,10,33,52),1586,320,207,11],[new Date(2013,11,08,12,30,19),1587,320,207,12],[new Date(2013,11,08,16,36,44),1591,320,200,9],[new Date(2013,11,08,18,31,13),1592,320,202,12],[new Date(2013,11,08,20,30,00),1592,320,202,12],[new Date(2013,11,08,22,35,19),1595,322,190,5],[new Date(2013,11,09,00,30,14),1595,322,193,8],[new Date(2013,11,09,00,58,47),1597,322,192,9],[new Date(2013,11,09,01,05,35),1597,322,194,11],[new Date(2013,11,09,01,07,04),1597,322,194,11],[new Date(2013,11,09,01,07,47),1597,322,194,11],[new Date(2013,11,09,01,08,54),1597,322,194,11],[new Date(2013,11,09,01,09,34),1597,322,194,11],[new Date(2013,11,09,01,12,13),1597,322,194,11],[new Date(2013,11,09,01,16,22),1597,322,194,11],[new Date(2013,11,09,01,17,52),1597,322,194,11],[new Date(2013,11,09,01,19,05),1597,322,194,11],[new Date(2013,11,09,01,21,34),1597,322,194,11],[new Date(2013,11,09,01,30,17),1597,322,194,11],[new Date(2013,11,09,01,32,15),1597,322,194,11],[new Date(2013,11,09,01,38,13),1597,322,194,11],[new Date(2013,11,09,02,30,00),1597,322,194,11],[new Date(2013,11,09,04,35,59),1596,317,189,0],[new Date(2013,11,09,06,34,59),1599,317,188,2],[new Date(2013,11,09,08,32,58),1601,317,189,5],[new Date(2013,11,09,10,30,19),1602,317,190,7],[new Date(2013,11,09,12,30,19),1603,317,191,9],[new Date(2013,11,09,14,32,01),1605,317,188,8],[new Date(2013,11,09,16,30,21),1607,317,186,8],[new Date(2013,11,09,18,35,23),1610,317,182,7],[new Date(2013,11,09,20,30,00),1610,317,185,10],[new Date(2013,11,09,22,31,55),1611,317,183,9],[new Date(2013,11,10,02,34,41),1615,317,176,6],[new Date(2013,11,10,04,30,18),1616,317,178,9],[new Date(2013,11,10,06,33,23),1618,317,175,8],[new Date(2013,11,10,08,30,15),1618,317,177,10],[new Date(2013,11,10,10,30,07),1618,317,177,10],[new Date(2013,11,10,12,30,07),1618,317,177,10],[new Date(2013,11,10,14,32,50),1620,317,173,8],[new Date(2013,11,10,16,30,07),1620,317,175,10],[new Date(2013,11,10,18,34,48),1623,317,169,7],[new Date(2013,11,10,20,30,07),1623,317,172,10],[new Date(2013,11,10,22,30,11),1624,317,170,9],[new Date(2013,11,11,00,31,44),1625,314,169,9],[new Date(2013,11,11,01,18,57),1626,314,168,9],[new Date(2013,11,11,02,05,19),1626,314,169,10],[new Date(2013,11,11,02,30,00),1626,314,169,10],[new Date(2013,11,11,04,34,41),1629,314,163,7],[new Date(2013,11,11,06,30,07),1629,314,166,10],[new Date(2013,11,11,08,35,07),1632,314,160,7],[new Date(2013,11,11,10,30,07),1632,314,163,10],[new Date(2013,11,11,12,30,13),1632,314,163,10],[new Date(2013,11,11,14,30,07),1632,314,163,10],[new Date(2013,11,11,16,30,07),1632,314,163,10],[new Date(2013,11,11,18,31,45),1633,314,161,9],[new Date(2013,11,11,20,33,00),1635,314,159,9],[new Date(2013,11,11,22,30,15),1635,314,161,11],[new Date(2013,11,12,00,31,36),1636,314,159,10],[new Date(2013,11,12,02,30,15),1636,314,160,11],[new Date(2013,11,12,04,34,41),1630,100,42,2],[new Date(2013,11,12,06,30,13),1630,100,42,2],[new Date(2013,11,12,08,30,07),1630,100,42,2],[new Date(2013,11,12,10,30,14),1630,100,42,2],[new Date(2013,11,12,12,30,07),1630,100,42,2],[new Date(2013,11,12,14,30,07),1630,100,42,2],[new Date(2013,11,12,16,30,06),1630,100,42,2],[new Date(2013,11,12,18,31,51),1631,100,42,2],[new Date(2013,11,12,20,30,06),1631,100,42,2],[new Date(2013,11,12,23,35,16),1631,0,0,0],[new Date(2013,11,12,23,36,51),1631,0,0,0],[new Date(2013,11,12,23,42,36),1631,0,0,0],[new Date(2013,11,13,00,28,20),1631,329,147,0],[new Date(2013,11,13,00,30,10),1631,329,151,4],[new Date(2013,11,13,00,38,34),1631,329,151,4],[new Date(2013,11,13,00,41,20),1631,329,151,4],[new Date(2013,11,13,00,42,26),1631,329,151,4],[new Date(2013,11,13,02,30,00),1631,329,151,4],[new Date(2013,11,13,04,30,16),1631,329,151,4],[new Date(2013,11,13,06,35,38),1635,329,143,0],[new Date(2013,11,13,08,31,40),1636,329,145,3],[new Date(2013,11,13,10,30,22),1638,329,143,3],[new Date(2013,11,13,12,30,11),1638,329,146,6],[new Date(2013,11,13,14,30,24),1640,329,143,5],[new Date(2013,11,13,16,30,18),1641,329,144,7],[new Date(2013,11,13,18,30,00),1641,329,146,9],[new Date(2013,11,13,20,30,00),1641,329,148,11],[new Date(2013,11,13,22,30,00),1641,329,148,11],[new Date(2013,11,14,00,30,16),1641,329,148,11],[new Date(2013,11,14,02,34,07),1644,329,142,8],[new Date(2013,11,14,04,31,31),1645,329,143,10],[new Date(2013,11,14,06,30,00),1645,329,144,11],[new Date(2013,11,14,08,30,00),1645,329,144,11],[new Date(2013,11,14,10,30,00),1645,329,144,11],[new Date(2013,11,14,12,30,00),1645,329,144,11],[new Date(2013,11,14,14,30,00),1645,329,144,11],[new Date(2013,11,14,16,30,15),1645,329,144,11],[new Date(2013,11,14,18,30,00),1645,329,144,11],[new Date(2013,11,14,20,30,16),1645,329,144,11],[new Date(2013,11,14,22,37,29),1645,329,144,11],[new Date(2013,11,15,00,18,23),1645,329,144,11],[new Date(2013,11,15,00,30,00),1645,329,144,11],[new Date(2013,11,15,17,12,55),1643,331,133,0],[new Date(2013,11,15,17,13,57),1643,331,133,0],[new Date(2013,11,15,17,14,10),1643,331,134,1],[new Date(2013,11,15,17,24,36),1643,331,135,2],[new Date(2013,11,15,17,26,33),1643,331,136,3],[new Date(2013,11,15,17,31,19),1643,331,137,4],[new Date(2013,11,15,17,33,43),1643,331,137,4],[new Date(2013,11,15,17,34,54),1643,331,137,4],[new Date(2013,11,15,17,36,01),1643,331,137,4],[new Date(2013,11,15,17,42,50),1643,331,137,4],[new Date(2013,11,15,17,43,30),1643,331,137,4],[new Date(2013,11,15,17,55,17),1643,331,137,4],[new Date(2013,11,15,18,30,15),1643,331,137,4],[new Date(2013,11,15,22,30,15),1644,331,135,3],[new Date(2013,11,16,00,30,15),1644,331,137,5],[new Date(2013,11,16,02,30,19),1645,331,137,6],[new Date(2013,11,16,04,30,00),1645,331,139,8],[new Date(2013,11,16,05,30,24),1645,331,141,10],[new Date(2013,11,16,05,36,59),1645,331,143,12],[new Date(2013,11,16,05,40,36),1645,331,143,12],[new Date(2013,11,16,05,44,03),1645,331,143,12],[new Date(2013,11,16,05,45,29),1645,331,143,12],[new Date(2013,11,16,05,51,39),1645,331,143,12],[new Date(2013,11,16,06,08,23),1645,331,143,12],[new Date(2013,11,16,06,14,41),1645,331,143,12],[new Date(2013,11,16,06,18,47),1645,331,143,12],[new Date(2013,11,16,06,30,00),1645,331,143,12],[new Date(2013,11,16,06,37,47),1645,331,143,12],[new Date(2013,11,16,08,30,00),1645,331,143,12],[new Date(2013,11,16,10,30,00),1645,331,143,12],[new Date(2013,11,16,12,30,14),1645,331,143,12],[new Date(2013,11,16,14,31,46),1646,331,141,11],[new Date(2013,11,16,16,30,16),1647,331,139,10],[new Date(2013,11,16,18,30,00),1647,331,139,10],[new Date(2013,11,16,20,25,45),1647,331,139,10],[new Date(2013,11,16,20,29,16),1647,331,139,10],[new Date(2013,11,16,20,30,07),1647,331,139,10],[new Date(2013,11,16,20,32,38),1647,331,139,10],[new Date(2013,11,16,21,06,15),1647,331,139,10],[new Date(2013,11,16,21,15,53),1647,331,145,16],[new Date(2013,11,16,21,23,21),1647,331,145,16],[new Date(2013,11,16,22,31,30),1645,331,133,2],[new Date(2013,11,17,00,30,13),1645,331,138,7],[new Date(2013,11,17,02,31,53),1648,331,135,7],[new Date(2013,11,17,04,30,15),1648,331,139,11],[new Date(2013,11,17,06,30,00),1648,331,140,12],[new Date(2013,11,17,08,30,00),1648,331,140,12],[new Date(2013,11,17,10,31,42),1649,331,139,12],[new Date(2013,11,17,12,30,15),1649,331,139,12],[new Date(2013,11,17,14,30,00),1649,331,139,12],[new Date(2013,11,17,16,30,00),1649,331,139,12],[new Date(2013,11,17,18,30,33),1649,331,139,12],[new Date(2013,11,17,20,30,00),1649,331,139,12],[new Date(2013,11,17,22,30,00),1649,331,139,12],[new Date(2013,11,18,00,30,17),1649,331,139,12],[new Date(2013,11,18,02,32,14),1650,331,137,11],[new Date(2013,11,18,04,31,16),1649,330,130,0],[new Date(2013,11,18,06,31,37),1650,330,131,2],[new Date(2013,11,18,08,30,22),1651,330,132,4],[new Date(2013,11,18,10,30,22),1652,330,132,5],[new Date(2013,11,18,12,30,00),1652,330,135,8],[new Date(2013,11,18,14,30,00),1652,330,136,9],[new Date(2013,11,18,16,30,10),1652,330,136,9],[new Date(2013,11,18,18,30,00),1652,330,136,9],[new Date(2013,11,18,20,30,00),1652,330,136,9],[new Date(2013,11,18,22,30,56),1652,330,136,9],[new Date(2013,11,19,00,31,24),1653,330,135,9],[new Date(2013,11,19,02,30,15),1653,330,135,9],[new Date(2013,11,19,04,30,00),1653,330,135,9],[new Date(2013,11,19,06,30,12),1653,330,135,9],[new Date(2013,11,19,08,30,10),1653,330,135,9],[new Date(2013,11,19,10,30,16),1653,330,135,9],[new Date(2013,11,19,12,30,00),1653,330,135,9],[new Date(2013,11,19,14,30,18),1654,330,133,8],[new Date(2013,11,19,16,30,00),1654,330,133,8],[new Date(2013,11,19,18,30,00),1654,330,133,8],[new Date(2013,11,19,22,02,58),1654,330,133,8],[new Date(2013,11,19,22,18,44),1654,330,134,9],[new Date(2013,11,19,22,19,51),1654,330,135,10],[new Date(2013,11,19,22,58,53),1654,330,136,11],[new Date(2013,11,19,23,33,07),1654,330,144,19],[new Date(2013,11,19,23,34,37),1654,330,152,27],[new Date(2013,11,19,23,35,36),1654,330,152,27],[new Date(2013,11,20,00,01,28),1654,330,152,27],[new Date(2013,11,20,00,30,13),1651,319,143,4],[new Date(2013,11,20,02,33,00),1653,319,137,0],[new Date(2013,11,20,04,33,17),1656,319,139,5],[new Date(2013,11,20,06,30,24),1659,319,136,5],[new Date(2013,11,20,08,33,24),1664,319,130,4],[new Date(2013,11,20,10,33,26),1666,319,131,7],[new Date(2013,11,20,12,33,30),1668,319,129,7],[new Date(2013,11,20,14,33,22),1670,319,128,8],[new Date(2013,11,20,16,33,44),1672,319,126,8],[new Date(2013,11,20,18,30,41),1672,319,126,8],[new Date(2013,11,21,00,30,43),1672,319,126,8],[new Date(2013,11,21,00,31,16),1672,319,126,8],[new Date(2013,11,21,00,32,00),1672,319,126,8],[new Date(2013,11,21,00,37,33),1672,319,126,8],[new Date(2013,11,21,00,55,59),1672,319,126,8],[new Date(2013,11,21,02,34,35),1675,319,128,13],[new Date(2013,11,21,04,33,09),1670,322,120,0],[new Date(2013,11,21,06,30,24),1673,322,120,3],[new Date(2013,11,21,08,30,16),1674,322,121,5],[new Date(2013,11,21,10,30,15),1674,322,122,6],[new Date(2013,11,21,12,31,44),1675,322,120,5],[new Date(2013,11,21,14,30,16),1675,322,121,6],[new Date(2013,11,21,16,31,50),1676,322,120,6],[new Date(2013,11,21,16,58,08),1677,322,120,7],[new Date(2013,11,21,17,31,00),1677,322,120,7],[new Date(2013,11,21,18,05,40),1677,322,120,7],[new Date(2013,11,21,20,30,15),1673,321,118,0],[new Date(2013,11,21,23,03,57),1673,328,143,0],[new Date(2013,11,21,23,39,45),1675,328,145,4],[new Date(2013,11,22,00,33,00),1677,328,143,4],[new Date(2013,11,22,01,30,21),1677,328,145,6],[new Date(2013,11,22,02,31,47),1678,328,143,5],[new Date(2013,11,22,03,31,25),1679,328,137,0],[new Date(2013,11,22,17,41,50),1679,327,138,0],[new Date(2013,11,22,18,32,24),1681,327,140,4],[new Date(2013,11,22,19,30,52),1681,327,142,6],[new Date(2013,11,22,20,33,54),1684,327,142,9],[new Date(2013,11,22,21,32,02),1685,327,143,11],[new Date(2013,11,22,22,34,09),1688,327,143,14],[new Date(2013,11,22,23,30,47),1688,327,146,17],[new Date(2013,11,23,00,32,35),1690,327,148,21],[new Date(2013,11,23,01,32,17),1691,327,148,22],[new Date(2013,11,23,02,32,43),1693,327,150,26],[new Date(2013,11,23,03,31,43),1694,327,150,27],[new Date(2013,11,23,04,31,28),1695,327,149,27],[new Date(2013,11,23,05,34,46),1698,327,145,26],[new Date(2013,11,23,07,30,41),1698,327,147,28],[new Date(2013,11,23,08,30,17),1699,327,151,33],[new Date(2013,11,23,09,30,16),1700,327,150,33],[new Date(2013,11,23,10,30,51),1700,327,151,34],[new Date(2013,11,23,11,30,18),1701,327,155,39],[new Date(2013,11,23,12,30,41),1701,327,156,40],[new Date(2013,11,23,13,31,40),1702,327,160,45],[new Date(2013,11,23,14,30,07),1702,327,161,46],[new Date(2013,11,23,15,30,08),1702,327,161,46],[new Date(2013,11,23,16,30,06),1702,327,161,46],[new Date(2013,11,23,18,30,06),1702,327,161,46],[new Date(2013,11,23,19,30,17),1703,327,159,45],[new Date(2013,11,23,21,30,44),1703,327,160,46],[new Date(2013,11,23,22,31,45),1704,327,164,51],[new Date(2013,11,23,23,30,09),1704,327,164,51],[new Date(2013,11,24,00,30,17),1705,327,162,50],[new Date(2013,11,24,01,30,07),1705,327,163,51],[new Date(2013,11,24,02,31,50),1706,327,161,50],[new Date(2013,11,24,09,30,46),1701,327,116,0],[new Date(2013,11,24,10,30,47),1701,327,122,6],[new Date(2013,11,24,11,32,36),1703,327,124,10],[new Date(2013,11,24,12,31,22),1705,327,122,10],[new Date(2013,11,24,13,30,13),1706,327,122,11],[new Date(2013,11,24,14,30,52),1706,327,123,12],[new Date(2013,11,24,15,31,40),1708,327,125,16],[new Date(2013,11,24,16,30,20),1710,327,123,16],[new Date(2013,11,24,17,30,56),1710,327,125,18],[new Date(2013,11,24,18,30,45),1710,327,131,24],[new Date(2013,11,24,19,30,07),1710,327,135,28],[new Date(2013,11,24,20,30,07),1710,327,135,28],[new Date(2013,11,24,21,30,38),1710,327,135,28],[new Date(2013,11,24,22,30,40),1710,327,139,32],[new Date(2013,11,24,23,30,38),1710,327,143,36],[new Date(2013,11,25,00,30,06),1710,327,147,40],[new Date(2013,11,25,01,30,13),1710,327,147,40],[new Date(2013,11,25,02,30,13),1710,327,147,40],[new Date(2013,11,25,04,30,45),1710,327,147,40],[new Date(2013,11,25,05,31,36),1711,327,149,43],[new Date(2013,11,25,06,30,08),1711,327,150,44],[new Date(2013,11,25,07,30,40),1711,327,150,44],[new Date(2013,11,25,08,30,38),1711,327,154,48],[new Date(2013,11,25,09,30,40),1711,327,158,52],[new Date(2013,11,25,10,30,22),1712,327,160,55],[new Date(2013,11,25,11,30,07),1712,327,161,56],[new Date(2013,11,25,12,30,13),1712,327,161,56],[new Date(2013,11,25,13,30,07),1712,327,161,56],[new Date(2013,11,25,14,30,07),1712,327,161,56],[new Date(2013,11,25,15,30,45),1712,327,161,56],[new Date(2013,11,25,16,30,37),1712,327,165,60],[new Date(2013,11,25,17,30,38),1712,327,169,64],[new Date(2013,11,25,18,30,06),1712,327,173,68],[new Date(2013,11,25,19,30,39),1712,327,173,68],[new Date(2013,11,25,20,30,37),1712,327,177,72],[new Date(2013,11,25,21,30,38),1712,327,181,76],[new Date(2013,11,25,22,30,42),1712,327,185,80],[new Date(2013,11,25,23,30,38),1712,327,189,84],[new Date(2013,11,26,00,30,42),1712,327,193,88],[new Date(2013,11,26,00,41,48),1712,327,197,92],[new Date(2013,11,26,01,30,07),1712,327,201,96],[new Date(2013,11,26,02,30,46),1712,327,201,96],[new Date(2013,11,26,03,30,11),1706,327,136,23],[new Date(2013,11,26,04,30,26),1706,327,115,2],[new Date(2013,11,26,05,30,21),1706,327,115,2],[new Date(2013,11,26,08,30,48),1706,327,115,2],[new Date(2013,11,26,09,32,57),1708,327,121,10],[new Date(2013,11,26,10,31,37),1709,327,121,11],[new Date(2013,11,27,09,32,27),1711,327,119,11],[new Date(2013,11,27,10,31,49),1711,327,119,11],[new Date(2013,11,27,11,30,11),1711,327,129,21],[new Date(2013,11,27,12,30,48),1711,327,129,21],[new Date(2013,11,27,13,30,18),1712,327,137,30],[new Date(2013,11,27,14,30,08),1712,327,137,30],[new Date(2013,11,27,15,31,51),1714,327,133,28],[new Date(2013,11,27,16,31,00),1714,327,136,31],[new Date(2013,11,27,17,31,39),1715,327,143,39],[new Date(2013,11,27,19,30,53),1715,327,143,39],[new Date(2013,11,27,19,56,34),1715,327,151,47],[new Date(2013,11,27,20,31,27),1716,327,157,54],[new Date(2013,11,27,21,30,09),1716,327,159,56],[new Date(2013,11,27,22,30,08),1716,327,159,56],[new Date(2013,11,27,23,30,07),1716,327,159,56],[new Date(2013,11,28,00,31,26),1717,327,157,55],[new Date(2013,11,28,01,30,46),1717,327,157,55],[new Date(2013,11,28,02,30,38),1717,327,165,63],[new Date(2013,11,28,04,30,14),1715,332,102,0],[new Date(2013,11,28,05,30,09),1715,332,105,3],[new Date(2013,11,28,06,30,49),1715,332,105,3],[new Date(2013,11,28,07,30,19),1717,332,111,11],[new Date(2013,11,28,08,30,20),1719,332,109,11],[new Date(2013,11,28,10,30,18),1720,332,109,12],[new Date(2013,11,28,11,30,21),1721,332,108,12],[new Date(2013,11,28,14,36,13),1721,332,109,13],[new Date(2013,11,28,15,30,47),1721,332,118,22],[new Date(2013,11,28,16,30,14),1721,332,126,30],[new Date(2013,11,28,17,30,06),1721,332,126,30],[new Date(2013,11,28,18,30,13),1721,332,126,30],[new Date(2013,11,28,19,30,07),1721,332,126,30],[new Date(2013,11,28,20,30,47),1721,332,126,30],[new Date(2013,11,28,21,30,17),1722,332,132,37],[new Date(2013,11,28,22,30,14),1722,332,133,38],[new Date(2013,11,28,23,30,08),1722,332,133,38],[new Date(2013,11,29,00,30,08),1722,332,146,38],[new Date(2013,11,29,01,32,31),1724,332,144,38],[new Date(2013,11,29,02,32,59),1726,332,142,38],[new Date(2013,11,29,03,30,27),1726,332,144,40],[new Date(2013,11,29,04,31,24),1727,332,142,39],[new Date(2013,11,29,05,30,53),1727,332,142,39],[new Date(2013,11,29,06,33,14),1729,332,148,47],[new Date(2013,11,29,07,33,07),1731,332,146,47],[new Date(2013,11,29,08,30,21),1731,332,149,50],[new Date(2013,11,29,09,30,54),1731,332,149,50],[new Date(2013,11,29,10,20,12),1731,414,382,58],[new Date(2013,11,29,10,30,08),1731,414,384,60],[new Date(2013,11,29,11,31,39),1732,414,382,59],[new Date(2013,11,29,13,31,54),1734,414,379,58],[new Date(2013,11,29,14,32,59),1736,414,377,58],[new Date(2013,11,29,16,30,16),1738,414,375,58],[new Date(2013,11,29,18,30,17),1740,414,373,58],[new Date(2013,11,29,19,33,16),1742,414,371,58],[new Date(2013,11,29,20,30,52),1742,414,373,60],[new Date(2013,11,29,21,31,34),1743,414,381,69],[new Date(2013,11,29,22,36,48),1748,414,371,64],[new Date(2013,11,29,23,31,27),1749,414,374,68],[new Date(2013,11,30,00,31,49),1750,414,372,67],[new Date(2013,11,30,02,32,36),1752,414,370,67],[new Date(2013,11,30,03,32,20),1746,413,326,16],[new Date(2013,11,30,04,31,47),1747,413,309,0],[new Date(2013,11,30,07,30,10),1749,413,308,1],[new Date(2013,11,30,08,30,48),1749,413,310,3],[new Date(2013,11,30,09,34,38),1752,413,314,10],[new Date(2013,11,30,10,30,58),1752,413,316,12],[new Date(2013,11,30,11,30,52),1752,413,326,22],[new Date(2013,11,30,12,30,58),1752,413,336,32],[new Date(2013,11,30,13,32,09),1754,413,342,40],[new Date(2013,11,30,14,33,37),1756,413,340,40],[new Date(2013,11,30,17,30,58),1758,413,338,40],[new Date(2013,11,30,18,30,08),1758,413,348,50],[new Date(2013,11,30,19,31,49),1759,413,346,49],[new Date(2013,11,30,20,32,03),1759,413,346,49],[new Date(2013,11,30,21,30,24),1759,413,356,59],[new Date(2013,11,30,23,31,35),1760,413,354,58],[new Date(2013,11,31,00,30,19),1761,413,352,57],[new Date(2013,11,31,02,30,17),1761,413,354,59],[new Date(2013,11,31,03,33,19),1763,413,352,59],[new Date(2013,11,31,04,33,14),1765,413,350,59],[new Date(2013,11,31,05,32,01),1766,413,350,60],[new Date(2013,11,31,06,30,11),1766,413,351,61],[new Date(2013,11,31,09,32,36),1767,413,349,60],[new Date(2013,11,31,11,30,09),1769,413,355,68],[new Date(2013,11,31,12,31,57),1770,413,355,69],[new Date(2013,11,31,13,31,57),1771,413,354,69],[new Date(2013,11,31,14,30,59),1771,413,355,70],[new Date(2013,11,31,15,34,18),1774,413,359,77],[new Date(2013,11,31,16,35,09),1777,413,356,77],[new Date(2013,11,31,17,30,59),1777,413,359,80],[new Date(2013,11,31,18,31,55),1779,413,365,88],[new Date(2013,11,31,19,31,54),1780,413,364,88],[new Date(2013,11,31,20,30,09),1780,413,366,90],[new Date(2013,11,31,21,30,09),1780,413,366,90],[new Date(2013,11,31,22,30,57),1780,413,366,90],[new Date(2013,11,31,23,30,08),1780,413,376,100],[new Date(2014,0,01,00,31,27),1781,413,374,99],[new Date(2014,0,01,01,30,09),1781,413,375,100],[new Date(2014,0,01,03,31,23),1782,413,373,99],[new Date(2014,0,01,06,32,41),1784,413,370,98],[new Date(2014,0,01,07,30,14),1784,413,372,100],[new Date(2014,0,01,08,30,08),1784,413,372,100],[new Date(2014,0,01,09,30,09),1784,413,372,100],[new Date(2014,0,01,10,33,05),1786,413,368,98],[new Date(2014,0,01,11,30,16),1786,413,370,100],[new Date(2014,0,01,12,31,23),1787,413,368,99],[new Date(2014,0,01,13,30,16),1787,413,369,100],[new Date(2014,0,01,14,31,51),1788,413,367,99],[new Date(2014,0,01,15,30,16),1788,413,368,100],[new Date(2014,0,01,16,31,27),1789,413,366,99],[new Date(2014,0,01,18,31,51),1790,413,365,99],[new Date(2014,0,01,19,31,52),1791,413,364,99],[new Date(2014,0,01,20,30,15),1791,413,365,100],[new Date(2014,0,01,22,31,37),1792,413,363,99],[new Date(2014,0,01,23,30,10),1792,413,364,100],[new Date(2014,0,02,00,30,16),1792,413,364,100],[new Date(2014,0,02,01,31,40),1793,413,362,99],[new Date(2014,0,02,02,32,00),1794,413,361,99],[new Date(2014,0,02,03,30,13),1792,415,333,71],[new Date(2014,0,02,04,32,01),1794,416,270,10],[new Date(2014,0,02,05,31,44),1795,416,260,1],[new Date(2014,0,02,06,30,16),1795,416,261,2],[new Date(2014,0,02,07,31,37),1796,416,259,1],[new Date(2014,0,02,08,30,16),1796,416,260,2],[new Date(2014,0,02,09,31,44),1797,416,258,1],[new Date(2014,0,02,10,31,31),1798,415,259,2],[new Date(2014,0,02,11,30,16),1798,415,260,3],[new Date(2014,0,02,12,30,10),1798,415,260,3],[new Date(2014,0,02,13,33,24),1800,415,256,1],[new Date(2014,0,02,14,30,10),1800,415,258,3],[new Date(2014,0,02,16,31,34),1801,415,256,2],[new Date(2014,0,02,17,31,18),1801,415,257,3],[new Date(2014,0,02,18,30,09),1801,415,267,13],[new Date(2014,0,02,19,30,09),1801,415,267,13],[new Date(2014,0,02,20,15,45),1801,415,266,12],[new Date(2014,0,02,20,30,13),1801,415,272,18],[new Date(2014,0,02,21,31,53),1802,415,270,17],[new Date(2014,0,02,23,34,55),1805,415,264,14],[new Date(2014,0,03,00,30,12),1805,415,268,18],[new Date(2014,0,03,02,31,04),1805,415,268,18],[new Date(2014,0,03,03,31,36),1806,415,274,25],[new Date(2014,0,03,04,30,16),1806,415,275,26],[new Date(2014,0,03,05,30,09),1806,415,275,26],[new Date(2014,0,03,07,31,48),1807,415,273,25],[new Date(2014,0,03,08,34,33),1807,415,274,26],[new Date(2014,0,03,08,36,10),1807,415,284,36],[new Date(2014,0,03,09,32,40),1809,415,280,34],[new Date(2014,0,03,10,30,16),1809,415,280,34],[new Date(2014,0,03,12,34,13),1812,415,277,34],[new Date(2014,0,03,13,30,29),1812,415,287,44],[new Date(2014,0,03,14,33,26),1814,415,283,42],[new Date(2014,0,03,16,30,53),1814,415,283,42],[new Date(2014,0,03,17,31,47),1815,415,291,51],[new Date(2014,0,03,20,36,58),1815,415,290,50],[new Date(2014,0,03,21,00,16),1815,415,292,52],[new Date(2014,0,03,21,02,30),1815,415,292,52],[new Date(2014,0,03,21,06,06),1815,415,292,52],[new Date(2014,0,03,21,34,45),1819,415,284,48],[new Date(2014,0,03,21,37,24),1819,415,286,50],[new Date(2014,0,03,21,58,44),1819,415,286,50],[new Date(2014,0,03,22,00,43),1819,415,287,51],[new Date(2014,0,03,22,31,46),1820,415,285,50],[new Date(2014,0,03,23,33,05),1822,415,282,49],[new Date(2014,0,04,00,33,04),1824,415,280,49],[new Date(2014,0,04,01,12,12),1825,415,280,50],[new Date(2014,0,04,01,32,17),1826,415,278,49],[new Date(2014,0,04,02,30,10),1826,415,287,58],[new Date(2014,0,04,03,30,16),1827,416,264,37],[new Date(2014,0,04,04,30,15),1827,415,257,29],[new Date(2014,0,04,05,30,16),1827,418,226,0],[new Date(2014,0,04,06,34,37),1831,418,222,0],[new Date(2014,0,04,07,31,52),1832,418,222,1],[new Date(2014,0,04,08,30,16),1832,418,224,3],[new Date(2014,0,04,09,30,09),1832,418,225,4],[new Date(2014,0,04,10,30,15),1832,418,226,5],[new Date(2014,0,04,11,31,48),1833,418,225,5],[new Date(2014,0,04,13,32,18),1834,418,225,6],[new Date(2014,0,04,14,31,25),1835,418,223,5],[new Date(2014,0,04,15,30,17),1835,418,225,7],[new Date(2014,0,04,16,30,09),1835,418,225,7],[new Date(2014,0,04,17,34,33),1838,418,220,5],[new Date(2014,0,04,17,41,16),1838,418,224,9],[new Date(2014,0,04,17,53,41),1839,418,222,8],[new Date(2014,0,04,18,30,11),1839,418,223,9],[new Date(2014,0,04,19,31,50),1840,418,221,8],[new Date(2014,0,04,20,30,16),1840,418,222,9],[new Date(2014,0,04,21,32,39),1841,418,220,8],[new Date(2014,0,04,22,31,40),1843,418,226,16],[new Date(2014,0,04,23,32,49),1845,418,223,15],[new Date(2014,0,05,00,32,34),1847,418,222,16],[new Date(2014,0,05,01,32,43),1848,418,222,17],[new Date(2014,0,05,02,30,40),1848,418,223,18],[new Date(2014,0,05,03,35,34),1849,418,221,17],[new Date(2014,0,05,04,36,09),1851,418,227,25],[new Date(2014,0,05,05,37,23),1854,418,224,25],[new Date(2014,0,05,06,30,35),1854,418,224,25],[new Date(2014,0,05,07,34,00),1855,418,224,26],[new Date(2014,0,05,09,34,08),1857,418,221,25],[new Date(2014,0,05,10,30,46),1857,418,223,27],[new Date(2014,0,05,11,31,05),1857,418,223,27],[new Date(2014,0,05,15,47,28),1861,418,215,23],[new Date(2014,0,05,16,30,45),1861,418,225,33],[new Date(2014,0,05,17,30,19),1862,418,223,32],[new Date(2014,0,05,18,30,20),1863,418,222,32],[new Date(2014,0,05,19,31,59),1865,418,219,31],[new Date(2014,0,05,21,30,57),1866,418,219,32],[new Date(2014,0,05,22,33,09),1868,418,220,35],[new Date(2014,0,05,23,30,15),1868,418,220,35],[new Date(2014,0,06,00,30,13),1868,418,220,35],[new Date(2014,0,06,01,31,41),1869,418,218,34],[new Date(2014,0,06,02,30,10),1869,418,221,37],[new Date(2014,0,06,03,32,43),1863,418,217,27],[new Date(2014,0,06,04,30,20),1862,424,202,9],[new Date(2014,0,06,05,30,24),1862,424,202,9],[new Date(2014,0,06,06,30,25),1862,424,202,9],[new Date(2014,0,06,07,31,55),1863,424,200,8],[new Date(2014,0,06,08,30,31),1863,424,200,8],[new Date(2014,0,06,09,30,30),1863,424,200,8],[new Date(2014,0,06,10,30,22),1863,424,200,8],[new Date(2014,0,06,11,30,20),1863,424,200,8],[new Date(2014,0,06,12,30,32),1863,424,200,8],[new Date(2014,0,06,13,30,26),1863,424,200,8],[new Date(2014,0,06,14,30,26),1863,424,200,8],[new Date(2014,0,06,15,30,26),1863,424,200,8],[new Date(2014,0,06,16,30,11),1863,424,200,8],[new Date(2014,0,06,17,30,10),1863,424,200,8],[new Date(2014,0,06,18,30,18),1864,424,198,7],[new Date(2014,0,06,19,31,24),1865,424,197,7],[new Date(2014,0,06,20,31,50),1866,424,197,8],[new Date(2014,0,06,22,30,20),1867,424,195,7],[new Date(2014,0,07,21,30,22),1867,424,195,7],[new Date(2014,0,07,22,30,11),1867,424,195,7],[new Date(2014,0,08,00,30,51),1867,424,195,7],[new Date(2014,0,08,01,32,48),1869,424,201,15],[new Date(2014,0,08,02,31,48),1870,424,200,15],[new Date(2014,0,08,03,30,14),1868,423,198,10],[new Date(2014,0,08,04,31,24),1833,423,232,9],[new Date(2014,0,08,05,33,15),1817,441,231,10],[new Date(2014,0,08,06,30,22),1817,441,231,10],[new Date(2014,0,08,07,30,15),1817,441,231,10],[new Date(2014,0,08,09,30,20),1793,441,254,9],[new Date(2014,0,08,10,32,36),1794,440,253,8],[new Date(2014,0,08,17,30,27),1795,440,261,17],[new Date(2014,0,08,18,32,50),1797,440,267,25],[new Date(2014,0,08,19,30,48),1797,440,269,27],[new Date(2014,0,08,20,30,17),1871,423,194,7],[new Date(2014,0,08,21,31,43),1824,384,182,0],[new Date(2014,0,08,22,30,46),1824,384,183,1],[new Date(2014,0,08,23,30,08),1824,384,191,9],[new Date(2014,0,09,00,30,10),1825,384,190,9],[new Date(2014,0,09,01,32,20),1826,384,189,9],[new Date(2014,0,09,02,32,19),1828,384,193,15],[new Date(2014,0,09,03,31,38),1829,384,199,22],[new Date(2014,0,09,04,30,18),1830,384,197,21],[new Date(2014,0,09,07,32,42),1832,384,195,21],[new Date(2014,0,09,08,30,14),1833,384,195,22],[new Date(2014,0,09,09,30,15),1833,384,196,23],[new Date(2014,0,09,10,32,20),1834,384,194,22],[new Date(2014,0,09,11,30,18),1835,384,200,29],[new Date(2014,0,09,12,31,41),1836,384,198,28],[new Date(2014,0,09,13,31,15),1837,384,198,29],[new Date(2014,0,09,14,32,23),1838,384,197,29],[new Date(2014,0,09,15,30,18),1839,384,203,36],[new Date(2014,0,09,16,31,39),1840,384,201,35],[new Date(2014,0,09,17,31,41),1841,384,201,36],[new Date(2014,0,09,18,30,09),1841,384,202,37],[new Date(2014,0,09,19,33,09),1843,384,198,35],[new Date(2014,0,09,20,30,17),1844,384,198,36],[new Date(2014,0,09,21,15,37),1844,384,198,36],[new Date(2014,0,09,21,30,09),1844,384,206,44],[new Date(2014,0,09,22,33,11),1847,384,200,41],[new Date(2014,0,10,00,30,49),1849,384,199,42],[new Date(2014,0,10,01,33,12),1851,384,203,48],[new Date(2014,0,10,02,33,11),1853,384,201,48],[new Date(2014,0,10,03,32,56),1846,382,162,0],[new Date(2014,0,10,04,34,10),1849,382,159,0],[new Date(2014,0,10,05,31,45),1850,382,160,2],[new Date(2014,0,10,06,32,31),1852,382,160,4],[new Date(2014,0,10,08,30,47),1853,382,158,3],[new Date(2014,0,10,09,30,20),1855,382,162,9],[new Date(2014,0,10,10,31,38),1856,382,162,10],[new Date(2014,0,10,12,31,39),1857,382,161,10],[new Date(2014,0,10,19,30,24),1857,382,162,11],[new Date(2014,0,10,21,32,19),1854,386,151,0],[new Date(2014,0,10,23,30,50),1855,386,150,0],[new Date(2014,0,10,23,42,47),1855,386,158,8],[new Date(2014,0,11,03,46,32),1857,386,162,14],[new Date(2014,0,11,04,30,54),1856,381,154,0],[new Date(2014,0,11,05,31,00),1856,381,154,0],[new Date(2014,0,11,06,32,19),1856,381,154,0],[new Date(2014,0,11,07,30,42),1856,381,162,8],[new Date(2014,0,11,08,31,45),1856,381,170,16],[new Date(2014,0,11,09,31,35),1856,381,170,16],[new Date(2014,0,11,10,31,29),1856,381,170,16],[new Date(2014,0,11,11,32,16),1856,381,170,16],[new Date(2014,0,11,12,29,40),1858,381,174,22],[new Date(2014,0,11,12,30,08),1858,381,176,24],[new Date(2014,0,11,13,00,11),1859,381,174,23],[new Date(2014,0,12,01,53,38),1907,419,156,0],[new Date(2014,0,12,02,31,18),1908,418,156,0],[new Date(2014,0,12,03,30,57),1908,417,157,0],[new Date(2014,0,12,03,55,52),1908,417,158,1],[new Date(2014,0,12,04,04,00),1909,417,157,1],[new Date(2014,0,12,04,30,10),1909,417,160,4],[new Date(2014,0,12,04,36,49),1909,417,163,7],[new Date(2014,0,12,05,35,54),1912,417,159,6],[new Date(2014,0,12,06,30,10),1912,417,162,9],[new Date(2014,0,12,07,32,28),1913,417,160,8],[new Date(2014,0,12,08,30,50),1913,417,168,16],[new Date(2014,0,12,09,31,42),1914,417,174,23],[new Date(2014,0,12,11,30,11),1916,417,171,22],[new Date(2014,0,12,12,30,16),1916,417,178,29],[new Date(2014,0,12,13,30,12),1916,417,179,30],[new Date(2014,0,12,23,22,23),1917,417,177,29],[new Date(2014,0,12,23,30,10),1917,417,184,36],[new Date(2014,0,13,00,33,00),1919,417,180,34],[new Date(2014,0,13,01,30,09),1919,417,183,37],[new Date(2014,0,13,02,30,16),1919,417,183,37],[new Date(2014,0,13,03,30,07),1919,417,183,37],[new Date(2014,0,13,04,30,08),1919,417,183,37],[new Date(2014,0,13,06,30,09),1919,417,183,37],[new Date(2014,0,13,07,30,06),1919,417,183,37],[new Date(2014,0,13,22,06,24),1919,417,183,37],[new Date(2014,0,13,22,30,10),1919,417,191,45],[new Date(2014,0,13,23,34,03),1920,421,179,33],[new Date(2014,0,14,00,31,16),1921,421,178,33],[new Date(2014,0,14,01,31,32),1922,421,178,34],[new Date(2014,0,14,02,30,15),1922,421,179,35],[new Date(2014,0,14,03,32,33),1923,424,140,0],[new Date(2014,0,14,04,34,06),1925,424,138,0],[new Date(2014,0,14,05,30,18),1925,424,140,2],[new Date(2014,0,14,06,34,32),1927,424,139,3],[new Date(2014,0,14,07,30,16),1927,424,147,11],[new Date(2014,0,14,08,31,52),1928,424,145,10],[new Date(2014,0,14,10,30,08),1929,424,151,17],[new Date(2014,0,14,11,30,08),1929,424,152,18],[new Date(2014,0,14,12,30,08),1929,424,152,18],[new Date(2014,0,14,13,30,50),1929,424,152,18],[new Date(2014,0,14,14,31,15),1930,424,158,25],[new Date(2014,0,14,15,31,55),1931,424,157,25],[new Date(2014,0,14,16,30,19),1931,424,158,26],[new Date(2014,0,14,17,30,08),1931,424,158,26],[new Date(2014,0,14,18,30,16),1931,424,158,26],[new Date(2014,0,14,19,32,22),1932,424,156,25],[new Date(2014,0,14,20,30,14),1932,424,164,33],[new Date(2014,0,14,21,32,11),1933,424,162,32],[new Date(2014,0,14,22,31,33),1934,424,168,39],[new Date(2014,0,14,23,30,10),1934,424,168,39],[new Date(2014,0,15,00,31,42),1935,424,167,39],[new Date(2014,0,15,01,30,09),1935,424,168,40],[new Date(2014,0,15,02,30,07),1935,424,168,40],[new Date(2014,0,15,03,30,14),1935,424,168,40],[new Date(2014,0,15,04,30,08),1935,424,168,40],[new Date(2014,0,15,05,30,08),1935,424,168,40],[new Date(2014,0,15,08,30,08),1935,424,168,40],[new Date(2014,0,15,09,31,35),1936,424,166,39],[new Date(2014,0,15,10,30,59),1937,424,165,39],[new Date(2014,0,15,11,30,14),1937,424,166,40],[new Date(2014,0,15,12,30,53),1937,424,166,40],[new Date(2014,0,15,13,32,25),1938,424,172,47],[new Date(2014,0,15,14,33,43),1939,424,170,46],[new Date(2014,0,15,16,27,48),1939,424,178,54],[new Date(2014,0,15,16,30,08),1939,424,178,54],[new Date(2014,0,15,17,32,25),1939,423,126,0],[new Date(2014,0,15,18,32,48),1941,423,130,6],[new Date(2014,0,15,20,31,50),1944,423,132,11],[new Date(2014,0,15,21,55,15),1944,423,135,14],[new Date(2014,0,15,22,30,08),1944,425,140,21],[new Date(2014,0,15,23,30,13),1945,425,139,21],[new Date(2014,0,16,00,30,08),1945,425,139,21],[new Date(2014,0,16,01,30,45),1945,425,140,22],[new Date(2014,0,16,02,30,08),1945,425,148,30],[new Date(2014,0,16,03,30,09),1945,421,123,0],[new Date(2014,0,16,04,30,38),1945,421,123,0],[new Date(2014,0,16,05,30,07),1945,421,123,0],[new Date(2014,0,16,06,30,08),1945,421,123,0],[new Date(2014,0,16,07,30,08),1945,421,123,0],[new Date(2014,0,16,08,30,07),1945,421,123,0],[new Date(2014,0,16,09,30,16),1945,421,123,0],[new Date(2014,0,16,10,31,44),1946,421,122,0],[new Date(2014,0,16,11,30,45),1946,421,122,0],[new Date(2014,0,16,12,32,27),1947,421,128,7],[new Date(2014,0,16,13,30,14),1947,421,136,15],[new Date(2014,0,16,14,31,13),1948,421,136,16],[new Date(2014,0,16,16,30,58),1948,421,137,17],[new Date(2014,0,16,17,30,52),1948,421,137,17],[new Date(2014,0,16,18,30,08),1948,421,145,25],[new Date(2014,0,16,19,30,14),1948,421,147,27],[new Date(2014,0,16,20,30,08),1948,421,147,27],[new Date(2014,0,16,21,30,54),1948,421,147,27],[new Date(2014,0,16,22,30,47),1948,421,155,35],[new Date(2014,0,16,23,30,08),1948,421,163,43],[new Date(2014,0,17,00,30,09),1948,421,163,43],[new Date(2014,0,17,01,30,50),1948,421,163,43],[new Date(2014,0,17,02,30,09),1948,421,171,51],[new Date(2014,0,17,03,31,38),1949,421,169,50],[new Date(2014,0,17,07,31,38),1950,421,167,49],[new Date(2014,0,17,08,30,09),1950,421,167,49],[new Date(2014,0,17,09,30,51),1950,421,167,49],[new Date(2014,0,17,10,30,07),1950,421,173,55],[new Date(2014,0,17,11,30,07),1950,421,173,55],[new Date(2014,0,17,12,30,41),1950,421,173,55],[new Date(2014,0,17,13,30,10),1950,421,179,61],[new Date(2014,0,17,14,30,58),1951,421,177,60],[new Date(2014,0,17,15,31,22),1952,421,176,60],[new Date(2014,0,17,17,30,14),1952,421,177,61],[new Date(2014,0,17,18,30,43),1952,421,177,61],[new Date(2014,0,17,19,30,46),1952,421,183,67],[new Date(2014,0,17,20,32,39),1953,421,187,72],[new Date(2014,0,17,21,30,46),1953,421,193,78],[new Date(2014,0,17,22,30,07),1953,421,199,84],[new Date(2014,0,17,23,30,53),1953,421,199,84],[new Date(2014,0,18,00,30,48),1953,421,205,90],[new Date(2014,0,18,01,30,15),1953,421,211,96],[new Date(2014,0,18,02,30,07),1953,421,211,96],[new Date(2014,0,18,03,30,49),1953,421,120,0],[new Date(2014,0,18,04,33,47),1955,421,124,6],[new Date(2014,0,18,05,31,20),1956,421,130,13],[new Date(2014,0,18,06,30,50),1956,421,133,16],[new Date(2014,0,18,07,30,09),1956,421,141,24],[new Date(2014,0,18,08,30,45),1956,421,141,24],[new Date(2014,0,18,09,30,08),1621,327,146,31],[new Date(2014,0,18,10,30,47),1956,421,150,33],[new Date(2014,0,18,11,30,42),1956,421,158,41],[new Date(2014,0,18,12,31,45),1957,421,164,48],[new Date(2014,0,18,13,30,15),1957,421,164,48],[new Date(2014,0,18,14,30,08),1957,421,164,48],[new Date(2014,0,18,15,30,07),1957,421,164,48],[new Date(2014,0,18,16,30,07),1957,421,164,48],[new Date(2014,0,18,17,30,07),1957,421,164,48],[new Date(2014,0,18,18,30,09),1957,421,164,48],[new Date(2014,0,18,19,30,07),1957,421,164,48],[new Date(2014,0,18,20,30,09),1957,421,164,48],[new Date(2014,0,18,21,30,09),1957,421,164,48],[new Date(2014,0,18,23,31,34),1958,421,162,47],[new Date(2014,0,19,00,30,14),1958,421,168,53],[new Date(2014,0,19,01,30,46),1958,421,168,53],[new Date(2014,0,19,02,30,42),1958,421,174,59],[new Date(2014,0,19,03,30,07),1958,421,180,65],[new Date(2014,0,19,04,30,07),1958,421,180,65],[new Date(2014,0,19,05,30,08),1958,421,180,65],[new Date(2014,0,19,09,30,41),1958,421,180,65],[new Date(2014,0,19,10,30,43),1958,421,181,66],[new Date(2014,0,19,11,30,51),1958,421,187,72],[new Date(2014,0,19,12,30,40),1958,421,193,78],[new Date(2014,0,19,13,30,41),1958,421,199,84],[new Date(2014,0,19,14,30,51),1958,421,205,90],[new Date(2014,0,19,15,30,14),1958,421,211,96],[new Date(2014,0,19,16,30,09),1958,421,211,96],[new Date(2014,0,19,18,33,03),1959,421,209,95],[new Date(2014,0,19,19,30,15),1959,421,215,101],[new Date(2014,0,19,20,30,43),1959,421,215,101],[new Date(2014,0,19,21,30,49),1959,421,221,107],[new Date(2014,0,19,22,30,08),1959,421,221,107],[new Date(2014,0,19,22,45,08),1959,421,221,107],[new Date(2014,0,19,23,30,08),1959,424,256,107],[new Date(2014,0,20,00,32,07),1961,424,254,107],[new Date(2014,0,20,01,32,22),1963,424,252,107],[new Date(2014,0,20,02,31,13),1964,424,252,108],[new Date(2014,0,20,03,31,16),1965,427,141,0],[new Date(2014,0,20,04,30,48),1965,427,142,1],[new Date(2014,0,20,05,33,31),1967,427,146,7],[new Date(2014,0,20,06,33,06),1969,427,150,13],[new Date(2014,0,20,07,33,21),1971,427,148,13],[new Date(2014,0,20,08,32,35),1973,427,146,13],[new Date(2014,0,20,09,31,24),1974,427,146,14],[new Date(2014,0,20,10,32,04),1975,427,144,13],[new Date(2014,0,20,11,32,06),1976,427,143,13],[new Date(2014,0,20,12,33,09),1978,427,147,19],[new Date(2014,0,20,13,30,09),1978,427,147,19],[new Date(2014,0,20,14,32,55),1980,427,145,19],[new Date(2014,0,20,15,31,51),1981,427,151,26],[new Date(2014,0,20,16,32,49),1983,427,148,25],[new Date(2014,0,20,17,32,33),1985,427,146,25],[new Date(2014,0,20,18,33,05),1987,427,144,25],[new Date(2014,0,20,19,30,21),1987,427,146,27],[new Date(2014,0,20,20,32,52),1989,427,142,25],[new Date(2014,0,20,21,30,10),1989,427,142,25],[new Date(2014,0,20,22,31,47),1990,427,142,26],[new Date(2014,0,20,22,52,04),1990,427,143,27],[new Date(2014,0,20,22,53,40),1990,427,143,27],[new Date(2014,0,20,22,54,59),1990,427,143,27],[new Date(2014,0,20,23,31,47),1991,427,141,26],[new Date(2014,0,21,00,30,11),1991,427,143,28],[new Date(2014,0,21,01,33,09),1992,427,143,29],[new Date(2014,0,21,02,30,45),1992,427,151,37],[new Date(2014,0,21,03,31,39),1993,427,157,44],[new Date(2014,0,21,04,30,15),1993,427,157,44],[new Date(2014,0,21,05,30,08),1993,427,159,46],[new Date(2014,0,21,06,30,08),1993,427,159,46],[new Date(2014,0,21,07,30,10),1993,427,159,46],[new Date(2014,0,21,09,30,52),1993,427,159,46],[new Date(2014,0,21,10,30,09),1993,427,167,54],[new Date(2014,0,21,11,30,08),1993,427,167,54],[new Date(2014,0,21,12,30,08),1993,427,167,54],[new Date(2014,0,21,13,30,44),1993,427,167,54],[new Date(2014,0,21,14,30,43),1993,427,174,61],[new Date(2014,0,21,15,30,08),1993,427,180,67],[new Date(2014,0,21,16,30,50),1993,427,180,67],[new Date(2014,0,21,17,30,49),1993,427,186,73],[new Date(2014,0,21,18,30,08),1993,427,192,79],[new Date(2014,0,21,19,30,44),1993,427,192,79],[new Date(2014,0,21,20,30,44),1993,427,198,85],[new Date(2014,0,21,21,30,15),1993,427,204,91],[new Date(2014,0,21,22,30,08),1993,427,204,91],[new Date(2014,0,21,23,30,44),1993,427,204,91],[new Date(2014,0,22,00,30,43),1993,427,210,97],[new Date(2014,0,22,01,30,39),1993,503,353,103],[new Date(2014,0,22,02,30,17),1993,503,361,111],[new Date(2014,0,22,03,34,51),1997,506,248,0],[new Date(2014,0,22,04,33,50),1999,506,248,2],[new Date(2014,0,22,05,30,17),1999,506,256,10],[new Date(2014,0,22,06,30,49),1999,506,256,10],[new Date(2014,0,22,07,31,46),2000,506,262,17],[new Date(2014,0,22,08,33,10),2002,506,259,16],[new Date(2014,0,22,09,30,18),2002,506,261,18],[new Date(2014,0,22,10,32,20),2003,506,260,18],[new Date(2014,0,22,11,30,16),2003,506,268,26],[new Date(2014,0,22,12,30,10),2003,506,269,27],[new Date(2014,0,22,13,31,46),2004,506,267,26],[new Date(2014,0,22,14,33,09),2006,506,264,25],[new Date(2014,0,22,15,31,51),2007,506,264,26],[new Date(2014,0,22,18,30,09),2007,506,265,27],[new Date(2014,0,22,19,32,49),2008,506,263,26],[new Date(2014,0,22,20,31,37),2009,506,269,33],[new Date(2014,0,22,21,30,42),2009,575,191,24],[new Date(2014,0,22,22,31,41),2010,574,191,24],[new Date(2014,0,22,23,30,11),2010,574,192,25],[new Date(2014,0,23,00,30,11),2010,574,192,25],[new Date(2014,0,23,01,30,10),2010,574,192,25],[new Date(2014,0,23,02,30,43),2010,574,192,25],[new Date(2014,0,23,03,32,38),2011,574,190,24],[new Date(2014,0,23,04,30,11),2011,574,198,32],[new Date(2014,0,23,05,30,16),2011,574,198,32],[new Date(2014,0,23,06,30,09),2011,574,198,32],[new Date(2014,0,23,07,30,09),2011,574,198,32],[new Date(2014,0,23,09,30,49),2011,574,198,32],[new Date(2014,0,23,10,31,43),2012,574,204,39],[new Date(2014,0,23,11,30,10),2012,574,205,40],[new Date(2014,0,23,12,30,08),2012,574,205,40],[new Date(2014,0,23,13,31,46),2013,574,203,39],[new Date(2014,0,27,13,39,08),2028,491,217,1],[new Date(2014,0,27,14,00,18),2028,530,223,0],[new Date(2014,0,27,14,35,39),2030,530,221,0],[new Date(2014,0,27,14,35,58),2030,530,221,2],[new Date(2014,0,27,14,40,54),2030,529,222,3],[new Date(2014,0,27,15,32,08),2031,514,236,1],[new Date(2014,0,27,16,22,39),2032,444,305,0],[new Date(2014,0,27,16,30,10),2032,444,305,0],[new Date(2014,0,27,17,30,45),2032,444,305,0],[new Date(2014,0,27,18,30,13),2032,444,305,7],[new Date(2014,0,27,22,30,52),2034,444,303,6],[new Date(2014,0,27,22,33,55),2034,444,303,13],[new Date(2014,0,27,23,30,12),2034,444,303,13],[new Date(2014,0,28,00,37,23),2036,444,301,11],[new Date(2014,0,28,01,30,55),2037,444,300,13],[new Date(2014,0,28,01,55,10),2038,444,299,12],[new Date(2014,0,28,02,32,51),2039,444,298,12],[new Date(2014,0,28,03,30,13),2039,449,294,0],[new Date(2014,0,28,04,30,48),2039,449,294,0],[new Date(2014,0,28,05,34,32),2040,449,293,0],[new Date(2014,0,28,06,30,16),2040,449,293,7],[new Date(2014,0,28,07,30,41),2040,449,293,8],[new Date(2014,0,28,08,30,10),2040,449,293,8],[new Date(2014,0,28,09,30,11),2040,449,293,8],[new Date(2014,0,28,10,34,04),2041,449,292,7],[new Date(2014,0,28,11,30,18),2041,449,292,8],[new Date(2014,0,28,13,30,54),2041,449,292,8],[new Date(2014,0,28,14,36,26),2043,449,290,13],[new Date(2014,0,28,15,30,13),2043,449,290,15],[new Date(2014,0,28,16,35,48),2045,449,288,13],[new Date(2014,0,28,17,33,24),2046,449,287,19],[new Date(2014,0,28,18,33,16),2047,449,286,20],[new Date(2014,0,28,19,33,13),2048,449,285,19],[new Date(2014,0,28,20,31,28),2049,449,284,18],[new Date(2014,0,28,21,33,06),2050,449,283,17],[new Date(2014,0,28,22,37,19),2053,449,280,16],[new Date(2014,0,28,23,31,16),2054,449,279,23],[new Date(2014,0,29,00,33,54),2055,449,278,23],[new Date(2014,0,29,01,30,20),2055,449,278,24],[new Date(2014,0,29,02,30,12),2055,449,278,24],[new Date(2014,0,29,03,34,26),2056,449,277,23],[new Date(2014,0,29,04,30,29),2056,449,277,31],[new Date(2014,0,29,05,33,10),2057,449,276,30],[new Date(2014,0,29,06,31,00),2058,449,275,29],[new Date(2014,0,29,07,30,49),2058,449,275,29],[new Date(2014,0,29,08,30,10),2058,449,275,37],[new Date(2014,0,29,09,33,33),2059,449,274,36],[new Date(2014,0,29,12,32,49),2060,449,273,36],[new Date(2014,0,29,13,34,05),2061,449,272,43],[new Date(2014,0,29,14,30,18),2061,449,272,51],[new Date(2014,0,29,15,32,18),2063,449,270,49],[new Date(2014,0,29,16,30,38),2063,449,270,57],[new Date(2014,0,29,17,30,47),2063,449,270,65],[new Date(2014,0,29,18,30,16),2063,449,270,65],[new Date(2014,0,29,19,30,14),2063,449,270,65],[new Date(2014,0,29,20,30,50),2064,449,269,65],[new Date(2014,0,29,21,30,50),2064,449,269,73],[new Date(2014,0,29,22,31,37),2065,449,268,72],[new Date(2014,0,29,23,33,25),2066,449,267,79],[new Date(2014,0,30,00,30,18),2066,449,267,80],[new Date(2014,0,30,01,30,10),2066,449,267,80],[new Date(2014,0,30,02,30,18),2066,449,267,80],[new Date(2014,0,30,03,32,32),2066,435,282,0],[new Date(2014,0,30,04,32,35),2067,435,281,0],[new Date(2014,0,30,05,30,25),2067,435,281,8],[new Date(2014,0,30,06,31,31),2068,435,280,9],[new Date(2014,0,30,07,30,18),2068,435,280,9],[new Date(2014,0,30,08,33,14),2069,435,279,8],[new Date(2014,0,30,09,30,52),2069,435,279,8],[new Date(2014,0,30,10,30,16),2069,435,279,16],[new Date(2014,0,30,11,30,14),2069,435,279,16],[new Date(2014,0,30,12,34,04),2070,435,278,15],[new Date(2014,0,30,13,31,17),2071,435,277,22],[new Date(2014,0,30,14,30,24),2071,435,277,22],[new Date(2014,0,30,15,34,42),2073,435,275,22],[new Date(2014,0,30,16,33,31),2074,435,274,21],[new Date(2014,0,30,20,30,13),2074,435,274,22],[new Date(2014,0,30,21,33,06),2075,435,273,22],[new Date(2014,0,30,22,30,19),2075,435,273,23],[new Date(2014,0,30,23,33,10),2076,435,272,22],[new Date(2014,0,31,00,04,36),2076,435,272,23],[new Date(2014,0,31,00,11,01),2076,435,272,23],[new Date(2014,0,31,00,18,05),2077,435,271,22],[new Date(2014,0,31,00,29,47),2077,435,271,24],[new Date(2014,0,31,00,30,13),2077,435,271,24],[new Date(2014,0,31,00,30,47),2077,435,271,24],[new Date(2014,0,31,00,33,06),2078,435,270,23],[new Date(2014,0,31,00,37,05),2078,435,270,24],[new Date(2014,0,31,00,43,42),2078,435,270,24],[new Date(2014,0,31,00,44,40),2078,435,270,24],[new Date(2014,0,31,01,04,40),2078,435,270,24],[new Date(2014,0,31,01,30,44),2078,435,270,24],[new Date(2014,0,31,02,30,12),2078,435,270,32],[new Date(2014,0,31,03,30,20),2078,435,270,33],[new Date(2014,0,31,05,34,27),2080,435,268,31],[new Date(2014,0,31,06,31,44),2081,435,267,33],[new Date(2014,0,31,07,30,18),2081,435,267,33],[new Date(2014,0,31,08,33,15),2082,435,266,32],[new Date(2014,0,31,09,30,16),2082,435,266,33],[new Date(2014,0,31,10,30,11),2082,435,266,33],[new Date(2014,0,31,13,30,10),2082,435,266,33],[new Date(2014,0,31,14,30,11),2082,435,266,33],[new Date(2014,0,31,17,38,07),2084,435,264,31],[new Date(2014,0,31,18,30,17),2084,435,264,39],[new Date(2014,0,31,19,30,09),2084,435,264,39],[new Date(2014,0,31,20,30,10),2084,435,264,39],[new Date(2014,0,31,21,30,42),2084,435,264,39],[new Date(2014,0,31,22,32,39),2085,435,263,46],[new Date(2014,0,31,23,33,20),2087,435,261,52],[new Date(2014,1,01,00,31,50),2088,435,260,53],[new Date(2014,1,01,01,31,40),2089,435,259,53],[new Date(2014,1,01,02,36,54),2090,435,258,53],[new Date(2014,1,01,02,37,38),2090,435,258,61],[new Date(2014,1,01,03,30,52),2090,435,258,61],[new Date(2014,1,01,04,30,14),2090,435,258,61],[new Date(2014,1,01,05,30,39),2090,435,258,61],[new Date(2014,1,01,06,30,39),2090,435,258,61],[new Date(2014,1,01,07,30,10),2090,435,258,61],[new Date(2014,1,01,08,31,38),2091,435,257,62],[new Date(2014,1,01,09,30,57),2092,435,256,62],[new Date(2014,1,01,10,33,41),2094,435,254,61],[new Date(2014,1,01,11,30,11),2094,435,254,61],[new Date(2014,1,01,12,30,11),2094,435,254,63],[new Date(2014,1,01,13,30,12),2094,435,254,63],[new Date(2014,1,01,15,30,12),2094,435,254,63],[new Date(2014,1,01,17,13,58),2095,435,253,62],[new Date(2014,1,01,17,18,17),2095,435,253,63],[new Date(2014,1,01,17,21,37),2095,435,253,63],[new Date(2014,1,01,17,23,19),2095,435,253,63],[new Date(2014,1,01,17,30,09),2095,435,253,63],[new Date(2014,1,01,18,30,12),2095,435,253,63],[new Date(2014,1,01,19,30,59),2096,435,252,64],[new Date(2014,1,01,20,31,50),2097,435,251,64],[new Date(2014,1,01,22,58,31),2098,435,250,64],[new Date(2014,1,01,23,02,55),2098,435,250,65],[new Date(2014,1,01,23,32,07),2099,435,249,64],[new Date(2014,1,01,23,40,30),2099,435,249,65],[new Date(2014,1,01,23,44,01),2099,435,249,65],[new Date(2014,1,01,23,50,15),2099,435,249,65],[new Date(2014,1,02,00,10,00),2099,435,249,65],[new Date(2014,1,02,00,37,53),2100,435,248,64],[new Date(2014,1,02,00,49,19),2101,435,247,64],[new Date(2014,1,02,02,03,04),2104,435,244,62],[new Date(2014,1,02,02,31,56),2105,435,243,71],[new Date(2014,1,02,03,31,22),2108,453,236,0],[new Date(2014,1,02,04,33,30),2110,453,234,0],[new Date(2014,1,02,05,31,55),2111,453,233,9],[new Date(2014,1,02,06,30,49),2111,453,233,9],[new Date(2014,1,02,07,30,41),2111,453,233,9],[new Date(2014,1,02,08,30,43),2111,453,233,9],[new Date(2014,1,02,09,30,41),2111,453,233,9],[new Date(2014,1,02,10,30,43),2111,453,233,9],[new Date(2014,1,02,11,30,37),2111,453,233,9],[new Date(2014,1,02,13,31,01),2111,453,233,9],[new Date(2014,1,02,14,31,01),2111,453,233,19],[new Date(2014,1,02,16,31,28),2112,453,232,28],[new Date(2014,1,02,17,29,42),2112,455,233,0],[new Date(2014,1,02,17,30,30),2112,455,233,0],[new Date(2014,1,02,18,30,29),2112,455,233,0],[new Date(2014,1,02,19,30,30),2112,455,233,0],[new Date(2014,1,02,20,30,37),2112,455,233,0],[new Date(2014,1,02,21,30,47),2112,455,233,0],[new Date(2014,1,02,22,30,34),2112,455,233,0],[new Date(2014,1,02,23,30,50),2112,455,233,0],[new Date(2014,1,03,00,31,09),2112,455,233,0],[new Date(2014,1,03,01,28,40),2112,455,233,10],[new Date(2014,1,03,01,36,03),2112,455,233,10],[new Date(2014,1,03,01,58,01),2112,455,233,10],[new Date(2014,1,03,01,58,58),2112,455,233,11],[new Date(2014,1,03,04,06,10),2112,455,233,11],[new Date(2014,1,03,04,11,16),2112,455,233,21],[new Date(2014,1,03,04,31,43),2112,455,233,21],[new Date(2014,1,03,05,31,48),2112,455,233,21],[new Date(2014,1,03,06,31,46),2112,455,233,22],[new Date(2014,1,03,07,31,46),2112,455,233,22],[new Date(2014,1,03,09,31,07),2112,455,233,22],[new Date(2014,1,03,10,28,24),2112,455,233,32],[new Date(2014,1,03,10,30,34),2112,455,233,42],[new Date(2014,1,03,11,08,30),2106,452,242,39],[new Date(2014,1,03,11,23,26),2106,452,242,39],[new Date(2014,1,03,11,33,57),2107,452,241,38],[new Date(2014,1,03,11,36,12),2107,452,241,40],[new Date(2014,1,03,11,41,22),2107,452,241,40],[new Date(2014,1,03,11,45,08),2107,452,241,40],[new Date(2014,1,03,12,31,06),2107,452,241,40],[new Date(2014,1,03,16,33,53),2111,452,237,45],[new Date(2014,1,03,19,31,08),2114,452,234,52],[new Date(2014,1,03,20,30,36),2114,452,234,62],[new Date(2014,1,03,21,30,29),2114,452,234,62],[new Date(2014,1,04,02,30,32),2131,440,247,62],[new Date(2014,1,04,03,32,08),2136,429,254,0],[new Date(2014,1,04,04,30,36),2136,429,254,1],[new Date(2014,1,04,05,30,28),2136,429,254,1],[new Date(2014,1,04,06,30,29),2136,429,254,1],[new Date(2014,1,04,07,30,35),2136,429,254,1],[new Date(2014,1,04,08,31,03),2136,429,254,1],[new Date(2014,1,04,10,31,10),2137,429,253,10],[new Date(2014,1,04,12,31,09),2138,429,252,19],[new Date(2014,1,04,13,30,34),2138,429,252,29],[new Date(2014,1,04,14,30,28),2138,429,252,29],[new Date(2014,1,04,15,30,28),2138,429,252,29],[new Date(2014,1,04,16,30,29),2138,429,252,29],[new Date(2014,1,04,17,30,29),2138,429,252,29],[new Date(2014,1,04,18,30,29),2138,429,252,29],[new Date(2014,1,04,19,30,30),2138,429,252,29],[new Date(2014,1,04,20,30,41),2138,429,252,29],[new Date(2014,1,04,21,32,06),2139,429,251,28],[new Date(2014,1,04,22,30,36),2139,429,251,29],[new Date(2014,1,04,23,30,29),2139,429,251,29],[new Date(2014,1,05,00,31,06),2139,429,251,29],[new Date(2014,1,05,01,31,05),2139,429,251,39],[new Date(2014,1,05,02,30,58),2139,429,251,39],[new Date(2014,1,05,03,30,57),2139,429,251,39],[new Date(2014,1,05,05,31,44),2139,429,251,39],[new Date(2014,1,05,06,31,05),2139,429,251,39],[new Date(2014,1,05,09,37,55),2140,429,250,38],[new Date(2014,1,05,10,37,49),2141,429,249,46],[new Date(2014,1,05,11,32,54),2141,429,249,56],[new Date(2014,1,05,13,34,10),2143,429,247,64],[new Date(2014,1,05,15,30,39),2145,429,245,72],[new Date(2014,1,05,16,33,29),2146,429,244,73],[new Date(2014,1,05,20,30,04),2148,429,242,73],[new Date(2014,1,05,20,30,47),2148,429,242,83],[new Date(2014,1,05,20,49,46),2159,418,242,83],[new Date(2014,1,05,21,30,37),2159,426,239,0],[new Date(2014,1,05,22,30,42),2159,426,239,0],[new Date(2014,1,05,23,34,15),2160,426,238,0],[new Date(2014,1,06,01,30,41),2162,426,236,7],[new Date(2014,1,06,02,34,36),2163,426,235,8],[new Date(2014,1,06,03,31,02),2161,417,246,0],[new Date(2014,1,06,06,31,12),2163,417,244,7],[new Date(2014,1,06,07,31,02),2163,417,244,16],[new Date(2014,1,06,22,08,51),2164,417,243,24],[new Date(2014,1,06,22,31,02),2164,417,243,33],[new Date(2014,1,06,22,59,09),2164,417,243,42],[new Date(2014,1,06,23,30,31),2164,417,243,42],[new Date(2014,1,07,00,30,32),2164,417,243,42],[new Date(2014,1,07,01,30,34),2164,417,243,42],[new Date(2014,1,07,02,31,02),2164,417,243,42],[new Date(2014,1,07,03,30,37),2164,417,243,51],[new Date(2014,1,07,04,30,37),2164,417,243,51],[new Date(2014,1,07,05,30,31),2164,417,243,51],[new Date(2014,1,07,08,30,38),2164,417,243,51],[new Date(2014,1,07,09,30,38),2164,417,243,51],[new Date(2014,1,07,10,30,47),2164,417,243,51],[new Date(2014,1,07,11,30,48),2164,417,243,51],[new Date(2014,1,07,12,30,34),2164,417,243,51],[new Date(2014,1,07,13,31,11),2164,417,243,51],[new Date(2014,1,07,14,30,51),2164,417,243,60],[new Date(2014,1,07,15,30,41),2164,417,243,60],[new Date(2014,1,07,18,56,02),2165,417,242,59],[new Date(2014,1,07,19,30,34),2165,417,242,60],[new Date(2014,1,07,20,30,39),2165,417,242,60],[new Date(2014,1,07,21,30,39),2165,417,242,60],[new Date(2014,1,07,22,34,01),2166,417,241,59],[new Date(2014,1,07,23,31,10),2166,417,241,60],[new Date(2014,1,08,00,30,39),2166,417,241,69],[new Date(2014,1,08,01,30,39),2166,417,241,69],[new Date(2014,1,08,02,07,34),2166,417,241,69],[new Date(2014,1,08,02,30,34),2167,417,240,68],[new Date(2014,1,08,03,30,36),2173,411,240,68],[new Date(2014,1,08,04,30,41),2170,429,241,0],[new Date(2014,1,08,05,30,40),2170,429,241,0],[new Date(2014,1,08,10,30,39),2171,429,240,1],[new Date(2014,1,08,11,30,46),2173,429,238,1],[new Date(2014,1,08,12,31,11),2175,429,236,1],[new Date(2014,1,08,13,30,45),2175,429,236,10],[new Date(2014,1,08,14,30,42),2175,429,236,10],[new Date(2014,1,08,17,31,07),2176,429,235,9],[new Date(2014,1,08,17,35,23),2176,429,235,18],[new Date(2014,1,08,18,04,47),2176,429,235,18],[new Date(2014,1,08,18,31,35),2171,429,241,0],[new Date(2014,1,08,19,31,33),2171,429,241,0],[new Date(2014,1,08,20,31,29),2171,429,241,0],[new Date(2014,1,08,21,31,27),2171,429,241,0],[new Date(2014,1,08,22,31,26),2171,429,241,0],[new Date(2014,1,08,23,31,26),2171,429,241,0],[new Date(2014,1,09,00,31,28),2171,429,241,0],[new Date(2014,1,09,01,35,16),2172,429,240,0],[new Date(2014,1,09,02,31,32),2172,429,240,0],[new Date(2014,1,09,03,31,37),2172,429,240,0],[new Date(2014,1,09,04,31,37),2172,429,240,0],[new Date(2014,1,09,05,31,37),2172,429,240,0],[new Date(2014,1,09,06,31,37),2172,429,240,0],[new Date(2014,1,09,07,31,39),2172,429,240,0],[new Date(2014,1,09,08,31,38),2172,429,240,0],[new Date(2014,1,09,09,31,37),2172,429,240,0],[new Date(2014,1,09,10,31,37),2172,429,240,0],[new Date(2014,1,09,11,31,38),2172,429,240,0],[new Date(2014,1,09,12,31,37),2172,429,240,0],[new Date(2014,1,09,13,31,38),2172,429,240,0],[new Date(2014,1,09,14,31,37),2172,429,240,0],[new Date(2014,1,09,15,31,39),2172,429,240,0],[new Date(2014,1,09,16,31,37),2172,429,240,0],[new Date(2014,1,09,17,31,39),2172,429,240,0],[new Date(2014,1,09,18,31,29),2172,429,240,0],[new Date(2014,1,09,19,31,35),2172,429,240,0],[new Date(2014,1,09,20,31,36),2172,429,240,0],[new Date(2014,1,09,21,31,39),2172,429,240,0],[new Date(2014,1,09,22,31,35),2172,429,240,0],[new Date(2014,1,09,23,31,30),2172,429,240,0],[new Date(2014,1,10,00,31,29),2172,429,240,0],[new Date(2014,1,10,01,31,30),2172,429,240,0],[new Date(2014,1,10,02,31,36),2172,429,240,0],[new Date(2014,1,10,03,31,35),2173,428,240,0],[new Date(2014,1,10,04,31,29),2173,428,240,0],[new Date(2014,1,10,05,31,35),2173,428,240,0],[new Date(2014,1,10,06,31,29),2173,428,240,0],[new Date(2014,1,10,07,31,28),2173,428,240,0],[new Date(2014,1,10,08,31,28),2173,428,240,0],[new Date(2014,1,10,09,31,35),2173,428,240,0],[new Date(2014,1,10,10,31,35),2173,428,240,0],[new Date(2014,1,10,11,31,36),2173,428,240,0],[new Date(2014,1,10,12,31,35),2173,428,240,0],[new Date(2014,1,10,13,31,28),2173,428,240,0],[new Date(2014,1,10,14,31,36),2173,428,240,0],[new Date(2014,1,10,15,31,28),2173,428,240,0],[new Date(2014,1,10,16,31,29),2173,428,240,0],[new Date(2014,1,10,17,31,29),2173,428,240,0],[new Date(2014,1,10,18,31,29),2173,428,240,0],[new Date(2014,1,10,19,31,30),2173,428,240,0],[new Date(2014,1,10,20,31,32),2173,428,240,0],[new Date(2014,1,10,21,31,37),2173,428,240,0],[new Date(2014,1,10,22,31,33),2173,428,240,0],[new Date(2014,1,10,23,31,30),2173,428,240,0],[new Date(2014,1,11,00,31,30),2173,428,240,0],[new Date(2014,1,11,01,31,30),2173,428,240,0],[new Date(2014,1,11,02,31,35),2173,428,240,0],[new Date(2014,1,11,03,31,30),2173,428,240,0],[new Date(2014,1,11,04,31,36),2173,428,240,0],[new Date(2014,1,11,05,31,28),2173,428,240,0],[new Date(2014,1,11,06,31,28),2173,428,240,0],[new Date(2014,1,11,07,31,36),2173,428,240,0],[new Date(2014,1,11,08,31,29),2173,428,240,0],[new Date(2014,1,11,09,31,29),2173,428,240,0],[new Date(2014,1,11,10,31,29),2173,428,240,0],[new Date(2014,1,11,11,31,35),2173,428,240,0],[new Date(2014,1,11,12,31,36),2173,428,240,0],[new Date(2014,1,11,13,31,27),2173,428,240,0],[new Date(2014,1,11,14,31,36),2173,428,240,0],[new Date(2014,1,11,15,31,35),2173,428,240,0],[new Date(2014,1,11,16,31,36),2173,428,240,0],[new Date(2014,1,11,17,31,30),2173,428,240,0],[new Date(2014,1,11,18,31,37),2173,428,240,0],[new Date(2014,1,11,19,31,29),2173,428,240,0],[new Date(2014,1,11,20,31,36),2173,428,240,0],[new Date(2014,1,11,21,31,29),2173,428,240,0],[new Date(2014,1,11,22,31,37),2173,428,240,0],[new Date(2014,1,11,23,31,31),2173,428,240,0],[new Date(2014,1,12,00,31,36),2173,428,240,0],[new Date(2014,1,12,01,29,26),2173,428,240,0],[new Date(2014,1,12,01,30,35),2173,428,240,0],[new Date(2014,1,12,01,31,30),2173,428,240,0],[new Date(2014,1,12,01,38,26),2173,428,240,0],[new Date(2014,1,12,02,32,19),2175,427,238,0],[new Date(2014,1,12,03,32,23),2173,425,242,0],[new Date(2014,1,12,04,32,17),2173,425,242,0],[new Date(2014,1,12,05,32,15),2173,425,242,0],[new Date(2014,1,12,06,32,23),2173,425,242,0],[new Date(2014,1,12,07,31,42),2175,425,240,8],[new Date(2014,1,12,08,32,17),2175,425,240,8],[new Date(2014,1,12,09,32,24),2175,425,240,8],[new Date(2014,1,12,10,32,16),2177,425,238,16],[new Date(2014,1,12,11,32,16),2177,425,238,16],[new Date(2014,1,12,12,32,25),2177,425,238,16],[new Date(2014,1,12,13,32,30),2179,425,236,24],[new Date(2014,1,12,14,36,40),2181,425,234,22],[new Date(2014,1,12,17,32,33),2182,425,233,24],[new Date(2014,1,12,18,32,18),2182,425,233,24],[new Date(2014,1,12,19,32,18),2182,425,233,24],[new Date(2014,1,12,20,32,23),2182,425,233,24],[new Date(2014,1,12,21,18,40),2182,425,233,24],[new Date(2014,1,12,21,32,17),2182,425,233,24],[new Date(2014,1,12,21,53,40),2182,425,233,24],[new Date(2014,1,12,22,09,45),2182,425,233,24],[new Date(2014,1,12,22,15,20),2182,425,233,24],[new Date(2014,1,12,22,20,37),2182,425,233,24],[new Date(2014,1,12,22,23,02),2182,425,233,24],[new Date(2014,1,12,22,24,10),2182,425,233,24],[new Date(2014,1,12,22,32,15),2182,425,233,24],[new Date(2014,1,12,22,32,41),2182,425,233,24],[new Date(2014,1,12,22,40,57),2182,425,233,24],[new Date(2014,1,12,22,46,27),2182,425,233,24],[new Date(2014,1,12,22,50,46),2182,425,233,24],[new Date(2014,1,12,22,52,17),2182,425,233,24],[new Date(2014,1,12,22,58,45),2181,425,234,24],[new Date(2014,1,12,23,15,50),2184,422,234,24],[new Date(2014,1,12,23,19,34),2184,422,234,24],[new Date(2014,1,12,23,23,07),2184,422,234,24],[new Date(2014,1,12,23,32,29),2181,418,241,0],[new Date(2014,1,13,00,34,16),2182,418,240,0],[new Date(2014,1,13,01,32,26),2182,418,240,0],[new Date(2014,1,13,01,44,24),2182,421,243,0],[new Date(2014,1,13,02,36,02),2184,421,241,12],[new Date(2014,1,13,03,39,50),2187,421,238,9],[new Date(2014,1,13,04,32,24),2187,421,238,10],[new Date(2014,1,13,05,32,18),2187,421,238,10],[new Date(2014,1,13,06,32,18),2187,421,238,10],[new Date(2014,1,13,07,32,17),2187,421,238,10],[new Date(2014,1,13,08,32,26),2187,421,238,10],[new Date(2014,1,13,09,32,18),2187,421,238,10],[new Date(2014,1,13,10,32,18),2187,421,238,10],[new Date(2014,1,13,11,32,18),2187,421,238,10],[new Date(2014,1,13,12,34,05),2188,421,237,9],[new Date(2014,1,13,13,32,18),2188,421,237,10],[new Date(2014,1,13,14,32,22),2188,421,237,10],[new Date(2014,1,13,15,32,24),2188,421,237,10],[new Date(2014,1,13,16,32,20),2188,421,237,10],[new Date(2014,1,13,17,32,36),2188,421,237,10],[new Date(2014,1,13,18,34,31),2190,421,235,22],[new Date(2014,1,13,19,32,24),2190,421,235,22],[new Date(2014,1,13,20,35,26),2191,421,234,21],[new Date(2014,1,13,21,32,23),2191,421,234,21],[new Date(2014,1,13,22,32,23),2191,421,234,21],[new Date(2014,1,13,23,32,17),2191,421,234,21],[new Date(2014,1,14,00,32,20),2190,421,235,15],[new Date(2014,1,14,01,32,15),2190,422,234,15],[new Date(2014,1,14,02,32,17),2190,422,234,15],[new Date(2014,1,14,03,32,24),2193,419,234,15],[new Date(2014,1,14,04,32,25),2193,419,234,29],[new Date(2014,1,14,05,33,33),2193,425,228,0],[new Date(2014,1,14,06,32,29),2193,425,228,0],[new Date(2014,1,14,07,32,15),2193,425,228,14],[new Date(2014,1,14,08,32,26),2193,425,228,14],[new Date(2014,1,14,09,31,41),2193,425,228,28],[new Date(2014,1,14,10,33,53),2194,425,227,27],[new Date(2014,1,14,11,32,24),2194,425,227,27],[new Date(2014,1,14,12,32,15),2194,425,227,27],[new Date(2014,1,14,13,32,16),2194,425,227,27],[new Date(2014,1,14,14,32,16),2194,425,227,27],[new Date(2014,1,14,15,32,14),2194,425,227,27],[new Date(2014,1,14,16,32,15),2194,425,227,27],[new Date(2014,1,14,17,32,16),2194,425,227,27],[new Date(2014,1,14,18,32,15),2194,425,227,27],[new Date(2014,1,14,19,32,16),2194,425,227,27],[new Date(2014,1,14,20,32,21),2194,425,227,27],[new Date(2014,1,14,21,32,20),2194,425,227,27],[new Date(2014,1,14,22,32,16),2194,424,228,27],[new Date(2014,1,14,23,28,47),2194,22,631,435],[new Date(2014,1,14,23,30,31),2194,22,631,435],[new Date(2014,1,15,00,32,21),2194,22,631,435],[new Date(2014,1,15,01,32,27),2194,22,631,435],[new Date(2014,1,15,02,34,32),2195,22,630,448],[new Date(2014,1,15,03,42,07),2199,22,626,444],[new Date(2014,1,15,04,32,26),2199,22,626,445],[new Date(2014,1,15,05,32,19),2199,22,626,445],[new Date(2014,1,15,06,32,19),2199,22,626,445],[new Date(2014,1,15,07,34,36),2200,22,625,444],[new Date(2014,1,15,08,32,27),2200,22,625,445],[new Date(2014,1,15,09,32,19),2200,22,625,445],[new Date(2014,1,15,10,32,21),2200,22,625,445],[new Date(2014,1,15,11,32,22),2200,22,625,445],[new Date(2014,1,15,11,38,29),2200,22,625,445],[new Date(2014,1,15,11,39,13),2200,22,625,445],[new Date(2014,1,15,12,34,55),2201,22,624,444],[new Date(2014,1,15,13,32,36),2201,22,624,444],[new Date(2014,1,15,14,32,27),2201,22,624,444],[new Date(2014,1,15,15,32,26),2201,22,624,444],[new Date(2014,1,15,16,32,19),2201,22,624,444],[new Date(2014,1,15,17,32,19),2201,22,624,444],[new Date(2014,1,15,18,32,35),2201,22,624,444],[new Date(2014,1,15,19,38,35),2203,22,622,446],[new Date(2014,1,15,20,34,44),2204,22,621,447],[new Date(2014,1,15,21,32,27),2204,22,621,448],[new Date(2014,1,15,22,36,17),2205,22,620,447],[new Date(2014,1,15,23,32,30),2205,22,620,448],[new Date(2014,1,16,00,32,19),2205,22,620,448],[new Date(2014,1,16,01,32,19),2205,22,620,448],[new Date(2014,1,16,02,32,19),2205,22,620,448],[new Date(2014,1,16,03,32,21),2205,22,624,439],[new Date(2014,1,16,04,32,18),2205,22,624,439],[new Date(2014,1,16,05,32,18),2205,22,624,439],[new Date(2014,1,16,06,32,19),2205,22,624,439],[new Date(2014,1,16,07,32,19),2205,22,624,439],[new Date(2014,1,16,08,32,26),2205,22,624,439],[new Date(2014,1,16,09,32,19),2205,22,624,439],[new Date(2014,1,16,10,32,26),2205,22,624,439],[new Date(2014,1,16,11,32,21),2205,22,624,439],[new Date(2014,1,16,12,32,49),2205,22,624,439],[new Date(2014,1,16,13,36,20),2206,22,623,439],[new Date(2014,1,16,14,35,20),2207,22,622,440],[new Date(2014,1,16,15,38,29),2209,22,620,440],[new Date(2014,1,16,16,35,10),2210,22,619,441],[new Date(2014,1,16,17,32,27),2210,22,619,442],[new Date(2014,1,16,18,32,26),2210,22,619,442],[new Date(2014,1,16,19,32,19),2210,22,619,442],[new Date(2014,1,16,20,32,27),2210,22,619,442],[new Date(2014,1,16,21,36,09),2211,22,618,442],[new Date(2014,1,16,22,32,27),2211,22,618,443],[new Date(2014,1,16,23,35,04),2212,22,617,446],[new Date(2014,1,17,00,36,05),2213,22,616,446],[new Date(2014,1,17,01,32,27),2213,22,616,447],[new Date(2014,1,17,02,34,58),2214,22,615,446],[new Date(2014,1,17,03,35,03),2215,22,614,447],[new Date(2014,1,17,04,32,25),2215,22,614,447],[new Date(2014,1,17,05,32,19),2215,22,614,447],[new Date(2014,1,17,06,32,19),2215,22,614,447],[new Date(2014,1,17,07,32,25),2215,22,614,447],[new Date(2014,1,17,08,32,18),2215,22,614,447],[new Date(2014,1,17,09,32,19),2215,22,614,447],[new Date(2014,1,17,10,32,19),2215,22,614,447],[new Date(2014,1,17,11,32,19),2215,22,614,447],[new Date(2014,1,17,12,32,25),2215,22,614,447],[new Date(2014,1,17,13,34,21),2216,22,613,446],[new Date(2014,1,17,14,35,04),2217,22,612,446],[new Date(2014,1,17,15,35,03),2218,22,611,446],[new Date(2014,1,17,16,32,31),2218,22,611,447],[new Date(2014,1,17,17,37,13),2220,22,609,449],[new Date(2014,1,17,18,32,27),2220,22,609,451],[new Date(2014,1,17,19,32,28),2220,22,609,451],[new Date(2014,1,17,20,35,10),2221,22,608,450],[new Date(2014,1,17,21,35,56),2222,22,607,450],[new Date(2014,1,17,22,35,06),2223,22,606,450],[new Date(2014,1,17,23,35,09),2224,22,605,450],[new Date(2014,1,18,00,32,28),2224,22,605,450],[new Date(2014,1,18,01,34,58),2225,22,604,450],[new Date(2014,1,18,02,32,24),2225,22,604,451],[new Date(2014,1,18,03,35,05),2226,22,603,429],[new Date(2014,1,18,04,35,49),2227,22,602,429],[new Date(2014,1,18,05,34,49),2228,22,601,429],[new Date(2014,1,18,06,32,36),2228,22,601,430],[new Date(2014,1,18,07,35,29),2229,22,600,433],[new Date(2014,1,18,08,35,10),2230,22,599,433],[new Date(2014,1,18,09,32,32),2230,22,599,434],[new Date(2014,1,18,10,32,27),2230,22,599,438],[new Date(2014,1,18,11,37,50),2232,22,597,440],[new Date(2014,1,18,12,35,00),2233,22,596,441],[new Date(2014,1,18,13,32,29),2233,22,596,442],[new Date(2014,1,18,14,32,28),2233,22,596,442],[new Date(2014,1,18,15,35,13),2234,22,595,441],[new Date(2014,1,18,16,34,58),2235,22,594,441],[new Date(2014,1,18,17,32,28),2235,22,594,442],[new Date(2014,1,18,18,32,27),2235,22,594,442],[new Date(2014,1,18,19,32,35),2235,22,594,442],[new Date(2014,1,18,20,32,41),2235,22,594,446],[new Date(2014,1,18,21,35,06),2236,22,593,445],[new Date(2014,1,18,22,35,04),2237,22,592,445],[new Date(2014,1,18,23,25,10),2238,22,591,445],[new Date(2014,1,18,23,28,53),2238,22,591,445],[new Date(2014,1,18,23,30,46),2238,22,591,447],[new Date(2014,1,18,23,31,14),2238,22,591,447],[new Date(2014,1,18,23,45,15),2238,22,591,447],[new Date(2014,1,18,23,55,12),2238,22,591,447],[new Date(2014,1,19,00,26,38),2240,22,589,446],[new Date(2014,1,19,00,32,32),2240,22,589,446],[new Date(2014,1,19,00,56,58),2240,22,589,446],[new Date(2014,1,19,00,57,58),2240,22,589,446],[new Date(2014,1,19,01,36,15),2241,22,588,446],[new Date(2014,1,19,02,32,21),2241,22,588,446],[new Date(2014,1,19,03,32,27),2241,22,588,446],[new Date(2014,1,19,04,32,32),2241,22,588,446],[new Date(2014,1,19,05,32,30),2241,22,588,446],[new Date(2014,1,19,06,32,26),2241,22,588,446],[new Date(2014,1,19,07,32,31),2241,22,588,446],[new Date(2014,1,19,08,32,29),2241,22,588,446],[new Date(2014,1,19,09,32,28),2241,22,588,446],[new Date(2014,1,19,10,34,43),2242,22,587,446],[new Date(2014,1,19,11,32,30),2242,22,587,446],[new Date(2014,1,19,12,35,12),2243,22,586,446],[new Date(2014,1,19,13,35,40),2244,22,585,446],[new Date(2014,1,19,14,32,35),2244,22,585,446],[new Date(2014,1,19,15,35,08),2245,22,584,446],[new Date(2014,1,19,16,32,27),2245,22,584,446],[new Date(2014,1,19,17,32,29),2245,22,584,446],[new Date(2014,1,19,18,33,25),2245,22,584,446],[new Date(2014,1,19,19,32,23),2245,22,584,446],[new Date(2014,1,19,20,32,33),2245,22,584,446],[new Date(2014,1,19,21,35,11),2246,22,583,446],[new Date(2014,1,19,22,35,01),2247,22,582,446],[new Date(2014,1,19,23,32,41),2247,22,582,446],[new Date(2014,1,20,00,08,18),2247,468,136,0],[new Date(2014,1,20,00,16,42),2247,468,136,0],[new Date(2014,1,20,00,32,42),2247,468,136,0],[new Date(2014,1,20,01,34,32),2248,468,135,0],[new Date(2014,1,20,02,32,25),2248,468,135,0],[new Date(2014,1,20,03,32,28),2248,451,153,0],[new Date(2014,1,20,04,32,29),2248,451,153,0],[new Date(2014,1,20,05,32,30),2248,451,153,0],[new Date(2014,1,20,06,32,34),2248,451,153,0],[new Date(2014,1,20,07,32,28),2248,451,153,0],[new Date(2014,1,20,08,32,26),2248,451,153,0],[new Date(2014,1,20,09,32,27),2248,451,153,0],[new Date(2014,1,20,10,32,29),2248,451,153,0],[new Date(2014,1,20,11,32,30),2248,451,153,0],[new Date(2014,1,20,12,32,31),2248,451,153,0],[new Date(2014,1,20,13,32,33),2248,451,153,0],[new Date(2014,1,20,14,32,32),2248,451,153,0],[new Date(2014,1,20,15,32,33),2248,451,153,0],[new Date(2014,1,20,16,32,31),2248,451,153,0],[new Date(2014,1,20,17,32,33),2248,451,153,0],[new Date(2014,1,20,18,32,30),2248,451,153,0],[new Date(2014,1,20,19,32,32),2248,451,153,0],[new Date(2014,1,20,20,32,30),2248,451,153,0],[new Date(2014,1,20,21,32,39),2248,451,153,0],[new Date(2014,1,20,22,32,37),2248,451,153,0],[new Date(2014,1,20,23,32,33),2248,451,153,0],[new Date(2014,1,21,00,32,30),2248,451,153,0],[new Date(2014,1,21,01,32,34),2248,451,153,0],[new Date(2014,1,21,02,32,30),2248,451,153,0],[new Date(2014,1,21,03,32,34),2248,451,153,0],[new Date(2014,1,21,04,32,32),2248,451,153,0],[new Date(2014,1,21,05,32,33),2248,451,153,0],[new Date(2014,1,21,06,32,30),2248,451,153,0],[new Date(2014,1,21,07,32,34),2248,451,153,0],[new Date(2014,1,21,08,32,31),2248,451,153,0],[new Date(2014,1,21,09,32,34),2248,451,153,0],[new Date(2014,1,21,10,32,30),2248,451,153,0],[new Date(2014,1,21,11,32,33),2248,451,153,0],[new Date(2014,1,21,12,32,35),2248,451,153,0],[new Date(2014,1,21,13,32,36),2248,451,153,0],[new Date(2014,1,21,14,32,33),2248,451,153,0],[new Date(2014,1,21,15,32,36),2248,451,153,0],[new Date(2014,1,21,16,32,33),2248,451,153,0],[new Date(2014,1,21,17,32,37),2248,451,153,0],[new Date(2014,1,21,18,32,33),2248,451,153,0],[new Date(2014,1,21,19,32,37),2248,451,153,0],[new Date(2014,1,21,20,32,33),2248,451,153,0],[new Date(2014,1,21,21,32,36),2248,451,153,0],[new Date(2014,1,21,22,32,33),2248,451,153,0],[new Date(2014,1,21,23,32,35),2248,451,153,0],[new Date(2014,1,22,00,32,33),2248,451,153,0],[new Date(2014,1,22,01,33,06),2248,451,153,0],[new Date(2014,1,22,02,32,23),2248,451,153,0],[new Date(2014,1,22,03,32,26),2248,193,413,0],[new Date(2014,1,22,04,37,08),2250,193,411,0],[new Date(2014,1,22,05,35,05),2252,193,409,0],[new Date(2014,1,22,06,34,52),2254,193,407,0],[new Date(2014,1,22,07,36,37),2256,193,405,0],[new Date(2014,1,22,08,35,18),2258,193,403,0],[new Date(2014,1,22,09,34,44),2260,193,401,0],[new Date(2014,1,22,10,37,42),2262,193,399,0],[new Date(2014,1,22,11,36,55),2264,193,397,0],[new Date(2014,1,22,12,35,55),2266,193,395,0],[new Date(2014,1,22,13,35,10),2268,193,393,0],[new Date(2014,1,22,14,37,42),2270,193,391,0],[new Date(2014,1,22,15,35,51),2272,193,389,0],[new Date(2014,1,22,16,35,06),2274,193,387,0],[new Date(2014,1,22,17,35,22),2276,193,385,0],[new Date(2014,1,22,18,32,34),2276,193,385,0],[new Date(2014,1,22,19,32,32),2276,193,385,0],[new Date(2014,1,22,20,32,49),2276,193,385,0],[new Date(2014,1,22,21,32,43),2276,193,385,0],[new Date(2014,1,22,22,32,49),2276,193,385,0],[new Date(2014,1,22,23,36,04),2276,193,385,0],[new Date(2014,1,23,00,38,08),2273,198,383,0],[new Date(2014,1,23,01,18,46),2249,448,157,0],[new Date(2014,1,23,01,32,25),2249,448,157,0],[new Date(2014,1,23,02,32,21),2249,448,157,0],[new Date(2014,1,23,03,32,22),2249,448,157,0],[new Date(2014,1,23,04,32,30),2249,448,157,0],[new Date(2014,1,23,05,32,29),2249,448,157,0],[new Date(2014,1,23,06,32,28),2249,448,157,0],[new Date(2014,1,23,07,32,22),2249,448,157,0],[new Date(2014,1,23,08,32,27),2249,448,157,0],[new Date(2014,1,23,09,32,29),2249,448,157,0],[new Date(2014,1,23,10,32,29),2249,448,157,0],[new Date(2014,1,23,11,32,29),2249,448,157,0],[new Date(2014,1,23,12,32,28),2249,448,157,0],[new Date(2014,1,23,13,34,47),2249,448,157,0],[new Date(2014,1,23,20,33,23),2249,448,157,0],[new Date(2014,1,23,21,30,38),2249,448,157,0],[new Date(2014,1,23,22,52,48),2249,454,164,0],[new Date(2014,1,23,23,00,30),2249,472,171,0],[new Date(2014,1,23,23,38,41),2250,472,170,0],[new Date(2014,1,24,00,34,34),2251,472,169,0],[new Date(2014,1,24,01,32,24),2251,472,169,0],[new Date(2014,1,24,02,35,06),2252,472,168,0],[new Date(2014,1,24,03,33,46),2253,496,145,0],[new Date(2014,1,24,04,35,50),2255,496,143,0],[new Date(2014,1,24,05,33,37),2256,496,142,0],[new Date(2014,1,24,06,33,54),2257,496,141,0],[new Date(2014,1,24,07,33,45),2258,496,140,0],[new Date(2014,1,24,08,33,36),2259,496,139,0],[new Date(2014,1,24,09,33,39),2260,496,138,0],[new Date(2014,1,24,10,36,03),2262,496,136,0],[new Date(2014,1,24,11,35,02),2264,496,134,0],[new Date(2014,1,24,12,34,52),2266,496,132,0],[new Date(2014,1,24,13,32,33),2266,496,132,0],[new Date(2014,1,24,14,32,28),2266,496,132,0],[new Date(2014,1,24,15,32,23),2266,496,132,0],[new Date(2014,1,24,16,32,20),2266,496,132,0],[new Date(2014,1,24,17,32,30),2266,496,132,0],[new Date(2014,1,24,18,32,27),2266,496,132,0],[new Date(2014,1,24,19,32,19),2266,496,132,0],[new Date(2014,1,24,20,32,31),2266,496,132,0],[new Date(2014,1,24,21,32,28),2266,496,132,0],[new Date(2014,1,24,22,24,26),2266,496,132,0],[new Date(2014,1,24,22,32,31),2267,496,132,0],[new Date(2014,1,24,23,32,15),2267,500,191,0],[new Date(2014,1,25,00,32,17),2267,500,191,0],[new Date(2014,1,25,01,36,29),2269,500,189,0],[new Date(2014,1,25,02,34,29),2271,500,187,0],[new Date(2014,1,25,03,34,35),2273,500,185,0],[new Date(2014,1,25,04,34,32),2275,500,183,0],[new Date(2014,1,25,05,34,57),2277,500,181,0],[new Date(2014,1,25,06,34,37),2279,500,179,0],[new Date(2014,1,25,07,34,40),2281,500,177,0],[new Date(2014,1,25,08,34,32),2283,500,175,0],[new Date(2014,1,25,09,35,15),2285,503,173,0],[new Date(2014,1,25,10,35,07),2287,503,171,0],[new Date(2014,1,25,11,35,03),2289,503,169,0],[new Date(2014,1,25,12,35,10),2291,503,167,0],[new Date(2014,1,25,13,35,00),2293,503,165,0],[new Date(2014,1,25,14,35,07),2295,503,163,0],[new Date(2014,1,25,15,34,55),2297,503,161,0],[new Date(2014,1,25,16,34,03),2298,503,160,0],[new Date(2014,1,25,17,33,55),2299,503,159,0],[new Date(2014,1,25,18,33,36),2300,503,158,0],[new Date(2014,1,25,19,33,59),2301,503,157,0],[new Date(2014,1,26,00,33,32),2302,503,156,0],[new Date(2014,1,26,01,25,48),2302,503,156,0],[new Date(2014,1,26,01,30,40),2302,503,156,0],[new Date(2014,1,26,01,42,20),2302,503,156,0],[new Date(2014,1,26,02,33,57),2303,503,155,0],[new Date(2014,1,26,03,33,56),2304,503,154,0],[new Date(2014,1,26,18,40,14),2305,503,153,0],[new Date(2014,1,26,18,41,22),2305,503,153,0],[new Date(2014,1,26,19,34,55),1250,369,121,0],[new Date(2014,1,26,20,33,52),2307,503,151,0],[new Date(2014,1,26,21,33,57),2308,503,150,0],[new Date(2014,1,26,22,34,00),2309,503,149,0],[new Date(2014,1,26,23,34,03),2310,503,148,0],[new Date(2014,1,27,00,33,25),2311,503,147,0],[new Date(2014,1,27,01,34,13),2312,503,146,0],[new Date(2014,1,27,02,33,44),2313,503,145,0],[new Date(2014,1,27,03,34,09),2314,503,144,0],[new Date(2014,1,27,04,35,27),2316,503,142,0],[new Date(2014,1,27,05,35,00),2318,503,140,0],[new Date(2014,1,27,06,35,36),2320,503,138,0],[new Date(2014,1,27,07,35,32),2322,503,136,0],[new Date(2014,1,27,08,34,13),2323,503,135,0],[new Date(2014,1,27,09,33,50),2324,503,134,0],[new Date(2014,1,27,10,34,00),2325,503,133,0],[new Date(2014,1,27,11,34,02),2326,503,132,0],[new Date(2014,1,27,12,34,06),2327,503,131,0],[new Date(2014,1,27,13,34,06),2328,503,130,0],[new Date(2014,1,27,14,32,40),2328,503,130,0],[new Date(2014,1,27,15,32,39),2328,503,130,0],[new Date(2014,1,27,16,32,37),2328,503,130,0],[new Date(2014,1,27,17,32,38),2328,503,130,0],[new Date(2014,1,27,18,32,40),2328,503,130,0],[new Date(2014,1,27,19,32,40),2328,503,130,0],[new Date(2014,1,27,20,32,41),2328,503,130,0],[new Date(2014,1,27,21,32,41),2328,503,130,0],[new Date(2014,1,27,22,34,01),2329,503,129,0],[new Date(2014,1,27,23,36,35),1614,418,106,0],[new Date(2014,1,28,00,32,39),2330,503,128,0],[new Date(2014,1,28,01,32,38),2330,503,128,0],[new Date(2014,1,28,02,32,27),2330,503,128,0],[new Date(2014,1,28,03,32,26),2330,503,128,0],[new Date(2014,1,28,04,33,01),2330,503,128,0],[new Date(2014,1,28,05,32,36),2330,503,128,0],[new Date(2014,1,28,06,32,38),2330,503,128,0],[new Date(2014,1,28,07,32,41),2330,503,128,0],[new Date(2014,1,28,08,32,32),2330,503,128,0],[new Date(2014,1,28,09,32,31),2330,503,128,0],[new Date(2014,1,28,10,32,31),2330,503,128,0],[new Date(2014,1,28,11,34,48),2331,503,127,0],[new Date(2014,1,28,12,32,28),2331,503,127,0],[new Date(2014,1,28,13,32,33),2331,503,127,0],[new Date(2014,1,28,14,32,28),2331,503,127,0],[new Date(2014,1,28,15,32,30),2331,503,127,0],[new Date(2014,1,28,16,33,53),2332,503,126,0],[new Date(2014,1,28,17,32,29),2332,503,126,0],[new Date(2014,1,28,18,32,28),2332,503,126,0],[new Date(2014,1,28,19,32,29),2332,503,126,0],[new Date(2014,1,28,20,32,28),2332,503,126,0],[new Date(2014,1,28,21,34,37),2333,503,125,0],[new Date(2014,1,28,22,32,36),2333,503,125,0],[new Date(2014,1,28,23,32,30),2333,503,125,0],[new Date(2014,2,01,00,32,33),2333,503,125,0],[new Date(2014,2,01,01,32,33),2333,503,125,0],[new Date(2014,2,01,02,32,26),2333,503,125,0],[new Date(2014,2,01,03,32,30),2333,503,125,0],[new Date(2014,2,01,04,32,30),2333,503,125,0],[new Date(2014,2,01,05,32,33),2333,503,125,0],[new Date(2014,2,01,06,32,30),2333,503,125,0],[new Date(2014,2,01,07,32,31),2333,503,125,0],[new Date(2014,2,01,08,32,29),2333,503,125,0],[new Date(2014,2,01,09,32,32),2333,503,125,0],[new Date(2014,2,01,10,32,29),2333,503,125,0],[new Date(2014,2,01,11,32,33),2333,503,125,0],[new Date(2014,2,01,12,32,24),2333,503,125,0],[new Date(2014,2,01,13,32,30),2333,503,125,0],[new Date(2014,2,01,14,32,28),2333,503,125,0],[new Date(2014,2,01,15,32,25),2333,503,125,0],[new Date(2014,2,01,16,32,24),2333,503,125,0],[new Date(2014,2,01,17,32,26),2333,503,125,0],[new Date(2014,2,01,18,32,29),2333,503,125,0],[new Date(2014,2,01,19,32,28),2333,503,125,0],[new Date(2014,2,01,20,32,27),2333,503,125,0],[new Date(2014,2,01,21,32,27),2333,503,125,0],[new Date(2014,2,01,22,32,51),2333,503,125,0],[new Date(2014,2,01,23,32,27),2333,503,125,0],[new Date(2014,2,02,00,32,25),2333,503,125,0],[new Date(2014,2,02,01,32,23),2333,503,125,0],[new Date(2014,2,02,02,32,29),2333,503,125,0],[new Date(2014,2,02,03,32,24),2333,503,125,0],[new Date(2014,2,02,04,32,27),2333,503,125,0],[new Date(2014,2,02,05,32,28),2333,503,125,0],[new Date(2014,2,02,06,32,26),2333,503,125,0],[new Date(2014,2,02,07,32,28),2333,503,125,0],[new Date(2014,2,02,08,32,26),2333,503,125,0],[new Date(2014,2,02,09,32,26),2333,503,125,0],[new Date(2014,2,02,10,32,26),2333,503,125,0],[new Date(2014,2,02,11,32,30),2333,503,125,0],[new Date(2014,2,02,12,32,27),2333,503,125,0],[new Date(2014,2,02,13,34,22),2333,503,125,0],[new Date(2014,2,02,14,32,28),2333,503,125,0],[new Date(2014,2,02,15,32,26),2333,503,125,0],[new Date(2014,2,02,16,32,26),2333,503,125,0],[new Date(2014,2,02,17,32,26),2333,503,125,0],[new Date(2014,2,02,18,32,26),2333,503,125,0],[new Date(2014,2,02,19,32,27),2333,503,125,0],[new Date(2014,2,02,20,32,28),2333,503,125,0],[new Date(2014,2,02,21,32,25),2333,503,125,0],[new Date(2014,2,02,21,57,13),2333,503,125,0],[new Date(2014,2,02,22,32,26),2333,503,125,0],[new Date(2014,2,02,23,32,26),2333,503,125,0],[new Date(2014,2,03,00,32,25),2333,503,125,0],[new Date(2014,2,03,01,32,26),2333,503,125,0],[new Date(2014,2,03,02,32,26),2333,503,125,0],[new Date(2014,2,03,03,32,24),2333,503,125,0],[new Date(2014,2,03,04,32,27),2333,503,125,0],[new Date(2014,2,03,05,32,27),2333,503,125,0],[new Date(2014,2,03,06,32,27),2333,503,125,0],[new Date(2014,2,03,07,32,27),2333,503,125,0],[new Date(2014,2,03,08,32,27),2333,503,125,0],[new Date(2014,2,03,09,32,28),2333,503,125,0],[new Date(2014,2,03,10,32,27),2333,503,125,0],[new Date(2014,2,03,11,32,27),2333,503,125,0],[new Date(2014,2,03,12,32,28),2333,503,125,0],[new Date(2014,2,03,13,32,28),2333,503,125,0],[new Date(2014,2,03,14,32,26),2333,503,125,0],[new Date(2014,2,03,15,32,27),2333,503,125,0],[new Date(2014,2,03,16,32,26),2333,503,125,0],[new Date(2014,2,03,17,32,23),2333,503,125,0],[new Date(2014,2,03,18,32,27),2333,503,125,0],[new Date(2014,2,03,19,32,25),2333,503,125,0],[new Date(2014,2,03,20,32,27),2333,503,125,0],[new Date(2014,2,03,20,46,44),2333,503,125,0],[new Date(2014,2,03,21,32,34),2333,499,147,0],[new Date(2014,2,03,22,19,28),2335,499,145,0],[new Date(2014,2,03,22,32,44),2335,499,145,0],[new Date(2014,2,03,23,35,17),2337,499,143,0],[new Date(2014,2,04,00,35,20),2339,499,141,0],[new Date(2014,2,04,01,34,36),2341,499,139,0],[new Date(2014,2,04,02,35,42),2343,499,137,0],[new Date(2014,2,04,03,36,15),2345,499,135,0],[new Date(2014,2,04,04,35,11),2344,510,125,0],[new Date(2014,2,04,05,35,20),2346,510,123,0],[new Date(2014,2,04,06,35,53),2348,510,121,0],[new Date(2014,2,04,07,36,05),2349,510,120,0],[new Date(2014,2,04,08,38,18),2351,510,118,0],[new Date(2014,2,04,09,35,40),2352,510,117,0],[new Date(2014,2,04,10,32,37),2352,510,117,0],[new Date(2014,2,04,11,33,41),2353,510,116,0],[new Date(2014,2,04,12,34,14),2354,510,115,0],[new Date(2014,2,04,13,32,33),2354,510,115,0],[new Date(2014,2,04,14,32,29),2354,510,115,0],[new Date(2014,2,04,15,32,31),2354,510,115,0],[new Date(2014,2,04,16,32,30),2354,510,115,0],[new Date(2014,2,04,17,32,31),2354,510,115,0],[new Date(2014,2,04,18,32,28),2354,510,115,0],[new Date(2014,2,04,19,32,28),2354,510,115,0],[new Date(2014,2,04,20,32,28),2354,510,115,0],[new Date(2014,2,04,21,32,27),2354,510,115,0],[new Date(2014,2,04,22,32,34),2354,510,115,0],[new Date(2014,2,04,23,32,25),2354,510,115,0],[new Date(2014,2,05,00,32,27),2354,510,115,0],[new Date(2014,2,05,01,33,05),2354,510,115,0],[new Date(2014,2,05,02,33,08),2354,510,115,0],[new Date(2014,2,05,03,33,45),2354,510,115,0],[new Date(2014,2,05,04,33,40),2354,510,115,0],[new Date(2014,2,05,05,34,01),2354,510,115,0],[new Date(2014,2,05,06,34,01),2354,510,115,0],[new Date(2014,2,05,07,33,29),2354,510,115,0],[new Date(2014,2,05,08,33,23),2354,510,115,0],[new Date(2014,2,05,09,33,08),2354,510,115,0],[new Date(2014,2,05,10,32,30),2354,510,115,0],[new Date(2014,2,05,11,32,28),2354,510,115,0],[new Date(2014,2,05,12,32,28),2354,510,115,0],[new Date(2014,2,05,13,32,27),2354,510,115,0],[new Date(2014,2,05,14,32,28),2354,510,115,0],[new Date(2014,2,05,15,32,30),2354,510,115,0],[new Date(2014,2,05,16,32,27),2354,510,115,0],[new Date(2014,2,05,17,32,32),2354,510,115,0],[new Date(2014,2,05,18,32,35),2354,510,115,0],[new Date(2014,2,05,19,32,27),2354,510,115,0],[new Date(2014,2,05,20,32,31),2354,510,115,0],[new Date(2014,2,05,21,32,30),2354,510,115,0],[new Date(2014,2,05,22,32,27),2354,510,115,0],[new Date(2014,2,05,23,32,26),2354,510,115,0],[new Date(2014,2,06,00,32,24),2354,510,115,0],[new Date(2014,2,09,19,04,04),2354,484,147,0],[new Date(2014,2,09,19,05,40),2354,484,147,0],[new Date(2014,2,09,20,00,46),2356,484,146,0]]);	  

		data.addRows(['.$data.']);	  

        var options = {
          title: \'Series\',
        };

        var chart = new google.visualization.LineChart(document.getElementById(\'chart\'));
        chart.draw(data, options);
      }
    </script>';
	$bodyend =  '<div id="chart" style="width: 600px; height: 300px;"></div>';
	
	$message = '
	<html>
	  <head>
	    <title>Visualisation statistiques SeriesAutoDownload</title>
		'.$head.'
	  </head>
	  <body>
		'.$bodyend.'
	  </body>
	</html>
	';
	
	return $message;
}


function OpenListAction($rep){
	$myListActionfile = array();
	$myListActionfile["myListActionfile"] =$rep."ListAction.xml";
	$myListActionfile["ListActionxml"]=new DomDocument('1.0');
	
	$createdefault = false;
	if(file_exists($myListActionfile["myListActionfile"])){
//		echo "file_exists". "</br>\n";
		$myListActionfile["ListActionxml"]->preserveWhiteSpace = false;
		$myListActionfile["ListActionxml"]->formatOutput = true; 
		$myListActionfile["ListActionxml"]->load($myListActionfile["myListActionfile"]);
		if (isset($myListActionfile["ListActionxml"]->documentElement)) {
			$myListActionfile["ListActionxmlbody"] = $myListActionfile["ListActionxml"]->documentElement;
		} else {
			$myListActionfile["ListActionxmlbody"] = $myListActionfile["ListActionxml"]->appendChild($myListActionfile["ListActionxml"]->createElement('body')); 
			$createdefault = true;
		}
	}else{
		$myListActionfile["ListActionxml"]->formatOutput = true; 
		$myListActionfile["ListActionxmlbody"] = $myListActionfile["ListActionxml"]->appendChild($myListActionfile["ListActionxml"]->createElement('body')); 
		$createdefault = true;
	}	
	if ($createdefault) {
		echo  "creation Default Xml". "</br>\n";
		$urlrep = $myListActionfile["ListActionxmlbody"]->appendChild($myListActionfile["ListActionxml"]->createElement('repertoire')); 
		$urlrep->appendChild($myListActionfile["ListActionxml"]->createTextNode($rep));
		$date = $myListActionfile["ListActionxmlbody"]->appendChild($myListActionfile["ListActionxml"]->createElement('date')); 
		$dt =  new DateTime();
		$dt->add(new DateInterval('PT6H'));
		$dt->setTime(0, 0);
		$dt->add(new DateInterval('PT18H'));
		$date->appendChild($myListActionfile["ListActionxml"]->createTextNode($dt->format('Y-m-d H:i:s')));		
		$myListActionfile["ListActionxml"]->save($myListActionfile["myListActionfile"]); 
	}
	return $myListActionfile;
}

function SvgListAction($rep,$rep2,$dt){
	if (copy($rep."ListAction.xml",$rep2."ListAction-$dt.xml")){
return "ListAction-$dt.xml";
} else {
return "";
}
}

function ClearListAction($rep){
	unlink($rep."ListAction.xml");
}

function AddListAction($ObjStat,$hash="",$LbAction,$EleAction="",$QualAction="",$CompAction=""){
	
	$Actionxml =  $ObjStat["ListActionxmlbody"]->appendChild($ObjStat["ListActionxml"]->createElement('Action')); 
	$hashxml = $Actionxml->appendChild($ObjStat["ListActionxml"]->createElement('hash')); 
	$hashxml->appendChild($ObjStat["ListActionxml"]->createTextNode($hash));
	$DtActionxml = $Actionxml->appendChild($ObjStat["ListActionxml"]->createElement('DtAction')); 
	$DtActionxml->appendChild($ObjStat["ListActionxml"]->createTextNode(date("Y-m-d H:i:s")));
	$LbActionxml = $Actionxml->appendChild($ObjStat["ListActionxml"]->createElement('LbAction')); 
	$LbActionxml->appendChild($ObjStat["ListActionxml"]->createTextNode($LbAction));	
	$EleActionxml = $Actionxml->appendChild($ObjStat["ListActionxml"]->createElement('EleAction')); 
	$EleActionxml->appendChild($ObjStat["ListActionxml"]->createTextNode($EleAction));	
	$QualActionxml = $Actionxml->appendChild($ObjStat["ListActionxml"]->createElement('QualAction')); 
	$QualActionxml->appendChild($ObjStat["ListActionxml"]->createTextNode($QualAction));	
	$CompActionxml = $Actionxml->appendChild($ObjStat["ListActionxml"]->createElement('CompAction')); 
	$CompActionxml->appendChild($ObjStat["ListActionxml"]->createTextNode($CompAction));
	
	$ObjStat["ListActionxml"]->save($ObjStat["myListActionfile"]); 

}

function CompteListAction($rep,$dtdeclenchement){

	$ObjStat= OpenListAction($rep);
	
	$n=0;
	$Action = $ObjStat["ListActionxml"]->getElementsByTagName('Action');
	foreach ($Action as $st) {
		if ($dtdeclenchement > $ObjStat["ListActionxml"]->getElementsByTagName('date')->item(0)->nodeValue) {
			$n +=1;
		}
	}
	return $n;
}

function VisualisationEtStockageListAction($rep,$repserie,$dtStart){
	
$ecrit =0;
$ObjStat= OpenListAction($rep);

    $DtComp = $ObjStat["ListActionxml"]->getElementsByTagName('date')->item(0)->nodeValue;
	$dt = new DateTime($DtComp);
	$dt->sub(new DateInterval('P1D'));
	$DtComp=$dt->format('Y-m-d H:i:s');
	$n=0;
	$arr=array();
	$Action = $ObjStat["ListActionxml"]->getElementsByTagName('Action');
	foreach ($Action as $st) {
		//$dtct = preg_split("/[: -]/",$st->getElementsByTagName('DtAction')->item(0)->nodeValue);
		//$dtc=date('Y-m-d H:i:s',$dtct[0]."-".($dtct[1]-1)."-".$dtct[2]." ".$dtct[3].":".$dtct[4].":".$dtct[5]);
		//$dtc = date('Y-m-d H:i:s',strtotime("-1 month",strtotime($st->getElementsByTagName('DtAction')->item(0)->nodeValue)));
		$arr[$n]=$st->getElementsByTagName('DtAction')->item(0)->nodeValue."/ActionN".substr($st->getElementsByTagName('LbAction')->item(0)->nodeValue,1,1)."/".$st->getElementsByTagName('hash')->item(0)->nodeValue;
		$n +=1;
	}

	asort($arr);

	$vp ="";
	foreach ($arr as $key => $value){
		if ($vp == $value){
			unset($arr[$key]);
		} else {
			$vp = $value;
		}
	}

	$body="";
	$body .='<table width="100%" border="0" cellspacing="0" cellpadding="5"><TABLE BORDER>';
	$body .="<TR>";
	$body .="<TD>Serie</TD>";
	$body .="<TD>S00E00</TD>";
	$body .="<TD>Titre</TD>";
	$body .="<TD>Ajout du torrent</TD>";
	$body .="<TD>Relance</TD>";
	$body .="<TD>copy bloqué</TD>";
	$body .="<TD>Download Complet + stockage</TD>";
	$body .="<TD>Purge Torrent</TD>";
	$body .="<TD></TD>";
	$body .="<TD>Download Complet</TD>";
	$body .="</TR>";
	
	$MyDirectory = opendir($repserie) or die('Erreur opendir');
	while($Entry = @readdir($MyDirectory)) {
		if(is_dir($repserie.$Entry.DIRECTORY_SEPARATOR)&& $Entry != '.' && $Entry != '..') {
			if(file_exists($repserie.$Entry.DIRECTORY_SEPARATOR.$Entry.".xml")){

				$myEntryfile=OpenEntry($repserie.$Entry.DIRECTORY_SEPARATOR,$Entry);
				$torentnum = $myEntryfile["Entryxml"]->getElementsByTagName('hash');
				//echo "$Entry ". "</br>\n";	
				foreach ($torentnum as $torenti) {
					$AEcrire = false;
					//echo "$torenti->nodeValue". "</br>\n";	
					foreach ($arr as $key => $value){
						$arrayData = preg_split("~/~", strtolower($value));
						if($arrayData[2]!=""){
							if($torenti->nodeValue == $arrayData[2]){
							 //echo "$Entry $arrayData[0] $arrayData[1] $arrayData[2]". "</br>\n";
								$episode=$torenti->parentNode;
								if (!isset($episode->getElementsByTagName($arrayData[1])->item(0)->nodeValue)){
									$cr=$episode->appendChild($myEntryfile["Entryxml"]->createElement($arrayData[1]));
								}
								$episode->getElementsByTagName($arrayData[1])->item(0)->nodeValue = $arrayData[0]; 
								$myEntryfile["Entryxml"]->save($myEntryfile["myEntryfile"]);
								$AEcrire = true;
							}
						}
					}
					if ($AEcrire){
$ecrit +=1;

						$episode=$torenti->parentNode;
						$season=$episode->parentNode;
						$body .="<TR>";
						$body .="<TD>".preg_replace("/[^a-zA-Z0-9. ]/", "",@$Entry)."</TD>";
						$body .="<TD>"."S".@$season->getElementsByTagName('valeur')->item(0)->nodeValue."E".@$episode->getElementsByTagName('valeur')->item(0)->nodeValue."</TD>";
						$body .="<TD>".preg_replace("/[^a-zA-Z0-9. ]/", "",@$episode->getElementsByTagName('titre')->item(0)->nodeValue)."</TD>";


						$body .="<TD>".FormatBold(@$episode->getElementsByTagName('actionn1')->item(0)->nodeValue,$DtComp)."</TD>";
//						$body .="<TD>".FormatBold(@$episode->getElementsByTagName('(2)')->item(0)->nodeValue,$DtComp)."</TD>";
						$body .="<TD>".FormatBold(@$episode->getElementsByTagName('actionn3')->item(0)->nodeValue,$DtComp)."</TD>";
						$body .="<TD>".FormatBold(@$episode->getElementsByTagName('actionn5')->item(0)->nodeValue,$DtComp)."</TD>";
						$body .="<TD>".FormatBold(@$episode->getElementsByTagName('actionn6')->item(0)->nodeValue,$DtComp)."</TD>";
						$body .="<TD>".FormatBold(@$episode->getElementsByTagName('actionn7')->item(0)->nodeValue,$DtComp)."</TD>";
						$body .="<TD></TD>";
						$body .="<TD>".FormatBold(@$episode->getElementsByTagName('actionn4')->item(0)->nodeValue,$DtComp)."</TD>";
						$body .="</TR>";
					}
				}
			}
		}
	}
	$body .="</TABLE></table>";	
	
	//echo $body;
		//die("");
		
	$head="";
	
	$message = '
	<html>
	  <head>
	    <title>Log des Actions de SeriesAutoDownload</title>
		'.$head.'
	  </head>
	  <body>
		'.$body.'
	  </body>
	</html>
	';
	
if ($ecrit > 0) { 
	return $message;
} else {
return "";
}
}

function FormatBold($dtaafficher,$DtComp){
	if ($dtaafficher > $DtComp) {
//echo "$dtaafficher > $DtComp";
	   return "<strong>".$dtaafficher."</strong>";

	} else {
	   return $dtaafficher;
	}
}

function OpenEntry($rep,$Entry){
	$myEntryfile = array();
	$myEntryfile["myEntryfile"] =$rep.$Entry.".xml";
	$myEntryfile["Entryxml"]=new DomDocument('1.0');
	
	$createdefault = false;
	
	
	if(file_exists($myEntryfile["myEntryfile"])){
		//echo "file_exists". "</br>\n";
		$myEntryfile["Entryxml"]->preserveWhiteSpace = false;
		$myEntryfile["Entryxml"]->formatOutput = true; 
		$myEntryfile["Entryxml"]->load($myEntryfile["myEntryfile"]);
		if (isset($myEntryfile["Entryxml"]->documentElement)) {
			$myEntryfile["Entryxmlbody"] = $myEntryfile["Entryxml"]->documentElement;
		} else {
			$myEntryfile["Entryxmlbody"] = $myEntryfile["Entryxml"]->appendChild($myEntryfile["Entryxml"]->createElement('body')); 
			$createdefault = true;
		}
	}else{
		$myEntryfile["Entryxml"]->formatOutput = true; 
		$myEntryfile["Entryxmlbody"] = $myEntryfile["Entryxml"]->appendChild($myEntryfile["Entryxml"]->createElement('body')); 
		$createdefault = true;
	}	
	
	if ($createdefault) {
		echo  "creation Default Xml". "</br>\n";
		$ser = new Serie($Entry);
		$ser->rep = get_url_serie($Entry);
		$ser->save($rep);
		$myEntryfile["Entryxmlbody"] = $myEntryfile["Entryxml"]->documentElement;
	}
	
	return $myEntryfile;
}

function MajEntry($myEntryfile,$local,$Entry,$ns,$ne,$dt,$ti,$num,$pod,$NDS,$tabseedv,$magnetv){
	//$myEntryfile = OpenEntry($local,$Entry);
	if (!isset($myEntryfile["Entryxml"]->getElementsByTagName('NumeroDernierSaison')->item(0)->nodeValue)){
		$NumeroDernierSaison = $myEntryfile["Entryxmlbody"]->appendChild($myEntryfile["Entryxml"]->createElement('NumeroDernierSaison'));
	}
	$season = $myEntryfile["Entryxml"]->getElementsByTagName('season');
	foreach($season as $s){
		if ($s->getElementsByTagName('valeur')->item(0)->nodeValue == $ns){
			$ns ="";
			$episode = $s->getElementsByTagName('episode');
			foreach($episode as $e){
				if ($e->getElementsByTagName('valeur')->item(0)->nodeValue == $ne){
					$ne="";
					if (!isset($e->getElementsByTagName('date')->item(0)->nodeValue)){
						$date = $e->appendChild($myEntryfile["Entryxml"]->createElement('date'));
					}
					if (!isset($e->getElementsByTagName('titre')->item(0)->nodeValue)){
						$titre = $e->appendChild($myEntryfile["Entryxml"]->createElement('titre'));
					}
					if (!isset($e->getElementsByTagName('numero')->item(0)->nodeValue)){
						$numero = $e->appendChild($myEntryfile["Entryxml"]->createElement('numero'));
					}
					if (!isset($e->getElementsByTagName('presence')->item(0)->nodeValue)){
						$presence = $e->appendChild($myEntryfile["Entryxml"]->createElement('presence'));
					}
					if (!isset($e->getElementsByTagName('grpmagnet')->item(0)->nodeValue)){
						$grpmagnet = $e->appendChild($myEntryfile["Entryxml"]->createElement('grpmagnet'));
					}
					//** purge de lancienne methode de gestion de magnet/seed
					if (isset($e->getElementsByTagName('seed')->item(0)->nodeValue)){
						remove_node("",$e->getElementsByTagName('seed')->item(0));
					}
					if (isset($e->getElementsByTagName('magnet')->item(0)->nodeValue)){
						remove_node("",$e->getElementsByTagName('magnet')->item(0));
					}
					//**
					break;
				}
			}
			break;
		}
	}
	if ($ns <>""){
		$s = $myEntryfile["Entryxmlbody"]->appendChild($myEntryfile["Entryxml"]->createElement('season')); 
		$valeur = $s->appendChild($myEntryfile["Entryxml"]->createElement('valeur'));
		$valeur->appendChild($myEntryfile["Entryxml"]->createTextNode($ns));	
	}
	if ($ne <>""){
		$e = $s->appendChild($myEntryfile["Entryxml"]->createElement('episode')); 
		$valeur = $e->appendChild($myEntryfile["Entryxml"]->createElement('valeur'));
		$valeur->appendChild($myEntryfile["Entryxml"]->createTextNode($ne));	
		$titre = $e->appendChild($myEntryfile["Entryxml"]->createElement('titre')); 
		$date = $e->appendChild($myEntryfile["Entryxml"]->createElement('date')); 		
		$numero = $e->appendChild($myEntryfile["Entryxml"]->createElement('numero')); 
		$presence = $e->appendChild($myEntryfile["Entryxml"]->createElement('presence')); 
		//$seed = $e->appendChild($myEntryfile["Entryxml"]->createElement('seed'));
		$grpmagnet = $e->appendChild($myEntryfile["Entryxml"]->createElement('grpmagnet')); 
	}
	//echo $ti. "</br>\n";
	$e->getElementsByTagName('titre')->item(0)->nodeValue = $ti; 
	$e->getElementsByTagName('date')->item(0)->nodeValue = $dt; 
	$e->getElementsByTagName('numero')->item(0)->nodeValue = $num; 
	$e->getElementsByTagName('presence')->item(0)->nodeValue = $pod; 
	
	remove_node("",$e->getElementsByTagName('grpmagnet')->item(0));
	$grpmagnet = $e->appendChild($myEntryfile["Entryxml"]->createElement('grpmagnet')); 	
	foreach ($tabseedv as $key => $value) {
		$elemagnet = $grpmagnet->appendChild($myEntryfile["Entryxml"]->createElement('elemagnet')); 		
		$seed = $elemagnet->appendChild($myEntryfile["Entryxml"]->createElement('seed')); 
		$seed->appendChild($myEntryfile["Entryxml"]->createTextNode($key));
		$magnet = $elemagnet->appendChild($myEntryfile["Entryxml"]->createElement('magnet')); 
		$magnet->appendChild($myEntryfile["Entryxml"]->createTextNode($value));
		
		//$e->getElementsByTagName('seed')->item(0)->nodeValue = $key; 
		//$e->getElementsByTagName('seed')->item(0)->nodeValue = $seedv; 
		//remove_node("",$e->getElementsByTagName('magnet')->item(0));
		//$magnet = $e->appendChild($myEntryfile["Entryxml"]->createElement('magnet')); 
		//$magnet->appendChild($myEntryfile["Entryxml"]->createTextNode($value));
		//$magnet->appendChild($myEntryfile["Entryxml"]->createTextNode($magnetv));
	}
	//$e->getElementsByTagName('magnet')->item(0)->textContent = $magnetv; 
	//echo($magnetv)."</br>\n";
	$myEntryfile["Entryxml"]->getElementsByTagName('NumeroDernierSaison')->item(0)->nodeValue = $NDS; 
	
	$myEntryfile["Entryxml"]->save($myEntryfile["myEntryfile"]); 

}


function OpenHashTorrent($rep){
	echo "OpenHashTorrent". "</br>\n";
	$myHashTorrentfile = array();
	$myHashTorrentfile["myHashTorrentfile"] =$rep."HashTorrent.xml";
	$myHashTorrentfile["HashTorrent"]=new DomDocument('1.0');
	
	$createdefault = false;
	if(file_exists($myHashTorrentfile["myHashTorrentfile"])){
		$myHashTorrentfile["HashTorrent"]->preserveWhiteSpace = false;
		$myHashTorrentfile["HashTorrent"]->formatOutput = true; 
		$myHashTorrentfile["HashTorrent"]->load($myHashTorrentfile["myHashTorrentfile"]);
		if (isset($myHashTorrentfile["HashTorrent"]->documentElement)) {
			$myHashTorrentfile["HashTorrentbody"] = $myHashTorrentfile["HashTorrent"]->documentElement;
		} else {
			$myHashTorrentfile["HashTorrentbody"] = $myHashTorrentfile["HashTorrent"]->appendChild($myHashTorrentfile["HashTorrent"]->createElement('body')); 
			$createdefault = true;
		}
	}else{
		$myHashTorrentfile["HashTorrent"]->formatOutput = true; 
		$myHashTorrentfile["HashTorrentbody"] = $myHashTorrentfile["HashTorrent"]->appendChild($myHashTorrentfile["HashTorrent"]->createElement('body')); 
		$createdefault = true;
	}	
	if ($createdefault) {
		echo  "creation Default Xml". "</br>\n";
		$urlrep = $myHashTorrentfile["HashTorrentbody"]->appendChild($myHashTorrentfile["HashTorrent"]->createElement('url')); 
		$urlrep->appendChild($myHashTorrentfile["HashTorrent"]->createTextNode(date("Y-m-d H:i:s")));
		$myHashTorrentfile["HashTorrent"]->save($myHashTorrentfile["myHashTorrentfile"]); 
	}
	return $myHashTorrentfile;
}

function MajHashTorrent($myHashTorrentfile,$Entry,$ns,$ne,$numero,$name,$hash){
	$h = $myHashTorrentfile["HashTorrentbody"]->appendChild($myHashTorrentfile["HashTorrent"]->createElement('HashUnique')); 
	$Entryvaleur = $h->appendChild($myHashTorrentfile["HashTorrent"]->createElement('Entry'));
	$Entryvaleur->appendChild($myHashTorrentfile["HashTorrent"]->createTextNode($Entry));	
	$nsvaleur = $h->appendChild($myHashTorrentfile["HashTorrent"]->createElement('ns'));
	$nsvaleur->appendChild($myHashTorrentfile["HashTorrent"]->createTextNode($ns));	
	$nevaleur = $h->appendChild($myHashTorrentfile["HashTorrent"]->createElement('ne'));
	$nevaleur->appendChild($myHashTorrentfile["HashTorrent"]->createTextNode($ne));	
	$numerovaleur = $h->appendChild($myHashTorrentfile["HashTorrent"]->createElement('numero'));
	$numerovaleur->appendChild($myHashTorrentfile["HashTorrent"]->createTextNode($numero));
	$namevaleur = $h->appendChild($myHashTorrentfile["HashTorrent"]->createElement('name'));
	$namevaleur->appendChild($myHashTorrentfile["HashTorrent"]->createTextNode($name));
	$hashvaleur = $h->appendChild($myHashTorrentfile["HashTorrent"]->createElement('hash'));
	$hashvaleur->appendChild($myHashTorrentfile["HashTorrent"]->createTextNode($hash));	

	$myHashTorrentfile["HashTorrent"]->save($myHashTorrentfile["myHashTorrentfile"]); 
}

function AddFiles($repXmlSeries,$FilePattern,$Determinant,$FileExt){
	$fp = fopen($repXmlSeries.$FilePattern.$Determinant.".".$FileExt, "a");
	fputs ($fp, date("Y-m-d H:i:s"));
	fclose ($fp);
	//sleep(5);
}

function DelFiles($repXmlSeries,$FilePattern,$Determinant,$FileExt){
	@unlink($repXmlSeries.$FilePattern.$Determinant.".".$FileExt);
	//sleep(5);
}	

function CountFiles($repXmlSeries,$FilePattern,$FileExt){
	$nb=0;
	$files = glob($repXmlSeries.$FilePattern.".".$FileExt, GLOB_MARK);
    foreach ($files as $file) {
		//echo $file. "</br>\n";
		$nb+=1;
    }
	return $nb;
}

function AddStatXmlSeries($repXmlSeries,$Entry,$GVListAction,$GVnbepisodestotal,$GVnbepisodespresenttotal,$GVnbepisodesdisponibletotal){	
	$myEntryfile = array();
	$myEntryfile["myEntryfile"] =$repXmlSeries."XmlSeriesStat-".$Entry.".xml";
	$myEntryfile["Entryxml"]=new DomDocument('1.0');
	$myEntryfile["Entryxml"]->formatOutput = true; 
	$myEntryfile["Entryxmlbody"] = $myEntryfile["Entryxml"]->appendChild($myEntryfile["Entryxml"]->createElement('body')); 
	$urlrep1 = $myEntryfile["Entryxmlbody"]->appendChild($myEntryfile["Entryxml"]->createElement('GVListAction')); 
	$urlrep1->appendChild($myEntryfile["Entryxml"]->createTextNode($GVListAction));
	$urlrep2 = $myEntryfile["Entryxmlbody"]->appendChild($myEntryfile["Entryxml"]->createElement('GVnbepisodestotal')); 
	$urlrep2->appendChild($myEntryfile["Entryxml"]->createTextNode($GVnbepisodestotal));
	$urlrep3 = $myEntryfile["Entryxmlbody"]->appendChild($myEntryfile["Entryxml"]->createElement('GVnbepisodespresenttotal')); 
	$urlrep3->appendChild($myEntryfile["Entryxml"]->createTextNode($GVnbepisodespresenttotal));
	$urlrep4 = $myEntryfile["Entryxmlbody"]->appendChild($myEntryfile["Entryxml"]->createElement('GVnbepisodesdisponibletotal')); 
	$urlrep4->appendChild($myEntryfile["Entryxml"]->createTextNode($GVnbepisodesdisponibletotal));
	$myEntryfile["Entryxml"]->save($myEntryfile["myEntryfile"]); 
	//sleep(5);
}

function ResultXmlSeriesToGV($repXmlSeries){
	global $GV;
	//echo $repXmlSeries. "</br>\n";
	$files = glob($repXmlSeries."XmlSeriesStat-"."*".".xml", GLOB_MARK);
    foreach ($files as $file) {
		//echo $file. "</br>\n";

		$myEntryfile = array();
		$myEntryfile["myEntryfile"] =$file;
		$myEntryfile["Entryxml"]=new DomDocument('1.0');
		$myEntryfile["Entryxml"]->preserveWhiteSpace = false;
		$myEntryfile["Entryxml"]->formatOutput = true; 
		$myEntryfile["Entryxml"]->load($myEntryfile["myEntryfile"]);
		$myEntryfile["Entryxmlbody"] = $myEntryfile["Entryxml"]->documentElement;

		$XmlSeriesEntry =$myEntryfile["Entryxmlbody"]->getElementsByTagName('GVListAction');
		foreach ($XmlSeriesEntry as $XmlS) {
			$GV["ListAction"].=$XmlS->nodeValue;
		}
		$XmlSeriesEntry =$myEntryfile["Entryxmlbody"]->getElementsByTagName('GVnbepisodestotal');
		foreach ($XmlSeriesEntry as $XmlS) {
			$GV["nbepisodestotal"]+=$XmlS->nodeValue;
		}
		$XmlSeriesEntry =$myEntryfile["Entryxmlbody"]->getElementsByTagName('GVnbepisodespresenttotal');
		foreach ($XmlSeriesEntry as $XmlS) {
			$GV["nbepisodespresenttotal"]+=$XmlS->nodeValue;
		}
		$XmlSeriesEntry =$myEntryfile["Entryxmlbody"]->getElementsByTagName('GVnbepisodesdisponibletotal');
		foreach ($XmlSeriesEntry as $XmlS) {
			$GV["nbepisodesdisponibletotal"]+=$XmlS->nodeValue;
		}
		
		unlink($file);
		
    }	
}

function deleteDir($dirPath) {
    if (! is_dir($dirPath)) {
        throw new InvalidArgumentException("$dirPath must be a directory");
    }
    if (substr($dirPath, strlen($dirPath) - 1, 1) != '/') {
        $dirPath .= '/';
    }
    $files = glob($dirPath . '*', GLOB_MARK);
    foreach ($files as $file) {
        if (is_dir($file)) {
            deleteDir($file);
        } else {
            unlink($file);
        }
    }
    rmdir($dirPath);
}

function ScanDire($Directory,$table){
	$MyDirectory = opendir($Directory);
//	echo "+".$Directory."</br>\n";
	while($Entry = readdir($MyDirectory)) {
//		echo "-".$Directory."*".$Entry."</br>\n";
		if ($Entry != "." && $Entry != "..") {
			if(is_dir($Directory.$Entry)){
//				echo "repertoire=".$Entry."</br>\n";
				$table=ScanDire("$Directory$Entry/",$table);
			}	else {
//				echo "@".$Directory.$Entry."</br>\n";
				$table[] = $Directory.$Entry;
			}
		}
   	}
	closedir($MyDirectory);
	return $table;
}


function OpenExclusion($rep,$Exclusion="Exclusion"){
	echo "OpenExclusion". "</br>\n";
	$myExclusionfile = array();
	$myExclusionfile["myExclusionfile"] =$rep.$Exclusion.".xml";
	$myExclusionfile["Exclusionxml"]=new DomDocument('1.0');
	
	$createdefault = false;
	if(file_exists($myExclusionfile["myExclusionfile"])){
		//echo "file_exists". "</br>\n";
		$myExclusionfile["Exclusionxml"]->preserveWhiteSpace = false;
		$myExclusionfile["Exclusionxml"]->formatOutput = true; 
		$myExclusionfile["Exclusionxml"]->load($myExclusionfile["myExclusionfile"]);
		if (isset($myExclusionfile["Exclusionxml"]->documentElement)) {
			$myExclusionfile["Exclusionxmlbody"] = $myExclusionfile["Exclusionxml"]->documentElement;
		} else {
			$myExclusionfile["Exclusionxmlbody"] = $myExclusionfile["Exclusionxml"]->appendChild($myExclusionfile["Exclusionxml"]->createElement('body')); 
			$createdefault = true;
		}
	}else{
		$myExclusionfile["Exclusionxml"]->formatOutput = true; 
		$myExclusionfile["Exclusionxmlbody"] = $myExclusionfile["Exclusionxml"]->appendChild($myExclusionfile["Exclusionxml"]->createElement('body')); 
		$createdefault = true;
	}	
	if ($createdefault) {
		echo  "creation Default Xml". "</br>\n";
		$urlrep = $myExclusionfile["Exclusionxmlbody"]->appendChild($myExclusionfile["Exclusionxml"]->createElement('url')); 
		$urlrep->appendChild($myExclusionfile["Exclusionxml"]->createTextNode(date("Y-m-d H:i:s")));
		$myExclusionfile["Exclusionxml"]->save($myExclusionfile["myExclusionfile"]); 
	}
	return $myExclusionfile;
}

function MajExclusion($myExclusionfile,$HashExclusion){
	$HExclusion = $myExclusionfile["Exclusionxml"]->getElementsByTagName('HashExclusion');
	foreach($HExclusion as $h){
		if ($h->getElementsByTagName('valeur')->item(0)->nodeValue == $HashExclusion){
			// hash deja exclu
			return;
		}
	}

	$h = $myExclusionfile["Exclusionxmlbody"]->appendChild($myExclusionfile["Exclusionxml"]->createElement('HashExclusion')); 
	$valeur = $h->appendChild($myExclusionfile["Exclusionxml"]->createElement('valeur'));
	$valeur->appendChild($myExclusionfile["Exclusionxml"]->createTextNode($HashExclusion));	
	$e = $h->appendChild($myExclusionfile["Exclusionxml"]->createElement('date')); 
	$e->appendChild($myExclusionfile["Exclusionxml"]->createTextNode(date("Y-m-d H:i:s")));	
	
	$myExclusionfile["Exclusionxml"]->save($myExclusionfile["myExclusionfile"]); 
}

function IsExclu($myExclusionfile,$HashExclusion){
	$HExclusion = $myExclusionfile["Exclusionxml"]->getElementsByTagName('HashExclusion');
	foreach($HExclusion as $h){
		//echo $h->getElementsByTagName('valeur')->item(0)->nodeValue." == ".$HashExclusion. "</br>\n";
		if (strtolower($h->getElementsByTagName('valeur')->item(0)->nodeValue) == strtolower($HashExclusion)){
			// hash exclu
			return true;
		}
	}
	return false;
}
function constitutionmessage($ns,$ne,$dt,$ti,$num,$pod,$local,$repSerie,$Entry,$typ,$Exclusion,$nbepisodes="",$nbepisodespresent="",$nbepisodesdisponible="",$RepDernieresaison=array(""),$NumeroDernierSaison="",$TorrentMagnet=array("")) {
	$ret =array();
	$ret["ns"]=$ns;
	$ret["ne"]=$ne;
	$ret["dt"]=$dt;
	$ret["ti"]=$ti;
	$ret["num"]=$num;
	$ret["pod"]=$pod;
	$ret["local"]=$local;
	$ret["repSerie"]=$repSerie;
	$ret["Entry"]=$Entry;
	$ret["typ"]=$typ;
	$ret["Exclusion"]=$Exclusion;
	$ret["nbepisodes"]=$nbepisodes;
	$ret["nbepisodespresent"]=$nbepisodespresent;
	$ret["nbepisodesdisponible"]=$nbepisodesdisponible;
	$ret["#RepDernieresaison"]=serialize($RepDernieresaison);
	$ret["NumeroDernierSaison"]=$NumeroDernierSaison;
	$ret["#TorrentMagnet"]=serialize($TorrentMagnet);
	return serialize($ret)."</br>\n";
}
?>
