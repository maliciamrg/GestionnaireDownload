<?php
/**
 */
require_once( 'Serializer.class.php' );

Class magnet
{
	Private $status;
	Private $magnetlink;
	public $seedscore;
	Private $hash;
	Public $dtajout;
	Public $dtrelance;
	Public $dtcopybloque;
	Public $dtcopystock;
	Public $dtpurge;
	
	Public function magnet ( $magnetlink='' , $seedscore='' )
	{
	 $this->magnetlink = $magnetlink ;
	 $this->seedscore = $seedscore;
	 if (@strpos(" ".$magnetlink,"btih:",1)>0){
		$this->hash = strtolower((new sch($magnetlink,"","btih:","&"))->result ); 
	 } else {
		$this->hash = $magnetlink;
	 }
	}
	
	Public function set_status ($status)
	{
	 $this->status = $status; 
	}	
	Public function set_magnetlink ($magnetlink)
	{
	 $this->magnetlink = $magnetlink; 
	 if (@strpos(" ".$this->magnetlink,"btih:",1)>0){
		$this->hash = strtolower((new sch($this->magnetlink,"","btih:","&"))->result ); 
	 } else {
		$this->hash = $magnetlink;
	 }
	}	
	Public function set_seedscore ($seedscore)
	{
	 $this->seedscore = $seedscore; 
	}	
	Public function set_hash ($hash)
	{
	 //$this->hash = $hash; 
	}	
	Public function set_dtajout ($dtajout)
	{
	 $this->dtajout = $dtajout; 
	}	
	Public function set_dtrelance ($dtrelance)
	{
	 $this->dtrelance = $dtrelance; 
	}	
	Public function set_dtcopybloque ($dtcopybloque)
	{
	 $this->dtcopybloque = $dtcopybloque; 
	}	
	Public function set_dtcopystock ($dtcopystock)
	{
	 $this->dtcopystock = $dtcopystock; 
	}	
	Public function set_dtpurge ($dtpurge)
	{
	 $this->dtpurge = $dtpurge; 
	}	
	
	Public function getmagnetlink ()
	{
	 return $this->magnetlink; 
	}
	Public function gethash ()
	{
	 return $this->hash; 
	}	
	Public function getstatus ()
	{
	 return $this->status; 
	}
	Public function statusOk ()
	{
	 return ($this->status!="KO"); 
	}	
	Public function statusEnc ()
	{
	 return ($this->status=="Enc"); 
	}	
	Public function SetstatusKo ()
	{
	 $this->status ="KO"; 
	}	
	Public function SetstatusOk ()
	{
		//torrent use avec success 
	 $this->status ="OK"; 
	}	
	Public function SetstatusEnc ()
	{
	 $this->status ="Enc"; 
	}	

}

