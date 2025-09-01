package fr.revoicechat.core.web.error;

import static fr.revoicechat.core.nls.HttpStatusErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import fr.revoicechat.core.error.BadRequestException;
import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.nls.CommonErrorCode;
import fr.revoicechat.core.nls.LocalizedMessageTestEnum;
import fr.revoicechat.core.stub.HttpHeadersMock;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;

class TestGlobalExceptionMapper {

  private GlobalExceptionMapper mapper;

  @BeforeEach
  void setUp() {
    UnknownErrorFileGenerator fileGenerator = new UnknownErrorFileGenerator();
    mapper = new GlobalExceptionMapper(fileGenerator);
  }

  @Test
  void testBadRequestException() {
    mapper.logError = true;
    mapper.headers = new HttpHeadersMock() {};
    var ex = new BadRequestException(LocalizedMessageTestEnum.TEST_IN_ENGLISH_ONLY);
    var response = mapper.toResponse(ex);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(LocalizedMessageTestEnum.TEST_IN_ENGLISH_ONLY.translate());
  }

  @Test
  void testResourceNotFoundException() {
    mapper.headers = new HttpHeadersMock() {};
    var id = UUID.randomUUID();
    var ex = new ResourceNotFoundException(Object.class, id);
    var response = mapper.toResponse(ex);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(CommonErrorCode.NOT_FOUND.translate(Object.class.getSimpleName(), id));
  }

  @Test
  void testUnauthorizedExceptionJSON() {
    mapper.headers = new HttpHeadersMock() {
      @Override
      public List<MediaType> getAcceptableMediaTypes() {
        return List.of(MediaType.APPLICATION_JSON_TYPE);
      }
    };
    var ex = new UnauthorizedException();
    var response = mapper.toResponse(ex);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(
        ErrorMapperUtils.JSON_MESSAGE.formatted(
            UNAUTHORIZED_TITLE.translate(),
            UNAUTHORIZED_MESSAGE.translate(),
            DOCUMENTATION_API_LINK.translate()
        )
    );
  }

  @Test
  void testUnauthorizedExceptionHTML() {
    mapper.headers = new HttpHeadersMock() {
      @Override
      public List<MediaType> getAcceptableMediaTypes() {
        return List.of(MediaType.TEXT_HTML_TYPE);
      }
    };
    var ex = new UnauthorizedException();
    var response = mapper.toResponse(ex);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(
        ErrorMapperUtils.HTML_MESSAGE.formatted(
            UNAUTHORIZED_TITLE.translate(),
            UNAUTHORIZED_MESSAGE.translate(),
            DOCUMENTATION_API_LINK.translate()
        )
    );
  }

  @Test
  void testForbiddenExceptionJSON() {
    mapper.headers = new HttpHeadersMock() {
      @Override
      public List<MediaType> getAcceptableMediaTypes() {
        return List.of(MediaType.APPLICATION_JSON_TYPE);
      }
    };
    var ex = new ForbiddenException();
    var response = mapper.toResponse(ex);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(
        ErrorMapperUtils.JSON_MESSAGE.formatted(
            FORBIDDEN_TITLE.translate(),
            FORBIDDEN_MESSAGE.translate(),
            DOCUMENTATION_API_LINK.translate()
        )
    );
  }

  @Test
  void testForbiddenExceptionHTML() {
    mapper.headers = new HttpHeadersMock() {};
    var ex = new ForbiddenException();
    var response = mapper.toResponse(ex);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(
        ErrorMapperUtils.HTML_MESSAGE.formatted(
            FORBIDDEN_TITLE.translate(),
            UNAUTHORIZED_MESSAGE.translate(),
            DOCUMENTATION_API_LINK.translate()
        )
    );
  }

  @Test
  void testNotFoundExceptionJSON() {
    mapper.headers = new HttpHeadersMock() {
      @Override
      public List<MediaType> getAcceptableMediaTypes() {
        return List.of(MediaType.APPLICATION_JSON_TYPE);
      }
    };
    var ex = new NotFoundException();
    var response = mapper.toResponse(ex);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(
        ErrorMapperUtils.JSON_MESSAGE.formatted(
            NOT_FOUND_TITLE.translate(),
            NOT_FOUND_MESSAGE.translate(),
            DOCUMENTATION_API_LINK.translate()
        )
    );
  }

  @Test
  void testNotFoundExceptionHTML() {
    mapper.headers = null;
    var ex = new NotFoundException();
    var response = mapper.toResponse(ex);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(
        ErrorMapperUtils.HTML_MESSAGE.formatted(
            NOT_FOUND_TITLE.translate(),
            NOT_FOUND_MESSAGE.translate(),
            DOCUMENTATION_API_LINK.translate()
        )
    );
  }

  @Test
  void testNotAllowedException() {
    mapper.headers = new HttpHeadersMock() {
      @Override
      public List<MediaType> getAcceptableMediaTypes() {
        return List.of(MediaType.APPLICATION_JSON_TYPE);
      }
    };
    var ex = new NotAllowedException("not allowed");
    var response = mapper.toResponse(ex);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.METHOD_NOT_ALLOWED.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(
        ErrorMapperUtils.JSON_MESSAGE.formatted(
            METHOD_NOT_ALLOWED_TITLE.translate(),
            METHOD_NOT_ALLOWED_MESSAGE.translate(),
            DOCUMENTATION_API_LINK.translate()
        )
    );
  }

  @Test
  void testUnknownExceptionHTML(@TempDir File tempDir) {
    UnknownErrorFileGenerator fileGenerator = new UnknownErrorFileGenerator();
    fileGenerator.errorLogDirectoryPath = tempDir.getAbsolutePath();
    mapper = new GlobalExceptionMapper(fileGenerator);
    mapper.headers = null;
    var ex = new NullPointerException();
    var response = mapper.toResponse(ex);
    var fileName = Objects.requireNonNull(tempDir.list())[0];
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(
        ErrorMapperUtils.UNKNOWN_HTML_MESSAGE.formatted(
            INTERNAL_SERVER_ERROR_TITLE.translate(),
            INTERNAL_SERVER_ERROR_MESSAGE.translate(),
            fileName,
            DOCUMENTATION_API_LINK.translate()
        )
    );
  }
}