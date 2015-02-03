import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.*;

import com.jcraft.jsch.*;

import java.text.*;

import org.apache.xmlrpc.*;

public class BibliothequeSerie
{
	private final static Logger logger = Logger.getLogger(BibliothequeSerie.class);
	private Map<String, Serie> listSeries = new TreeMap<String, Serie>();
	private ArrayList<Episode> listEpisodesManquant = new ArrayList<Episode>(0);
	private ArrayList<Episode> listEpisodesAVenir = new ArrayList<Episode>(0);
	private ArrayList<Episode> listEpisodesEnCours = new ArrayList<Episode>(0);
	private ArrayList<Episode> listEpisodesPresent = new ArrayList<Episode>(0);
	public Map<String, Episode> listEpisodesTotal = new HashMap<String, Episode>(0);

	private Map<String, Boolean> listeBibliotheque = new HashMap<String, Boolean>(0);
	private Map<String, ArrayList<String>> listeBibliothequeSerie = new HashMap<String, ArrayList<String>>(0);
	private Map<String, ArrayList<String>> listeBibliothequeSerieRanger = new HashMap<String, ArrayList<String>>(0);

	public boolean findHash(ArrayList<Episode> listEpisodesTorent, String hash) throws IOException
	{
		for (Episode _ep : listEpisodesTorent)
		{
			Serie _ser = listSeries.get(_ep.getTitreSerie());
			if (_ser != null)
			{
				if (_ser.isHash(_ep.getNumeroSaison(), _ep.getNumeroEpisode(), _ep.getNumeroEpisodeDouble(), _ep.getNumeroSeq(), hash))
				{
					return true;
				}
			}
		}
		return false;
	}

	public void purgeHash(ArrayList<Episode> listEpisodesTorent) throws IOException
	{
		Map<String, Serie> serieMaj = new HashMap<String, Serie>(0);
		for (Episode _ep : listEpisodesTorent)
		{
			Serie _ser = listSeries.get(_ep.getTitreSerie());
			if (_ser != null)
			{
				_ser.purgeHash(_ep.getNumeroSaison(), _ep.getNumeroEpisode(), _ep.getNumeroEpisodeDouble(), _ep.getNumeroSeq());
				serieMaj.put(_ep.getTitreSerie(), _ser);
			}
		}

		for (Serie _ser : serieMaj.values())
		{
			_ser.sauvegardeXmlSerie();
		}
	}

	public void addHash(ArrayList<Episode> listEpisodesTorent, String hash) throws IOException
	{
		Map<String, Serie> serieMaj = new HashMap<String, Serie>(0);
		for (Episode _ep : listEpisodesTorent)
		{
			Serie _ser = listSeries.get(_ep.getTitreSerie());
			if (_ser != null)
			{
				_ser.addHash(_ep.getNumeroSaison(), _ep.getNumeroEpisode(), _ep.getNumeroEpisodeDouble(), _ep.getNumeroSeq(), hash);
				serieMaj.put(_ep.getTitreSerie(), _ser);
			}
		}

		for (Serie _ser : serieMaj.values())
		{
			_ser.sauvegardeXmlSerie();
		}
	}

	public Integer getNumberOfTheLastFinishSeason(String titreSerie)
	{
		int numeroSaisonMax = 0;
		int numeroSaisonAvenir = 99;
		Serie _ser = listSeries.get(titreSerie);
		if (_ser != null)
		{
			for (Episode _ep : _ser.getListeEpisodes().values())
			{
				if (_ep.isStatus(EnumStatusEpisode.avenir))
				{
					if (numeroSaisonAvenir > _ep.getNumeroSaison())
					{
						numeroSaisonAvenir = _ep.getNumeroSaison();
					}
				}
				if (numeroSaisonMax < _ep.getNumeroSaison())
				{
					numeroSaisonMax = _ep.getNumeroSaison();
				}
			}
		}
		int numeroSaisonEncours = (numeroSaisonAvenir == 99) ? numeroSaisonMax : numeroSaisonAvenir - 1;
		logger.debug("getNumberOfTheLastFinishSeason " + titreSerie + " (" + (numeroSaisonEncours) + ")");
		return numeroSaisonEncours;
	}

	public String getRepertoire(String titreSerie)
	{
		Serie _ser = listSeries.get(titreSerie);
		if (_ser != null)
		{
			return _ser.repertoire;
		}
		return "";
	}

	public String getRepertoireBase(String titreSerie)
	{
		Serie _ser = listSeries.get(titreSerie);
		if (_ser != null)
		{
			return _ser.getRepertoireBase();
		}
		return "";
	}

