import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.converters.basic.*;

public class Ssh
{
	private final static Logger logger = Logger.getLogger(Ssh.class);

	private static String RemoteHostName = "home.daisy-street.fr";
	private static String remoteHostUserName = "root";
	private static String remoteHostpassword = "roxanne01";

	public static ArrayList<String> executeAction(String command) throws JSchException, IOException, InterruptedException
	{

		ArrayList<String> retour = new ArrayList<String>(0);
		logger.warn("command: " + command);

		JSch jsch = new JSch();

		Session session = jsch.getSession(remoteHostUserName, RemoteHostName, 22);
		session.setPassword(remoteHostpassword);
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();

		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);

		channel.setInputStream(null);

		InputStream stderr = ((ChannelExec) channel).getErrStream();

		InputStream in = channel.getInputStream();

		BufferedReader inb = new BufferedReader(new InputStreamReader(in));
		channel.connect();

		String line;
		byte[] tmp = new byte[1024];

		while (true)
		{
			while (in.available() > 0)
			{
				while ((line = inb.readLine()) != null)
				{
					logger.warn("ssh- " + line);
					retour.add(line);
				}
			}
			if (channel.isClosed())
			{
				if (in.available() > 0)
					continue;
				logger.warn("ssh- " + "exit-status: " + channel.getExitStatus());
				if (channel.getExitStatus() != 0)
				{
					BufferedReader inbstderr = new BufferedReader(new InputStreamReader(stderr));
					while ((line = inbstderr.readLine()) != null)
					{
						logger.warn("ssh- " + line);
					}
					if (channel.getExitStatus() > 1 && channel.getExitStatus() != 255)
					{
						throw new InterruptedException("exit-status: " + channel.getExitStatus());
					}
				}
				break;
			}
			Thread.sleep(1000);
		}
		channel.disconnect();
		session.disconnect();
		return retour;
	}


	public static ArrayList<String> getRemoteFileList(String Remoterepertoire)
	{
		ArrayList<String> ret = new ArrayList<String>(0);
		ArrayList<String> repertoire = new ArrayList<String>(0);
		String SFTPHOST = RemoteHostName;
		int SFTPPORT = 22;
		String SFTPUSER = remoteHostUserName;
		String SFTPPASS = remoteHostpassword;
		String SFTPWORKINGDIR = Remoterepertoire;

		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;

		try
		{
			JSch jsch = new JSch();
			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
			session.setPassword(SFTPPASS);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;

			boucleLsSftp(ret, repertoire, SFTPWORKINGDIR, channelSftp);
			for (int i = 0; i < repertoire.size(); i++)
			{
				boucleLsSftp(ret, repertoire, repertoire.get(i), channelSftp);
			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		channelSftp.disconnect();
		channel.disconnect();
		session.disconnect();
		return ret;

	}

	/**
	 * @param ret
	 * @param repertoire
	 * @param directory
	 * @param channelSftp
	 * @throws SftpException
	 */
	private static void boucleLsSftp(ArrayList<String> ret, ArrayList<String> repertoire, String directory, ChannelSftp channelSftp) throws SftpException
	{
		channelSftp.cd(directory);
		Vector filelist = channelSftp.ls(directory);
		for (int i = 0; i < filelist.size(); i++)
		{
			LsEntry entry = (LsEntry) filelist.get(i);
			String type = entry.getLongname().substring(13, 15);
			String filename = entry.getFilename();
			if (type.compareTo(" 1") == 0)
			{
				ret.add(Repertoire.formatPath(directory + File.separator + filename, false));
			}
			if (type.compareTo(" 2") == 0 && !filename.equals(".") && !filename.equals(".."))
			{
				repertoire.add(Repertoire.formatPath(directory + File.separator + filename, false));
			}
		}
	}

	public static File getRemoteFile(String remoteDir, String localDir, String fileName) throws JSchException, IOException, SftpException
	{
		File newFile = null;
		JSch jsch = new JSch();
		Session session = jsch.getSession(remoteHostUserName, RemoteHostName, 22);
		session.setPassword(remoteHostpassword);
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.setTimeout(5000);
		session.connect();
		Channel channel = (ChannelSftp) session.openChannel("sftp");
		channel.connect();
		ChannelSftp channelSftp = (ChannelSftp) channel;
		logger.warn("ssh- " + remoteDir + " to " + localDir + " file: " + fileName);
		channelSftp.cd(remoteDir);
		byte[] buffer = new byte[1024];
		BufferedInputStream bis = new BufferedInputStream(channelSftp.get(fileName));
		newFile = new File(Repertoire.formatPath(localDir + "/" + fileName));
		OutputStream os = new FileOutputStream(newFile);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		int readCount;
		while ((readCount = bis.read(buffer)) > 0)
		{
			bos.write(buffer, 0, readCount);
		}
		bis.close();
		bos.close();

		return newFile;
	}

	public static void actionexecChmodR777(String formatPath) throws JSchException, IOException, InterruptedException
	{
		Ssh.executeAction("chmod -R 777 \"" + formatPath.replace(" ", "*") + "\" ");
	}
}
