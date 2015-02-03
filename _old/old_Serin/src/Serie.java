import java.io.*;
import java.lang.ProcessBuilder.*;
import java.net.*;
import java.util.*;

import org.xml.sax.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.jcraft.jsch.*;

import net.htmlparser.jericho.*;

import java.text.*;
import java.util.regex.*;
import java.security.acl.*;

import org.apache.log4j.Logger;
import org.apache.log4j.chainsaw.*;

import java.util.concurrent.*;

import org.apache.xmlrpc.*;

/**
 * Defini les caracteristique d'une serie 
 *
 * @author Romain CLEMENT
 */

/**
 * @author Malicia
 * 
 */
public class Serie extends Repertoire
{
	private final static Logger logger = Logger.getLogger(Serie.class);
	
	private String titreSerieFormatRepertoire;
	private EnumTypeDeSerie typeSerie;
	private String repertoireBase;
	private Boolean repertoirreAccesible;
	private boolean estTermine;
	private Map<String, Episode> listeEpisodes = new TreeMap<String, Episode>();
	private Calendar dateDeDerniereMiseAJourWeb = new GregorianCalendar();
	private Calendar dateDeProchainAirdate = new GregorianCalendar();

	
	private Fichier fichierXml;
	
	private boolean recuperationRemoteXmlFaite = false;

	private Date lastMajRepertoireDate;

	public Serie()
	{
	}

	public Date getLastMajRepertoireDate()
	{
		return lastMajRepertoireDate;
	}

	public void setLastMajRepertoireDate(Date lastMajRepertoireDate)
	{
		this.lastMajRepertoireDate = lastMajRepertoireDate;
	}

	public String getRepertoireBase()
	{
		return repertoireBase;
	}

	public void incorporerEncours(Integer numeroSaison, Integer numeroEpisode, Integer numeroEpisodeDouble, Integer numeroSeq)
	{
		Episode _ep = recupererEpisode(String.valueOf(numeroSaison), String.valueOf(numeroEpisode), String.valueOf(numeroSeq));
		if (_ep != null)
		{
			_ep.setAEncours();
		}
		if (numeroEpisodeDouble > 0)
		{
			Episode _epbis = recupererEpisode(String.valueOf(numeroSaison), String.valueOf(numeroEpisodeDouble), String.valueOf(numeroSeq));
			if (_epbis != null)
			{
				_epbis.setAEncours();
			}

		}
	}

	public void purgeHash(Integer numeroSaison, Integer numeroEpisode, Integer numeroEpisodeDouble, Integer numeroSeq)
	{
		Episode _ep = recupererEpisode(String.valueOf(numeroSaison), String.valueOf(numeroEpisode), String.valueOf(numeroSeq));
		if (_ep != null)
		{
			_ep.arrayHashExcluclear();
		}
		if (numeroEpisodeDouble > 0)
		{
			Episode _epbis = recupererEpisode(String.valueOf(numeroSaison), String.valueOf(numeroEpisodeDouble), String.valueOf(numeroSeq));
			if (_epbis != null)
			{
				_epbis.arrayHashExcluclear();
			}

		}
	}
	public void addHash(Integer numeroSaison, Integer numeroEpisode, Integer numeroEpisodeDouble, Integer numeroSeq, String hashCode)
	{
		Episode _ep = recupererEpisode(String.valueOf(numeroSaison), String.valueOf(numeroEpisode), String.valueOf(numeroSeq));
		if (_ep != null)
		{
			_ep.arrayHashExcluadd(hashCode);
		}
		if (numeroEpisodeDouble > 0)
		{
			Episode _epbis = recupererEpisode(String.valueOf(numeroSaison), String.valueOf(numeroEpisodeDouble), String.valueOf(numeroSeq));
			if (_epbis != null)
			{
				_epbis.arrayHashExcluadd(hashCode);
			}

		}
	}
	public Boolean isHash(Integer numeroSaison, Integer numeroEpisode, Integer numeroEpisodeDouble, Integer numeroSeq, String hashCode)
	{
		boolean ret = false;
		Episode _ep = recupererEpisode(String.valueOf(numeroSaison), String.valueOf(numeroEpisode), String.valueOf(numeroSeq));
		if (_ep != null)
		{
			ret = ret || _ep.arrayHashExclucontains(hashCode);
		}
		if (numeroEpisodeDouble > 0)
		{
			Episode _epbis = recupererEpisode(String.valueOf(numeroSaison), String.valueOf(numeroEpisodeDouble), String.valueOf(numeroSeq));
			if (_epbis != null)
			{
				ret = ret || _epbis.arrayHashExclucontains(hashCode);
			}
		}
		return ret;
	}

	public boolean isEstTermine()
	{
		return estTermine;
	}

