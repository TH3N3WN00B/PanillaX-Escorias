package com.ruinscraft.panilla.api.nbt.checks;

import com.ruinscraft.panilla.api.IPanilla;
import com.ruinscraft.panilla.api.exception.FailedNbt;
import com.ruinscraft.panilla.api.exception.FailedNbtList;
import com.ruinscraft.panilla.api.exception.NbtNotPermittedException;
import com.ruinscraft.panilla.api.nbt.INbtTagCompound;
import com.ruinscraft.panilla.api.nbt.INbtTagList;
import com.ruinscraft.panilla.api.nbt.NbtDataType;
import com.ruinscraft.panilla.api.nbt.checks.paper1_20_6.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NbtChecks {

    private static final Map<String, NbtCheck> checks = new HashMap<>();

    static {
        // 1.20.6
        register(new NbtCheck_BlockEntityData());
        register(new NbtCheck_Container());
        register(new NbtCheck_ContainerLoot());
        register(new NbtCheck_CustomModelData_1_20_6());
        register(new NbtCheck_CustomName());
        register(new NbtCheck_CustomPotionEffects_1_20_6());
        register(new NbtCheck_Fireworks());
        register(new NbtCheck_Lock());
        register(new NbtCheck_Lore());
        register(new NbtCheck_SkullOwner1_20_6());
        register(new NbtCheck_WritableBookContent());
        register(new NbtCheck_WrittenBookContent());
        register(new NbtCheck_EntityData());
        register(new NbtCheck_ChargedProjectiles_1_20_6());
        register(new NbtCheck_Enchantments_1_20_6());
        // non-vanilla
        register(new NbtCheck_CustomData_1_20_6());
        register(new NbtCheck_BundleContents_1_20_6());
        register(new NbtCheck_OminousBottleAmplifier_1_20_6());
        register(new NbtCheck_SuspiciousStewEffects_1_20_6());

        // other
        // vanilla
        register(new NbtCheck_Unbreakable());
        register(new NbtCheck_CanDestroy());
        register(new NbtCheck_CanPlaceOn());
        register(new NbtCheck_BlockEntityTag());
        register(new NbtCheck_BlockStateTag());
        register(new NbtCheck_ench());
        register(new NbtCheck_RepairCost());
        register(new NbtCheck_AttributeModifiers());
        register(new NbtCheck_CustomPotionEffects());
        register(new NbtCheck_Potion());
        register(new NbtCheck_CustomPotionColor());
        register(new NbtCheck_display());
        register(new NbtCheck_HideFlags());
        register(new NbtCheck_resolved());
        register(new NbtCheck_generation());
        register(new NbtCheck_author());
        register(new NbtCheck_title());
        register(new NbtCheck_pages());
        register(new NbtCheck_SkullOwner());
        register(new NbtCheck_SkullProfile());
        register(new NbtCheck_Explosion());
        register(new NbtCheck_Fireworks());
        register(new NbtCheck_EntityTag());
        register(new NbtCheck_BucketVariantTag());
        register(new NbtCheck_map());
        register(new NbtCheck_map_scale_direction());
        register(new NbtCheck_Decorations());
        register(new NbtCheck_Effects());
        register(new NbtCheck_CustomModelData());
        register(new NbtCheck_HasVisualFire()); // 1.17
        register(new NbtCheck_ChargedProjectiles());
        register(new NbtCheck_Items());
    }

    private static void register(NbtCheck check) {
        checks.put(check.getName(), check);
        for (String alias : check.getAliases()) checks.put(alias, check);
    }

    public static Map<String, NbtCheck> getChecks() {
        return checks;
    }

    public static void checkPacketPlayIn(int slot, INbtTagCompound tag, String nmsItemClassName, String nmsPacketClassName,
                                         IPanilla panilla) throws NbtNotPermittedException {
        List<FailedNbt> failedNbtList = checkAll(tag, nmsItemClassName, panilla);

        FailedNbt lastNonCritical = null;

        for (FailedNbt failedNbt : failedNbtList) {
            if (failedNbt.result == NbtCheck.NbtCheckResult.CRITICAL) {
                throw new NbtNotPermittedException(nmsPacketClassName, false, failedNbt, slot);
            } else if (FailedNbt.fails(failedNbt)) {
                lastNonCritical = failedNbt;
            }
        }

        if (lastNonCritical != null) {
            throw new NbtNotPermittedException(nmsPacketClassName, true, lastNonCritical, slot);
        }
    }

    public static void checkPacketPlayOut(int slot, INbtTagCompound tag, String nmsItemClassName, String nmsPacketClassName,
                                          IPanilla panilla) throws NbtNotPermittedException {
        FailedNbtList failedNbtList = checkAll(tag, nmsItemClassName, panilla);

        if (failedNbtList.containsCritical()) {
            throw new NbtNotPermittedException(nmsPacketClassName, false, failedNbtList.getCritical(), slot);
        }

        FailedNbt failedNbt = failedNbtList.findFirstNonCritical();

        if (failedNbt != null) {
            throw new NbtNotPermittedException(nmsPacketClassName, false, failedNbt, slot);
        }
    }

    private static final class NbtWalkResult {
        boolean keyThresholdMet = true;
        NbtCheck.NbtCheckResult nonFiniteResult = NbtCheck.NbtCheckResult.PASS;
    }

    private static void walkForThresholdAndNonFinite(INbtTagCompound tag, IPanilla panilla, int depth, NbtWalkResult result) {
        int maxDepth = panilla.getPConfig().maxNbtDepth;

        if (depth > maxDepth) {
            result.keyThresholdMet = false;
            return;
        }

        if (result.keyThresholdMet && tag.getNonMinecraftKeys().size() > panilla.getPConfig().maxNonMinecraftNbtKeys) {
            result.keyThresholdMet = false;
        }

        for (String key : tag.getKeys()) {
            if (result.nonFiniteResult == NbtCheck.NbtCheckResult.PASS) {
                if (tag.hasKeyOfType(key, NbtDataType.FLOAT)) {
                    if (!Float.isFinite(tag.getFloat(key))) {
                        result.nonFiniteResult = NbtCheck.NbtCheckResult.CRITICAL;
                    }
                } else if (tag.hasKeyOfType(key, NbtDataType.DOUBLE)) {
                    if (!Double.isFinite(tag.getDouble(key))) {
                        result.nonFiniteResult = NbtCheck.NbtCheckResult.CRITICAL;
                    }
                }
            }

            if (tag.hasKeyOfType(key, NbtDataType.COMPOUND)) {
                INbtTagCompound subTag = tag.getCompound(key);

                if (subTag != null && subTag.getHandle() != null) {
                    walkForThresholdAndNonFinite(subTag, panilla, depth + 1, result);
                }
            } else if (tag.hasKeyOfType(key, NbtDataType.LIST)) {
                INbtTagList list = tag.getList(key);

                for (int i = 0; i < list.size(); i++) {
                    if (list.isCompound(i)) {
                        INbtTagCompound subTag = list.getCompound(i);

                        if (subTag != null && subTag.getHandle() != null) {
                            walkForThresholdAndNonFinite(subTag, panilla, depth + 1, result);
                        }
                    }
                }
            }
        }
    }

    public static FailedNbtList checkAll(INbtTagCompound tag, String nmsItemClassName, IPanilla panilla) {
        FailedNbtList failedNbtList = new FailedNbtList();

        NbtWalkResult walkResult = new NbtWalkResult();
        walkForThresholdAndNonFinite(tag, panilla, 0, walkResult);

        if (!walkResult.keyThresholdMet) {
            failedNbtList.add(FailedNbt.FAIL_KEY_THRESHOLD);
        }

        if (walkResult.nonFiniteResult != NbtCheck.NbtCheckResult.PASS) {
            failedNbtList.add(new FailedNbt("non_finite_number", walkResult.nonFiniteResult));
        }

        for (String key : tag.getKeys()) {
            if (panilla.getPConfig().nbtWhitelist.contains(key)) {
                continue;
            }

            if (tag.hasKeyOfType(key, NbtDataType.LIST)) {
                INbtTagList list = tag.getList(key);

                if (list.size() > 128) {
                    failedNbtList.add(new FailedNbt((key), NbtCheck.NbtCheckResult.CRITICAL));
                }
            }

            NbtCheck check = checks.get(key);

            if (check == null) {
                // a non-minecraft NBT tag
                continue;
            }

            if (check.getTolerance().ordinal() > panilla.getPConfig().strictness.ordinal()) {
                continue;
            }

            NbtCheck.NbtCheckResult result = check.check(tag, nmsItemClassName, panilla);

            if (result != NbtCheck.NbtCheckResult.PASS) {
                failedNbtList.add(new FailedNbt(key, result));
            }
        }

        return failedNbtList;
    }

}
