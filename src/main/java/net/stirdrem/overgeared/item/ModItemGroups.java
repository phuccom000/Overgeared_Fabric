package net.stirdrem.overgeared.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModItemGroups {
    public static final ItemGroup RUBY_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(OvergearedMod.MOD_ID, "overgeared_tab"),
            FabricItemGroup.builder().displayName(Text.translatable("creativetab.overgeared_tab"))
                    .icon(() -> new ItemStack(ModItems.STEEL_TONG)).entries((displayContext, entries) -> {
                        entries.add(ModItems.STEEL_INGOT);
                        entries.add(ModItems.CRUDE_STEEL);
                        entries.add(ModBlocks.STEEL_BLOCK);
                        entries.add(ModBlocks.SMITHING_ANVIL);
                    }).build());


    public static void registerItemGroups() {
        OvergearedMod.LOGGER.info("Registering Item Groups for " + OvergearedMod.MOD_ID);
    }
}