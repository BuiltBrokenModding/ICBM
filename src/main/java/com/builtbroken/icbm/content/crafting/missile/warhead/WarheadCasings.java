package com.builtbroken.icbm.content.crafting.missile.warhead;

import com.builtbroken.icbm.ICBM;
import com.builtbroken.icbm.content.crafting.missile.MissileModuleBuilder;

/**
 * Created by robert on 1/16/2015.
 */
public enum WarheadCasings
{
    EXPLOSIVE_MICRO(WarheadMicro.class, true),
    EXPLOSIVE_SMALL(WarheadSmall.class, true),
    EXPLOSIVE_STANDARD(WarheadStandard.class, false),
    EXPLOSIVE_MEDIUM(WarheadMedium.class, false),
    EXPLOSIVE_LARGE(WarheadLarge.class, false);

    public final Class<? extends Warhead> warhead_clazz;
    public final boolean enabled;

    private WarheadCasings(Class<? extends Warhead> warhead_clazz, boolean enabled)
    {
        this.warhead_clazz = warhead_clazz;
        this.enabled = enabled;
    }

    public static void register()
    {
        for (WarheadCasings size : values())
        {
            MissileModuleBuilder.INSTANCE.register(ICBM.DOMAIN, "warhead_" + size.name().toLowerCase(), size.warhead_clazz);
        }
    }

    public static WarheadCasings get(int i)
    {
        if(i >= 0 && i < values().length)
            return values()[i];
        return EXPLOSIVE_MICRO;
    }
}
