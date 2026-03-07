package fr.revoicechat.opengraph.service;

import java.util.Optional;

import org.jsoup.nodes.Document;

abstract class OpenGraphMapper<T> {

  public abstract T map(Document doc);

  protected String getMeta(Document document, String property) {
    return Optional.ofNullable(document)
                   .map(doc -> doc.selectFirst("meta[property=" + property + "]"))
                   .map(elem -> elem.attr("content"))
                   .orElse(null);
  }
}
