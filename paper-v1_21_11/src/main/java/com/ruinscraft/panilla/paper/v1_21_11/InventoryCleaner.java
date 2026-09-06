package com.ruinscraft.panilla.paper.v1_21_11;

import com.ruinscraft.panilla.api.IInventoryCleaner;
import com.ruinscraft.panilla.api.IPanilla;
import com.ruinscraft.panilla.api.IPanillaPlayer;
import com.ruinscraft.panilla.api.exception.FailedNbt;
import com.ruinscraft.panilla.api.exception.FailedNbtList;
import com.ruinscraft.panilla.api.nbt.INbtTagCompound;
import com.ruinscraft.panilla.api.nbt.checks.NbtChecks;
import com.ruinscraft.panilla.paper.v1_21_11.nbt.NbtTagCompound;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.List;

public class InventoryCleaner implements IInventoryCleaner {

    private final IPanilla panilla;

    public InventoryCleaner(IPanilla panilla) {
        this.panilla = panilla;
    }

    @Override
    public void clean(IPanillaPlayer player) {
        CraftPlayer craftPlayer = (CraftPlayer) player.getHandle();
        Inventory container = craftPlayer.getHandle().getInventory();

        for (int slot = 0; slot < container.getContents().size(); slot++) {
            ItemStack itemStack = container.getContents().get(slot);

            if (itemStack == null || itemStack.isEmpty() || itemStack.getComponents().isEmpty()) {
                continue;
            }

            INbtTagCompound tag = NbtTagCompound.fromItemStack(itemStack);
            String itemName = itemStack.getItem().getDescriptionId();

            FailedNbtList failedNbtList = NbtChecks.checkAll(tag, itemName, panilla);

            for (FailedNbt failedNbt : failedNbtList) {
                if (FailedNbt.failsThreshold(failedNbt)) {
                    PatchedDataComponentMap map = (PatchedDataComponentMap) itemStack.getComponents();
                    List<DataComponentType<?>> types = new ArrayList<>(map.keySet());
                    for (DataComponentType<?> type : types) {
                        map.remove(type);
                    }

                    break;
                } else if (FailedNbt.fails(failedNbt)) {
                    Registry<DataComponentType<?>> registry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.DATA_COMPONENT_TYPE);
                    DataComponentType<?> type = registry.get(Identifier.parse(failedNbt.key)).map(Holder.Reference::value).orElse(null);

                    if (type != null) {
                        ((PatchedDataComponentMap) itemStack.getComponents()).remove(type);
                    }

                    break;
                }
            }
        }
    }

}