	public Map<String, Episode> getListeEpisodes()
	{
		return listeEpisodes;
	}

	public Calendar getDateDeDerniereMiseAJourWeb()
	{
		return dateDeDerniereMiseAJourWeb;
	}

	public ArrayList<Episode> getListEpisodes(EnumStatusEpisode status)
	{
		ArrayList<Episode> ret = new ArrayList<Episode>(0);
		for (Episode _ep : listeEpisodes.values())
		{
			if (_ep.isStatus(status))
			{
				ret.add(_ep);
			}
		}
		return ret;
	}

	/**
	 * @param repertoire
	 * @param titre
	 * @param typeSerie
	 */
	public Serie(String repertoire, String titre, EnumTypeDeSerie typeSerie, Boolean repertoirreAccesible, Boolean feezeStatus) throws ParseException,
	IOException
	{
		super(Serie.nomRepertoireSerie(repertoire, Serie.getTitreNettoyer(titre)));

		this.repertoireBase = repertoire;
		// this.titreSerieBrut = titre;
		this.titreSerieFormatRepertoire = Serie.getTitreNettoyer(titre);
		this.typeSerie = typeSerie;
		this.repertoirreAccesible = repertoirreAccesible;

		this.dateDeDerniereMiseAJourWeb.setTime((new SimpleDateFormat("dd/mm/yy")).parse("01/01/01"));

		this.SelectionFichierXmlSerie();
		this.Setfeezer(feezeStatus);
		this.chargementXmlSerie();

		this.sauvegardeXmlSerie();
	}

	public Boolean recuperationRemoteXmlFile() throws JSchException, IOException, SftpException
	{
		if (!recuperationRemoteXmlFaite)
		{
			Fichier f;
			Fichier finacessible;
			f = new Fichier(this.repertoire, this.titreSerieFormatRepertoire, EnumTypeDeFichier.xml.extensions[0]);
			finacessible = new Fichier(Main.P.workRepertoireXml, this.titreSerieFormatRepertoire, EnumTypeDeFichier.xml.extensions[0]);
			Ssh.getRemoteFile(Repertoire.formatPath(f.repertoire), Repertoire.formatPath(finacessible.repertoire), finacessible.getNom() + "."
							  + finacessible.extension);
			recuperationRemoteXmlFaite = true;
			return true;

			// return false;
		}
		return false;
	}

	private void SelectionFichierXmlSerie() throws IOException
	{

		Fichier f;
		Fichier finacessible;
		f = new Fichier(this.repertoire, this.titreSerieFormatRepertoire, EnumTypeDeFichier.xml.extensions[0]);
		finacessible = new Fichier(Main.P.workRepertoireXml, this.titreSerieFormatRepertoire, EnumTypeDeFichier.xml.extensions[0]);

		if (repertoirreAccesible)
		{
			Fichier.copyFile(f.file, finacessible.file,false);
		}
		else
		{
			f = finacessible;
		}

		this.fichierXml = f;

	}

	/**
	 * @param f
	 * @throws FileNotFoundException
	 */
	private void chargementXmlSerie() throws FileNotFoundException
	{
		if (this.fichierXml.presenceOnDrive && (this.fichierXml.file.length() > 21))
		{
			// Display.affichageLigne(f.cheminComplet);
			Serie unSerialSerie = new Serie();
			this.fichierXml.unserialiseXML(unSerialSerie);

			estTermine = unSerialSerie.isEstTermine();
			listeEpisodes = new TreeMap<String, Episode>(unSerialSerie.getListeEpisodes());
			
			dateDeDerniereMiseAJourWeb = unSerialSerie.getDateDeDerniereMiseAJourWeb();
			setLastMajRepertoireDate(unSerialSerie.getLastMajRepertoireDate());
			dateDeProchainAirdate = unSerialSerie.dateDeProchainAirdate;
			

		}
	}

	public void sauvegardeXmlSerie() throws IOException
	{
		this.fichierXml.serialiseXML(this);

	}

	public static String StatsEntete()
	{
		String content = "";
		content += (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime());
		content += "<TABLE BORDER>";
		content += "<TR>";
		content += "<TD>titreSerie</TD>";
		content += "<TD>dateDeDerniereMiseAJourWeb</TD>";
		content += "<TD>nb total" + "<BR>" + /*
			 * </TD>"; content += "<TD>
			 */"nb present" + "<BR>" + /*
			 * </TD>";
			 * content
			 * +=
			 * "<TD>
			 */"percentComplet</TD>";
		// content += "<TD>dateDeProchainEpisode</TD>";
		content += "<TD>nb encours" + "<BR>" + /*
			 * </TD>"; content += "<TD>
			 */"nb absent" + "<BR>" + /*
			 * </TD>";
			 * content
			 * +=
			 * "<TD>
			 */"nb avenir</TD>";
		content += "<TD>schema</TD>";
		content += "</TR>";
		return content;
	}

