<?php
//http://trac.opensubtitles.org/projects/opensubtitles/wiki/XMLRPC
//http://trac.opensubtitles.org/projects/opensubtitles/wiki/XmlRpcIntro

include 'xmlrpc.inc';
include 'FileHash.php';

const path="/mnt/HD/HD_b2/VideoClubSeries/Serie/24/Season01/";

// Make an object to represent our server.
$server = new xmlrpc_client('/xml-rpc',
                            'api.opensubtitles.org', 80);	   
$message = new xmlrpcmsg('LogIn', 
                       array(new xmlrpcval("", ''),
                             new xmlrpcval("", ''),
							 new xmlrpcval("", ''),
							 new xmlrpcval("lm v2.0", '')));
$result = $server->send($message);
$token = @$result->val->me['struct']['token']->me['string'];
$n =0;
$hashArry = array();
foreach(glob(path."*.avi") as $v){ 
	$hashArry[]  = new xmlrpcval(OpenSubtitlesHash($v), 'string');
	$n ++;
}
foreach(glob(path."*.mkv") as $v){ 
	$hashArry[]  = new xmlrpcval(OpenSubtitlesHash($v), 'string');
	$n ++;
	if ($n>3) {break;}
}
//foreach(glob(path."*.mp4") as $v){ 
//	$hashArry[]  = new xmlrpcval(OpenSubtitlesHash($v), 'string');
//}
echo "<pre>"; print_r($hashArry); echo "</pre>";
//die("");
$message = new xmlrpcmsg('CheckMovieHash', 
                       array(new xmlrpcval($token, 'string'),new xmlrpcval($hashArry, 'array')) );
$result = $server->send($message);
echo "<pre>"; print_r($result); echo "</pre>";


// Process the response.
if (!$result) {
    print "<p>Could not connect to HTTP server.</p>";
} elseif ($result->faultCode()) {
    print "<p>XML-RPC Fault #" . $result->faultCode() . ": " . $result->faultString();
} else {
	echo "<pre>"; print_r($result); echo "</pre>";
}
?>
