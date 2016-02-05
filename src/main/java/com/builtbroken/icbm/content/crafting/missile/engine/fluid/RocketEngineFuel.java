package com.builtbroken.icbm.content.crafting.missile.engine.fluid;

import com.builtbroken.icbm.api.modules.IMissile;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/28/2015.
 */
public class RocketEngineFuel extends RocketEngineFluid
{
    public RocketEngineFuel(ItemStack item)
    {
        super(item, "engine.fuel", FluidContainerRegistry.BUCKET_VOLUME);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        if (resource != null && resource.getFluid().getName().equalsIgnoreCase("fuel"))
        {
            return super.fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public float getSpeed(IMissile missile)
    {
        return 0.6f;
    }

    @Override
    public float getMaxDistance(IMissile missile)
    {
        if (tank.getFluidAmount() > 0)
        {
            return 3f * tank.getFluidAmount();
        }
        return 0f;
    }

    @Override
    public void initFuel()
    {
        if (FluidRegistry.getFluid("fuel") != null)
        {
            tank.fill(new FluidStack(FluidRegistry.getFluid("fuel"), tank.getCapacity()), true);
        }
    }
}
