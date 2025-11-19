package net.stirdrem.overgeared.event;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.github.fabricators_of_create.porting_lib.entity.events.PlayerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.OnlyResetMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.ResetMinigameS2CPacket;
import net.stirdrem.overgeared.util.ModTags;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ModEvents {
    private static final int HEATED_ITEM_CHECK_INTERVAL = 20; // 1 second
    private static int serverTick = 0;

    // … copy your static fields (owned by your class) …
    public static UUID ownerUUID = null;
    private static boolean isVisible = false;
    public static boolean minigameStarted = false;
    public static ItemStack resultItem = null;
    public static int hitsRemaining = 0;
    public static float arrowPosition = 1;
    public static float arrowSpeed = 0;
    public static float maxArrowSpeed = 0;
    public static float speedIncreasePerHit = 0;
    public static boolean movingDown = false;
    public static int perfectHits = 0;
    public static int goodHits = 0;
    public static int missedHits = 0;
    public static int perfectZoneStart = 45;
    public static int perfectZoneEnd = 55;
    public static int goodZoneStart = 35;
    public static int goodZoneEnd = 65;
    public static float zoneShrinkFactor = 0.95f;
    public static float zoneShiftAmount = 15.0f;
    public static float perfectZoneSize = perfectZoneEnd - perfectZoneStart;
    public static Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());
    public static int skillLevel = 0;

    private static final int TICKS_PER_PRINT = 1;
    private static int tickAccumulator = 0;

    private static float currentPerfectZoneSize = 0;
    private static float currentGoodZoneSize = 0;

    // ---------------------------
    // Registration method
    public static void register() {
        // Server end-tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            serverTick++;
        });

        // Player tick callback: you may use PlayerTickCallback if available, else world tick
        PlayerTickEvents.END.register(player -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            if (serverTick % HEATED_ITEM_CHECK_INTERVAL != 0) return;
            handleAnvilDistance(serverPlayer, serverPlayer.level());
        });

        // Player join/disconnect
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            resetMinigameForPlayer(player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            resetMinigameForPlayer(player);
        });

        // Server stopping
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                resetMinigameForPlayer(player);
            }
            OvergearedMod.LOGGER.info("Reset all minigames on server stop.");
        });

        // Item attribute modification
        ModifyItemAttributeModifiersCallback.EVENT.register((stack, slot, modifiers) -> {
            if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
                String quality = stack.getTag().getString("ForgingQuality");
                applyWeaponAttributes(modifiers, quality);
                applyArmorAttributes(modifiers, quality);
            }
        });

        // Tooltip injection (client side)
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            // Forging quality tooltip
            if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
                String quality = stack.getTag().getString("ForgingQuality");
                Component qualityComponent = switch (quality) {
                    case "poor"   -> Component.translatable("tooltip.overgeared.poor").withStyle(ChatFormatting.RED);
                    case "well"   -> Component.translatable("tooltip.overgeared.well").withStyle(ChatFormatting.YELLOW);
                    case "expert" -> Component.translatable("tooltip.overgeared.expert").withStyle(ChatFormatting.BLUE);
                    case "perfect"-> Component.translatable("tooltip.overgeared.perfect").withStyle(ChatFormatting.GOLD);
                    case "master" -> Component.translatable("tooltip.overgeared.master").withStyle(ChatFormatting.LIGHT_PURPLE);
                    default       -> null;
                };
                if (qualityComponent != null) {
                    lines.add(1, qualityComponent);
                }
            }

            // Polished/Heated/Failed etc
            if (stack.hasTag() && stack.getTag().contains("Polished")) {
                boolean isPolished = stack.getTag().getBoolean("Polished");
                Component polishComponent = isPolished
                        ? Component.translatable("tooltip.overgeared.polished").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)
                        : Component.translatable("tooltip.overgeared.unpolished").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC);
                lines.add(1, polishComponent);
            }
            if (stack.hasTag() && stack.getTag().contains("Heated")) {
                lines.add(1, Component.translatable("tooltip.overgeared.heated").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            }
            if (stack.hasTag() && stack.getTag().contains("failedResult")) {
                lines.add(Component.translatable("tooltip.overgeared.failedResult").withStyle(ChatFormatting.RED));
            }

            if (!ServerConfig.ENABLE_MOD_TOOLTIPS.get()) return;

            if (stack.is(Items.FLINT) && ServerConfig.GET_ROCK_USING_FLINT.get()) {
                lines.add(Component.translatable("tooltip.overgeared.flint_flavor").withStyle(ChatFormatting.GRAY));
            }
            if (stack.is(ModItems.DIAMOND_SHARD.get())) {
                lines.add(Component.translatable("tooltip.overgeared.diamond_shard").withStyle(ChatFormatting.GRAY));
            }
            if (stack.is(ModTags.Items.HEATED_METALS)) {
                lines.add(Component.translatable("tooltip.overgeared.heatedingots.tooltip").withStyle(ChatFormatting.RED));
            }
            if (stack.is(ModTags.Items.HEATABLE_METALS)) {
                lines.add(Component.translatable("tooltip.overgeared.heatablemetals.tooltip").withStyle(ChatFormatting.GRAY));
            }
            if (stack.is(ModTags.Items.HOT_ITEMS)) {
                lines.add(Component.translatable("tooltip.overgeared.hotitems.tooltip").withStyle(ChatFormatting.RED));
            }
            if (stack.is(ModTags.Items.GRINDABLE)) {
                lines.add(Component.translatable("tooltip.overgeared.grindable").withStyle(ChatFormatting.GRAY));
            }
        });
    }

    // ---------------------------
    // Utility & logic methods
    private static void handleAnvilDistance(ServerPlayer player, Level level) {
        if (AnvilMinigameEvents.hasAnvilPosition(player.getUUID())) {
            BlockPos anvilPos = AnvilMinigameEvents.getAnvilPos(player.getUUID());
            BlockEntity be = level.getBlockEntity(anvilPos);
            if (be instanceof AbstractSmithingAnvilBlockEntity) {
                double distSq = player.blockPosition().distSqr(anvilPos);
                int maxDist = ServerConfig.MAX_ANVIL_DISTANCE.get();
                if (distSq > maxDist * maxDist) {
                    resetMinigameForPlayer(player);
                }
            }
        }
    }

    private static void applyWeaponAttributes(Multimap<Attribute, AttributeModifier> modifiers, String quality) {
        double damageBonus = getDamageBonusForQuality(quality);
        double speedBonus = getSpeedBonusForQuality(quality);
        modifyAttribute(modifiers, Attributes.ATTACK_DAMAGE, damageBonus);
        modifyAttribute(modifiers, Attributes.ATTACK_SPEED, speedBonus);
    }

    private static void applyArmorAttributes(Multimap<Attribute, AttributeModifier> modifiers, String quality) {
        double armorBonus = getArmorBonusForQuality(quality);
        modifyAttribute(modifiers, Attributes.ARMOR, armorBonus);
    }

    private static void modifyAttribute(Multimap<Attribute, AttributeModifier> modifiers,
                                        Attribute attribute, double bonus) {
        modifiers.get(attribute).forEach(mod -> {
            modifiers.remove(attribute, mod);
            modifiers.put(attribute, new AttributeModifier(
                    mod.getId(),
                    mod.getName() + "_forged",
                    mod.getAmount() + bonus,
                    mod.getOperation()
            ));
        });
    }

    private static double getDamageBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "master"  -> ServerConfig.MASTER_WEAPON_DAMAGE.get();
            case "perfect" -> ServerConfig.PERFECT_WEAPON_DAMAGE.get();
            case "expert"  -> ServerConfig.EXPERT_WEAPON_DAMAGE.get();
            case "well"    -> ServerConfig.WELL_WEAPON_DAMAGE.get();
            case "poor"    -> ServerConfig.POOR_WEAPON_DAMAGE.get();
            default        -> 0.0;
        };
    }

    private static double getSpeedBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "master"  -> ServerConfig.MASTER_WEAPON_SPEED.get();
            case "perfect" -> ServerConfig.PERFECT_WEAPON_SPEED.get();
            case "expert"  -> ServerConfig.EXPERT_WEAPON_SPEED.get();
            case "well"    -> ServerConfig.WELL_WEAPON_SPEED.get();
            case "poor"    -> ServerConfig.POOR_WEAPON_SPEED.get();
            default        -> 0.0;
        };
    }

    private static double getArmorBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "master"  -> ServerConfig.MASTER_ARMOR_BONUS.get();
            case "perfect" -> ServerConfig.PERFECT_ARMOR_BONUS.get();
            case "expert"  -> ServerConfig.EXPERT_ARMOR_BONUS.get();
            case "well"    -> ServerConfig.WELL_ARMOR_BONUS.get();
            case "poor"    -> ServerConfig.POOR_ARMOR_BONUS.get();
            default        -> 0.0;
        };
    }

    public static void resetMinigameForPlayer(ServerPlayer player) {
        if (player == null) return;
        UUID playerId = player.getUUID();
        ModMessages.sendToPlayer(new OnlyResetMinigameS2CPacket(), player);
        String blueprintQuality = BlueprintQuality.PERFECT.getDisplayName();
        if (ModItemInteractEvents.playerAnvilPositions.containsKey(playerId)) {
            BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(playerId);
            BlockEntity be = player.level().getBlockEntity(anvilPos);
            if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                anvil.setProgress(0);
                anvil.setChanged();
                anvil.setMinigameOn(false);
                ModMessages.sendToPlayer(new ResetMinigameS2CPacket(anvilPos), player);
                ModItemInteractEvents.releaseAnvil(player, anvilPos);
                ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
                blueprintQuality = anvil.blueprintQuality();
            }
        }
        AnvilMinigameEvents.reset(blueprintQuality);
    }

    // Duplicate version with pos
    public static void resetMinigameForPlayer(ServerPlayer player, BlockPos anvilPos) {
        if (player == null) return;
        String quality = "perfect";
        BlockEntity be = player.level().getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.setMinigameOn(false);
            quality = anvil.blueprintQuality();
        }
        AnvilMinigameEvents.reset(quality);
        ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
        ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
    }

    public static void resetMinigameForAnvil(Level level, BlockPos anvilPos) {
        String quality = "perfect";
        BlockEntity be = level.getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.setMinigameOn(false);
            anvil.clearOwner();
            quality = anvil.blueprintQuality();
        }
        AnvilMinigameEvents.reset(quality);
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                ModMessages.sendToPlayer(new ResetMinigameS2CPacket(anvilPos), player);
                if (ModItemInteractEvents.playerAnvilPositions.getOrDefault(player.getUUID(), BlockPos.ZERO).equals(anvilPos)) {
                    ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
                    ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
                    break;
                }
            }
        }
    }
}