Class Episode
{
	Private $titre;
	public $numid;
	Private $epnum;
	Private $saison;
	/*info epguides*/
	Private $numero;
	Private $airdate;
	/*info disque*/
	Private $presence;
	/*info torrent*/
	public $magnettab = array();
	
	Public function Episode ( $titre ='' , $numid ='' , $saison ='' , $numero ='' ,  $airdate ='' , $epnum ='')
	{
	 $this->titre = $titre ;
	 if ($numid ==''){
		if (is_numeric($numero)) { 
			$numid = sprintf('%1$04d',$saison * 100 + $numero); 
		}else{
			$numid_1 = sprintf('%1$02d',$saison );
			$numid = $numid_1 . $numero;
		} 
	 } 
	 $this->numid = $numid ;
	 $this->epnum = $epnum ;
	 $this->saison = $saison ;
	 $this->numero = $numero ;
	 $this->airdate = $airdate ;
	 $this->addmagnet( new magnet( "",0 ) );
	}
 	public function set_titre ($titre)
	{
	 $this->titre = $titre ;
	}
 	public function set_numid ($numid)
	{
	 $this->numid = $numid ;
	}
 	public function set_epnum ($epnum)
	{
	 $this->epnum = $epnum ;
	}
 	public function set_saison ($saison)
	{
	 $this->saison = $saison ;
	}
 	public function set_numero ($numero)
	{
	 $this->numero = $numero ;
	}
 	public function set_airdate ($airdate)
	{
	 $this->airdate = $airdate ;
	}
 	public function set_presence ($presence)
	{
	 $this->presence = $presence ;
	}
	public function calculpresence($pathtoxml ='' , $nom = ''  , $episodetab = '' ) 
	{
		if ($pathtoxml !='' && $nom != '' && $episodetab != '' ) {
			$this->presence = $this->Is_Present_on_Drive($nom, $pathtoxml.$nom.DIRECTORY_SEPARATOR ,  $this->saison,$this->numero , $episodetab );
		}
		if ($this->presence != 1 ) {
			$this->presence=0;			
			foreach ($this->magnettab as  $key => $magnet ){
				if (is_a($magnet,"magnet")) {
					if ($magnet->statusEnc())
					{
						 $this->presence=-1;
					}
				} else {
					unset($this->magnettab[$key]);
				}
			}
		}
	}
	private function Is_Present_on_Drive($Entry,$local,$ns,$ne , $episodetab) {
		if (is_numeric($ne)){
			$file=(new FormatNom(preg_replace("/[^a-zA-Z0-9. ]/", "", $Entry),(new FormatRepertoire($local,$ns))->result,$ns,$ne."*","*"))->result;
			$list=glob($file);
			//echo $file."</br>\n"; 
			foreach ($list as $filename) {
				//echo "?".$filename."</br>\n"; 
				$ext = pathinfo($filename, PATHINFO_EXTENSION);
				if ((new isvideo($ext))->result) {return true;}
			}
		} else {
				//echo $Entry."-".$ne."</br>\n"; 
				foreach ($episodetab as $episodetabele) {
				if ($episodetabele->get_saison()==$ns){
					//echo $episodetabele->get_saison()."-".$episodetabele->get_numero()."</br>\n"; 
					if (is_numeric($episodetabele->get_numero())){
						$file=(new FormatNom(preg_replace("/[^a-zA-Z0-9. ]/", "", $Entry),(new FormatRepertoire($local,$ns))->result,$episodetabele->get_saison(),$episodetabele->get_numero()."*","*"))->result;
						$list=glob($file);
						//echo $file."</br>\n"; 
						$trouve = false;
						foreach ($list as $filename) {
							//echo "?".$filename."</br>\n"; 
							$ext = pathinfo($filename, PATHINFO_EXTENSION);
							if ((new isvideo($ext))->result) {
								//echo $trouve." = true"."</br>\n"; 
								$trouve = true;
							}
						}
						if (!$trouve){
							//echo "true"."</br>\n"; 
							return false;
						}
					}
				}
			}
			//echo "true"."</br>\n"; 
			return true;
		}
		//echo "false"."</br>\n"; 
		return false;
	}
 	public function set_magnettab ($magnettab)
	{
		if (is_array($magnettab)) {
			foreach ($magnettab as  $magnetele){
		 		$this->magnettab[$magnetele->gethash()] = $magnetele ;
			}
		} else {
			if (!empty($magnettab)){
				$this->magnettab[@$magnettab->gethash()] = $magnettab;
			}
		}
		$this->calculpresence();
	}
	
	public function setpresence ($boolpresence)
	{
	 $this->presence = $boolpresence ;
	}
	Private function setestpresent ()
	{
	 $this->presence = 1 ;
	}
	Private function setestpaspresent ()
	{
	 $this->presence = 0 ;
	}
	public function get_airdate ()
	{
	 return date('l j F Y',strtotime($this->airdate));
	}
	public function get_airdatebrut ()
	{
	 return $this->airdate;
	}
	public function get_saison ()
	{
	 return $this->saison;
	}
	public function get_titre ()
	{
	 return $this->titre;
	}
	public function get_numero ()
	{
	 return $this->numero;
	}
	public function estpresent ()
	{
	 return ($this->presence == 1) ;
	}
	public function estencours ()
	{
		foreach ($this->magnettab as $magnet){
			if ($magnet->statusEnc()) {
				return true;
			}
		}
		return false ;
	}
	public function estadown ()
	{
		foreach ($this->magnettab as $magnet){
			if ($magnet->getmagnetlink()!='')
			{
				return true;
			}
		}
		return false ;
	}
	public function estavenir ()
	{
		if (date("Ymd") > $this->airdate) {
			return false;
		}
		return true;
	}	
	public function estpaspresent ()
	{
	 return($this->presence == 0) ;
	}
	Public function addmagnet ( $magnetele ) 
	{
		//foreach ($this->magnettab as $key => $magnet){
		if (isset($this->magnettab[ $magnetele->gethash() ])){	
			if (is_a($magnetele,"magnet")){
				if (is_a($this->magnettab[ $magnetele->gethash() ],"magnet")){
					//$this->magnettab[$hash] = $magnet;
					$this->magnettab[ $magnetele->gethash() ]->set_magnetlink ( $magnetele->getmagnetlink());
					$this->magnettab[ $magnetele->gethash() ]->seedscore =  $magnetele->seedscore;
					//unset ($this->magnettab[$key]) ;
					//krsort( $this->magnettab);
					return;
				}
			} 
		}
		//}
		//si magnet non existant
		$this->magnettab[ $magnetele->gethash() ] = $magnetele  ;
		//krsort( $this->magnettab);
	} 	

	Public function statusEnc ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						if ($magnet->statusEnc())
						{
							return $magnet->statusEnc();
						}
					}
				}
			}
		}
		return false ;	
	} 	

	Public function sethashok ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						$magnet->SetstatusOk();
						return;
					}
				}
			}
		}
		$this->magnettab[$hash] = new magnet ( $hash ,"99999" ) ;
			
	} 	
	Public function sethashko ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						$magnet->SetstatusKo();
					}
				}
			}
		}
			
	} 	
	Public function sethashenc ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						$magnet->SetstatusEnc();
					}
				}
			}
		}
			
	} 
	Public function sethashVIDE ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						$magnet->set_status("");
					}
				}
			}
		}
			
	} 
	
	Public function ReinitAllHash ( ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					$magnet->set_status("");
				}
			}
		}
			
	} 
	Public function setdtajout ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						$magnet->dtajout=date("Y-m-d H:i:s");
					}
				}
			}
		}
	} 	
	Public function setdtrelance ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						$magnet->dtrelance=date("Y-m-d H:i:s");
					}
				}
			}
		}
	} 	
	Public function setdtcopybloque ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						$magnet->dtcopybloque=date("Y-m-d H:i:s");
					}
				}
			}
		}
	} 	
	Public function setdtcopystock ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						$magnet->dtcopystock=date("Y-m-d H:i:s");
					}
				}
			}
		}
	} 	
	Public function setdtpurge ( $hash ) 
	{
		foreach ($this->magnettab as $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->getmagnetlink()!='')
				{
					if ($hash == $magnet->gethash())
					{
						$magnet->dtpurge=date("Y-m-d H:i:s");
					}
				}
			}
		}
	} 	
 
	
	public function getlistnexttorent ($nom) 
	{
		$this->calculpresence();
		if ($this->presence == 0 ) {
			//echo $nom."-".$this->numid."-".$this->presence."-".count($this->magnettab)."</br>\n"; 
			foreach ($this->magnettab as  $key => $magnet ){
				//echo "<pre>"; print_r($magnet); echo "</pre>";
				if (is_a($magnet,"magnet")){
					if ($magnet->getmagnetlink()!='' && $magnet->statusOk())
					{
						//echo $magnet->gethash()."</br>\n"; 
						$nkey = sprintf('%1$06d',$magnet->seedscore)."-".$magnet->gethash();
						$tb[$nkey]["magnetlink"]=$magnet->getmagnetlink();
						$tb[$nkey]["numid"]=$this->numid;
						$tb[$nkey]["serie"]=$nom;
						$tb[$nkey]["titre"]=$this->titre;
						$tb[$nkey]["hashString"]=$magnet->gethash();
					
						return $tb;
					}
				}
			}
		}		
	}   
	
	public function getallhash ($nom) 
	{
		//$this->calculpresence();
		$allh = array();
		foreach ($this->magnettab as $key => $magnet){
			if (is_a($magnet,"magnet")){
				if ($magnet->gethash()=='') {
					unset($this->magnettab[$key]);
				}else {
					$h = $magnet->gethash();
					$allh[$h]["numid"]=$this->numid;
					$allh[$h]["status"]=$magnet->getstatus();
					$allh[$h]["serie"]=$nom;
					$allh[$h]["presence"]=$this->presence;
					$allh[$h]["hashString"]=$magnet->gethash();
				}
			}
		}
		return $allh;
	} 	
	
	
	public function get_libelle ($nom) 
	{	
		return (new FormatNom(preg_replace("/[^a-zA-Z0-9. ]/", "", $nom),"",$this->saison,$this->numero,$this->titre,""))->result;
	} 
	

}

