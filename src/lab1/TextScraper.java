package lab1;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TextScraper {
	public String getData(String url){
		HttpURLConnection connection = null;
		BufferedReader br = null;
		StringBuilder sb = null;
		String line = null;
		String data = null;
		URL address = null;
		try{
			// use HttpURLConnection to get connection with the website
			address = new URL(url);
			connection = null;
			connection = (HttpURLConnection) address.openConnection();
			connection.setRequestMethod("GET"); 
			connection.setDoOutput(true); 
			connection.setReadTimeout(100000);
			connection.connect();
			
			//recieve data from server
			
			br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			sb = new StringBuilder();
			
			
			while((line = br.readLine()) != null){				
				sb.append(line.trim() + '\n');
			}
			data = sb.toString().trim();
			return data;
		}catch(MalformedURLException e){
			e.printStackTrace();
			return null;
		}catch (ProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}finally{
			//close connection
			connection.disconnect();
			br = null;
			sb = null;
			connection = null;
		}
	}
	
	public void result(String keyword){
		String query = null;
		String data;
		if(keyword.toLowerCase().equals("cnn")){
			 query = "http://www.cnn.com/";
			 
		}
		
		data = getData(query);
		
		if(data == null){
			System.out.println("invalid arguments");
			return;
		}
		
		Document document = Jsoup.parse(data);
		
		
		Elements firstpage1 = document.getElementsByClass("cnn_mtt1content");
		
		for(Element bin1 : firstpage1){
			Elements newsList = bin1.getElementsByTag("li");
			for(Element e : newsList){
				Elements news = e.getElementsByTag("a");
				for(Element ee : news){
					String[] url = ee.toString().split("\"");
					
					if(url[1].startsWith("/20")){
						String query2 = "http://www.cnn.com" + url[1];
						String data2 = getData(query2);
						if(data2 == null){
							System.out.println("No such news!");
							return;
						}
						Document document2 = Jsoup.parse(data2);
						Elements secondpage = document2.getElementsByTag("p");
						
						String title = url[2].trim();
						title= title.substring(1, title.length()-4);
						title = title.replaceAll("[^\\w]", "_");
						
						File filename = new File(title + ".cnn");
						String filein = secondpage.text();
						
						RandomAccessFile raf = null;
						
							try {
								raf = new RandomAccessFile(filename, "rw");
								raf.writeBytes(filein);
								if(raf != null)
									raf.close();
							} catch (FileNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						
					}
					
				}
			}
			
		}
		Elements firstpage2 = document.getElementsByClass("cnn_sectbincntnt2");

		for(Element bin2 : firstpage2){
			Elements newsList = bin2.getElementsByTag("li");
			for(Element e : newsList){
				Elements news = e.getElementsByTag("a");
				for(Element ee : news){
					String[] url = ee.toString().split("\"");
					
					if(url[1].startsWith("/20")){
						String query2 = "http://www.cnn.com" + url[1];
						String data2 = getData(query2);
						if(data2 == null){
							System.out.println("No such news!");
							return;
						}
						Document document2 = Jsoup.parse(data2);
						Elements secondpage = document2.getElementsByTag("p");
					
						String title = ee.text().trim();
						
						
						title = title.replaceAll("[^\\w]", "_");
						
						File filename = new File(title + ".cnn");
						String filein = secondpage.text();
						
						RandomAccessFile raf = null;
						
							try {
								raf = new RandomAccessFile(filename, "rw");
								raf.writeBytes(filein);
								if(raf != null)
									raf.close();
							} catch (FileNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						
					}
					
				}
			}
			
		}
		
		
	}
	public static void main(String[] args){
		TextScraper ts = new TextScraper();
		ts.result("cnn");
	}
	
}
