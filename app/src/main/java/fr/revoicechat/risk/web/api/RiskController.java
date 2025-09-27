package fr.revoicechat.risk.web.api;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.risk.representation.RiskCategoryRepresentation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Tag(name = "Risk", description = "Endpoints for managing risk")
@Path("risk")
public interface RiskController {

  @Operation(summary = "Get all risk", description = "Get all risk")
  @APIResponse(responseCode = "200", description = "Risks successfully retrieved")
  @GET
  List<RiskCategoryRepresentation> getAllRisks();
}
