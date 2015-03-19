import ca.benow.transmission.model.*;
import ca.benow.transmission.model.TorrentStatus.*;

import com.jcraft.jsch.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.apache.xmlrpc.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;
import org.jfree.util.*;
import org.json.*;
import org.jfree.chart.*;


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
	 * @throws XmlRpcException 
	 * @throws JSchException 
	 *
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, XmlRpcException
	{
		(new File("error.txt")).delete();
		try {
			initialisation(args);
			initialisation_bdd(args);
			alimentation_bdd(args);	
			FileBot.rangerserie("/media/videoclub/anime/Detective Conan/","/media/videoclub/anime");
			transmisson(args);
			rangerdownload(args);
			purgerrepertioiredetravail(args);
			analyserrepertoire(args);
			lancerlesprochainshash(args);
			cloture(args);
		} catch (IOException | ParseException | SQLException | JSchException | InterruptedException e) {
			PrintWriter writer = new PrintWriter("error.txt", "UTF-8");
			writer.println(Param.eToString(e));
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
	private static void lancerlesprochainshash(String[] args)
			throws SQLException, NumberFormatException, IOException {
		System.out.println("lancerlesprochainshash");
		int nbserieencours = nbhashserienonterminee();
		int nbmagnetachercher = Param.nbtelechargementseriesimultaner
				- nbserieencours;

		while (nbmagnetachercher > 0) {

			String serie = "";
			String saison = "";
			Map<String, String> ret = recupererprochainesaisonarechercher();

			serie=ret.get("serie");
			saison=ret.get("saison");
			if (serie=="" || saison =="") {break;}
			
			ArrayList<Integer> episodes = recupurerlisteepisodesarechercher(ret.get("serie"), ret.get("saison"));
		
			mettretoutelasaisonaencours(serie, saison);
			
			ArrayList<String> magnet = Torrent.getMagnetFor(serie,
					Integer.parseInt(saison), episodes,
					Integer.parseInt(nbepisodesaison(serie, saison)));

			if (magnet.size()==0){
				mettreenattenteepisode(serie,saison,episodes);
			}
			for (String strMagnet : magnet) {
				if (transmission.ajouterlemagnetatransmission(strMagnet)) {
					String strHash = "";
					int debSubStr = strMagnet.indexOf("btih:") + 5;
					int finSubStr = strMagnet.indexOf("&");
					strHash = strMagnet.substring(debSubStr, finSubStr);
					if (!strHash.equals("")) {
						ajouterhashserie(strHash, strMagnet);
						Param.logger.debug("Torrent Ep:" + serie + " " + saison
								+ "-" + episodes.toString()
								+ " Magnet:" + strMagnet);
					}
					nbmagnetachercher--;
				}
			}

		}
	}

	private static ArrayList<Integer> recupurerlisteepisodesarechercher(String serie, String saison) throws SQLException, NumberFormatException
	{
		ArrayList<Integer> episodes = new ArrayList<Integer>();
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
												   ResultSet.CONCUR_UPDATABLE);
		String sql = ("SELECT num_episodes"
			+ " FROM episodes "
			+ " WHERE "
			+ "      NOT encours "
			+ "  AND ( isnull(chemin_complet) or chemin_complet = \"\" )"
			+ "  AND serie=\"" + serie + "\""
			+ "  AND num_saison=\"" + saison + "\"" + "");
		rs = stmt.executeQuery(sql);
		while (rs.next())
		{
			episodes.add(Integer.parseInt(rs.getString("num_episodes")));
		}
		rs.close();
		return episodes;
	}

	private static Map<String, String> recupererprochainesaisonarechercher() throws SQLException
	{
		Map<String, String> ret = new HashMap<String, String>();
		ResultSet rs = null;
		Statement stmt = Param.con
			.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							 ResultSet.CONCUR_UPDATABLE);
		String sql = ("SELECT serie , num_saison"
			+ " FROM episodes "
			+ " WHERE "
			+ "      NOT encours "
			+ "  AND ( isnull(chemin_complet) or chemin_complet = \"\" )"
			+ "  AND ( isnull(freezesearchuntil) or freezesearchuntil < \""
			+ (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "\")" 
			+ "  AND airdate < \""
			+ (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJourUsa) + "\"" 
			+ " ORDER BY " + "  airdate Desc");
		rs = stmt.executeQuery(sql);
		rs.first();
		if (rs.getRow() > 0)
		{
			ret.put("serie",  rs.getString("serie"));
			ret.put("saison", rs.getString("num_saison"));
		} else{
			ret.put("serie",  "");
			ret.put("saison", "");	
		}
		rs.close();
		return ret;
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
	
	private static String getseriestathtml(String serie) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT *"
				 + " FROM episodes "
				 + " WHERE "
				 + "      serie = \"" + serie + "\""
				 + " ORDER BY "
				 + "  airdate Desc"
				 + "" );
		int nbtotal = 0;
		int nbpresent = 0;
		int nbencours = 0;
		int nbabsent = 0;
		int nbavenir = 0;
		ArrayList<ArrayList<String>> visu = new ArrayList<ArrayList<String>>();
		String strnbjourprochainepisodes = "";
		while (rs.next())
		{
			nbtotal ++;
			String icone = "#";
			if ( rs.getString("chemin_complet") != null){
				nbpresent++;
				icone = "X";
			} else {
				if (rs.getBoolean("encours")){
					nbencours++;
					icone = ">";
				} else {
					if ((rs.getDate("airdate")).before(Param.dateDuJourUsa)){	
						nbabsent++;
						icone = "<span title=\"" + (new SimpleDateFormat("yyyy-MM-dd")).format(rs.getDate("airdate"))+" "+serie+"(S"+rs.getString("num_saison")+"E"+rs.getString("num_episodes")+")" + "\">-</span>";
					}else{
						nbavenir++;
						icone = "<span title=\"" + (new SimpleDateFormat("yyyy-MM-dd")).format(rs.getDate("airdate"))+" "+serie+"(S"+rs.getString("num_saison")+"E"+rs.getString("num_episodes")+")" + "\">_</span>";

						long nbjourprochainepisodes = rs.getDate("airdate").getTime() - Param.dateDuJour.getTime();
						strnbjourprochainepisodes = (nbjourprochainepisodes < 0 ? " -" : "")
						+ ((new SimpleDateFormat("yyyy-MM-dd")).format(rs.getDate("airdate")).equals("2099-01-31") ? "-----" : TimeUnit.MILLISECONDS
						.toDays((long) Math.abs(nbjourprochainepisodes))) + " days";	
					}
				}
			}
			
			while (visu.size() < Integer.parseInt(rs.getString("num_saison")))
			{
				visu.add(new ArrayList<String>());
			}
			ArrayList<String> getVisuSaison = visu.get(Integer.parseInt(rs.getString("num_saison")) - 1);
			while (getVisuSaison.size() < Integer.parseInt(rs.getString("num_episodes")))
			{
				getVisuSaison.add("");
			}
			getVisuSaison.set(Integer.parseInt(rs.getString("num_episodes")) - 1, icone);
			visu.set(Integer.parseInt(rs.getString("num_saison")) - 1, getVisuSaison);
			
		}
		rs.close();		 
		String titre = serie + " " + strnbjourprochainepisodes;
		
		/*JFreeChart*/
		DefaultPieDataset objDataset = new DefaultPieDataset();
		objDataset.setValue("nbpresent",nbpresent);
		objDataset.setValue("nbencours",nbencours);
		objDataset.setValue("nbabsent",nbabsent);
		objDataset.setValue("nbavenir",nbavenir);
		JFreeChart objChart = ChartFactory.createPieChart3D (
		    titre,   //Chart title
		    objDataset,          //Chart Data 
		    true,               // include legend?
		    true,               // include tooltips?
		    false               // include URLs?
		    );
        PieSectionLabelGenerator generator = new StandardPieSectionLabelGenerator(
                "{0} {1}", new DecimalFormat("0"), new DecimalFormat("0.00"));
        PiePlot3D plot = (PiePlot3D) objChart.getPlot();
        plot.setStartAngle(180);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        plot.setLabelGenerator(generator);
        int width = 320; /* Width of the image */
        int height = 240; /* Height of the image */ 
		//File pieChart = new File( "PieChart_"+serie+".jpeg" ); 

		//ChartUtilities.writeChartAsPNG( imageString , objChart , width , height );
		BufferedImage objBufferedImage=objChart.createBufferedImage(width,height);
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		try {
			ImageIO.write(objBufferedImage, "png", bas);
		} catch (IOException e) {
			 e.printStackTrace();
		}
		byte[] byteArray=bas.toByteArray();
	    
		String imagestatseriehtml = "<img src='data:image/png;base64," + DatatypeConverter.printBase64Binary(byteArray) + "'>";
		
		mettreimagestatmajserieserie(serie,imagestatseriehtml);
		String tableaustatseriehtml =Miseenforme(visu);
		mettretableaustatmajserieserie(serie,tableaustatseriehtml);
		String returnhtml = imagestatseriehtml;
		returnhtml = returnhtml + "</br>";
		returnhtml = returnhtml + tableaustatseriehtml;
		
		return returnhtml;
	}
	
	private static String Miseenforme(ArrayList<ArrayList<String>> visu)
	{
		String out = "<TABLE BORDER>";
		int nSaison = 0;
		for (ArrayList<String> listSaison : visu)
		{
			out += "<tr><td>";
			nSaison++;
			out += String.format("%1$02d", nSaison) + ":";
			String[] listEpisode = listSaison.toArray(new String[0]);
			int nEpisode = 0;
			for (String etatEpisode : listEpisode)
			{
				nEpisode++;
				if (nEpisode % 100 == 0)
				{
					out += "</td></tr><tr><td>..:";
				}
				out += etatEpisode;
			}
			out += "</td></tr>";
		}
		out += "</table>";// </table></body>";
		return out;
	}			
	
	private static void mettreenattenteepisode(String serie, String saison, ArrayList<Integer> episodes) throws SQLException {
		Statement stmt = Param.con.createStatement(
				ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String clausein = episodes.toString();
		stmt.executeUpdate("UPDATE episodes "
				 + " set freezesearchuntil = \""
				 + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateJourP7)
				 + "\""
				 + "WHERE "
				 + " serie = \"" + serie + "\""
				 + " and num_saison = \"" + saison + "\""			 
				 + " and num_episodes in (" +clausein.substring(1, clausein.length()-1) + ")"		
				 + " ");
	}

	private static void ajouterhashserie(String hash,String Magnet) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
