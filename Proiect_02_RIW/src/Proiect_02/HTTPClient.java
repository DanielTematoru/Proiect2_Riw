package Proiect_02;
import java.util.*;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;

public class HTTPClient {

	private int statusCode;

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getResource(String resourceName, String domain, int portTCP) {
		try {
			StringBuilder request = new StringBuilder();
			String location;
			URL newLocation;
			request.append("GET " + resourceName + " HTTP/1.1\r\n");
			request.append("Host: " + domain + "\r\n");
			request.append("User-Agent: " + Restrictii.userAgent + "\r\n");
			request.append("Connection: close\r\n");
			request.append("\r\n");

			String httpRequest = request.toString();
			//System.out.println("Request HTTP: " + httpRequest);
			//InetAddress ip=Crawler.getIP(domain);
			String IP=Crawler.getIP(domain);
			String []ips= {"81.180.223.1","8.8.8.8"};
			String []urls= {"http://riweb.tibeica.com","http://google.com"};
			CacheDns dns=new CacheDns();
			TrieNode root=new TrieNode();
			dns.insertTrie(root, ips, urls);
			String error="Ip invalid";
			if(error==dns.search(root, "81.180.223.1",0))
			{
				System.out.print("----Numele de domeniu nu a fost gasit in cache----\n\n");
			}
			else
			{
				System.out.print("----Numele de domeniu  a fost gasit in cache----\n\n");
			}
			Socket tcpSocket = new Socket(domain, 80);
						
			DataOutputStream outToServer = new DataOutputStream(tcpSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

			outToServer.writeBytes(httpRequest);

			// System.out.println("Clientul a primit raspuns de la server: \n");

			String responseLine;
			responseLine = inFromServer.readLine();
			StringBuilder httpProtocol = new StringBuilder();
			int poz = 0;
			while (poz < responseLine.length() && responseLine.charAt(poz) != ' ') {
				httpProtocol.append(responseLine.charAt(poz));
				poz++;
			}
			poz++;
			if (httpProtocol.toString().equals("HTTP/1.1")) {
				switch (responseLine.substring(poz, poz + 3)) {
				case "200":
					setStatusCode(200);
					String path = domain + resourceName;
					if (!(path.endsWith(".html") || path.endsWith("htm"))) {
						if (!path.endsWith("/")) {
							path += "/";
						}
						path += "userAgent.html";
					}

					File file = new File(path);
					File parentDirectory = file.getParentFile();
					if (!parentDirectory.exists()) {
						parentDirectory.mkdirs();
					}
					BufferedWriter writer = new BufferedWriter(new FileWriter(path));
					while ((responseLine = inFromServer.readLine()) != null) {
						//System.out.println(responseLine);
						if (responseLine.equals("")) {
							break;
						}
					}
					while ((responseLine = inFromServer.readLine()) != null) {
						writer.write(responseLine + System.lineSeparator());
						if (responseLine.toLowerCase().equals("</html>")) {
							break;
						}
					}
					writer.close();
					return path;
				case "404":
					System.out.println("Not Found");
					setStatusCode(404);
					return null;
				case "301":
					setStatusCode(301);
					location = "";
					while ((responseLine = inFromServer.readLine()) != null) {
						if (responseLine.equals("")) {
							break;
						}
						if (responseLine.startsWith("Location:")) {
							location = responseLine.replace("Location: ", "");
						}
					}
					newLocation = new URL(location);
					return getResource(newLocation.getPath(), newLocation.getHost(), newLocation.getPort());
				case "307":
					setStatusCode(307);
					location = "";
					while ((responseLine = inFromServer.readLine()) != null) {
						if (responseLine.equals("")) {
							break;
						}
						if (responseLine.startsWith("Location:")) {
							location = responseLine.replace("Location: ", "");
						}
					}
					newLocation = new URL(location);
					return getResource(newLocation.getPath(), newLocation.getHost(), newLocation.getPort());
				}
			}
			tcpSocket.close();
		} catch (UnknownHostException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isAllowed(URL url, String strCommands) {
		boolean flag = true;
		if (strCommands.contains("Disallow")) 
		{
			String[] split = strCommands.split("\n");
			HashMap<String, String> robotsRules = new HashMap<String, String>();
			String mostRecentUserAgent = null;
			for (int i = 0; i < split.length; i++) {
				String line = split[i].trim();
				if (line.toLowerCase().startsWith("user-agent")) {
					int k = 0;
					while (line.charAt(k) != ':' && k < line.length()) {
						k++;
					}
					while ((!Character.isLetterOrDigit(line.charAt(k))) && (line.charAt(k) != '*')) {
						k++;
					}
					mostRecentUserAgent = line.substring(k, line.length()).trim();
				} else if (line.startsWith("Disallow")) {
					int start = line.indexOf(":") + 1;
					int end = line.length();
					String rules = line.substring(start, end).trim();
					robotsRules.put(mostRecentUserAgent, rules);
					
				}
			}

			for (String key : robotsRules.keySet()) {
				String path = url.getPath();
				if (robotsRules.get(key).length() == 0) 
				{
					flag = true;
				}

				if (robotsRules.get(key).length() == 0 && Restrictii.userAgent.equals(key)) 
				{														
					return true;
				}
				if (robotsRules.get(key).equals("/") && Restrictii.userAgent.equals(key))
				{															
					return false;
				}
				if (robotsRules.get(key).equals("/"))
				{
					flag = false;
				}
				
				if ((robotsRules.get(key).length() <= path.length() && Restrictii.userAgent.equals(key)) || (robotsRules.get(key).length() <= path.length() && key == "*")) 
				{
					String pathCompare = path.substring(0, robotsRules.get(key).length());
					if (pathCompare.equals(robotsRules.get(key)))
					{
						return false;
					}
				}
			}
		}
		return flag;
	}
}

