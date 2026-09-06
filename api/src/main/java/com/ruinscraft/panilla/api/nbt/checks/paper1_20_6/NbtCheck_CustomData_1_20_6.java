package com.ruinscraft.panilla.api.nbt.checks.paper1_20_6;

import com.ruinscraft.panilla.api.IPanilla;
import com.ruinscraft.panilla.api.config.PStrictness;
import com.ruinscraft.panilla.api.nbt.INbtTagCompound;
import com.ruinscraft.panilla.api.nbt.NbtDataType;
import com.ruinscraft.panilla.api.nbt.checks.NbtCheck;

public class NbtCheck_CustomData_1_20_6 extends NbtCheck {

    public NbtCheck_CustomData_1_20_6() {
        super("minecraft:custom_data", PStrictness.LENIENT, "weBrushJson");
    }

    @Override
    public NbtCheckResult check(INbtTagCompound tag, String itemName, IPanilla panilla) {
        INbtTagCompound customData = tag.getCompound("minecraft:custom_data");

        if (customData != null && customData.getHandle() != null) {
            if (customData.hasKey("weBrushJson") && panilla.getPConfig().preventFaweBrushNbt) {
                return NbtCheckResult.FAIL;
            }

            if (customData.hasKeyOfType("Paper.Range", NbtDataType.DOUBLE)) {
                double paperRange = customData.getDouble("Paper.Range");

                if (paperRange > 2048) {
                    return NbtCheckResult.CRITICAL;
                }
            }
        }

        if (tag.hasKey("weBrushJson") && panilla.getPConfig().preventFaweBrushNbt) {
            return NbtCheckResult.FAIL;
        }

        return NbtCheckResult.PASS;
    }

}