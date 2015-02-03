import ca.benow.transmission.model.*;
import ca.benow.transmission.model.TorrentStatus.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import net.htmlparser.jericho.*;
import org.apache.log4j.*;
import org.json.*;

import org.apache.log4j.Logger;

public class Torrents implements Comparable<Torrents>
{
	private final static Logger logger = Logger.getLogger(Torrents.class);
	private static int perfectSize = 330;



	static int goodForLeech = 2000;
	private static int seuilPts = 4000;

	private double ecartTaille = 0;
	private double ecartSeedBrut = 0;
	private double ecartSeed = 0;
	private int importanceSeed = 0;
	private double pts = 0;

	String nom;
	String magnet;
	String hash;
	private Integer seed;
	Integer score;
	private Double taille; // en Mo
	Integer nbEpisodeRechercher;
	private ArrayList<Episode> listEpisodesRechercher = new ArrayList<Episode>(0);

	Integer status;
	ArrayList<String> arrayFileName = new ArrayList<String>(0);	


	public Double getTaille()
	{
		return taille;
	}


	public Integer getSeed()
	{
		return seed;
	}
	/**
	 * @return the perfectSize
	 */
	public static int getPerfectSize(Integer nbEpisodeRechercher)
	{
		if (nbEpisodeRechercher > 1)
		{
			return perfectSize / 2;
		}
		else
		{
			return perfectSize;
		}
	}
	
	public String scoreToString()
	{
		String ret = "(s:" + seed + "/t:" + humanize( taille) + ")\tectaille:" + ecartTaille + "\tnbepeisode" + nbEpisodeRechercher+ "(" + humanize(taille /  nbEpisodeRechercher) + "/ep)"
			+ "\tecseedb:" + ecartSeedBrut + "\tecseed:" + ecartSeed + "\timpseed:" + importanceSeed + "%\tpts:" + Math.round(pts) + "\t=" + score + " ";
		return ret;
	}

	public Torrents()
	{
	}

	public Torrents(String hash)
	{
		this.hash = hash;
	}

	public Torrents(String nom, String magnet, String hash, Integer seed, Double taille, Integer nbEpisodeRechercher)
	{
		this.nom = nom;
		this.magnet = magnet;
		this.hash = hash;
		this.seed = seed;
		this.taille = taille;
		this.nbEpisodeRechercher = nbEpisodeRechercher;
		this.calculScore(taille, seed, nbEpisodeRechercher);
	}

	public void setListEpisodesRechercher(ArrayList<Episode> listEpisodesRechercher)
	{
		this.listEpisodesRechercher = listEpisodesRechercher;
		this.nbEpisodeRechercher = listEpisodesRechercher.size();
	}

	public void addListEpisodesRechercher(Episode episodesRechercher)
	{
		this.listEpisodesRechercher.add(episodesRechercher);
	}

	public ArrayList<Episode> getListEpisodesRechercher()
	{
		return listEpisodesRechercher;
	}