class Serie
{
  public $epguides;
  public $Exclusion;
  public $nom;
  private $entry; 
  Private $dernieresaison;
  public $episodetab = array();
  Private $url;
  Private $type;
  
  Public function Serie ( $nom='' ,$pathtoxml ='' )
  {
     $this->set_nom($nom);
	 $this->set_type("serie");
	 if ($pathtoxml!=''){
		$this->LoadOldXml($pathtoxml);	
	 }
  }
  
  public function set_Exclusion( $Exclusion)
  {
	$this->Exclusion= $Exclusion;
  }  
  public function set_epguides ( $epguides)
  {
	$this->epguides= $epguides;
  }
  public function set_nom ( $nom )
  {
	$this->nom = $nom ;
	$this->entry = preg_replace("/[^a-zA-Z0-9. -]/", "", $nom);
  }
  public function set_entry ( $entry )
  {
	$this->entry = $entry ;
  }
  public function set_dernieresaison ( $dernieresaison )
  {
	$this->dernieresaison = $dernieresaison ;
  }   
   public function set_episodetab ( $episodetab )
  {
  	if (is_array($episodetab)) {
  		foreach ($episodetab as  $episodeele){
  			$this->episodetab[$episodeele->numid] = $episodeele ;
  		}
  	} else {
		if ($episodetab != '') {
			$this->episodetab[@$episodetab->numid] = $episodetab;
		}
  	}
  }
  public function set_url ( $url )
  {
	$this->url = $url ;
  }
  public function set_type ( $type )
  {
	$this->type = $type ;
  }  
  public function addepisode ( $numid = '',  $titre , $saison , $numero ,  $airdate , $numep = '' )
  {
	if ($numid ==''){
		if (is_numeric($numero)) { 
			$numid = sprintf('%1$04d',$saison * 100 + $numero); 
		}else{
			$numid_1 = sprintf('%1$02d',$saison );
			$numid = $numid_1 . $numero;
		} 
	} 
	if(isset($this->episodetab[$numid])){
		if (is_a($this->episodetab[$numid],"Episode")) {
			$this->episodetab[$numid].Episode( $titre , $numid , $saison , $numero ,  $airdate ,  $numep) ;
		} else {
			unset($this->episodetab[$numid]);
			$this->episodetab[$numid] = new Episode ( $titre , $numid , $saison , $numero ,  $airdate ,  $numep) ;
		}
	} else {
		$this->episodetab[$numid] = new Episode ( $titre , $numid , $saison , $numero ,  $airdate ,  $numep) ;
		if ($this->dernieresaison < $saison){
			$this->dernieresaison = $saison;
		}
	}
	return $numid;
  } 
  
  public function Save ( $path )
  {
  	//echo "-----".__FUNCTION__."-----".$path."-".$this->nom."</br>\n";
	//echo $this->nom."</br>\n";
	$myclassins= new Serializer();
	try {
		//echo "<pre>"; print_r($this); echo "</pre>";
		$Xml= $myclassins->Serialize($this,'Serie');
		//echo "<pre>";echo $Xml."</br>\n"; echo "</pre>";
		$filename=$path.$this->nom.DIRECTORY_SEPARATOR.$this->nom.".xml";
		//echo $filename."</br>\n";
		$myclassins->WriteXmlFile2($Xml,$filename);
	} catch (Exception $e) {
		echo('[ERROR] ' . $e->getMessage() . PHP_EOL . "</br>\n");
		echo "<pre>"; print_r($this); echo "</pre>";
	}
	
  }
  
  public function Stats ()
  {
  	//echo "-----".__FUNCTION__."-----".$this->nom."</br>\n";
	$Statstab=array();
	
	$Statstab["nbtotal"]=0;
	$Statstab["nbeppresent"]=0;
	$Statstab["nbepencours"]=0;
	$Statstab["nbepadown"]=0;
	$Statstab["nbepabsent"]=0;
	$Statstab["nbepavenir"]=0;
	$visu = array();
	foreach ($this->episodetab as  $episodeele){
		if (is_a($episodeele,"Episode")){
			if (is_numeric($episodeele->get_numero ())){
				//echo $episodeele->get_saison ()."-".$episodeele->get_numero ()."</br>\n";
				$Statstab["nbtotal"]++ ;
				$visu[$episodeele->get_saison ()][$episodeele->get_numero ()]="#";
				if ($episodeele->estpresent()){
					$Statstab["nbeppresent"]++ ;
					$visu[$episodeele->get_saison ()][$episodeele->get_numero ()]="X";
				} else {
					if ($episodeele->estencours()){		
						$Statstab["nbepencours"]++ ;
						$visu[$episodeele->get_saison ()][$episodeele->get_numero ()]=">";
					} else { 
						if ($episodeele->estadown()){		
							$Statstab["nbepadown"]++ ;
							$visu[$episodeele->get_saison ()][$episodeele->get_numero ()]="<A HREF=\"http://home.daisy-street.fr/SerieAutoDownload/GestionTorrents.php?argv1=".$this->nom."&argv2=".$episodeele->numid."\">O</A>";
						} else { 
							if ($episodeele->estavenir()){		
								$Statstab["nbepavenir"]++ ;	
								$visu[$episodeele->get_saison ()][$episodeele->get_numero ()]="<span title=\"".$episodeele->get_airdate()."\">_</span>";
							} else {
								$Statstab["nbepabsent"]++ ;
								$visu[$episodeele->get_saison ()][$episodeele->get_numero ()]="-";
							}
						}
					}
				}
			}
		}
	}	
	$Statstab["lienboucle"]="<A HREF=\"http://home.daisy-street.fr/SerieAutoDownload/BoucleXmlSeries.php?argv1=".$this->nom."\">$this->nom</A>";
	$Statstab["schema"] = $this->Miseenforme($visu);
	
	if ($Statstab["nbtotal"]>0) {
		$nb = ($Statstab["nbeppresent"]*100)/$Statstab["nbtotal"];
	} else {
		$nb = 0;
	}
	
	$Statstab["mefonerow"] ="<TR>";
	$Statstab["mefonerow"] .="<TD>".$Statstab["lienboucle"]."</TD>";
	$Statstab["mefonerow"] .="<TD>".$Statstab["nbtotal"]."</TD>";
	$Statstab["mefonerow"] .="<TD>".$Statstab["nbeppresent"]."</TD>";
	$Statstab["mefonerow"] .="<TD>". number_format($nb,2)."%"."</TD>";
	$Statstab["mefonerow"] .="<TD>".$Statstab["nbepencours"]."</TD>";
	$Statstab["mefonerow"] .="<TD>".$Statstab["nbepadown"]."</TD>";
	$Statstab["mefonerow"] .="<TD>".$Statstab["nbepabsent"]."</TD>";
	$Statstab["mefonerow"] .="<TD>".$Statstab["nbepavenir"]."</TD>";
	$Statstab["mefonerow"] .="<TD>".$Statstab["schema"]."</TD>";
	$Statstab["mefonerow"] .="<TR>";

	return $Statstab;
  }  
 	
