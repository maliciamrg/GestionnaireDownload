import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.jcraft.jsch.JSchException;





import ca.benow.transmission.model.TorrentStatus.TorrentField;

public class FileBot
{

	public static void rangerfilm(String pathdesfilmaranger, String pathdelabibliothequesdesfilm) throws JSchException, IOException, InterruptedException
	{
		Ssh.executeAction("rm /mnt/HD/HD_a2/ffp/opt/share/filebot/data/history.xml");
		Ssh.executeAction(Param.filebotlaunchechaine + " -clear-cache ");
		Ssh.executeAction(Param.filebotlaunchechaine + " -script fn:amc --output  \"" + pathdelabibliothequesdesfilm 
				+ "\" --log-file amc.log --action move "
				+ "\"" + pathdesfilmaranger + "\" -non-strict " 
				+ "--def \"movieFormat="+pathdelabibliothequesdesfilm+"/{n.replaceAll(/[:]/,'')} ({y})/{n.replaceAll(/[:]/,'')} ({y}, {director}) {vf} {af}\" "
				+ "--def ut_label=movie --def clean=y --def artwork=y ");
/*		Ssh.executeAction(Param.filebotlaunchechaine + " -rename \"" + pathdesfilmaranger
				+ "\" --db TheMovieDB --lang en --conflict override --encoding=UTF-8 --format "
				+ "\"" + pathdelabibliothequesdesfilm + "/{n.replaceAll(/[:]/,\"\")} ({y})/{n.replaceAll(/[:]/,\"\")} ({y}, {director}) {vf} {af}\""
				+ " -r -non-strict ");*/
	}

	public static void rangerserie(String pathdelaseriearanger, String pathdelabibliothequesdelaserie) throws JSchException, IOException, InterruptedException
	{
		Ssh.executeAction("rm /mnt/HD/HD_a2/ffp/opt/share/filebot/data/history.xml");
		Ssh.executeAction(Param.filebotlaunchechaine + " -clear-cache ");
		Ssh.executeAction(Param.filebotlaunchechaine + " -script fn:amc --output  \"" + pathdelabibliothequesdelaserie
				+ "\" --log-file amc.log --action move "
				+ "\"" + pathdelaseriearanger + " -non-strict " 
				+ "--def \"animeFormat="+pathdelabibliothequesdelaserie+"/{n}/Saison {s.pad(2)}/{n} {s00e00} ep_{absote.pad(3)} {t}\" "
				+ "\"seriesFormat="+pathdelabibliothequesdelaserie+"/{n}/Saison {s.pad(2)}/{n} {s00e00} ep_{absolute.pad(3)} {t}\" "
				+ "--def ut_label=tv --def clean=y --def artwork=y ");
/*		Ssh.executeAction(Param.filebotlaunchechaine + " -rename \"" + pathdelaseriearanger
				+ "\" --db TheTVDB --lang en --conflict override --encoding=UTF-8 --format "
				+ "\"" + pathdelabibliothequesdelaserie + "/{n}/Saison {s.pad(2)}/{n} {s00e00} ep_{absolute.pad(3)} {t}\""
				+ " -r -non-strict ");*/		
	}

	public static void getsubtitleserie( String pathdelabibliothequesdelaserie) throws JSchException, IOException, InterruptedException
	{
		Ssh.executeAction(Param.filebotlaunchechaine + " -get-missing-subtitles \"" + pathdelabibliothequesdelaserie
				+ "\" --lang fr --output srt --encoding utf8 -r -non-strict ");
		Ssh.executeAction(Param.filebotlaunchechaine + " -get-missing-subtitles \"" + pathdelabibliothequesdelaserie
				+ "\" --lang en --output srt --encoding utf8 -r -non-strict ");
	}
	
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
			if (Param.isNumeric(exLineEp[1]) && Param.isNumeric(exLineEp[2]))
			{
				if (!Param.isNumeric(exLineEp[3]))
				{
					exLineEp[3] = String.valueOf(numSeqPrec + 1);
				}
				numSeqPrec = Integer.valueOf(exLineEp[3]);
				
				/* exLineEp[0] , */
				Date airDate;
				if(exLineEp[4].compareTo("") != 0){
					airDate=(new SimpleDateFormat("yyyy-MM-dd")).parse(exLineEp[4]);
				}
				else
				{
					airDate = (new SimpleDateFormat("dd/mm/yyyy")).parse("31/12/2099");
				}
				
			
				ResultSet rsFilebot = null;
				Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
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
							 + " ( nom , airdate , serie , num_saison , num_episodes ) VALUES " 
							 + " ("
							 + " \"" + nomnettoyer + "\" ,"
							 + " \"" + (new SimpleDateFormat("yyyy-MM-dd")).format(airDate) + "\" ,"
							 + " \"" + serie + "\" ,"
							 + " \"" + Integer.valueOf(exLineEp[1]) + "\" ,"
							 + " \"" + Integer.valueOf(exLineEp[2]) + "\" "				 
							 + " )");
				} else {
					
					rsFilebot.updateString("nom",nomnettoyer );
					rsFilebot.updateString("airdate",  (new SimpleDateFormat("yyyy-MM-dd")).format(airDate));
					rsFilebot.updateRow();
				}
				rsFilebot.close();

			}
		}

	}
}
