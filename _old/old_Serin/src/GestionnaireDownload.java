import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;
import org.json.*;

import ca.benow.transmission.TransmissionClient;
import ca.benow.transmission.model.TorrentStatus;
import ca.benow.transmission.model.TorrentStatus.TorrentField;
import ca.benow.transmission.*;

import java.lang.annotation.*;

import javax.net.ssl.SSLEngineResult.Status;

import ca.benow.transmission.model.*;

import javax.net.ssl.*;

import com.jcraft.jsch.*;
import javax.xml.namespace.*;

public class GestionnaireDownload
{
	private final static Logger logger = Logger.getLogger(GestionnaireDownload.class);

	private TransmissionClient client;

	private Fichier fichierListTorentsAuto;
	private ArrayList<Torrents> listTorrentAuto = new ArrayList<Torrents>(0);

	private int nbPlaceRestante = -1;	

	private static int vingtquatreheure = 86400;
	private static int douzeheure = 43200;
	private static int sixheure = 21600;
	private static int troisheure = 10800;
	private static int deuxheure = 7200;
	private static int uneheure = 3600;

	/**
	 * ouverture du gestionnaire de telechargement
	 */
	public GestionnaireDownload() throws FileNotFoundException, ProtocolException, MalformedURLException, IOException, JSchException,
	InterruptedException
	{
		logger.debug("GestionnaireDownload");

		if (Main.P.topConnectTransmission)
		{
			connectTransmission();	
		}

		majFichierSuiviDownload();

		Main.P.logger.info("(0) nbPlaceRestante" + "gestionTelechargementEnCours " + getNbPlaceRestante());

	}

	private void connectTransmission() throws IOException, JSchException, InterruptedException
	{
		/**
		 * connect avec transmission
		 */

		int i = 0;
		for (i = 1; i < 4; i++)
		{
			boolean isError = true;

			URL url = new URL("http://" + Main.P.gestdownusername + ":" + Main.P.gestdownpassword + "@" + Main.P.gestdownhttp + "");

			/*
			 * url = new URL("http://" + this.username + ":" + this.password
			 * + "@" + this.http + ""); HttpURLConnection huc =
			 * (HttpURLConnection) url.openConnection();
			 * huc.setRequestMethod("GET"); huc.connect(); int code =
			 * huc.getResponseCode(); client = new TransmissionClient(url);
			 */

			HttpURLConnection huc = (HttpURLConnection) url.openConnection();

			if (huc.getContentLength() > 0)
			{
				// 4xx: client error, 5xx: server error. See:
				// http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html.
				// huc.setRequestMethod("GET");
				// huc.connect();

				int code = huc.getResponseCode();
				isError = code >= 400;
				if (code == 409)
				{
					isError = false;
				}

				logger.debug("huc.getResponseCode()=" + huc.getResponseCode());
				// The normal input stream doesn't work in error-cases.

				if (!isError)
				{

					client = new TransmissionClient(url);
				}
			}
			if (client == null || isError)
			{
				logger.debug("transmission- connexion non effecuer");
				Ssh.executeAction("/ffp/start/transmission.sh start");

			}
			else
			{
				i = 99;
			}

		}
	}

	public void majListTorrent() throws IOException
	{
		maintenanceTorrentsByDate();
		purgeListTorrentAuto();
	}

	public void majListTorrent(BibliothequeSerie bibSerie) throws IOException
	{
		majListTorrentAutoAvecOrphelin(bibSerie);
		majListTorrentAutoParBibliotheque(bibSerie);
	}

	public int getNbPlaceRestante() throws JSONException, IOException
	{
		if (nbPlaceRestante == -1)
		{
			nbPlaceRestante = calculNbPlaceRestante();
		}
		return nbPlaceRestante;
	}

	private void majFichierSuiviDownload() throws IOException, JSONException
	{
		logger.debug("majFichierSuiviDownload");
		if (client == null)
		{
			return;
		}
		List<TorrentStatus> torrents = this.client.getAllTorrents(new TorrentField[] { TorrentField.files, TorrentField.fileStats, TorrentField.id,
																	  TorrentField.name, TorrentField.hashString, TorrentField.downloadDir, TorrentField.status, TorrentField.startDate, TorrentField.addedDate,
																	  TorrentField.activityDate, TorrentField.percentDone });
		for (TorrentStatus curr : torrents)
		{
			String name = (String) curr.getField(TorrentField.name);
			Double percentDone = Double.parseDouble(String.valueOf(curr.getField(TorrentField.percentDone)));
			String strATracer=String.valueOf(percentDone * 100);
			tracerActionTransmission(name, strATracer);
		}

	}

