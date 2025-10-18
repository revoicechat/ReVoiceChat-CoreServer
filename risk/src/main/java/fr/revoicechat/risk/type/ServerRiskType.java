package fr.revoicechat.risk.type;

@RiskCategory("SERVER_RISK_TYPE")
public enum ServerRiskType implements RiskType {
  SERVER_UPDATE,
  SERVER_DELETE,
  SERVER_ROOM_UPDATE,
  SERVER_ROOM_DELETE,
}
