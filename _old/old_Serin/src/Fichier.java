import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.*;

import java.io.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.apache.log4j.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Defini les caracteristique d'un fichier logique (non forcement existant) dans
 * le cadre de la constitution d'une bibliotheque d'episode de series
 * 
 * @author Romain CLEMENT
 */

public class Fichier extends Repertoire
{
	private final static Logger logger = Logger.getLogger(Fichier.class);

	/**
	 * 
	 */
	public Fichier()
	{
		super();
	}

	private String nom;
	public boolean freeze = false;
	String extension;
	Boolean presenceOnDrive;
	String cheminComplet;
	File file;

	public Fichier(String repertoire, String nom, EnumTypeDeFichier typeFichier)
	{
		super(repertoire);
		this.nom = nom;
		this.extension = Fichier.estTypePresent(this.repertoire, this.nom, typeFichier);
		this.presenceOnDrive = (this.extension != "");
		this.cheminComplet = Repertoire.formatPath(Fichier.constituerCheminComplet(this.repertoire, this.nom, this.extension));

		this.file = new File(this.cheminComplet);
	}

	public Fichier(String repertoire)
	{
		super(repertoire);
		this.nom = "";
		this.extension = "";
		this.cheminComplet = Repertoire.formatPath(this.repertoire);
		this.presenceOnDrive = isPresenceOnDrive();

		this.file = new File(this.cheminComplet);
	}

	public Fichier(String repertoire, String nom, String extension)
	{
		super(repertoire);
		this.nom = nom;
		this.extension = extension;
		this.cheminComplet = Repertoire.formatPath(Fichier.constituerCheminComplet(this.repertoire, this.nom, this.extension));
		this.presenceOnDrive = isPresenceOnDrive();

		this.file = new File(this.cheminComplet);
	}

	private static void purgeFicher(String cheminComplet)
	{
		if (Fichier.estPresent(cheminComplet))
		{
			logger.debug("fichier- delete " + cheminComplet);
			(new File(cheminComplet)).delete();
		}
	}

	public static void purgeListeFichier(ArrayList<String> listeFichierAPurger, String extensionAExclure)
	{
		for (String _st : listeFichierAPurger)
		{
			if (!_st.endsWith(extensionAExclure))
			{
				Fichier.purgeFicher(_st);
			}
		}
	}

	public Boolean delete()
	{
		String ch = this.cheminComplet;
		// this.file = null;
		return (new File(ch)).delete();
	}

	public boolean isPresenceOnDrive()
	{
		return Fichier.estPresent(this.cheminComplet);
	}

	/**
	 * getter setter
	 * 
	 */
	public void setNom(String nom)
	{
		this.nom = nom;
//		this.setCheminComplet();
	}

	public String getNom()
	{
		return nom;
	}

	/**
	 * @param extension
	 *            the extension to set
	 */
//	public void setExtension(String extension) {
//		this.extension = extension;
////		this.setCheminComplet();
//	}

	/**
	 * @param repertoire
	 * @param nom
	 * @param extension
	 * @param presenceOnDrive
	 * @param cheminComplet
	 */
	public Fichier(String repertoire, String nom, String extension, Boolean presenceOnDrive, String cheminComplet)
	{
		super(repertoire);
		this.nom = nom;
		this.extension = extension;
		this.presenceOnDrive = presenceOnDrive;

		this.cheminComplet = Repertoire.formatPath(cheminComplet);
		this.file = new File(this.cheminComplet);

	}

	public String getCheminComplet()
	{
		return cheminComplet;
	}

//	private void setCheminComplet() {
//		this.cheminComplet = Repertoire.formatPath(Fichier.constituerCheminComplet(this.repertoire, this.nom, this.extension));
//		this.file = new File(this.cheminComplet);
//	}

	// public void setCheminComplet(String cheminComplet) {
	// this.cheminComplet = Repertoire.formatPath(cheminComplet);
	// this.file = new File(this.cheminComplet);
	// this.extension = Episode.getFileExtension(cheminComplet);
	// }

	/**
	 * centr&lisation de la creation du chemin complet
	 * 
	 */
	public static String constituerCheminComplet(String repertoire, String nom, String extension)
	{
		String str = Repertoire.formatPath(repertoire + File.separator + nom + "." + extension);
		str = str.replace(File.separator + File.separator, File.separator);
		return str;
	}

