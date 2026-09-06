package com.ruinscraft.panilla.api.nbt.checks.paper1_20_6;

import com.ruinscraft.panilla.api.IPanilla;
import com.ruinscraft.panilla.api.config.PStrictness;
import com.ruinscraft.panilla.api.nbt.INbtTagCompound;
import com.ruinscraft.panilla.api.nbt.NbtDataType;
import com.ruinscraft.panilla.api.nbt.checks.NbtCheck;

public class NbtCheck_OminousBottleAmplifier_1_20_6 extends NbtCheck {

    public NbtCheck_OminousBottleAmplifier_1_20_6() {
        super("minecraft:ominous_bottle_amplifier", PStrictness.AVERAGE);
    }

    @Override
    public NbtCheckResult check(INbtTagCompound tag, String itemName, IPanilla panilla) {
        if (tag.hasKeyOfType(getName(), NbtDataType.INT)) {
            int amplifier = tag.getInt(getName());

            if (amplifier < 0 || amplifier > 4) {
                return NbtCheckResult.FAIL;
            }
        }

        return NbtCheckResult.PASS;
    }

}