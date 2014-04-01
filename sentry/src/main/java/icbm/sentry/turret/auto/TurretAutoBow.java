package icbm.sentry.turret.auto;

import icbm.sentry.interfaces.ITurret;
import icbm.sentry.interfaces.ITurretUpgrade;
import icbm.sentry.turret.block.TileTurret;
import icbm.sentry.turret.traits.SentryTraitDouble;
import icbm.sentry.turret.weapon.types.WeaponBow;

/** Automated cross bow like sentry
 * 
 * @author DarkGuardsman */
public class TurretAutoBow extends TurretAuto
{
    public TurretAutoBow(TileTurret host)
    {
        super(host);
        weaponSystem = new WeaponBow(this);
        maxCooldown = 50;
        barrelLength = 1f;
        setTrait(ITurret.SEARCH_RANGE_TRAIT, 10.0);
        setTrait(ITurret.MAX_HEALTH_TRAIT, 10.0);
    }
}
