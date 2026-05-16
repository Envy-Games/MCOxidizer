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
        ContainerData data = be.getData();

        int activeLanes = 0;
        boolean changed = false;
        int energyPerLane = ModConfigs.ENERGY_PER_TICK.get();
        int maxProgress = Math.max(1, data.get(OxidizerBlockEntity.DATA_MAX_PROGRESS));

        for (int lane = 0; lane < OxidizerBlockEntity.LANE_COUNT; lane++) {
            LaneRecipe recipe = recipeForLane(inv, lane);
            if (recipe == null || !canOutput(inv, lane, recipe.output())) {
                changed |= resetProgress(data, lane);
                continue;
            }

            activeLanes++;
            if (energy.getEnergyStored() < energyPerLane) {
                continue;
            }

            energy.extractEnergy(energyPerLane, false);
            changed = true;

            int progress = data.get(lane) + 1;
            data.set(lane, progress);

            if (progress >= maxProgress) {
                finishLane(inv, lane, recipe);
                data.set(lane, 0);
            }
        }

        if (data.get(OxidizerBlockEntity.DATA_ACTIVE_LANES) != activeLanes) {
            data.set(OxidizerBlockEntity.DATA_ACTIVE_LANES, activeLanes);
            changed = true;
        }

        if (changed) {
            be.setChanged();
        }
    }

    private static LaneRecipe recipeForLane(ItemStackHandler inv, int lane) {
        ItemStack input = inv.getStackInSlot(OxidizerBlockEntity.copperSlot(lane));
        ItemStack chip = inv.getStackInSlot(OxidizerBlockEntity.chipSlot(lane));
        StageChipItem.Stage target = StageChipItem.fromStack(chip);

        if (input.isEmpty() || !OxidizerIngredients.isCopperInput(input) || target == null) {
            return null;
        }

        boolean useWax = !OxidizerIngredients.isWaxedCopperInput(input)
                && OxidizerIngredients.isWaxPrecursor(inv.getStackInSlot(OxidizerBlockEntity.waxSlot(lane)));
        boolean wantWaxed = OxidizerIngredients.isWaxedCopperInput(input) || useWax;
        ItemStack output = CopperTransform.compute(input, target, wantWaxed);

        if (output.isEmpty() || ItemStack.isSameItemSameComponents(input, output)) {
            return null;
        }
        return new LaneRecipe(output, useWax);
    }

    private static boolean resetProgress(ContainerData data, int lane) {
        if (data.get(lane) == 0) {
            return false;
        }
        data.set(lane, 0);
        return true;
    }

    private static void finishLane(ItemStackHandler inv, int lane, LaneRecipe recipe) {
        inv.extractItem(OxidizerBlockEntity.copperSlot(lane), 1, false);
        if (recipe.consumeWax()) {
            inv.extractItem(OxidizerBlockEntity.waxSlot(lane), 1, false);
        }
        insertOutput(inv, lane, recipe.output());
    }

    private static boolean canOutput(ItemStackHandler inv, int lane, ItemStack stack) {
        ItemStack current = inv.getStackInSlot(OxidizerBlockEntity.outputSlot(lane));
        if (current.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(current, stack)) {
            return false;
        }
        return current.getCount() + stack.getCount() <= current.getMaxStackSize();
    }

    private static void insertOutput(ItemStackHandler inv, int lane, ItemStack stack) {
        int slot = OxidizerBlockEntity.outputSlot(lane);
        ItemStack current = inv.getStackInSlot(slot);
        if (current.isEmpty()) {
            inv.setStackInSlot(slot, stack.copy());
        } else {
            current.grow(stack.getCount());
            inv.setStackInSlot(slot, current);
        }
    }

    private record LaneRecipe(ItemStack output, boolean consumeWax) {}
}
