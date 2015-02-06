import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.log4j.*;
import org.apache.log4j.varia.*;

import java.util.Date;
import java.sql.*;

public class Param extends Fichier
{


	public String gestdownhttp="home.daisy-street.fr:9091/transmission/rpc";
	public String gestdownusername="admin";
	public String gestdownpassword="admin";
	public int gestdownnbPlaceMax=7;

	public boolean topForceExecutionMalgresZeroplaces = true;
	public boolean topForceExecutionMalgresLeFichieLock = true;

	public boolean topChargementEpisodesXml = false;

	public boolean topConnectTransmission = false;

	public boolean topMAJSerie = true;
	public boolean topMAJSerieViaWeb = false;
	public boolean topMAJSerieAvecRepertoire = false;
	public boolean topMAJSerieAvecRepertoireSupressionFichierHorsSeries = false;
	public boolean topMAJSerieAvecRepertoireRenomerEtRangerSiNecessaireLesEpisodes = false;
	public boolean topMAJSerieAvecRepertoireRenomerEtRangerSiNecessaireLesEpisodesSupressionParasite = false;

	public boolean topRechercherTorrents = false;
	public boolean topRechercherTorrentsAjoutDesTorrentsATransmission = false;
	public boolean topSupressionTorrentUtiliser = false;

	public int horrareFilebotDebut = 1;
	public int horrareFilebotFin = 5;

	public String SerieForcer = "";
	public String PerCentCpu = "15";
	public String NomRepertoireTmpARanger = "TmpARanger";
	public String NomRepertoireCorbeille = "Corbeille";
	public String CheminCorbeille = "/mnt/HD/HD_a2/VideoClub/";
	
	public String repertoireXml = "\\fichierFonctionelle\\xmlSeries";
	public String repertoireLog = "\\Log";
	public String repertoireSuivi = "\\suivi";
	public static String repertoireParam = "\\parametres";

	public String Urlkickassusearch = "http://kickass.to/usearch/";
	private interfacedonnees inter;

	public static Logger logger = Logger.getRootLogger();

	public static Boolean horrareFilebot = true;

	public static String workRepertoireParam;
	public static String workRepertoireXml ;
	public static String workRepertoireLog ;
	public static String workRepertoireSuivi ;
	
	public static String workRepertoire;
	public static String idUnique;
	public static String log4j_cheminComplet_error;
	public static String log4j_cheminComplet_debug;
	public static String log4j_cheminComplet_debugtransmisson;
	public static String log4j_cheminComplet_info;
	public static String log4j_cheminComplet_warn;
	public static FileAppender faMaster;
	public static FileAppender faDebug;
	public static FileAppender faDebugtrans;
	public static FileAppender faWarn;
	public static FileAppender faInfo;

	public static Date dateLowValue;
	public static Date dateHighValue;
	public static Date dateDuJour;
	public static Date dateDuJourUsa;

	private static String filname;

	private static String filrep;

	private static String filext;

	private static Calendar calendareDuJour;

	private static String dburl;
	private static String dbuser;
	private static String dbpasswd;
	private static String dbbase;
	public static Connection con;


	public Param() throws IOException, SQLException
	{
		super(filrep, filname, filext);
		if (isPresenceOnDrive())
		{
			try
			{
				unserialiseXML();
			}
			catch (FileNotFoundException e)
			{
			}
		}
		sauvegarder();
		
		
		
		Properties props = new Properties(); 
		FileInputStream in = null;
		in = new FileInputStream(workRepertoire+file.separator+ "database.properties"); 
		props.load(in);
		dburl = props.getProperty("db.url"); 
		dbuser = props.getProperty("db.user"); 
		dbpasswd = props.getProperty("db.passwd");
		dbbase = props.getProperty("db.base");
		con = DriverManager.getConnection(dburl+dbbase, dbuser, dbpasswd);
		inter = new interfacedonnees();
		
		//** calcul parametre
		int hNow = (Calendar.getInstance()).get(Calendar.HOUR_OF_DAY);
		int horrareFilebotFinMod = (horrareFilebotDebut > horrareFilebotFin) ? (24)  : (horrareFilebotFin) ;
		int horrareFilebotDebutMod = (horrareFilebotFin < horrareFilebotDebut) ? (0)  : (horrareFilebotDebut) ;
		horrareFilebot = ((hNow >= horrareFilebotDebutMod && hNow <= horrareFilebotFin ) || (hNow >= horrareFilebotDebut && hNow <= horrareFilebotFinMod ) );
		Param.workRepertoireLog=Param.workRepertoire+repertoireLog;
		Param.workRepertoireSuivi=Param.workRepertoire+repertoireSuivi;
		Param.workRepertoireXml=Param.workRepertoire+repertoireXml;
		
		Calendar usaCal = calendareDuJour;
		usaCal.add(Calendar.HOUR_OF_DAY, -49);
		Main.P.dateDuJourUsa = usaCal.getTime();
		
		/**
		 * init_alisation fichier trace
		 */
		initialisationTrace();

	}


