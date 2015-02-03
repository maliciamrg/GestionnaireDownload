<?php
/*
 * Created on 23 oct. 2013
 *
 * To change the template for this generated file go to
 * Window - Preferences - PHPeclipse - PHP - Code Templates
 */

function SendMail ($subject,$mess,$file="",$head="",$bodyend="",$MessageToFile="") {
	// Destinataire
	$to = "Malicia.Mrg@gmail.com";
	// Sujet
	//$subject = 'SerieAutoDownload - Log';
	 
	// Message
	$message = '
	<html>
	  <head>
	    <title>Log des Actions de SeriesAutoDownload</title>
		'.$head.'
	  </head>
	  <body>
	    <table width="100%" border="0" cellspacing="0" cellpadding="5">
	      <tr>
	          <p>
	            '.$mess.'
	          </p>
	      </tr>
	    </table>
		'.$bodyend.'
	  </body>
	</html>
	';
	 
/*	if($MessageToFile!=""){
		file_put_contents($MessageToFile,$message);
		$file=$MessageToFile;
	}
*/		
	if($file==""){
		// Pour envoyer un mail HTML, l en-tête Content-type doit être défini
		$headers = "MIME-Version: 1.0" . "\n";
		$headers .= "Content-type: text/html; charset=utf-8" . "\r\n";
		$body=$message;
	} else {
		$boundary = "_".md5 (uniqid (rand())); 
		$headers = "MIME-Version: 1.0\nContent-Type: multipart/mixed;\n boundary=".$boundary."\n";
		$attached_file = $message.file_get_contents($file); 
		$attached_file = chunk_split(base64_encode($attached_file));
		$attached = "\n\n". "--" .$boundary . "\nContent-Type: application; name=".$file."\r\nContent-Transfer-Encoding: base64\r\nContent-Disposition: attachment; filename=".$file."\r\n\n".$attached_file . "--" . $boundary . "--";
		$body = "--". $boundary ."\nContent-Type: text/html; charset=ISO-8859-1\r\n\n".$message . $attached;
	}
	 
	// En-têtes additionnels
	$headers .= 'From: Mail de HomeServeur <no-reply@daisystreet.fr>' . "\r\n";
	 
	// Envoie
	$resultat = mail($to, $subject, $body, $headers);
/*
	echo  $to."<br />";
	echo  $subject."<br />";
	echo  $message."<br />";
	echo  $headers."<br />";
	if ($resultat){
	echo  "Le mail est envoyer..<br />";
	}*/
	print_r($resultat);

	$array[]=$resultat;
	$array[]=$message;
	return $array;
}

?>

