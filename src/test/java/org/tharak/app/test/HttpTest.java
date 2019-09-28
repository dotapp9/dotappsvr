package org.tharak.app.test;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

public class HttpTest {

	public static void main(String[] args)throws Exception {
		//add();
		select();
	}
	private static void select() throws Exception{
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();
		RequestEntity<String> requestEntity = new RequestEntity<String>("{\"query\": \"select Gst_Number,Per from tour_package where Tour_Id=$Tour_Id\", \"Tour_Id\": \"1\"}", headers, HttpMethod.POST, new URI("http://localhost:8080/api/1/test")) ;
//		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
		String response = restTemplate.exchange(requestEntity, String.class).getBody();
		System.out.println(response);		
	}
	private static void add() throws Exception{
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();
		RequestEntity<String> requestEntity = new RequestEntity<String>("{\"query\": \"INSERT INTO tour_package(Tour_Id) VALUES($Tour_Id)\", \"Tour_Id\": \"1\"}", headers, HttpMethod.POST, new URI("http://localhost:8080/api/1/test")) ;
//		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
		String response = restTemplate.exchange(requestEntity, String.class).getBody();
		System.out.println(response);		
	}
}
