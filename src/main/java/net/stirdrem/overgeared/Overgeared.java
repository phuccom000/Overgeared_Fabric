package net.stirdrem.overgeared;

import net.fabricmc.api.ModInitializer;

import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItemGroups;
import net.stirdrem.overgeared.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Overgeared implements ModInitializer {
	public static final String MOD_ID = "overgeared";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModItemGroups.registerItemGroups();
	}


}