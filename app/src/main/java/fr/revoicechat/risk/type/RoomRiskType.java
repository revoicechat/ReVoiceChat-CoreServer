package fr.revoicechat.risk.type;

@RiskCategory("ROOM_RISK_TYPE")
public enum RoomRiskType implements RiskType {
  SERVER_ROOM_READ,
  SERVER_ROOM_SEND_MESSAGE,
  SERVER_ROOM_READ_MESSAGE,
}