	public static String StatsPied()
	{
		String content = "";
		content += "</TABLE BORDER>";
		return content;
	}

	public String Stats() throws IOException, ParseException
	{
		logger.debug(this.titreSerieFormatRepertoire);
		int Statstabnbtotal = 0;
		int Statstabnbeppresent = 0;
		int Statstabnbepencours = 0;
		int Statstabnbepabsent = 0;
		int Statstabnbepavenir = 0;

		ArrayList<ArrayList<String>> visu = new ArrayList<ArrayList<String>>();
		if (this.listeEpisodes != null)
		{
			for (Episode episodeele : this.listeEpisodes.values())
			{
				if (episodeele.getNumeroSaison() > 0 && episodeele.getNumeroEpisode() > 0)
				{
					Statstabnbtotal++;
					String icone = "#";
					if (episodeele.isStatus(EnumStatusEpisode.present))
					{
						Statstabnbeppresent++;
						icone = "X";
					}
					else
					{
						if (episodeele.isStatus(EnumStatusEpisode.encours))
						{
							Statstabnbepencours++;
							icone = ">";
						}
						else
						{
							if (episodeele.isStatus(EnumStatusEpisode.avenir))
							{
								Statstabnbepavenir++;
								icone = "<span title=\"" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(episodeele.getAirDate()) + "\">_</span>";
							}
							else
							{
								Statstabnbepabsent++;
								icone = "<span title=\"" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(episodeele.getAirDate()) + "\">-</span>";
							}
						}
					}
					while (visu.size() < episodeele.getNumeroSaison())
					{
						visu.add(new ArrayList<String>());
					}
					ArrayList<String> getVisuSaison = visu.get(episodeele.getNumeroSaison() - 1);
					while (getVisuSaison.size() < episodeele.getNumeroEpisode())
					{
						getVisuSaison.add("");
					}
					getVisuSaison.set(episodeele.getNumeroEpisode() - 1, icone);
					visu.set(episodeele.getNumeroSaison() - 1, getVisuSaison);
				}
			}
		}
		String Statstabschema = this.Miseenforme(visu);

		int percentComplet = 0;
		if (Statstabnbtotal > 0)
		{
			percentComplet = (Statstabnbeppresent * 100) / Statstabnbtotal;
		}

		this.sauvegardeXmlSerie();

		String Statstabmefonerow = "<TR>";
		Statstabmefonerow += "<TD>" + this.titreSerieFormatRepertoire + " " + (this.estTermine ? "<+>" : "") + "</TD>";

		// nombre de jour depuis derniere maj web
		long nbdaydermaj = Calendar.getInstance().getTime().getTime() - this.dateDeDerniereMiseAJourWeb.getTime().getTime();
		// nb jour prochin airdate
		long nbdaynextep = this.dateDeProchainAirdate.getTime().getTime() - Calendar.getInstance().getTime().getTime();
		// logger.debug((new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		// logger.debug((new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(this.dateDeDerniereMiseAJourWeb.getTime()));
		// logger.debug((new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(this.dateDeProchainAirdate.getTime()));
		logger.debug(this.titreSerieFormatRepertoire + ":nbdaydermaj=" + nbdaydermaj + ":nbdaynextep=" + nbdaynextep);
		Statstabmefonerow += "<TD>"
			// + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(
			// this.dateDeDerniereMiseAJourWeb.getTime() )
			+ "last maj web :" + "<BR>"
			+ (nbdaydermaj < 0 ? " -" : "")
			+ TimeUnit.MILLISECONDS.toDays((long) Math.abs(nbdaydermaj))
			+ " days ago"
			+ "<BR>"
			+ "next ep :" + "<BR>"
			+ (nbdaynextep < 0 ? " -" : "")
			+ ((new SimpleDateFormat("yyyy-MM-dd")).format(this.dateDeProchainAirdate.getTime()).equals("2099-01-31") ? "-----" : TimeUnit.MILLISECONDS
			.toDays((long) Math.abs(nbdaynextep))) + " days" + "</TD>";

		Statstabmefonerow += "<TD>" + Statstabnbtotal + "<BR>" /*
			 * "</TD>";
			 * Statstabmefonerow
			 * += "<TD>"
			 */+ Statstabnbeppresent + "<BR>" /*
			 * "</TD>"
			 * ;
			 * Statstabmefonerow
			 * +=
			 * "<TD>"
			 */+ String.format("%1$02d", percentComplet) + "%" + "</TD>";

		// Statstabmefonerow += "<TD>"
		// + (this.dateDeProchainEpisode == null ? "" : ((new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(this.dateDeProchainEpisode.getTime())))
		// + "</TD>";
		Statstabmefonerow += "<TD>" + (Statstabnbepencours > 0 ? ("" + Statstabnbepencours) : "0") + "<BR>" /*
			 * "</TD>"
			 * ;
			 * /
			 * /
			 * Statstabmefonerow
			 * +=
			 * "<TD>"
			 * +
			 * StatstabnbepisStalled
			 * +
			 * "</TD>"
			 * ;
			 * Statstabmefonerow
			 * +=
			 * "<TD>"
			 */
			+ (Statstabnbepabsent > 0 ? ("" + Statstabnbepabsent) : "0") + "<BR>" /*
			 * "</TD>"
			 * ;
			 * Statstabmefonerow
			 * +=
			 * "<TD>"
			 */+ (Statstabnbepavenir > 0 ? ("" + Statstabnbepavenir) : "0") + "</TD>";
		Statstabmefonerow += "<TD>" + Statstabschema + "</TD>";
		Statstabmefonerow += "<TD>" +  "</TD>";
		Statstabmefonerow += "<TR>";

		return Statstabmefonerow;
	}

