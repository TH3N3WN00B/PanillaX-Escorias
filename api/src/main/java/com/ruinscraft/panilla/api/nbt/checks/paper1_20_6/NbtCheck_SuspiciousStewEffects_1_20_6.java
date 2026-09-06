package com.ruinscraft.panilla.api.nbt.checks.paper1_20_6;

import com.ruinscraft.panilla.api.IPanilla;
import com.ruinscraft.panilla.api.config.PStrictness;
import com.ruinscraft.panilla.api.nbt.INbtTagCompound;
import com.ruinscraft.panilla.api.nbt.INbtTagList;
import com.ruinscraft.panilla.api.nbt.NbtDataType;
import com.ruinscraft.panilla.api.nbt.checks.NbtCheck;

public class NbtCheck_SuspiciousStewEffects_1_20_6 extends NbtCheck {

    private static final int MAX_DURATION = 1_000_000;

    public NbtCheck_SuspiciousStewEffects_1_20_6() {
        super("minecraft:suspicious_stew_effects", PStrictness.AVERAGE);
    }

    @Override
    public NbtCheckResult check(INbtTagCompound tag, String itemName, IPanilla panilla) {
        INbtTagList effects = tag.getList(getName(), NbtDataType.COMPOUND);

        for (int i = 0; i < effects.size(); i++) {
            INbtTagCompound effect = effects.getCompound(i);

            if (effect.hasKeyOfType("duration", NbtDataType.INT)) {
                int duration = effect.getInt("duration");

                if (duration < 0 || duration > MAX_DURATION) {
                    return NbtCheckResult.FAIL;
                }
            }
        }

        return NbtCheckResult.PASS;
    }

}