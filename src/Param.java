import java.io.*;
import java.nio.channels.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import org.apache.log4j.*;
import org.apache.log4j.varia.*;

import java.util.Date;

public class Param 
{

	public static String CheminTemporaire;

	public static String Urlkickassusearch;

	public static Logger logger;

	public static String workRepertoire;

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

	private static Calendar calendareDuJour;

	public static int nbtelechargementseriesimultaner;

	public static String gestdownhttp;
	public static String gestdownusername;
	public static String gestdownpassword;

	private static String dburl;
	private static String dbuser;
	private static String dbpasswd;
	private static String dbbase;
	public static Connection con;

	private static boolean debug;


	public Param() 
	{
	}

	public static void ChargerParametrage() throws FileNotFoundException, IOException, SQLException, ParseException
	{
		workRepertoire = currentPath("/mnt/storage/AppProjects/GestionnaireDownload/");

		Properties props = new Properties(); 
		FileInputStream in = null;
		in = new FileInputStream(workRepertoire + File.separator + "projet.properties"); 
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

		nbtelechargementseriesimultaner = Integer.parseInt(props.getProperty("nbtelechargementseriesimultaner"));
		CheminTemporaire = props.getProperty("CheminTemporaire");
		Urlkickassusearch = props.getProperty("Urlkickassusearch");

		calendareDuJour = Calendar.getInstance();
		dateDuJour = calendareDuJour.getTime();
		dateLowValue = (new SimpleDateFormat("dd/mm/yyyy")).parse("01/01/0001");
		dateHighValue = (new SimpleDateFormat("dd/mm/yyyy")).parse("31/12/2099");

		Calendar usaCal = calendareDuJour;
		usaCal.add(Calendar.HOUR_OF_DAY, -49);
		dateDuJourUsa = usaCal.getTime();

		if (debug)
		{
			System.out.println("pas de db2 en debug");
			System.out.println(workRepertoire);
		}
		else
		{
			con = DriverManager.getConnection(dburl + dbbase, dbuser, dbpasswd);
		}
		/**
		 * init_alisation fichier trace
		 */
		initialisationTrace();

		// capture stdout et stderr to log4j
		tieSystemOutAndErrToLog();



	}

	public static String currentPath(String defaultPath)
	{
		String workRepertoire = System.getProperty(("user.dir")) + File.separator;
		workRepertoire = workRepertoire.replace(File.separator + File.separator, File.separator);
		if (workRepertoire.compareTo(File.separator) == 0)
		{
			workRepertoire = workRepertoire + defaultPath;
		}
		return workRepertoire;
	}


	private static void initialisationTrace() throws IOException
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
			Thread.sleep(1000);
		}
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
		if (copyFile(f, new File(workRepertoire + "Log" + File.separator + (new SimpleDateFormat("yyyyMMdd")).format(dateDuJour).toString() + "_" + f.getName()), true))
		{
			Thread.sleep(1000);
			copyFile(f, new File(workRepertoire + File.separator + "last_log_" + f.getName()), false);
			Thread.sleep(1000);
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
		System.out.println("eToString(Exception e)");
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static boolean copyFile(File source, File dest, Boolean append) throws FileNotFoundException, IOException
	{
		FileChannel in = null; // canal d'entr�e
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


}
