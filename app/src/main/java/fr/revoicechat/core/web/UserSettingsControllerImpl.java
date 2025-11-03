package fr.revoicechat.core.web;

import java.util.UUID;

import fr.revoicechat.core.service.user.UserSettingsService;
import fr.revoicechat.core.web.api.UserSettingsController;

public class UserSettingsControllerImpl implements UserSettingsController {

  private final UserSettingsService userSettingsService;

  public UserSettingsControllerImpl(final UserSettingsService userSettingsService) {
    this.userSettingsService = userSettingsService;
  }

  @Override
  public String me() {
    return userSettingsService.ofCurrentUser();
  }

  @Override
  public String ofUser(final UUID id) {
    return userSettingsService.ofUser(id);
  }

  @Override
  public String me(final String settings) {
    return userSettingsService.updateForCurrentUser(settings);
  }
}
