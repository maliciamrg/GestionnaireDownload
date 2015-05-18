package com.maliciamrg.gestion.download;
/*
 * Copyright (c) 2015 by Malicia All rights reserved.
 * 
 * 26 mars 2015
 * 
 * 
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.benow.transmission.AddTorrentParameters;
import ca.benow.transmission.SetTorrentParameters;
import ca.benow.transmission.model.AddedTorrentInfo;
import ca.benow.transmission.model.TorrentStatus;
import ca.benow.transmission.model.TorrentStatus.TorrentField;
import ca.benow.transmission.model.TransmissionSession.SessionField;

import com.jcraft.jsch.JSchException;

// TODO: Auto-generated Javadoc
/**
 * The Class transmission.
 */
public class transmission {

	/**
	 * Instantiates a new transmission.
	 */
	public transmission() {
	}

	/**
	 * Listhash transmission.
	 * 
	 * @return the array list
	 * @throws JSONException
	 *             the JSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static ArrayList<String> listhashTransmission()
			throws JSONException, IOException {
		ArrayList<String> ret = new ArrayList<String>();
		List<TorrentStatus> torrents = Param.client
				.getAllTorrents(new TorrentField[] { TorrentField.hashString });
		for (TorrentStatus curr : torrents) {
			String hash = (String) curr.getField(TorrentField.hashString);
			ret.add(hash);
		}
		return ret;
	}

	/**
	 * Supprimer_hash.
	 * 
	 * @param hash
	 *            the hash
	 * @throws JSONException
	 *             the JSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void supprimer_hash(String hash) throws JSONException,
			IOException {
		int torrentId = torrentIdOfHash(hash);
		Param.client.removeTorrents(new Object[] { torrentId }, false);
	}

	public static void resume_hash(String hash) throws JSONException,
			IOException {
		int torrentId = torrentIdOfHash(hash);
		Param.client.startTorrents(torrentId);
	}

	/**
	 * All_fichier_absent.
	 * 
	 * @param hash
	 *            the hash
	 * @return true, if successful
	 * @throws JSONException
	 *             the JSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws JSchException
	 *             the j sch exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public static boolean all_fichier_absent(String hash) throws JSONException,
			IOException, JSchException, InterruptedException {
		int torrentId = torrentIdOfHash(hash);
		List<TorrentStatus> torrents = Param.client.getTorrents(
				new int[] { torrentId }, TorrentField.downloadDir,
				TorrentField.files);
		for (TorrentStatus curr : torrents) {
			String downloadDir = (String.valueOf(curr
					.getField(TorrentField.downloadDir)));
			JSONArray listFile = (JSONArray) curr.getField(TorrentField.files);
			// Display.affichageProgressionInit("nb series traiter");
			int i = 0;
			for (i = 0; i < listFile.length(); i++) {
				JSONObject n = (JSONObject) listFile.get(i);
				if (Ssh.Fileexists(downloadDir + Param.Fileseparator
						+ n.getString("name"))) {
					return false;
				}
			}
			if (i > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Deplacer_fichier.
	 * 
	 * @param hash
	 *            the hash
	 * @param cheminTemporaire
	 *            the chemin temporaire
	 * @throws JSONException
	 *             the JSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws JSchException
	 *             the j sch exception
	 */
	public static void deplacer_fichier(String hash, String cheminTemporaire)
			throws JSONException, IOException, InterruptedException,
			JSchException {
		int torrentId = torrentIdOfHash(hash);
		List<TorrentStatus> torrents = Param.client.getTorrents(
				new int[] { torrentId }, TorrentField.downloadDir,
				TorrentField.files);
		for (TorrentStatus curr : torrents) {
			String downloadDir = (String.valueOf(curr
					.getField(TorrentField.downloadDir)));
			JSONArray listFile = (JSONArray) curr.getField(TorrentField.files);
			// Display.affichageProgressionInit("nb series traiter");
			int i = 0;
			for (i = 0; i < listFile.length(); i++) {
				JSONObject n = (JSONObject) listFile.get(i);
				String src = downloadDir + Param.Fileseparator
						+ n.getString("name");
				String dest = cheminTemporaire + n.getString("name");
				Ssh.moveFile(src, dest);
			}
		}
	}

