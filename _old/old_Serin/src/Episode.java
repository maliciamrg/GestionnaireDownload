import java.text.*;
import java.util.*;

import com.thoughtworks.xstream.io.path.*;

import java.io.*;
import java.net.URLEncoder;
import java.util.regex.*;

import org.apache.log4j.Logger;
import org.json.*;

import com.mysql.jdbc.*;

/**
 ** Defini les caracteristique d'un episodes d'une serie ou d'un anime et les
 * 
 **** actions possible sur l'episode (ajout supression nomage presence) *
 * 
 * * @author Romain CLEMENT peut etre soit : present , absent , avenir peut etre
 * : encours oui/non
 **/
public class Episode extends Fichier implements Comparable<Episode>
{
	private final static Logger logger = Logger.getLogger(Episode.class);

	private String titreSerie;
	private String titreEpisode;
	private Integer numeroSaison;
	private Integer numeroEpisode;
	private Integer numeroSeq;
	private Integer numeroEpisodeDouble;
	private Date airDate;
	private String critereUnicite;

	private EnumStatusEpisode statusActuel = EnumStatusEpisode.avenir;
	private ArrayList<String> historiqueStatus = new ArrayList<String>(0);
	private ArrayList<String> arrayHashExclu = new ArrayList<String>(0);

	public Episode()
	{
	}

	public Episode(String repertoire, String serieNom, String titre, int numeroEpisode, int numeroSaison, int numeroSeq, Date inAirDate)
	throws ParseException
	{
		super(Episode.nomRepertoireSaison(repertoire, numeroSaison), Episode.nomEpisode(serieNom, titre, numeroEpisode, 0, numeroSaison, numeroSeq),
			  EnumTypeDeFichier.video);
		if (inAirDate == null)
		{
			inAirDate = (new SimpleDateFormat("dd/mm/yyyy")).parse("31/12/2099");
		}
		this.titreSerie = serieNom;
		this.titreEpisode = titre;
		this.numeroEpisode = numeroEpisode;
		this.numeroEpisodeDouble = 0;
		this.numeroSaison = numeroSaison;
		this.numeroSeq = numeroSeq;
		this.critereUnicite = Episode.calculCritereUnicite(serieNom, numeroSaison, numeroEpisode, numeroSeq);
		this.airDate = inAirDate;
	}

	public void setAAbsent()
	{
		setStatusActuel(EnumStatusEpisode.absent);
	}

	public void setAEncours()
	{
		setStatusActuel(EnumStatusEpisode.encours);
	}

	public void setAPresent(String pathVideoPresent)
	{
		setStatusActuel(EnumStatusEpisode.present);
		this.cheminComplet = pathVideoPresent;		
	}

	public boolean arrayHashExclucontains(String hashCode)
	{
		return arrayHashExclu.contains(hashCode);
	}

	public void arrayHashExcluadd(String hashCode)
	{
		arrayHashExclu.add(hashCode);
	}

	public void arrayHashExcluclear()
	{
		arrayHashExclu.clear();
	}

	public Date getAirDate() throws ParseException
	{
		if (airDate == null)
		{
			airDate = (new SimpleDateFormat("dd/mm/yy")).parse("01/01/01");
		}

		return airDate;
	}

	/**
	 * 
	 * @return the titreSerie
	 */
	public String getTitreSerie()
	{
		return titreSerie;
	}

	/**
	 * @return the titre
	 */
	public String getTitre()
	{
		return titreEpisode;
	}

