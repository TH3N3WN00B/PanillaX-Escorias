package com.ruinscraft.panilla.paper.v1_21_5.nbt;

import com.ruinscraft.panilla.api.nbt.INbtTagCompound;
import com.ruinscraft.panilla.api.nbt.INbtTagList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class NbtTagList implements INbtTagList {

    private final ListTag handle;

    public NbtTagList(ListTag handle) {
        this.handle = handle;
    }

    @Override
    public INbtTagCompound getCompound(int index) {
        return new NbtTagCompound(handle.getCompoundOrEmpty(index));
    }

    @Override
    public String getString(int index) {
        return handle.getStringOr(index, "");
    }

    @Override
    public boolean isCompound(int index) {
        return handle.get(index) instanceof CompoundTag;
    }

    @Override
    public int size() {
        return handle.size();
    }

}