//		stmt.executeUpdate("delete from hash "
//				 + " where  "
//				 + " hash = \"" + hash + "\" "			 
//				 + "");
		stmt.executeUpdate("insert into hash "
				 + " ( hash , classification , magnet ,timestamp_ajout) "
				 + "VALUE "
				 + " (\"" + hash + "\" ,"			 
				 + " \"serie\" ,"		
				 + " \"" + Magnet + "\" ,"		
				 + " \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "\""
				 + " ) "
				 + " ON DUPLICATE KEY UPDATE "
				 + " timestamp_ajout=\"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "\" ,"
				 + " classification=\"serie\""
				 + "");
	}
	
	private static void mettreepisodeaencours(String serie, String numsaison, String numepisode) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE episodes "
				 + " set encours = true "
				 + "WHERE "
				 + " serie = \"" + serie + "\""
				 + " and num_saison = \"" + numsaison + "\""			 
				 + " and num_episodes = \"" + numepisode + "\""		
				 + " ");
	}
	
	private static void mettretoutelasaisonaencours(String serie, String numsaison) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE episodes "
				 + " set encours = true "
				 + "WHERE "
				 + " serie = \"" + serie + "\""
				 + " and num_saison = \"" + numsaison + "\""			 
				 + " ");
	}

	private static void mettredatemajserieserie(String serie) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE series "
				 + " set date_maj_web = \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "\""
				 + "WHERE "
				 + " nom = \"" + serie + "\""		
				 + " ");
	}
	
	private static void mettretableaustatmajserieserie(String serie ,String Stat_Tableau) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = stmt.executeQuery("SELECT *"
				 + " FROM series "
				 + " WHERE "
				 + " nom = \"" + serie + "\""	
				 + "  ");
		while (rs.next() )
		{
				rs.updateString("Stat_Tableau", Stat_Tableau);
				rs.updateRow();
		}
		rs.close();
	}
	private static void mettreimagestatmajserieserie(String serie ,String imagestat) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE series "
				 + " set Stat_Image_Base64 = \"" + imagestat + "\""
				 + "WHERE "
				 + " nom = \"" + serie + "\""		
				 + " ");
	}	
	private static boolean notlastsaisonactive(String serie, String num_saison) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT max(num_saison) as MaxSaison "
									 + " FROM episodes "
									 + " WHERE serie = \""+serie+"\" " 
									 + "   AND airdate < \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJourUsa) + "\""
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
		System.out.println("purgerrepertioiredetravail");
		if (Ssh.Fileexists( Param.CheminTemporaireTmp())){
			//deplacer fichier a la racine
			Ssh.executeAction("cd \"" + Param.CheminTemporaireTmp() + "\";find . -type f -iname '*.*' -exec mv '{}' \"" + Param.CheminTemporaireTmp() + "\" \\;");
			//purge rep=ertzooire v,wide
			Ssh.actionexecChmodR777(Param.CheminTemporaireTmp() );
			Ssh.executeAction("cd \"" + Param.CheminTemporaireTmp() + "\";find . -type d -depth -exec rmdir 2>/dev/null '{}' \\;");
		}
	}

	private static void rangerdownload(String[] args) throws SQLException, InterruptedException, JSchException, IOException
	{
		System.out.println("rangerdownload");
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
			String src = Param.CheminTemporaireSerie() + rs.getString("nom") + Param.Fileseparator;
			if (Ssh.Fileexists(src)){
				Ssh.copyFile(src,  rs.getString("repertoire") );
				Ssh.actionexecChmodR777(rs.getString("repertoire"));
			}
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
	 * @throws XmlRpcException 
	 *
	 */
	private static void analyserrepertoire(String[] args) throws SQLException, IOException, JSchException, InterruptedException, XmlRpcException
	{
		System.out.println("analyserrepertoire");
		String clausewhere = (args.length >0)?" WHERE nom = \""+args[0]+"\"":"";

		final List<Map<String, String>> listeret = new ArrayList<Map<String, String>>();
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT * "
									 + " FROM series "
									 + clausewhere
									 + "  ");
		while (rs.next())
		{
	    	getseriestathtml(rs.getString("nom"));
		    if ((Ssh.Fileexists(rs.getString("repertoire")))){
		    	ArrayList<String> files = Ssh.getRemoteFileList(rs.getString("repertoire"));
		    	for (String file:files){
		    		if (isvideo(file.toString())){
				    	if (!fichierdanslabaseepisodes(file.toString())){
					    	Map<String, String> ret =  new HashMap<String, String>();
					    	ret = conversionnom2episodes(file.toString());
							if (ret.get("serie") != null && !ret.get("serie").equals(""))
							{
								if(!episodesachemincomplet(ret)){
									ret.put("chemin", file.toString());	
									listeret.add(ret);
									Param.logger.debug("Present Ep:"+ret.get("serie")+" "+ret.get("saison")+"-"+ret.get("episode")+" File:" + file.toString()); 
								}
							}	
				    	}
		    		}
		    	}
		    	
		    	boolean top = false;
		    	ArrayList<String> filebase = chemincompletdelaserie(rs.getString("nom"));
		    	for (String fb:filebase){
		    		if (!files.contains(fb)){
		    			suprimerfichierdelabase(fb);
		    			top=true;
		    		}
		    	}
		    	if (top){getseriestathtml(rs.getString("nom"));}
	            
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

		for (Map<String, String> curr:listeret)
		{
			int ret = stmt.executeUpdate("UPDATE episodes "
				 + " set chemin_complet = \"" + curr.get("chemin") + "\""
				 + "   , timestamp_completer = \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "\""
				 + "WHERE "
				 + " serie = \"" + curr.get("serie") + "\""
				 + " and num_saison = \"" + curr.get("saison") + "\""
				 + " and num_episodes = \"" + curr.get("episode") + "\""				 
				 + " ");
			String seriestathtml = getseriestathtml((String) curr.get("serie"));
			if(Param.WordPressPost){
				String NomFichier=((String) curr.get("chemin")).substring((Math.max(((String) curr.get("chemin")).lastIndexOf('/'), ((String) curr.get("chemin")).lastIndexOf('\\')))+1);
				WordPressHome.publishOnBlog(
					6,
					(new SimpleDateFormat("yyyyMMdd_HHmmSS")).format(Param.dateDuJour) + "_" + NomFichier,
					NomFichier,
					new String[] {(String) curr.get("serie"), "S" + curr.get("saison"), "E" + curr.get("episode") },
					new String[] { "Serie" },
					/*"http://home.daisy-street.fr/BibPerso/stream.php?flux="*/
					"<a href=\""+Param.UrlduStreamerInterne
					+ URLEncoder.encode((String) curr.get("chemin"), "UTF-8") + "\">" + NomFichier + "</a>" + "\n"
					+ seriestathtml
					+ "" ) ;
			}
		}
	}

	private static void suprimerfichierdelabase(String fb) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE episodes "
				 + " set chemin_complet = null "
				 + " , timestamp_completer = null "
				 + "WHERE "
				 + " chemin_complet = \"" + fb + "\""				 
				 + " ");
	}

	private static boolean isvideo(String pathfile) {
		String extension = "";
		String[] Video = new String[] {"avi","mp4","mpg","mkv","wnv","divx"};
		
		int i = pathfile.lastIndexOf('.');
		int p = Math.max(pathfile.lastIndexOf('/'), pathfile.lastIndexOf('\\'));
		
		if (i > p) {
		    extension = pathfile.substring(i+1);
		}
		
		return Arrays.asList(Video).contains(extension.toLowerCase());
	}


	private static boolean fichierdanslabaseepisodes(String pathfile) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement();
		rs = stmt.executeQuery("SELECT * "
									 + " FROM episodes "
									 + " WHERE "
									 + "      chemin_complet = \"" + pathfile  + "\""
									 + "  ");
		rs.last();
		if (rs.getRow() == 0)
		{				
			return false;
		}
		rs.close();
		return true ;
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
		System.out.println("transmisson");
		ResultSet rs = null;
	  	List<TorrentStatus> torrents = Param.client.getAllTorrents(new TorrentField[] { TorrentField.hashString ,TorrentField.files,TorrentField.name,TorrentField.percentDone,TorrentField.activityDate ,TorrentField.eta});
		for (TorrentStatus curr : torrents)
		{
			String hash = (String) curr.getField(TorrentField.hashString);
			Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			rs = stmt.executeQuery("SELECT * "
										 + " FROM hash "
										 + " WHERE "
										 + "      hash = \"" + hash + "\""
										 + "  ");
			while (rs.next())
			{
				if (rs.getDate("timestamp_termine") != null)
				{
					transmission.supprimer_hash(hash);
				}
				else
				{
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
								if (ret == null || episodesachemincomplet(ret) )
								{
									nbfichierbruler ++;
									transmission.cancelFilenameOfTorrent(hash, i);
								}
								else
								{
									mettreepisodeaencours(ret.get("serie"),ret.get("saison"),ret.get("episode"));
									if (!n.get("bytesCompleted").equals(0))
									{
										if (n.get("bytesCompleted").equals(n.get("length")))
										{
											transmission.deplacer_fichier(hash, Param.CheminTemporaireSerie(),i);										
											nbfichierbruler ++;
										} else {
											Param.logger.debug("Encours Ep:"+ret.get("serie")+" "+ret.get("saison")+"-"+ret.get("episode")+" name:" + n.getString("name")); 
										}
									}
								}
							}
							if ( curr.getField(TorrentField.percentDone).equals(1))
							{
								if (listFile.length()==nbfichierbruler)
								{
									rs.updateString("classification","effacer");
									rs.updateRow();
								}
							}
							if (rs.getDate("timestamp_ajout").before(Param.dateJourM1))
							{
								long derniereactiviteilyaminutes = ((Param.dateDuJour.getTime()/1000) - (Integer)curr.getField(TorrentField.activityDate)) ;
								if (derniereactiviteilyaminutes > (Integer.parseInt( Param.minutesdinactivitesautorize) *60)){
									rs.updateString("classification","effacer");
									rs.updateRow();
								}
							}

							break;
						case "film": 
							if (curr.getField(TorrentField.percentDone).equals(1))
							{
								if (transmission.all_fichier_absent(hash))
								{
									rs.updateString("classification","effacer");
									rs.updateRow();
								}
								else
								{
									transmission.deplacer_fichier(hash, Param.CheminTemporaireFilm());
								}
							}
							if (rs.getDate("timestamp_ajout").before(Param.dateJourM1))
							{
								long derniereactiviteilyaminutes = ((Param.dateDuJour.getTime()/1000) - (Integer)curr.getField(TorrentField.activityDate)) ;
								if (derniereactiviteilyaminutes > (Integer.parseInt( Param.minutesdinactivitesautorize) *60)){
									rs.updateString("classification","filemaeffacer");
									rs.updateString("nom", (String) curr.getField(TorrentField.name));
									rs.updateRow();
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
							rs.updateString("nom", (String) curr.getField(TorrentField.name));
							rs.updateRow();
							break;
					}
				}
			}
			rs.close();
		}
	}

	private static boolean episodesachemincomplet(Map<String, String> ret) throws SQLException {
		if (ret == null){return false;}
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs ;
		rs = stmt.executeQuery("SELECT * "
									 + " FROM episodes "
									 + " WHERE "
									 + "      serie = \"" + ret.get("serie") + "\""
									 + "  and    num_saison = \"" + ret.get("saison") + "\""
									 + "  and    num_episodes = \"" + ret.get("episode") + "\""
									 + "  and 	  not(isnull(chemin_complet))"
									 + "");
		while (rs.next())
		{
				return true;
		}
		rs.close();
		return false;
	}

	private static ArrayList<String> chemincompletdelaserie(String serie) throws SQLException {
		ArrayList<String> ret = new ArrayList<String>();
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs ;
		rs = stmt.executeQuery("SELECT * "
									 + " FROM episodes "
									 + " WHERE "
									 + "      serie = \"" + serie + "\""
									 + "  and 	  chemin_complet IS NOT NULL"
									 + "");
		while (rs.next())
		{
			ret.add(rs.getString("chemin_complet"));;
		}
		rs.close();
		return ret;
	}
	private static void initialisation(String[] args) throws IOException, ParseException, SQLException, JSchException, InterruptedException 
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
								 + " Stat_Image_Base64 TEXT , "
								 + " Stat_Tableau TEXT , "
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
								 + " chemin_complet  VARCHAR(255) CHARACTER SET utf8 DEFAULT NULL , "
								 + " freezesearchuntil DATE , "
								 + " PRIMARY KEY ( serie , num_saison , num_episodes  ) , "
								 + "         INDEX   ( airdate ) "
								 + ") "
								 + " ");
		
		//Param.stmt.executeUpdate("DROP TABLE hash ");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS hash "
								 + "(hash VARCHAR(255) not NULL , "
								 + " nom VARCHAR(255) , "
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
	private static void alimentation_bdd(String[] args) throws SQLException, IOException, NumberFormatException, ParseException, JSchException, InterruptedException
	{
		System.out.println("alimentation_bdd");
//		FichierQR.AjouterNouvelleSerie();

		
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);

		ResultSet rs ;
		rs = stmt.executeQuery("SELECT series.nom , max(episodes.airdate) as MaxDate"
									 + " FROM series "
									 + " LEFT OUTER join episodes "
									 + "    on series.nom = episodes.serie "
									 + " WHERE "
									 + "      series.date_maj_web < \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateJourM30) + "\""
									 + "   OR series.date_maj_web IS NULL"
									 + " GROUP BY "
									 + "  series.nom");
		while (rs.next())
		{	
			if (rs.getObject("MaxDate")==null || (rs.getDate("MaxDate")).after(Param.dateDuJour) ){
				FileBot.maj_liste_episodes(rs.getString("nom"));
				mettredatemajserieserie(rs.getString("nom"));
			}else{
				if ((rs.getDate("MaxDate")).before(Param.dateJourM300))
				{ 
					FileBot.maj_liste_episodes(rs.getString("nom"));
					mettredatemajserieserie(rs.getString("nom"));
				}
/*				else
				{
					FichierQR.ForcerMajSerieWeb(rs.getString("nom"));
				}*/
			}
		}
		rs.close();

		stmt.executeUpdate("update episodes set encours = false ");

	  	List<TorrentStatus> torrents = Param.client.getAllTorrents(new TorrentField[] { TorrentField.hashString });
		for (TorrentStatus curr : torrents)
		{ 
			
			String hash = (String) curr.getField(TorrentField.hashString);
			rs = stmt.executeQuery("SELECT * "
										 + " FROM hash "
										 + " WHERE "
										 + "      hash = \"" + hash + "\""
										 + "  ");
			rs.last();
			if (rs.getRow() == 0)
			{				
				String sql = "INSERT INTO hash "
					+ " (hash, classification,timestamp_ajout) VALUES  " 
					+ " ( "
					+ " \"" + hash + "\" , "
					+ " 'autres' , "
					+ " \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour) + "\"  "
					+ " ) ";
				stmt.executeUpdate(sql);

			}
			rs.close();
		}

	}


	private static void cloture(String[] args) throws InterruptedException, IOException, SQLException
	{
		System.out.println("+        Fin       +" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		System.out.println("+---+----+----+----+");
		System.out.print("</pre>");

		Param.cloture();
	}
	
	public static Map<String, String> conversionnom2episodes(String fileName) throws SQLException
	{
		//Param.logger.debug("episode-" + "decomposerNom " + fileName);
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
			//Param.logger.debug("episode-" + "decomposerNom 4-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
			//			 + numeroSequentielTrouve.toString());
		}

		if (m3.find())
		{
			partname = namecmp.substring(0, m3.start(0));
			numeroSaisonTrouve.put("saison", m3.group(1).toString());
			numeroEpisodeTrouve.put("episode", m3.group(2).toString());
			//Param.logger.debug("episode-" + "decomposerNom 3-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
			//			 + numeroSequentielTrouve.toString());
		}

		if (m2.find())
		{
			numeroEpisodeTrouve.clear();
			numeroSaisonTrouve.clear();
			numeroSequentielTrouve.clear();
			partname = namecmp.substring(0, m2.start(0));
			numeroSaisonTrouve.put("saison", m2.group(1).toString());
			numeroEpisodeTrouve.put("episode", m2.group(2).toString());
			//Param.logger.debug("episode-" + "decomposerNom 2-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
			//			 + numeroSequentielTrouve.toString());
		}

		if (m5.find())
		{
			numeroEpisodeTrouve.clear();
			numeroSaisonTrouve.clear();
			numeroSequentielTrouve.clear();
			partname = namecmp.substring(0, m5.start(0));
			numeroSaisonTrouve.put("saison", m5.group(2).toString());
			numeroEpisodeTrouve.put("episode", m5.group(4).toString());
			//Param.logger.debug("episode-" + "decomposerNom 5-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
			//			 + numeroSequentielTrouve.toString());
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
				//Param.logger.debug("episode-" + "decomposerNom 6-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
				//			 + numeroSequentielTrouve.toString());
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
							//Param.logger.debug("episode-" + "decomposerNom 1-" + partname + " " + numeroSaisonTrouve.toString() + " "
							//			 + numeroEpisodeTrouve.toString() + " " + numeroSequentielTrouve.toString());
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
					//Param.logger.debug("episode- decomposerNom" + rs.getString("nom"));
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
