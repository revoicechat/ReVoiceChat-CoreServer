package fr.revoicechat.opengraph;

/**
 * Find an URL in the text.
 * If there is only one, fetch it, and return it's {@link OpenGraphSchema}.
 */
@FunctionalInterface
public interface OpenGraphExtractor {

  /**
   * @return the {@link OpenGraphSchema} of the only url in the text,
   *         if there is only one url.
   * If the url does not return an HTML, the {@link OpenGraphSchema} returned must be null.
   */
  OpenGraphSchema extract(String text);
}