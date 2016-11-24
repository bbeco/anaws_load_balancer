package it.ing.unipi.anaws.reverseproxy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ReverseProxy {
	private static final String urlString = "http://www.ing.unipi.it";
	private static final String htmlFilePath = "/home/andrea/httpRequest/ContikiRPL.html";
	
	/**
	 * This function retrieves the list of border router neighbors.
	 * It performs an HTTP request to the border router and parse the response.
	 * 
	 * @param borderRouter a string with the border router IP address
	 * @return an array of string containing the mote's IP addresses
	 * @throws MalformedURLException when the border router IP is misspelled
	 * @throws IOException when there is a generic error while performing the 
	 * 		request.
	 * @throws SocketTimeoutException when the border router does not reply in 
	 * 		after a timeout has expired.
	 */
	public static String[] findNeighbors(String borderRouter) 
			throws MalformedURLException, IOException, SocketTimeoutException {
		URL url = new URL(borderRouter);
		URLConnection connection = url.openConnection();
		//The following call is executed implicitly by getInputStream
		//connection.connect();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		
		StringBuilder builder = new StringBuilder();
		String response, s;
		while ((s = in.readLine()) != null) {
			builder.append(s + '\n');
		}
		response = builder.toString();
		
		return parseHtml(response);
	}
	
	/**
	 * This function parse a string in order to find the list of border router 
	 * neighbors.
	 * @param html The string to be parsed
	 * @return an array of string containing the mote's IP addresses
	 */
	protected static String[] parseHtml(String html) {
		/**
		 * The following are the strings that come before and after the list 
		 * of neighbors in the border router response. They are used by this 
		 * function to locate the node list.
		 */
		final String startTag = "Routes<pre>";
		final String endTag = "</pre>\n</body>";
		/** this is the separator between addresses **/
		final String separator = "/128";
		
		ArrayList<String> neighbors = new ArrayList<>();
		int index = html.indexOf(startTag);
		if (index < 0) {
			return null;
		}
		index += startTag.length();
		while (!html.substring(index, index + endTag.length()).equals(endTag)) {
			int endIndex = html.substring(index).indexOf(separator);
			neighbors.add(html.substring(index, index + endIndex));
			endIndex = html.substring(index).indexOf("\n");
			index += endIndex + 1;
		}
		
		String[] result = new String[neighbors.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = neighbors.get(i);
		}
		
		return result;
	}

	public static void main(String[] args) {
		try {
			String[] res = findNeighbors(urlString);
			/*BufferedReader br = new BufferedReader(
					new FileReader(htmlFilePath));
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = br.readLine()) != null) {
				sb.append(s + '\n');
			}
			String[] res = parseHtml(sb.toString());
			br.close();*/
			if (res != null) {
				for (String s1 : res) {
					System.out.println(s1);
				}
			}
		} catch (MalformedURLException e) {
			System.err.println("Invalid URL string");
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			System.err.println("Timeout");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Unable to create UrlConnection object");
			e.printStackTrace();
		}

	}

}