	/**
	 * Ajouterlemagnetatransmission.
	 * 
	 * @param magnet
	 *            the magnet
	 * @return the boolean
	 */
	public static Boolean ajouterlemagnetatransmission(String magnet) {

		AddTorrentParameters paramTorrent = new AddTorrentParameters(magnet);
		/**
		 * addTorrent tombe en execption si torrent duplicate
		 */
		AddedTorrentInfo ret;
		try {
			ret = Param.client.addTorrent(paramTorrent);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * cancelle un des fichier du torrent.
	 * 
	 * @param hashTransmisionTorrents
	 *            the hash transmision torrents
	 * @param indiceOfFile
	 *            the indice of file
	 * @throws JSONException
	 *             the JSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void cancelFilenameOfTorrent(String hashTransmisionTorrents,
			int indiceOfFile) throws JSONException, IOException {
		if (Param.client == null) {
			return;
		}
		SetTorrentParameters torrentParam = new SetTorrentParameters(
				torrentIdOfHash(hashTransmisionTorrents));
		ArrayList<Integer> listFilesUnwanted = new ArrayList<Integer>(0);
		listFilesUnwanted.add(indiceOfFile);
		torrentParam.filesUnwanted = listFilesUnwanted;
		Param.client.setTorrents(torrentParam);

	}

	/**
	 * cancelle un des fichier du torrent.
	 * 
	 * @param hashTransmisionTorrents
	 *            the hash transmision torrents
	 * @param indiceOfFile
	 *            the indice of file
	 * @throws JSONException
	 *             the JSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void uncancelFilenameOfTorrent(
			String hashTransmisionTorrents, int indiceOfFile)
			throws JSONException, IOException {
		if (Param.client == null) {
			return;
		}
		SetTorrentParameters torrentParam = new SetTorrentParameters(
				torrentIdOfHash(hashTransmisionTorrents));
		ArrayList<Integer> listFilesWanted = new ArrayList<Integer>(0);
		listFilesWanted.add(indiceOfFile);
		torrentParam.filesWanted = listFilesWanted;
		Param.client.setTorrents(torrentParam);

	}

	/**
	 * Torrent id of hash.
	 * 
	 * @param hash
	 *            the hash
	 * @return the int
	 * @throws JSONException
	 *             the JSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static int torrentIdOfHash(String hash) throws JSONException,
			IOException {
		List<TorrentStatus> torrents = Param.client
				.getAllTorrents(new TorrentField[] { TorrentField.id,
						TorrentField.hashString });
		for (TorrentStatus curr : torrents) {
			String hashTorrent = ((String) curr
					.getField(TorrentField.hashString));
			if (hash.compareToIgnoreCase(hashTorrent) == 0) {
				return ((Integer) curr.getField(TorrentField.id));
			}
		}
		return 0;
	}

	/**
	 * Deplacer_fichier.
	 * 
	 * @param hash
	 *            the hash
	 * @param cheminTemporaire
	 *            the chemin temporaire
	 * @param numfichiertransmission
	 *            the numfichiertransmission
	 * @throws JSONException
	 *             the JSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws JSchException
	 *             the j sch exception
	 */
	public static Boolean deplacer_fichier(String hash,
			String cheminTemporaire, int numfichiertransmission)
			throws JSONException, IOException, InterruptedException,
			JSchException {
		int torrentId = torrentIdOfHash(hash);
		boolean ret = true;
		List<TorrentStatus> torrents = Param.client.getTorrents(
				new int[] { torrentId }, TorrentField.doneDate,
				TorrentField.isFinished, TorrentField.downloadDir,
				TorrentField.files);
		for (TorrentStatus curr : torrents) {
			Map<SessionField, Object> SessionParametre = Param.client
					.getSession();

			String downloadDir = "";
			if (Integer.parseInt(String.valueOf(curr
					.getField(TorrentField.doneDate))) > 0) {
				downloadDir = (String.valueOf(curr
						.getField(TorrentField.downloadDir)));
			} else {
				downloadDir = (String.valueOf(SessionParametre
						.get(SessionField.incompleteDir)));
			}
			JSONArray listFile = (JSONArray) curr.getField(TorrentField.files);
			// Display.affichageProgressionInit("nb series traiter");

			JSONObject n = (JSONObject) listFile.get(numfichiertransmission);
			String src = downloadDir + Param.Fileseparator
					+ n.getString("name");
			String dest = cheminTemporaire + n.getString("name");
			ret = ret && Ssh.moveFile(src, dest);

		}
		return ret;
	}

}
