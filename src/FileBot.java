/*
 * Copyright (c) 2015 by Malicia All rights reserved.
 * 
 * 26 mars 2015
 * 
 * 
 */
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.jcraft.jsch.JSchException;


// TODO: Auto-generated Javadoc
/**
 * The Class FileBot.
 */
public class FileBot
{

	/**
	 * Rangerfilm.
	 *
	 * @param pathdesfilmaranger the pathdesfilmaranger
	 * @param pathdelabibliothequesdesfilm the pathdelabibliothequesdesfilm
	 * @throws JSchException the j sch exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void rangerfilm(String pathdesfilmaranger, String pathdelabibliothequesdesfilm) throws JSchException, IOException, InterruptedException
	{
		Ssh.executeAction("rm /mnt/HD/HD_a2/ffp/opt/share/filebot/data/history.xml");
		Ssh.executeAction(Param.filebotlaunchechaine + " -clear-cache ");
		Ssh.executeAction(Param.filebotlaunchechaine + " -script fn:amc --output  \"" + pathdelabibliothequesdesfilm 
						  + "\" --log-file amc.log --action move "
						  + "\"" + pathdesfilmaranger + "\" -non-strict " 
						  + "--def \"movieFormat=" + pathdelabibliothequesdesfilm + "/{n.replaceAll(/[:]/,'')} ({y})/{n.replaceAll(/[:]/,'')} ({y}, {director}) {vf} {af}\" "
						  + "--def subtitles=en,fr --def ut_label=movie --def clean=y --def artwork=y ");
		/*		Ssh.executeAction(Param.filebotlaunchechaine + " -rename \"" + pathdesfilmaranger
		 + "\" --db TheMovieDB --lang en --conflict override --encoding=UTF-8 --format "
		 + "\"" + pathdelabibliothequesdesfilm + "/{n.replaceAll(/[:]/,\"\")} ({y})/{n.replaceAll(/[:]/,\"\")} ({y}, {director}) {vf} {af}\""
		 + " -r -non-strict ");*/
	}

	/**
	 * Rangerserie.
	 *
	 * @param pathdelaseriearanger the pathdelaseriearanger
	 * @param pathdelabibliothequesdelaserie the pathdelabibliothequesdelaserie
	 * @throws JSchException the j sch exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws SQLException 
	 */
	public static void rangerserie(String pathdelaseriearanger, String pathdelabibliothequesdelaserie, String pathdelabibliothequesdesfilm) throws JSchException, IOException, InterruptedException, SQLException
	{
		//String pathdelaseriearangercorriger = pathdelaseriearanger.substring(0, pathdelaseriearanger.substring(0, pathdelaseriearanger.substring(0, pathdelaseriearanger.length() - 1).lastIndexOf("/")).lastIndexOf("/")) + "/" ;
		//String pathdelaseriearangercorriger = (pathdelaseriearanger.substring(0, pathdelaseriearanger.substring(0, pathdelaseriearanger.length()-1).lastIndexOf("/")))+"/" ;
		Ssh.executeAction("rm /mnt/HD/HD_a2/ffp/opt/share/filebot/data/history.xml");
		Ssh.executeAction(Param.filebotlaunchechaine + " -clear-cache ");
		ArrayList<String> ret = Ssh.executeAction(Param.filebotlaunchechaine + " -script fn:amc --output  \"" + pathdelabibliothequesdelaserie
												  + "\" --log-file amc.log --action "/*move " */+ "test "
												  + "\"" + pathdelaseriearanger + "\" -non-strict " 
												  + "--def \"animeFormat=" + pathdelabibliothequesdelaserie + "/{n}/Saison {s.pad(2)}/{n} S{s.pad(2)}E{es*.pad(2).join('-E')} ep_{absolute*.pad(3).join('_')} {t}\" "
												  + "\"seriesFormat=" + pathdelabibliothequesdelaserie + "/{n}/Saison {s.pad(2)}/{n} S{s.pad(2)}E{es*.pad(2).join('-E')} ep_{absolute*.pad(3).join('_')} {t}\" "
												  + "\"movieFormat=" + pathdelabibliothequesdesfilm + "/{n.replaceAll(/[:]/,'')} ({y})/{n.replaceAll(/[:]/,'')} ({y}, {director}) {vf} {af}\" "
												  + /*"--def subtitles=en,fr ut_label=tv*/ " --def clean=y --def artwork=y  --conflict override");
		/**
		 * nettoyage tableau retour
		 */
//		ArrayList<String> arrayListEpisode = new ArrayList<String>(0);
//		ArrayList<String> ret = new ArrayList<String>(0);
//		ret.add("Auto-detected query: [Detective Conan]");
//		ret.add("[TEST] Rename [/media/videoclub/unloading_dock/tmp/serie/Detective Conan Season 9/Detective Conan - 246-247 [DCTP][5F7ECB81].avi] to [/media/videoclub/unloading_dock/tmp/serie/Detective Conan/Saison 09/Detective Conan S09E27 ep_246 The Mystery in the Net (Part One).avi]");
//		ret.add("[TEST] Rename [/media/videoclub/unloading_dock/tmp/serie/Detective Conan Season 9/Detective Conan - 248 [AZFS][526F3797].avi] to [/media/videoclub/unloading_dock/tmp/serie/Detective Conan/Saison 09/Detective Conan S09E29 ep_248 The Alibi of the Soothing Forest.avi]");
//		ret.add("Processed 13 files");
		for (String lineEp : ret)
		{
			if (lineEp.startsWith("[TEST] Rename"))
			{
				String[] spl = lineEp.substring(15, lineEp.length() - 1).split("\\] to \\[");
				String destmod=correctiondestination(spl[0], spl[1], pathdelabibliothequesdelaserie, pathdelabibliothequesdesfilm);
				Ssh.moveFile(spl[0], destmod);
				Param.logger.debug("deplacement:" + spl[0]);
				Param.logger.debug("vers:" + destmod);
			}
		}
		/*		Ssh.executeAction(Param.filebotlaunchechaine + " -rename \"" + pathdelaseriearanger
		 + "\" --db TheTVDB --lang en --conflict override --encoding=UTF-8 --format "
		 + "\"" + pathdelabibliothequesdelaserie + "/{n}/Saison {s.pad(2)}/{n} {s00e00} ep_{absolute.pad(3)} {t}\""
		 + " -r -non-strict ");*/		
	}

	/**
	 * correction des serie avec algorythme perso
	 * les episode detective conan avec seulement le numero absolu on tendance 
	 * a etre renomer par filebot comme si la saisn̂ et le numero depisode et&at colle 
	 * ex : ep 705 = S7E05
	 *
	 * @param pathdelabibliothequesdelaserie
	 * @param lineEp
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws JSchException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private static String correctiondestination(String src , String dest, String pathdelabibliothequesdelaserie, String pathdelabibliothequesdesfilm)throws SQLException, InterruptedException, JSchException,IOException, NumberFormatException
	{
		if (src.indexOf(pathdelabibliothequesdesfilm) > 0)
		{
			return dest;
		}
		String code0 = "";
		String code1 = "";
		Map<String, String> retepisode = Main.conversionnom2episodes(src) ;
		if (!retepisode.get("serie").equals("")	&& !retepisode.get("saison").equals("000") 	&& !retepisode.get("episode").equals("000"))
		{		
			code0 = " Ep:" + retepisode.get("serie") + " " + retepisode.get("saison") + "-" + retepisode.get("episode") + (retepisode.containsKey("episodebis") ?("-" + retepisode.get("episodebis")): ("")) + " " ; 
		}	
		Map<String, String> retepisode2 = Main.conversionnom2episodes(dest) ;
		if (!retepisode2.get("serie").equals("")	&& !retepisode2.get("saison").equals("000") 	&& !retepisode2.get("episode").equals("000"))
		{		
			code1 = " Ep:" + retepisode2.get("serie") + " " + retepisode2.get("saison") + "-" + retepisode2.get("episode") + (retepisode2.containsKey("episodebis") ?("-" + retepisode2.get("episodebis")): ("")) + " " ; 
		}	
		if (code0.equals(code1) || code0.equals(""))
		{
			return dest;
		}
		else
		{
			String newname;
			if (retepisode.containsKey("sequentielbis"))
			{
				newname = pathdelabibliothequesdelaserie + "/" + retepisode.get("serie") 
					+ "/Saison " + String.format("%02d", Integer.parseInt(retepisode.get("saison"))) 
					+ "/" + retepisode.get("serie") 
					+ " S" + String.format("%02d", Integer.parseInt(retepisode.get("saison"))) 
					+ "E" + String.format("%02d", Integer.parseInt(retepisode.get("episode"))) 
					+ "-E" + String.format("%02d", Integer.parseInt(retepisode.get("episodebis"))) 
					+ " ep_" + String.format("%03d", Integer.parseInt(retepisode.get("sequentiel"))) 
					+ "_" + String.format("%03d", Integer.parseInt(retepisode.get("sequentielbis"))) + " "
					+ retepisode2.get("partiedroite");
			}
			else
			{
				newname = pathdelabibliothequesdelaserie + "/" + retepisode.get("serie") 
					+ "/Saison " + String.format("%02d", Integer.parseInt(retepisode.get("saison"))) 
					+ "/" + retepisode.get("serie") 
					+ " S" + String.format("%02d", Integer.parseInt(retepisode.get("saison"))) 
					+ "E" + String.format("%02d", Integer.parseInt(retepisode.get("episode"))) 
					+ " ep_" + String.format("%03d", Integer.parseInt(retepisode.get("sequentiel"))) + " "
					+ retepisode2.get("partiedroite");
			}
			return newname.trim();
		}
	}

	/**
	 * Gets the subtitleserie.
	 *
	 * @param pathdelabibliothequesdelaserie the pathdelabibliothequesdelaserie
	 * @throws JSchException the j sch exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void getsubtitleserie(String pathdelabibliothequesdelaserie) throws JSchException, IOException, InterruptedException
	{
		//Ssh.executeAction(Param.filebotlaunchechaine + " -get-missing-subtitles \"" + pathdelabibliothequesdelaserie
		//		+ "\" --lang en,fr --output srt --encoding utf8 -r -non-strict ");
	}

	/**
	 * Maj_liste_episodes.
	 *
	 * @param serie the serie
	 * @throws NumberFormatException the number format exception
	 * @throws SQLException the SQL exception
	 * @throws ParseException the parse exception
	 * @throws JSchException the j sch exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void maj_liste_episodes(String serie) throws NumberFormatException, SQLException, ParseException, JSchException, IOException, InterruptedException
	{
		ArrayList<String> ret = new ArrayList<String>(0);

		ret = Ssh.executeAction(Param.filebotlaunchechaine + " -list --db TheTVDB --q \"" + serie
								+ "\" --format '{n}#{s}#{e}#{absolute}#{airdate}#{t}#' ");

		/**
		 * nettoyage tableau retour
		 */
		ArrayList<String> arrayListEpisode = new ArrayList<String>(0);
		arrayListEpisode.addAll(ret);
		for (String lineEp : ret)
		{
			if (lineEp.replaceAll("[^a-zA-Z0-9-.'() ]", "").startsWith(serie))
			{
				Param.logger.debug("serie- serieweb" + lineEp);
			}
			else
			{
				arrayListEpisode.remove(lineEp);
			}
		}

		int numSeqPrec = 0;
		for (String lineEp : arrayListEpisode)
		{
			// {n}#{s}#{e}#{absolute}#{airdate}#{t}#
			String[] exLineEp = (lineEp + "0#").split("[/#]");
			// System.out.println(lineEp + "." + exLineEp.length);
			if (Param.isNumeric(exLineEp[1]) && Param.isNumeric(exLineEp[2]) && Integer.parseInt(exLineEp[2]) >0)
			{
				if (!Param.isNumeric(exLineEp[3]))
				{
					exLineEp[3] = String.valueOf(numSeqPrec + 1);
				}
				if (numSeqPrec >= Integer.valueOf(exLineEp[3]))
				{
					exLineEp[3] = String.valueOf(numSeqPrec+1);
				}
				numSeqPrec = Integer.valueOf(exLineEp[3]);

				/* exLineEp[0] , */
				Date airDate;
				if (exLineEp[4].compareTo("") != 0)
				{
					airDate = (new SimpleDateFormat("yyyy-MM-dd")).parse(exLineEp[4]);
				}
				else
				{
					airDate = (new SimpleDateFormat("dd/mm/yyyy")).parse("31/12/2099");
				}


				ResultSet rsFilebot = null;
				Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rsFilebot = stmt.executeQuery("SELECT * "
											  + " FROM episodes "
											  + "WHERE "
											  + " serie = \"" + serie + "\""
											  + " and num_saison = \"" + Integer.valueOf(exLineEp[1]) + "\""
											  + " and num_episodes = \"" + Integer.valueOf(exLineEp[2]) + "\""				 
											  + " ");
				rsFilebot.last();
				String nomnettoyer = exLineEp[5].replaceAll("[^a-zA-Z0-9.-]", "_");
				if (rsFilebot.getRow() == 0)
				{	
					stmt.executeUpdate("INSERT INTO episodes "
									   + " ( nom , airdate , serie , num_saison , num_episodes , sequentiel) VALUES " 
									   + " ("
									   + " \"" + nomnettoyer + "\" ,"
									   + " \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(airDate) + "\" ,"
									   + " \"" + serie + "\" ,"
									   + " \"" + Integer.valueOf(exLineEp[1]) + "\" ,"
									   + " \"" + Integer.valueOf(exLineEp[2]) + "\" ,"		
									   + " \"" + Integer.valueOf(exLineEp[3]) + "\" "	
									   + " )");
				}
				else
				{

					rsFilebot.updateString("nom", nomnettoyer);
					rsFilebot.updateString("airdate",  (new SimpleDateFormat("yyyy-MM-dd")).format(airDate));
					rsFilebot.updateInt("sequentiel",  Integer.valueOf(exLineEp[3]));
					rsFilebot.updateRow();
				}
				rsFilebot.close();

			}
		}
		
		mettredatemajserieserie(serie);

	}
	
	/**
	 * Mettredatemajserieserie.
	 *
	 * @param serie the serie
	 * @throws SQLException the SQL exception
	 */
	private static void mettredatemajserieserie(String serie) throws SQLException
	{
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("UPDATE series "
						   + " set date_maj_web = \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(Param.dateDuJour()) + "\""
						   + "WHERE "
						   + " nom = \"" + serie + "\""		
						   + " ");
	}

}
