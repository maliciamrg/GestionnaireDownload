<?php

	$hostname = "localhost";//host name
	$dbname = "seriedownload";//database name
	$username = "seriedownload";//username you use to login to php my admin
	$password = "seriedownload";//password you use to login
	
	//CONNECTION OBJECT
	//This Keeps the Connection to the Databade
	$conn =  mysql_connect($hostname, $username, $password) or die('Can not connect to database')	;
	mysql_select_db($dbname, $conn);	

?>

<?php
	if(isset($_POST['Delete'])){
		$clenom = $_POST['updatenom'];
		$query="DELETE FROM series WHERE nom= '".$clenom ."'";
		mysql_query($query);
		echo mysql_error();
	}

	if(isset($_POST['Update'])){
		$clenom = $_POST['updatenom'];
		$updaterepertoire = $_POST['updaterepertoire'];
		$query="UPDATE series SET repertoire=\"$updaterepertoire\" WHERE nom= \"".$clenom ."\"";
		mysql_query($query);
		echo mysql_error();
	}
	
	if(isset($_POST['Insert'])){
		$clenom = $_POST['updatenom'];
		$updaterepertoire = $_POST['updaterepertoire'];	
		$query="INSERT INTO series (nom,repertoire) VALUES (\"$clenom \", \"$updaterepertoire\" ) ";
		mysql_query($query, $conn) ;
		echo mysql_error();
	}
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>EditBase Series</title>
</head>
<body>

<?php

//Create a query
$sql = "SELECT * FROM series ";

//submit the query and capture the result

$result = mysql_query($sql, $conn);
echo mysql_error();


?>
<h2>Insert Series </h2>

<form action="" method="post">   
<table border="0" cellspacing="10">
<tr>
<td>nom:</td> <td><input type="text" name="updatenom" value=""></td>
</tr>
<tr>
<td>repertoire:</td> <td><input type="text" name="updaterepertoire" value=""></td>
</tr>
<tr>
<td><INPUT TYPE="Submit" VALUE="Insert a Record" NAME="Insert"></td>
</tr>
</table>
</form>

<h2>Update Series </h2>


<?php
echo "Nb Row Found = ".mysql_num_rows($result);	
if  (mysql_num_rows($result)>0){
?>
	<table border="0" cellspacing="10">
		<tr>
		<td>
		<td>nom:</td> 
		</td>
		<td>
		<td>repertoire:</td> 
		</td>
		<td>
		<td>Date Maj Web:</td> 
		</td>
		<td>
		<td></td>
		</td>
		</tr>
	
	<?php	
	while ($row = mysql_fetch_array($result)) {?>	 
		<form action="" method="post">

		<tr>
		<td>
		<td><input type="text" name="updatenom" value="<?php echo $row['nom']; ?>"></td>
		</td>
		<td>
		<td><input type="text" name="updaterepertoire" value="<?php echo $row['repertoire']; ?>"></td>
		</td>
		<td>
		<td><?php echo $row['date_maj_web']; ?></td>
		</td>
		<td>
		<td><INPUT TYPE="Submit" VALUE="Update the Series" NAME="Update"></td>
		<td><INPUT TYPE="Submit" VALUE="Delete the Series" NAME="Delete"></td>
		</td>
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