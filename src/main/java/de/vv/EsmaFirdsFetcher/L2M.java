package de.vv.EsmaFirdsFetcher;

import java.util.ArrayList;

public class L2M {
	public ArrayList<String> urls = new ArrayList<String>();
	public ArrayList<String> fns = new ArrayList<String>();
	
	public void appendU(String s){
		urls.add(s);
	}
	
	public void appendF(String s){
		fns.add(s);
	}
}
