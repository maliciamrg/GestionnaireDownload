import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.benow.transmission.AddTorrentParameters;
import ca.benow.transmission.SetTorrentParameters;
import ca.benow.transmission.model.AddedTorrentInfo;
import ca.benow.transmission.model.TorrentStatus;
import ca.benow.transmission.model.TorrentStatus.TorrentField;


public class transmission {

	public transmission() {
	}

	public static void supprimer_hash(String hash) throws JSONException, IOException {
		int torrentId = torrentIdOfHash(hash);
		Param.client.removeTorrents(new Object[] { torrentId }, true);
	}

	public static boolean all_fichier_absent(String hash) throws JSONException, IOException {
		int torrentId = torrentIdOfHash(hash);
		List<TorrentStatus> torrents =  Param.client.getTorrents( new int[] {torrentId}, TorrentField.downloadDir ,TorrentField.files);
		for (TorrentStatus curr : torrents)
		{
			String downloadDir = (String.valueOf(curr.getField(TorrentField.downloadDir)));
			JSONArray listFile = (JSONArray) curr.getField(TorrentField.files);
			// Display.affichageProgressionInit("nb series traiter");
			int i = 0;
			for (i = 0; i < listFile.length(); i++)
			{
				JSONObject n = (JSONObject) listFile.get(i);
				if (new File(downloadDir + File.separator + n.getString("name")).exists())
				{
					return false;
				}
			}
			if (i>0){return true;}
		}
		return false;
	}

	public static void deplacer_fichier(String hash, String cheminTemporaire) throws JSONException, IOException {
		int torrentId = torrentIdOfHash(hash);
		List<TorrentStatus> torrents =  Param.client.getTorrents( new int[] {torrentId}, TorrentField.downloadDir ,TorrentField.files);
		for (TorrentStatus curr : torrents)
		{
			String downloadDir = (String.valueOf(curr.getField(TorrentField.downloadDir)));
			JSONArray listFile = (JSONArray) curr.getField(TorrentField.files);
			// Display.affichageProgressionInit("nb series traiter");
			int i = 0;
			for (i = 0; i < listFile.length(); i++)
			{
				JSONObject n = (JSONObject) listFile.get(i);
				String src = downloadDir + File.separator + n.getString("name");
				String dest = cheminTemporaire + File.separator + n.getString("name");
				if (new File(src).exists() && !new File(dest).exists())
				{
					Param.copyFile(new File(src), new File(dest), false);
				}
			}
		}
	}

	public static Boolean ajouterlemagnetatransmission(String magnet) {

		AddTorrentParameters paramTorrent = new AddTorrentParameters(magnet);
		/**
		 * addTorrent tombe en execption si torrent duplicate
		 */
		AddedTorrentInfo ret;
		try
		{
			ret = Param.client.addTorrent(paramTorrent);
		}
		catch (JSONException | IOException e)
		{
			e.printStackTrace();
		}

		return true;
	}
	
	/**
	 * cancelle un des fichier du torrent
	 */
	public static void cancelFilenameOfTorrent(String hashTransmisionTorrents, int indiceOfFile) throws JSONException, IOException
	{
		if (Param.client == null)
		{
			return;
		}
		SetTorrentParameters torrentParam = new SetTorrentParameters(torrentIdOfHash(hashTransmisionTorrents));
		ArrayList<Integer> listFilesUnwanted = new ArrayList<Integer>(0);
		listFilesUnwanted.add(indiceOfFile);
		torrentParam.filesUnwanted = listFilesUnwanted;
		Param.client.setTorrents(torrentParam);

	}

	private static int torrentIdOfHash(String hash) throws JSONException, IOException
	{
		List<TorrentStatus> torrents = Param.client.getAllTorrents(new TorrentField[] { TorrentField.id, TorrentField.hashString });
		for (TorrentStatus curr : torrents)
		{
			String hashTorrent = ((String) curr.getField(TorrentField.hashString));
			if (hash.compareToIgnoreCase(hashTorrent) == 0)
			{
				return ((Integer) curr.getField(TorrentField.id));
			}
		}
		return 0;
	}

}