	private function Miseenforme($visu) {
				// echo "<pre>"; print_r($visu); echo "</pre>";
		ksort($visu);
		$out ="<TABLE BORDER>";
		foreach ($visu as $key => $s) {
			$out .="<tr><td>".sprintf('%1$02d',$key).":";
			
							// echo "<pre>"; print_r($s); echo "</pre>";
			$i=0;
			do {
				$i++;
				$k=sprintf('%1$02d',$i);
				if ($i % 100 == 0) {
					$out .= "</td></tr><tr><td>..:";
				}
				if (isset($s[$k])) {
					$out .=$s[$k];
					unset ($s[$k]);
				} else {
					$out .="?";
				}
								// echo "<pre>"; print_r($s); echo "</pre>";
								// echo $out."</br>\n"; 
								// echo $i."</br>\n"; 
								// echo $k."</br>\n"; 
								// die("");
								
			} while (count($s)> 0);			
			
			$out .= "</td></tr>";	
		}			
		$out .="</table>";
		return $out;
	}
 
  public function getlistnexttorent () 
  {
	$listnt = array();
	foreach ($this->episodetab as $episode){
		$ret = $episode->getlistnexttorent($this->nom);
		if (!empty($ret)){
			$listnt = $listnt + $ret;
		}
	}
	return $listnt;
  }   
  
