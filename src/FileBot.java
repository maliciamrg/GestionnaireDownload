import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ca.benow.transmission.model.TorrentStatus.TorrentField;

public class FileBot
{

	public static void rangerfilm(String pathdesfilmaranger, String pathdelabibliothequesdesfilm)
	{
		Ssh.executeAction("rm /mnt/HD/HD_a2/ffp/opt/share/filebot/data/history.xml");
		Ssh.executeAction("nice -n 19 \"/mnt/HD/HD_a2/ffp/opt/share/filebot/bin/filebot.sh\" -clear-cache ");
		Ssh.executeAction(
				"nice -n 19 \"/mnt/HD/HD_a2/ffp/opt/share/filebot/bin/filebot.sh\" -rename \"" + pathdesfilmaranger
				+ "\" --db TheTVDB --lang en --conflict override --encoding=UTF-8 --format "
				+ "\"" + pathdelabibliothequesdesfilm + "/{n.replaceAll(/[:]/,\"\")} ({y})/{n.replaceAll(/[:]/,\"\")} ({y}, {director}) {vf} {af}\""
				+ " -r -non-strict ");
	}

	public static void rangerserie(String pathdelaseriearanger, String pathdelabibliothequesdelaserie)
	{
		Ssh.executeAction("rm /mnt/HD/HD_a2/ffp/opt/share/filebot/data/history.xml");
		Ssh.executeAction("nice -n 19 \"/mnt/HD/HD_a2/ffp/opt/share/filebot/bin/filebot.sh\" -clear-cache ");
		Ssh.executeAction(
				"nice -n 19 \"/mnt/HD/HD_a2/ffp/opt/share/filebot/bin/filebot.sh\" -rename \"" + pathdelaseriearanger
				+ "\" --db TheTVDB --lang en --conflict override --encoding=UTF-8 --format "
				+ "\"" + pathdelabibliothequesdelaserie + "/{n}/Saison {s.pad(2)}/{n} {s00e00} ep_{absolute.pad(3)} {t}\""
				+ " -r -non-strict ");
	}


	public static void maj_liste_episodes(String serie) throws NumberFormatException, SQLException, ParseException
	{
		ArrayList<String> ret = new ArrayList<String>(0);

		ret = Ssh.executeAction("nice -n 19  \"/mnt/HD/HD_a2/ffp/opt/share/filebot/bin/filebot.sh\" -list --db TheTVDB --q \"" + serie
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
				
			
				ResultSet rs = null;
				rs = Param.stmt.executeQuery("SELECT * "
											 + " FROM episodes "
											 + "WHERE "
											 + " serie = '" + serie + "'"
											 + " and num_saison = '" + Integer.valueOf(exLineEp[1]) + "'"
											 + " and num_episodes = '" + Integer.valueOf(exLineEp[2]) + "'"				 
											 + " ");
				rs.last();
				if (rs.getRow() == 0)
				{	
					Param.stmt.executeUpdate("INSERT INTO episodes "
							 + " ( nom , airdate , serie , num_saison , num_episodes ) VALUES " 
							 + " ("
							 + " '" + exLineEp[5] + "' ,"
							 + " '" + (new SimpleDateFormat("yyyy-MM-dd")).format(airDate) + "' ,"
							 + " '" + serie + "' ,"
							 + " '" + Integer.valueOf(exLineEp[1]) + "' ,"
							 + " '" + Integer.valueOf(exLineEp[2]) + "' "				 
							 + " ");
				} else {
					rs.updateString("nom",exLineEp[5] );
					rs.updateDate("airdate",(java.sql.Date) airDate);
				}
			

			}
		}

	}
}
