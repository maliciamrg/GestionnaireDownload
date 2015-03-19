import java.io.*;
import java.nio.channels.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import org.apache.log4j.*;
import org.apache.log4j.varia.*;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.Date;

import ca.benow.transmission.*;

import java.net.*;

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
	public static String Fileseparator = "/";
	
	private static String CheminTemporaire;

	public static String Urlkickassusearch;

	public static Logger logger;

	public static String workRepertoire;
	public static String RepertoireFilm;

	private static String log4j_cheminComplet_error;
	private static String log4j_cheminComplet_debug;
	private static String log4j_cheminComplet_debugtransmisson;
	private static String log4j_cheminComplet_info;
	private static String log4j_cheminComplet_warn;
	private static FileAppender faMaster;
	private static FileAppender faDebug;
	private static FileAppender faDebugtrans;
	private static FileAppender faWarn;
	private static FileAppender faInfo;

	public static Date dateLowValue;
	public static Date dateHighValue;
	public static Date dateDuJour;
	public static Date dateDuJourUsa;
	public static Date dateJourM1;
	public static Date dateJourM30;
	public static Date dateJourM300;
	public static Date dateJourP7;	

	public static int nbtelechargementseriesimultaner;

	private static String gestdownhttp;
	private static String gestdownusername;
	private static String gestdownpassword;
	public static String minutesdinactivitesautorize;
	public static TransmissionClient client;
	
	private static String dburl;
	private static String dbuser;
	private static String dbpasswd;
	private static String dbbase;
	public static Connection con;
	
	private static String sshhost;
	private static String sshusername;
	private static String sshpassword;
	private static JSch jsch;
	public static Session session;


	public static boolean debug;

	public static String UrlduStreamerInterne;

	public static Boolean WordPressPost;
	public static Object WordPressusername;
	public static Object WordPresspwd;
	public static String WordPressxmlRpcUrl;

	public static String filebotlaunchechaine;

	public Param() 
	{
	}

	public static void ChargerParametrage() throws FileNotFoundException, IOException, SQLException, ParseException, JSchException, InterruptedException
	{
		workRepertoire = currentPath("/mnt/storage/AppProjects/GestionnaireDownload/");

		Properties props = new Properties(); 
		FileInputStream in = null;
		in = new FileInputStream(workRepertoire + Param.Fileseparator + "projet.properties"); 
		props.load(in);

		System.out.println("environement=." + props.getProperty("environement") + ".");

		debug = (props.getProperty("environement").equals("debug")) ?true: false; 

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
		Ssh.actionexecChmodR777(workRepertoire);
		
		// capture stdout et stderr to log4j
		tieSystemOutAndErrToLog();

		ConnectClientTransmission();

	}

	public static void initialiser_dates() throws ParseException
	{
		dateDuJour =  Calendar.getInstance().getTime();
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
	
	public static void cloture() throws FileNotFoundException, IOException, InterruptedException, SQLException
	{
			session.disconnect();
			con.close();
			clotureTrace();
	}
	
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

	private static void printAllAppender()
	{
		Enumeration appenders =  logger.getAllAppenders();
		for (;appenders.hasMoreElements();)
		{
			Appender app=(Appender) appenders.nextElement();
			System.out.println("<" + app.getName() + "-" + app.hashCode() + ">");
		}
	}

	private static void archiveLog(String archive) throws FileNotFoundException, IOException, InterruptedException
	{
		File f = new File(archive);
		if (copyLocalFile(f, new File(workRepertoire + "Log" + Param.Fileseparator + (new SimpleDateFormat("yyyyMMdd")).format(dateDuJour).toString() + "_" + f.getName()), true))
		{
			Thread.sleep(2000);
			copyLocalFile(f, new File(workRepertoire + Param.Fileseparator + "last_log_" + f.getName()), false);
			Thread.sleep(2000);
			f.delete();
		}
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
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

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

	private static String CheminTemporaire() {
		return CheminTemporaire;
	}

	public static String CheminTemporaireTmp() {
		return CheminTemporaire()+"tmp"+Param.Fileseparator;
	}
	
	public static String CheminTemporaireFilm() {
		return CheminTemporaire()+"tmp"+Param.Fileseparator+"film"+Param.Fileseparator;
	}

	public static String CheminTemporaireSerie() {
		return CheminTemporaire()+"tmp"+Param.Fileseparator+"serie"+Param.Fileseparator;
	}
	public static boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  
	public static String GetOs() { 
		String s = "name: " + System.getProperty ("os.name");
		s += ", version: " + System.getProperty ("os.version");
		s += ", arch: " + System.getProperty ("os.arch");
		Param.logger.debug("OS=" + s);
		return s; 
	}
	
}
