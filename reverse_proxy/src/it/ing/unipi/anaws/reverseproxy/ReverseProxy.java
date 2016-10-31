package it.ing.unipi.anaws.reverseproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

public class ReverseProxy {
	private static final String urlString = "http://www.ing.unipi.it";
	
	public static void findNeighbors(String borderRouter) 
			throws MalformedURLException, IOException, SocketTimeoutException {
		URL url = new URL(borderRouter);
		URLConnection connection = url.openConnection();
		//connection.connect();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		
		StringBuilder builder = new StringBuilder();
		String response, s;
		while ((s = in.readLine()) != null) {
			builder.append(s + "\n");
		}
		response = builder.toString();
		System.out.println(response);
	}

	public static void main(String[] args) {
		try {
			findNeighbors(urlString);
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
