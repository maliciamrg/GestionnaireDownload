<?php
	// Analyse sans sections
	$config_array = parse_ini_file("config.ini");

	
	//CONNECTION OBJECT
	//This Keeps the Connection to the Databade
	$conn =  mysql_connect($config_array[hostname], $config_array[username], $config_array[password]) or die('Can not connect to database')	;
	mysql_select_db($config_array[dbname], $conn);	

?>

<?php

	if(isset($_POST['serie'])){
		$hash = $_POST['hash'];
		$query="update hash set classification =\"serie\" WHERE hash= \"".$hash."\"";
		mysql_query($query);
		echo mysql_error();
	}
	if(isset($_POST['film'])){
		$hash = $_POST['hash'];
		$query="update hash set classification =\"film\" WHERE hash= \"".$hash."\"";
		mysql_query($query);
		echo mysql_error();
	}
	if(isset($_POST['effacer'])){
		$hash = $_POST['hash'];
		$query="update hash set classification =\"effacer\" WHERE hash= \"".$hash."\"";
		mysql_query($query);
		echo mysql_error();
	}
	if(isset($_POST['ignorer'])){
		$hash = $_POST['hash'];
		$query="update hash set classification =\"ignorer\" WHERE hash= \"".$hash."\"";
		mysql_query($query);
		echo mysql_error();
	}
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>hash a classer</title>
</head>
<body>

<?php

//Create a query
$sql = "SELECT * FROM hash WHERE  classification =\"autres\" ";

//submit the query and capture the result

$result = mysql_query($sql, $conn);
echo mysql_error();


?>
<h2>hash a classer </h2>


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
		<td><INPUT TYPE="Submit" VALUE="serie" NAME="serie"></td>
		<td><INPUT TYPE="Submit" VALUE="film" NAME="film"></td>
		<td><INPUT TYPE="Submit" VALUE="effacer" NAME="effacer"></td>
		<td><INPUT TYPE="Submit" VALUE="ignorer" NAME="ignorer"></td>
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