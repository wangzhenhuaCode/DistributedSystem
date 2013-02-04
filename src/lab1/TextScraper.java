package lab1;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TextScraper implements MigratableProcess {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private volatile boolean stop = false;
	private TransactionalFileOutputStream outFile;
	private Integer i1, i2, j1, j2, k1, k2;

	@SuppressWarnings("finally")
	public String getData(String url) throws Exception {
		HttpURLConnection connection = null;
		BufferedReader br = null;
		StringBuilder sb = null;
		String line = null;
		String data = null;
		URL address = null;
		try {
			// use HttpURLConnection to get connection with the website
			address = new URL(url);
			connection = null;
			connection = (HttpURLConnection) address.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);
			connection.connect();

			// recieve data from server

			br = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			sb = new StringBuilder();

			while ((line = br.readLine()) != null) {
				sb.append(line.trim() + '\n');
			}
			data = sb.toString().trim();
			
		} catch (MalformedURLException e) {
			throw e;
			
		} catch (ProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			// close connection
			connection.disconnect();
			br.close();
			
			connection = null;
			return data;
		}
	}

	public TextScraper(String url) {
		i1 = 0;
		i2 = 0;
		j1 = 0;
		j2 = 0;
		k1 = 0;
		k2 = 0;
		stop = false;
		try {
			outFile = new TransactionalFileOutputStream(
					url, false);
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String query = "http://www.cnn.com/";
		String data = null;
		
		PrintStream out = new PrintStream(outFile);
		try {
			data = getData(query);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			out.println("Connection error");
		}

		if (data == null) {
			out.println("invalid arguments");
			return;
		}

		Document document = Jsoup.parse(data);

		Elements firstpage1 = document.getElementsByClass("cnn_mtt1content");
		
		while (i1 < firstpage1.size()) {
			Element bin1 = firstpage1.get(i1);
			Elements newsList = bin1.getElementsByTag("li");
			
			while (j1 < newsList.size()) {
				Element e = newsList.get(j1);
				Elements news = e.getElementsByTag("a");
				
				while (k1 < news.size()) {
					Element ee = news.get(k1);
					String[] url = ee.toString().split("\"");

					if (url[1].startsWith("/20")) {
						String query2 = "http://www.cnn.com" + url[1];
						String data2 = null;
						try {
							data2 = getData(query2);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							k1++;
							continue;
						}
						if (data2 == null) {
							out.println("No such news!");
							return;
						}
						Document document2 = Jsoup.parse(data2);
						Elements secondpage = document2.getElementsByTag("p");

						String title = url[2].trim();
						title = title.substring(1, title.length() - 4);
						// title = title.replaceAll("[^\\w]", "_");
						String filein = secondpage.text();

						out.println(title);
						out.println(filein);
						out.flush();
						System.out.println("get article: "+title);
					}
					k1++;
					if (stop) {
						out.close();
						try {
							outFile.close();
						} catch (IOException ex2) {
							// TODO Auto-generated catch block
							ex2.printStackTrace();
						}
						stop = false;
						return;
					}

				}
				k1=0;
				j1++;
			}
			j1=0;
			i1++;

		}
		Elements firstpage2 = document.getElementsByClass("cnn_sectbincntnt2");
		
		while (i2 < firstpage2.size()) {
			Element bin2 = firstpage2.get(i2);
			Elements newsList = bin2.getElementsByTag("li");
			
			while (j2 < newsList.size()) {
				Element e = newsList.get(j2);
				Elements news = e.getElementsByTag("a");
			
				while (k2 < news.size()) {
					Element ee = news.get(k2);
					String[] url = ee.toString().split("\"");

					if (url[1].startsWith("/20")) {
						String query2 = "http://www.cnn.com" + url[1];
						String data2=null;
						try {
							data2 = getData(query2);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							k2++;
							continue;
						}
						if (data2 == null) {
							out.println("No such news!");
							return;
						}
						Document document2 = Jsoup.parse(data2);
						Elements secondpage = document2.getElementsByTag("p");

						String title = ee.text().trim();

						// = title.replaceAll("[^\\w]", "_");

						// File filename = new File(title + ".cnn");
						String filein = secondpage.text();
						out.println(title);
						out.println(filein);
						out.flush();
						System.out.println("get article: "+title);
					}
					k2++;
					if (stop) {
						out.flush();
						out.close();
						try {
							outFile.flush();
							outFile.close();
						} catch (IOException ex2) {
							// TODO Auto-generated catch block
							ex2.printStackTrace();
						}
						stop = false;
						return;
					}

				}
				k2=0;
				j2++;
			}
			j2=0;
			i2++;

		}
		out.flush();
		out.close();
		try {
			outFile.flush();
			outFile.close();
		} catch (IOException ex2) {
			// TODO Auto-generated catch block
			ex2.printStackTrace();
		}

		stop = false;
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		stop = true;
	}

}
