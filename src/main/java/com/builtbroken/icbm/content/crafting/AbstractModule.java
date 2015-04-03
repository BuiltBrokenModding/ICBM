package com.builtbroken.icbm.content.crafting;

import com.builtbroken.icbm.ICBM;
import com.builtbroken.icbm.api.IModuleContainer;
import com.builtbroken.icbm.content.crafting.missile.MissileModuleBuilder;
import com.builtbroken.mc.api.ISave;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

/**
 * Created by robert on 12/28/2014.
 */
public class AbstractModule implements ISave
{
    /** ItemStack that represents this module */
    protected ItemStack item;
    protected String name;

    public AbstractModule(ItemStack item, String name)
    {
        this.item = item;
        this.name = name;
    }

    public String getUnlocaizedName()
    {
        return "module." + ICBM.PREFIX + name;
    }

    /** Loads from the item's NBT */
    public final AbstractModule load()
    {
        load(item);
        return this;
    }

    /** Loads from an ItemStack's NBT, can be used to clone modules */
    public final void load(ItemStack stack)
    {
        if(stack.getTagCompound() != null)
            load(stack.getTagCompound());
    }

    public final ItemStack save()
    {
        save(item);
        return item;
    }

    /** Saves to an ItemStack's NBT, can be used to clone to an itemstakc */
    public final void save(ItemStack stack)
    {
        stack.setTagCompound(new NBTTagCompound());
        save(stack.getTagCompound());

        stack.getTagCompound().setString(ModuleBuilder.SAVE_ID, MissileModuleBuilder.INSTANCE.getID(this));
    }

    public ItemStack toStack()
    {
        save(item);
        return item;
    }

    @Override
    public void load(NBTTagCompound nbt)
    {

    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        return nbt;
    }

    /** Called when the module is installed */
    public void onAdded(IModuleContainer container)
    {

    }

    /** Called when the module is removed */
    public void onRemoved(IModuleContainer container)
    {

    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register)
    {

    }
}
