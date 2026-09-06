package com.ruinscraft.panilla.api.nbt.checks.paper1_20_6;

import com.ruinscraft.panilla.api.IPanilla;
import com.ruinscraft.panilla.api.config.PStrictness;
import com.ruinscraft.panilla.api.exception.FailedNbtList;
import com.ruinscraft.panilla.api.nbt.INbtTagCompound;
import com.ruinscraft.panilla.api.nbt.INbtTagList;
import com.ruinscraft.panilla.api.nbt.NbtDataType;
import com.ruinscraft.panilla.api.nbt.checks.NbtCheck;
import com.ruinscraft.panilla.api.nbt.checks.NbtChecks;

public class NbtCheck_BundleContents_1_20_6 extends NbtCheck {

    public NbtCheck_BundleContents_1_20_6() {
        super("minecraft:bundle_contents", PStrictness.LENIENT);
    }

    @Override
    public NbtCheckResult check(INbtTagCompound tag, String itemName, IPanilla panilla) {
        INbtTagList items = tag.getList(getName(), NbtDataType.COMPOUND);
        NbtCheckResult result = NbtCheckResult.PASS;

        for (int i = 0; i < items.size(); i++) {
            INbtTagCompound item = items.getCompound(i);

            if (!item.hasKey("components")) {
                continue;
            }

            INbtTagCompound components = item.getCompound("components");

            // Nested bundles are not possible in vanilla Minecraft and are a crash/DoS vector
            if (components != null && components.getHandle() != null && components.hasKey("minecraft:bundle_contents")) {
                return NbtCheckResult.CRITICAL;
            }

            FailedNbtList failedNbtList = NbtChecks.checkAll(components, itemName, panilla);

            if (failedNbtList.containsCritical()) {
                return NbtCheckResult.CRITICAL;
            }

            if (failedNbtList.findFirstNonCritical() != null) {
                result = NbtCheckResult.FAIL;
            }
        }

        return result;
    }

}