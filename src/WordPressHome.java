import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.*;
import java.io.*;

/**
 *  * @author romain clement
 */
public class WordPressHome
{
	private final static Logger logger = Logger.getLogger(WordPressHome.class);
//	private static String username = "mobile";	
// 	private static String pwd = "mobile";
//	private static String xmlRpcUrl = "http://www.daisy-street.fr/wordpress/xmlrpc";
	private static ArrayList<HashMap> tableBlog = new ArrayList();

	public static ArrayList<HashMap> getTableBlog() throws XmlRpcException, MalformedURLException
	{
		if (tableBlog.size() - 1 < 1)
		{
			initTableBlog();
		}
		return tableBlog;
	}

	/**
	 *      * Publishes the given posts on the blog.      
	 */
	public static void publishOnBlog(int numBlog, String title, String Resume, String[] motcle, String[] categorie, String billet) throws XmlRpcException, MalformedURLException
	{

		if (tableBlog.size() - 1 < numBlog)
		{
			initTableBlog();
		}

		HashMap hashSite = tableBlog.get(numBlog);

		Integer[] categorieid = convertTerms(hashSite, "category", categorie);
		Integer[] motcleid = convertTerms(hashSite, "post_tag", motcle);

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL((String) hashSite.get("xmlrpc")));
		XmlRpcClient client = new XmlRpcClient();
		// client.setTransportFactory(new
		// XmlRpcSunHttpTransportFactory(client));
		client.setConfig(config);
		HashMap hmContent = new HashMap();
		// hmContent.put("title", title);
		// hmContent.put("description", Resume);
		hmContent.put("post_title", title);
		hmContent.put("post_content", billet);
		hmContent.put("post_excerpt", Resume);
		// hmContent.put("post_status", "draft");
		hmContent.put("post_status", "publish");

		hmContent.put("tags_input", new String[] { "1" });
		hmContent.put("post_category", "test1,test2,test3");

		hmContent.put("comment_status", true);
		hmContent.put("ping_status", false);
		// Basically, we can put anything here as long as it match's
		// wordpress's fields.
		HashMap<String, Integer[]> terms = new HashMap<String, Integer[]>();
		terms.put("category", categorieid);
		terms.put("post_tag", motcleid);
		// Display.affichageListe("terms", terms);
		// hmContent.put("terms_names", terms);
		hmContent.put("terms", terms);
		// All set!! Let's roll~ and call the wordpress.
		Object[] params = new Object[] { numBlog, Param.WordPressusername, Param.WordPresspwd, hmContent, terms };
		String result = (String) client.execute("wp.newPost", params);
		logger.debug("WordPress-" + "post_id=" + result);


	}

	private static Integer[] convertTerms(HashMap hashsite, String termsValeurs, String[] motcle) throws MalformedURLException, XmlRpcException
	{
		ArrayList<Integer> retId = new ArrayList<Integer>();
		HashMap<String, HashMap> terms = getTerms(hashsite, termsValeurs);
		for (String _ter : motcle)
		{
			String key = _ter + "." + termsValeurs;
			if (!terms.containsKey(key))
			{
				if (_ter != null && termsValeurs != null)
				{
					// Display.affichageLigne(key);
					terms.put(key, newTerm(hashsite, termsValeurs, _ter));
				}
			}
			retId.add(Integer.valueOf((String) terms.get(key).get("term_id")));
		}
		// Display.affichageLigne("retId", Arrays.toString(retId.toArray(new
		// Integer[0])));
		return retId.toArray(new Integer[0]);
	}

	/**
	 *      * Publishes the given posts on the blog.      
	 */
	private static void initTableBlog() throws MalformedURLException, XmlRpcException
	{

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(Param.WordPressxmlRpcUrl));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);

		Object[] params2 = new Object[] { Param.WordPressusername, Param.WordPresspwd };
		Object[] result2 = (Object[]) client.execute("wp.getUsersBlogs", params2);
		for (Object _hi : result2)
		{
			HashMap _h = (HashMap) _hi;

			Integer n = Integer.valueOf((String) _h.get("blogid"));

			int i = 0;
			for (i = tableBlog.size(); i < n + 1; i++)
			{
				tableBlog.add(new HashMap());
			}
			tableBlog.set(n, _h);
			System.out.println("blog=" + n + "-" + _h);
		}


	};

	/**
	 *      * Publishes the given posts on the blog.      
	 */
	private static HashMap<String, HashMap> getTerms(HashMap hashsite, String termsValeurs) throws MalformedURLException, XmlRpcException
	{
		HashMap<String, HashMap> terms = new HashMap<String, HashMap>();
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL((String) hashsite.get("xmlrpc")));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);

		Object[] params2 = new Object[] { 0, Param.WordPressusername, Param.WordPresspwd, termsValeurs };
		Object[] result2 = (Object[]) client.execute("wp.getTerms", params2);

		// Display.affichageListe("getTaxonomy", result2);
		for (Object _hi : result2)
		{
			HashMap _h = (HashMap) _hi;
			// Display.affichageListe("getTerms", _h);
			terms.put((String) _h.get("name") + "." + termsValeurs, _h);
		}


		return terms;
	}

	private static HashMap newTerm(HashMap hashsite, String termsValeurs, String _ter) throws MalformedURLException, XmlRpcException
	{
		HashMap ret = new HashMap();
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL((String) hashsite.get("xmlrpc")));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);

		HashMap<String, String> terms = new HashMap<String, String>();
		terms.put("name", _ter);
		terms.put("taxonomy", termsValeurs);
		// Display.affichageListe("", terms);
		Object[] params2 = new Object[] { 0, Param.WordPressusername, Param.WordPresspwd, terms };
		String result2 = (String) client.execute("wp.newTerm", params2);

		// Display.affichageLigne("newTerm", result2);
		ret.put("name", _ter);
		ret.put("taxonomy", termsValeurs);
		ret.put("term_id", result2);
		// for (Object _hi : result2)
		// {
		// HashMap _h = (HashMap) _hi;
		// Display.affichageListe("getTerms", _h);
		// terms.put((String)_h.get("name") + "." + termsValeurs, _h);
		// }

		return ret;
	}

}