  public function getallhash () 
  {
	$allh = array();
	foreach ($this->episodetab as $episode){
		if (is_object($episode)){
			//$episode->ReinitAllHash();
			$ret = $episode->getallhash($this->nom);
			if (!empty($ret)){
				$allh = $allh + $ret;
			}
		}
	}
	return $allh;
  } 	
  
/*import old version*/
  Private function LoadOldXml ( $pathtoxml )
  {
   //echo $pathtoxml. "</br>\n";
	$myEntryfile = $this->OpenEntry($pathtoxml.$this->nom.DIRECTORY_SEPARATOR, $this->nom);
	$season = $myEntryfile["Entryxml"]->getElementsByTagName('season');
	foreach ($season as $seasoni) {
		$ns = $seasoni->getElementsByTagName('valeur')->item(0)->nodeValue;
		$episode = $seasoni->getElementsByTagName('episode');
		foreach ($episode as $episodei) {
			$ne = $episodei->getElementsByTagName('valeur')->item(0)->nodeValue;
			$titre = $episodei->getElementsByTagName('titre')->item(0)->nodeValue;
			$airdate = @$episodei->getElementsByTagName('date')->item(0)->nodeValue;
			$numid = sprintf('%1$04d',$ns * 100 + $ne); 
			if($numid != 0 ){
				$this->episodetab[$numid]=new Episode( $titre ,$numid, $ns , $ne ,  $airdate);
				$this->episodetab[$numid]->setpresence( @$episodei->getElementsByTagName('presence')->item(0)->nodeValue);
				$elemagnet = $episodei->getElementsByTagName('elemagnet');
				foreach ($elemagnet as $elemagneti) {
					$seedscore = $elemagneti->getElementsByTagName('seed')->item(0)->nodeValue;
					$this->episodetab[$numid] = new magnet(
						$elemagneti->getElementsByTagName('magnet')->item(0)->nodeValue ,
						$seedscore );
					$this->episodetab[$numid]->magnettab[$seedscore]->dtajout = @$episodei->getElementsByTagName('actionn1')->item(0)->nodeValue;			
					$this->episodetab[$numid]->magnettab[$seedscore]->dtrelance = @$episodei->getElementsByTagName('actionn3')->item(0)->nodeValue;			
					$this->episodetab[$numid]->magnettab[$seedscore]->dtcopystock = @$episodei->getElementsByTagName('actionn6')->item(0)->nodeValue;			
					$this->episodetab[$numid]->magnettab[$seedscore]->dtcopybloque = @$episodei->getElementsByTagName('actionn5')->item(0)->nodeValue;			
					$this->episodetab[$numid]->magnettab[$seedscore]->dtpurge = @$episodei->getElementsByTagName('actionn7')->item(0)->nodeValue;				
				}
				$this->url=@$myEntryfile["Entryxml"]->getElementsByTagName('url')->item(0)->nodeValue;
				if ($this->dernieresaison < $ns){
					$this->dernieresaison = $ns;
				}
			}
		}
	}		
  }
  private function OpenEntry($rep,$Entry)
  {
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
		echo  "creation Default Xml".$rep."-".$Entry. "</br>\n";
//		$urlrep = $myEntryfile["Entryxmlbody"]->appendChild($myEntryfile["Entryxml"]->createElement('url')); 
//		$urlrep->appendChild($myEntryfile["Entryxml"]->createTextNode(get_url_serie($Entry)));
		$myEntryfile["Entryxml"]->save($myEntryfile["myEntryfile"]); 
	}
	return $myEntryfile;
  }
	
	public function copyepisode ($numid , $pathtoepisode , $files , $pathtoxml, $controle = false) 
	{

	//return false;
		
//function Copier($tor,$src,$ns,$ne,$entry,$titre){
//	global $GV;
	
	$retarray["status"]=false;
	$retarray["copyde"] = array ();
	$retarray["files"]=$files;
	$retarray["nom"]=$this->nom;
	$retarray["numid"]=$numid;
	
	if (!is_array(@$files)) { return $retarray;}
	$first = true;

	$retarray["faketorrent"] = true;	
	
	$ns = $this->episodetab[$numid]->get_saison();

	$filecount = 0;
	$filesizemin = 0;
	$filesizetot = 0;
	foreach ($files as $f) {
		$filecount = $filecount + 1 ;
		$filesizetot= $filesizetot + $f->length;
	}	
	$filesizemin = ($filesizetot / $filecount) * 0.75;
	
	$namemax="";
	foreach ($files as $key => $f) {
		
		$retarray["retour files"][$key]= "files-unwanted";
		
		$ext = pathinfo($f->name, PATHINFO_EXTENSION);
		$rena = "";
		//echo "name=".$f->name."</br>\n";
		//echo "length=".$f->length."</br>\n";
		if ($this->isvideoourar($ext)){

			$retarray["faketorrent"] = false;
			
			$namemax = $f->name;
			$flength= $f->length;	

			//echo "$nec-$namemax"."</br>\n";
			if ($flength > $filesizemin){
			
				$retfile = (new AnalyseFileName ( $this->nom , $namemax ))->result;
				
			/*	$namecmp = preg_replace("/[^a-zA-Z0-9]/", " ", $namemax);
				$retfile=array();
				$retfile["namecmp"]=$namecmp;
				
				$retsch = (new sch("??????".basename(strtolower($namecmp)),""," ".strtolower("S".$ns."E"),""))->result;
				$nec = preg_replace("/[^0-9]/", "$",substr($retsch,0,2));
				if (substr($retsch,2,1)=="e"){
					$nec2 = substr($retsch,3,2);
				}
				$partname = ((new sch("??????".basename(strtolower($namecmp)),"","",strtolower("S".$ns."E")))->result);
				$retfile[1]["retsch"]=$retsch;
				$retfile[1]["ns"]=$ns;
				$retfile[1]["nec"]=$nec;
				$retfile[1]["nec2"]=$nec2;
				$retfile[1]["partname"]=$partname;
				//echo "$nec-$partname"."</br>\n";
				if (!is_numeric($nec)){
					$nec = preg_replace("/[^0-9]/", "$",substr((new sch("??????".basename(strtolower($namecmp)),""," ".strtolower(substr($ns."E",1,3)),""))->result,0,2));
					$partname = ((new sch("??????".basename(strtolower($namecmp)),"","",strtolower(substr($ns."E",1,3))))->result);
					$retfile[2]["retsch"]=$retsch;
					$retfile[2]["ns"]=$ns;
					$retfile[2]["nec"]=$nec;
					$retfile[2]["nec2"]=$nec2;
					$retfile[2]["partname"]=$partname;
					//echo "$nec-$partname"."</br>\n";
					if (!is_numeric($nec)){
						$nec = preg_replace("/[^0-9]/", "$",substr((new sch("??????".basename(strtolower($namecmp)),""," ".strtolower($ns),""))->result,0,2));
						$partname = ((new sch("??????".basename(strtolower($namecmp)),"","",strtolower($ns)))->result);
						$retfile[3]["retsch"]=$retsch;
						$retfile[3]["ns"]=$ns;
						$retfile[3]["nec"]=$nec;
						$retfile[3]["nec2"]=$nec2;
						$retfile[3]["partname"]=$partname;
						//echo "$nec-$partname"."</br>\n";
						if (!is_numeric($nec)){
							$nec = preg_replace("/[^0-9]/", "$",substr((new sch("??????".basename(strtolower($namecmp)),""," ".strtolower(sprintf('%1$01d',$ns)),""))->result,0,2));
							$partname = ((new sch("??????".basename(strtolower($namecmp)),"","",strtolower(sprintf('%1$01d',$ns))))->result);
							$retfile[4]["retsch"]=$retsch;
							$retfile[4]["ns"]=$ns;
							$retfile[4]["nec"]=$nec;
							$retfile[4]["nec2"]=$nec2;
							$retfile[4]["partname"]=$partname;
							//echo "$nec-$partname"."</br>\n";
							if (!is_numeric($nec)){
								$nec = preg_replace("/[^0-9]/", "$",substr((new sch("??????".basename(strtolower($namecmp)),""," ".strtolower(sprintf('%1$01d',$ns)),""))->result,1,2));
								$partname = ((new sch("??????".basename(strtolower($namecmp)),"","",strtolower(sprintf('%1$01d',$ns))))->result);	
								$retfile[5]["retsch"]=$retsch;
								$retfile[5]["ns"]=$ns;
								$retfile[5]["nec"]=$nec;
								$retfile[5]["nec2"]=$nec2;
								$retfile[5]["partname"]=$partname;
								//echo "$nec-$partname"."</br>\n";								
							}
						}
					}
				}
				if (!is_numeric($nec2)){
					$nec2 = "$$";
				} */

				
				
				
				
				//echo "$nec-$partname"."</br>\n";

				//echo "<pre>"; print_r($arrayEntry); echo "</pre>";
//die("");
				$retarray["file"][$f->name]=$retfile;
								
				if (is_numeric($retfile["nsc"]) && is_numeric($retfile["nec"]) && $retfile["ctrlnom"] ){// && $retfile["nec"] == $ne ){
				
					if (isset($this->episodetab[ $retfile["numid"] ])){
						
						$controlepresenceglobal= true;
						$controlepresenceglobal = $controlepresenceglobal && $this->episodetab[ $retfile["numid"] ]->estpresent();
						if ($retfile["numid2"]  !="") {
							$controlepresenceglobal = $controlepresenceglobal && $this->episodetab[ $retfile["numid2"] ]->estpresent();
						}
						
						if ($controlepresenceglobal){
							$retarray["file"][$f->name]["est present"] = true;
						} else {
						
							if ($controle) {
								$retarray["retour files"][$key]= "files-wanted";
								$retarray["status"]=true;
						//		return $retarray;
							} else {
								if ($first){
									$retarray["status"] = true;
									$first = false;
								}
								
								$dest = $pathtoxml.(new FormatRepertoire($this->nom.DIRECTORY_SEPARATOR,$retfile["nsc"]))->result;	
								if (!@mkdir($dest)) {};
								chmod($dest,0777);
								

								$s=str_replace('/',DIRECTORY_SEPARATOR ,$pathtoepisode.$namemax);
								
								$numid = $retfile["numid"];
								$rena = (new FormatNom($this->nom,"",$retfile["nsc"],$retfile["nec"],$this->episodetab[$numid]->get_titre(),""))->result;
								$d=str_replace('/',DIRECTORY_SEPARATOR ,$dest.preg_replace("/[^a-zA-Z0-9. ]/", "", $rena).".".$ext);	
								if (is_numeric($retfile["nec2"]) && $retfile["nec2"] != "00" ){
									$numid2 = sprintf('%1$04d',$retfile["nsc"] * 100 + $retfile["nec2"]);
									$rena2 = (new FormatNom($this->nom,"",$retfile["nsc"],$retfile["nec2"],$this->episodetab[$numid2]->get_titre(),""))->result;
									$d2=str_replace('/',DIRECTORY_SEPARATOR ,$dest.preg_replace("/[^a-zA-Z0-9. ]/", "", $rena2).".".$ext);	
								}
								
								if ($ext=="rar"){
									$repunrar =$pathtoepisode."Unrar-".rand(0, 10000).DIRECTORY_SEPARATOR;
									sleep(5);
									echo "$repunrar"."</br>\n";
									mkdir ($repunrar);
									//@chdir($repunrar);
									if (is_dir($repunrar)) {
										echo "exist = $repunrar"."</br>\n";
									}
									if (is_file ($s)) {
										echo "exist = $s"."</br>\n";
									}
									$SpecialAction="/ffp/bin/nohup unrar e -y -ad \"$s\" \"$repunrar\" 0>/dev/null >>'".$pathtoepisode.$rena."-Unrar".".Log'";
									//print $SpecialAction."</br>\n";
									$result = shell_exec($SpecialAction);
									echo $result;
									//$result=`$SpecialAction`;
									//chdir ($GV["repbase"]);
									
									$ret = $this->ScanDire($repunrar,"");
									echo "<pre>"; print_r($ret); echo "</pre>";
									$ntor = array();
									$count =0 ;
									foreach ($ret as $filename) {
										$ntor[$count]->name = @$filename;
										$ntor[$count]->length = filesize($filename);
										$count=$count+1;
									}
							// $pathtoepisode
									$ret2 = $this->copyepisode($numid , "" , $ntor , $pathtoxml);
									//$ret2 = deplacerresultat("",$pathtoepisode,$ntor , $pathtoxml);
									$retarray["status"] =  ($retarray["status"] && $ret2["status"]);
									$retarray["copyde"] =  $retarray["copyde"] + $ret2["copyde"];
									$retarray["file"]   =  $retarray["file"]   + $ret2["file"];
									$this->deleteDir($repunrar);
									sleep(5);				
									
								} else {
							
							//		echo $retsch."</br>\n";
						//die("");
									echo "=== copy ======================="."</br>\n";
									echo $s."</br>\n";
									echo $d."</br>\n";
									if (is_numeric($retfile["nec2"]) && $retfile["nec2"] != "00" ){echo $d2."</br>\n";}			
									
									$retarray["status"] = ($retarray["status"] && true);
									$retarray["status"] = ($retarray["status"] && copy($s,$d));
									if (is_numeric($retfile["nec2"]) && $retfile["nec2"] != "00" ){$retarray["status"] = ($retarray["status"] && copy($s,$d2));}			

									if ($retarray["status"]){
										$retcpy ["ns"]=$retfile["nsc"];
										$retcpy ["ne"]=$retfile["nec"];
										$retcpy ["numid"]=$retfile["numid"];
										$retcpy ["nsd"]=$this->dernieresaison;
										$retcpy ["source"]=$s;
										$retcpy ["dest"]=$d;
										$retarray["copyde"][] = $retcpy;
										if (is_numeric($retfile["nec2"]) && $retfile["nec2"] != "00" ){
											$retcpy ["ns"]=$retfile["nsc"];
											$retcpy ["ne"]=$retfile["nec2"];
											$retcpy ["numid"]=$numid2;
											$retcpy ["nsd"]=$this->dernieresaison;
											$retcpy ["source"]=$s;
											$retcpy ["dest"]=$d2;
											$retarray["copyde"][] = $retcpy;
										}			
									}
								}
							}
						}
						
						
						
					}
				}
			}
		}		
	}
	
	// echo "retarray=>";
	// echo "<pre>"; print_r($retarray); echo "</pre>";
	// echo "</br>\n";
	//constitutuion tableau de param files
	foreach ($retarray["retour files"] as $key => $elew) {
		if ($elew == "files-wanted"){
			$retarray["files-wanted"][]=$key;
		}
		if ($elew == "files-unwanted"){
			$retarray["files-unwanted"][]=$key;
		}
	}
	
	return $retarray;
	

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
            $this->deleteDir($file);
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
					$table=$this->ScanDire("$Directory$Entry/",$table);
				}	else {
	//				echo "@".$Directory.$Entry."</br>\n";
					$table[] = $Directory.$Entry;
				}
			}
		}
		closedir($MyDirectory);
		return $table;
	}	

	function isvideoourar($ext){
		if((new isvideo($ext))->result){
			return true;
		} else {
			if(strrpos("-rar",$ext)>0){
				return true;
			} else {
				return false;
			}
		}
	}
	
	//* retourne un tableau des prochaines airdate des episodes de la series*//
	//* superieur ou egal a la date transmise*//
	//* cle    (libelle episode)	*//
	//* valeur (airdate)            *//
	public function Planseries ( $dt ) {
		$ret = array();
		foreach ($this->episodetab as $episode){
			$adt = $episode->get_airdatebrut();
			if ($adt>=$dt) {
				$lep = $episode->get_libelle($this->nom);
				$ret[$lep]= $adt;
			}
		}
		return $ret;
	}
	
}

