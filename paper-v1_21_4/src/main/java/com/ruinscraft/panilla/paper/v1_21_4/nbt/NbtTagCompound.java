package com.ruinscraft.panilla.paper.v1_21_4.nbt;

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
            return new NbtTagCompound(compound.getCompound("components"));
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
        return handle != null && handle.contains(key, nbtDataType.id);
    }

    @Override
    public Set<String> getKeys() {
        return handle == null ? Collections.emptySet() : handle.getAllKeys();
    }

    @Override
    public int getInt(String key) {
        return handle.getInt(key);
    }

    @Override
    public double getDouble(String key) {
        return handle.getDouble(key);
    }

    @Override
    public float getFloat(String key) {
        return handle.getFloat(key);
    }

    @Override
    public short getShort(String key) {
        return handle.getShort(key);
    }

    @Override
    public byte getByte(String key) {
        return handle.getByte(key);
    }

    @Override
    public String getString(String key) {
        return handle.getString(key);
    }

    @Override
    public int[] getIntArray(String key) {
        return handle.getIntArray(key);
    }

    @Override
    public INbtTagList getList(String key, NbtDataType nbtDataType) {
        return new NbtTagList(handle.getList(key, nbtDataType.id));
    }

    @Override
    public INbtTagList getList(String key) {
        return new NbtTagList(handle.getList(key, NbtDataType.COMPOUND.id));
    }

    @Override
    public INbtTagCompound getCompound(String key) {
        return new NbtTagCompound(handle.getCompound(key));
    }

}