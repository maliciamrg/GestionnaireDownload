import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;


public class Torrent {


	private static int perfectSize3 = 330;
	private static int perfectSize6 = 660;
	private static int goodForLeech = 2000;
	private static int seuilPts = 4000;
	public static int perfectSize;
	
	public Torrent() {
		// TODO Auto-generated constructor stub
	}
	static ArrayList<String> getMagnetFor(String serie, Integer saison, ArrayList<Integer> episode,Integer nbEpisodeSaison) throws IOException, NumberFormatException, SQLException {
		ArrayList<String> retTorrents = new ArrayList<String>(0);

		String strMagnet3 = "";
		String strMagnet6 = "";
		String[] strMagnetep3 = new String[nbEpisodeSaison];
		String[] strMagnetep6 = new String[nbEpisodeSaison];
		
		String html = getDataTorrents(serie, saison, (episode.size() > 1 ? -1 : episode.get(0)));
		if (html == null) {
			return retTorrents;
		}
		if (html.indexOf("did not match any documents") > 0) {
			return retTorrents;
		}

		Source source = new Source(html);
		Element eleDataTorrents = source.getFirstElementByClass("data");
		if (eleDataTorrents != null) {
			List<Element> eleRowTorrents = eleDataTorrents
					.getAllElements(HTMLElementName.TR);
			int i = 0;
			for (Element row : eleRowTorrents) {
				i++;
				if (row.getFirstElementByClass("firstr") == null) {

					// System.out.println(row.toString());

					String strNom = row.getFirstElementByClass("cellMainLink")
							.getTextExtractor().toString();
					String strMagnet = row
							.getFirstElementByClass("imagnet icon16")
							.getAttributeValue("href").toString();
					int debSubStr = strMagnet.indexOf("btih:") + 5;
					int finSubStr = strMagnet.indexOf("&");
					String strHash = strMagnet.substring(debSubStr, finSubStr);
					if (!hashdanslabasehash(strHash)) {

						String strTaille = row
								.getFirstElementByClass("nobr center")
								.getTextExtractor().toString();
						Integer seed = Integer.valueOf(retOrNull(row
								.getFirstElementByClass("green center")
								.getTextExtractor().toString()));
						

						Integer score3 = 0;
						Integer score6 = 0;
						if (strNom.toLowerCase().contains("season")){
							perfectSize=perfectSize3;
							score3 = calculScore(
									unhumanize(retOrNull(strTaille)), seed,
									(episode.size() > 1 ? nbEpisodeSaison : 1));
							if (score3 > 0) {
								if (strMagnet3 == "") {
									strMagnet3 = strMagnet;
								}
							}
							
							perfectSize=perfectSize6;
							score6 = calculScore(
									unhumanize(retOrNull(strTaille)), seed,
									(episode.size() > 1 ? nbEpisodeSaison : 1));
							if (score6 > 0) {
								if (strMagnet6 == "") {
									strMagnet6 = strMagnet;
								}
							}
						} else{
							Map<String, String> ret = Main.conversionnom2episodes(strNom);
							if (ret.get("serie") != null
									&& !ret.get("serie").equals("")
									&& episode.contains(Integer.parseInt(ret.get("episode")))) {
								perfectSize=perfectSize3;
								score3 = calculScore(unhumanize(retOrNull(strTaille)), seed,1);								
								if (score3 > 0) {
									strMagnetep3[Integer.parseInt(ret.get("episode"))-1] = strMagnet;
								}
								perfectSize=perfectSize6;
								score6 = calculScore(unhumanize(retOrNull(strTaille)), seed,1);								
								if (score6 > 0) {
									strMagnetep6[Integer.parseInt(ret.get("episode"))-1] = strMagnet;
								}
							}
						}
						
						if (score3 > 0 || score6 > 0) {
							Param.logger
									.debug("----- taille:"
											+ strTaille
											+ " tailleH:"
											+ unhumanize(retOrNull(strTaille))
											+ " seed:"
											+ seed
											+ " nbep:"
											+ (episode.size() > 1 ? nbEpisodeSaison : 1) 
											+ " score:" 
											+ ((score3 > 0)?score3:score6)
											+ " nom:" 
											+ strNom);
						}

						
						/*
						 * else {
						 * 
						 * Param.logger.debug("----- Ko ----- taille:" +
						 * strTaille + " tailleH:" +
						 * unhumanize(retOrNull(strTaille)) + " seed:" + seed +
						 * " nbep:" + nbEpisodeSaison + " score:" + score +
						 * " nom:" + strNom);
						 * 
						 * }
						 */

					}
					// System.out.println(torrent.toString());
				}

			}
		}

		//Param.logger.debug("torrent- retTorrents" + retTorrents);
		if(strMagnet3!=""){
			retTorrents.add(strMagnet3);
		}else{
			if(strMagnet6!=""){
				retTorrents.add(strMagnet6);
			}else{
				for (int i = 0;i<nbEpisodeSaison-1;i++){
					if(strMagnetep3[i]!=null){
						retTorrents.add(strMagnetep3[i]);
					}else{			
						if(strMagnetep6[i]!=null){
							retTorrents.add(strMagnetep6[i]);
						}
					}					
				}
			}			
		}
		
		
		return retTorrents;
	}
	
	
	private static boolean hashdanslabasehash(String hash) throws SQLException {
		ResultSet rs = null;
		Statement stmt = Param.con.createStatement();
		rs = stmt.executeQuery("SELECT * "
									 + " FROM hash "
									 + " WHERE "
									 + "      hash = \"" + hash  + "\""
									 + "  ");
		rs.last();
		if (rs.getRow() == 0)
		{				
			return false;
		}
		rs.close();
		return true ;
	}
	