	/**
	 * mise ajour des epidsodes de la serie
	 * 
	 */

	public Boolean MiseAJourEpisodesWeb() throws JSchException, IOException, ParseException, InterruptedException, SftpException
	{

		if (!estTermine)
		{

			if (this.dateDeDerniereMiseAJourWeb.after(Calendar.getInstance()))
			{
				this.dateDeDerniereMiseAJourWeb = Calendar.getInstance();
			}
			Calendar dateDeclenchement2Month = Calendar.getInstance();
			dateDeclenchement2Month.setTime(this.dateDeDerniereMiseAJourWeb.getTime());
			dateDeclenchement2Month.add(Calendar.MONTH, 3);
			Calendar dateDeclenchement1Month = Calendar.getInstance();
			dateDeclenchement1Month.setTime(this.dateDeDerniereMiseAJourWeb.getTime());
			dateDeclenchement1Month.add(Calendar.MONTH, 1);
			Calendar dateDeclenchement7Day = Calendar.getInstance();
			dateDeclenchement7Day.setTime(this.dateDeDerniereMiseAJourWeb.getTime());
			dateDeclenchement7Day.add(Calendar.DAY_OF_YEAR, 7);

			this.dateDeProchainAirdate.setTime(calculNextAirdate());

			if (Calendar.getInstance().getTime().after(dateDeclenchement2Month.getTime())
				|| (Calendar.getInstance().getTime().after(dateDeclenchement1Month.getTime()) && (this.listeEpisodes.size() == 0))
				|| (Calendar.getInstance().getTime().after(dateDeclenchement7Day.getTime()) && Calendar.getInstance().getTime()
				.after(this.dateDeProchainAirdate.getTime())))
			{

				if (!recuperationRemoteXmlFaite && !repertoirreAccesible)
				{
					if (this.recuperationRemoteXmlFile())
					{
						SelectionFichierXmlSerie();
						chargementXmlSerie();
						recuperationRemoteXmlFaite = true;
						return MiseAJourEpisodesWeb();
					}
				}

				Map<String, Episode> webEpisode = Serie.recupererListEpisodesWeb(this);

				this.listeEpisodes.putAll(Serie.mergeListEpisodes(this.listeEpisodes, webEpisode));
				this.dateDeDerniereMiseAJourWeb = Calendar.getInstance();
				this.dateDeProchainAirdate.setTime(calculNextAirdate());
				this.sauvegardeXmlSerie();
				return true;
			}

		}
		return false;

	}

	private Date calculNextAirdate() throws ParseException
	{
		// logger.debug("calculNextAirdate");
		Date dtJour = Calendar.getInstance().getTime();
		// date maximun 9999
		Calendar returnDate = Calendar.getInstance();
		returnDate.setTime((new SimpleDateFormat("dd/mm/yyyy")).parse("31/12/2099"));
		if (dateDeProchainAirdate == null)
		{
			dateDeProchainAirdate = returnDate;
		}

		// if (returnDate.getTime().compareTo(dateDeProchainAirdate.getTime())
		// == 0)
		// {
		long diffPrev = returnDate.getTime().getTime() - dtJour.getTime();
		for (Episode _ep : listeEpisodes.values())
		{
			// if (!(new
			// SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(_ep.getAirDate()).equals("2001-01-01 00:01:00"))
			// {
			// logger.info(_ep.getStatusString()+"="+_ep.toString() );
			if (_ep.isStatus(EnumStatusEpisode.avenir))
			{
				// calcul de la date
				long diff = _ep.getAirDate().getTime() - dtJour.getTime();
				if (diff < diffPrev)
				{
					diffPrev = diff;
					returnDate.setTime(_ep.getAirDate());
				}
			}
			// }
		}
		// stockage date propriete Serie
		this.dateDeProchainAirdate = returnDate;
		// }
		return dateDeProchainAirdate.getTime();
	}

