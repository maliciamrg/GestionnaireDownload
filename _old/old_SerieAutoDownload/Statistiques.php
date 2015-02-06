<?php 
	require_once( 'Serie.class.php' );

    $EntrySolo="";
	if (@$_GET['entry']) {
		$EntrySolo = @$_GET['entry'];
	}
	if (isset($argv[1])) {
		$EntrySolo = ($argv[1]);
	} 
	echo "-".$EntrySolo."-". "</br>\n";
	
    $path="";
	if (@$_GET['path']) {
		$path = unserialize(@$_GET['path']);
	}
	if (isset($argv[2])) {
		$path = unserialize(base64_decode($argv[2]));
	} 
	//echo "<pre>"; print_r($path); echo "</pre>";
	
	if ( count($path) >0) {
		//******************************************************************//
		//******************************************************************//
		//*******************Statistique************************************//		
		$msg = "";
		$Bibseries = new BibSeries($path,$EntrySolo);
		$stats = $Bibseries->Stats($EntrySolo);	
		$planseries = $Bibseries->Planseries($EntrySolo);	
		if ($stats["nbtotal"]>0){
			$msg = $stats["mefonerow"] . $planseries;
			//echo($msg). "</br>\n";
			file_put_contents($path[0]."Series"."-".$EntrySolo."-".".html",$msg);
		}
	}
?>