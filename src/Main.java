import ca.benow.transmission.model.TorrentStatus;
import ca.benow.transmission.model.TorrentStatus.TorrentField;

import java.io.*;p
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.*; 
import java.nio.file.SimpleFileVisitor;
import java.sql.*;
import java.sql.Date;
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*;

import com.jcraft.jsch.JSchException;


public class Main
{
	/**
	 *lecture parametrage
	 *	transmission : addres , user , mdp
	 *	db2 : adres , user , mdp
	 *	nbtelechargementseriesimultaner
	 *
	 *initilisation bdd
	 *	(table)
	 *	series
	 *		nom
	 *		repertoire
	 *		date maj web
	 *	episodes
	 *		airdate
	 *		serie
	 *		num saison 
	 *		num episodes
	 *		nom
	 *		encours (o/n)
	 *		timestamp completer
	 *		chemin complet
	 *	hash
	 *		hash
	 *		classification (default autres)
	 *		magnet
	 *		timestamp ajout
	 *		timestamp terminé
	 *
	 *alimentation bdd
	 *	serie:
	 *		ajouter de nouvelle series -> fichier qr html
	 *		mettre a non l indicateur encours de tout les episodes
	 *		si date maj web > 30 jours 
	 *			si derniere airdate < 300 jours
	 *				recuperer/maj les listes d'episodes via filebot
	 *			sinon
	 *				proposer maj web -> fichier qr html
	 *	transmission
	 *		ajouter a la bdd les non present entant que autres
	 *
	 *transmisson
	 *	hash dans la base & not timestamp terminé
	 *		si timestamp ajout plus de 24h :
	 *			demander classification effacer -> fichier qr html
	 *		(clasification du hash)
	 *		serie:
	 *			telechargement en cours
	 *				si le fichier telecharger est un episode (conversionnom2episodes)
	 *					mettre l'indicateur encours a oui
	 *				sinon
	 *					annuler le fichier
	 *		serie ou film:
	 * 			telechargement terminé 
	 *				deplacer fichiers dans le reperoire temporaire
	 *			telechargement terminé & fichier absent:
	 * 				supprimer le telechargement de transmission & timestamp terminé
	 *		effacer:
	 * 			supprimer le telechargement de transmission & timestamp terminé
	 *		ignorer:
	 *			ne rien faire & timestamp terminé
	 *		autres:
	 *			demander classification -> fichier qr html
	 *
	 *ranger les downloads (filebot) a partir du repertoire temporaire
	 *   series 
	 *   film
	 *
	 *purger repertoire temporaire
	 *	serie = repertojretmp + nom_series
	 *	film = repertoiretmp + "film"
	 *
	 *analyser repertoire
	 *	serie:
	 *		lister les episodes present (conversionnom2episodes) --> bdd episodes.chemincomplet & timestamp completer
	 *
	 *lancer les prochains hash
	 *	serie:
	 *		nbserieencours = nb hash de class serie sans timestamp terminé
	 *		nbmagnetachercher = param.nbtelechargementseriesimultaner - nbserieencours
	 *		constituer la liste de episodes non completer et nonencours avec airdate < dtjour trier par airdate decroissante
	 *		boucle tant que nbmagnetachercher > 0
	 *			recupere l'episodes suivant de la liste et le mettre a encours
	 *			si l'airdate a plus de 300 jours ou la saison n'est pas la dernier saison du dernier episods ayant une airdate < dtjour
	 * 				recherche du magnet de la saison
	 *				mettre toute la saison a encours
	 *			sinon
	 *				recherche du magnet de l'episode
	 *			ajoute le magnet a transmission
	 *			nbmagnetachercher - 1
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 * @throws JSchException 
	 *
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		(new File("error.txt")).delete();
		try {
			initialisation(args);
			initialisation_bdd(args);
			alimentation_bdd(args);		  
			transmisson(args);
			rangerdownload(args);
			purgerrepertioiredetravail(args);
			analyserrepertoire(args);
			lancerlesprochainshash(args);
			cloture(args);
		} catch (IOException | ParseException | SQLException | JSchException | InterruptedException e) {
			PrintWriter writer = new PrintWriter("error.txt", "UTF-8");
			writer.println(e.getStackTrace().toString());
			writer.close();
			e.printStackTrace();
		}
	}

	/**lancer les prochains hash
	 *	serie:
	 *		nbserieencours = nb hash de class serie sans timestamp terminé
	 *		nbmagnetachercher = param.nbtelechargementseriesimultaner - nbserieencours
	 *		constituer la liste de episodes non completer et nonencours avec airdate < dtjour trier par airdate decroissante
	 *		boucle tant que nbmagnetachercher > 0
	 *			recupere l'episodes suivant de la liste et le mettre a encours
	 *			si l'airdate a plus de 300 jours ou la saison n'est pas la dernier saison du dernier episods ayant une airdate < dtjour
	 * 				recherche du magnet de la saison
	 *				mettre toute la saison a encours
	 *			sinon
	 *				recherche du magnet de l'episode
	 *			ajoute le magnet a transmission
	 *			nbmagnetachercher - 1
	 * @throws IOException 
	 * @throws NumberFormatException 
	 *
	 */
	private static void lancerlesprochainshash(String[] args) throws SQLException, NumberFormatException, IOException {
		int nbserieencours = nbhashserienonterminee();
		int nbmagnetachercher = Param.nbtelechargementseriesimultaner - nbserieencours;
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT *"
				 + " FROM episodes "
				 + " WHERE "
				 + "      NOT encours "
				 + "  AND ( isnull(chemin_complet) or chemin_complet = \"\" )"
				 + "  AND airdate < '" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJourUsa) + "'"
				 + " ORDER BY "
				 + "  airdate Desc");
		while (rs.next() && nbmagnetachercher > 0)
		{
			rs.updateBoolean("encours", true);
			ArrayList<String> magnet=new ArrayList<String>();
			if ((rs.getDate("airdate")).before(Param.dateJourM300) || notlastsaisonactive(rs.getString("serie"),rs.getString("num_saison"))){
				magnet = Torrent.getMagnetFor(rs.getString("serie"),Integer.parseInt(rs.getString("num_saison")),-1,Integer.parseInt(nbepisodesaison(rs.getString("serie"),rs.getString("num_saison"))));
				mettretoutelasaisonaencours(rs.getString("serie"),rs.getString("num_saison"));
			}else{
				magnet = Torrent.getMagnetFor(rs.getString("serie"),Integer.parseInt(rs.getString("num_saison")),Integer.parseInt(rs.getString("num_episodes")),1);
			}
			Boolean ret=false;
			String strHash="";
			String strMagnet="";
			for (String  _st : magnet )
			{
				strMagnet = _st;
				ret = transmission.ajouterlemagnetatransmission(strMagnet);
				int debSubStr = strMagnet.indexOf("btih:") + 5;
				int finSubStr = strMagnet.indexOf("&");
				strHash = strMagnet.substring(debSubStr, finSubStr);
				if (ret){
					if(!strHash.equals("")){
						ajouterhashserie(strHash,strMagnet);
					}
					nbmagnetachercher--;
					break;
				}
			}
			rs.updateRow();
		}
		rs.close();
	}

	

	private static String nbepisodesaison(String serie, String num_saison) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT count(*) as nbEpisodes"
				 + " FROM episodes "
				 + " WHERE "
				 + "      serie = \""+serie+"\" "
				 + "  AND num_saison  = \""+num_saison+"\" "
				 + "  ");
		while (rs.next() )
		{
				return (rs.getString("nbEpisodes"));
		}
		rs.close();
		return "0";
	}

	private static void ajouterhashserie(String hash,String Magnet) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("insert into hash "
				 + " ( hash , classification , magnet ,timestamp_ajout) "
				 + "VALUE "
				 + " ('" + hash + "' ,"			 
				 + " 'serie' ,"		
				 + " '" + Magnet + "' ,"		
				 + " '" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "'"
				 + " ) "
				 + " ON DUPLICATE KEY UPDATE timestamp_ajout="
				 + " '" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "'"
				 + "");
	}
	
	private static void mettreepisodeaencours(String serie, String numsaison, String numepisode) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE episodes "
				 + " set encours = true "
				 + "WHERE "
				 + " serie = '" + serie + "'"
				 + " and num_saison = '" + numsaison + "'"			 
				 + " and num_episodes = '" + numepisode + "'"		
				 + " ");
	}
	
	private static void mettretoutelasaisonaencours(String serie, String numsaison) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE episodes "
				 + " set encours = true "
				 + "WHERE "
				 + " serie = '" + serie + "'"
				 + " and num_saison = '" + numsaison + "'"			 
				 + " ");
	}

	private static void mettredatemajserieserie(String serie) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE series "
				 + " set date_maj_web = '" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "'"
				 + "WHERE "
				 + " nom = '" + serie + "'"		
				 + " ");
	}
	
	private static boolean notlastsaisonactive(String serie, String num_saison) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT max(num_saison) as MaxSaison "
									 + " FROM episodes "
									 + " WHERE serie = \""+serie+"\" " 
									 + "   AND airdate < '" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJourUsa) + "'"
			         				 + "  ");
		while (rs.next())
		{
			int MaxSaison = rs.getInt("MaxSaison");
			if (MaxSaison > Integer.parseInt(num_saison)){
				return true;
			} else {
				return false;
			}
		}
		rs.close();
		return false;
	}

	private static int nbhashserienonterminee() throws SQLException {
		int ret = 0;
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT count(*) as NBHash "
									 + " FROM hash "
									 + " WHERE classification = \"serie\"" 
									 + "   AND timestamp_termine is null "
									 + "  ");
		while (rs.next())
		{
			ret = rs.getInt("NBHash");
		}
		rs.close();
		return ret;
	}

	private static void purgerrepertioiredetravail(String[] args) throws JSchException, IOException, InterruptedException
	{
		if (Ssh.Fileexists( Param.CheminTemporaireTmp())){
			//deplacer fichier a la racine
			Ssh.executeAction("cd \"" + Param.CheminTemporaireTmp() + "\";find -iname '*.*' -exec mv '{}' \"" + Param.CheminTemporaireTmp() + "\"");
			//purge rep=ertzooire v,wide
			Ssh.actionexecChmodR777(Param.CheminTemporaireTmp() );
			Ssh.executeAction("cd \"" + Param.CheminTemporaireTmp() + "\";find . -type d -empty -delete");
		}
	}

	private static void rangerdownload(String[] args) throws SQLException, InterruptedException, JSchException, IOException
	{
		// ranger les serie dans les sous repertoire de tmp
		if (Ssh.Fileexists(Param.CheminTemporaireSerie())){
			FileBot.rangerserie(Param.CheminTemporaireSerie() , Param.CheminTemporaireSerie() );
		}
		
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT * "
									 + " FROM series "
									 + "  ");
		while (rs.next())
		{
			Ssh.copyFile(Param.CheminTemporaireSerie() + rs.getString("nom") + Param.Fileseparator,  rs.getString("repertoire") );
		}
		rs.close();

		if (Ssh.Fileexists(Param.CheminTemporaireFilm())){
			FileBot.rangerfilm(Param.CheminTemporaireFilm() , Param.RepertoireFilm);
		}

	}

	/**analyser repertoire
	 *	serie:
	 *		lister les episodes present (conversionnom2episodes) --> bdd episodes.chemincomplet & timestamp completer
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws JSchException 
	 *
	 */
	private static void analyserrepertoire(String[] args) throws SQLException, IOException, JSchException, InterruptedException
	{
		final List<Map> listeret = new ArrayList<Map>();
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT * "
									 + " FROM series "
									 + "  ");
		while (rs.next())
		{
		    if ((Ssh.Fileexists(rs.getString("repertoire")))){
		    	ArrayList<String> files = Ssh.getRemoteFileList(rs.getString("repertoire"));
		    	for (String file:files){
		    		if (isvideo(file.toString()){
				    	Map<String, String> ret =  new HashMap<String, String>();
				    	if (!fichierdanslabaseepisodes(file.toString())){
					    	ret = conversionnom2episodes(file.toString());
						if (ret.get("serie") != null && !ret.get("serie").equals(""))
						{
							ret.put("chemin", file.toString());	
							listeret.add(ret);
							System.out.println("Ep:"+ret.get("serie")+" "+ret.get("saison")+"-"+ret.get("episode")+" File:" + file.toString()); 
						}	
				    	}
		    		}
		    	}
	            
/*			    Path startPath = Paths.get(rs.getString("repertoire"));
			    Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
			        @Override
			        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
						Map<String, String> ret =  new HashMap<String, String>();
						try {
							ret = conversionnom2episodes(file.toString());
						} catch (SQLException e) {
							e.printStackTrace();
						}
						if (ret.get("serie") == null)
						{
							ret.put("chemin", file.toString());	
							listeret.add(ret);
						}	
			            System.out.println("File: " + file.toString());           
			            return FileVisitResult.CONTINUE;
			        }
			    });*/
			 }
		}
		rs.close();

		for (Map curr:listeret)
		{
			stmt.executeUpdate("UPDATE episodes "
				 + " set chemin_complet = '" + curr.get("chemin") + "'"
				 + "   , timestamp_completer = '" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "'"
				 + "WHERE "
				 + " serie = '" + curr.get("serie") + "'"
				 + " and num_saison = '" + curr.get("saison") + "'"
				 + " and num_episodes = '" + curr.get("episode") + "'"				 
				 + " ");
			
			String NomFichier=pathfile.substring((Math.max(curr.get("chemin").lastIndexOf('/'), curr.get("chemin").lastIndexOf('\\')))+1);
			WordPressHome.publishOnBlog(
				6,
				(new SimpleDateFormat("yyyyMMdd_HHmmSS")).format(Param.dateDuJour) + "_" + NomFichier,
				NomFichier,
				new String[] {ret.get("serie"), "S" + curr.get("saison"), "E" + curr.get("episode") },
				new String[] { "Serie" },
				"<a href=\"http://home.daisy-street.fr/BibPerso/stream.php?flux="
				+ URLEncoder.encode(curr.get("chemin"), "UTF-8") + "\">" + NomFichier + "</a>" + "\n"
				+ "" ) ;
		}
	}

	private static boolean isvideo(String pathfile) {
		String extension = "";
		String[] Video = new String[] {"avi","mp4","mpg","mkv","wnv","divx"};
		
		int i = pathfile.lastIndexOf('.');
		int p = Math.max(pathfile.lastIndexOf('/'), pathfile.lastIndexOf('\\'));
		
		if (i > p) {
		    extension = pathfile.substring(i+1);
		}
		
		return Arrays.asList(Video).contains(extension.toLowerCase();
	}


	private static boolean fichierdanslabaseepisodes(String pathfile) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement();
		rs = stmt.executeQuery("SELECT * "
									 + " FROM episodes "
									 + " WHERE "
									 + "      chemin_complet = \"" + pathfile + "\""
									 + "  ");
		rs.last();
		if (rs.getRow() == 0)
		{				
			return true;
		}
		rs.close();
		return false ;
	}

	/**
	 *transmisson
	 *	hash dans la base & not timestamp terminé
	 *		si timestamp ajout plus de 24h :
	 *			demander classification effacer -> fichier qr html
	 *		(clasification du hash)
	 *		serie:
	 *			telechargement en cours
	 *				si le fichier telecharger est un episode (conversionnom2episodes)
	 *					mettre l'indicateur encours a oui
	 *				sinon
	 *					annuler le fichier
	 *		serie ou film:
	 * 			telechargement terminé 
	 *				deplacer fichiers dans le reperoire temporaire
	 *			telechargement terminé & fichier absent:
	 * 				supprimer le telechargement de transmission & timestamp terminé
	 *		effacer:
	 * 			supprimer le telechargement de transmission & timestamp terminé
	 *		ignorer:
	 *			ne rien faire & timestamp terminé
	 *		autres:
	 *			demander classification -> fichier qr html
	 * @throws JSchException 
	 * @throws InterruptedException 
	 *
	 */
	private static void transmisson(String[] args) throws JSONException, IOException, SQLException, InterruptedException, JSchException
	{
		ResultSet rs = null;
	  	List<TorrentStatus> torrents = Param.client.getAllTorrents(new TorrentField[] { TorrentField.hashString ,TorrentField.files,TorrentField.name,TorrentField.percentDone });
		for (TorrentStatus curr : torrents)
		{
			String hash = (String) curr.getField(TorrentField.hashString);
			Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			rs = stmt.executeQuery("SELECT * "
										 + " FROM hash "
										 + " WHERE "
										 + "      hash = '" + hash + "'"
										 + "  ");
			while (rs.next())
			{
				if (rs.getDate("timestamp_termine") != null)
				{
					transmission.supprimer_hash(hash);
				}
				else
				{
					if (rs.getDate("timestamp_ajout").before(Param.dateJourM1))
					{
						FichierQR.demanderClassificationEffacer((String)curr.getField(TorrentField.name), hash);
					}
					int nbfichierbruler;
					switch (rs.getString("classification"))
					{
						case "serie": 
							nbfichierbruler = 0;
							JSONArray listFile = (JSONArray) curr.getField(TorrentField.files);
							int i = 0;
							for (i = 0; i < listFile.length(); i++)
							{
								JSONObject n = (JSONObject) listFile.get(i);
								Map<String, String> ret=conversionnom2episodes(n.getString("name"));
								if (ret == null)
								{
									nbfichierbruler ++;
									transmission.cancelFilenameOfTorrent(hash, i);
								}
								else
								{
									mettreepisodeaencours(ret.get("serie"),ret.get("saison"),ret.get("episode"));
									if (n.get("bytesCompleted").equals(n.get("length")))
									{
										transmission.deplacer_fichier(hash, Param.CheminTemporaireSerie(),i);										
										nbfichierbruler ++;
									}
								}
							}
							if ( curr.getField(TorrentField.percentDone).equals(1))
							{
								if (listFile.length()==nbfichierbruler)
								{
									transmission.supprimer_hash(hash);
									rs.updateString("timestamp_termine", (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour));
									rs.updateRow();
								}
							}
							break;
						case "film": 
							if ((double) curr.getField(TorrentField.percentDone) == 1.0)
							{
								if (transmission.all_fichier_absent(hash))
								{
									transmission.supprimer_hash(hash);
									rs.updateString("timestamp_termine", (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour));
									rs.updateRow();
								}
								else
								{
									transmission.deplacer_fichier(hash, Param.CheminTemporaireFilm());
								}
							}
							break;
						case "effacer": 
							transmission.supprimer_hash(hash);
							rs.updateString("timestamp_termine", (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour));
							rs.updateRow();
							break;
						case "ignorer": 
							break;
						case "autres": 
							FichierQR.demanderClassification((String)curr.getField(TorrentField.name), hash);
							break;
					}
				}
			}
			rs.close();
		}
	}

	public static void initialisation(String[] args) throws IOException, ParseException, SQLException, JSchException, InterruptedException 
	{
		Param.ChargerParametrage();

		System.out.print("<pre>");
		System.out.println("+---+----+----+----+");
		System.out.println("+       Debut      +" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		System.out.println(Param.GetOs());
	}

    /**
     * 
     * 
     * @throws SQLException
     */
	private static void initialisation_bdd(String[] args) throws SQLException
	{
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS series "
								 + "(nom VARCHAR(255) not NULL , "
								 + " repertoire VARCHAR(255) , "
								 + " date_maj_web DATE , "
								 + " PRIMARY KEY ( nom ) "
								 + ") "
								 + " ");
		
		//Param.stmt.executeUpdate("DROP TABLE episodes ");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS episodes "
								 + "(serie VARCHAR(255) not NULL , "
								 + " num_saison INTEGER  , "
								 + " num_episodes INTEGER  , "
								 + " nom  VARCHAR(255) , "
								 + " airdate DATE , "
								 + " encours BOOLEAN , "					
								 + " timestamp_completer DATE , "
								 + " chemin_complet  VARCHAR(255) , "
								 + " PRIMARY KEY ( serie , num_saison , num_episodes  ) , "
								 + "         INDEX   ( airdate ) "
								 + ") "
								 + " ");
		
		//Param.stmt.executeUpdate("DROP TABLE hash ");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS hash "
								 + "(hash VARCHAR(255) not NULL , "
								 + " classification  VARCHAR(255) , "
								 + " magnet  VARCHAR(255) , "
								 + " timestamp_ajout DATE , "
								 + " timestamp_termine DATE , "
								 + " PRIMARY KEY ( hash ) ,"
								 + "         INDEX   ( timestamp_termine ) "
								 + ") "
								 + " ");

	}

    /**
	 *serie: 
	 *    ajouter de nouvelle series -> fichier qr html
	 *    mettre a non l indicateur encours de tout les episodes
	 *    si date maj web > 30 jours
	 *    -si derniere airdate < 300 jours
	 *    --recuperer/maj les listes d'episodes via filebot
	 *    -sinon
	 *    --proposer maj web -> fichier qr html
	 *transmission
	 *    ajouter a la bdd les non present entant que autres
     * @throws SQLException
     * @throws ParseException 
     * @throws NumberFormatException 
     * @throws InterruptedException 
     * @throws JSchException 
     */
	private static void alimentation_bdd(String[] args) throws SQLException, JSONException, IOException, NumberFormatException, ParseException, JSchException, InterruptedException
	{

		FichierQR.AjouterNouvelleSerie();

		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("update episodes set encours = false ");


		ResultSet rs ;
		rs = stmt.executeQuery("SELECT series.nom , max(episodes.airdate) as MaxDate"
									 + " FROM series "
									 + " LEFT OUTER join episodes "
									 + "    on series.nom = episodes.serie "
									 + " WHERE "
									 + "      series.date_maj_web < '" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateJourM30) + "'"
									 + "   OR series.date_maj_web IS NULL"
									 + " GROUP BY "
									 + "  series.nom");
		while (rs.next())
		{	
			if (rs.getObject("MaxDate")==null){
				FileBot.maj_liste_episodes(rs.getString("nom"));
				mettredatemajserieserie(rs.getString("nom"));
			}else{
				if ((rs.getDate("MaxDate")).after(Param.dateJourM300))
				{ 
					FileBot.maj_liste_episodes(rs.getString("nom"));
					mettredatemajserieserie(rs.getString("nom"));
				}
				else
				{
					FichierQR.ForcerMajSerieWeb(rs.getString("nom"));
				}
			}
		}
		rs.close();

	  	List<TorrentStatus> torrents = Param.client.getAllTorrents(new TorrentField[] { TorrentField.hashString });
		for (TorrentStatus curr : torrents)
		{
			
			String hash = (String) curr.getField(TorrentField.hashString);
			rs = stmt.executeQuery("SELECT * "
										 + " FROM hash "
										 + " WHERE "
										 + "      hash = '" + hash + "'"
										 + "  ");
			rs.last();
			if (rs.getRow() == 0)
			{				
				String sql = "INSERT INTO hash "
					+ " (hash, classification,timestamp_ajout) VALUES  " 
					+ " ( "
					+ " '" + hash + "' , "
					+ " 'autres' , "
					+ " '" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "'  "
					+ " ) ";
				stmt.executeUpdate(sql);

			}
			rs.close();
		}

	}


	private static void cloture(String[] args) throws InterruptedException, IOException, SQLException
	{
		Param.cloture();

		System.out.println("+        Fin       +" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		System.out.println("+---+----+----+----+");
		System.out.print("</pre>");
	}
	
	private static Map<String, String> conversionnom2episodes(String fileName) throws SQLException
	{
		Param.logger.debug("episode-" + "decomposerNom " + fileName);
		fileName = Param.getFilePartName(fileName);
		Map<String, String> ret = new HashMap<String, String>();

		String partname = "$$";
		String namecmp = (fileName.toLowerCase() + " ").replaceAll("[+_-]", " ").replaceAll("[(][^)]*[)]", "").replaceAll("[\\[][^\\]]*[\\]]", "");

		Pattern p1 = Pattern
			.compile("([Ss]eason[ ]*|[Ss]|[Ss][Nn])([0-9]{1,2})[ x._-]*([Ee]pisode[ ]*|[Ee]|[Ee][Pp])[ ._-]*([0-9]{0,2})[ ]*[Ee&x._-]([0-9]{0,2})[ ._-]");
		Pattern p6 = Pattern.compile("([Ss]eason[ ]*|[Ss]|[Ss][Nn])([0-9]{1,2})[ x._-]*([Ee]pisode[ ]*|[Ee]|[Ee][Pp])[ ._-]*([0-9]{0,2})[ ._-]");
		Pattern p5 = Pattern.compile("([Ss]eason[ ]*|[Ss]|[Ss][Nn])([0-9]{1,2})[ x._-]*([Ee]pisode[ ]*|[Ee]|[Ee][Pp])[ ._-]*([0-9]{3,3})[ ._-]");
		Pattern p2 = Pattern.compile("[._ (-]([0-9]+)x([0-9]+)");
		Pattern p3 = Pattern.compile("[._ (-]([0-9]+)([0-9][0-9])");
		Pattern p4 = Pattern.compile("[._ (-]([0-9]+)");

		Matcher m1 = p1.matcher(namecmp.toLowerCase());
		Matcher m2 = p2.matcher(namecmp.toLowerCase());
		Matcher m3 = p3.matcher(namecmp.toLowerCase());
		Matcher m4 = p4.matcher(namecmp.toLowerCase());
		Matcher m5 = p5.matcher(namecmp.toLowerCase());
		Matcher m6 = p6.matcher(namecmp.toLowerCase());

		HashMap<String, String> numeroEpisodeTrouve = new HashMap<String, String>();
		HashMap<String, String> numeroSequentielTrouve = new HashMap<String, String>();
		HashMap<String, String> numeroSaisonTrouve = new HashMap<String, String>();
		numeroEpisodeTrouve.clear();
		numeroSequentielTrouve.clear();
		numeroSaisonTrouve.clear();
		if (m4.find())
		{
			numeroEpisodeTrouve.clear();
			numeroSequentielTrouve.clear();
			numeroSaisonTrouve.clear();
			partname = namecmp.substring(0, m4.start(0));
			numeroSequentielTrouve.put("sequentiel", m4.group(1).toString());
			Param.logger.debug("episode-" + "decomposerNom 4-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
						 + numeroSequentielTrouve.toString());
		}

		if (m3.find())
		{
			partname = namecmp.substring(0, m3.start(0));
			numeroSaisonTrouve.put("saison", m3.group(1).toString());
			numeroEpisodeTrouve.put("episode", m3.group(2).toString());
			Param.logger.debug("episode-" + "decomposerNom 3-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
						 + numeroSequentielTrouve.toString());
		}

		if (m2.find())
		{
			numeroEpisodeTrouve.clear();
			numeroSaisonTrouve.clear();
			numeroSequentielTrouve.clear();
			partname = namecmp.substring(0, m2.start(0));
			numeroSaisonTrouve.put("saison", m2.group(1).toString());
			numeroEpisodeTrouve.put("episode", m2.group(2).toString());
			Param.logger.debug("episode-" + "decomposerNom 2-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
						 + numeroSequentielTrouve.toString());
		}

		if (m5.find())
		{
			numeroEpisodeTrouve.clear();
			numeroSaisonTrouve.clear();
			numeroSequentielTrouve.clear();
			partname = namecmp.substring(0, m5.start(0));
			numeroSaisonTrouve.put("saison", m5.group(2).toString());
			numeroEpisodeTrouve.put("episode", m5.group(4).toString());
			Param.logger.debug("episode-" + "decomposerNom 5-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
						 + numeroSequentielTrouve.toString());
		}
		else
		{
			if (m6.find())
			{
				numeroEpisodeTrouve.clear();
				numeroSaisonTrouve.clear();
				// numeroSequentielTrouve.clear();
				partname = namecmp.substring(0, m6.start(0));
				numeroSaisonTrouve.put("saison", m6.group(2).toString());
				numeroEpisodeTrouve.put("episode", m6.group(4).toString());
				Param.logger.debug("episode-" + "decomposerNom 6-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
							 + numeroSequentielTrouve.toString());
			}
			if (m1.find())
			{
				if (m1.group(5).toString().compareTo("") != 0)
				{
					if (Param.isNumeric(m1.group(4).toString()) && Param.isNumeric(m1.group(5).toString()))
					{
						if (Integer.parseInt(m1.group(5).toString()) == (Integer.parseInt(m1.group(4).toString()) + 1))
						{
							numeroEpisodeTrouve.clear();
							numeroSaisonTrouve.clear();
							numeroSequentielTrouve.clear();
							partname = namecmp.substring(0, m1.start(0));
							numeroSaisonTrouve.put("saison", m1.group(2).toString());
							numeroEpisodeTrouve.put("episode", m1.group(4).toString());
							numeroEpisodeTrouve.put("episodebis", m1.group(5).toString());
							Param.logger.debug("episode-" + "decomposerNom 1-" + partname + " " + numeroSaisonTrouve.toString() + " "
										 + numeroEpisodeTrouve.toString() + " " + numeroSequentielTrouve.toString());
						}
					}
				}
			}
		}

		Integer nbtrouve = 0;
		if ((numeroSaisonTrouve.size() > 0 && numeroEpisodeTrouve.size() > 0) || numeroSequentielTrouve.size() > 0)
		{

			Boolean ctrlnom;
			ResultSet rs = null;
			Statement stmt = Param.con.createStatement();
			rs = stmt.executeQuery("SELECT * "
										 + " FROM series "
										 + "  ");
			while (rs.next())
			{
				ctrlnom = true;
				String textSerieNettoyer = rs.getString("nom").replaceAll("[(]([0-9a-zA-Z]*)[)]", "");
				String[] textSerie = textSerieNettoyer.split("[-,'._() ]+");
				String partnameDouble = partname.replaceAll("(.)(?=\\1)", "");
				for (String mot : textSerie)
				{
					if (mot.length() > 1)
					{
						String motDouble = mot.replaceAll("(.)(?=\\1)", "");
						if ((" " + partname).indexOf(mot.toLowerCase()) < 1 && (" " + partnameDouble).indexOf(motDouble.toLowerCase()) < 1)
						{
							ctrlnom = false;
						}
					}
				}
				if (ctrlnom)
				{
					nbtrouve++;
					ret.put("serie", rs.getString("nom"));
					Param.logger.debug("episode- decomposerNom" + rs.getString("nom"));
				}

			}
			rs.close();

			ret.putAll(numeroSaisonTrouve);
			ret.putAll(numeroEpisodeTrouve);
			ret.putAll(numeroSequentielTrouve);
		}

		if (nbtrouve == 0)
		{
			ret.put("serie", "");
		}
		return ret;

	}


}