	public Episode getEpisode(String titreSerie, Integer numeroSaison, Integer numeroEpisode, Integer numeroSeq)
	{
		Serie _ser = listSeries.get(titreSerie);
		if (_ser != null)
		{
			return _ser.recupererEpisode(String.valueOf(numeroSaison), String.valueOf(numeroEpisode), String.valueOf(numeroSeq));
		}
		return null;
	}

	public Episode getEpisode(String titreSerie, String numeroSaison, String numeroEpisode, String numeroSeq)
	{
		Serie _ser = listSeries.get(titreSerie);
		if (_ser != null)
		{
			return _ser.recupererEpisode(numeroSaison, numeroEpisode, numeroSeq);
		}
		return null;
	}

	public void MiseAJourWeb() throws JSchException, IOException, ParseException, InterruptedException, SftpException
	{

		/**
		 * traitement de chaque serie
		 */

		int nbSerieMajWeb = 0;
		for (Serie _ser : listSeries.values())
		{
			if (!_ser.isFeezer())
			{
				if (_ser.MiseAJourEpisodesWeb())
				{
					nbSerieMajWeb++;
				}
			}
		}
		logger.info("BibliothequeSerie- MiseAJourWeb=" + nbSerieMajWeb);
	}

	public void MiseAJourRepertoire() throws JSchException, IOException, InterruptedException, ParseException, XmlRpcException
	{

		/**
		 * traitement des repertoires
		 */
		for (String keylisteBibliotheque : listeBibliotheque.keySet())
		{
			logger.info("BibliothequeSerie- MiseAJourRepertoire" + (listeBibliotheque.get(keylisteBibliotheque) ? "true-" : "false-") + keylisteBibliotheque);
			ArrayList<String> listeWithPathFichierRepertoireGlobal = new ArrayList<String>(0);
			listeWithPathFichierRepertoireGlobal = Repertoire.searchAllFiles(keylisteBibliotheque, listeBibliotheque.get(keylisteBibliotheque), 99, false);
			logger.debug("BibliothequeSerie- MiseAJourRepertoire" + (listeBibliotheque.get(keylisteBibliotheque) ? "true-" : "false-") + keylisteBibliotheque
						 + "=" + listeWithPathFichierRepertoireGlobal.size());
			distribuerEtPurgerFichierDuRepertoire(listeWithPathFichierRepertoireGlobal);
			Repertoire.purgeEmptyRepertoire(keylisteBibliotheque, new String[] { Main.P.NomRepertoireTmpARanger, Main.P.NomRepertoireCorbeille });
		}

		/**
		 * display
		 */
		for (String _ele : listeBibliothequeSerieRanger.keySet())
		{
			logger.debug("BibliothequeSerie- listeBibliothequeSerieRanger " + _ele + "=" + listeBibliothequeSerieRanger.get(_ele).size());
		}

		// for( Serie _ele : listSeries.values())
		// {
		// Display.affichageLigne("listSeries",_ele.repertoire
		// +"="+_ele.titreSerie);
		// }

		/**
		 * traitement de chaque serie
		 */
		// Display.affichageProgressionInit("MiseAJourEpisodesRepertoire");
		ArrayList<String> listeNouveauFichier = new ArrayList<String>(0);
		for (Serie _ser : listSeries.values())
		{
			if (!_ser.isFeezer())
			{
				listeNouveauFichier.addAll(_ser.MiseAJourEpisodesRepertoire(listeBibliothequeSerieRanger.get(Repertoire.formatPath(_ser.repertoire))));
			}
		}
		logger.info("BibliothequeSerie- nombre \"Nouveau\" Fichier=" + listeNouveauFichier.size());

		/**
		 * traitement des Nouveau Fichier
		 */
		distribuerEtPurgerFichierDuRepertoire(listeNouveauFichier);

		if (Main.P.getHoraireFilebot() && Main.P.topMAJSerieAvecRepertoireRenomerEtRangerSiNecessaireLesEpisodes)
		{

			// ArrayList<String> listeNouveauFichierRanger = new
			// ArrayList<String>(0);
			/**
			 * rangement des videos
			 */
			for (String _key : listeBibliothequeSerieRanger.keySet())
			{
				if (_key.contains(Main.P.NomRepertoireTmpARanger))
				{
					if (listeBibliothequeSerieRanger.get(_key).size() > 0)
					{
						logger.debug("BibliothequeSerie- a Trier = " + "(" + listeBibliothequeSerieRanger.get(_key).size() + ")" + _key);
						/* listeNouveauFichierRanger = */AnalyserEtRangerVideoNonAttendue(_key);
					}
				}
			}

			// distribuerEtPurgerFichierDuRepertoire(listeNouveauFichierRanger);
			//
			//
			// /**
			// * retraitement de chaque serie pour prise en compte des
			// rangements
			// */
			// for (Serie _ser : listSeries.values()) {
			// _ser.MiseAJourEpisodesRepertoire(listeBibliothequeSerieRanger.get(Repertoire.formatPath(_ser.repertoire)));
			// _ser.setLastMajRepertoireDate(Calendar.getInstance().getTime());
			// }
		}

	}

