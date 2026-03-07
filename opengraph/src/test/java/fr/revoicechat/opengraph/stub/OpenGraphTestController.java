package fr.revoicechat.opengraph.stub;

import java.io.InputStream;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@PermitAll
@Path("/tests/open-graph")
public class OpenGraphTestController {

  @GET
  @Path("/article")
  @Produces(MediaType.TEXT_HTML)
  public Response article() {
    return htmlResource("og-article.html");
  }

  @GET
  @Path("/music-album")
  @Produces(MediaType.TEXT_HTML)
  public Response musicAlbum() {
    return htmlResource("og-music-album.html");
  }

  @GET
  @Path("/book")
  @Produces(MediaType.TEXT_HTML)
  public Response book() {
    return htmlResource("og-book.html");
  }

  @GET
  @Path("/profile")
  @Produces(MediaType.TEXT_HTML)
  public Response profile() {
    return htmlResource("og-profile.html");
  }

  @GET
  @Path("/video")
  @Produces(MediaType.TEXT_HTML)
  public Response video() {
    return htmlResource("og-video.html");
  }

  @GET
  @Path("/edge-cases")
  @Produces(MediaType.TEXT_HTML)
  public Response edgeCases() {
    return htmlResource("og-edge-cases.html");
  }

  private Response htmlResource(String filename) {
    InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
    return Response.ok(stream).build();
  }
}