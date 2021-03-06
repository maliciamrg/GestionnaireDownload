package com.maliciamrg.gestion.download;



/*
 * Copyright (c) 2015 by Malicia All rights reserved.
 * 
 * 26 mars 2015
 * 
 * 
 */
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.benow.transmission.model.TorrentStatus;
import ca.benow.transmission.model.TorrentStatus.TorrentField;

import com.jcraft.jsch.JSchException;
import com.maliciamrg.homeFunction.*;
import com.mysql.jdbc.PreparedStatement;

import java.io.*;

// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 */
public class Main {

	/**
	 * lecture parametrage transmission : addres , user , mdp db2 : adres , user
	 * , mdp nbtelechargementseriesimultaner
	 * 
	 * initilisation bdd (table) series nom repertoire date maj web episodes
	 * airdate serie num saison num episodes nom encours (o/n) timestamp
	 * completer chemin complet hash hash classification (default autres) magnet
	 * timestamp ajout timestamp terminé
	 * 
	 * alimentation bdd serie: ajouter de nouvelle series -> fichier qr html
	 * mettre a non l indicateur encours de tout les episodes si date maj web >
	 * 30 jours si derniere airdate < 300 jours recuperer/maj les listes
	 * d'episodes via filebot sinon proposer maj web -> fichier qr html
	 * transmission ajouter a la bdd les non present entant que autres
	 * 
	 * transmisson hash dans la base & not timestamp terminé si timestamp ajout
	 * plus de 24h : demander classification effacer -> fichier qr html
	 * (clasification du hash) serie: telechargement en cours si le fichier
	 * telecharger est un episode (conversionnom2episodes) mettre l'indicateur
	 * encours a oui sinon annuler le fichier serie ou film: telechargement
	 * terminé deplacer fichiers dans le reperoire temporaire telechargement
	 * terminé & fichier absent: supprimer le telechargement de transmission &
	 * timestamp terminé effacer: supprimer le telechargement de transmission &
	 * timestamp terminé ignorer: ne rien faire & timestamp terminé autres:
	 * demander classification -> fichier qr html
	 * 
	 * ranger les downloads (filebot) a partir du repertoire temporaire series
	 * film
	 * 
	 * purger repertoire temporaire serie = repertojretmp + nom_series film =
	 * repertoiretmp + "film"
	 * 
	 * analyser repertoire serie: lister les episodes present
	 * (conversionnom2episodes) --> bdd episodes.chemincomplet & timestamp
	 * completer
	 * 
	 * lancer les prochains hash serie: nbserieencours = nb hash de class serie
	 * sans timestamp terminé nbmagnetachercher =
	 * param.nbtelechargementseriesimultaner - nbserieencours constituer la
	 * liste de episodes non completer et nonencours avec airdate < dtjour trier
	 * par airdate decroissante boucle tant que nbmagnetachercher > 0 recupere
	 * l'episodes suivant de la liste et le mettre a encours si l'airdate a plus
	 * de 300 jours ou la saison n'est pas la dernier saison du dernier episods
	 * ayant une airdate < dtjour recherche du magnet de la saison mettre toute
	 * la saison a encours sinon recherche du magnet de l'episode ajoute le
	 * magnet a transmission nbmagnetachercher - 1
	 *
	 * @param args
	 *            the arguments
	 * @throws Exception
	 */

	public static void main(String[] args)
	{
		say("main-start");
		(new File("error.txt")).delete();
		try
		{
			try
			{
				initialisation(args);
				mise_a_jour_mpd(args);
				List<String> arrayArgs = Arrays.asList(args);
				if (arrayArgs.contains("--majbase"))
				{
					say("majbase-start");
					alimentation_bdd(args);
					say("majbase-stop");
				}
				if (arrayArgs.contains("--gestiontransmission"))
				{
					say("gestiontransmission-start");
					synchronisation_bdd_transmission();
					transmisson(args);
					lancerlesprochainshash(args);
					say("gestiontransmission-stop");
				}
				if (arrayArgs.contains("--rangerlesrepertoires"))
				{
					say("rangerlesrepertoires-start");
					// mettre l'indicateur "encours" de tout les episodes a zero
					ArrayList<String> fileexclu = rangerdownload(args);
					purgerrepertioiredetravail(args, fileexclu);
					say("rangerlesrepertoires-stop");
				}
				if (arrayArgs.contains("--analyserrepertoire"))
				{
					say("analyserrepertoire-start");
					analyserrepertoire(args);
					say("analyserrepertoire-stop");
				}
	
			}
			catch (InterruptedException e)
			{
				say("InterruptedException-start");
				PrintWriter writer = new PrintWriter("error.txt", "UTF-8");
				writer.println(Param.eToString(e));
				writer.close();
				e.printStackTrace();
				say("InterruptedException-stop");
			}
			catch (NumberFormatException e)
			{
				say("NumberFormatException-start");
				PrintWriter writer = new PrintWriter("error.txt", "UTF-8");
				writer.println(Param.eToString(e));
				writer.close();
				e.printStackTrace();
				say("NumberFormatException-stop");
			}
			catch (SQLException e)
			{
				say("SQLException-start");
				PrintWriter writer = new PrintWriter("error.txt", "UTF-8");
				writer.println(Param.eToString(e));
				writer.close();
				e.printStackTrace();
				say("SQLException-stop");
			}
			catch (IOException e)
			{
				say("IOException-start");
				PrintWriter writer = new PrintWriter("error.txt", "UTF-8");
				writer.println(Param.eToString(e));
				writer.close();
				e.printStackTrace();
				say("IOException-stop");
			}
			catch (JSchException e)
			{
				say("JSchException-start");
				PrintWriter writer = new PrintWriter("error.txt", "UTF-8");
				writer.println(Param.eToString(e));
				writer.close();
				e.printStackTrace();
				say("JSchException-stop");
			}
			catch (ParseException e)
			{
				say("ParseException-start");
				PrintWriter writer = new PrintWriter("error.txt", "UTF-8");
				writer.println(Param.eToString(e));
				writer.close();
				e.printStackTrace();
				say("ParseException-stop");
			}
			catch (XmlRpcException e)
			{
				say("XmlRpcException-start");
				PrintWriter writer = new PrintWriter("error.txt", "UTF-8");
				writer.println(Param.eToString(e));
				writer.close();
				e.printStackTrace();
				say("XmlRpcException-stop");
			}
		}
		catch (FileNotFoundException e)
		{
			say("FileNotFoundException-start");
			e.printStackTrace();
			say("FileNotFoundException-stop");
		}
		catch (UnsupportedEncodingException e)
		{
			say("UnsupportedEncodingException-start");
			e.printStackTrace();
			say("UnsupportedEncodingException-stop");
		}
		
		try {
			say("cloture-start");
			cloture(args);
			say("cloture-stop");
		} catch (InterruptedException | IOException | SQLException e) {
			say("clotureException-start");
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
			say("clotureException-stop");
		}	
		
		say("main-stop");
	}
	
	private static void say(String string) {
		System.out.println("-="+string+"=-");
	}

