import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import com.jcraft.jsch.JSchException;

/**
 * Defini les caracteristique d'un repertoire (non forcement existant) dans le
 * cadre de la constitution d'une bibliotheque d'episode de series
 * 
 * @author Romain CLEMENT
 */
public class Repertoire {


	String repertoire;
	private static ArrayList<String> listFile = new ArrayList<String>(0);
	private static Boolean windowsMode;

	public static String currentPath(String defaultPath) {
		String workRepertoire = System.getProperty(("user.dir")) + File.separator;
		workRepertoire = workRepertoire.replace(File.separator + File.separator, File.separator);
		if (workRepertoire.compareTo(File.separator) == 0) {

			workRepertoire = workRepertoire + defaultPath;
		}
		if (workRepertoire.indexOf(":") > 0) {
			Repertoire.setWindowsMode(true);
		} else {
			Repertoire.setWindowsMode(false);
		}
		return Repertoire.formatPath(workRepertoire);
	}

	/**
	 * @param windowsMode
	 *            the windowsMode to set
	 */
	public static void setWindowsMode(Boolean windowsMode) {
		Repertoire.windowsMode = windowsMode;
	}

	public Repertoire() {
		this.repertoire = null;
	}

	public Repertoire(String repertoire) {
		this.repertoire = repertoire;
		Boolean ret = creeRepertoire(repertoire);
	}

	/**
	 * cree le repertoire si necesaire
	 * 
	 */
	public static Boolean creeRepertoire(String repertoire) {
		File dir = new File(Repertoire.formatPath(repertoire));
		dir.mkdirs();
		return null;
	}

	public static void purgeEmptyRepertoire(String chemin, String[] exclusion) throws UnsupportedEncodingException {
		File rep = new File(Repertoire.formatPath(chemin));
		bouclesRepertoire(rep, 99, false, exclusion);
	}

	private static void bouclesRepertoire(File pathCheminRacine, Integer nivRecurence, Boolean avecRepertoire, String[] exclusion)
			throws UnsupportedEncodingException {
		if (pathCheminRacine.isDirectory()) {
			for (String _ex : exclusion) {
				if (pathCheminRacine.getPath().contains(_ex)) {
					return;
				}
			}
			File[] sousfichier = pathCheminRacine.listFiles();
			for (File f : sousfichier) {
				if (f.isDirectory()) {
					bouclesRepertoire(f, 99, false, exclusion);
					listFile.clear();
					bouclesFiles(f, nivRecurence, avecRepertoire);
					if (listFile.size() == 0) {
						f.delete();
					}
				}
			}
		}
	}

	public static void purgeRepertoire(String chemin) {
		File rep = new File(Repertoire.formatPath(chemin));
		bouclesPurgeRepertoire(rep);
	}

	private static void bouclesPurgeRepertoire(File fichier) {

		File[] sousfichier = fichier.listFiles();
		if (sousfichier != null) {
			for (File f : sousfichier) {
				if (f.isDirectory()) {
					bouclesPurgeRepertoire(f);
				} else {
					f.delete();
				}
			}
		}
		fichier.delete();
	}

	public static ArrayList<String> searchAllFiles(String repertoire, String beginWith, String ExtensionExclu, Boolean repertoirreAccesible,
			Integer nivRecurence, Boolean avecRepertoire) throws IOException, JSchException, InterruptedException {
		ArrayList<String> ret = searchAllFiles(repertoire, repertoirreAccesible, nivRecurence, avecRepertoire);
		ArrayList<String> retASup = new ArrayList<String>(0);
		for (String _st : ret) {
			if (!Fichier.getFilePartName(_st).startsWith(beginWith) || _st.endsWith(ExtensionExclu)) {
				retASup.add(_st);
			}
		}
		ret.removeAll(retASup);
		return ret;

	}

	public static ArrayList<String> searchAllFiles(String repertoire, Boolean repertoirreAccesible, Integer nivRecurence, Boolean avecRepertoire)
			throws IOException {
		// Display.affichageLigne("repertoire",
		// (repertoirreAccesible?"true-":"false-")+repertoire);
		if (repertoirreAccesible) {
			return searchAllFiles(repertoire, nivRecurence, avecRepertoire);
		} else {
			// return Ssh.recupererSshAllFiles(repertoire,avecRepertoire);
			return Ssh.getRemoteFileList(repertoire);
		}
	}

	public static ArrayList<String> searchAllFiles(String chemin, Integer nivRecurence, Boolean avecRepertoire) throws UnsupportedEncodingException {
		listFile = new ArrayList<String>(0);
		listFile.clear();
		File rep = new File(Repertoire.formatPath(chemin));
		bouclesFiles(rep, nivRecurence, avecRepertoire);
		return listFile;
	}

	private static void bouclesFiles(File fichier, Integer nivRecurence, Boolean avecRepertoire) throws UnsupportedEncodingException {
		if (fichier.isDirectory()) {
			if (avecRepertoire) {
				listFile.add(/* Fichier.FormatUTF8toISO8859 */(fichier.toString() + File.separator));
			}
			if (nivRecurence > 0) {
				File[] sousfichier = fichier.listFiles();
				for (File f : sousfichier) {
					bouclesFiles(f, nivRecurence - 1, avecRepertoire);
				}
			}
		} else {
			if (nivRecurence >= 0) {
				listFile.add(/* Fichier.FormatUTF8toISO8859 */(formatPath(fichier.toString())));
			}
		}
	}

	public static String formatPath(String path) {
		return formatPath(path, windowsMode);
	}

	public static String formatPath(String path, Boolean wMode) {
		String ret = path;
		ret = ret.replace("\\\\", "/");
		ret = ret.replace("\\", "/");
		ret = ret.replace("//", "/");

		if (wMode) {
			ret = ret.replace("/mnt/HD/HD_a2/ffp/opt/srv/www", "W:");
			ret = ret.replace("/mnt/HD/HD_b2/VideoClubSeries", "V:");
			ret = ret.replace("/mnt/HD/HD_a2/VideoClub", "U:");
		} else {
			ret = ret.replace("W:", "/mnt/HD/HD_a2/ffp/opt/srv/www");
			ret = ret.replace("V:", "/mnt/HD/HD_b2/VideoClubSeries");
			ret = ret.replace("U:", "/mnt/HD/HD_a2/VideoClub");
		}
		return ret;
	}

}
