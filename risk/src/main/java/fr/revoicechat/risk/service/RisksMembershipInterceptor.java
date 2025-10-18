package fr.revoicechat.risk.service;

import static fr.revoicechat.risk.nls.RiskMembershipErrorCode.RISK_MEMBERSHIP_ERROR;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.revoicechat.i18n.LocalizedMessage;
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
  private static final ThreadLocal<RiskService> holder = new ThreadLocal<>();

  @AroundInvoke
  public Object intercept(InvocationContext context) throws Exception {
    Method method = context.getMethod();
    RisksMembershipData membership = risksMembership(method);
    RiskEntity entity = membership.retriever().getConstructor().newInstance().get(context);
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
    RiskService sender = holder.get();
    if (sender == null) {
      sender = CDI.current().select(RiskService.class).get();
      holder.set(sender);
    }
    return sender;
  }

  static void setRiskService(RiskService sender) {
    holder.set(sender);
  }

  static void cleanRiskService() {
    holder.remove();
  }
}
