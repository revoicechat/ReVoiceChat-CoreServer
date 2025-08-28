package fr.revoicechat.core.nls;

public enum ServerErrorCode implements LocalizedMessage {
  APPLICATION_DOES_NOT_ALLOW_SERVER_CREATION,
  APPLICATION_DOES_NOT_ALLOW_SERVER_DELETION;

  @Override
  public String toString() {
    return translate();
  }
}