	public Boolean getHoraireFilebot()
	{
		return horrareFilebot;
	}

	public static void initialiserParam() throws ParseException
	{
		if (filrep == null)
		{
			Param.idUnique = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(Calendar.getInstance().getTime());
			Param.workRepertoire = Repertoire.currentPath("/mnt/storage/AppProjects/SerieDownload/");
			Param.workRepertoireParam=Param.workRepertoire+Param.repertoireParam;
			Param.filrep = Repertoire.formatPath(Param.workRepertoireParam);
			Param.filname = "param";
			Param.filext = EnumTypeDeFichier.xml.extensions[0];
			Param.calendareDuJour= Calendar.getInstance();
			Param.dateDuJour= calendareDuJour.getTime();
			Param.dateLowValue= (new SimpleDateFormat("dd/mm/yyyy")).parse("01/01/0001");
			Param.dateHighValue= (new SimpleDateFormat("dd/mm/yyyy")).parse("31/12/2099");
			
		}
	}

	private void initialisationTrace() throws IOException
	{
		log4j_cheminComplet_error = Repertoire.formatPath(Fichier.constituerCheminComplet(workRepertoire, "log4j_error", EnumTypeDeFichier.html.extensions[0]));
		log4j_cheminComplet_debug = Repertoire.formatPath(Fichier.constituerCheminComplet(workRepertoire, "log4j_debug", EnumTypeDeFichier.html.extensions[0]));
		log4j_cheminComplet_debugtransmisson = Repertoire.formatPath(Fichier.constituerCheminComplet(workRepertoire, "log4j_debugtransmisson",
																									 EnumTypeDeFichier.html.extensions[0]));
		log4j_cheminComplet_info = Repertoire.formatPath(Fichier.constituerCheminComplet(workRepertoire, "log4j_info", EnumTypeDeFichier.html.extensions[0]));
		log4j_cheminComplet_warn = Repertoire.formatPath(Fichier.constituerCheminComplet(workRepertoire, "log4j_warn", EnumTypeDeFichier.html.extensions[0]));

		LevelMatchFilter filterDebugOut = new LevelMatchFilter();
		filterDebugOut.setLevelToMatch("DEBUG");
		filterDebugOut.setAcceptOnMatch(false);
		LevelMatchFilter filterDebugIn = new LevelMatchFilter();
		filterDebugIn.setLevelToMatch("DEBUG");
		filterDebugIn.setAcceptOnMatch(true);
		LevelMatchFilter filterWarn = new LevelMatchFilter();
		filterWarn.setLevelToMatch("WARN");
		filterWarn.setAcceptOnMatch(true);
		LevelMatchFilter filterInfo = new LevelMatchFilter();
		filterInfo.setLevelToMatch("INFO");
		filterInfo.setAcceptOnMatch(true);
		StringMatchFilter filterReadOut = new StringMatchFilter();
		filterReadOut.setStringToMatch("Read:");
		filterReadOut.setAcceptOnMatch(false);
		StringMatchFilter filterReadIn = new StringMatchFilter();
		filterReadIn.setStringToMatch("Read:");
		filterReadIn.setAcceptOnMatch(true);
		StringMatchFilter filterWroteOut = new StringMatchFilter();
		filterWroteOut.setStringToMatch("Wrote:");
		filterWroteOut.setAcceptOnMatch(false);
		StringMatchFilter filterWroteIn = new StringMatchFilter();
		filterWroteIn.setStringToMatch("Wrote:");
		filterWroteIn.setAcceptOnMatch(true);
		DenyAllFilter denyAllFilter = new DenyAllFilter();

		faMaster = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), log4j_cheminComplet_error);
		faDebug = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), log4j_cheminComplet_debug);
		faDebugtrans = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), log4j_cheminComplet_debugtransmisson);
		faWarn = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), log4j_cheminComplet_warn);
		faInfo = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), log4j_cheminComplet_info);

		faMaster.setThreshold((Level) Level.ERROR);
		faMaster.activateOptions();
		logger.addAppender(faMaster);
		faDebug.addFilter(filterReadOut);
		faDebug.addFilter(filterWroteOut);
		faDebug.addFilter(filterDebugIn);
		faDebug.addFilter(denyAllFilter);
		faDebug.activateOptions();
		logger.addAppender(faDebug);

		faDebugtrans.addFilter(filterReadIn);
		faDebugtrans.addFilter(filterWroteIn);
		faDebugtrans.addFilter(denyAllFilter);
		faDebugtrans.activateOptions();
		logger.addAppender(faDebugtrans);

		faWarn.addFilter(filterWarn);
		faWarn.addFilter(denyAllFilter);
		faWarn.activateOptions();
		logger.addAppender(faWarn);

		faInfo.addFilter(filterInfo);
		faInfo.addFilter(denyAllFilter);
		faInfo.activateOptions();
		logger.addAppender(faInfo);
	}
	
	public static void clotureTrace() throws FileNotFoundException, IOException, InterruptedException
	{
		Thread.sleep(500);
		faMaster.close();
		Thread.sleep(500);
		faDebug.close();
		Thread.sleep(500);
		faDebugtrans.close();
		Thread.sleep(500);
		faWarn.close();
		Thread.sleep(500);
		faInfo.close();
		Thread.sleep(500);
		logger.removeAllAppenders();
		Thread.sleep(1000);
		archiveLog(log4j_cheminComplet_info);
		archiveLog(log4j_cheminComplet_debug);
		archiveLog(log4j_cheminComplet_debugtransmisson);
		archiveLog(log4j_cheminComplet_warn);
		archiveLog(log4j_cheminComplet_error);

	}

	private static void archiveLog(String archive) throws FileNotFoundException, IOException, InterruptedException
	{
		File f = new File(archive);
		if (Fichier.copyFile(f, new File(Repertoire.formatPath(workRepertoireLog + File.separator + idUnique.substring(0, 8) + "_" + f.getName())), true))
		{
			Thread.sleep(1000);
			Fichier.copyFile(f, new File(Repertoire.formatPath(workRepertoire + File.separator + "last_log_" + f.getName())), false);
			Thread.sleep(1000);
			f.delete();
		}
	}

	

	public void sauvegarder() throws IOException
	{
		serialiseXML(this);
	}
	public static void tieSystemOutAndErrToLog() 
	{ 
		System.setOut(createLoggingProxy(System.out)); 
		System.setErr(createLoggingProxyErr(System.err)); 
	} 
	public static PrintStream createLoggingProxy(final PrintStream realPrintStream) 
	{ 
		return new PrintStream(realPrintStream) 
		{ 
			public void print(final String string) 
			{ 
				realPrintStream.print(string); 
				logger.debug(string); 
				logger.info(string); 
				logger.warn(string); 
				logger.error(string); 
			} 
		}; 
	} 
	public static PrintStream createLoggingProxyErr(final PrintStream realPrintStream) 
	{ 
		return new PrintStream(realPrintStream) 
		{ 
			public void print(final String string) 
			{ 
				realPrintStream.print(string); 
				logger.error(string); 
			} 
		}; 
	} 
	
	public static String eToString(Exception e)
	{
		System.out.println("eToString(Exception e)");
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	
}
