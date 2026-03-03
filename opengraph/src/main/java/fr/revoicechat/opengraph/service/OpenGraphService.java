package fr.revoicechat.opengraph.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import fr.revoicechat.opengraph.representation.OpenGraphData;
import fr.revoicechat.opengraph.utils.UrlsExtractor;

@ApplicationScoped
public class OpenGraphService {

  private final HttpClient httpClient = HttpClient.newHttpClient();

  public OpenGraphData extractForSingleUrl(String text) throws IOException, InterruptedException {
    List<String> urls = UrlsExtractor.extract(text);
    if (urls.size() != 1) {
      return null;
    }
    String url = urls.getFirst();
    return fetchOpenGraph(url);
  }

  private OpenGraphData fetchOpenGraph(String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
                                     .uri(URI.create(url))
                                     .header("User-Agent", "ReVoiceChat-OpenGraph-Bot")
                                     .GET()
                                     .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    Document doc = Jsoup.parse(response.body());
    String title = getMeta(doc, "og:title");
    String description = getMeta(doc, "og:description");
    String image = getMeta(doc, "og:image");
    String siteName = getMeta(doc, "og:site_name");

    return new OpenGraphData(url, title, description, image, siteName);
  }

  private String getMeta(Document document, String property) {
    return Optional.ofNullable(document)
                   .map(doc -> doc.selectFirst("meta[property=" + property + "]"))
                   .map(elem -> elem.attr("content"))
                   .orElse(null);
  }
}