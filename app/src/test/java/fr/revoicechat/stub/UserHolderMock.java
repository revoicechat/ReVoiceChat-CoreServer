package fr.revoicechat.stub;

import fr.revoicechat.model.User;
import fr.revoicechat.security.UserHolder;

public class UserHolderMock implements UserHolder {
  private User user;

  @Override
  public User get() {
    return user;
  }

  public void set(final User user) {
    this.user = user;
  }
}