	/**
	 * distibu les fichier du repertoire keylisteBibliotheque dans
	 * listeBibliothequeSerieRanger baser sur listeBibliothequeSerie et purge
	 * les fichier n'etant pas distribuer
	 * 
	 * @param keylisteBibliotheque
	 * @throws IOException
	 * @throws JSchException
	 * @throws InterruptedException
	 * @throws UnsupportedEncodingException
	 */
	private void distribuerEtPurgerFichierDuRepertoire(ArrayList<String> listeNouveauFichier)
	{
		logger.debug("BibliothequeSerie- listeNouveauFichier" + listeNouveauFichier.size());

		// Display.affichageProgressionInit("distribution fichier");
		int i = 0;
		for (String keylisteBibliothequeSerie : listeBibliothequeSerie.keySet())
		{
			i++;

			// if
			// (keylisteBibliothequeSerie.startsWith(keylisteBibliotheque))
			// {
			// Display.affichageProgressionAdd(i,
			// listeBibliothequeSerie.size(), "#");
			ArrayList<String> listeWithPathFichierRepertoire = new ArrayList<String>(0);
			if (listeBibliothequeSerieRanger.containsKey(keylisteBibliothequeSerie))
			{
				listeWithPathFichierRepertoire = listeBibliothequeSerieRanger.get(keylisteBibliothequeSerie);
			}

			for (String _fileWithPath : listeNouveauFichier)
			{
				if (_fileWithPath.startsWith(keylisteBibliothequeSerie))
				{
					listeWithPathFichierRepertoire.add(_fileWithPath);
				}
			}
			/**
			 * on distribue les fichir selectionner
			 */
			listeBibliothequeSerieRanger.put(keylisteBibliothequeSerie, listeWithPathFichierRepertoire);
			/**
			 * on retire les fichier selectionner du scan global
			 */
			if (listeWithPathFichierRepertoire.size() > 0)
			{
				listeNouveauFichier.removeAll(listeWithPathFichierRepertoire);
			}
			// }
			// else
			// {
			// Display.affichageProgressionAdd(i,
			// listeBibliothequeSerie.size());
		}
		// }
		if (Main.P.topMAJSerieAvecRepertoireRenomerEtRangerSiNecessaireLesEpisodesSupressionParasite)
		{
			if (Main.P.SerieForcer.compareTo("") == 0)
			{
				if (listeNouveauFichier.size() > 0)
				{
					Fichier.purgeListeFichier(listeNouveauFichier, EnumTypeDeFichier.part.extensions[0]);
					/**
					 * display
					 */
					for (String _ele : listeNouveauFichier)
					{
						logger.debug("BibliothequeSerie- purgeListeFichier " + _ele);
					}
				}
			}
		}

	}

	public void incorporerEncours(ArrayList<Episode> listEpisodesAuto) throws IOException
	{
		Map<String, Serie> serieMaj = new HashMap<String, Serie>(0);
		for (Episode _ep : listEpisodesAuto)
		{
			Serie _ser = listSeries.get(_ep.getTitreSerie());
			if (_ser != null)
			{
				_ser.incorporerEncours(_ep.getNumeroSaison(), _ep.getNumeroEpisode(), _ep.getNumeroEpisodeDouble(), _ep.getNumeroSeq());
				serieMaj.put(_ep.getTitreSerie(), _ser);
			}
		}

		for (Serie _ser : serieMaj.values())
		{
			_ser.sauvegardeXmlSerie();
		}
	}

