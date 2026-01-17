package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;

import java.awt.*;

public class CustomNotificationUtil {

    public static void sendNotification(PlayerRef ref, Message primaryMessage, Message secondaryMessage, String iconId) {
        var packetHandler = ref.getPacketHandler();
        var icon = new ItemStack(iconId, 1).toPacket();

        NotificationUtil.sendNotification(
                packetHandler,
                primaryMessage,
                secondaryMessage,
                icon);
    }

    public static void sendNotification(PlayerRef ref, String primaryMessage, Color primaryColor, String secondaryMessage, Color secondaryColor, String iconId) {
        sendNotification(ref, Message.raw(primaryMessage).color(primaryColor), Message.raw(secondaryMessage).color(secondaryColor), iconId);
    }

    public static void sendNotification(PlayerRef ref, String primaryMessage, String primaryColor, String secondaryMessage, String secondaryColor, String iconId) {
        sendNotification(ref, Message.raw(primaryMessage).color(primaryColor), Message.raw(secondaryMessage).color(secondaryColor), iconId);
    }
}
