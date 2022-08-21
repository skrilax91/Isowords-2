package sponge.listener;

import common.IsoChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;

public class ChatListeners {
    @Listener
    public void onIsochat(PlayerChatEvent event, @First ServerPlayer sender) {
        if (IsoChat.isActivated(sender.uniqueId())) {
            if (sender.world().properties().name().endsWith("-isoworld")) {
                event.setCancelled(true);
                sender.world().players().forEach(p -> p.sendMessage(Component.text("[Isochat] " + sender.name() + ": " + event.originalMessage()).color(NamedTextColor.BLUE)));
            } else {
                IsoChat.toggle(sender.uniqueId());
            }
        }
    }
}