	/**
	 * lancer les prochains hash serie: nbserieencours = nb hash de class serie
	 * sans timestamp terminé nbmagnetachercher =
	 * param.nbtelechargementseriesimultaner - nbserieencours constituer la
	 * liste de episodes non completer et nonencours avec airdate < dtjour trier
	 * par airdate decroissante boucle tant que nbmagnetachercher > 0 recupere
	 * l'episodes suivant de la liste et le mettre a encours si l'airdate a plus
	 * de 300 jours ou la saison n'est pas la dernier saison du dernier episods
	 * ayant une airdate < dtjour recherche du magnet de la saison mettre toute
	 * la saison a encours sinon recherche du magnet de l'episode ajoute le
	 * magnet a transmission nbmagnetachercher - 1
	 *
	 * @param args
	 *            the args
	 * @throws SQLException
	 *             the SQL exception
	 * @throws NumberFormatException
	 *             the number format exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void lancerlesprochainshash(String[] args) throws SQLException, NumberFormatException, IOException {
		System.out.println("lancerlesprochainshash");
		int nbserieencours = nbhashserienonterminee();
		int nbmagnetachercher = Integer.parseInt(Param.props.getProperty("Telechargement.nbseriesimultaner")) - nbserieencours;

		while (nbmagnetachercher > 0) {

			String serie = "";
			String saison = "";
			Map<String, String> ret = recupererprochainesaisonarechercher();

			serie = ret.get("serie");
			saison = ret.get("saison");
			if (serie == "" || saison == "") {
				break;
			}

			ArrayList<Integer> episodes = recupurerlisteepisodesarechercher(ret.get("serie"), ret.get("saison"));
			ArrayList<Integer> sequentiel = recupurerlistesequentielarechercher(ret.get("serie"), ret.get("saison"));

			mettretoutelasaisonaencours(serie, saison);

			int nbepsaison = Integer.parseInt(nbepisodesaison(serie, saison));

			// recherchede la saison complete
			ArrayList<String> magnet = Torrent.getMagnetFor(serie, Integer.parseInt(saison), null, null, episodes, nbepsaison);

			// recherche des ep_sodes par episosdes
			if (magnet.size() == 0) {

				for (Integer ep : episodes) {
					magnet.addAll(Torrent.getMagnetFor(serie, Integer.parseInt(saison), ep, null, null, nbepsaison));
				}

			}

			// recherche des episodrs sequentiel par sequentiel
			if (magnet.size() == 0) {
				for (Integer seq : sequentiel) {
					magnet.addAll(Torrent.getMagnetFor(serie, Integer.parseInt(saison), null, seq, null, nbepsaison));
				}
			}

			if (magnet.size() == 0) {
				mettreenattenteepisode(serie, saison, episodes);
			}

			for (String strMagnet : magnet) {
				if (transmission.ajouterlemagnetatransmission(strMagnet)) {
					String strHash = "";
					int debSubStr = strMagnet.indexOf("btih:") + 5;
					int finSubStr = strMagnet.indexOf("&");
					strHash = strMagnet.substring(debSubStr, finSubStr);
					if (!strHash.equals("")) {
						ajouterhashserie(strHash, strMagnet);
						Param.logger.debug("Torrent Ep:" + serie + " " + saison + "-" + episodes.toString() + " Magnet:" + strMagnet);
					}
					nbmagnetachercher--;
				}
			}

		}
	}

	/**
	 * Recupurerlisteepisodesarechercher.
	 *
	 * @param serie
	 *            the serie
	 * @param saison
	 *            the saison
	 * @return the array list
	 * @throws SQLException
	 *             the SQL exception
	 * @throws NumberFormatException
	 *             the number format exception
	 */
	private static ArrayList<Integer> recupurerlisteepisodesarechercher(String serie, String saison) throws SQLException, NumberFormatException {
		ArrayList<Integer> episodes = new ArrayList<Integer>();
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String sql = ("SELECT num_episodes" + " FROM episodes " + " WHERE " + "      NOT encours "
				+ "  AND ( isnull(chemin_complet) or chemin_complet = \"\" )" + "  AND serie=\"" + serie + "\"" + "  AND num_saison=\"" + saison + "\""
				+ "  AND airdate < \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJourUsa) + "\"" + "");
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			episodes.add(Integer.parseInt(rs.getString("num_episodes")));
		}
		rs.close();
		return episodes;
	}

	/**
	 * recupurerlistesequentielarechercher.
	 *
	 * @param serie
	 *            the serie
	 * @param saison
	 *            the saison
	 * @return the array list
	 * @throws SQLException
	 *             the SQL exception
	 * @throws NumberFormatException
	 *             the number format exception
	 */
	private static ArrayList<Integer> recupurerlistesequentielarechercher(String serie, String saison) throws SQLException, NumberFormatException {
		ArrayList<Integer> sequentiels = new ArrayList<Integer>();
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String sql = ("SELECT sequentiel" + " FROM episodes " + " WHERE " + "      NOT encours " + "  AND ( isnull(chemin_complet) or chemin_complet = \"\" )"
				+ "  AND serie=\"" + serie + "\"" + "  AND num_saison=\"" + saison + "\"" + "  AND airdate < \""
				+ (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJourUsa) + "\"" + "");
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			sequentiels.add(Integer.parseInt(rs.getString("sequentiel")));
		}
		rs.close();
		return sequentiels;
	}

	/**
	 * Recupererprochainesaisonarechercher.
	 *
	 * @return the map
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static Map<String, String> recupererprochainesaisonarechercher() throws SQLException {
		Map<String, String> ret = new HashMap<String, String>();
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String sql = ("SELECT serie , num_saison" + " FROM episodes " + " WHERE " + "      NOT encours "
				+ "  AND ( isnull(chemin_complet) or chemin_complet = \"\" )"
				// +
				// "  AND ( isnull(freezesearchuntil) or freezesearchuntil < \""
				// + (new
				// SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour()) +
				// "\")"
				+ "  AND airdate < \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJourUsa) + "\"" + " ORDER BY " + "  airdate Desc");
		rs = stmt.executeQuery(sql);
		rs.first();
		if (rs.getRow() > 0) {
			ret.put("serie", rs.getString("serie"));
			ret.put("saison", rs.getString("num_saison"));
		} else {
			ret.put("serie", "");
			ret.put("saison", "");
		}
		rs.close();
		return ret;
	}

	/**
	 * Nbepisodesaison.
	 *
	 * @param serie
	 *            the serie
	 * @param num_saison
	 *            the num_saison
	 * @return the string
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static String nbepisodesaison(String serie, String num_saison) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT count(*) as nbEpisodes" + " FROM episodes " + " WHERE " + "      serie = \"" + serie + "\" " + "  AND num_saison  = \""
				+ num_saison + "\" " + "  ");
		while (rs.next()) {
			return (rs.getString("nbEpisodes"));
		}
		rs.close();
		return "0";
	}

	/**
	 * Gets the seriestathtml.
	 *
	 * @param serie
	 *            the serie
	 * @return the seriestathtml
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static String getseriestathtml(String serie) throws SQLException {
		System.out.println("getseriestathtml " + serie);
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT *" + " FROM episodes " + " WHERE " + "      serie = \"" + serie + "\"" + " ORDER BY " + "  airdate Desc" + "");
		int nbtotal = 0;
		int nbpresent = 0;
		int nbencours = 0;
		int nbabsent = 0;
		int nbavenir = 0;
		ArrayList<ArrayList<String>> visu = new ArrayList<ArrayList<String>>();
		String strnbjourprochainepisodes = "";
		while (rs.next()) {
			nbtotal++;
			String icone = "#";
			if (rs.getString("chemin_complet") != null) {
				nbpresent++;
				icone = "X";
			} else {
				if (rs.getBoolean("encours")) {
					nbencours++;
					icone = ">";
				} else {
					if ((rs.getDate("airdate")).before(Param.dateDuJourUsa)) {
						nbabsent++;
						icone = "<span title=\"" + (new SimpleDateFormat("yyyy-MM-dd")).format(rs.getDate("airdate")) + " " + serie + "(S"
								+ rs.getString("num_saison") + "E" + rs.getString("num_episodes") + ")" + "\">-</span>";
					} else {
						nbavenir++;
						icone = "<span title=\"" + (new SimpleDateFormat("yyyy-MM-dd")).format(rs.getDate("airdate")) + " " + serie + "(S"
								+ rs.getString("num_saison") + "E" + rs.getString("num_episodes") + ")" + "\">_</span>";

						long nbjourprochainepisodes = rs.getDate("airdate").getTime() - Param.dateDuJour().getTime();
						strnbjourprochainepisodes = (nbjourprochainepisodes < 0 ? " -" : "")
								+ ((new SimpleDateFormat("yyyy-MM-dd")).format(rs.getDate("airdate")).equals("2099-01-31") ? "-----" : TimeUnit.MILLISECONDS
										.toDays(Math.abs(nbjourprochainepisodes))) + " days";
					}
				}
			}

			while (visu.size() < Integer.parseInt(rs.getString("num_saison"))) {
				visu.add(new ArrayList<String>());
			}
			ArrayList<String> getVisuSaison = visu.get(Integer.parseInt(rs.getString("num_saison")) - 1);
			while (getVisuSaison.size() < Integer.parseInt(rs.getString("num_episodes"))) {
				getVisuSaison.add("");
			}
			getVisuSaison.set(Integer.parseInt(rs.getString("num_episodes")) - 1, icone);
			visu.set(Integer.parseInt(rs.getString("num_saison")) - 1, getVisuSaison);

		}
		rs.close();

		String imagestatseriehtml = new HomeFunction().generate_image_resumer_serie(serie, strnbjourprochainepisodes, nbpresent, nbencours, nbabsent, nbavenir);

		mettreimagestatmajserieserie(serie, imagestatseriehtml);
		String tableaustatseriehtml = Miseenforme(visu);
		mettretableaustatmajserieserie(serie, tableaustatseriehtml);
		String returnhtml = imagestatseriehtml;
		returnhtml = returnhtml + "</br>";
		returnhtml = returnhtml + tableaustatseriehtml;

		return returnhtml;
	}

	/**
	 * Miseenforme.
	 *
	 * @param visu
	 *            the visu
	 * @return the string
	 */
	private static String Miseenforme(ArrayList<ArrayList<String>> visu) {
		String out = "<TABLE BORDER>";
		int nSaison = 0;
		for (ArrayList<String> listSaison : visu) {
			out += "<tr><td>";
			nSaison++;
			out += String.format("%1$02d", nSaison) + ":";
			String[] listEpisode = listSaison.toArray(new String[0]);
			int nEpisode = 0;
			for (String etatEpisode : listEpisode) {
				nEpisode++;
				if (nEpisode % 100 == 0) {
					out += "</td></tr><tr><td>..:";
				}
				out += etatEpisode;
			}
			out += "</td></tr>";
		}
		out += "</table>";// </table></body>";
		return out;
	}

	/**
	 * Mettreenattenteepisode.
	 *
	 * @param serie
	 *            the serie
	 * @param saison
	 *            the saison
	 * @param episodes
	 *            the episodes
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static void mettreenattenteepisode(String serie, String saison, ArrayList<Integer> episodes) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String clausein = episodes.toString();
		stmt.executeUpdate("UPDATE episodes " + " set freezesearchuntil = \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateJourP7) + "\"" + "WHERE "
				+ " serie = \"" + serie + "\"" + " and num_saison = \"" + saison + "\"" + " and num_episodes in ("
				+ clausein.substring(1, clausein.length() - 1) + ")" + " ");
	}

	/**
	 * Ajouterhashserie.
	 *
	 * @param hash
	 *            the hash
	 * @param Magnet
	 *            the magnet
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static void ajouterhashserie(String hash, String Magnet) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		// stmt.executeUpdate("delete from hash "
		// + " where  "
		// + " hash = \"" + hash + "\" "
		// + "");
		stmt.executeUpdate("insert into hash " + " ( hash , classification , magnet ,timestamp_ajout) " + "VALUE " + " (\"" + hash + "\" ," + " \"serie\" ,"
				+ " \"" + Magnet + "\" ," + " \"" + (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(Param.dateDuJour()) + "\"" + " ) "
				+ " ON DUPLICATE KEY UPDATE " + " timestamp_ajout=\"" + (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(Param.dateDuJour()) + "\" ,"
				+ " classification=\"serie\"" + "");
	}

	/**
	 * Mettreepisodeaencours.
	 *
	 * @param serie
	 *            the serie
	 * @param numsaison
	 *            the numsaison
	 * @param numepisode
	 *            the numepisode
	 * @param sequentiel
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static void mettreepisodeaencours(String serie, String numsaison, String numepisode, String sequentiel) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		if (sequentiel.equals("000")) {
			stmt.executeUpdate("UPDATE episodes " + " set encours = true " + "WHERE " + " serie = \"" + serie + "\"" + " and num_saison = \"" + numsaison
					+ "\"" + " and num_episodes = \"" + numepisode + "\"" + " ");
		} else {
			stmt.executeUpdate("UPDATE episodes " + " set encours = true " + "WHERE " + " serie = \"" + serie + "\"" + " and sequentiel = \"" + sequentiel
					+ "\"" + " ");
		}
	}

	/**
	 * Mettretoutelasaisonaencours.
	 *
	 * @param serie
	 *            the serie
	 * @param numsaison
	 *            the numsaison
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static void mettretoutelasaisonaencours(String serie, String numsaison) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE episodes " + " set encours = true " + "WHERE " + " serie = \"" + serie + "\"" + " and num_saison = \"" + numsaison + "\""
				+ " ");
	}

	/**
	 * Mettretableaustatmajserieserie.
	 *
	 * @param serie
	 *            the serie
	 * @param Stat_Tableau
	 *            the stat_ tableau
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static void mettretableaustatmajserieserie(String serie, String Stat_Tableau) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = stmt.executeQuery("SELECT *" + " FROM series " + " WHERE " + " nom = \"" + serie + "\"" + "  ");
		while (rs.next()) {
			rs.updateString("Stat_Tableau", Stat_Tableau);
			rs.updateRow();
		}
		rs.close();
	}

	/**
	 * Mettreimagestatmajserieserie.
	 *
	 * @param serie
	 *            the serie
	 * @param imagestat
	 *            the imagestat
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static void mettreimagestatmajserieserie(String serie, String imagestat) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE series " + " set Stat_Image_Base64 = \"" + imagestat + "\"" + "WHERE " + " nom = \"" + serie + "\"" + " ");
	}

	/**
	 * Notlastsaisonactive.
	 *
	 * @param serie
	 *            the serie
	 * @param num_saison
	 *            the num_saison
	 * @return true, if successful
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static boolean notlastsaisonactive(String serie, String num_saison) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT max(num_saison) as MaxSaison " + " FROM episodes " + " WHERE serie = \"" + serie + "\" " + "   AND airdate < \""
				+ (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJourUsa) + "\"" + "  ");
		while (rs.next()) {
			int MaxSaison = rs.getInt("MaxSaison");
			if (MaxSaison > Integer.parseInt(num_saison)) {
				return true;
			} else {
				return false;
			}
		}
		rs.close();
		return false;
	}

	/**
	 * Nbhashserienonterminee.
	 *
	 * @return the int
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static int nbhashserienonterminee() throws SQLException {
		int ret = 0;
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT count(*) as NBHash " + " FROM hash " + " WHERE classification = \"serie\"" + "   AND timestamp_termine is null " + "  ");
		while (rs.next()) {
			ret = rs.getInt("NBHash");
		}
		rs.close();
		return ret;
	}

	/**
	 * Purgerrepertioiredetravail.
	 *
	 * @param args
	 *            the args
	 * @param listefichierexclu
	 * @throws JSchException
	 *             the j sch exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws SQLException 
	 */
	private static void purgerrepertioiredetravail(String[] args, ArrayList<String> listefichierexclu) throws JSchException, IOException, InterruptedException, SQLException {
		System.out.println("purgerrepertioiredetravail");

		ArrayList<String> listfichiertransmission = new ArrayList<String>(0);
		List<TorrentStatus> torrents = Param.client.getAllTorrents(new TorrentField[] { TorrentField.hashString, TorrentField.files, TorrentField.name,
				TorrentField.percentDone, TorrentField.activityDate, TorrentField.eta });
		for (TorrentStatus curr : torrents) {
			JSONArray listFile = ((JSONArray) curr.getField(TorrentField.files));
			int i = 0;
			for (i = 0; i < listFile.length(); i++) {
				JSONObject n = (JSONObject) listFile.get(i);
				if (isvideo(n.getString("name"))) {
					listfichiertransmission.add(Param.CheminTemporaireSerie() + n.getString("name"));
					listfichiertransmission.add(Param.CheminTemporaireSerie() + n.getString("name") + ".part");
				}
			}
		}

		listefichierexclu.removeAll(listfichiertransmission);
		for (String fileExclu : listefichierexclu) {
			Ssh.executeAction("rm \"" + fileExclu + "\"");
		}

		// suprimer repertoire vide
		if (Ssh.Fileexists(Param.CheminTemporaireTmp())) {
			Ssh.executeAction("cd \"" + Param.CheminTemporaireTmp() + "\";find . -type d -empty -print -delete ");
		}
	}

	/**
	 * Rangerdownload.
	 *
	 * @param args
	 *            the args
	 * @throws SQLException
	 *             the SQL exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws JSchException
	 *             the j sch exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws XmlRpcException
	 */
	private static ArrayList<String> rangerdownload(String[] args) throws SQLException, InterruptedException, JSchException, IOException, XmlRpcException {
		System.out.println("rangerdownload");

		ArrayList<String> listefichierexclu = new ArrayList<String>(0);

		// ranger les serie dans les sous repertoire de tmp
		if (Ssh.getRemoteFileList(Param.CheminTemporaireSerie()).size() > 0) {
			listefichierexclu = FileBot.rangerserie(Param.CheminTemporaireSerie(), Param.CheminTemporaireSerie(), Param.props.getProperty("Repertoire.Film"));
		}

		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

		rs = stmt.executeQuery("SELECT * " + " FROM series " + "  ");
		while (rs.next()) {
			String src = Param.CheminTemporaireSerie() + rs.getString("nom") + Param.Fileseparator;
			if (Ssh.Fileexists(src)) {
				Ssh.copyRepertoireFileVideo(src, rs.getString("repertoire"));
				Ssh.actionexecChmodR777(rs.getString("repertoire"));
				FileBot.getsubtitleserie(rs.getString("repertoire"));
			}
		}
		rs.close();

		/*
		 * les films sont directioment deplaceer dans le bon repertoire par la
		 * fonction rangerserie
		 */

		// if (Ssh.Fileexists(Param.CheminTemporaireFilm()))
		// {
		// if (Ssh.getRemoteFileList(Param.CheminTemporaireFilm()).size() > 0) {
		// FileBot.rangerfilm(Param.CheminTemporaireFilm(),
		// Param.RepertoireFilm);
		// }
		// }

		// de mise a encours des episodes
		stmt.executeUpdate("update episodes set encours = false "/*+"where timestamp_completer is not null"*/);
		return listefichierexclu;
	}

	/**
	 * analyser repertoire serie: lister les episodes present
	 * (conversionnom2episodes) --> bdd episodes.chemincomplet & timestamp
	 * completer
	 *
	 * @param args
	 *            the args
	 * @throws SQLException
	 *             the SQL exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws JSchException
	 *             the j sch exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws XmlRpcException
	 *             the xml rpc exception
	 */
	private static void analyserrepertoire(String[] args) throws SQLException, IOException, JSchException, InterruptedException, XmlRpcException {
		System.out.println("analyserrepertoire");
		
		List<String> arrayArgs = Arrays.asList(args);
		
		ArrayList<String> argsclean = new ArrayList<String>(0);
		for (int i = 0; i < args.length; i++) {
			if (!args[i].substring(0, 2).equals("--")) {
				argsclean.add(args[i]);
			}
		}
		String[] argscleans = argsclean.toArray(new String[0]);
		String clausewhere = (argscleans.length > 0) ? " WHERE nom = \"" + argscleans[0] + "\"" : "";

		final List<Map<String, String>> listeret = new ArrayList<Map<String, String>>();
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery("SELECT * " + " FROM series " + clausewhere + "  ");

		while (rs.next()) {
			Param.logger.debug(rs.getString("nom"));

			if (arrayArgs.contains("--rangerlesrepertoires")) {
				Ssh.executeAction("find \"" + rs.getString("repertoire") + "\" -name \\*.nfo -exec rm {} \\;");
				FileBot.rangerserie(rs.getString("repertoire"), Param.getlastPath(rs.getString("repertoire")), Param.props.getProperty("Repertoire.Film"));
				Ssh.executeAction("cd \"" + rs.getString("repertoire") + "\";find . -type d -delete \\;");
			}
			
			getseriestathtml(rs.getString("nom"));

			if ((Ssh.Fileexists(rs.getString("repertoire")))) {
				Param.logger.debug("   " + rs.getString("repertoire"));
				ArrayList<String> files = Ssh.getRemoteFileList(rs.getString("repertoire"));
				for (String file : files) {
					if (isvideo(file.toString())) {
						//if (!fichierdanslabaseepisodes(file.toString())) {
							Map<String, String> ret = new HashMap<String, String>();
							ret = conversionnom2episodes(file.toString());
							if (ret.get("serie").equals("") || ret.get("saison").equals("000") || ret.get("episode").equals("000")) {

							} else {
								if (!episodesachemincomplet(ret)) {
									ret.put("chemin", file.toString());
									listeret.add(ret);
									Param.logger.debug("Present Ep:" + ret.get("serie") + " " + ret.get("saison") + "-" + ret.get("episode")+ "-" + ret.get("episodebis") + " File:"
											+ file.toString());
								}
							}
						//}
					}
				}

				boolean top = false;
				ArrayList<String> filebase = chemincompletdelaserie(rs.getString("nom"));
				for (String fb : filebase) {
					if (!files.contains(fb)) {
						suprimerfichierdelabase(fb);
						top = true;
					}
				}
				if (top) {
					getseriestathtml(rs.getString("nom"));
				}

				/*
				 * Path startPath = Paths.get(rs.getString("repertoire"));
				 * Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				 * 
				 * @Override public FileVisitResult visitFile(Path file,
				 * BasicFileAttributes attrs) { Map<String, String> ret = new
				 * HashMap<String, String>(); try { ret =
				 * conversionnom2episodes(file.toString()); } catch
				 * (SQLException e) { e.printStackTrace(); } if
				 * (ret.get("serie") == null) { ret.put("chemin",
				 * file.toString()); listeret.add(ret); }
				 * System.out.println("File: " + file.toString()); return
				 * FileVisitResult.CONTINUE; } });
				 */
			}

		}
		rs.close();

		for (Map<String, String> curr : listeret) {
			ArrayList<String> arryterms = new ArrayList<String>(0);
			arryterms.add(curr.get("serie"));
			arryterms.add( "S" + curr.get("saison"));

			if (curr.get("episode")!=null && curr.get("episode") != "000")  {
				Param.logger.debug("update : " + curr.get("serie") + " S" + curr.get("saison") + "E" + curr.get("episode") + " -> " + curr.get("chemin"));
				int ret = stmt.executeUpdate("UPDATE episodes " + " set chemin_complet = \"" + curr.get("chemin") + "\"" + "   , timestamp_completer = \""
						+ (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour()) + "\"" + "WHERE " + " serie = \"" + curr.get("serie") + "\""
						+ " and num_saison = \"" + curr.get("saison") + "\"" + " and num_episodes = \"" + curr.get("episode") + "\"" + " ");
				arryterms.add("E" + curr.get("episode"));
			}
			if  (curr.get("episodebis")!=null && curr.get("episodebis") != "000")  {
				Param.logger.debug("update : " + curr.get("serie") + " S" + curr.get("saison") + "E" + curr.get("episodebis") + " -> " + curr.get("chemin"));
				int ret = stmt.executeUpdate("UPDATE episodes " + " set chemin_complet = \"" + curr.get("chemin") + "\"" + "   , timestamp_completer = \""
						+ (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour()) + "\"" + "WHERE " + " serie = \"" + curr.get("serie") + "\""
						+ " and num_saison = \"" + curr.get("saison") + "\"" + " and num_episodes = \"" + curr.get("episodebis") + "\"" + " ");
				arryterms.add( "E" + curr.get("episodebis"));
			}
			String seriestathtml = getseriestathtml(curr.get("serie"));
			String NomFichier = curr.get("chemin").substring((Math.max(curr.get("chemin").lastIndexOf('/'), curr.get("chemin").lastIndexOf('\\'))) + 1);
			WordPressHome.publishOnBlog(6, (new SimpleDateFormat("yyyyMMdd_HHmmSS")).format(Param.dateDuJour()) + "_" + NomFichier, NomFichier,
					arryterms.toArray(new String[0]) , new String[] { "Serie" },
					/* "http://home.daisy-street.fr/BibPerso/stream.php?flux=" */
					"<a href=\"" + Param.props.getProperty("Url.StreamerInterne") + URLEncoder.encode(curr.get("chemin"), "UTF-8") + "\">" + NomFichier
							+ "</a>" + "\n" + seriestathtml + "");

		}
	}

	/**
	 * Suprimerfichierdelabase.
	 *
	 * @param fb
	 *            the fb
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static void suprimerfichierdelabase(String fb) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE episodes " + " set chemin_complet = null " + " , timestamp_completer = null " + "WHERE " + " chemin_complet = \"" + fb
				+ "\"" + " ");
	}

	/**
	 * Checks if is video.
	 *
	 * @param pathfile
	 *            the pathfile
	 * @return true, if is video
	 */
	private static boolean isvideo(String pathfile) {
		String extension = "";
		String[] Video = new String[] { "avi", "mp4", "mpg", "mkv", "wmv", "divx" };

		int i = pathfile.lastIndexOf('.');
		int p = Math.max(pathfile.lastIndexOf('/'), pathfile.lastIndexOf('\\'));

		if (i > p) {
			extension = pathfile.substring(i + 1);
		}

		return Arrays.asList(Video).contains(extension.toLowerCase());
	}

	/**
	 * Checks if is video.
	 *
	 * @param pathfile
	 *            the pathfile
	 * @return true, if is video
	 */
	private static boolean issubtitle(String pathfile) {
		String extension = "";
		String[] Video = new String[] { "srt", "sub" };

		int i = pathfile.lastIndexOf('.');
		int p = Math.max(pathfile.lastIndexOf('/'), pathfile.lastIndexOf('\\'));

		if (i > p) {
			extension = pathfile.substring(i + 1);
		}

		return Arrays.asList(Video).contains(extension.toLowerCase());
	}

	/**
	 * Fichierdanslabaseepisodes.
	 *
	 * @param pathfile
	 *            the pathfile
	 * @return true, if successful
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static boolean fichierdanslabaseepisodes(String pathfile) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement();
		rs = stmt.executeQuery("SELECT * " + " FROM episodes " + " WHERE " + "      chemin_complet = \"" + pathfile + "\"" + "  ");
		rs.last();
		if (rs.getRow() == 0) {
			return false;
		}
		rs.close();
		return true;
	}

	/**
	 * transmisson hash dans la base & not timestamp terminé si timestamp ajout
	 * plus de 24h : demander classification effacer -> fichier qr html
	 * (clasification du hash) serie: telechargement en cours si le fichier
	 * telecharger est un episode (conversionnom2episodes) mettre l'indicateur
	 * encours a oui sinon annuler le fichier serie ou film: telechargement
	 * terminé deplacer fichiers dans le reperoire temporaire telechargement
	 * terminé & fichier absent: supprimer le telechargement de transmission &
	 * timestamp terminé effacer: supprimer le telechargement de transmission &
	 * timestamp terminé ignorer: ne rien faire & timestamp terminé autres:
	 * demander classification -> fichier qr html.
	 *
	 * @param args
	 *            the args
	 * @throws JSONException
	 *             the JSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SQLException
	 *             the SQL exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws JSchException
	 *             the j sch exception
	 */
	private static void transmisson(String[] args) throws JSONException, IOException, SQLException, InterruptedException, JSchException {
		System.out.println("transmisson");
		ResultSet rs = null;
		long dtjour = Param.dateDuJour().getTime();
		List<TorrentStatus> torrents = Param.client.getAllTorrents(new TorrentField[] { TorrentField.hashString, TorrentField.files, TorrentField.name,
				TorrentField.percentDone, TorrentField.activityDate, TorrentField.eta });
		for (TorrentStatus curr : torrents) {
			String hash = (String) curr.getField(TorrentField.hashString);
			Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = stmt.executeQuery("SELECT * " + " FROM hash " + " WHERE " + "      hash = \"" + hash + "\"" + "  ");
			while (rs.next()) {
				JSONArray listFile = ((JSONArray) curr.getField(TorrentField.files));

				if (rs.getDate("timestamp_termine") != null) {
					transmission.supprimer_hash(hash);
				} else {
					int nbfichierbruler;
					switch (rs.getString("classification")) {
					case "serie":
						nbfichierbruler = 0;
						int i = 0;
						for (i = 0; i < listFile.length(); i++) {
							JSONObject n = (JSONObject) listFile.get(i);
							if (!isvideo(n.getString("name")) && !issubtitle(n.getString("name"))) {
								nbfichierbruler++;
								transmission.cancelFilenameOfTorrent(hash, i);
							} else {
								Map<String, String> ret = conversionnom2episodes(n.getString("name"));
								if (ret.get("serie").equals("") || ret.get("saison").equals("000") || ret.get("episode").equals("000")
										|| episodesachemincomplet(ret)
										|| (!n.get("bytesCompleted").equals(0) && n.get("bytesCompleted").equals(n.get("length")))) {
									nbfichierbruler++;
									transmission.cancelFilenameOfTorrent(hash, i);
								} else {
									transmission.uncancelFilenameOfTorrent(hash, i);
									mettreepisodeaencours(ret.get("serie"), ret.get("saison"), ret.get("episode"), ret.get("sequentiel"));
									// if (!n.get("bytesCompleted").equals(0) &&
									// n.get("bytesCompleted").equals(n.get("length")))
									// {
									// nbfichierbruler++;
									// } else {

									// }
									// if
									// (transmission.deplacer_fichier(hash,
									// Param.CheminTemporaireSerie(),
									// i))
									// { }
									// }
									// else
									// {
									// Param.logger.debug("Encours Ep:" +
									// ret.get("serie") + " " +
									// ret.get("saison") + "-" +
									// ret.get("episode") + " name:" +
									// n.getString("name"));
									// }

								}
							}
						}
						if (curr.getField(TorrentField.percentDone).equals(1)) {
							if (listFile.length() == nbfichierbruler) {
								rs.updateString("classification", "effacer");
								rs.updateRow();
							} else {
								transmission.resume_hash(hash);
							}
						}
						if (rs.getDate("timestamp_ajout").before(Param.dateJourM1)) {
							long derniereactiviteilyaminutes = ((dtjour / 1000) - (Integer) curr.getField(TorrentField.activityDate));
							if (derniereactiviteilyaminutes > (Integer.parseInt(Param.props.getProperty("gestdown.minutesdinactivitesautorize")) * 60)) {
								rs.updateString("classification", "effacer");
								rs.updateRow();
							}
						}

						break;
					case "film":
					case "filemaeffacer":
						if (curr.getField(TorrentField.percentDone).equals(1)) {
							if (transmission.all_fichier_absent(hash)) {
								rs.updateString("classification", "effacer");
								rs.updateRow();
							} else {
								transmission.deplacer_fichier(hash, Param.CheminTemporaireFilm());
							}
						}
						if (rs.getDate("timestamp_ajout").before(Param.dateJourM1)) {
							long derniereactiviteilyaminutes = ((dtjour / 1000) - (Integer) curr.getField(TorrentField.activityDate));
							if (derniereactiviteilyaminutes > (Integer.parseInt(Param.props.getProperty("gestdown.minutesdinactivitesautorize")) * 60)) {
								rs.updateString("classification", "filemaeffacer");
								rs.updateString("nom", (String) curr.getField(TorrentField.name));
								rs.updateRow();
							}
						}
						break;
					case "effacer":
						transmission.supprimer_hash(hash);
						rs.updateString("timestamp_termine", (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(Param.dateDuJour()));
						rs.updateRow();
						break;
					case "ignorer":
						break;
					case "autres":
						rs.updateString("nom", (String) curr.getField(TorrentField.name));
						if (Double.parseDouble(curr.getField(TorrentField.percentDone).toString()) > 0.995) {
							if (transmission.all_fichier_absent(hash)) {
								rs.updateString("classification", "effacer");
								rs.updateRow();
							}
						}
						rs.updateRow();
						break;
					}
				}
			}
			rs.close();
		}
	}

	/**
	 * Episodesachemincomplet.
	 *
	 * @param ret
	 *            the ret
	 * @return true, if successful
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static boolean episodesachemincomplet(Map<String, String> ret) throws SQLException {
		if (ret == null) {
			return false;
		}
		boolean retour = true;
		if (ret.get("episode")!=null && ret.get("episode") != "000") {
			Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs;
			rs = stmt.executeQuery("SELECT * " + " FROM episodes " + " WHERE " + "      serie = \"" + ret.get("serie") + "\"" + "  and    num_saison = \""
					+ ret.get("saison") + "\"" + "  and    num_episodes = \"" + ret.get("episode") + "\"" + "  and 	  not(isnull(chemin_complet))" + "");
			if (!rs.first()) {
				retour = false;
			}
			rs.close();
		}
		if (ret.get("episodebis")!=null && ret.get("episodebis") != "000") {
			Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs;
			rs = stmt.executeQuery("SELECT * " + " FROM episodes " + " WHERE " + "      serie = \"" + ret.get("serie") + "\"" + "  and    num_saison = \""
					+ ret.get("saison") + "\"" + "  and    num_episodes = \"" + ret.get("episodebis") + "\"" + "  and 	  not(isnull(chemin_complet))" + "");
			if (!rs.first()) {
				retour = false;
			} 
			rs.close();

		}
		return retour;
	}

	/**
	 * Chemincompletdelaserie.
	 *
	 * @param serie
	 *            the serie
	 * @return the array list
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static ArrayList<String> chemincompletdelaserie(String serie) throws SQLException {
		ArrayList<String> ret = new ArrayList<String>();
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs;
		rs = stmt.executeQuery("SELECT * " + " FROM episodes " + " WHERE " + "      serie = \"" + serie + "\"" + "  and 	  chemin_complet IS NOT NULL" + "");
		while (rs.next()) {
			ret.add(rs.getString("chemin_complet"));
			;
		}
		rs.close();
		return ret;
	}

	/**
	 * Initialisation.
	 *
	 * @param args
	 *            the args
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParseException
	 *             the parse exception
	 * @throws SQLException
	 *             the SQL exception
	 * @throws JSchException
	 *             the j sch exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private static void initialisation(String[] args) throws IOException, ParseException, SQLException, JSchException, InterruptedException {
		Param.ChargerParametrage();
		System.out.println("args=." + Arrays.toString(args) + ".");
		System.out.print("<pre>");
		System.out.println("+---+----+----+----+");
		System.out.println("+       Debut      +" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		System.out.println(Param.GetOs());
	}

	/**
	 * Mise a jour de modele physique de données comparaison des format des
	 * tables et mise ajour du format
	 *
	 * @param args
	 *            the args
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static void mise_a_jour_mpd(String[] args) throws SQLException {
		// TODO: add auto alter
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS series " + "(nom VARCHAR(255) not NULL , " + " repertoire VARCHAR(255) , " + " date_maj_web DATE , "
				+ " Stat_Image_Base64 TEXT , " + " Stat_Tableau TEXT , " + " PRIMARY KEY ( nom ) " + ") " + " ");

		// Param.stmt.executeUpdate("DROP TABLE episodes ");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS episodes " + "(serie VARCHAR(255) not NULL , " + " num_saison INTEGER  , " + " num_episodes INTEGER  , "
				+ " sequentiel INTEGER  , " + " nom  VARCHAR(255) , " + " airdate DATE , " + " encours BOOLEAN , " + " timestamp_completer DATE , "
				+ " chemin_complet  VARCHAR(255) CHARACTER SET utf8 DEFAULT NULL , " + " freezesearchuntil DATE , "
				+ " PRIMARY KEY ( serie , num_saison , num_episodes  ) , " + "         INDEX   ( airdate ) ," + "         INDEX   ( serie , sequentiel ) "
				+ ") " + " ");

		// Param.stmt.executeUpdate("DROP TABLE hash ");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS hash " + "(hash VARCHAR(255) not NULL , " + " nom VARCHAR(255) , " + " classification  VARCHAR(255) , "
				+ " magnet  VARCHAR(255) , " + " timestamp_ajout TIMESTAMP  , " + " timestamp_termine TIMESTAMP  , " + " PRIMARY KEY ( hash ) ,"
				+ "         INDEX   ( timestamp_termine ) " + ") " + " ");

		// Param.stmt.executeUpdate("DROP TABLE hash ");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS question " 
						   + "("
						   + " question VARCHAR(255) not NULL , " 
						   + " champsquestion TEXT not null, " 
						   + " champsreponse TEXT , "
						   + " PRIMARY KEY     ( question ) "
						   + ") " 
						   + " ");
		
	}

	/**
	 * metre ajour la piste des epispdes de chaque series le cas echeant remise
	 * a zerp de lindicateur "encours" synchronisation des hash de la base de
	 * données avec ceux prensent dans transmission
	 *
	 * @param args
	 *            the args
	 * @throws SQLException
	 *             the SQL exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws NumberFormatException
	 *             the number format exception
	 * @throws ParseException
	 *             the parse exception
	 * @throws JSchException
	 *             the j sch exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private static void alimentation_bdd(String[] args) throws SQLException, IOException, NumberFormatException, ParseException, JSchException,
			InterruptedException {
		System.out.println("alimentation_bdd");

		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

		mise_a_jour_liste_episodes(stmt);

	}

	private static void synchronisation_bdd_transmission() throws SQLException, IOException, JSONException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs;

		for (String hash : transmission.listhashTransmission()) {
			rs = stmt.executeQuery("SELECT * " + " FROM hash " + " WHERE " + "      hash = \"" + hash + "\"" + "  ");
			rs.last();
			if (rs.getRow() == 0) {
				String sql = "INSERT INTO hash " + " (hash, classification,timestamp_ajout) VALUES  " + " ( " + " \"" + hash + "\" , " + " 'autres' , " + " \""
						+ (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(Param.dateDuJour()) + "\"  " + " ) ";
				stmt.executeUpdate(sql);

			}
			rs.close();
		}

		ArrayList<String> lstHash = transmission.listhashTransmission();
		rs = stmt.executeQuery("SELECT * " + " FROM hash " + " WHERE " + "      timestamp_termine is null" + "  ");
		while (rs.next()) {
			if (!lstHash.contains(rs.getString("hash").toLowerCase())) {
				rs.updateString("timestamp_termine", (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(Param.dateDuJour()));
				if (rs.getString("nom") == null) {
					rs.updateString("nom", "null");
				} else {
					rs.updateString("nom", Normalizer.normalize(rs.getString("nom"), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
				}
				rs.updateRow();
			}
		}
		rs.close();
	}

	private static void mise_a_jour_liste_episodes(Statement stmt) throws NumberFormatException, SQLException, InterruptedException, IOException,
			JSchException, ParseException {
		ResultSet rs;

		rs = stmt.executeQuery("SELECT series.nom , max(episodes.airdate) as MaxDate , series.date_maj_web" + " FROM series " + " LEFT OUTER join episodes "
				+ "    on series.nom = episodes.serie "
				// + " WHERE "
				// + "      series.date_maj_web < \"" + (new
				// SimpleDateFormat("yyyy-MM-dd")).format(Param.dateJourM30)
				// + "\""
				// + "   OR series.date_maj_web IS NULL"
				+ " GROUP BY " + "  series.nom");
		while (rs.next()) {
			Object rsMaxDate = rs.getObject("MaxDate");
			Object rsdatemajweb = rs.getObject("date_maj_web");
			Param.logger.debug(rs.getString("nom") + " MaxDate = " + rs.getString("MaxDate") + Param.getDateDiff(Param.dateDuJour(), (Date) rsMaxDate)
					+ Param.getDateDiff((Date) rsMaxDate, Param.dateJourP300) + " date_maj_web = " + rs.getString("date_maj_web")
					+ Param.getDateDiff(Param.dateJourM30, rs.getDate("date_maj_web")));
			if (rsMaxDate == null || ((Date) rsMaxDate).before(Param.dateDuJour()) || ((Date) rsMaxDate).after(Param.dateJourP300)) {
				// FileBot.maj_liste_episodes(rs.getString("nom"));
				// } else {
				// if ((rs.getDate("MaxDate")).after(Param.dateJourM300)) {
				// FileBot.maj_liste_episodes(rs.getString("nom"));
				// } else {
				if (rsdatemajweb == null || ((Date)rsdatemajweb).before(Param.dateJourM30)) {
					FileBot.maj_liste_episodes(rs.getString("nom"));
				}
				// }
			}
		}
		rs.close();
	}

	/**
	 * Cloture.
	 *
	 * @param args
	 *            the args
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SQLException
	 *             the SQL exception
	 */
	private static void cloture(String[] args) throws InterruptedException, IOException, SQLException {
		System.out.println("+        Fin       +" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		System.out.println("+---+----+----+----+");
		System.out.print("</pre>");

		Param.cloture();
	}

	/**
	 * Conversionnom2episodes.
	 *
	 * @param fileName
	 *            the file name
	 * @return the map
	 * @throws SQLException
	 *             the SQL exception
	 */
	public static Map<String, String> conversionnom2episodes(String fileName) throws SQLException {
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
		// Pattern p3 = Pattern.compile("[._ (-]([0-9]+)([0-9][0-9])");
		Pattern p4 = Pattern.compile("[np._ (-]([0-9]+)[._ (-]+([0-9]+)*");

		Matcher m1 = p1.matcher(namecmp.toLowerCase());
		Matcher m2 = p2.matcher(namecmp.toLowerCase());
		// Matcher m3 = p3.matcher(namecmp.toLowerCase());
		Matcher m4 = p4.matcher(namecmp.toLowerCase());
		Matcher m5 = p5.matcher(namecmp.toLowerCase());
		Matcher m6 = p6.matcher(namecmp.toLowerCase());

		HashMap<String, String> numeroEpisodeTrouve = new HashMap<String, String>();
		HashMap<String, String> numeroSequentielTrouve = new HashMap<String, String>();
		HashMap<String, String> numeroSaisonTrouve = new HashMap<String, String>();
		HashMap<String, String> nometextension = new HashMap<String, String>();
		numeroEpisodeTrouve.clear();
		numeroSequentielTrouve.clear();
		numeroSaisonTrouve.clear();
		if (m4.find()) {
			numeroEpisodeTrouve.clear();
			numeroSequentielTrouve.clear();
			numeroSaisonTrouve.clear();
			partname = namecmp.substring(0, m4.start(0));
			numeroSequentielTrouve.put("sequentiel", String.format("%03d", Integer.parseInt(m4.group(1))));
			if (m4.groupCount() > 1) {
				String seq2 = m4.group(2);
				if (seq2 != null) {
					if (!seq2.equals("")) {
						numeroSequentielTrouve.put("sequentielbis", String.format("%03d", Integer.parseInt(m4.group(2))));
					}
				}
			}
			if (!nometextension.containsKey("partiedroite")) {
				nometextension.put("partiedroite", namecmp.substring(m4.end()));
			}
			// Param.logger.debug("episode-" + "decomposerNom 4-" + partname +
			// " " + numeroSaisonTrouve.toString() + " " +
			// numeroEpisodeTrouve.toString() + " "
			// + numeroSequentielTrouve.toString());
		}

		// if (m3.find())
		// {
		// partname = namecmp.substring(0, m3.start(0));
		// numeroSaisonTrouve.put("saison", String.format("%03d",
		// Integer.parseInt(m3.group(1).toString())));
		// numeroEpisodeTrouve.put("episode", String.format("%03d",
		// Integer.parseInt(m3.group(2).toString())));
		// //Param.logger.debug("episode-" + "decomposerNom 3-" + partname + " "
		// + numeroSaisonTrouve.toString() + " " +
		// numeroEpisodeTrouve.toString() + " "
		// // + numeroSequentielTrouve.toString());
		// }

		if (m2.find()) {
			numeroEpisodeTrouve.clear();
			numeroSaisonTrouve.clear();
			numeroSequentielTrouve.clear();
			partname = namecmp.substring(0, m2.start(0));
			numeroSaisonTrouve.put("saison", String.format("%03d", Integer.parseInt(m2.group(1).toString())));
			numeroEpisodeTrouve.put("episode", String.format("%03d", Integer.parseInt(m2.group(2).toString())));
			if (!nometextension.containsKey("partiedroite")) {
				nometextension.put("partiedroite", namecmp.substring(m2.end()));
			}
			// Param.logger.debug("episode-" + "decomposerNom 2-" + partname +
			// " " + numeroSaisonTrouve.toString() + " " +
			// numeroEpisodeTrouve.toString() + " "
			// + numeroSequentielTrouve.toString());
		}

		if (m5.find()) {
			numeroEpisodeTrouve.clear();
			numeroSaisonTrouve.clear();
			numeroSequentielTrouve.clear();
			partname = namecmp.substring(0, m5.start(0));
			numeroSaisonTrouve.put("saison", String.format("%03d", Integer.parseInt(m5.group(2).toString())));
			numeroEpisodeTrouve.put("episode", String.format("%03d", Integer.parseInt(m5.group(4).toString())));
			if (!nometextension.containsKey("partiedroite")) {
				nometextension.put("partiedroite", namecmp.substring(m5.end()));
			}
			// Param.logger.debug("episode-" + "decomposerNom 5-" + partname +
			// " " + numeroSaisonTrouve.toString() + " " +
			// numeroEpisodeTrouve.toString() + " "
			// + numeroSequentielTrouve.toString());
		} else {
			if (m6.find()) {
				numeroEpisodeTrouve.clear();
				numeroSaisonTrouve.clear();
				// numeroSequentielTrouve.clear();
				partname = namecmp.substring(0, m6.start(0));
				numeroSaisonTrouve.put("saison", String.format("%03d", Integer.parseInt(m6.group(2).toString())));
				numeroEpisodeTrouve.put("episode", String.format("%03d", Integer.parseInt(m6.group(4).toString())));
				if (!nometextension.containsKey("partiedroite")) {
					nometextension.put("partiedroite", namecmp.substring(m6.end()));
				}
				// Param.logger.debug("episode-" + "decomposerNom 6-" + partname
				// + " " + numeroSaisonTrouve.toString() + " " +
				// numeroEpisodeTrouve.toString() + " "
				// + numeroSequentielTrouve.toString());
			}
			if (m1.find()) {
				if (m1.group(5).toString().compareTo("") != 0) {
					if (Param.isNumeric(m1.group(4).toString()) && Param.isNumeric(m1.group(5).toString())) {
						if (Integer.parseInt(m1.group(5).toString()) == (Integer.parseInt(m1.group(4).toString()) + 1)) {
							numeroEpisodeTrouve.clear();
							numeroSaisonTrouve.clear();
							numeroSequentielTrouve.clear();
							partname = namecmp.substring(0, m1.start(0));
							numeroSaisonTrouve.put("saison", String.format("%03d", Integer.parseInt(m1.group(2).toString())));
							numeroEpisodeTrouve.put("episode", String.format("%03d", Integer.parseInt(m1.group(4).toString())));
							numeroEpisodeTrouve.put("episodebis", String.format("%03d", Integer.parseInt(m1.group(5).toString())));
							if (!nometextension.containsKey("partiedroite")) {
								nometextension.put("partiedroite", namecmp.substring(m1.end()));
							}
							// Param.logger.debug("episode-" +
							// "decomposerNom 1-" + partname + " " +
							// numeroSaisonTrouve.toString() + " "
							// + numeroEpisodeTrouve.toString() + " " +
							// numeroSequentielTrouve.toString());
						}
					}
				}
			}
		}

		Integer nbtrouve = 0;
		// numeroSaisonTrouve.remove("saison", "000");
		// numeroEpisodeTrouve.remove("episode", "000");
		// numeroSequentielTrouve.remove("sequentiel", "000");
		if ((numeroSaisonTrouve.size() > 0 && numeroEpisodeTrouve.size() > 0) || numeroSequentielTrouve.size() > 0) {

			Boolean ctrlnom;
			ResultSet rs = null;
			Statement stmt = Param.con.createStatement();
			rs = stmt.executeQuery("SELECT * " + " FROM series " + "  ");
			while (rs.next()) {
				ctrlnom = true;
				String textSerieNettoyer = rs.getString("nom").replaceAll("[(]([0-9a-zA-Z]*)[)]", "");
				String[] textSerie = textSerieNettoyer.split("[-,'._() ]+");
				String partnameDouble = partname.replaceAll("(.)(?=\\1)", "");
				for (String mot : textSerie) {
					if (mot.length() > 1) {
						String motDouble = mot.replaceAll("(.)(?=\\1)", "");
						if ((" " + partname).indexOf(mot.toLowerCase()) < 1 && (" " + partnameDouble).indexOf(motDouble.toLowerCase()) < 1) {
							ctrlnom = false;
						}
					}
				}
				if (ctrlnom) {
					nbtrouve++;
					ret.put("serie", rs.getString("nom"));
					String where;
					if ((numeroSaisonTrouve.size() == 0 && numeroEpisodeTrouve.size() == 0)) {
						where = " sequentiel = " + numeroSequentielTrouve.get("sequentiel");
						if (numeroSequentielTrouve.get("sequentielbis") != null) {
							where += " OR sequentiel = " + numeroSequentielTrouve.get("sequentielbis");
						}
						ResultSet rs2 = null;
						Statement stmt2 = Param.con.createStatement();
						rs2 = stmt2.executeQuery("SELECT * " + " FROM episodes " + " where " + where);
						while (rs2.next()) {
							numeroSaisonTrouve.put("saison", String.format("%03d", Integer.parseInt(rs2.getString("num_saison"))));
							if (numeroEpisodeTrouve.size() == 0) {
								numeroEpisodeTrouve.put("episode", String.format("%03d", Integer.parseInt(rs2.getString("num_episodes"))));
							} else {
								numeroEpisodeTrouve.put("episodebis", String.format("%03d", Integer.parseInt(rs2.getString("num_episodes"))));
							}
						}
					}
					// Param.logger.debug("episode- decomposerNom" +
					// rs.getString("nom"));
				}

			}
			rs.close();

			ret.putAll(numeroSaisonTrouve);
			ret.putAll(numeroEpisodeTrouve);
			ret.putAll(numeroSequentielTrouve);
			ret.putAll(nometextension);
		}

		if (nbtrouve == 0) {
			ret.put("serie", "");
		}
		if (!ret.containsKey("saison")) {
			ret.put("saison", "000");
		}
		if (!ret.containsKey("episode")) {
			ret.put("episode", "000");
		}
		if (!ret.containsKey("sequentiel")) {
			ret.put("sequentiel", "000");
		}
		return ret;

	}

	public static void addquestion(String question, ArrayList<String> champsquestion) throws SQLException {
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("insert into question " + " ( question , champsquestion , champsreponse ) " 
				+ "VALUE "
				+ " (\"" + question + "\" ,"
				+ " \"" + champsquestion.toString() + "\" ,"
				+ " \"" + "vide" + "\"" + " ) "
				+ " ON DUPLICATE KEY UPDATE champsquestion  = champsquestion  ;");
	}

}