	public void constituerListEpisodes()
	{
		listEpisodesManquant = new ArrayList<Episode>(0);
		listEpisodesAVenir = new ArrayList<Episode>(0);
		listEpisodesEnCours = new ArrayList<Episode>(0);
		listEpisodesPresent = new ArrayList<Episode>(0);
		listEpisodesTotal = new HashMap<String, Episode>(0);
		for (Serie _ser : listSeries.values())
		{
			listEpisodesManquant.addAll(_ser.getListEpisodes(EnumStatusEpisode.absent));
			listEpisodesAVenir.addAll(_ser.getListEpisodes(EnumStatusEpisode.avenir));
			listEpisodesEnCours.addAll(_ser.getListEpisodes(EnumStatusEpisode.encours));
			listEpisodesPresent.addAll(_ser.getListEpisodes(EnumStatusEpisode.present));
			listEpisodesTotal.putAll(_ser.getListeEpisodes());
		}
		Collections.sort(listEpisodesManquant, Episode.airDateComparator);
		Collections.sort(listEpisodesAVenir, Episode.airDateComparatorPlusAncienDabord);
		Collections.sort(listEpisodesEnCours, Episode.airDateComparatorPlusAncienDabord);
		Collections.sort(listEpisodesPresent, Episode.airDateComparatorPlusAncienDabord);
	}

	public ArrayList<Episode> getListEpisodesManquant()
	{
		return listEpisodesManquant;
	}

	public ArrayList<Episode> getListEpisodesAVenir()
	{
		return listEpisodesAVenir;
	}

	public ArrayList<Episode> getListEpisodesEnCours()
	{
		return listEpisodesEnCours;
	}

	public ArrayList<Episode> getListEpisodesPresent()
	{
		return listEpisodesPresent;
	}

	/**
	 * 
	 */
	public BibliothequeSerie() throws JSchException, IOException, InterruptedException, SftpException, ParseException, XmlRpcException
	{


		if (Main.P.topChargementEpisodesXml)
		{

			int nbSerie = 0;
			int nbSerieMajWeb = 0;

			ajouterNouvelleSerie();
			/**
			 * ajout de serie
			 */
			String fileJson = new Scanner(new File(Fichier.constituerCheminComplet(Main.P.workRepertoireParam, "paramSerie",
																				   EnumTypeDeFichier.json.extensions[0]))).useDelimiter("//A").next();
			JSONObject paramJSON = new JSONObject(fileJson);

			Object luSerie;
			String typeSerie;
			for (int t = 1; t < 3; t++)
			{
				luSerie = paramJSON.getString("bib" + String.valueOf(t));
				if (luSerie != null)
				{
					String biblothequeJson;
					if (((String) luSerie).indexOf(":") > 0 || ((String) luSerie).indexOf("/") == 0)
					{
						// repertoire ecrit en complet
						biblothequeJson = (String) luSerie;
					}
					else
					{
						biblothequeJson = Main.P.workRepertoire + (String) luSerie + File.separator;
					}

					Boolean repertoireAccesible = new Fichier(biblothequeJson).isPresenceOnDrive();
					this.listeBibliotheque.put(Repertoire.formatPath(biblothequeJson), repertoireAccesible);
					logger.info("BibliothequeSerie- biblothequeJson" + (repertoireAccesible ? "true-" : "false-") + biblothequeJson);
					Ssh.actionexecChmodR777(Repertoire.formatPath(biblothequeJson, false));

					Fichier.purgeEmptyRepertoire(biblothequeJson, new String[] { Main.P.NomRepertoireTmpARanger, Main.P.NomRepertoireCorbeille });
					typeSerie = paramJSON.getString(("typ" + String.valueOf(t)));
					JSONArray listParam = paramJSON.getJSONArray("serie" + String.valueOf(t));
					// Display.affichageProgressionInit("nb series traiter");
					int i = 0;
					for (i = 0; i < listParam.length(); i++)
					{
						String serie;
						serie = listParam.getString(i);

						if (Main.P.SerieForcer.compareTo(serie) == 0 || Main.P.SerieForcer.compareTo("") == 0)
						{
							logger.debug("BibliothequeSerie- serie" + serie + "==" + Main.P.SerieForcer);
							String titre = serie;
							boolean statusFreeze = false;

							if (serie.startsWith("#off#"))
							{
								titre = serie.substring(5);
								statusFreeze = true;
							}

							// int indiceSerie =
							// BibliothequeSerie.seriePresente(this.listSeries,
							// titre);
							// if (indiceSerie == -1)
							// {

							Serie NewSerie = new Serie(biblothequeJson, titre, EnumTypeDeSerie.getTypeDeSerie(typeSerie), repertoireAccesible,
													   statusFreeze);
							// Display.affichageProgressionIntermediaire("1");
							nbSerie++;

							// Display.affichageProgressionIntermediaire("2");
							this.listSeries.put(titre, NewSerie);
							this.listeBibliothequeSerie.put(Repertoire.formatPath(NewSerie.repertoire), new ArrayList<String>(0));
							this.listeBibliothequeSerie.put(Repertoire.formatPath(getRepertoireTmpAranger(titre)), new ArrayList<String>(0));
							// }
						}
						// Display.affichageProgressionAdd(i + 1,
						// listParam.length());
					}
				}
			}
			logger.debug("BibliothequeSerie- nombre de serie= " + nbSerie);
			// logger.debug("BibliothequeSerie- nombre de mise a jour web= " +
			// nbSerieMajWeb);
			if (Main.P.topMAJSerie)
			{
				MiseAJourWeb();
				MiseAJourRepertoire();
				setChmod777All();
				// Display.affichageProgressionIntermediaire("3");

			}
		}


	}