	/**
	 * verifie la presence sur le disque du fichier=
	 * 
	 */
	public static Boolean estPresent(String cheminComplet)
	{
		// System.out.println(cheminComplet);
		File f = new File(Repertoire.formatPath(cheminComplet));
		if (f.exists())
		{// && !f.isDirectory()) {
			f = null;
			return true;
		}
		f = null;
		return false;
	}

	/**
	 * verifie la presence sur le disque d'un fichier du type demader et renvoie
	 * l'extension ou blanc
	 * 
	 */
	public static String estTypePresent(String repertoire, String nom, EnumTypeDeFichier typeFichier)
	{

		String ext = "";
		return (typeFichier.EstDuTypeDeFichier(ext)) ? ext : "";
	}

	/**
	 * sauvegarde de la bibliotheque passe en parametre dans dans le fichier
	 * chemin complet sous la forme d'une serialisation xml
	 * 
	 * @throws FileNotFoundException
	 */
	public void serialiseXML(Object p1) throws FileNotFoundException, IOException
	{
		if (!freeze)
		{
			// logger.info("serialiseXML-"+ this.cheminComplet);
			FileOutputStream fout = new FileOutputStream(this.cheminComplet);
			fout.write("<?xml version=\"1.0\"?>".getBytes("UTF-8"));
			XStream xs = new XStream(new DomDriver("UTF-8"));
			byte[] buf = xs.toXML(p1).getBytes("UTF-8");
			fout.write(buf);
			fout.flush();
			fout.close();
			fout = null;
			/*
			 * XMLEncoder encoder = new XMLEncoder(fout); try { // serialisation
			 * de l'objet encoder.writeObject(p1); encoder.flush(); } finally {
			 * // fermeture de l'encodeur encoder.close(); }
			 */
			// logger.info("serialiseXML- serialiseXML ok");
		}
	}

	public static String getSerialiseXML(Object p1)
	{
		XStream xs = new XStream(new DomDriver("UTF-8"));
		return xs.toXML(p1);
	}

	/**
	 * recupere la bibliotheque par de-serialisation du fichier de sauvegarde
	 * 
	 * @param bibSerie
	 *            objet bibliothequeserie a chargr avec le fichier xml
	 * @param cheminComplet
	 *            emplacement du fichie xml a charger
	 */
	public void unserialiseXML() throws FileNotFoundException
	{
		if (!freeze)
		{
			FileInputStream fin = new FileInputStream(this.cheminComplet);
			String svgCheminComplet = this.cheminComplet;
			XStream xs = new XStream(new DomDriver("UTF-8"));
			xs.fromXML(fin, this);
			this.cheminComplet = svgCheminComplet;
			this.file = new File(this.cheminComplet);
		}
	}

	public Object unserialiseXML(Object p1) throws FileNotFoundException
	{
		if (!freeze)
		{
			logger.debug("unserialiseXML..." + this.cheminComplet);
			FileInputStream fin = new FileInputStream(this.cheminComplet);
			XStream xs = new XStream(new DomDriver("UTF-8"));
			xs.fromXML(fin, p1);
		}
		return p1;
	}

	public static Object unserialiseXML(String p1)
	{
		XStream xs = new XStream(new DomDriver("UTF-8"));
		return xs.fromXML(p1);
	}

	public String getContent() throws FileNotFoundException, IOException
	{
		logger.debug(this.cheminComplet);
		if (!file.exists()){return "";}
		StringBuilder sb = new StringBuilder();
		FileInputStream fin = new FileInputStream(this.cheminComplet);
		Reader r = new InputStreamReader(fin, "UTF-8");
		char[] buf = new char[1024];
		int ch = r.read(buf);
		while (ch >= 0)
		{
			sb.append(buf, 0, ch);
			ch = r.read(buf);
		}
		r.close();
		fin.close();
		return sb.toString();
		// return "";
	}

	public static String getFileExtension(String fileName)
	{
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		else
			return "";
	}

	public static String getFileChemin(String fileName)
	{
		int pos;
		pos = fileName.lastIndexOf("/");
		if (pos > 0)
		{
			return fileName.substring(0, pos);
		}
		pos = fileName.lastIndexOf("\\");
		if (pos > 0)
		{
			return fileName.substring(0, pos);
		}
		return "";
	}

