<?php 
	
const TransmissionStart ="/ffp/start/transmission.sh start";
const TransmissionStop ="/ffp/start/transmission.sh stop";

class Constants {
    public static $repertoire = array(
		"film" => "/mnt/HD/HD_a2/VideoClub/Film A Ranger/",
		"cam" =>"/mnt/HD/HD_a2/ffp/opt/srv/www/download/Film-Cam/"
	);
}

Debut();

Load_include();

$TRPC = ChargementTransmission();

$ret = MoveMovie($TRPC,@$_GET['hash'],@$_GET['mode']);
krumo($ret);

$ret = PurgHash($TRPC,@$_GET['hash']);
krumo($ret);

makeChmod($TRPC);

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
	include '/srv/www/pages/BibPerso/krumo/class.krumo.php';
	require_once( 'TransmissionRPC.class.php' );
}

//*Transmission*//
function ChargementTransmission(){
	echo "-----".__FUNCTION__."-----"."</br>\n"; 
	
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
	
	return $ret;
}

//*purge hash**//
function MoveMovie($TRPC,$hash ="",$mode =""){


	$ret["result"]="";
	
	if ($hash =="") {$ret["result"]="$hash vide";}
	if ($mode =="") {$ret["result"]="$mode vide";}
	
	foreach (@$TRPC["arrayridstatus"]->arguments->torrents as $key => $t ) 
	{
		if (strtolower($t->hashString)==strtolower($hash)){
			$ret["torrents"]= $t;
			
			foreach ($t->files as $f) {
				$s= $TRPC["rpc_download_dir"].$f->name;
				$d= Constants::$repertoire[$mode].$f->name;
				@mkdir(dirname($d), 0777, true);
				@rename($s,$d);
				$ret["move file"]["source"][]=$s;
				$ret["move file"]["destination"][]=$d;
			}	
			
			break;
		}
	}
	return $ret;
}

//*purge hash**//
function PurgHash($TRPC,$hash =""){

	$ret["result"]="";
	
	if ($hash =="") {$ret["result"]="$hash vide";}
	
	foreach (@$TRPC["arrayridstatus"]->arguments->torrents as $key => $t ) 
	{
		if (strtolower($t->hashString)==strtolower($hash)){
			$ret["torrents"]= $t;
			$ret["result"]= $TRPC["TransmissionRpc"]->remove($t->id,true);
			break;
		}
	}
	return $ret;
}

//*droit sur les repertoire**//
function makeChmod($TRPC){

	exec ("find '".Constants::$repertoire["film"]."' -exec chmod 0777 {} +"); 
	exec ("find '".Constants::$repertoire["cam"]."' -exec chmod 0777 {} +"); 
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