Class BibSeries
{
	public $pathtoxml = array();
	public $serie = array();
	Private $listnexttorent = array();
	Private $allhash = array();

	
	public function BibSeries ( $tabpathtoxml , $EntrySolo ="" , $method =0)
	{
		foreach ($tabpathtoxml as $pathtoxml){	
			echo "-----".__FUNCTION__."-----".$pathtoxml."</br>\n"; 
			$MyDirectory = opendir($pathtoxml) or die('Erreur opendir');	
			while($Entry = @readdir($MyDirectory)) {			
				if( is_dir($pathtoxml.$Entry.DIRECTORY_SEPARATOR) && $Entry != '.' && $Entry != '..') {
				//echo "$Entry == $EntrySolo"."</br>\n";;
					if(( strtolower($EntrySolo) == strtolower($Entry) ) || ( $EntrySolo=="" )) {
					//echo "$Entry == $EntrySolo"."</br>\n";;
						if ( $method==1){
							$this->serie[$Entry] = new Serie($Entry,$pathtoxml);
							$this->pathtoxml[$Entry]=$pathtoxml;
						} else {
							$myclassins= new Serializer();
							$filename=$pathtoxml.$Entry.DIRECTORY_SEPARATOR.$Entry.".xml";
							if (is_file ($filename)) {
								$imp = $myclassins->DeserializeClass($filename); 
								if ( is_a($imp,"Serie")){
									$this->serie[$Entry]=$imp;
									$this->pathtoxml[$Entry]=$pathtoxml;
								}
							} else {
								$this->serie[$Entry] = new Serie($Entry);
								$this->pathtoxml[$Entry]=$pathtoxml;
							}
						}
						$this->serie[$Entry]->nom = $Entry;
						//echo "<pre>"; print_r($this->serie); echo "</pre>";
						$this->serie[$Entry]->set_type("serie");
						if (@strpos(strtolower(" ".$pathtoxml),"anime",1)>0){
							$this->serie[$Entry]->set_type("anime");
						} 
					}
				}
			}
			closedir($MyDirectory);
		}
		//$this->pathtoxml=$pathtoxml;
		$this->getallmagnet = false;
		echo "<pre>"; print_r($this->pathtoxml); echo "</pre>";
		//die("");
	}
	
	public function Stats ($EntrySolo="")
	{
		$Statstab=array();

		$Statstab["nbtotal"]=0;
		$Statstab["nbeppresent"]=0;
		$Statstab["nbepencours"]=0;
		$Statstab["nbepadown"]=0;
		$Statstab["nbepabsent"]=0;
		$Statstab["nbepavenir"]=0;
		$Statstab["mefonerow"]="<TABLE BORDER>";
		$Statstab["mefonerow"].=	"<TR><TD>".date("Y-m-d H:i:s")."</TD><TR>";
		$Statstab["mefonerow"].=	"<TR><TD>Serie</TD><TD>Total</TD><TD>Present</TD><TD>% complet</TD><TD>En cours</TD><TD>A down</TD><TD>Absent</TD><TD>A venir</TD><TD></TD><TR>";
		
		ksort($this->serie);
		
		foreach ($this->serie as $ser){
			if (is_a($ser,"Serie")) {
				if  ( $EntrySolo == $ser->nom || $EntrySolo=="" ) {

					$st = $ser->Stats();
					//echo $ser->nom."</br>\n";
					//echo "<pre>"; print_r($st); echo "</pre>";
					$Statstab["nbtotal"]=$Statstab["nbtotal"]+$st["nbtotal"] ;
					$Statstab["nbeppresent"]=$Statstab["nbeppresent"]+$st["nbeppresent"] ;
					$Statstab["nbepencours"]=$Statstab["nbepencours"]+$st["nbepencours"] ;
					$Statstab["nbepadown"]=$Statstab["nbepadown"]+$st["nbepadown"] ;
					$Statstab["nbepabsent"] +=$st["nbepabsent"] ;
					$Statstab["nbepavenir"] +=$st["nbepavenir"] ;	
					$Statstab["mefonerow"] .= $st["mefonerow"] ;				
					
				}
			}
		}
		
		$Statstab["mefonerow"].="</TABLE>";
		
		return $Statstab;
	}  
  
	public function getlistnexttorent () 
	  {
		$listnt = array();
		foreach ($this->serie as $ser){
			if (is_a($ser,"Serie")) {
				$listnt = $listnt + $ser->getlistnexttorent();
			}
		}
		krsort($listnt);
		return $listnt;
	  }
	  
    public function getallhash () 
	  {
		$allh = array();
		foreach ($this->serie as $ser){
			if (is_a($ser,"Serie")) {
				$allh = $allh + $ser->getallhash();
			}
		}
		return $allh;
	  }
	  public function SaveBib ($EntrySolo="")
	  {
	  	//echo "-----".__FUNCTION__."-----"."</br>\n"; 
		foreach ($this->serie as $key => $ser){
			//echo  "key = $key - Seriesnom = ".$ser->nom." - EntrySolo  = $EntrySolo </br>\n"; 
			if (is_a($ser,"Serie")) {
				//echo  "EntrySolo  = $EntrySolo </br>\n"; 
				if (( strtolower($EntrySolo) == strtolower($ser->nom) ) || ( $EntrySolo=="" )) {
					Echo "Sauvegarde ".$ser->nom." ici ".$this->pathtoxml[$key]."</br>\n"; 
					$ser->Save($this->pathtoxml[$key]);
				}
			}
		}
	}	  
	
	public function Planseries ($EntrySolo="" , $dt = "")
	{
		if ($dt==""){$dt = date("Ymd");}
		$planseries="";

		$planseries="<TABLE BORDER>";
		$planseries.=	"<TR><TD>".date("Y-m-d H:i:s")."</TD><TR>";
		$planseries.=	"<TR><TD>Date</TD><TD>Episode</TD><TR>";
		
		ksort($this->serie);
		$tabps = array();
		foreach ($this->serie as $ser){
			if (is_a($ser,"Serie")) {
				if  ( $EntrySolo == $ser->nom || $EntrySolo=="" ) {
					$tabps = $tabps + $ser->Planseries($dt);							
				}
			}
		}
		
		asort($tabps);
		
		$tabpseleprev ="";
		$epaff = "";
		foreach ($tabps as $key => $tabpsele){
			if ($epaff !=""){$epaff .= "</br>\n" ;}
			$epaff .= $key;
			if ($tabpsele != $tabpseleprev ) {
				$planseries.=	"<TR><TD>".date('l j F Y',strtotime($tabpsele))."</TD><TD>".$epaff."</TD><TR>";
				$epaff = "";
			}
			$tabpseleprev = $tabpsele;
		}
		if ($epaff != ""){
			$planseries.=	"<TR><TD>".date('l j F Y',strtotime($tabpsele))."</TD><TD>".$epaff."</TD><TR>";
		}
		
		$planseries.="</TABLE>";
		
		return $planseries;
	}  
}

