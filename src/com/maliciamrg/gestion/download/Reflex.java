package com.maliciamrg.gestion.download;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.jcraft.jsch.JSchException;

public class Reflex {

	public static Object lancerMethode(Object o, Object[] args,
			String nomMethode) throws Exception {
		Class[] paramTypes = null;
		if (args != null) {
			paramTypes = new Class[args.length];
			for (int i = 0; i < args.length; ++i) {
				paramTypes[i] = args[i].getClass();
			}
		}
		Method m = o.getClass().getMethod(nomMethode, paramTypes);
		return m.invoke(o, args);
	}

	public void maj_liste_episodes_allseries() throws SQLException, NumberFormatException, ParseException, JSchException, IOException, InterruptedException {
		System.out.println("maj_liste_episodes_allseries");
		Statement stmt = Param.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs;
		rs = stmt.executeQuery("SELECT series.nom "
						+ " FROM series "
						+ " GROUP BY "
						+ "  series.nom");
		while (rs.next()) {
			System.out.println(rs.getString("nom"));
			FileBot.maj_liste_episodes(rs.getString("nom"));
		}
		rs.close();
	}

	public void maj_liste_episodes(String serie) throws SQLException, NumberFormatException, ParseException, JSchException, IOException, InterruptedException {
		System.out.println("maj_liste_episodes");
		String[] spl = serie.split(",");
		for(String str : spl)
		{
			System.out.println(str);
			FileBot.maj_liste_episodes(str);
		}
	}
}
