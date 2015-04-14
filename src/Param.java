/*
 * Copyright (c) 2015 by Malicia All rights reserved.
 * 
 * 26 mars 2015
 * 
 * 
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.varia.DenyAllFilter;
import org.apache.log4j.varia.LevelMatchFilter;
import org.apache.log4j.varia.StringMatchFilter;

import ca.benow.transmission.TransmissionClient;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

// TODO: Auto-generated Javadoc
/**
 * The Class Param.
 */
public class Param 
{
	/*
	 * "projet.properties"
environement=prod
db.url=jdbc:mysql://home.daisy-street.fr:3306/
db.user=seriedownload
db.passwd=seriedownload
db.base=seriedownload
gestdown.http=home.daisy-street.fr:9091/transmission/rpc/
gestdown.username=admin
gestdown.passwordadmin
gestdown.minutesdinactivitesautorize=5
WordPress.addpost=True
WordPress.xmlRpcUrl=http://www.daisy-street.fr/wordpress/homebox/rpc
WordPress.username=seriedownload
WordPress.password=seriedownload
ssh.host=192.168.1.100
ssh.username=david
ssh.password=haller
nbtelechargementseriesimultaner=5
CheminTemporaire=\\media\\videoclub\\unloading_dock\\
RepertoireFilm=
Urlkickassusearch=https://kickass.to/usearch/
UrlduStreamerInterne=
filebotlaunchechaine=filebot 
'filebotlaunchechaine=nice -n 19  "/mnt/HD/HD_a2/ffp/opt/share/filebot/bin/filebot.sh"
	 */
	/** The Fileseparator. */
	public static String Fileseparator = "/";
	
	/** The Chemin temporaire. */
	private static String CheminTemporaire;

	/** The Urlkickassusearch. */
	public static String Urlkickassusearch;

	/** The logger. */
	public static Logger logger;

	/** The work repertoire. */
	public static String workRepertoire;
	
	/** The Repertoire film. */
	public static String RepertoireFilm;

	/** The log4j_chemin complet_error. */
	private static String log4j_cheminComplet_error;
	
	/** The log4j_chemin complet_debug. */
	private static String log4j_cheminComplet_debug;
	
	/** The log4j_chemin complet_debugtransmisson. */
	private static String log4j_cheminComplet_debugtransmisson;
	
	/** The log4j_chemin complet_info. */
	private static String log4j_cheminComplet_info;
	
	/** The log4j_chemin complet_warn. */
	private static String log4j_cheminComplet_warn;
	
	/** The fa master. */
	private static FileAppender faMaster;
	
	/** The fa debug. */
	private static FileAppender faDebug;
	
	/** The fa debugtrans. */
	private static FileAppender faDebugtrans;
	
	/** The fa warn. */
	private static FileAppender faWarn;
	
	/** The fa info. */
	private static FileAppender faInfo;

	/** The date low value. */
	public static Date dateLowValue;
	
	/** The date high value. */
	public static Date dateHighValue;
	
	/** The date du jour usa. */
	public static Date dateDuJourUsa;
	
	/** The date jour m1. */
	public static Date dateJourM1;
	
	/** The date jour m30. */
	public static Date dateJourM30;
	
	/** The date jour m300. */
	public static Date dateJourM300;
	
	/** The date jour p7. */
	public static Date dateJourP7;	

	/** The nbtelechargementseriesimultaner. */
	public static int nbtelechargementseriesimultaner;

	/** The gestdownhttp. */
	private static String gestdownhttp;
	
	/** The gestdownusername. */
	private static String gestdownusername;
	
	/** The gestdownpassword. */
	private static String gestdownpassword;
	
	/** The minutesdinactivitesautorize. */
	public static String minutesdinactivitesautorize;
	
	/** The client. */
	public static TransmissionClient client;
	
	/** The dburl. */
	private static String dburl;
	
	/** The dbuser. */
	private static String dbuser;
	
	/** The dbpasswd. */
	private static String dbpasswd;
	
	/** The dbbase. */
	private static String dbbase;
	
	/** The con. */
	public static Connection con;
	
	/** The sshhost. */
	private static String sshhost;
	
	/** The sshusername. */
	private static String sshusername;
	
	/** The sshpassword. */
	private static String sshpassword;
	
	/** The jsch. */
	private static JSch jsch;
	
	/** The session. */
	public static Session session;


	/** The debug. */
	public static boolean debug;
	
	/** The analyserrepertoire. */
	public static boolean analyserrepertoire;
	
	/** The Urldu streamer interne. */
	public static String UrlduStreamerInterne;

	/** The Word press post. */
	public static Boolean WordPressPost;
	