	/**
	 * recuperr la liste depisodes depuis le web
	 * 
	 */
	private static Map<String, Episode> recupererListEpisodesWeb(Serie serieAMettreAJour) throws JSchException, IOException, ParseException,
	InterruptedException
	{
		ArrayList<String> ret = new ArrayList<String>(0);

		if (Main.P.topMAJSerieViaWeb)
		{
			ret = Ssh.executeAction("nice -n 19  \"/mnt/HD/HD_a2/ffp/opt/share/filebot/bin/filebot.sh\" -list --db TheTVDB --q \"" + serieAMettreAJour.titreSerieFormatRepertoire
								 + "\" --format '{n}#{s}#{e}#{absolute}#{airdate}#{t}#' ");
		}

		/**
		 * nettoyage tableau retour
		 */
		ArrayList<String> arrayListEpisode = new ArrayList<String>(0);
		arrayListEpisode.addAll(ret);
		for (String lineEp : ret)
		{
			if (lineEp.replaceAll("[^a-zA-Z0-9-.'() ]", "").startsWith(serieAMettreAJour.titreSerieFormatRepertoire))
			{
				logger.debug("serie- serieweb" + lineEp);
			}
			else
			{
				arrayListEpisode.remove(lineEp);
			}
		}

		logger.debug("serie- serieweb arrayListEpisode" + arrayListEpisode);

		Map<String, Episode> _ep = new HashMap<String, Episode>(0);
		int numSeqPrec = 0;
		for (String lineEp : arrayListEpisode)
		{
			// {n}#{s}#{e}#{absolute}#{airdate}#{t}#
			// public Episode(String repertoire, String serie, String titre, int
			// numeroEpisode, int numeroSaison, int numeroSeq,TypeDeSerie
			// typeSerie)
			String[] exLineEp = (lineEp + "0#").split("[/#]");
			// System.out.println(lineEp + "." + exLineEp.length);
			if (isNumeric(exLineEp[1]) && isNumeric(exLineEp[2]))
			{
				if (!isNumeric(exLineEp[3]))
				{
					exLineEp[3] = String.valueOf(numSeqPrec + 1);
				}
				numSeqPrec = Integer.valueOf(exLineEp[3]);
				Episode newEp = null;
				/* exLineEp[0] , */
				Date airDate;
				if(exLineEp[4].compareTo("") != 0){
					airDate=(new SimpleDateFormat("yyyy-MM-dd")).parse(exLineEp[4]);
				}
				else
				{
					airDate = (new SimpleDateFormat("dd/mm/yyyy")).parse("31/12/2099");
				}
				newEp = new Episode(serieAMettreAJour.repertoire, serieAMettreAJour.titreSerieFormatRepertoire, getTitreNettoyer(exLineEp[5]), Integer.valueOf(exLineEp[2]),
									Integer.valueOf(exLineEp[1]), Integer.valueOf(exLineEp[3]),airDate);
				_ep.put(newEp.getCritereUnicite(), newEp);
			}
		}

		return _ep;

	}

	/**
	 * incorpore la liste d'episodes en entree dans la liste d'episodes maitre
	 * 
	 */
	private static Map<String, Episode> mergeListEpisodes(Map<String, Episode> listeEpisodesACompleter, Map<String, Episode> listeEpisodes)
	throws ParseException
	{
		/**
		 * boucle sur les episode -les tri les tableau d'episodes sont fait en
		 * interne -merge des episode present dans listacomplete -ajout des
		 * autre
		 */

		HashMap<String, Episode> ret = new HashMap<String, Episode>(0);

		if (listeEpisodesACompleter != null && listeEpisodes == null)
		{
			return listeEpisodesACompleter;
		}
		if (listeEpisodesACompleter == null && listeEpisodes != null)
		{
			return listeEpisodes;
		}
		if (listeEpisodesACompleter == null && listeEpisodes == null)
		{
			return ret;
		}

		ret.putAll(listeEpisodesACompleter);
		for (String _st : listeEpisodes.keySet())
		{
			if (ret.get(_st) != null)
			{
				Episode _ep = ret.get(_st);
				_ep.mergeAvecEpisode(listeEpisodes.get(_st));
				ret.put(_st, _ep);
			}
			else
			{
				ret.put(_st, listeEpisodes.get(_st));
			}
		}
		return ret;

	}

	private static String getTitreNettoyer(String titre)
	{
		String ret = titre.replaceAll("[^a-zA-Z0-9-.'() ]", "");
		return ret;
	}

