package net.stirdrem.overgeared.entity;

import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;

import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class ModEntities {
    public static final LazyRegistrar<EntityType<?>> ENTITY_TYPES =
            LazyRegistrar.create(BuiltInRegistries.ENTITY_TYPE, OvergearedMod.MOD_ID);

    public static final RegistryObject<EntityType<LingeringArrowEntity>> LINGERING_ARROW =
            registerEntity(EntityType.Builder.<LingeringArrowEntity>of(LingeringArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F), "lingering_arrow");


  /*  public static final RegistryObject<EntityType<ModularArrowEntity>> MODULAR_ARROW =
            ENTITY_TYPES.register("modular_arrow", () ->
                    EntityType.Builder.<ModularArrowEntity>of(ModularArrowEntity::new, MobCategory.MISC)
                            .setShouldReceiveVelocityUpdates(true)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("modular_arrow")
            );*/

    public static final RegistryObject<EntityType<UpgradeArrowEntity>> UPGRADE_ARROW =
            registerEntity(EntityType.Builder.<UpgradeArrowEntity>of(UpgradeArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F), "upgrade_arrow");

    /*public static final RegistryObject<EntityType<Arrow>> ARROW =
            ENTITY_TYPES.register("arrow", () ->
                    EntityType.Builder.<Arrow>of(Arrow::new, MobCategory.MISC)
                            .setShouldReceiveVelocityUpdates(true)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("arrow")
            );*/

    private static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(EntityType.Builder<T> builder, String entityName) {
        return registerEntity(builder, -1, entityName);
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(EntityType.Builder<T> builder, int serverTrackingRange, String entityName) {
        return ENTITY_TYPES.register(entityName, () -> {
            if (serverTrackingRange != -1)
                builder.clientTrackingRange(serverTrackingRange);

            return builder.build(entityName);
        });
    }
}