	private void ajouterNouvelleSerie() throws FileNotFoundException
	{
		int nbSerie = 0;
		/**
		 * ajout de serie
		 */
		String fileJson = new Scanner(new File(Fichier.constituerCheminComplet(Main.P.workRepertoireParam, "paramSerie",
																			   EnumTypeDeFichier.json.extensions[0]))).useDelimiter("//A").next();
		JSONObject paramJSON = new JSONObject(fileJson);

		Object luSerie;
		for (int t = 1; t < 3; t++)
		{
			luSerie = paramJSON.getString("bib" + String.valueOf(t));
			if (luSerie != null)
			{
				String biblothequeJson;
				if (((String) luSerie).indexOf(":") > 0 || ((String) luSerie).indexOf("/") == 0)
				{
					// repertoire ecrit en complet
					biblothequeJson = (String) luSerie;
				}
				else
				{
					biblothequeJson = Main.P.workRepertoire + (String) luSerie + File.separator;
				}

				Boolean repertoireAccesible = new Fichier(biblothequeJson).isPresenceOnDrive();
				this.listeBibliotheque.put(Repertoire.formatPath(biblothequeJson), repertoireAccesible);

				String typeSerie = paramJSON.getString(("typ" + String.valueOf(t)));
				JSONArray listParam = paramJSON.getJSONArray("serie" + String.valueOf(t));

				int i = 0;
				for (i = 0; i < listParam.length(); i++)
				{
					String serie;
					serie = listParam.getString(i);

					String titre = serie;
					String status = "encours";

					if (serie.startsWith("#off#"))
					{
						titre = serie.substring(5);
						status = "off";
					}

					interfacedonnees.addSerie(titre , typeSerie , Param.dateLowValue , biblothequeJson , status);


					nbSerie++;

				}
			}
		}
		
	}

	public String genererFichirStats() throws ParseException, IOException
	{

		if (listEpisodesManquant.size() == 0)
		{
			constituerListEpisodes();
		}

		Fichier f = new Fichier(Main.P.workRepertoire, "Statistique", EnumTypeDeFichier.html.extensions[0]);

		String content = "";
		content += Serie.StatsEntete();

		for (Serie _ser : listSeries.values())
		{
			content += _ser.Stats();
		}
		int nbtotal = (this.listEpisodesPresent.size() + this.listEpisodesManquant.size() + this.listEpisodesEnCours.size());
		int percent = (this.listEpisodesPresent.size() * 100) / nbtotal;
		content += "<TR>";
		content += "<TD>TOTAL</TD>";
		content += "<TD></TD>";
		content += "<TD>" + nbtotal + " (+" + this.listEpisodesAVenir.size() + ")" + "<BR>" + /*
			 * +
			 * "</TD>"
			 * ;
			 * content
			 * +=
			 * "<TD>"
			 * +
			 */this.listEpisodesPresent.size() + "<BR>"
			+ /*
			 * + "</TD>"; content += "<TD>"
			 */String.format("%1$02d", percent) + "%" + "</TD>";
		// content += "<TD></TD>";
		content += "<TD>" + this.listEpisodesEnCours.size() + "<BR>" + /*
			 * +
			 * "</TD>"
			 * ;
			 * content
			 * +=
			 * "<TD>"
			 * +
			 */this.listEpisodesManquant.size() + "<BR>" + /*
			 * +
			 * "</TD>"
			 * ;
			 * content
			 * +=
			 * "<TD>"
			 * +
			 */this.listEpisodesAVenir.size()
			+ "</TD>";
		content += "<TD></TD>";
		content += "</TR>";

		content += Serie.StatsPied();

		content += "<TABLE BORDER>";
		content += "<TR><TD>Les 50 episodes en cours </TD><TD>Les 50 episodes manquant suivant</TD><TD>les 50 prochain episodes</TD></TR>";
		for (int i = 0; (i < listEpisodesManquant.size() || i < this.listEpisodesAVenir.size() || i < this.listEpisodesEnCours.size()) && i < 50; i++)
		{
			content += "<TR>";
			content += "<TD>";
			if (i < listEpisodesEnCours.size())
			{
				content += listEpisodesEnCours.get(i).toString();
			}
			content += "</TD>";
			content += "<TD>";
			if (i < listEpisodesManquant.size())
			{
				content += listEpisodesManquant.get(i).toString();
			}
			content += "</TD>";
			content += "<TD>";
			if (i < this.listEpisodesAVenir.size())
			{
				content += this.listEpisodesAVenir.get(i).toString();
			}
			content += "</TD>";
			content += "</TR>";
		}
		content += "</TABLE>";

		f.delete();
		f.WriteToFile(content);
		return content;
	}

