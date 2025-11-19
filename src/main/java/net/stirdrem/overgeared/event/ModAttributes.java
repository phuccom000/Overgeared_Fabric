package net.stirdrem.overgeared.event;

import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.stirdrem.overgeared.OvergearedMod;

public class ModAttributes {
    public static final LazyRegistrar<Attribute> ATTRIBUTES =
            LazyRegistrar.create(BuiltInRegistries.ATTRIBUTE, OvergearedMod.MOD_ID);

    /*public static final RegistryObject<Attribute> ATTACK_REACH =
            ATTRIBUTES.register("attack_reach",
                    () -> new RangedAttribute("attribute." + OvergearedMod.MOD_ID + ".attack_reach",
                            5.0D, 0.0D, 1024.0D)
                            .setSyncable(true));*/

}



