package fr.revoicechat.web.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping
@Tag(name = "Chat", description = "Endpoints for real-time chat using Server-Sent Events (SSE)")
public interface ChatController extends LoggedApi {

  @GetMapping("/sse")
  @Operation(
      summary = "Subscribe to chat messages (SSE)",
      description = "Opens a Server-Sent Events (SSE) connection to receive chat messages in real time. "
                    + "The connection will continuously stream messages without needing repeated requests.",
      tags = {"Chat"},
      responses = {
          @ApiResponse(responseCode = "200", description = "SSE stream successfully opened")
      }
  )
  SseEmitter generateSseEmitter();
}
