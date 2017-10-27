package de.vv.EsmaFirdsFetcher;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class App {
	final static Logger logger = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) {
		logger.info("EsmaFirdsFetcher: invoke");
		if(args.length!=1){
			logger.error("Invalid args amt: {}", args.length);
			System.err.println("Es wird ein Arguemtn erwartet:"
					+ "\n1: Storagepath");
			return;
		}
		String storage = args[0];
		String ts = getCurrentTimeStamp();					// holt sich das Heutige Datum im Format yyyy-MM-dd
		String s = getCURL(ts);								// erstellt die Esma Solr Url
		File dir = new File(storage+File.separator +ts);	// erstellt das SpeicherVerzeichnis
		if(!dir.exists())dir.mkdir();						// falls dieses Verzeichnis noch nicht exestiert, wird es angelegt
		L2M l2m = processFetch(s);							// parsed das Result und liefert Download-Filename Dictonary
		
		dlZip(dir, l2m);									// Laedt alle Dateien runter
		logger.info("EsmaFirdsFetcher: finished");
	}

	public static L2M processFetch(String s) {
		URL url;
		L2M l2m = new L2M();
		try {
			// get URL content
			url = new URL(s);
			URLConnection conn = url.openConnection();

			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				if (inputLine.contains("name=\"download_link\"")) {
					String asd = inputLine.split(">")[1];
					asd = asd.split("<")[0];
					l2m.appendU(asd);
				} else if (inputLine.contains("name=\"file_name\"")) {
					String asd = inputLine.split(">")[1];
					asd = asd.split("<")[0];
					l2m.appendF(asd);
				}
			}

		} catch (MalformedURLException e) {
			logger.error("MalformedURLException: {}", e.getMessage());
		} catch (IOException e) {
			logger.error("IOException: {}", e.getMessage());
		}
		return l2m;
	}

	public static String getCURL(String ts) {
		String[] str = {
				"https://registers.esma.europa.eu/solr/esma_registers_firds_files/select?q=*&fq=publication_date:%5B", // 2017-10-27
				"T00:00:00Z+TO+", // 2017-10-27
				"T23:59:59Z%5D&wt=xml&indent=true&start=0&rows=10000" };
		String s = str[0];
		for (int i = 1; i < str.length; i++) {
			s += ts + str[i];
		}
		return s;
	}

	public static void dlZip(File dir, L2M p) {
		for(int i = 0; i < p.fns.size(); i++){
			String u = p.urls.get(i);
			String f = p.fns.get(i);
			dlZip(u,f, dir);
		}
	}

	public static void dlZip(String u, String f, File pt) {
		try {
			URL url = new URL(u);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			InputStream in = connection.getInputStream();
			FileOutputStream out = new FileOutputStream(pt + File.separator  + f);
			copy(in, out, 1024);
			out.close();
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException: {}", e.getMessage());
		} catch (IOException e) {
			logger.error("IOException: {}", e.getMessage());
		}
	}

	public static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
		byte[] buf = new byte[bufferSize];
		int n = input.read(buf);
		while (n >= 0) {
			output.write(buf, 0, n);
			n = input.read(buf);
		}
		output.flush();
	}

	public static String getCurrentTimeStamp() {
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}

}