class sch 
{	  
	public $result;
	/*fonctions*/
  public function sch($Str,$dep="",$deb,$fin="") {
	//echo "-".$dep."-".$deb."-".$fin."-";
		if  ($dep == "" ) {
			$cSe = 0;
		} else {
			$cSe = @strpos($Str,$dep,1);
		}
	//	echo "*".$cSe."*";
		if  ($dep <> "" && $cSe == "") {
			$this->result = "";
		} else {
	//		$cSe =$cSe + strlen($dep);
		if  ($deb == "" ) {
			$dSe = 0;
		} else {
			$dSe = @strpos($Str,$deb,$cSe+1);
		}
	//echo "*".$dSe."*";
			if ($dSe == "" && $dSe != "0") {
				$this->result = "";
			} else {
				$dSe = $dSe + strlen($deb);
			}
			if ($fin<>""){
				$d2Se = @strpos($Str,$fin,$dSe);
			} else {
				$d2Se = "";
			}
	//echo "*".$d2Se."*";
			if ($d2Se != "") {
				$this->result = substr($Str,$dSe,$d2Se-$dSe);
			} else {
				$this->result = substr($Str,$dSe);
			}

		}
	}
}
class FormatNom 
{	  
	public $result;
	/*fonctions*/
	public function FormatNom($NomSerie, $Repertoire,$Ns,$Ne,$TitreEp,$Ext='*') {
		if ($Ns=="") {
			$Ss = "";
		} else {
			$Ss = "S".$Ns;
			//$Repertoire=$Repertoire."Season".$Ns.DIRECTORY_SEPARATOR;
		}
		if ($TitreEp=="*") {
			$ti = "";
		} else {
		if ($TitreEp!="") {
			$ti =".".$TitreEp;
	} else {$ti="";}
		}
		if ($NomSerie !="") {
		 $NomSerie .= ".";
		} 
		if ($Ext !="") {
		 $Ext = ".".$Ext;
		} 
		$this->result =  $Repertoire.$NomSerie.$Ss."E".$Ne.$ti.$Ext;
	}	
}
class FormatRepertoire 
{	 
	public $result;
	public function FormatRepertoire($Repertoire,$Ns) {
		if ($Ns=="") {
			$this->result =  $Repertoire;
		} else {
			$this->result =  $Repertoire."Season".$Ns.DIRECTORY_SEPARATOR;
		}
	}
}
class isvideo 
{	
	public $result;
	public function isvideo($ext){
		if(strrpos("-mkvmpgmp4aviwmvdivx",$ext)>0){
			$this->result =   true;
		} else {
			$this->result =   false;
		}
	}
}
class AnalyseFileName 
{	
	public $result;
	function AnalyseFileName ( $SerieName , $FileName ) {
	
		$nsc = "$$";
		$nec = "$$";
		$nec2 = "$$";
		$partname = "$$";
	
		$this->result=array();
		$this->result["namecmp"]=basename($FileName);		
		
		$n =  0; 
		
		if ($nsc == "$$") {
			$n++;
			preg_match('/([Ss]eason[ ]*|[Ss]|[Ss][Nn])([0-9]{1,2})[ x._-]*([Ee]pisode[ ]*|[Ee]|[Ee][Pp])([0-9]{0,2})[ Ee&x._-]*([0-9]{0,2})[ ._-]/',$this->result["namecmp"],$this->result[$n],PREG_OFFSET_CAPTURE);
			if (count($this->result[$n])>0) {
				$partname = substr($this->result["namecmp"] , 0, $this->result[$n][0][1]-1);
				$nsc = sprintf('%1$02d',$this->result[$n][2][0]);
				$nec = sprintf('%1$02d',$this->result[$n][4][0]);
				if (is_numeric($this->result[$n][5][0])){
					$nec2 = sprintf('%1$02d',$this->result[$n][5][0]);
				}
			}
		}
		
		if ($nsc == "$$") {
			$n++;
			preg_match('/[\\/\._ \[\(-]([0-9]+)x([0-9]+)/',$this->result["namecmp"],$this->result[$n],PREG_OFFSET_CAPTURE);
			if (count($this->result[$n])>0) {
				$partname = substr($this->result["namecmp"] , 0, $this->result[$n][0][1]-1);
				$nsc = sprintf('%1$02d',$this->result[$n][1][0]);
				$nec = sprintf('%1$02d',$this->result[$n][2][0]);
				$nec2 = "$$";
			}
		}
		
		if ($nsc == "$$") {
			$n++;
			preg_match('/[\\/\._ -]([0-9]+)([0-9][0-9])/',$this->result["namecmp"],$this->result[$n],PREG_OFFSET_CAPTURE);
			if (count($this->result[$n])>0) {
				$partname = substr($this->result["namecmp"] , 0, $this->result[$n][0][1]);
				$nsc = sprintf('%1$02d',$this->result[$n][1][0]);
				$nec = sprintf('%1$02d',$this->result[$n][2][0]);
				$nec2 = "$$";
			}
		}
		
		$ctrlnom = true;
		$thisnom=$SerieName;
		$thisnom=preg_replace("~\(([^\[]+)\)~","",strtolower($thisnom));
		$thisnom=preg_replace("~\[([^\[]+)\]~","",strtolower($thisnom));
		$arrayEntry=preg_split("~[s,'._() ]+~",strtolower($thisnom));
		foreach ($arrayEntry as $mot){
			if (strlen ($mot)>1){
				//echo $this->nom ."-".$mot."-". stripos($partname,$mot)."</br>\n";
				if (!stripos(" ".$partname,$mot)){
					$this->result["mauvais_nom__".$mot] = $partname;
					$this->result["arrayEntry"] = $arrayEntry;
					$retarray["faketorrent"] = true;
					$ctrlnom = false;
				}
			}
		}
		
		$this->result["partname"]=$partname ;
		$this->result["nsc"]=$nsc ;
		$this->result["nec"]=$nec ;
		$this->result["nec2"]=$nec2;
		$this->result["ctrlnom"]=$ctrlnom;
		$this->result["numid"]= sprintf('%1$04d',$nsc * 100 + $nec); 
		if (is_numeric($nec2) && $nec2 != "00" ){
			$this->result["numid2"]=sprintf('%1$04d',$nsc * 100 + $nec2);
		} else { 
			$this->result["numid2"]= "" ; 
		}
	}
}	
?>

