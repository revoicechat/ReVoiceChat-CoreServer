package fr.revoicechat.opengraph;

public record OpenGraphArticle(String publishedTime,
                               String modifiedTime,
                               String expirationTime,
                               String author,
                               String section,
                               String tag) {}
