package fr.revoicechat.web.api;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@ApiResponses({
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized – user not logged in",
        content = @Content(
            mediaType = "text/plain",
            schema = @Schema(type = "string", example = "user not logged in")
        )
    )
})
public interface LoggedApi {
}
