package Proiect_02;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {

	public HashSet<String> sitesNotFound;;
	public HTTPClient httpClient;
	public Queue<String> Q;
	public HashMap<String, String> visitedSites;
	public DnsClient dnsClient;
	public Site pageComponents;
	public HashMap<String, String> domainRobots;

	public Crawler() {
		sitesNotFound = new HashSet<String>();
		httpClient = new HTTPClient();
		Q = new LinkedList<String>();
		visitedSites = new HashMap<String, String>();
		dnsClient = new DnsClient();
		domainRobots = new HashMap<String, String>();
	}

	public void processesQueue() throws IOException {

		int numberOfCurrentPages = 0;
		while (!Q.isEmpty() && numberOfCurrentPages < Restrictii.limitOfPages) {

			String urlAddress = Q.poll();

			//System.out.println("Adresa coada: " + urlAddress);
			numberOfCurrentPages++;
			System.out.println("----Link-ul: " + numberOfCurrentPages+" ----");
			System.out.println("Adresa din coada: " + urlAddress);
			URL currentURL = new URL(urlAddress);

			int port = currentURL.getPort();
			if (port == -1) {
				port = 80;
				// System.out.print("ok");
			}
			String path = currentURL.getPath();
			if (path.equals("")) {
				path = "/";
			}

			if (!currentURL.getProtocol().equals("http")) {
				System.out.println("----PROTOCOL_NECUNOSCUT=> URL: " + urlAddress+"----");
			} else {
				if (!domainRobots.containsKey(currentURL.getHost())) {
					// System.out.print(currentURL.getUserInfo());
					String pathRobots = httpClient.getResource("/robots.txt", currentURL.getHost(), port);

					if (pathRobots != null) {
						byte[] encoded = Files.readAllBytes(Paths.get(pathRobots));
						String rulesRobots = new String(encoded);
						domainRobots.put(currentURL.getHost(), rulesRobots);

					} else if (httpClient.getStatusCode() == 404) {
						domainRobots.put(currentURL.getHost(), null);

					}

				}

				String robotsRules = domainRobots.get(currentURL.getHost());
				if (robotsRules != null) {
					if (!HTTPClient.isAllowed(currentURL, robotsRules)) {
						continue;
					}
				}
				//System.out.print(currentURL.getHost());
				String fileName = httpClient.getResource(path, currentURL.getHost(), port);

				if (fileName != null) {

					if (httpClient.getStatusCode() == 200) {
						visitedSites.put(urlAddress, fileName);
						Document doc;

						try {
							//System.out.println(fileName);
							//System.out.println("----Adresa initiala:" + urlAddress+"----");
							doc = Jsoup.parse(new File(fileName), null, urlAddress);
							// System.out.println(doc);
						} catch (IOException e) {
							doc = null;
						}

						Set<String> listOfLinks = getLinks(doc);
						for (String link : listOfLinks) {

							URL newLink = new URL(link);
							String futurePathForLing = newLink.getHost() + newLink.getPath();
							if (!(futurePathForLing.endsWith(".html") || futurePathForLing.endsWith("htm"))) {
								if (!futurePathForLing.endsWith("/")) {
									futurePathForLing += "/";
								}
								futurePathForLing += "index.html";
							}
							File f = new File(futurePathForLing);
							if (f.exists()) {
								System.out.println(
										"----Acest link a mai fost vizitat " + link+"----");
							} else {
								Q.add(link);
							}

							if (!visitedSites.containsKey(link)) {
								Q.add(link);
								// System.out.println("Link: " + link);
							}
						}

					} else if (httpClient.getStatusCode() == 301 || httpClient.getStatusCode() == 307) {
						//visitedSites.replace(urlAddress, fileName);
						 fileName = httpClient.getResource(path, currentURL.getHost(), port);
						 if (fileName != null) {
							 visitedSites.put(urlAddress, fileName);
								Document doc;

								try {
									//System.out.println(fileName);
									//System.out.println("----Adresa initiala:" + urlAddress+"----");
									doc = Jsoup.parse(new File(fileName), null, urlAddress);
									// System.out.println(doc);
								} catch (IOException e) {
									doc = null;
								}

								Set<String> listOfLinks = getLinks(doc);
								for (String link : listOfLinks) {

									URL newLink = new URL(link);
									String futurePathForLing = newLink.getHost() + newLink.getPath();
									if (!(futurePathForLing.endsWith(".html") || futurePathForLing.endsWith("htm"))) {
										if (!futurePathForLing.endsWith("/")) {
											futurePathForLing += "/";
										}
										futurePathForLing += "index.html";
									}
									File f = new File(futurePathForLing);
									if (f.exists()) {
										System.out.println(
												"----Acest link a mai fost vizitat " + link+"----");
									} else {
										Q.add(link);
									}

									if (!visitedSites.containsKey(link)) {
										Q.add(link);
										// System.out.println("Link: " + link);
									}
								}
							 
						 }
					}
				}
			}
		}
	}

	public static String getIP(String domain) throws IOException {

		DnsClient dnsClient = new DnsClient();
		byte[] request = new byte[12 + domain.length() + 6];
		byte[] responseFromRequest = new byte[512];

		dnsClient.createRequest(request, domain);
		//System.out.println("Requst : ");
		dnsClient.printArrayByte(request);

		dnsClient.getResponse(request, responseFromRequest);
		//System.out.println("Response : ");
		dnsClient.printArrayByte(responseFromRequest);

		String IPv4 = "";
		IPv4 = dnsClient.checkResponse(responseFromRequest, request, domain);
		//System.out.print(IPv4);
		return IPv4;
	}

	public Set<String> getLinks(Document doc) {
		Elements links = doc.select("a[href], A[href]");
		Set<String> URLs = new HashSet<String>();
		for (Element link : links) {
			String absoluteLink = link.attr("abs:href");
			int anchorPosition = absoluteLink.indexOf('#');
			if (anchorPosition != -1) {
				StringBuilder tempLink = new StringBuilder(absoluteLink);
				tempLink.replace(anchorPosition, tempLink.length(), "");
				absoluteLink = tempLink.toString();
			}

			try {
				URL absoluteLinkURL = new URL(absoluteLink);
				String path = absoluteLinkURL.getPath();
				String extension = path.substring(path.lastIndexOf(".") + 1);
				if (!extension.isEmpty()) {
					if (!(path.endsWith("html") || path.endsWith("htm"))) {
						continue;
					}
				}

				URLs.add(absoluteLink);
			} catch (MalformedURLException e) {
				System.out.println("EROARE: " + absoluteLink);
				continue;
			}
		}
		
		return URLs;
	}

}
