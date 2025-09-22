package fr.revoicechat.risk.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import fr.revoicechat.security.model.AuthenticatedUser;
import jakarta.el.MethodNotFoundException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "RVC_USER")
public class UserRoleMembership implements AuthenticatedUser {
  @Id
  private UUID id;
  @ManyToMany
  @JoinTable(name = "RVC_USER_SERVER_ROLES",
      joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"),
      inverseJoinColumns = @JoinColumn(name = "SERVER_ROLE_ID", referencedColumnName = "ID"))
  private List<ServerRoles> serverRoles;

  @Override
  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public List<ServerRoles> getServerRoles() {
    return serverRoles;
  }

  public void setServerRoles(final List<ServerRoles> serverRoles) {
    this.serverRoles = serverRoles;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserRoleMembership user)) {
      return false;
    }
    return Objects.equals(getId(), user.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override public String getDisplayName() {throw new MethodNotFoundException();}
  @Override public String getLogin() {throw new MethodNotFoundException();}
  @Override public Set<String> getRoles() {throw new MethodNotFoundException();}
}
