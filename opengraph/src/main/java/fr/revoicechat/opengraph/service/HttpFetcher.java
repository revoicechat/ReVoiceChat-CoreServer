package fr.revoicechat.opengraph.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

class HttpFetcher {

  private final HttpClient httpClient = HttpClient.newHttpClient();

  Document fetch(String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
                                     .uri(URI.create(url))
                                     .header("User-Agent", "ReVoiceChat-OpenGraph-Bot")
                                     .GET()
                                     .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return Jsoup.parse(response.body());
  }
}