	/**
	 * -compare la liste en entree avec les fichier attendue , les fichier non
	 * attendue sont transferer vers getRepertoireTmpAranger specifique a la
	 * serie -supression des fichiers non video et non soustire des videos et
	 * non xml serie -purge des repertoire vide du repertoire de la serie
	 * -recalcul des status -sauvegarde xml
	 * 
	 * @throws IOException
	 * @throws JSchException
	 */
	public ArrayList<String> MiseAJourEpisodesRepertoire(ArrayList<String> listeWithPathFichierRepertoire) throws JSchException, IOException, ParseException,
	XmlRpcException, InterruptedException
	{

		ArrayList<String> listeNouveauFichier = new ArrayList<String>(0);
		if (Main.P.topMAJSerieAvecRepertoire)
		{

			ArrayList<String> FIXEListeWithPathFichierRepertoire = (ArrayList<String>) listeWithPathFichierRepertoire.clone();
			// Display.affichageListe("listeFichierVideoRepertoire",
			// FIXEListeWithPathFichierRepertoire);

			if (Main.P.topMAJSerieAvecRepertoireSupressionFichierHorsSeries)
			{
				Map<String, ArrayList<String>> listeVideo = filtreFichierVideo(FIXEListeWithPathFichierRepertoire, repertoireBase);
				ArrayList<String> listeWithPathFichierAPurger = new ArrayList<String>(0);
				listeWithPathFichierAPurger.addAll(listeVideo.get("listeVideoRepertoireAutre"));
				listeWithPathFichierAPurger.removeAll(liste_stARemove_sans_extensions(listeWithPathFichierAPurger,listeVideo.get("listeVideoRepertoire"))); // *exclusion
				// des
				// soustitres
				listeWithPathFichierAPurger.remove(this.fichierXml.cheminComplet);// *exclusion
				// du
				// fichier
				// xml
				if (listeWithPathFichierAPurger.size() > 0)
				{
					/**
					 * supression des fichier non video et non soustire des
					 * video et non xml serie
					 */
					Fichier.purgeListeFichier(listeWithPathFichierAPurger, EnumTypeDeFichier.part.extensions[0]);
					FIXEListeWithPathFichierRepertoire.removeAll(listeWithPathFichierAPurger);
				}
			}

			/**
			 * liste des video non ranger ou a supprimer
			 */
			Map<String, ArrayList<String>> listeVideo = filtreFichierVideo(FIXEListeWithPathFichierRepertoire, repertoireBase);
			ArrayList<String> listeVideoNonAttendue = new ArrayList<String>(0);
			listeVideoNonAttendue.addAll(listeVideo.get("listeVideoRepertoire"/*
														 * RelativeFileNameWithoutExt
														 * "
														 */));
			listeVideoNonAttendue.removeAll(liste_stARemove_sans_extensions(listeVideoNonAttendue, listeFichierVideoAttendu())); // *exclusion
			//listeVideoNonAttendue.removeAll(listeFichierVideoAttendu());

			/**
			 * rangement des videos
			 */
			listeVideoNonAttendue.removeAll(recupererListeVideoNonAttendue(listeVideoNonAttendue));
			
			if (listeVideoNonAttendue.size() > 0 && Main.P.topMAJSerieAvecRepertoireRenomerEtRangerSiNecessaireLesEpisodes)
			{
				/**
				 * display de listeFichierVideoAttendu()
				 */
				logger.debug("--==################################################################################==--");
				for (String _st : listeFichierVideoAttendu())
				{
					logger.debug("listeFichierVideoAttendu =" + _st);
				}
				logger.debug("----------------------------------------------------------------------------------------");
				for (String _st : listeVideoNonAttendue)
				{
					logger.debug("listeVideoNonAttendue =" + _st);
				}
				logger.debug("--==################################################################################==--");


				/**
				 * rangement de fichier n'etant pas attentu par _la bibliotheque
				 */

				if (Main.P.getHoraireFilebot() && Main.P.topMAJSerieAvecRepertoireRenomerEtRangerSiNecessaireLesEpisodes)
				{
					String dest;
					for (String _st : listeVideoNonAttendue)
					{
						dest = _st.replace(Fichier.formatPath(repertoire), Fichier.formatPath(getRepertoireTmpArangerSpecifiqueSerie()));
						Repertoire.creeRepertoire(Fichier.getPathParent(dest));
						Ssh.executeAction("mv \"" + Fichier.formatPath(_st) + "\" \"" + Fichier.formatPath(dest) + "\"");
//						Fichier.copyFile(new File(_st),new File(dest));
						FIXEListeWithPathFichierRepertoire.remove(_st);
						listeNouveauFichier.add(dest);
					}
				} 
				// }

			}

			/**
			 * purge des repertoire vide
			 */
			purgeEmptyRepertoire(repertoire, new String [] {Main.P.NomRepertoireTmpARanger,Main.P.NomRepertoireCorbeille});

			/**
			 * recalculer les status
			 */
			ArrayList<String> lr = FIXEListeWithPathFichierRepertoire;
			ArrayList<String> le = listeFichierVideoAttendu();
			//ArrayList<String> leabsent = new ArrayList<String>(0);
			//leabsent.addAll(le);
			//leabsent.removeAll(lr);
			// Display.ecrireFichierTrace("episodeManquant", "leabsent",
			// leabsent);
			ArrayList<String> lepresent = new ArrayList<String>(0);
			lepresent.addAll(le);
			lepresent.retainAll(lr);
			// Display.ecrireFichierTrace("episodeManquant", "lepresent",
			// lepresent);
			for (Episode _ep : listeEpisodes.values())
			{
				if (lepresent.contains(formatPath(_ep.cheminComplet)))
				{
					if (!_ep.isStatus(EnumStatusEpisode.present))
					{
						logger.debug(_ep.toString() + " => present");
					}
					_ep.setAPresent(_ep.cheminComplet);
				} else {
					_ep.setAAbsent();
				}
			}

			this.sauvegardeXmlSerie();

		}

		return listeNouveauFichier;
	}

