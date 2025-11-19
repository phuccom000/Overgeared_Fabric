package net.stirdrem.overgeared.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModItemGroups {
    public static final ItemGroup RUBY_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(Overgeared.MOD_ID, "ruby"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.overgeared"))
                    .icon(() -> new ItemStack(ModItems.SMITHING_HAMMER)).entries((displayContext, entries) -> {
                        entries.add(ModItems.SMITHING_HAMMER);
                        entries.add(ModItems.WOODEN_TONGS);
                        entries.add(Items.DIAMOND);
                        entries.add(ModBlocks.SMITHING_ANVIL);


                    }).build());


    public static void registerItemGroups() {
        Overgeared.LOGGER.info("Registering Item Groups for " + Overgeared.MOD_ID);
    }
}