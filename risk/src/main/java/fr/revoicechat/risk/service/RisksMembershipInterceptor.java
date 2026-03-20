package fr.revoicechat.risk.service;

import static fr.revoicechat.risk.nls.RiskMembershipErrorCode.*;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.revoicechat.i18n.LocalizedMessage;
import fr.revoicechat.moderation.service.SanctionService;
import fr.revoicechat.risk.RisksMembership;
import fr.revoicechat.risk.RisksMembershipData;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import fr.revoicechat.risk.type.RiskType;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@RisksMembership
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class RisksMembershipInterceptor {
  private static final ThreadLocal<RiskService> RISK_SERVICE = new ThreadLocal<>();
  private static final ThreadLocal<SanctionService> SANCTION_SERVICE = new ThreadLocal<>();

  @AroundInvoke
  public Object intercept(InvocationContext context) throws Exception {
    Method method = context.getMethod();
    RisksMembershipData membership = risksMembership(method);
    RiskEntity entity = membership.retriever().getConstructor().newInstance().get(context);
    if (getSanctionService().isSanctioned(entity.serverId(), membership.sanctionType())) {
      throw new UnauthorizedException(USER_SANCTIONED_ERROR.translate());
    }
    if (Stream.of(membership.risks())
              .map(this::toRiskType)
              .noneMatch(risk -> getRiskService().hasRisk(entity, risk))) {
      throw new UnauthorizedException(RISK_MEMBERSHIP_ERROR.translate(
          Stream.of(membership.risks())
                .map(this::toRiskType)
                .map(LocalizedMessage::translate)
                .collect(Collectors.joining("\n"))
      ));
    }
    return context.proceed();
  }

  private RiskType toRiskType(String risk) {
    return new DefaultRiskType(risk);
  }

  private RisksMembershipData risksMembership(Method method) {
    return Optional.ofNullable(method.getAnnotation(RisksMembershipData.class))
                   .orElseGet(() -> method.getDeclaringClass().getAnnotation(RisksMembershipData.class));
  }

  private static RiskService getRiskService() {
    RiskService riskService = RISK_SERVICE.get();
    if (riskService == null) {
      riskService = CDI.current().select(RiskService.class).get();
      RISK_SERVICE.set(riskService);
    }
    return riskService;
  }

  static void setRiskService(RiskService sender) {
    RISK_SERVICE.set(sender);
  }

  private static SanctionService getSanctionService() {
    SanctionService sanctionService = SANCTION_SERVICE.get();
    if (sanctionService == null) {
      sanctionService = CDI.current().select(SanctionService.class).get();
      SANCTION_SERVICE.set(sanctionService);
    }
    return sanctionService;
  }

  static void setSanctionService(SanctionService sanctionService) {
    SANCTION_SERVICE.set(sanctionService);
  }

  static void cleanServices() {
    RISK_SERVICE.remove();
    SANCTION_SERVICE.remove();
  }
}
