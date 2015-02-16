<?php

	$hostname = "192.168.1.120";//host name
	$dbname = "seriedownload";//database name
	$username = "seriedownload";//username you use to login to php my admin
	$password = "seriedownload";//password you use to login
	
	//CONNECTION OBJECT
	//This Keeps the Connection to the Databade
	$conn =  mysql_connect($hostname, $username, $password) or die('Can not connect to database')	;
	mysql_select_db($dbname, $conn);	

?>

<?php

	if(isset($_POST['purge'])){
		$hash = $_POST['hash'];
		$query="DELETE FROM hash WHERE hash= \"".$hash."\"";
		mysql_query($query);
		echo mysql_error();
	}
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Film a purger (timeout)</title>
</head>
<body>

<?php

//Create a query
$sql = "SELECT * FROM hash WHERE  classification =\"filemaeffacer\" and isnull(timestamp_termine) ";

//submit the query and capture the result

$result = mysql_query($sql, $conn);
echo mysql_error();


?>
<h2>List Film a purger (timeout) </h2>


<?php
echo "Nb Row Found = ".mysql_num_rows($result);	
if  (mysql_num_rows($result)>0){
?>
	<table border="0" cellspacing="10">
		<tr>
		<?php 
		for ($i=0; $i<mysql_num_fields($result); $i++) {
			echo "<td>";
			echo mysql_field_name($result,$i);
			echo"</td>";
		}
		?>
		</tr>
	
	<?php	
	while ($row = mysql_fetch_array($result)) {?>	 
		<form action="" method="post">

		<tr>
		<?php 
		for ($i=0; $i<mysql_num_fields($result); $i++) {
			echo "<td>";
			echo "<textarea readonly name=\"".mysql_field_name($result,$i)."\">";
			echo  $row[$i];
			echo "</textarea>";
			echo"</td>";
		}
		?>
		<td><INPUT TYPE="Submit" VALUE="Purge film" NAME="purge"></td>
		</tr>
			
		</form>
<?php
	}?>
	</table>
<?php
}
	?>

</body>
</html>