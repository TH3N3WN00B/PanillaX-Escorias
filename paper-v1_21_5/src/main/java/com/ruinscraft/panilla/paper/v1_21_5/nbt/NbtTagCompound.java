package com.ruinscraft.panilla.paper.v1_21_5.nbt;

import com.ruinscraft.panilla.api.nbt.INbtTagCompound;
import com.ruinscraft.panilla.api.nbt.INbtTagList;
import com.ruinscraft.panilla.api.nbt.NbtDataType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class NbtTagCompound implements INbtTagCompound {

    private final CompoundTag handle;

    public NbtTagCompound(CompoundTag handle) {
        this.handle = handle;
    }

    public static NbtTagCompound fromItemStack(ItemStack item) {
        if (item.getComponentsPatch().isEmpty()) {
            return new NbtTagCompound(new CompoundTag());
        }
        Tag tag = item.save(MinecraftServer.getServer().registryAccess());
        if (tag instanceof CompoundTag compound) {
            return new NbtTagCompound(compound.getCompoundOrEmpty("components"));
        }
        return new NbtTagCompound(new CompoundTag());
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    @Override
    public boolean hasKey(String key) {
        return handle != null && handle.contains(key);
    }

    @Override
    public boolean hasKeyOfType(String key, NbtDataType nbtDataType) {
        if (handle == null || !handle.contains(key)) return false;
        Tag tag = handle.get(key);
        return tag != null && tag.getId() == (byte) nbtDataType.id;
    }

    @Override
    public Set<String> getKeys() {
        return handle == null ? Collections.emptySet() : handle.keySet();
    }

    @Override
    public int getInt(String key) {
        return handle.getIntOr(key, 0);
    }

    @Override
    public double getDouble(String key) {
        return handle.getDoubleOr(key, 0.0D);
    }

    @Override
    public float getFloat(String key) {
        return handle.getFloatOr(key, 0.0F);
    }

    @Override
    public short getShort(String key) {
        return handle.getShortOr(key, (short) 0);
    }

    @Override
    public byte getByte(String key) {
        return handle.getByteOr(key, (byte) 0);
    }

    @Override
    public String getString(String key) {
        return handle.getStringOr(key, "");
    }

    @Override
    public int[] getIntArray(String key) {
        return handle.getIntArray(key).orElse(new int[0]);
    }

    @Override
    public INbtTagList getList(String key, NbtDataType nbtDataType) {
        return new NbtTagList(handle.getListOrEmpty(key));
    }

    @Override
    public INbtTagList getList(String key) {
        return new NbtTagList(handle.getListOrEmpty(key));
    }

    @Override
    public INbtTagCompound getCompound(String key) {
        return new NbtTagCompound(handle.getCompoundOrEmpty(key));
    }

}