	/**
	 * _____ class privée ______
	 */

	public int getNbEpisodesMax(String titreSerie, Integer numeroSaison)
	{
		int numMax = 0;
		Serie _ser = listSeries.get(titreSerie);
		if (_ser != null)
		{
			for (Episode _ep : _ser.getListeEpisodes().values())
			{
				if (_ep.getNumeroSaison().compareTo(numeroSaison) == 0)
				{
					if (numMax < _ep.getNumeroEpisode())
					{
						numMax = _ep.getNumeroEpisode();
					}
				}
			}
		}
		return numMax;
	}

	public String[] getListSeriesName()
	{
		String[] ret = listSeries.keySet().toArray(new String[0]);
		return ret;
	}

	public String getRepertoireTmpAranger(String titreSerie)
	{
		Serie _ser = listSeries.get(titreSerie);
		if (_ser != null)
		{
			return _ser.getRepertoireBase() + Main.P.NomRepertoireTmpARanger + File.separator + _ser.getTypeSerie().getNom() + File.separator;
		}
		return "";
	}

	public String setChmod777All() throws JSchException, IOException, InterruptedException
	{
		for (Serie _ser : listSeries.values())
		{
			Ssh.actionexecChmodR777(Repertoire.formatPath(_ser.repertoire, false));
		}
		return "";
	}

	public String getRepertoireTmpArangerSpecifiqueSerie(String titreSerie)
	{
		Serie _ser = listSeries.get(titreSerie);
		if (_ser != null)
		{
			return _ser.getRepertoireTmpArangerSpecifiqueSerie();
		}
		return "";
	}