	private static Double unhumanize(String text)
	{

		String[] power = { "k", "m", "g", "t" };
		Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s?(k|m|g|t)?b?");
		Matcher m = p.matcher(text.toLowerCase());

		if (m.find())
		{
			Double partieEntier = Double.valueOf(m.group(1).toString());
			Integer facteur = Arrays.asList(power).indexOf(m.group(2)) + 1;
			return (Double) (partieEntier * Math.pow(1024, facteur));
		}

		return Double.parseDouble("0");

	}
	public static String retOrNull(String str)
	{
		if (str == null)
		{
			return "";
		}
		return str;
	}
	/*
	 * calcul le score du torrents afiein de prioriser les torrents
	 */
	public static Integer calculScore(Double taille, Integer seed, Integer nbEpisodeRechercher)
	{
		seed = seed;
		taille = taille;
		nbEpisodeRechercher = nbEpisodeRechercher;
		
		if (nbEpisodeRechercher == 0)
		{nbEpisodeRechercher = 1;}
	//	System.out.println((getPerfectSize(nbEpisodeRechercher)));
		double ecartTaille = (-1 * Math.abs((getPerfectSize(nbEpisodeRechercher) * nbEpisodeRechercher) - (taille / 1024 / 1024))) / (double) (getPerfectSize(nbEpisodeRechercher) * nbEpisodeRechercher);
		int ecartSeedBrut = -(seed - goodForLeech);
		double ecartSeed = -((ecartSeedBrut + Math.abs(ecartSeedBrut)) / 2) / (double) goodForLeech;
		int importanceSeed = (30 / nbEpisodeRechercher);
		double pts = ((ecartTaille * (100 - importanceSeed)) + (ecartSeed * importanceSeed)) * 100;
		return Integer.valueOf((int) Math.round(pts)) + seuilPts;
	//	System.out.println("score="+this.score);
	//	System.out.println(scoreToString());
	}
	public static int getPerfectSize(Integer nbEpisodeRechercher)
	{
		if (nbEpisodeRechercher > 1)
		{
			return (int) (perfectSize / 1.33);
		}
		else
		{
			return perfectSize;
		}
	}	
	public static String getDataTorrents(String nomSerie, Integer numSaison,Integer numEpisode) throws IOException
	{
		String[] listUrlPossible = new String[1];
		String nomSerieNettoyer = nomSerie.replaceAll("[^a-zA-Z0-9. ]", " ");

		String urlRecherche;
		if (numEpisode == -1)
		{
			urlRecherche = nomSerieNettoyer.toLowerCase().trim() + " Season " + numSaison;
		}
		else
		{
			urlRecherche = nomSerieNettoyer.toLowerCase().trim() + " S" + String.format("%1$02d", numSaison) + "E" + String.format("%1$02d", numEpisode);
		}

		listUrlPossible[0] = Param.Urlkickassusearch + urlRecherche	+ "/?field=seeders&sorder=desc";

		Param.logger.debug("torrent-" + listUrlPossible[0]);
		int i = 0;
		for (i = 0; i < listUrlPossible.length; i++)
		{
			String[] ret;
			ret = valide_url(listUrlPossible[i]);
			if (ret[0] == "1")
			{
				return ret[1];
			}
		}

		return null;
	}
	
	
	public static String[] valide_url(String url) throws IOException {

		String[] ret = new String[3];
		ret[0] = "0";
		ret[1] = "";
		ret[2] = "";
		if (url == "") {
			return ret;
		}
		// Display.affichageLigne(url);;

		URL weburl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) weburl.openConnection();

		// 4xx: client error, 5xx: server error. See:
		// http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html.
		boolean isError;
		try {
			isError = connection.getResponseCode() >= 400;
		} catch (IOException e) {
			e.printStackTrace();
			Param.logger.error(Param.eToString(e));
			return ret;
		}
		//Param.logger.debug("connection.getResponseCode()=" + connection.getResponseCode());
		// The normal input stream doesn't work in error-cases.
		InputStream is = isError ? connection.getErrorStream() : connection.getInputStream();

		BufferedReader br;
		if (connection.getHeaderField("Content-Encoding") != null && connection.getHeaderField("Content-Encoding").equals("gzip")) {
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(is/*
																			   * weburl
																			   * .
																			   * openStream
																			   * (
																			   * )
																			   */)));
		} else {
			br = new BufferedReader(new InputStreamReader(is/*
														   * weburl.openStream(
														   * )
														   */));
		}
		String data = "";

		String dataln = br.readLine();
		while (dataln != null) {
			data = data + dataln + "\r\n";
			dataln = br.readLine();
		}
		br.close();

		ret[2] = url;
		ret[1] = data;
		// ret[1] = (String) con.getContent();
		String badr = "Bad Request";
		String notfound = "The page cannot be found";
		String notfound404 = "Nothing found!";
		// echo
		// $url."-".(strpos("#".$html,$notfound)>0)."-".(strpos("#".$html,$badr)>0
		// ). "</br>\n";

		ret[0] = ((("#" + ret[1]).indexOf(notfound404) > 0)  || (("#" + ret[1]).indexOf(notfound) > 0) || (("#" + ret[1]).indexOf(badr) > 0) || ret[1] == "") ? "0" : "1";

		return ret;
	}
	
}
