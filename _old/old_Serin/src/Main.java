import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import org.json.*;
import org.apache.log4j.*;

import com.jcraft.jsch.JSchException;

import org.apache.log4j.xml.*;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.*;
import org.apache.log4j.varia.*;

import java.text.*;

import org.apache.xmlrpc.*;

import com.jcraft.jsch.SftpException;

public class Main
{
	/*
	 * o;o;o;o;o initialisation
	 */
	public static BibliothequeSerie bibSerie;
	public static GestionnaireDownload gestDownload;

	public static Param P;

	private static int NumeroParagraphe = 0;

	public static void main(String[] args)
	{
		System.out.println("main(String[] args)");

		try
		{					
			methodMaitre(args);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			P.logger.error(Param.eToString(e));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			P.logger.error(Param.eToString(e));
		}
		catch (JSchException e)
		{
			e.printStackTrace();
			P.logger.error(Param.eToString(e));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			P.logger.error(Param.eToString(e));
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			P.logger.error(Param.eToString(e));
		}
		catch (XmlRpcException e)
		{
			e.printStackTrace();
			P.logger.error(Param.eToString(e));
		}
		catch (SftpException e)
		{
			e.printStackTrace();
			P.logger.error(Param.eToString(e));
		}
	}

	private static void methodMaitre(String[] args) throws InterruptedException, ParseException, JSchException, IOException, SftpException, JSONException, XmlRpcException, SQLException
	{
		System.out.println("methodMaitre(String[] args)");

		initialisation(args);
	}private static void stoprun(String[] args) throws JSchException, InterruptedException, IOException, ParseException, SftpException, XmlRpcException{
		gestDownload = new GestionnaireDownload();

		if (gestDownload.getNbPlaceRestante() > 0 || P.getHoraireFilebot() || P.topForceExecutionMalgresZeroplaces)
		{
			bibSerie = new BibliothequeSerie();
			bibSerie.incorporerEncours(interfacedonnees.getepisodesavechashencours());
			gestDownload.majListTorrent(bibSerie);
			ajoutTelechargement(args);

		}

		gestDownload.majListTorrent();
		fin();
	}

	public static void initialisation(String[] args) throws IOException, JSchException, InterruptedException, SQLException, ParseException
	{
		System.out.println("(" + String.format("%1$02d", NumeroParagraphe++) + ") " + "initialisation(String[] args)");
		Param.initialiserParam();
		P = new Param();

		// capture stdout et stderr to log4j
		Param.tieSystemOutAndErrToLog();

		System.out.print("<pre>");
		P.logger.info("+---+----+----+----+" + P.idUnique);
		P.logger.info("+       Debut      +" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));

		P.logger.info("workRepertoire" + P.workRepertoire);

