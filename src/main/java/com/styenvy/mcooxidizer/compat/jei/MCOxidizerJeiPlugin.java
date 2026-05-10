package com.styenvy.mcooxidizer.compat.jei;

import com.styenvy.mcooxidizer.MCOxidizer;
import com.styenvy.mcooxidizer.ModContent;
import com.styenvy.mcooxidizer.OxidizerScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public final class MCOxidizerJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MCOxidizer.MOD_ID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new OxidizerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        registration.addRecipes(OxidizerRecipeCategory.TYPE, OxidizerJeiRecipe.createAll());
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModContent.OXIDIZER_ITEM.get(), OxidizerRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new OxidizerRecipeTransferInfo());
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                OxidizerScreen.class,
                OxidizerScreen.JEI_CLICK_X,
                OxidizerScreen.JEI_CLICK_Y,
                OxidizerScreen.JEI_CLICK_W,
                OxidizerScreen.JEI_CLICK_H,
                OxidizerRecipeCategory.TYPE
        );
    }
}
