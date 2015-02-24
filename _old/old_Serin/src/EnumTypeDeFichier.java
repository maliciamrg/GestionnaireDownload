
import java.util.*;/**
 * Defini les type de fichier par extensions 
 *
 * @author Romain CLEMENT
 */

public enum EnumTypeDeFichier
{

	video     ("video",      new String[] {"avi","mp4","mpg","mkv","wnv","divx"}),
	texte 	  ("texte",  new String[] {"txt"}),
	csv 	  ("csv",  new String[] {"csv"}),
	soustitre ("soustitre",  new String[] {"srt"}),
	archive   ("archive",    new String[] {"rar","zip"}),
	json   	  ("param",      new String[] {"json"}),
	xml 	  ("data serial",new String[] {"xml"}),
	trace 	  ("trace text",      new String[] {"trace"}),
	lock 	  ("lock",      new String[] {"lck"}),
	html 	  ("internet",      new String[] {"html"}),
	part 	  ("part",      new String[] {"part"}),
	ini 	  ("param aplicatif",      new String[] {"ini"}),
	all 	  ("all",      new String[] {""});

	public String getNom()
	{
		return nom;
	};
	

	private String nom ="";
	String extensions[] = {"",""};

	EnumTypeDeFichier(String nom, String[] extensions)
	{
		this.nom = nom;
		this.extensions = extensions;
	}

	public boolean EstDuTypeDeFichier(String extensions)
	{
		if (this.nom.compareTo(EnumTypeDeFichier.all.getNom())==0){
			return true;
		}
		return Arrays.asList(this.extensions).contains(extensions.toLowerCase());
	}



}
