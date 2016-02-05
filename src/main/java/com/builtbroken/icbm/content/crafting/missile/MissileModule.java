package com.builtbroken.icbm.content.crafting.missile;

import com.builtbroken.icbm.content.crafting.AbstractModule;
import com.builtbroken.icbm.content.missile.EntityMissile;
import net.minecraft.item.ItemStack;

/**
 * Prefab for any module that can fit on a missile
 * Created by robert on 12/28/2014.
 */
public class MissileModule extends AbstractModule
{
    public MissileModule(ItemStack item, String name)
    {
        super(item, name);
    }

    public void update(EntityMissile missile)
    {

    }
}
