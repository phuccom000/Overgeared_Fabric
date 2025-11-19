package net.stirdrem.overgeared;

import net.fabricmc.api.ClientModInitializer;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

public class OvergearedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AnvilMinigameEvents.registerClientEvents();
    }
}
