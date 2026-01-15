package com.tobiasgraski.testplugin.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tobiasgraski.testplugin.utils.DuelRequests;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PendingDuelsPage extends InteractiveCustomUIPage<PendingDuelsPage.PendingDuelsPageEventData> {

    private static final Value<String> BUTTON_LABEL_STYLE =
            Value.ref("Pages/BasicTextButton.ui", "LabelStyle");

    private static final Value<String> BUTTON_LABEL_STYLE_SELECTED =
            Value.ref("Pages/BasicTextButton.ui", "SelectedLabelStyle");

    private final List<String> visibleDuelIds = new ObjectArrayList<>();
    private String selectedDuelId;

    public PendingDuelsPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, PendingDuelsPageEventData.CODEC);
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder cmd, UIEventBuilder eventBuilder, Store<EntityStore> store) {
        cmd.append("Pages/PendingDuels.ui");

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#AcceptButton", EventData.of("Accept", "true"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#DeclineButton", EventData.of("Decline", "true"));

        buildDuelList(ref, cmd, eventBuilder, store);

        if (!visibleDuelIds.isEmpty()) {
            selectDuel(ref, visibleDuelIds.getFirst(), cmd, eventBuilder, store);
        } else {
            cmd.set("#DuelName.TextSpans", Message.raw("No pending duels"));
            cmd.set("#DuelMeta.TextSpans", Message.raw("-"));
            cmd.set("#DuelDescription.TextSpans", Message.raw("-"));
            cmd.set("#AcceptButton.Visible", false);
            cmd.set("#DeclineButton.Visible", false);
        }
    }

    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, PendingDuelsPageEventData data) {
        if (data.duelId != null) {
            var cb = new UICommandBuilder();
            var eb = new UIEventBuilder();
            selectDuel(ref, data.duelId, cb, eb, store);
            sendUpdate(cb, eb, false);
        } else if (data.accept != null) {
            if (selectedDuelId != null) {
                handleAccept(ref, store, selectedDuelId);
                var cb = new UICommandBuilder();
                var eb = new UIEventBuilder();
                buildDuelList(ref, cb, eb, store);

                if (!visibleDuelIds.isEmpty()) {
                    selectDuel(ref, visibleDuelIds.getFirst(), cb, eb, store);
                } else {
                    cb.set("#DuelName.TextSpans", Message.raw("No pending duels"));
                    cb.set("#DuelMeta.TextSpans", Message.raw("-"));
                    cb.set("#DuelDescription.TextSpans", Message.raw("-"));
                    cb.set("#AcceptButton.Visible", false);
                    cb.set("#DeclineButton.Visible", false);
                }
                sendUpdate(cb, eb, false);
            }
        } else if (data.decline != null) {
            if (selectedDuelId != null) {
                handleDecline(ref, store, selectedDuelId);
                UICommandBuilder cb = new UICommandBuilder();
                UIEventBuilder eb = new UIEventBuilder();
                buildDuelList(ref, cb, eb, store);
                if (!visibleDuelIds.isEmpty()) {
                    selectDuel(ref, visibleDuelIds.getFirst(), cb, eb, store);
                } else {
                    cb.set("#DuelName.TextSpans", Message.raw("No pending duels"));
                    cb.set("#DuelMeta.TextSpans", Message.raw("-"));
                    cb.set("#DuelDescription.TextSpans", Message.raw("-"));
                    cb.set("#AcceptButton.Visible", false);
                    cb.set("#DeclineButton.Visible", false);
                    selectedDuelId = null;
                }
                sendUpdate(cb, eb, false);
            }
        } else if (data.back != null) {

        }
    }

    private void buildDuelList(Ref<EntityStore> ref, UICommandBuilder cmd, UIEventBuilder event, Store<EntityStore> store) {
        cmd.clear("#DuelList");
        visibleDuelIds.clear();

        UUID targetUUID = playerRef.getUuid();

        DuelRequests.PendingDuel pending = DuelRequests.get(targetUUID);

        if (pending == null || pending.isExpired()) {
            DuelRequests.clear(targetUUID);
            return;
        }

        var duelId = pending.senderUuid.toString();
        visibleDuelIds.add(duelId);

        cmd.append("#DuelList", "Pages/BasicTextButton.ui");
        cmd.set("#DuelList[0].TextSpans", Message.raw(pending.senderName));
        event.addEventBinding(CustomUIEventBindingType.Activating,
                "#DuelList[0]", EventData.of("Duel", duelId));

        if (duelId.equals(selectedDuelId)) {
            cmd.set("#DuelList[0].Style", BUTTON_LABEL_STYLE_SELECTED);

//        for (int i = 0; i < visibleDuelIds.size(); i++) {
//            var duelId = visibleDuelIds.get(i);
//
//            cmd.append("#DuelList", "Pages/BasicTextButton.ui");
//
//            var label = duelId;
//
//            cmd.set("#DuelList[" + i + "].TextSpans", Message.raw(label));
//            event.addEventBinding(CustomUIEventBindingType.Activating,
//                    "#DuelList[" + i + "]", EventData.of("Duel", duelId));
//
//            if (duelId.equals(selectedDuelId)) {
//                cmd.set("#DuelList[" + i + "].Style", BUTTON_LABEL_STYLE_SELECTED);
        }
//        }

    }

    private void selectDuel(Ref<EntityStore> ref, String duelId, UICommandBuilder cmd, UIEventBuilder event, Store<EntityStore> store) {
        UUID targetUuid = playerRef.getUuid();
        DuelRequests.PendingDuel pending = DuelRequests.get(targetUuid);

        if (pending == null || pending.isExpired()) {
            cmd.set("#DuelName.TextSpans", Message.raw("No pending duels"));
            cmd.set("#DuelMeta.TextSpans", Message.raw("-"));
            cmd.set("#DuelDescription.TextSpans", Message.raw("-"));
            cmd.set("#AcceptButton.Visible", true);
            cmd.set("#DeclineButton.Visible", true);
            selectedDuelId = null;
            return;
        }

        cmd.set("#DuelName.TextSpans", Message.raw("Duel vs " + pending.senderName));
        cmd.set("#DuelMeta.TextSpans", Message.raw("Incoming request"));
        cmd.set("#DuelDescription.TextSpans", Message.raw("Click Accept to start or Decline to dismiss."));

        cmd.set("#AcceptButton.Visible", true);
        cmd.set("#DeclineButton.Visible", true);

        if (!visibleDuelIds.isEmpty()) {
            cmd.set("#DuelList[0].Style", BUTTON_LABEL_STYLE_SELECTED);
        }

        selectedDuelId = duelId;
    }

    private void handleAccept(Ref<EntityStore> ref, Store<EntityStore> store, String duelId) {
        var targetUuid = playerRef.getUuid();
        DuelRequests.PendingDuel pending = DuelRequests.get(targetUuid);
        if (pending == null) return;

        var player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    private void handleDecline(Ref<EntityStore> ref, Store<EntityStore> store, String duelId) {
        var targetUuid = playerRef.getUuid();
        DuelRequests.clear(targetUuid);

        var senderRef = Universe.get().getPlayer(UUID.fromString(duelId));

        if (senderRef != null) {
            senderRef.sendMessage(Message.raw(playerRef.getUsername() + " declined your duel request."));
        }

        var player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    public static class PendingDuelsPageEventData {
        static final String KEY_DUEL = "Duel";
        static final String KEY_ACCEPT = "Accept";
        static final String KEY_DECLINE = "Decline";
        static final String KEY_BACK = "Back";

        public static final BuilderCodec<PendingDuelsPageEventData> CODEC;

        private String duelId;
        private String accept;
        private String decline;
        private String back;

        static {
            final BuilderCodec.Builder<PendingDuelsPageEventData> b =
                    BuilderCodec.builder(PendingDuelsPageEventData.class, PendingDuelsPageEventData::new);

            b.addField(new KeyedCodec(KEY_DUEL, Codec.STRING),
                    (PendingDuelsPageEventData e, String s) -> e.duelId = s,
                    (PendingDuelsPageEventData e) -> e.duelId
            );

            b.addField(new KeyedCodec(KEY_ACCEPT, Codec.STRING),
                    (PendingDuelsPageEventData e, String s) -> e.accept = s,
                    (PendingDuelsPageEventData e) -> e.accept
            );

            b.addField(new KeyedCodec(KEY_DECLINE, Codec.STRING),
                    (PendingDuelsPageEventData e, String s) -> e.decline = s,
                    (PendingDuelsPageEventData e) -> e.decline
            );

            b.addField(new KeyedCodec(KEY_BACK, Codec.STRING),
                    (PendingDuelsPageEventData e, String s) -> e.back = s,
                    (PendingDuelsPageEventData e) -> e.back
            );

            CODEC = b.build();
        }
    }
}

