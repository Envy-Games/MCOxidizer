package com.styenvy.mcooxidizer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class OxidizerSTick {
    private OxidizerSTick() {}

    /** Full server tick for the Oxidizer, using only BE public APIs. */
    public static void tick(Level level, BlockPos pos, BlockState state, OxidizerBlockEntity be) {
        final ItemStackHandler inv = be.getInv();
        final IEnergyStorage energy = be.getEnergy();

        // Input + chip checks
        ItemStack in   = inv.getStackInSlot(0);
        ItemStack chip = inv.getStackInSlot(2);
        if (in.isEmpty() || !isCopperBlock(in) || !(chip.getItem() instanceof StageChipItem)) {
            be.getData().set(0, 0); // progress = 0
            be.setChanged();
            return;
        }

        // Compute output via helper
        final boolean wantWaxed = isWaxedInput(in) || isWaxPrecursor(inv.getStackInSlot(1));
        final ItemStack result =
                CopperTransform.compute(in, ((StageChipItem) chip.getItem()).getStage(), wantWaxed);

        if (result.isEmpty()) {
            be.getData().set(0, 0);
            be.setChanged();
            return;
        }
        if (!canOutput(inv, result)) {
            be.getData().set(0, 0);
            be.setChanged();
            return;
        }

        // Power gate
        final int ept = ModConfigs.ENERGY_PER_TICK.get();
        if (energy.getEnergyStored() < ept) return;
        energy.extractEnergy(ept, false);

        // Progress
        int progress = be.getData().get(0);
        int max      = Math.max(1, be.getData().get(1));
        progress++;
        be.getData().set(0, progress);

        // Complete craft
        if (progress >= max) {
            boolean inputWasWaxed = isWaxedInput(in);
            boolean needConsumeWax = !inputWasWaxed && isWaxPrecursor(inv.getStackInSlot(1));

            in.shrink(1);
            if (needConsumeWax) inv.extractItem(1, 1, false);

            insertOutput(inv, result);
            be.getData().set(0, 0); // reset progress
        }

        be.setChanged();
    }

    // --- local helpers to mirror BE logic without touching private members ---

    private static boolean isCopperBlock(ItemStack s) {
        // Accept BOTH unwaxed and waxed copper inputs (matches BE gate)
        return s.is(MCOxTags.COPPER_INPUTS) || s.is(MCOxTags.WAXED_COPPER_INPUTS);
    }

    private static boolean isWaxedInput(ItemStack s) {
        return s.is(MCOxTags.WAXED_COPPER_INPUTS);
    }

    private static boolean isWaxPrecursor(ItemStack s) {
        if (s.isEmpty()) return false;
        if (ModConfigs.ALLOW_HONEYCOMB.get() && s.is(Items.HONEYCOMB)) return true;
        return ModConfigs.ALLOW_OIL_TAG.get() && s.is(MCOxTags.WAX_PRECURSORS);
    }

    private static boolean canOutput(ItemStackHandler inv, ItemStack stack) {
        ItemStack cur = inv.getStackInSlot(3);
        if (cur.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(cur, stack)) return false;
        return cur.getCount() + stack.getCount() <= cur.getMaxStackSize();
    }

    private static void insertOutput(ItemStackHandler inv, ItemStack stack) {
        ItemStack cur = inv.getStackInSlot(3);
        if (cur.isEmpty()) inv.setStackInSlot(3, stack.copy());
        else {
            cur.grow(stack.getCount());
            inv.setStackInSlot(3, cur);
        }
    }
}
