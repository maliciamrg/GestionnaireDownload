import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;


public class Torrent {


	private static int perfectSize = 330;
	private static int goodForLeech = 2000;
	private static int seuilPts = 4000;

	public Torrent() {
		// TODO Auto-generated constructor stub
	}
	static ArrayList<String> getMagnetFor(String serie, Integer saison, Integer episode,Integer nbEpisodeRechercher) throws IOException {
		ArrayList<String> retTorrents = new ArrayList<String>(0);

		String html = getDataTorrents(serie, saison, (nbEpisodeRechercher > 1 ? -1 : episode));
		if (html == null)
		{
			return retTorrents;
		}
		if (html.indexOf("did not match any documents") > 0)
		{
			return retTorrents;
		}

		Source source = new Source(html);
		Element eleDataTorrents = source.getFirstElementByClass("data");
		if (eleDataTorrents != null)
		{
			List<Element> eleRowTorrents = eleDataTorrents.getAllElements(HTMLElementName.TR);
			int i = 0;
			for (Element row : eleRowTorrents)
			{
				i++;
				if (row.getFirstElementByClass("firstr") == null)
				{

					// System.out.println(row.toString());

					String strNom = row.getFirstElementByClass("cellMainLink").getTextExtractor().toString();
					if ((episode==-1 && strNom.toLowerCase().contains("season")) || episode > 0 ){
						String strMagnet = row.getFirstElementByClass("imagnet icon16").getAttributeValue("href").toString();
						String strTaille = row.getFirstElementByClass("nobr center").getTextExtractor().toString();
						Integer seed = Integer.valueOf(retOrNull(row.getFirstElementByClass("green center").getTextExtractor().toString()));
						Integer score = calculScore(unhumanize(retOrNull(strTaille)),seed, nbEpisodeRechercher);
						if (score>0) {
							Param.logger.debug("----- taille:" + strTaille + " tailleH:"+unhumanize(retOrNull(strTaille)) + " seed:" + seed + " nbep:" + nbEpisodeRechercher + " score:" + score+ " nom:" + strNom);
							retTorrents.add(strMagnet);
						}else{
							Param.logger.debug("----- Ko ----- taille:" + strTaille + " tailleH:"+unhumanize(retOrNull(strTaille)) + " seed:" + seed + " nbep:" + nbEpisodeRechercher + " score:" + score+ " nom:" + strNom);
						}
					}
					// System.out.println(torrent.toString());
				}

			}
		}

		//Param.logger.debug("torrent- retTorrents" + retTorrents);

		return retTorrents;
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