	private void tracerActionTransmission(String idATracer, String ActionATracer) throws IOException
	{
		String dtj = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Main.P.dateDuJour);
		String str=interfacedonnees.tbTraceTransmisionAdd(dtj, idATracer, ActionATracer);
		logger.debug("tbTraceTransmisionAdd-" + idATracer + " => " + str);
	}


	private int calculNbPlaceRestante() throws JSONException, IOException
	{
		logger.debug("getNbPlaceRestante");
		int nbEncours = 0;

		List<TorrentStatus> torrents = this.client.getAllTorrents(new TorrentField[] { TorrentField.files, TorrentField.fileStats, TorrentField.id,
																	  TorrentField.name, TorrentField.hashString, TorrentField.downloadDir, TorrentField.status, TorrentField.startDate, TorrentField.addedDate,
																	  TorrentField.activityDate, TorrentField.percentDone });
		logger.debug("getNbPlaceRestante- : torrents=" + torrents.size());
		for (TorrentStatus curr : torrents)
		{

			String hash = (String) curr.getField(TorrentField.hashString);
			if (interfacedonnees.isHashGererAutomatique(hash))
			{
				Integer status = (Integer) curr.getField(TorrentField.status);
				Double percentDone = Double.valueOf(String.valueOf(curr.getField(TorrentField.percentDone)));

				if (status != 0)
				{
					if (percentDone != 1)
					{
//						logger.debug("getNbPlaceRestante-" + t.nom + " => (" + t.nbEpisodeRechercher + ") " + t.getListEpisodesRechercher().toString());
						nbEncours++;
					}
				}
			}
		}
		logger.debug("nbEncours=" + nbEncours);

		return Main.P.gestdownnbPlaceMax - nbEncours;
	}


	/**
	 * torrent avec activitydate > 2h ==> mise en pause torrent avec startdate >
	 * 4h * n episodes ==> mise en pause torrent avec adddate > 24h * n episodes
	 * ==> purge
	 */
	public void maintenanceTorrentsByDate() throws IOException
	{
		logger.debug("maintenanceTorrentsByDate");
		if (client == null)
		{
			return;
		}
		int intDateDuJour = (int) (new Date().getTime() / 1000);

		List<TorrentStatus> torrents = this.client.getAllTorrents(new TorrentField[] { TorrentField.files, TorrentField.fileStats, TorrentField.id,
																	  TorrentField.name, TorrentField.hashString, TorrentField.downloadDir, TorrentField.status, TorrentField.startDate, TorrentField.addedDate,
																	  TorrentField.activityDate, TorrentField.percentDone });
		logger.debug("maintenanceTorrentsByDate- : torrents=" + torrents.size());

		int n = 0;

		for (TorrentStatus curr : torrents)
		{

			n++;

			logger.debug("---===" + (String) curr.getField(TorrentField.name) + "===---");
			boolean hashlistTorrentAuto = false;
			String hash = (String) curr.getField(TorrentField.hashString);
			String name = (String) curr.getField(TorrentField.name);
			Integer torentId = (Integer) curr.getField(TorrentField.id);
			Integer torrentstartDate = (Integer) curr.getField(TorrentField.startDate);
			Integer torrentaddDate = (Integer) curr.getField(TorrentField.addedDate);
			Integer torrentactivityDate = (Integer) curr.getField(TorrentField.activityDate);
			Integer status = (Integer) curr.getField(TorrentField.status);
			Double percentDone = Double.valueOf(String.valueOf(curr.getField(TorrentField.percentDone)));

			/**
			 * determination du cas
			 */
			int cas = 3;

			Torrents t = interfacedonnees.gettorrent(hash);
			if (t.hash.compareToIgnoreCase(hash) == 0)
			{
				hashlistTorrentAuto = true;
				/* mise a jour du statut dans la base local */
				// 0= pause , 4= downloading
				t.status = status;
				ArrayList<String> arrayFileName = new ArrayList<String>(0);
				JSONArray files = (JSONArray) curr.getField(TorrentField.files);
				for (int index = 0; index < files.length(); index++)
				{
					arrayFileName.add((String) files.getJSONObject(index).get("name"));
				}
				t.arrayFileName = arrayFileName;
				int nbEpisodes = (t.nbEpisodeRechercher < 1 ? 1 : t.nbEpisodeRechercher);
				logger.debug("pause torrentactivityDate dans "
							 + ((torrentactivityDate > 0) ? "" + (deuxheure - (intDateDuJour - torrentactivityDate)) / 60 + " min" : " zero "));
				logger.debug("purge torrentstartDate dans "
							 + ((torrentstartDate > 0 && torrentactivityDate == 0) ? "" + (deuxheure - (intDateDuJour - torrentstartDate)) / 60 + " min"
							 : " zero "));
				logger.debug("baselocal nbEpisodeRechercher=" + t.nbEpisodeRechercher);
				logger.debug("score=" + t.scoreToString());
				//		System.out.println("2:" + t.scoreToString());
				if (percentDone < 1.0 && torrentactivityDate > 0 && deuxheure - (intDateDuJour - torrentactivityDate) < 0)
				{
					cas = 2;
				}
				if (percentDone < 1.0 && torrentstartDate > 0 && torrentactivityDate == 0 && (deuxheure - (intDateDuJour - torrentstartDate)) < 0)
				{
					cas = 1;
				}
				if (t.nbEpisodeRechercher.compareTo(0) == 0)
				{
					cas = 1;
				}
				if (t.score < 0)
				{
					cas = 1;
				}
			}
			
			if (Torrents.TorrentStalled(curr))
			{
				cas = 1;
			}

			/**
			 * application du cas
			 */
			String actionEffectuer = "";
			switch (cas)
			{
				case (1):
					this.purgeHash(torentId, hash);
					tracerActionTransmission(name, "purgeHash");
					// i--;
					actionEffectuer = "purgeHash";
					break;
				case (2):
					if (status != 0)
					{
						this.pauseHash(torentId, hash);
					}
					actionEffectuer = "pauseHash";
					tracerActionTransmission(name, "pauseHash");
					break;
				default:
					actionEffectuer = " en cours";
			}

			/*
			 * if (t.nbEpisodeRechercher.compareTo(0) == 0 ||
			 * Torrents.TorrentStalled(curr)) { // if (torrentaddDate > 0 &&
			 * (vingtquatreheure * // nbEpisodes) - (intDateDuJour -
			 * torrentaddDate) < 0 || // status == 6 // || (status == 0 &&
			 * percentDone == 1)) // { this.purgeHash(torentId, hash); i--; // }
			 * } if ((torrentstartDate > 0 && (deuxheure * nbEpisodes) -
			 * (intDateDuJour - torrentstartDate) < 0) || (torrentactivityDate >
			 * 0 && deuxheure - (intDateDuJour - torrentactivityDate) < 0)) {
			 * this.pauseHash(String.valueOf(torentId), hash); }
			 */

			if (hashlistTorrentAuto)
			{
				logger.info(actionEffectuer + " (" + String.format("%1$03d", n) + ") auto torrent " + (String) curr.getField(TorrentField.name) + " ");
			}
			else
			{
				logger.info("         " + " (" + String.format("%1$03d", n) + ") Non auto torrent ---===" + (String) curr.getField(TorrentField.name)
							+ "===---");
			}
		}
		fichierListTorentsAuto.serialiseXML(listTorrentAuto);
	}

	/**
	 * ajout du magnet
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Boolean addMagnet(Torrents eleTorrents, String chemin) throws FileNotFoundException, IOException
	{
		if (client == null)
		{
			return false;
		}

		listTorrentAuto.add(eleTorrents);
		fichierListTorentsAuto.serialiseXML(listTorrentAuto);

		AddTorrentParameters paramTorrent = new AddTorrentParameters(eleTorrents.magnet);
		paramTorrent.setLocation(Fichier.formatPath(chemin, false));
		/**
		 * addTorrent tombe en execption si torrent duplicate
		 */
		AddedTorrentInfo ret;
		try
		{
			ret = this.client.addTorrent(paramTorrent);
			for (Episode _ep : eleTorrents.getListEpisodesRechercher())
			{
				logger.debug("torrent- AddListEpisodesRechercher " + _ep.toString());
			}
			logger.debug("torrent- AddTorrent " + chemin);
			logger.debug("torrent- AddTorrent " + ret.getId() + " - " + ret.getName());
		}
		catch (JSONException | IOException e)
		{
			e.printStackTrace();
			logger.error(Param.eToString(e));
		}

		return true;
		// return false;
	}

	public Boolean purgeHash(int torrentId, String hash) throws IOException
	{
		logger.debug("purgeHash");
		if (client == null)
		{
			return false;
		}
		torrentId = torrentIdOfHash(hash);
		if (Main.P.topSupressionTorrentUtiliser)
		{
			client.removeTorrents(new Object[] { torrentId }, false);
			for (Torrents t : listTorrentAuto)
			{
				if (t.hash.compareToIgnoreCase(hash) == 0)
				{
					logger.debug("torrent- removeTorrents " + torrentId + " - " + t.nom);
					for (Episode _ep : t.getListEpisodesRechercher())
					{
						logger.debug("torrent- removeListEpisodesRechercher " + _ep.toString());
					}
					listTorrentAuto.remove(t);
					break;
				}
			}
			fichierListTorentsAuto.serialiseXML(listTorrentAuto);
		}

		return true;
		// return false;
	}

	public Boolean pauseHash(int torrentId, String hash) throws JSONException, IOException
	{
		logger.debug("pauseHash");
		if (client == null)
		{
			return false;
		}

		torrentId = torrentIdOfHash(hash);
		client.stopTorrents(new Object[] { torrentId });
		for (Torrents t : listTorrentAuto)
		{
			if (t.hash.compareToIgnoreCase(hash) == 0)
			{
				logger.info("torrent- pause Torrents " + torrentId + " - " + t.nom);
				for (Episode _ep : t.getListEpisodesRechercher())
				{
					logger.debug("torrent- pause ListEpisodesRechercher " + _ep.toString());
				}
				break;
			}
		}
		fichierListTorentsAuto.serialiseXML(listTorrentAuto);

		return true;
		// return false;
	}

	private int torrentIdOfHash(String hash) throws JSONException, IOException
	{
		List<TorrentStatus> torrents = this.client.getAllTorrents(new TorrentField[] { TorrentField.id, TorrentField.hashString });
		for (TorrentStatus curr : torrents)
		{
			String hashTorrent = ((String) curr.getField(TorrentField.hashString));
			if (hash.compareToIgnoreCase(hashTorrent) == 0)
			{
				return ((Integer) curr.getField(TorrentField.id));
			}
		}
		return 0;
	}

	/**
	 * cancelle un des fichier du torrent
	 */
	private void cancelFilenameOfTorrent(String hashTransmisionTorrents, int indiceOfFile) throws JSONException, IOException
	{
		if (client == null)
		{
			return;
		}
		logger.debug("cancelFilenameOfTorrent (" + indiceOfFile + ") " + hashTransmisionTorrents);
		SetTorrentParameters torrentParam = new SetTorrentParameters(this.torrentIdOfHash(hashTransmisionTorrents));
		ArrayList<Integer> listFilesUnwanted = new ArrayList<Integer>(0);
		listFilesUnwanted.add(indiceOfFile);
		torrentParam.filesUnwanted = listFilesUnwanted;
		if (Main.P.topSupressionTorrentUtiliser)
		{
			//		client.setTorrents(torrentParam);
		}

	}

	/**
	 * purge le fichier des torrents
	 */
	public void purgeListTorrentAuto() throws IOException
	{
		logger.debug("purgeListTorrentAuto");
		if (client == null)
		{
			return;
		}

		List<TorrentStatus> torrents = this.client.getAllTorrents(new TorrentField[] { TorrentField.files, TorrentField.fileStats, TorrentField.id,
																	  TorrentField.name, TorrentField.hashString, TorrentField.downloadDir, TorrentField.status, TorrentField.startDate, TorrentField.addedDate,
																	  TorrentField.activityDate });

		for (int i = 0; i < listTorrentAuto.size(); i++)
		{
			// for (Torrents t : listTorrentAuto) {

			Torrents t = listTorrentAuto.get(i);
			boolean aSupprimer = true;

			for (TorrentStatus curr : torrents)
			{
				String hash = (String) curr.getField(TorrentField.hashString);

				if (hash.compareToIgnoreCase(t.hash) == 0)
				{
					aSupprimer = false;
				}

			}

			if (aSupprimer)
			{
				listTorrentAuto.remove(t);
				i--;
			}

		}

		fichierListTorentsAuto.serialiseXML(listTorrentAuto);

	}

	/**
	 * scan les telechargement pour ajoute les telechargement orphelin
	 */
	public void majListTorrentAutoAvecOrphelin(BibliothequeSerie bibSerie) throws IOException
	{
		if (bibSerie == null)
		{
			return;
		}
		List<TorrentStatus> torrents = this.client.getAllTorrents(new TorrentField[] { TorrentField.files, TorrentField.fileStats, TorrentField.id,
																	  TorrentField.name, TorrentField.hashString, TorrentField.magnetLink, TorrentField.status, TorrentField.startDate, TorrentField.addedDate,
																	  TorrentField.activityDate , TorrentField.sizeWhenDone });

		for (TorrentStatus curr : torrents)
		{
			String magnet = (String) curr.getField(TorrentField.magnetLink);
			String hash = (String) curr.getField(TorrentField.hashString);
			String fileName = (String) curr.getField(TorrentField.name);
			String size = String.valueOf(curr.getField(TorrentField.sizeWhenDone));

			boolean hashlistTorrentAuto = false;
			for (int i = 0; i < listTorrentAuto.size(); i++)
			{
				// for (Torrents t : listTorrentAuto) {

				Torrents t = listTorrentAuto.get(i);

				if (hash.compareToIgnoreCase(t.hash) == 0)
				{
					t.nom = (String) curr.getField(TorrentField.name);
					hashlistTorrentAuto = true;
				}
			}

			if (!hashlistTorrentAuto)
			{
				logger.debug("---===" + (String) curr.getField(TorrentField.name) + "===---");
				// Serie[] seriesName = bibSerie.getListSeries();
				ArrayList<String> arrayFileName = new ArrayList<String>(0);
				// arrayFileName.add(fileName);
				JSONArray files = (JSONArray) curr.getField(TorrentField.files);
				for (int index = 0; index < files.length(); index++)
				{
					arrayFileName.add((String) files.getJSONObject(index).get("name"));
				}

				ArrayList<Episode> _arrayep = new ArrayList<Episode>(0);
				if (arrayFileName.size() > 0)
				{
					logger.debug("--- recalcul nbEpisodeRechercher by filename (" + arrayFileName.size() + "):" + fileName);
					for (String _st : arrayFileName)
					{
						if (EnumTypeDeFichier.video.EstDuTypeDeFichier(Fichier.getFileExtension(_st)))
						{
							_arrayep.addAll(bibSerie.getEpisodeFromName(_st));
						}
					}
				}
				else
				{
					logger.debug("--- recalcul nbEpisodeRechercher by torrentname :" + fileName);
					_arrayep.addAll(bibSerie.getEpisodeFromName(fileName));
				}
				// Map<String, String> ret =
				// Episode.decomposerNomFichier(fileName, seriesName);
				// if (ret.get("serie") != null) {
				// Episode _ep = bibSerie.getEpisode(ret.get("serie"),
				// ret.get("saison"), ret.get("episode"),
				// ret.get("sequentiel"));
				// if (_ep != null) {
				// ArrayList<Episode> _arrayep =
				// bibSerie.getEpisodeFromName(fileName);
				// for (Episode _ep : _arrayep) {
				if (_arrayep.size() > 0)
				{
					Torrents eleTorrents = new Torrents(fileName, magnet, hash, Torrents.goodForLeech, Double.parseDouble(size), _arrayep.size());
					eleTorrents.setListEpisodesRechercher(_arrayep);
					eleTorrents.arrayFileName = arrayFileName;
					listTorrentAuto.add(eleTorrents);
				}
				// logger.debug(_ep.toString());
				// }
				// }
			}
		}

		fichierListTorentsAuto.serialiseXML(listTorrentAuto);

	}

	/**
	 * purge le fichier des torrents
	 */
	public void majListTorrentAutoParBibliotheque(BibliothequeSerie bibSerie) throws IOException
	{
		if (bibSerie == null)
		{
			return;
		}

		for (int i = 0; i < listTorrentAuto.size(); i++)
		{
			// for (Torrents t : listTorrentAuto) {
			Torrents t = listTorrentAuto.get(i);

			// * balyage des fichier et exclusion des fichier/torrent
			// n'apartenant pas a la bibliotheque

			if (t.arrayFileName.size() > 0)
			{
				logger.debug("--- recalcul nbEpisodeRechercher by filename (" + t.arrayFileName.size() + "):" + t.nom);
				int nFichier = 0;
				int nEpisode=0;
				for (String _st : t.arrayFileName)
				{
					int nFichierPart = 0;
					if (EnumTypeDeFichier.video.EstDuTypeDeFichier(Fichier.getFileExtension(_st)))
					{
						ArrayList<Episode> _arrayep = new ArrayList<Episode>(0);
						_arrayep.addAll(bibSerie.getEpisodeFromName(_st));
						for (Episode _ep : _arrayep)
						{
							nEpisode++;
							if (!_ep.isStatus(EnumStatusEpisode.present))
							{
								nFichierPart++;
							}
							logger.debug("(" + _ep.getStatusString() + ")" + _ep.toString());
						}
						if (nFichierPart == 0)
						{
							cancelFilenameOfTorrent(t.hash, t.arrayFileName.indexOf(_st));
						}
					}
					else
					{
						cancelFilenameOfTorrent(t.hash, t.arrayFileName.indexOf(_st));
					}
					nFichier = nFichier + nFichierPart;
				}
				//	System.out.println("calcul score");
				t.calculScore(t.getTaille(), t.getSeed(), nEpisode);
				t.nbEpisodeRechercher = nFichier;

			}
			else
			{
				// * comptage des episodes encore a rechercher
				int n = t.getListEpisodesRechercher().size();
				logger.debug("--- recalcul nbEpisodeRechercher (" + n + "):" + t.nom);
				for (Episode _ep : t.getListEpisodesRechercher())
				{
					// logger.debug("en local=" + _ep.toString());
					Episode episodeBib = null;
					episodeBib = bibSerie.getEpisode(_ep.getTitreSerie(), _ep.getNumeroSaison(), _ep.getNumeroEpisode(), _ep.getNumeroSeq());
					if (episodeBib != null)
					{
						_ep = episodeBib;
						if (episodeBib.isStatus(EnumStatusEpisode.present))
						{
							n--;
						}
						else
						{
							setLocationTo(t.hash, bibSerie.getRepertoire(_ep.getTitreSerie()));

						}
						logger.debug("(" + episodeBib.getStatusString() + ")" + episodeBib.toString());
					}
				}
				t.nbEpisodeRechercher = n;
			}

			logger.debug("recalcul nbEpisodeRechercher :" + t.nbEpisodeRechercher);

		}

		fichierListTorentsAuto.serialiseXML(listTorrentAuto);

	}

	private void setLocationTo(String hash, String repertoireSerie) throws JSONException, IOException
	{

		List<TorrentStatus> torrents = this.client.getAllTorrents(new TorrentField[] { TorrentField.files, TorrentField.fileStats, TorrentField.id,
																	  TorrentField.name, TorrentField.hashString, TorrentField.downloadDir, TorrentField.status, TorrentField.startDate, TorrentField.addedDate,
																	  TorrentField.activityDate });
		for (TorrentStatus curr : torrents)
		{
			String hashtorrent = (String) curr.getField(TorrentField.hashString);

			if (hash.compareToIgnoreCase(hashtorrent) == 0)
			{
				logger.debug((String) curr.getField(TorrentField.name) + " ==> " + Repertoire.formatPath(repertoireSerie));

				SetTorrentParameters paramTorrent = new SetTorrentParameters(new Object[] { curr.getField(TorrentField.id) });
				paramTorrent.setLocation(Fichier.formatPath(repertoireSerie, false));
				/**
				 * addTorrent tombe en execption si torrent duplicate
				 */
				//		this.client.setTorrents(paramTorrent);

			}
		}

	}

	/**
	 * liste les fichier du hash (chemin complet)
	 * 
	 * @return jsonArray files+fileStats+torrentId du torrent
	 */
	public void close()
	{
		client = null;
	}
}