	public static String getFileParentName(String fileName)
	{
		File workfile = new File(fileName);
		if (workfile != null && workfile.getParentFile() != null)
		{
			return workfile.getParentFile().getName();
		}
		return null; // no parent for file
	}

	public static String getPathParent(String fileName)
	{
		File workfile = new File(fileName);
		if (workfile != null && workfile.getParent() != null)
		{
			return workfile.getParent();
		}
		return null; // no parent for file
	}

	public static String getFilePartName(String fileName)
	{
		int pos;
		pos = fileName.lastIndexOf("/");
		if (pos > 0)
		{
			fileName = fileName.substring(pos + 1);
		}
		pos = fileName.lastIndexOf("\\");
		if (pos > 0)
		{
			fileName = fileName.substring(pos + 1);
		}
		return fileName;
	}

	public static String getRelativeFileNameWithoutExt(String fileName, String repbase)
	{
		int pos;
		int pos2 = fileName.lastIndexOf(".");
		if (pos2 == -1)
		{
			pos2 = fileName.length();
		}
		fileName = fileName.substring(0, pos2).replace(repbase, "");
		return fileName;
	}

	public static String getFilePartNameWithoutExt(String fileName)
	{
		int pos;
		int pos2 = fileName.lastIndexOf(".");
		String ret = fileName.substring(0, pos2);
		if (pos2 == -1)
		{
			pos2 = fileName.length();
		}
		pos = fileName.lastIndexOf("/");
		if (pos > 0)
		{
			ret = fileName.substring(pos + 1, pos2);
		}
		pos = fileName.lastIndexOf("\\");
		if (pos > 0)
		{
			// System.out.println(fileName + " - " + pos +" - " + pos2);
			ret = fileName.substring(pos + 1, pos2);
		}
		return ret;
	}

	/**
	 * copie le fichier source dans le fichier resultat retourne vrai si cela
	 * r�ussit
	 */
	public static boolean copyFile(File source, File dest, Boolean append) throws FileNotFoundException, IOException
	{
		FileChannel in = null; // canal d'entr�e
		FileChannel out = null; // canal de sortie

		if (!source.exists())
		{
			return false;
		}

		// Init
		in = new FileInputStream(source).getChannel();
		out = new FileOutputStream(dest,append).getChannel();

		// Copie depuis le in vers le out
		in.transferTo(0, in.size(), out);

		in.close();
		out.close();
		return true;
	}

	public void WriteToFile(String content) throws IOException
	{

		if (!file.exists())
		{
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content + "\n");
		bw.close();

	}

	/**
	 * Zip it
	 * 
	 * @param zipFile
	 *            output ZIP file location
	 */
	public void zipItOnMe(Map<String, Fichier> fileList, Boolean EffacerSource) throws FileNotFoundException, IOException
	{

		if (EnumTypeDeFichier.archive.EstDuTypeDeFichier(this.extension))
		{
			byte[] buffer = new byte[1024];

			FileOutputStream fos = new FileOutputStream(this.cheminComplet);
			ZipOutputStream zos = new ZipOutputStream(fos);

			// System.out.println("Output to Zip : " + this.cheminComplet);

			for (String mapKey : fileList.keySet())
			{

				Fichier file = fileList.get(mapKey);
				if (file.isPresenceOnDrive())
				{
					String name = file.file.getName();
					// System.out.println("File Added : " + name);
					ZipEntry ze = new ZipEntry(name);
					zos.putNextEntry(ze);
					FileInputStream in = new FileInputStream(file.cheminComplet);

					int len;
					while ((len = in.read(buffer)) > 0)
					{
						zos.write(buffer, 0, len);
					}

					in.close();
					if (EffacerSource)
					{
						if (file.delete())
						{
							// System.out.println("File Deleted : " + name);
						}
					}
				}

			}

			zos.closeEntry();
			// remember close it
			zos.close();

			// System.out.println("Done");
		}
	}

	public static String FormatUTF8toISO8859(String stringutf8) throws UnsupportedEncodingException
	{
		String stringiso8859 = stringutf8;
		stringiso8859 = new String(stringutf8.getBytes("UTF-8"), "ISO-8859-1");
		return stringiso8859;
	}

	public static String FormatISO8859toUTF8(String stringiso8859) throws UnsupportedEncodingException
	{
		String stringutf8 = stringiso8859;
		stringutf8 = new String(stringiso8859.getBytes("ISO-8859-1"), "UTF-8");
		return stringutf8;
	}
}
