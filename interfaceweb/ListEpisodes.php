<?php
	// Analyse sans sections
	$config_array = parse_ini_file("config.ini");

	
	//CONNECTION OBJECT
	//This Keeps the Connection to the Databade
	$conn =  mysql_connect($config_array[hostname], $config_array[username], $config_array[password]) or die('Can not connect to database')	;
	mysql_select_db($config_array[dbname], $conn);	

?>

<?php
	if(isset($_POST['Delete'])){
		$clenom = $_POST['updatenom'];
		$query="DELETE FROM series WHERE nom= '".$clenom ."'";
		mysql_query($query);
		echo mysql_error();
	}
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>list episodes</title>
</head>
<body>

<?php

//Create a query
$sql = "SELECT * FROM episodes order by airdate desc";

//submit the query and capture the result

$result = mysql_query($sql, $conn);
echo mysql_error();


?>
<h2>List episodes </h2>


<?php
echo "Nb Row Found = ".mysql_num_rows($result);	
if  (mysql_num_rows($result)>0){
?>
	<table border="0" cellspacing="10">
		<tr>
		<?php 
		for ($i=0; $i<mysql_num_fields($result); $i++) {
			$a = mysql_field_type ( $result , $i );
			echo "<td>";
			echo "(".$a.")</br>".mysql_field_name($result,$i);
			echo"</td>";		
			if (mysql_field_name($result,$i)=="timestamp_completer"){
				$itc=$i;
			}
			if (mysql_field_name($result,$i)=="airdate"){
				$iair=$i;
				$dtnow=date('Y-m-d');
			}
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
			if ($row[$iair]>$dtnow){
				echo "<p style='color:darkblue'>";
			}
			if ($row[$itc]!=""){
				echo "<p style='color:green'>";
			}
			echo  $row[$i];
			if ($row[$itc]!="" || $row[$iair]>$dtnow){
				echo "<p>";
			}
			echo"</td>";
		}
		?>
		<td><INPUT TYPE="Submit" VALUE="Delete the episodes" NAME="Delete"></td>
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