	/** The Word pressusername. */
	public static Object WordPressusername;
	
	/** The Word presspwd. */
	public static Object WordPresspwd;
	
	/** The Word pressxml rpc url. */
	public static String WordPressxmlRpcUrl;

	/** The filebotlaunchechaine. */
	public static String filebotlaunchechaine;


	public static String actionrexlexionunique;
	public static String actionrexlexionuniqueparam;
	
	public static Boolean actionrangerdownload;
	
	/**
	 * Instantiates a new param.
	 */
	public Param() 
	{
	}

	/**
	 * Charger parametrage.
	 *
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SQLException the SQL exception
	 * @throws ParseException the parse exception
	 * @throws JSchException the j sch exception
	 * @throws InterruptedException the interrupted exception
	 */
	public static void ChargerParametrage() throws FileNotFoundException, IOException, SQLException, ParseException, JSchException, InterruptedException
	{
		workRepertoire = currentPath("/mnt/storage/AppProjects/GestionnaireDownload/");

		Properties props = new Properties(); 
		FileInputStream in = null;
		in = new FileInputStream(workRepertoire + Param.Fileseparator + "projet.properties"); 
		props.load(in);

		System.out.println("environement=." + props.getProperty("environement") + ".");

		debug = (props.getProperty("environement").equals("debug")) ?true: false; 
		
		analyserrepertoire = (props.getProperty("environement").equals("analyserrepertoire")) ?true: false; 
		actionrangerdownload = Boolean.parseBoolean(props.getProperty("action.rangerdownload")); 
		
		actionrexlexionunique= props.getProperty("action.rexlexionunique"); 
		actionrexlexionuniqueparam= props.getProperty("action.rexlexionuniqueparam"); 
		
		dburl = props.getProperty("db.url"); 
		dbuser = props.getProperty("db.user"); 
		dbpasswd = props.getProperty("db.passwd");
		dbbase = props.getProperty("db.base");

		gestdownhttp = props.getProperty("gestdown.http"); 
		gestdownusername = props.getProperty("gestdown.username"); 
		gestdownpassword = props.getProperty("gestdown.password");
		minutesdinactivitesautorize = props.getProperty("gestdown.minutesdinactivitesautorize");
		
		WordPressPost = Boolean.parseBoolean(props.getProperty("WordPress.addpost")); 
		WordPressxmlRpcUrl = props.getProperty("WordPress.xmlRpcUrl"); 
		WordPressusername = props.getProperty("WordPress.username"); 
		WordPresspwd = props.getProperty("WordPress.password");		

		sshhost = props.getProperty("ssh.host"); 
		sshusername = props.getProperty("ssh.username"); 
		sshpassword = props.getProperty("ssh.password");

		nbtelechargementseriesimultaner = Integer.parseInt(props.getProperty("nbtelechargementseriesimultaner"));
		CheminTemporaire = props.getProperty("CheminTemporaire");
		RepertoireFilm = props.getProperty("RepertoireFilm");
		Urlkickassusearch = props.getProperty("Urlkickassusearch");

		filebotlaunchechaine = props.getProperty("filebotlaunchechaine");
		
		UrlduStreamerInterne  = props.getProperty("UrlduStreamerInterne");
		initialiser_dates();

		if (debug)
		{
			System.out.println("pas de db2 en debug");
			System.out.println(workRepertoire);
		}
		else
		{
			System.out.println(dburl + dbbase);
			con = DriverManager.getConnection(dburl + dbbase+"?useUnicode=true&characterEncoding=utf-8", dbuser, dbpasswd);
		}
		
		jsch = new JSch();
		session = jsch.getSession(sshusername, sshhost, 22);
		session.setPassword(sshpassword);
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		if (!sshhost.equals("")){
			session.connect();
		}
		
		/**
		 * init_alisation fichier trace
		 */
		initialisationTrace();
		//Ssh.actionexecChmodR777(workRepertoire);
		
		// capture stdout et stderr to log4j
		tieSystemOutAndErrToLog();

		ConnectClientTransmission();

	}

	/**
	 * Date du jour.
	 *
	 * @return the date
	 */
	public static Date dateDuJour() 
	{
		return Calendar.getInstance().getTime();
	}
	