	private ArrayList<String> AnalyserEtRangerVideoNonAttendue(String RepertoireATraiter) throws JSchException, IOException, XmlRpcException, ParseException,
	InterruptedException
	{

		// *liste des nouveau fichier
		ArrayList<String> nouveauFichier = new ArrayList<String>(0);

		ArrayList<String> listeWithPathVideoPresent = new ArrayList<String>(0);
		ArrayList<String> listeWithPathVideoRanger = new ArrayList<String>(0);
		ArrayList<String> listeWithPathVideoCorbeille = new ArrayList<String>(0);

		// purge le fchier historique de filebot
		Ssh.executeAction("rm /mnt/HD/HD_a2/ffp/opt/share/filebot/data/history.xml");
		Ssh.executeAction("nice -n 19 \"/mnt/HD/HD_a2/ffp/opt/share/filebot/bin/filebot.sh\" -clear-cache ");

		EnumTypeDeSerie TypeSerie = EnumTypeDeSerie.getTypeDeSerie(Fichier.getFileParentName(RepertoireATraiter + "/*"));
		String RepertoireBase = Fichier.getPathParent(Fichier.getPathParent(RepertoireATraiter));

		Map<String, ArrayList<String>> listeVideoTest = Serie.filtreFichierVideo(Repertoire.searchAllFiles(RepertoireATraiter, 99, false), "");
		logger.debug("RepertoireATraiter => (" + listeVideoTest.get("listeVideoRepertoire").size() + ") " + RepertoireATraiter);
		if (listeVideoTest.get("listeVideoRepertoire").size() > 0)
		{
			logger.info("RepertoireATraiter => (" + listeVideoTest.get("listeVideoRepertoire").size() + ") " + RepertoireATraiter);
			// for (String cheminVideoNonAtendue : listeVideoNonAttendue) {
			// logger.debug(cheminVideoNonAtendue);
			ArrayList<String> ret = Ssh.executeAction(
				"nice -n 19 \"/mnt/HD/HD_a2/ffp/opt/share/filebot/bin/filebot.sh\" -rename \"" + Fichier.formatPath(RepertoireATraiter, false)
				+ "\" --db TheTVDB --lang en --conflict override --encoding=UTF-8 --format "
				+ Fichier.formatPath(this.formatSortie(RepertoireBase, TypeSerie)) + " -r -non-strict ");
			// --conflict override
			if (ret.size() > 2)
			{

				Pattern pattern = Pattern.compile("(\\[[^\\[\\]]*\\])+");
				for (String _st : ret)
				{
					if (_st.length() > 6)
					{
						Matcher matcher = pattern.matcher(_st);
						ArrayList<String> f = new ArrayList<String>(0);
						while (matcher.find())
						{
							f.add(matcher.group());
						}

						if (f.size() >= 2)
						{
							String fnom = Repertoire.formatPath(f.get(1).replaceAll("\\[", "").replaceAll("\\]", ""), false);
							if (f.get(0).compareToIgnoreCase("[MOVE]") == 0)
							{
								fnom = Repertoire.formatPath(f.get(2).replaceAll("\\[", "").replaceAll("\\]", ""), false);
								if (EnumTypeDeFichier.video.EstDuTypeDeFichier(Fichier.getFileExtension(fnom)))
								{
									ArrayList<Episode> retEp = this.getEpisodeFromName(_st);
									if (retEp.size() > 0)
									{
										listeWithPathVideoRanger.add(Repertoire.formatPath(fnom));
										listeWithPathVideoPresent.add(fnom);
									}
									else
									{
										listeWithPathVideoCorbeille.add(Repertoire.formatPath(fnom));
									}
								}
							}
							if (EnumTypeDeFichier.video.EstDuTypeDeFichier(Fichier.getFileExtension(fnom)))
							{
								listeWithPathVideoPresent.add(fnom);
							}
						}
					}
				}

				ret = incorporerListePresent(listeWithPathVideoPresent);
				for (String _st : ret)
				{
					listSeries.get(_st).sauvegardeXmlSerie();
				}

				for (String _st : listeWithPathVideoRanger)
				{

					logger.info("episode- Move to " + _st);

					// Map<String, String> detail =
					// Episode.decomposerNomFichier(_st, listSer);
					// Episode _ep = recupererEpisode(detail.get("serie"),
					// detail.get("saison"), detail.get("episode"),
					// detail.get("sequentiel"));
					ArrayList<Episode> _arrayep = getEpisodeFromName(_st);
					for (Episode _ep : _arrayep)
					{
						Serie _ser = this.listSeries.get(_ep.getTitreSerie());

						WordPressHome.publishOnBlog(
							6,
							Main.P.idUnique + "_" + _ep.file.getName(),
							_ep.file.getName(),
							new String[] { _ep.getTitreSerie(), "S" + _ep.getNumeroSaison(), "E" + _ep.getNumeroEpisode(), "N" + _ep.getNumeroSeq() },
							new String[] { "Serie" },
							"<a href=\"http://home.daisy-street.fr/BibPerso/stream.php?flux="
							+ URLEncoder.encode(Repertoire.formatPath(_st, false), "UTF-8") + "\">" + _ep.file.getName() + "</a>" + "\n"
							+ Serie.StatsEntete() + _ser.Stats() + Serie.StatsPied());
					}
					// PhpPhusion.InsertPhpPhusion("9", Main.idUnique + "_"
					// + nomEpi, nomEpi,
					// "<a href=\"http://home.daisy-street.fr/BibPerso/stream.php?flux="
					// + URLEncoder.encode(_st, "UTF-8") + "\">" + nomEpi
					// + "</a>");
				}

				for (String _st : listeWithPathVideoCorbeille)
				{
					String dest = Fichier.formatPath(Main.P.CheminCorbeille + Main.P.NomRepertoireCorbeille + File.separator , false);
					Repertoire.creeRepertoire(Fichier.getPathParent(dest));
					Ssh.executeAction("mv \"" + Fichier.formatPath(_st, false) + "\" \"" + Fichier.formatPath(dest, false) + "\"");
				}

				nouveauFichier.addAll(listeWithPathVideoRanger);
			}
		}

		// * purger des fichier retstant non video
		Map<String, ArrayList<String>> listeVideo = Serie.filtreFichierVideo(Repertoire.searchAllFiles(RepertoireATraiter, 99, false), "");
		ArrayList<String> listeWithPathFichierAPurger = new ArrayList<String>(0);
		listeWithPathFichierAPurger.addAll(listeVideo.get("listeVideoRepertoireAutre"));
		/**
		 * Remove en comparant sans les extension
		 */
		ArrayList<String> liste_stARemove = new ArrayList<String>(0);
		for (String _st : listeWithPathFichierAPurger)
		{
			for (String _stARemove : listeVideo.get("listeVideoRepertoire"))
			{
				if (Fichier.getFilePartNameWithoutExt(_st).contains(Fichier.getFilePartNameWithoutExt(_stARemove)))
				{
					liste_stARemove.add(_st);
				}
			}
		}
		// *exclusion dessoustitres
		listeWithPathFichierAPurger.removeAll(liste_stARemove);
		if (listeWithPathFichierAPurger.size() > 0)
		{
			/**
			 * supression des fichier non video et non soustire des video et non
			 * xml serie
			 */
			Fichier.purgeListeFichier(listeWithPathFichierAPurger, EnumTypeDeFichier.part.extensions[0]);
		}
		Repertoire.purgeEmptyRepertoire(RepertoireATraiter, new String[] { Main.P.NomRepertoireTmpARanger, Main.P.NomRepertoireCorbeille });
		return nouveauFichier;
	}