	/**
	 * @param listeVideoNonAttendue
	 */
	private ArrayList<String> recupererListeVideoNonAttendue(ArrayList<String> listeVideoNonAttendue) {
		ArrayList<String> listeVideoNonAttendueRecuperer = new ArrayList<String>(0);
		for (String _st : listeVideoNonAttendue)
		{
			Map<String, String> ret = Episode.decomposerNomFichier(_st, new String[] { this.titreSerieFormatRepertoire});
			Episode _ep = recupererEpisode(ret.get("serie"), ret.get("saison"), ret.get("episode"), ret.get("sequentiel"));
			if (_ep != null) {
				_ep.setAPresent(_st);
				listeVideoNonAttendueRecuperer.add(_st);
				logger.debug("recuperation =" + _st);
				logger.debug("recuperation =" + _ep.toString());
			}

			if (ret.containsKey("episodebis")) {
				Episode _epbis = recupererEpisode(ret.get("serie"), ret.get("saison"), ret.get("episodebis"), ret.get("sequentiel"));
				if (_epbis != null) {
					_epbis.setAPresent(_st);
					_epbis.transformerEnEpisodeDouble(Integer.valueOf(ret.get("episode")));
					_ep.transformerEnEpisodeDouble(Integer.valueOf(ret.get("episodebis")));
					_ep.setAPresent(_st);
					listeVideoNonAttendueRecuperer.add(_st);
					logger.debug("recuperation =" + _st);
					logger.debug("recuperation =" + _ep.toString());
				}
			}
		}
		return listeVideoNonAttendueRecuperer;
	}
	
	/**
	 * @param listeAAnalyser
	 * @param listeARemove TODO
	 * @return
	 */
	private ArrayList<String> liste_stARemove_sans_extensions(ArrayList<String> listeAAnalyser, ArrayList<String> listeARemove) {
		/**
		 * Remove en comparant sans les extension
		 */
		ArrayList<String> liste_stARemove = new ArrayList<String>(0);
		for (String _st : listeAAnalyser)
		{
			for (String _stARemove : listeARemove)
			{
				if (Fichier.getFilePartNameWithoutExt(_st).contains(Fichier.getFilePartNameWithoutExt(_stARemove)))
				{
					liste_stARemove.add(_st);
				}
			}
		}
		return liste_stARemove;
	}