		Ssh.actionexecChmodR777(Repertoire.formatPath(P.workRepertoire, false));
	}


	public static void ajoutTelechargement(String[] args) throws JSONException, IOException
	{
		System.out.println("(" + String.format("%1$02d", NumeroParagraphe++) + ") " + "ajoutTelechargement(String[]))");
		if (bibSerie == null || gestDownload == null)
		{
			return;
		}

		P.logger.info("(1) nbPlaceRestante" + "ajoutTelechargement " + gestDownload.getNbPlaceRestante());

		bibSerie.constituerListEpisodes();
		P.logger.info("listEpisodesPresent" + "  " + bibSerie.getListEpisodesPresent().size());
		P.logger.info("listEpisodesAVenir" + "   " + bibSerie.getListEpisodesAVenir().size());
		P.logger.info("listEpisodesManquant" + " " + bibSerie.getListEpisodesManquant().size());
		P.logger.info("listEpisodesEnCours" + "  " + bibSerie.getListEpisodesEnCours().size());
		P.logger.info("listEpisodesTotal" + "    " + bibSerie.listEpisodesTotal.size());

		ArrayList<Episode> list = bibSerie.getListEpisodesManquant();
		int nbATelecharger=gestDownload.getNbPlaceRestante();
		for (int ix = 0; ix < list.size() && nbATelecharger > 0; ix++)
		{
			ArrayList<Episode> listEpisodesTorent = new ArrayList<Episode>(0);
			Episode episodeManquant = list.get(ix);
			listEpisodesTorent.add(episodeManquant);
			list.remove(episodeManquant);
			ix = ix - 1;
			int nbEpisode = 1;
			/**
			 * episode par episode ou saison
			 */
			if ((episodeManquant.getNumeroSaison().compareTo(bibSerie.getNumberOfTheLastFinishSeason(episodeManquant.getTitreSerie())) <= 0))
			{
				int indiceLu = 0;
				int nbSupprimerAvantIndice = 0;
				for (Episode _ep : list)
				{
					if (episodeManquant.getTitreSerie().compareTo(_ep.getTitreSerie()) == 0
						&& episodeManquant.getNumeroSaison().compareTo(_ep.getNumeroSaison()) == 0)
					{
						listEpisodesTorent.add(_ep);
						if (indiceLu <= ix)
						{
							nbSupprimerAvantIndice++;
						}
					}
					indiceLu++;
				}
				list.removeAll(listEpisodesTorent);
				ix = ix - nbSupprimerAvantIndice;
				nbEpisode = bibSerie.getNbEpisodesMax(episodeManquant.getTitreSerie(), episodeManquant.getNumeroSaison());
			}

			// Display.ecrireFichierTraceEpisode("actionAfaire",
			// "episode manquant", listEpisodesTorent);

			if (P.topRechercherTorrents)
			{

				ArrayList<Torrents> listTorrents = Torrents.getListTorrentsEpisode(episodeManquant, nbEpisode);

				Collections.sort(listTorrents);

				int n = 0;
				boolean torrentTransmis = false;
				for (n = 0; !torrentTransmis && n < listTorrents.size(); n++)
				{
					P.logger.debug("torrent" + "score : " + listTorrents.get(n).score + "-" + listTorrents.get(n).nom);
					if (listTorrents.get(n).score > 0 
						&& !bibSerie.findHash(listEpisodesTorent, listTorrents.get(n).hash))
					{

						listTorrents.get(n).setListEpisodesRechercher(listEpisodesTorent);
						/**
						 * ajout du torrent a transmision
						 */

						P.logger.debug("torrent" + "gestDownload.addMagnet : " + listTorrents.get(n).nom);
						P.logger.info("torrent" + "gestDownload.addMagnet : " + listTorrents.get(n).nom);
						if (P.topRechercherTorrentsAjoutDesTorrentsATransmission)
						{
							// Display.ecrireFichierTrace("actionAfaire",
							// "torrent "
							// + listTorrents.get(n).nom);
							if (gestDownload.addMagnet(listTorrents.get(n), bibSerie.getRepertoireTmpArangerSpecifiqueSerie(episodeManquant.getTitreSerie())))
							{

								nbATelecharger--;
								P.logger.info("--== (2) nbPlaceRestante" + "torrentajouter , reste " + gestDownload.getNbPlaceRestante() + " places ==--");

								bibSerie.incorporerEncours(listEpisodesTorent);
								bibSerie.addHash(listEpisodesTorent, listTorrents.get(n).hash);

								torrentTransmis = true;
							}
						}
					}
					if (!torrentTransmis)
					{
						bibSerie.purgeHash(listEpisodesTorent);
					}
				}
			}
		}
	}

	public static void fin() throws ParseException, IOException, InterruptedException 
	{
		System.out.println("(" + String.format("%1$02d", NumeroParagraphe++) + ") " + "Cloture(String[] args)");
		P.logger.info("Cloture");

		if (bibSerie != null)
		{
			bibSerie.genererFichirStats();
		}
		gestDownload.close();

		System.out.println("(" + String.format("%1$02d", NumeroParagraphe++) + ") " + "fin()");
		P.logger.info("fin");

		P.logger.info("+       Fin        +" + "" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		P.logger.info("+---+----+----+----+" + "" + P.idUnique);

		Param.clotureTrace();

	}

}
