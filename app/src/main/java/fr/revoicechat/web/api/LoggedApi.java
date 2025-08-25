package fr.revoicechat.web.api;

import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@ApiResponse(
    responseCode = "401",
    description = "Unauthorized – user not logged in",
    content = {
        @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(type = "string", example = """
                {
                  "error": "Forbidden",
                  "message": "You do not have permission to access this resource.",
                  "swaggerDoc": "/api/swagger-ui/index.html"
                }""")
        ),
        @Content(
            schema = @Schema(type = "string", example = """
                <!DOCTYPE>
                <html lang="en">
                <head>
                    <title>403 Forbidden</title>
                </head>
                <body>
                <div class="container">
                    <h1>Access Denied</h1>
                    <p>You do not have permission to access this resource.</p>
                    <p>See the <a href="/api/swagger-ui/index.html">API documentation</a>.</p>
                </div>
                </body>
                </html>""")
        )
    }
)
public interface LoggedApi {
}
