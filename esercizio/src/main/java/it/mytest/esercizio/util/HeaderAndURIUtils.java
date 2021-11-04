package it.mytest.esercizio.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public final class HeaderAndURIUtils {

	static final String authSchema = "S2S";
	static final String apiKey = "FXOVVXXHVCPVPBZXIJOBGUGSKHDNFRRQJP";
	
	public static HttpHeaders composeHeader() {
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Auth-Schema", authSchema);  
		headers.set("Api-Key", apiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		return headers;
	}
	
	public static HttpHeaders composeHeaderForPost() {
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Auth-Schema", authSchema);  
		headers.set("Api-Key", apiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Time-Zone", "Europe/Rome");
		
		return headers;
	}
	
	public static String composeTransaciontUrl(String currentTransactionListUrl, String fromAccountingDate, String toAccountingDate) {
		return currentTransactionListUrl + "?fromAccountingDate=" + fromAccountingDate + "&toAccountingDate=" + toAccountingDate;
	}
	
}
