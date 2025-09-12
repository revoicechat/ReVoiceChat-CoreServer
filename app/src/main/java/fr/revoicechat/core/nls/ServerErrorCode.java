package fr.revoicechat.core.nls;

import fr.revoicechat.i18n.LocalizedMessage;

public enum ServerErrorCode implements LocalizedMessage {
  APPLICATION_DOES_NOT_ALLOW_SERVER_CREATION,
  APPLICATION_DOES_NOT_ALLOW_SERVER_DELETION,
  SERVER_STRUCTURE_WITH_ROOM_THAT_DOES_NOT_EXISTS;

  @Override
  public String toString() {
    return translate();
  }
}
