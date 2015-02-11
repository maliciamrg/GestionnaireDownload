import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class Ssh {

/*	public static ArrayList<String> executeAction(String command) throws InterruptedException, JSchException, IOException {
		

	
		StringBuffer output = new StringBuffer();
		Param.logger.debug("executeAction:"+command);
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");

			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>(); 
		}

		return (ArrayList<String>) Arrays.asList(output.toString().split("\n"));
	}
*/
	
	public static boolean Fileexists(String fichier) throws JSchException, IOException, InterruptedException {
		ArrayList<String> ret = Ssh.executeAction("stat \""+fichier+"\" > /dev/null 2>&1");
		return !ret.contains("exit-status:1");
	}

	public static void copyFile(String src, String dest) throws InterruptedException, JSchException, IOException {
		if (Fileexists(src)){
			if (!Fileexists(dest)){
				Ssh.executeAction("mkdir -p \"" + dest.substring(0,dest.lastIndexOf("/")) + "\"");
			}
			Ssh.executeAction("cp -r '"+src+"'* \"" + dest + "\"");
			Ssh.executeAction("rm -R '"+src+"'");
		}
	}



	public static ArrayList<String> executeAction(String command) throws JSchException, IOException, InterruptedException
	{

		ArrayList<String> retour = new ArrayList<String>(0);
		Param.logger.warn("command: " + command);


		Channel channel = Param.session.openChannel("exec");
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
					Param.logger.warn("ssh- " + line);
					retour.add(line);
				}
			}
			if (channel.isClosed())
			{
				if (in.available() > 0)
					continue;
				Param.logger.warn("ssh- " + "exit-status: " + channel.getExitStatus());
				if (retour.size()==0){
					retour.add("exit-status:"+channel.getExitStatus());
				}
				if (channel.getExitStatus() != 0)
				{
					BufferedReader inbstderr = new BufferedReader(new InputStreamReader(stderr));
					while ((line = inbstderr.readLine()) != null)
					{
						Param.logger.warn("ssh- " + line);
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
		return retour;
	}


	public static ArrayList<String> getRemoteFileList(String Remoterepertoire)
	{
		ArrayList<String> ret = new ArrayList<String>(0);
		ArrayList<String> repertoire = new ArrayList<String>(0);

		Channel channel = null;
		ChannelSftp channelSftp = null;

		try
		{

			channel = Param.session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;

			boucleLsSftp(ret, repertoire, Remoterepertoire, channelSftp);
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
				ret.add(directory + Param.Fileseparator + filename);
			}
			if (type.compareTo(" 2") == 0 && !filename.equals(".") && !filename.equals(".."))
			{
				repertoire.add(directory + Param.Fileseparator + filename);
			}
		}
	}

	public static File getRemoteFile(String remoteDir, String localDir, String fileName) throws JSchException, SftpException, IOException 
	{
		File newFile = null;

		Channel channel = (ChannelSftp) Param.session.openChannel("sftp");
		channel.connect();
		ChannelSftp channelSftp = (ChannelSftp) channel;
		Param.logger.warn("ssh- " + remoteDir + " to " + localDir + " file: " + fileName);
		channelSftp.cd(remoteDir);
		byte[] buffer = new byte[1024];
		BufferedInputStream bis = new BufferedInputStream(channelSftp.get(fileName));
		newFile = new File(localDir + "/" + fileName);
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
