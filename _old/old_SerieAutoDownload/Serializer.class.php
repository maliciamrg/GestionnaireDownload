<?php 

//$myclassins= new Serializer();
//$Xml= $myclassins->Serialize($this,'this_class');
//$filename=$pathtofoo.".xml";;
//$myclassins->WriteXmlFile2($Xml,$filename);

//$myclassins= new Serializer();
//$filename=$pathtofoo.".xml";
//$foo=$myclassins->DeserializeClass($filename); 


class Serializer
{
    private static $Data;

    private function GetaArray($arrayValue)
    {
            foreach ($arrayValue as $Member)
            {
				if (is_object($Member)){
                    $this->SerializeClass($Member,get_class($Member));
				}
            }
    }
    public function Serialize($ObjectInstance,$ClassName)
    {
             Serializer::$Data="<Root>";
             $this->SerializeClass($ObjectInstance,$ClassName);
             Serializer::$Data.="</Root>";
             return Serializer::$Data;

    }
    public  function SerializeClass($ObjectInstance,$ClassName)
    {
		//echo "ClassName". "</br>\n";
		//echo "-$ClassName-". "</br>\n"; 

        Serializer::$Data.="<".$ClassName.">";
        $Class=new ReflectionClass($ClassName);
        $ClassArray= ((array)$ObjectInstance);
        $Properties=$Class->getProperties();
		
		//echo "ClassArray". "</br>\n";
		//echo "<pre>"; print_r($ClassArray); echo "</pre>";
		
        $i=0;
        foreach ($ClassArray as $ClassMember)
        {
			$prpName="";
			$prpName= @$Properties[$i]->getName();
			if ($prpName!=""){
				Serializer::$Data.="<".$prpName.">";
				$prpType= gettype($ClassMember);


				if ($prpType=='object')
				{
					$serializerinstance= new Serializer();
					$serializerinstance->SerializeClass($ClassMember,get_class($ClassMember));
				}
				if ($prpType=='array')
				{
						$this->GetaArray($ClassMember);
				}
				else
				{
					Serializer::$Data.=htmlspecialchars($ClassMember);
				}
				Serializer::$Data.="</".$prpName.">";
			}
            $i++;
        }
    Serializer::$Data.="</".$ClassName.">";
    return Serializer::$Data;
    }

    public function WriteXmlFile($XmlData,$FilePath){
        $Xml = simplexml_load_string($XmlData);
        $Doc=new DOMDocument();
		//$Doc->preserveWhiteSpace = false;
		//$Doc->formatOutput = true;
        $Doc->loadXML($Xml->asXML());
        $Doc->save($FilePath);
    }
	public function WriteXmlFile2($XmlData,$FilePath){
		$myEntryfile = array();
		$myEntryfile["Entryxml"]=new DomDocument('1.0');
		$myEntryfile["Entryxml"]->preserveWhiteSpace = false;
		$myEntryfile["Entryxml"]->formatOutput = true; 
		$myEntryfile["Entryxml"]->loadXML($XmlData);
		$myEntryfile["Entryxml"]->save($FilePath); 
	}
    public function DeserializeClass($FilePath)
    {
        $Xml=simplexml_load_file($FilePath);
		return $this->Deserialize($Xml);
		/*$Xml=new DomDocument('1.0');
		$Xml->preserveWhiteSpace = false;
		$Xml->formatOutput = true; 
		$Xml->load($FilePath);		
		//echo "<pre>"; print_r($Xml); echo "</pre>";die("");
        return $this->Deserialize($Xml);*/
    }
    public function Deserialize($Root)
        {
		
			$result=null;
			$counter=0;
			$counternull=null;
			foreach ($Root as $member)
			{		
					try {			
						$instance = new ReflectionClass($member->getName());
						$ins=$instance->newInstance();
						foreach ($member as $child){
							$rp=$instance->getMethod("set_".$child->getName());
							if (count($child->children())==0){
								$rp->invoke($ins,(string) $child);
							} else {
								$rp->invoke($ins,$this->Deserialize($child->children()));
								//echo $child;
							}
						}
						if (count($Root)==1) {
								return $ins;
						} else {
							$result[$counter]=$ins;
							$counter++;
						}
					} catch (Exception $e) {
						echo('[ERROR] ' . $e->getMessage() . PHP_EOL . "</br>\n");
						//echo "<pre>"; print_r($member); echo "</pre>";
						$result[$counter]=null;
						$counternull[]=$counter;
						$counter++;
					}
					if ($counter==count($Root)) {
							//echo "<pre>"; print_r($result); echo "</pre>";
							if (count($counternull)>0) {
								foreach ($counternull as $ncounter){
									unset ($result[$ncounter]);
								}
								if (count($result)==1) {
									return reset($result);
								}
							}
							return $result;
					}

			}
        }


}
?>