	private void setStatusActuel(EnumStatusEpisode statusEp)
	{

		if (statusEp.compareTo(EnumStatusEpisode.absent) == 0)
		{
			if (Main.P.dateDuJourUsa.before(airDate))// || (new
			{
				statusEp = EnumStatusEpisode.avenir;
			}
		}

		if (this.statusActuel == null)
		{
			this.statusActuel = statusEp;
		}
		if ((this.statusActuel.getNumOrdre() < statusEp.getNumOrdre()))
		{
			this.statusActuel = statusEp;
			if (statusEp.compareTo(EnumStatusEpisode.absent) != 0)
			{
				String dt = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime());
				historiqueStatus.add(dt + " => " + statusEp.toString());
			}
		}
	}

	public Integer getNumeroSaison()
	{
		return numeroSaison;
	}

	public Integer getNumeroEpisode()
	{
		return numeroEpisode;
	}

	public Integer getNumeroSeq()
	{
		return numeroSeq;
	}

	public Integer getNumeroEpisodeDouble()
	{
		return numeroEpisodeDouble;
	}

	public String getCritereUnicite()
	{
		return critereUnicite;
	}

	public String getStatusString()
	{
		return statusActuel.toString();
	}

	public boolean isStatus(EnumStatusEpisode status)
	{
		return (status.compareTo(this.statusActuel) == 0);
	}

	public boolean equals(Object obj)
	{
		return critereUnicite.compareToIgnoreCase(((Episode) obj).critereUnicite) == 0;
	}

	/**
	 * fournit une methode de comparation entre 2 episode afin de les trier
	 */
	public int compareTo(Episode episodeCompare)
	{
		return critereUnicite.compareTo(episodeCompare.critereUnicite);
	}

	public static Comparator<Episode> airDateComparator = new Comparator<Episode>() {
		public int compare(Episode ep1, Episode ep2)
		{
			if (ep1.airDate == null)
			{
				return -1;
			}
			if (ep2.airDate == null)
			{
				return 1;
			}

			return ep2.airDate.compareTo(ep1.airDate);
		}
	};

	public static Comparator<Episode> airDateComparatorPlusAncienDabord = new Comparator<Episode>() {
		public int compare(Episode ep1, Episode ep2)
		{
			if (ep2.airDate == null)
			{
				return -1;
			}
			if (ep1.airDate == null)
			{
				return 1;
			}

			return ep1.airDate.compareTo(ep2.airDate);
		}
	};

	/**
	 * combine le titre le numero d'episode afin de formater le nom du fichier
	 * 
	 */
	private static String nomEpisode(String serie, String titre, int numeroEpisode, int numeroEpisodeDouble, int numeroSaison, int numeroseq)
	{
		String ret;
		if (numeroEpisodeDouble != 0)
		{
			ret = serie + " " + "S" + String.format("%1$02d", numeroSaison) + "E" + String.format("%1$02d", numeroEpisode) + " " + "E"
				+ String.format("%1$02d", numeroEpisodeDouble) + " " + titre;
		}
		else
		{
			ret = serie + " " + "S" + String.format("%1$02d", numeroSaison) + "E" + String.format("%1$02d", numeroEpisode) + " " + " " + titre;
		}
		return ret.replaceAll("[^a-zA-Z0-9 -.]", "").toLowerCase().trim();
	}

	/*
	 * format le nom du repertoire contenant les episodes
	 */
	private static String nomRepertoireSaison(String repertoire, int numeroSaison)
	{
		return repertoire + "Saison " + String.format("%1$02d", numeroSaison) + File.separator;
	}

	/**
	 * met a jours les donnée de lepisode a completer avec les données de
	 * lepisode
	 * 
	 */
	public void mergeAvecEpisode(Episode episode) throws ParseException
	{
		this.airDate = episode.getAirDate();
		this.titreEpisode = episode.titreEpisode;
		this.numeroEpisode = episode.getNumeroEpisode();
		this.numeroSaison = episode.getNumeroSaison();
		this.numeroSeq = episode.getNumeroSeq();
	}

	@Override
	public String toString()
	{
		return this.critereUnicite
			+ " - "
			+ String.format("%1$03d", this.numeroSeq)
			+ " - "
			+ this.getNom()
			+ " + " + (new SimpleDateFormat("dd-MM-yyyy")).format(this.airDate);
	}


	public void transformerEnEpisodeDouble(int numeroDouble)
	{
		this.numeroEpisodeDouble = numeroDouble;
	}

	/**
	 * calcul le critiere unique a chaque episode
	 * 
	 * @return chaine de caractere unique a chaque episodes serie(sans espace) &
	 *         "@" & numeroseq(sur 5 positions)
	 */
	public static String calculCritereUnicite(String serie, int numeroSaison, int numeroEpisode, int numeroSeq)
	{
		String partSeq = "";
		if (numeroSaison != 0)
		{
			partSeq = String.format("%1$05d", numeroSeq);
		}
		if (numeroSeq == 0 && numeroSaison != 0)
		{
			partSeq = String.format("%1$02d", numeroSaison) + String.format("%1$03d", numeroEpisode);
		}
		if (numeroSeq == 0 && numeroSaison == 0)
		{
			partSeq = "XX" + String.format("%1$03d", numeroEpisode);
		}
		return serie.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim() + "@" + partSeq;
	}


	/**
	 * serie/saison/episode/episodesbis/sequentiel analyse le nom de fichier
	 * afin d'extraire la seriies , le numero de la saison et le numero
	 * d'episode String nomSerie=ret[0]; String numeroSaison = ret[1]; String
	 * numeroEpisode = ret[2];
	 */
	public static Map<String, String> decomposerNomFichier(String fileName, String[] seriesName)
	{
		logger.debug("episode-" + "decomposerNom " + fileName);
		fileName = Fichier.getFilePartName(fileName);
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
			logger.debug("episode-" + "decomposerNom 4-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
						 + numeroSequentielTrouve.toString());
		}

		if (m3.find())
		{
			partname = namecmp.substring(0, m3.start(0));
			numeroSaisonTrouve.put("saison", m3.group(1).toString());
			numeroEpisodeTrouve.put("episode", m3.group(2).toString());
			logger.debug("episode-" + "decomposerNom 3-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
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
			logger.debug("episode-" + "decomposerNom 2-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
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
			logger.debug("episode-" + "decomposerNom 5-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
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
				logger.debug("episode-" + "decomposerNom 6-" + partname + " " + numeroSaisonTrouve.toString() + " " + numeroEpisodeTrouve.toString() + " "
							 + numeroSequentielTrouve.toString());
			}
			if (m1.find())
			{
				if (m1.group(5).toString().compareTo("") != 0)
				{
					if (Serie.isNumeric(m1.group(4).toString()) && Serie.isNumeric(m1.group(5).toString()))
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
							logger.debug("episode-" + "decomposerNom 1-" + partname + " " + numeroSaisonTrouve.toString() + " "
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
			int i = 0;
			for (i = 0; i < seriesName.length; i++)
			{
				ctrlnom = true;
				String textSerieNettoyer = seriesName[i].replaceAll("[(]([0-9a-zA-Z]*)[)]", "");
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
					ret.put("serie", seriesName[i]);
					logger.debug("episode- decomposerNom" + seriesName[i]);
				}

			}

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
