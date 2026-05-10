package com.styenvy.mcooxidizer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class OxidizerServerTicker {
    private OxidizerServerTicker() {}

    public static void tick(Level level, BlockPos pos, BlockState state, OxidizerBlockEntity be) {
        ItemStackHandler inv = be.getInv();
        IEnergyStorage energy = be.getEnergy();

        ItemStack input = inv.getStackInSlot(OxidizerBlockEntity.SLOT_INPUT_COPPER);
        ItemStack chip = inv.getStackInSlot(OxidizerBlockEntity.SLOT_INPUT_CHIP);
        StageChipItem.Stage target = StageChipItem.fromStack(chip);

        if (input.isEmpty() || !OxidizerIngredients.isCopperInput(input) || target == null) {
            resetProgress(be);
            return;
        }

        boolean wantWaxed = OxidizerIngredients.isWaxedCopperInput(input)
                || OxidizerIngredients.isWaxPrecursor(inv.getStackInSlot(OxidizerBlockEntity.SLOT_INPUT_WAX));
        ItemStack result = CopperTransform.compute(input, target, wantWaxed);

        if (result.isEmpty() || !canOutput(inv, result)) {
            resetProgress(be);
            return;
        }

        int energyPerTick = ModConfigs.ENERGY_PER_TICK.get();
        if (energy.getEnergyStored() < energyPerTick) {
            return;
        }
        energy.extractEnergy(energyPerTick, false);

        ContainerData data = be.getData();
        int progress = data.get(0) + 1;
        int maxProgress = Math.max(1, data.get(1));
        data.set(0, progress);

        if (progress >= maxProgress) {
            boolean consumeWax = !OxidizerIngredients.isWaxedCopperInput(input)
                    && OxidizerIngredients.isWaxPrecursor(inv.getStackInSlot(OxidizerBlockEntity.SLOT_INPUT_WAX));

            input.shrink(1);
            if (consumeWax) {
                inv.extractItem(OxidizerBlockEntity.SLOT_INPUT_WAX, 1, false);
            }

            insertOutput(inv, result);
            data.set(0, 0);
        }

        be.setChanged();
    }

    private static void resetProgress(OxidizerBlockEntity be) {
        be.getData().set(0, 0);
        be.setChanged();
    }

    private static boolean canOutput(ItemStackHandler inv, ItemStack stack) {
        ItemStack current = inv.getStackInSlot(OxidizerBlockEntity.SLOT_OUTPUT);
        if (current.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(current, stack)) {
            return false;
        }
        return current.getCount() + stack.getCount() <= current.getMaxStackSize();
    }

    private static void insertOutput(ItemStackHandler inv, ItemStack stack) {
        ItemStack current = inv.getStackInSlot(OxidizerBlockEntity.SLOT_OUTPUT);
        if (current.isEmpty()) {
            inv.setStackInSlot(OxidizerBlockEntity.SLOT_OUTPUT, stack.copy());
        } else {
            current.grow(stack.getCount());
            inv.setStackInSlot(OxidizerBlockEntity.SLOT_OUTPUT, current);
        }
    }
}
