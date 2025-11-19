package net.stirdrem.overgeared.loot;

import com.mojang.serialization.Codec;

import io.github.fabricators_of_create.porting_lib.loot.IGlobalLootModifier;
import io.github.fabricators_of_create.porting_lib.loot.PortingLibLoot;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.loot.AddItemModifier;

public class ModLootModifiers {
    public static final LazyRegistrar<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            LazyRegistrar.create(PortingLibLoot.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get(), OvergearedMod.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_ITEM =
            LOOT_MODIFIER_SERIALIZERS.register("add_item", AddItemModifier.CODEC);

    public static final RegistryObject<Codec<QualityLootModifier>> TOOL_QUALITY =
            LOOT_MODIFIER_SERIALIZERS.register("tool_quality", () -> QualityLootModifier.CODEC);

}