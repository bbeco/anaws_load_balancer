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
	
	protected static String[] parseHtml(String html) {
		/**
		 * The following are the strings that come before and after the list 
		 * of neighbors in the border router response. They are used by this 
		 * function to locate the nodes list.
		 */
		final String startTag = "Neighbors<pre>";
		final String endTag = "</pre>Routes";
		
		ArrayList<String> neighbors = new ArrayList<>();
		int index = html.indexOf(startTag);
		if (index < 0) {
			return null;
		}
		index += startTag.length();
		while (!html.substring(index, index + endTag.length()).equals(endTag)) {
			int endIndex = html.substring(index).indexOf('\n');
			neighbors.add(html.substring(index, index + endIndex));
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
			//findNeighbors(urlString);
			BufferedReader br = new BufferedReader(
					new FileReader(htmlFilePath));
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = br.readLine()) != null) {
				sb.append(s + '\n');
			}
			String[] res = parseHtml(sb.toString());
			br.close();
			for (String s1 : res) {
				System.out.println(s1);
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
