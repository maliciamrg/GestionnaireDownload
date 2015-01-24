import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import ca.benow.transmission.SetTorrentParameters;
import ca.benow.transmission.model.TorrentStatus;
import ca.benow.transmission.model.TorrentStatus.TorrentField;


public class transmission {

	public transmission() {
		// TODO Auto-generated constructor stub
	}

	public static void supprimer_hash(String hash) {
		// TODO Auto-generated method stub
		
	}

	public static boolean fichier_absent(String hash) {
		// TODO Auto-generated method stub
		return false;
	}

	public static void deplacer_fichier(String hash, String cheminTemporaire) {
		// TODO Auto-generated method stub
		
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
