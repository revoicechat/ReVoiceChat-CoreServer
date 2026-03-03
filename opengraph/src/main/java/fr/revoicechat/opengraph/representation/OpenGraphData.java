package fr.revoicechat.opengraph.representation;

public record OpenGraphData(String url,
                            String title,
                            String description,
                            String image,
                            String siteName) {}