	private boolean oldMiseAJour()
	{
		if (this.getLastMajRepertoireDate() == null)
		{
			return true;
		}
		Calendar dateDeControle = Calendar.getInstance();
		dateDeControle.setTime(this.getLastMajRepertoireDate());
		dateDeControle.add(Calendar.MONTH, 1);
		logger.debug("lastMajRepertoireDate" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(this.getLastMajRepertoireDate()));
		logger.debug("dateDeControle" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(dateDeControle.getTime()));
		logger.debug("Calendar.getInstance()" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		return Calendar.getInstance().after(dateDeControle);
	}

	public Episode recupererEpisode(String serie, String numsaison, String numepisode, String numsequentiel) {
		return listeEpisodes.get(Episode.calculCritereUnicite(serie, retint(numsaison), retint(numepisode), retint(numsequentiel)));

	}
	public Episode recupererEpisode(String numsaison, String numepisode, String numsequentiel)
	{
		return listeEpisodes.get(Episode.calculCritereUnicite(this.titreSerieFormatRepertoire, retint(numsaison), retint(numepisode), retint(numsequentiel)));
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

	/**
	 * en fonction de la liste en entre renvoie 3 arraylist Type.video (sans les
	 * aExclure ) + chemin complet => listeVideoRepertoire +
	 * RelativeFileNameWithoutExt =>
	 * listeVideoRepertoireRelativeFileNameWithoutExt les autres fichier =>
	 * listeVideoRepertoireAutre
	 * 
	 * @param listFichier
	 * @return arraylist de 3 array listeVideoRepertoire
	 *         listeVideoRepertoireRelativeFileNameWithoutExt
	 *         listeVideoRepertoireAutre
	 */
	public static Map<String, ArrayList<String>> filtreFichierVideo(ArrayList<String> listFichier, String repBase)
	{
		// Boolean withpath = false;
		Map<String, ArrayList<String>> ret = new HashMap<String, ArrayList<String>>(0);

		ArrayList<String> listeVideoRepertoire = new ArrayList<String>(0);
		ArrayList<String> listeVideoRepertoireAutre = new ArrayList<String>(0);
		ArrayList<String> listeVideoRepertoireRelativeFileNameWithoutExt = new ArrayList<String>(0);
		// try
		// {
		// ArrayList<String> listFichier =
		// Repertoire.searchAllFiles(this.repertoire, this.repertoirreAccesible,
		// 99, false);
		if (listFichier.size() > 0)
		{
			String repeB = Repertoire.formatPath(repBase);
			for (String fichier : listFichier)
			{
				String fic = Repertoire.formatPath(fichier);
				// fic.replace(repeB, "");
				String ext = Fichier.getFileExtension(fic);
				if (EnumTypeDeFichier.video.EstDuTypeDeFichier(ext))
				{
					if (!aExclure(fic))
					{
						// if (withpath) {
						listeVideoRepertoire.add(Repertoire.formatPath(fic));
						// } else {
						listeVideoRepertoireRelativeFileNameWithoutExt.add(Fichier.getRelativeFileNameWithoutExt(fic, repeB));
						// }
					}
					else
					{
						listeVideoRepertoireAutre.add(Repertoire.formatPath(fic));
					}
				}
				else
				{
					listeVideoRepertoireAutre.add(Repertoire.formatPath(fic));
				}

			}
		}
		// }
		// catch (IOException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// catch (JSchException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		ret.put("listeVideoRepertoire", listeVideoRepertoire);
		ret.put("listeVideoRepertoireRelativeFileNameWithoutExt", listeVideoRepertoireRelativeFileNameWithoutExt);
		ret.put("listeVideoRepertoireAutre", listeVideoRepertoireAutre);
		return ret;
	}

	private ArrayList<String> listeFichierVideoAttendu()
	{
		ArrayList<String> ret = new ArrayList<String>(0);
		/* String repeB = Repertoire.formatPath(repertoireBase); */
		for (Episode _ep : listeEpisodes.values())
		{
			// Display.affichageLigne(_ep.cheminComplet);
			// if
			// (EnumTypeDeFichier.video.EstDuTypeDeFichier(Fichier.getFileExtension(_ep.cheminComplet)))
			// {
			ret.add(Repertoire.formatPath(_ep.cheminComplet)/*
					 * ,
					 * repeB
					 * )
					 */);
			// }
		}
		return ret;
	}

	private static boolean aExclure(String fichier)
	{
		if (fichier.toLowerCase().contains("sample"))
		{
			return true;
		}
		return false;
	}

	static boolean isNumeric(String text)
	{
		return text.matches("[-+]?\\d+(\\.\\d+)?");
	}

	/**
	 * format le nom du repertoire contenant les episodes
	 * 
	 */
	public static String nomRepertoireSerie(String repertoire, String titreSerie)
	{
		// TODO: Implement this method
		String nomRepertoire = repertoire + Serie.getTitreNettoyer(titreSerie);
		if (nomRepertoire.endsWith("."))
		{
			nomRepertoire = nomRepertoire.substring(0, nomRepertoire.length() - 1);
		}
		return nomRepertoire + File.separator;
	}

	private String Miseenforme(ArrayList<ArrayList<String>> visu)
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

	public String getRepertoireTmpArangerSpecifiqueSerie()
	{
		return this.getRepertoireBase() + Main.P.NomRepertoireTmpARanger + File.separator + getTypeSerie().getNom() + File.separator + this.titreSerieFormatRepertoire;
	}

	public EnumTypeDeSerie getTypeSerie()
	{
		return typeSerie;
	}

	public boolean isFeezer()
	{
		return fichierXml.freeze;
	}
	public void Setfeezer(Boolean freezeStatus)
	{
		fichierXml.freeze = freezeStatus;
	}

}