	/**
	 * retour,e la liste des torents pour lepisode
	 */
	public static ArrayList<Torrents> getListTorrentsEpisode(Episode episodesRechercher, Integer nbEpisodeRechercher) throws IOException
	{
		logger.debug("torrent-" + episodesRechercher.toString() + " nb : " + nbEpisodeRechercher);
		ArrayList<Torrents> retTorrents = new ArrayList<Torrents>(0);

		String html = getDataTorrents(episodesRechercher.getTitreSerie(), episodesRechercher.getNumeroSaison(), episodesRechercher.getNumeroSeq(),
									  (nbEpisodeRechercher > 1 ? -1 : episodesRechercher.getNumeroEpisode()));
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
					String strMagnet = row.getFirstElementByClass("imagnet icon16").getAttributeValue("href").toString();
					int debSubStr = strMagnet.indexOf("btih:") + 5;
					int finSubStr = strMagnet.indexOf("&");
					String strHash = strMagnet.substring(debSubStr, finSubStr);
					String strTaille = row.getFirstElementByClass("nobr center").getTextExtractor().toString();
					retTorrents.add(new Torrents(retOrNull(strNom), retOrNull(strMagnet), retOrNull(strHash), Integer.valueOf(retOrNull(row
																																		.getFirstElementByClass("green center").getTextExtractor().toString())), unhumanize(retOrNull(strTaille)), nbEpisodeRechercher));

					// System.out.println(torrent.toString());
				}

			}
		}

		logger.debug("torrent- retTorrents" + retTorrents);
		logger.debug("torrent- getListTorrentsEpisode : " + retTorrents.size() + " == " + episodesRechercher.toString());

		return retTorrents;
	}

	public static String getDataTorrents(String nomSerie, Integer numSaison, Integer numSeq, Integer numEpisode)
	throws IOException
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
			if (numSeq != 0)
			{
				urlRecherche = nomSerieNettoyer.toLowerCase().trim() + " " + numSeq;
			}
			else
			{
				urlRecherche = nomSerieNettoyer.toLowerCase().trim() + " S" + String.format("%1$02d", numSaison) + "E" + String.format("%1$02d", numEpisode);
			}
		}

		// try
		// {
		listUrlPossible[0] = Main.P.Urlkickassusearch + /*
			 * URLEncoder.encode(
			 */urlRecherche/*
			 * ,
			 * "UTF-8"
			 * )
			 */
			+ "/?field=seeders&sorder=desc";
		// }
		// catch (UnsupportedEncodingException e)
		// {
		// Display.ecrireFichierTrace("erreur","",e.getStackTrace());
		// }
		logger.debug("torrent-" + listUrlPossible[0]);
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
			Main.P.logger.error(Param.eToString(e));
			return ret;
		}
		logger.debug("connection.getResponseCode()=" + connection.getResponseCode());
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
	
	private static String humanize(Double octet)
	{
		if (octet == 0 || octet.isInfinite() || octet==null)
		{return "zero";}
		String[] power = { "o" ,"Ko", "Mo", "Go", "To" };

		Double ret = octet;
		Double retprev = ret;
		int q = -1;
		while (ret.intValue() > 0 && !ret.isInfinite()){
//			System.out.println(ret);
			retprev = ret;
			ret = ret / 1024;
			q++;
		} 
		return (new DecimalFormat("#0.00")).format(retprev)+ " " + power[q];
	}

	/*
	 * calcul le score du torrents afiein de prioriser les torrents
	 */
	public void calculScore(Double taille, Integer seed, Integer nbEpisodeRechercher)
	{
		this.seed = seed;
		this.taille = taille;
		this.nbEpisodeRechercher = nbEpisodeRechercher;
		
		if (nbEpisodeRechercher == 0)
		{nbEpisodeRechercher = 1;}
	//	System.out.println((getPerfectSize(nbEpisodeRechercher)));
		this.ecartTaille = (-1 * Math.abs((getPerfectSize(nbEpisodeRechercher) * nbEpisodeRechercher) - (taille / 1024 / 1024))) / (double) (getPerfectSize(nbEpisodeRechercher) * nbEpisodeRechercher);
		this.ecartSeedBrut = -(seed - goodForLeech);
		this.ecartSeed = -((ecartSeedBrut + Math.abs(ecartSeedBrut)) / 2) / (double) goodForLeech;
		this.importanceSeed = (30 / nbEpisodeRechercher);
		this.pts = ((ecartTaille * (100 - importanceSeed)) + (ecartSeed * importanceSeed)) * 100;
		this.score = Integer.valueOf((int) Math.round(pts)) + seuilPts;
	//	System.out.println("score="+this.score);
	//	System.out.println(scoreToString());
	}

	/**
	 * fournit une methode de comparation entre 2 episode afin de les trier
	 */
	@Override
	public int compareTo(Torrents torrentCompare)
	{
		return torrentCompare.score.compareTo(score);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Torrents)
		{
			// System.out.println(this.hash + "==" + ((Torrents)obj).hash +
			// "|");
			return (obj instanceof Torrents) && this.hash.compareToIgnoreCase(((Torrents) obj).hash) == 0;
		}
		/*
		 * if (obj instanceof String) { System.out.println(this.hash + "==" +
		 * ((String)obj) + "|"); return (obj instanceof String) &&
		 * this.hash.compareTo(((String)obj)) == 0; }
		 */
		System.out.println("obj is=" + obj.getClass().toString() + "|");
		return false;
	}

	@Override
	public String toString()
	{
		return "" + this.nom + " " + this.taille + " " + this.seed + " " + this.hash + " " + this.magnet;
		// TODO : Implement this method
		// return super.toString();
	}

	public static String retOrNull(String str)
	{
		if (str == null)
		{
			return "";
		}
		return str;
	}

	public static boolean TorrentStalled(TorrentStatus curr)
	{
		if (String.valueOf(curr.getField(TorrentField.percentDone)).compareTo("1") == 0)
		{
			String dd = (String.valueOf(curr.getField(TorrentField.downloadDir)));
			if (new File(Repertoire.formatPath(dd)).exists())
			{

				JSONArray listFile = (JSONArray) curr.getField(TorrentField.files);
				// Display.affichageProgressionInit("nb series traiter");
				int i = 0;
				for (i = 0; i < listFile.length(); i++)
				{
					JSONObject n = (JSONObject) listFile.get(i);
					if (new File(Repertoire.formatPath(dd + File.separator + n.getString("name"))).exists())
					{
						return false;
					}
				}
				return true;
			}

		}
		return false;
	}

}
