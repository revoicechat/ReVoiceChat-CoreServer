package fr.revoicechat.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Users quick respond to messages using emojis.
 * This lightweight interaction improves engagement,
 * reduces noise from short replies, and provides instant feedback
 * without interrupting conversation flow.
 */
public record MessageReactions(List<MessageReaction> reactions) {

  public MessageReactions add(final String emoji, final UUID user) {
    var reactions = new ArrayList<>(reactions());
    var reaction = getReaction(emoji);
    if (reaction == null) {
      var users = new HashSet<UUID>();
      users.add(user);
      reactions.add(new MessageReaction(emoji, users));
    } else {
      reaction.users.add(user);
    }
    return new MessageReactions(reactions);
  }

  public MessageReactions remove(final String emoji, final UUID user) {
    var reactions = new ArrayList<>(reactions());
    var reaction = getReaction(emoji);
    if (reaction == null) {
      return this;
    }
    reaction.users.remove(user);
    if (reaction.users.isEmpty()) {
      reactions.remove(reaction);
    }
    return new MessageReactions(reactions);
  }

  private MessageReaction getReaction(final String emoji) {
    return reactions().stream()
                      .filter(react -> Objects.equals(react.emoji, emoji))
                      .findFirst()
                      .orElse(null);
  }

  /**
   * @param emoji It can be an emoji (🥖👽🍟...), or a UUID referring to an {@link Emote}
   * @param users all user that reacted with this specific emoji
   */
  public record MessageReaction(
      String emoji,
      Set<UUID> users
  ) {}
}
