import ca.benow.transmission.TransmissionClient;
import ca.benow.transmission.model.TorrentStatus;
import ca.benow.transmission.model.TorrentStatus.TorrentField;
import ca.benow.transmission.*;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import org.json.*;

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
	 *
	 */
	public static void main(String[] args) throws IOException, ParseException, SQLException, InterruptedException, JSONException
	{
		initialisation(args);
		initialisation_bdd(args);
		transmisson(args);
		alimentation_bdd(args);

		cloture(args);
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
	 *
	 */
	private static void transmisson(String[] args) throws JSONException, IOException, SQLException
	{
		ResultSet rs;
	  	List<TorrentStatus> torrents = Param.client.getAllTorrents(new TorrentField[] { TorrentField.hashString });
		for (TorrentStatus curr : torrents)
		{
			String hash = (String) curr.getField(TorrentField.hashString);
			rs = Param.stmt.executeQuery("SELECT * "
										 + " FROM episodes "
										 + " WHERE "
										 + "      hash = " + hash 
										 + "  ");

		}
	}

	public static void initialisation(String[] args) throws IOException, ParseException, SQLException 
	{
		Param.ChargerParametrage();

		System.out.print("<pre>");
		System.out.println("+---+----+----+----+");
		System.out.println("+       Debut      +" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
	}

    /**
     * 
     * 
     * @throws SQLException
     */
	private static void initialisation_bdd(String[] args) throws SQLException
	{
		Param.stmt.executeUpdate("CREATE TABLE IF NOT EXISTS series "
								 + "(nom VARCHAR(255) not NULL , "
								 + " repertoire VARCHAR(255) , "
								 + " date_maj_web DATE , "
								 + " PRIMARY KEY ( nom ) "
								 + ") "
								 + " ");

		Param.stmt.executeUpdate("CREATE TABLE IF NOT EXISTS episodes "
								 + "(serie VARCHAR(255) not NULL , "
								 + " num_saison INTEGER  , "
								 + " num_episodes INTEGER  , "
								 + " nom  VARCHAR(255) , "
								 + " airdate DATE , "
								 + " encours BOOLEAN , "					
								 + " timestamp_completer DATE , "
								 + " chemin_complet  VARCHAR(255) , "
								 + " PRIMARY KEY ( serie , num_saison , num_episodes  ) , "
								 + "         KEY ( airdate ) "
								 + ") "
								 + " ");

		Param.stmt.executeUpdate("CREATE TABLE IF NOT EXISTS hash "
								 + "(hash VARCHAR(255) not NULL , "
								 + " classification  VARCHAR(255) , "
								 + " magnet  VARCHAR(255) , "
								 + " timestamp_ajout DATE , "
								 + " timestamp_termine DATE , "
								 + " PRIMARY KEY ( hash ) "
								 + "         KEY ( timestamp_termine ) "
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
     */
	private static void alimentation_bdd(String[] args) throws SQLException, JSONException, IOException
	{

		FichierQR.AjouterNouvelleSerie();

		Param.stmt.executeUpdate("update episodes set encours = false ");


		ResultSet rs ;
		rs = Param.stmt.executeQuery("SELECT series.nom , max(episodes.airdate) as MaxDate"
									 + " FROM series "
									 + " INNER join episodes "
									 + "    on series.nom = episodes.serie "
									 + " WHERE "
									 + "      series.date_maj_web > " + Param.dateJourM30 
									 + " GROUP BY "
									 + "  series.nom");
		while (rs.next())
		{
			if ((rs.getDate("MaxDate")).after(Param.dateJourM300))
			{ 
				FileBot.maj_liste_episodes(rs.getString("nom"));
			}
			else
			{
				FichierQR.ForcerMajSerieWeb(rs.getString("nom"));
			}
		}
		rs.close();

	  	List<TorrentStatus> torrents = Param.client.getAllTorrents(new TorrentField[] { TorrentField.hashString });
		for (TorrentStatus curr : torrents)
		{
			String hash = (String) curr.getField(TorrentField.hashString);
			rs = Param.stmt.executeQuery("SELECT count(*) as NbHash "
										 + " FROM episodes "
										 + " WHERE "
										 + "      hash = " + hash 
										 + "  ");
			if ((rs.getInt("NbHash")) == 0)
			{
				Param.stmt.executeUpdate("insert into episodes hash( " 
										 + " " + hash + " , "
										 + " 'autres' , "
										 + " '' , "
										 + " " + Param.dateDuJour + " , "
										 + " '' "
										 + " ) ");
			}
		}

	}


	private static void cloture(String[] args) throws InterruptedException, IOException, SQLException
	{
		Param.cloture();

		System.out.println("+        Fin       +" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		System.out.println("+---+----+----+----+");
		System.out.print("</pre>");
	}



}
