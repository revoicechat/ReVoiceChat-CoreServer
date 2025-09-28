package fr.revoicechat.risk.service;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.revoicechat.risk.RisksMembership;
import fr.revoicechat.risk.RisksMembershipData;
import fr.revoicechat.risk.representation.RiskCategoryRepresentation;
import fr.revoicechat.risk.retriever.ServerIdRetriever;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import fr.revoicechat.risk.type.RiskType;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;

@QuarkusTest
class TestRisksMembershipInterceptor {

  @BeforeEach
  void setUp() {
    RisksMembershipInterceptor.cleanRiskService();
  }

  @Test
  void test() {
    var service = CDI.current().select(RisksMembershipInterceptorTestService.class).get();
    RisksMembershipInterceptor.setRiskService(new RiskServiceMock(true));
    var server = UUID.randomUUID();
    Assertions.assertThatCode(() -> service.test(server)).doesNotThrowAnyException();
  }

  @Test
  void testError() {
    var service = CDI.current().select(RisksMembershipInterceptorTestService.class).get();
    RisksMembershipInterceptor.setRiskService(new RiskServiceMock(false));
    var server = UUID.randomUUID();
    Assertions.assertThatThrownBy(() -> service.test(server)).isInstanceOf(UnauthorizedException.class);
  }

  @ApplicationScoped
  static class RisksMembershipInterceptorTestService {

    @RisksMembership
    @RisksMembershipData(risks = "TEST", retriever = ServerIdRetriever.class)
    void test(UUID server) {
      // nothing here
    }
  }

  private record RiskServiceMock(boolean hasRisk) implements RiskService {

    @Override
    public boolean hasRisk(final UUID userId, final RiskEntity entity, final RiskType riskType) {
      return hasRisk;
    }

    @Override
    public boolean hasRisk(final RiskEntity entity, final RiskType riskType) {
      return hasRisk;
    }

    @Override
    public List<RiskCategoryRepresentation> getAllRisks() {
      return List.of();
    }
  }
}