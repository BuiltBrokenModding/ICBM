package com.builtbroken.icbm.content.crafting.missile.casing;

import com.builtbroken.icbm.api.IModuleContainer;
import com.builtbroken.icbm.content.crafting.AbstractModule;
import com.builtbroken.icbm.content.crafting.missile.MissileModuleBuilder;
import com.builtbroken.icbm.content.crafting.missile.engine.RocketEngine;
import com.builtbroken.icbm.content.crafting.missile.guidance.Guidance;
import com.builtbroken.icbm.content.crafting.missile.warhead.Warhead;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Crafting object for the missile
 * Contains all the peaces that make up the
 * missile and allow it to function
 *
 * @author Darkguardsman
 */
public abstract class Missile extends AbstractModule implements IModuleContainer
{

    public final MissileCasings casing;

    public int body_length = 2;
    public int visual_id = 0;

    private Warhead warhead;
    private Guidance guidance;
    private RocketEngine engine;



    public Missile(ItemStack stack, MissileCasings casing)
    {
        super(stack, "missile");
        this.casing = casing;
        load(stack);
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        if (nbt.hasKey("warhead"))
        {
            setWarhead(MissileModuleBuilder.INSTANCE.buildWarhead(ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("warhead"))));
        }
        if (nbt.hasKey("engine"))
        {
            setEngine(MissileModuleBuilder.INSTANCE.buildEngine(ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("engine"))));
        }
        if (nbt.hasKey("guidance"))
        {
            setGuidance(MissileModuleBuilder.INSTANCE.buildGuidance(ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("guidance"))));
        }
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        if (getWarhead() != null)
            nbt.setTag("warhead", getWarhead().toStack().writeToNBT(new NBTTagCompound()));
        if (getEngine() != null)
            nbt.setTag("engine", getEngine().toStack().writeToNBT(new NBTTagCompound()));
        if (getGuidance() != null)
            nbt.setTag("guidance", getGuidance().toStack().writeToNBT(new NBTTagCompound()));

        return nbt;
    }

    @Override
    public boolean canInstallModule(ItemStack stack, AbstractModule module)
    {
        return module instanceof RocketEngine || module instanceof Warhead || module instanceof Guidance;
    }

    @Override
    public boolean installModule(ItemStack stack, AbstractModule module)
    {
        if (module instanceof RocketEngine && engine == null)
        {
            setEngine((RocketEngine)module);
            return getEngine() == module;
        }
        else if (module instanceof Warhead && warhead == null)
        {
            setWarhead((Warhead)module);
            return getWarhead() == module;
        }
        else if (module instanceof Guidance && guidance == null)
        {
            setGuidance((Guidance)module);
            return getGuidance() == module;
        }
        return false;
    }

    public void setWarhead(Warhead warhead)
    {
        this.warhead = warhead;
    }

    public void setGuidance(Guidance guidance)
    {
        this.guidance = guidance;
    }

    public void setEngine(RocketEngine engine)
    {
        this.engine = engine;
    }

    public Warhead getWarhead()
    {
        return warhead;
    }

    public Guidance getGuidance()
    {
        return guidance;
    }

    public RocketEngine getEngine()
    {
        return engine;
    }
}