	/**
	 * Initialiser_dates.
	 *
	 * @throws ParseException the parse exception
	 */
	public static void initialiser_dates() throws ParseException
	{
		dateLowValue = (new SimpleDateFormat("dd/mm/yyyy")).parse("01/01/0001");
		dateHighValue = (new SimpleDateFormat("dd/mm/yyyy")).parse("31/12/2099");

		Calendar usaCal = Calendar.getInstance();
		usaCal.add(Calendar.HOUR_OF_DAY, -49);
		dateDuJourUsa = usaCal.getTime();

		Calendar JourP7 =  Calendar.getInstance();;
		JourP7.add(Calendar.DAY_OF_YEAR, +7);
		dateJourP7 = JourP7.getTime();	 
		
		Calendar JourM1 =  Calendar.getInstance();;
		JourM1.add(Calendar.DAY_OF_YEAR, -1);
		dateJourM1 = JourM1.getTime();	 

		Calendar JourM30 =  Calendar.getInstance();;
		JourM30.add(Calendar.DAY_OF_YEAR, -30);
		dateJourM30 = JourM30.getTime();	 

		Calendar JourM300 =  Calendar.getInstance();;
		JourM300.add(Calendar.DAY_OF_YEAR, -300);
		dateJourM300  = JourM300.getTime();
	}
		
		
	/**
	 * Current path.
	 *
	 * @param defaultPath the default path
	 * @return the string
	 */
	public static String currentPath(String defaultPath)
	{
		String workRepertoire = System.getProperty(("user.dir")) + Param.Fileseparator;
		workRepertoire = workRepertoire.replace(Param.Fileseparator + Param.Fileseparator, Param.Fileseparator);
		if (workRepertoire.compareTo(Param.Fileseparator) == 0)
		{
			workRepertoire = workRepertoire + defaultPath;
		}
		return workRepertoire;
	}