	private String formatSortie(String repertoire, EnumTypeDeSerie typeSerie)
	{
		String ret;
		if (typeSerie.serie.equals(typeSerie))
		{
			ret = "\"" + repertoire + "/{n}/Saison {s.pad(2)}/{n} {s00e00} {t}\"";
		}
		else
		{
			ret = "\"" + repertoire + "/{n}/Saison {s.pad(2)}/{n} {s00e00} ep_{absolute.pad(3)} {t}\"";
		}
		return ret;
	}

	public ArrayList<String> incorporerListePresent(ArrayList<String> listeWithPathVideoPresent) throws UnsupportedEncodingException
	{
		// String repb = Repertoire.formatPath(repertoire, false);
		ArrayList<String> retour = new ArrayList<String>(0);
		for (String pathVideoPresent : listeWithPathVideoPresent)
		{

			Map<String, String> ret = Episode.decomposerNomFichier(pathVideoPresent, this.getListSeriesName());
			// if (ret.get("serie") == this.titreSerie) {

			Episode _ep = recupererEpisode(ret.get("serie"), ret.get("saison"), ret.get("episode"), ret.get("sequentiel"));
			if (_ep != null)
			{
				if (!retour.contains(ret.get("serie")))
				{
					retour.add(ret.get("serie"));
				}
				logger.debug(_ep.toString());
				// Display.affichageLigne(_ep.toString());
				_ep.setAPresent(pathVideoPresent);
				//_ep.setStatusActuel(EnumStatusEpisode.present);
				// Display.affichageLigne(_ep.cheminComplet);
				//_ep.cheminComplet = /* Fichier.FormatUTF8toISO8859 */(pathVideoPresent);
				// Display.affichageLigne(_ep.cheminComplet);
				// Display.affichageLigne(pathVideoPresent);
			}
			else
			{
				logger.debug("serie- episodeManquant incorporerListePresent not this : " + pathVideoPresent);
			}

			if (ret.containsKey("episodebis"))
			{
				Episode _epbis = recupererEpisode(ret.get("serie"), ret.get("saison"), ret.get("episodebis"), ret.get("sequentiel"));
				if (_epbis != null)
				{
					// Display.affichageLigne(_ep.toString());
					_epbis.setAPresent(pathVideoPresent);
					_epbis.transformerEnEpisodeDouble(Integer.valueOf(ret.get("episode")));
					_ep.transformerEnEpisodeDouble(Integer.valueOf(ret.get("episodebis")));
					_ep.setAPresent(pathVideoPresent);
				}

			}
			// }
		}

		return retour;
	}

	private Episode recupererEpisode(String serie, String numsaison, String numepisode, String numsequentiel)
	{
		Serie _ser = listSeries.get(serie);
		if (_ser != null)
		{
			return _ser.recupererEpisode(serie, numsaison, numepisode, numsequentiel);
		}
		return null;

	}

	private int retint(String numero)
	{
		if (numero == null)
		{
			return 0;
		}
		else
		{
			if (isNumeric(numero))
			{
				return Integer.valueOf(numero);
			}
			else
			{
				return 0;
			}
		}
	}

	static boolean isNumeric(String text)
	{
		return text.matches("[-+]?\\d+(\\.\\d+)?");
	}

	public ArrayList<Episode> getEpisodeFromName(String nom)
	{
		ArrayList<Episode> ret = new ArrayList<Episode>(0);
		Map<String, String> detail = Episode.decomposerNomFichier(nom, this.getListSeriesName());
		Episode _ep = recupererEpisode(detail.get("serie"), detail.get("saison"), detail.get("episode"), detail.get("sequentiel"));
		if (_ep != null)
		{
			ret.add(_ep);
		}
		if (detail.containsKey("episodebis"))
		{
			Episode _epbis = recupererEpisode(detail.get("serie"), detail.get("saison"), detail.get("episodebis"), detail.get("sequentiel"));
			if (_epbis != null)
			{
				ret.add(_epbis);
			}
		}
		return ret;
	}
}
