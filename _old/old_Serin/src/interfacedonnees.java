import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.*;

public class interfacedonnees
{
	public interfacedonnees() throws SQLException
	{
		createTorrent();
		createEpisodes();
		createSerie();
		createTraceTransmislson();
		
	}

	public static void addSerie(String titre, String typeSerie, Date dateDuJour, String biblothequeJson, String status)
	{
		// TODO: Implement this method
	}

	public static Torrents gettorrent(String hash)
	{
		// TODO: Implement this method
		return null;
	}

	public static ArrayList<Episode> getepisodesavechashencours()
	{
		// TODO: Implement this method
		return null;
	}

	private void createTorrent() throws SQLException
	{

		Statement stmt = Param.con.createStatement();

		String sql = "CREATE TABLE IF NOT EXISTS torrent                                         "
			+ "(hash                    VARCHAR(255)  not NULL              ,                "
			+ " magnet                  VARCHAR(255)                        ,                "
			+ " nom                     VARCHAR(255)                        ,                "
			+ " score                   INTEGER                             ,                "
			+ " nbEpisodeRechercher     INTEGER                             ,                "
			+ " status                  SET ('Ok','exclu','')            ,                "
			+ " serie                  VARCHAR(255)                        ,                "
			+ " PRIMARY KEY ( hash )                                                )                "
			+ "                                                                                    ";		
		stmt.executeUpdate(sql);
		stmt.close();
	}
	private void createEpisodes() throws SQLException
	{

		Statement stmt = Param.con.createStatement();

		String sql = "CREATE TABLE IF NOT EXISTS episodes                                         "
			+ "(critereunicite          VARCHAR(255)  not NULL              ,                "
			+ " cheminreel              VARCHAR(255)                        ,                "
			+ " nom                     VARCHAR(255)                        ,                "
			+ " serie                   VARCHAR(255)                              ,                "
			+ " numsaison               INTEGER                             ,                "
			+ " numepisode              INTEGER                ,                "
			+ " numsequentiel           INTEGER                        ,                "
			+ " numdouble               INTEGER                 ,                "
			+ " airdate                 DATE                                          ,    "
			+ " status                  SET ('avenir','absent','present','encours')   ,    "
			+ " hashencours             VARCHAR(255)                                  ,    "
			+ " PRIMARY KEY ( critereunicite )                                        ,    "
			+ "         KEY  ( hashencours )                                                )                "
			+ "                                                                                    ";		
		stmt.executeUpdate(sql);
		stmt.close();
	}
	
	private void createSerie() throws SQLException
	{

		Statement stmt = Param.con.createStatement();

		String sql = "CREATE TABLE IF NOT EXISTS series                                         "
			+ "(serie                    VARCHAR(255)  not NULL              ,                "
			+ " type                 SET ('serie','anime')                       ,                "
			+ " datededernieremajweb               DATE                 ,                "
			+ " repertoire                 VARCHAR(255)                       ,                "
			+ " status                  SET ('encours','terminée','off')               ,                "
			+ " PRIMARY KEY ( serie )                                                )                "
			+ "                                                                                    ";		
		stmt.executeUpdate(sql);
		stmt.close();
	}

	private void createTraceTransmislson() throws SQLException
	{

		Statement stmt = Param.con.createStatement();

		String sql = "CREATE TABLE IF NOT EXISTS tracetransmisson                                         "
			+ "(datetrace                    date  not NULL              ,                "
			+ " hash                  VARCHAR(255) not null                       ,                "
			+ " trace                     VARCHAR(255)                        ,                "
			+ " PRIMARY KEY ( datetrace )                 ,                                     "
			+ "         KEY  ( hash )                                                )                "
			+ "                                                                                    ";		
		stmt.executeUpdate(sql);
		stmt.close();
	}

	public static boolean isHashGererAutomatique(String hash)
	{
		// TODO: Implement this method
		return false;
	}

	public static String tbTraceTransmisionAdd(String dtj, String idATracer, String actionATracer)
	{
		// TODO: Implement this method
		return null;
	}

}