	/**
	 * Initialisation trace.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JSchException the j sch exception
	 * @throws InterruptedException the interrupted exception
	 */
	private static void initialisationTrace() throws IOException, JSchException, InterruptedException
	{
		
		log4j_cheminComplet_error = workRepertoire + "log4j_error" + ".html";
		log4j_cheminComplet_debug = workRepertoire + "log4j_debug" + ".html";
		log4j_cheminComplet_debugtransmisson = workRepertoire + "log4j_debugtransmisson" + ".html";
		log4j_cheminComplet_info = workRepertoire + "log4j_info" + ".html";
		log4j_cheminComplet_warn = workRepertoire + "log4j_warn" + ".html";

		logger = Logger.getLogger(Main.class.getName());
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
		faMaster.setName("error");
		faDebug = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), log4j_cheminComplet_debug);
		faDebug.setName("debug");
		faDebugtrans = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), log4j_cheminComplet_debugtransmisson);
		faDebugtrans.setName("debugtrans");
		faWarn = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), log4j_cheminComplet_warn);
		faWarn.setName("warn");
		faInfo = new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), log4j_cheminComplet_info);
		faInfo.setName("info");

		faMaster.setThreshold(Level.ERROR);
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
	
	/**
	 * Cloture.
	 *
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws SQLException the SQL exception
	 */
	public static void cloture() throws FileNotFoundException, IOException, InterruptedException, SQLException
	{
			session.disconnect();
			con.close();
			clotureTrace();
	}
	
	/**
	 * Cloture trace.
	 *
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void clotureTrace() throws FileNotFoundException, IOException, InterruptedException
	{
		if (debug)
		{
			printAllAppender();
		}
		else
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
		}
		Thread.sleep(5000);
		archiveLog(log4j_cheminComplet_info);
		archiveLog(log4j_cheminComplet_debug);
		archiveLog(log4j_cheminComplet_debugtransmisson);
		archiveLog(log4j_cheminComplet_warn);
		archiveLog(log4j_cheminComplet_error);


	}

	/**
	 * Prints the all appender.
	 */
	private static void printAllAppender()
	{
		Enumeration appenders =  logger.getAllAppenders();
		for (;appenders.hasMoreElements();)
		{
			Appender app=(Appender) appenders.nextElement();
			System.out.println("<" + app.getName() + "-" + app.hashCode() + ">");
		}
	}

	/**
	 * Archive log.
	 *
	 * @param archive the archive
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	private static void archiveLog(String archive) throws FileNotFoundException, IOException, InterruptedException
	{
		File f = new File(archive);
		if (copyLocalFile(f, new File(workRepertoire + "Log" + Param.Fileseparator + (new SimpleDateFormat("yyyyMMdd")).format(dateDuJour()).toString() + "_" + f.getName()), true))
		{
			Thread.sleep(2000);
			copyLocalFile(f, new File(workRepertoire + Param.Fileseparator + "last_log_" + f.getName()), false);
			Thread.sleep(2000);
			f.delete();
		}
	}


	/**
	 * Tie system out and err to log.
	 */
	public static void tieSystemOutAndErrToLog() 
	{ 
		System.setOut(createLoggingProxy(System.out)); 
		System.setErr(createLoggingProxyErr(System.err)); 
	} 
	
	/**
	 * Creates the logging proxy.
	 *
	 * @param realPrintStream the real print stream
	 * @return the prints the stream
	 */
	public static PrintStream createLoggingProxy(final PrintStream realPrintStream) 
	{ 
		return new PrintStream(realPrintStream) 
		{ 
			@Override
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
	
	/**
	 * Creates the logging proxy err.
	 *
	 * @param realPrintStream the real print stream
	 * @return the prints the stream
	 */
	public static PrintStream createLoggingProxyErr(final PrintStream realPrintStream) 
	{ 
		return new PrintStream(realPrintStream) 
		{ 
			@Override
			public void print(final String string) 
			{ 
				realPrintStream.print(string); 
				logger.error(string); 
			} 
		}; 
	} 

	/**
	 * E to string.
	 *
	 * @param e the e
	 * @return the string
	 */
	public static String eToString(Exception e)
	{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	/**
	 * Gets the file part name.
	 *
	 * @param fileName the file name
	 * @return the file part name
	 */
	public static String getFilePartName(String fileName)
	{
		int pos;
		pos = fileName.lastIndexOf("/");
		if (pos > 0)
		{
			fileName = fileName.substring(pos + 1);
		}
		pos = fileName.lastIndexOf("\\");
		if (pos > 0)
		{
			fileName = fileName.substring(pos + 1);
		}
		return fileName;
	}
	
	/**
	 * Gets the last path.
	 *
	 * @param fileName the file name
	 * @return the last path
	 */
	public static String getlastPath(String fileName)
	{
		int pos;
		pos = fileName.lastIndexOf("/");
		if (pos > 0)
		{
			fileName = fileName.substring(0,pos );
		}
		pos = fileName.lastIndexOf("\\");
		if (pos > 0)
		{
			fileName = fileName.substring(0,pos );
		}
		return fileName;
	}
	
	/**
	 * Copy local file.
	 *
	 * @param source the source
	 * @param dest the dest
	 * @param append the append
	 * @return true, if successful
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean copyLocalFile(File source, File dest, Boolean append) throws FileNotFoundException, IOException
	{
		FileChannel in = null; // canal d'entr?e
		FileChannel out = null; // canal de sortie

		if (!source.exists())
		{
			return false;
		}

		// Init
		in = new FileInputStream(source).getChannel();
		out = new FileOutputStream(dest, append).getChannel();

		// Copie depuis le in vers le out
		in.transferTo(0, in.size(), out);

		in.close();
		out.close();
		return true;
	}

	/**
	 * Connect client transmission.
	 *
	 * @throws MalformedURLException the malformed url exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JSchException the j sch exception
	 * @throws InterruptedException the interrupted exception
	 */
	public static void ConnectClientTransmission() throws MalformedURLException, IOException, JSchException, InterruptedException 
	{
			/**
			 * connect avec transmission
			 */

			int i = 0;
			for (i = 1; i < 4; i++)
			{
				boolean isError = true;

				URL url = new URL("http://" +  gestdownusername + ":" + gestdownpassword + "@" +  gestdownhttp + "");

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

	/**
	 * Chemin temporaire.
	 *
	 * @return the string
	 */
	private static String CheminTemporaire() {
		return CheminTemporaire;
	}

	/**
	 * Chemin temporaire tmp.
	 *
	 * @return the string
	 */
	public static String CheminTemporaireTmp() {
		return CheminTemporaire();//+"tmp"+Param.Fileseparator;
	}
	
	/**
	 * Chemin temporaire film.
	 *
	 * @return the string
	 */
	public static String CheminTemporaireFilm() {
		return CheminTemporaire()+"tmp"+Param.Fileseparator+"film"+Param.Fileseparator;
	}

	/**
	 * Chemin temporaire serie.
	 *
	 * @return the string
	 */
	public static String CheminTemporaireSerie() {
		return CheminTemporaire();//+"tmp"+Param.Fileseparator+"serie"+Param.Fileseparator;
	}
	
	/**
	 * Checks if is numeric.
	 *
	 * @param s the s
	 * @return true, if is numeric
	 */
	public static boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  
	
	/**
	 * Gets the os.
	 *
	 * @return the string
	 */
	public static String GetOs() { 
		String s = "name: " + System.getProperty ("os.name");
		s += ", version: " + System.getProperty ("os.version");
		s += ", arch: " + System.getProperty ("os.arch");
		Param.logger.debug("OS=" + s);
		return s; 
	}
	
}
