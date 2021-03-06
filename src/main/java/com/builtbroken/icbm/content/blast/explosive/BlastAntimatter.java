package com.builtbroken.icbm.content.blast.explosive;

import com.builtbroken.icbm.ICBM;
import com.builtbroken.mc.api.edit.IWorldEdit;
import com.builtbroken.mc.api.explosive.IExplosiveHandler;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.callback.PacketBlast;
import com.builtbroken.mc.imp.transform.vector.BlockPos;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.world.edit.BlockEdit;
import com.builtbroken.mc.prefab.entity.selector.EntityDistanceSelector;
import com.builtbroken.mc.prefab.explosive.blast.BlastSimplePath;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;

import java.util.List;

/**
 * Blast that will destroy everything ignoring resistance to explosives
 * as Antimatter + matter(everything) = nothing(energy but no objects). In
 * other words it doesn't actually destroy the blocks using a normal blast. Instead
 * it simple null out the block at the location.
 * Created by robert on 3/25/2015.
 */
public class BlastAntimatter extends BlastSimplePath<BlastAntimatter>
{
    public BlastAntimatter(IExplosiveHandler handler)
    {
        super(handler);
    }
    //TODO add delay secondary blast trigger that uses energy released to destroy more blocks

    @Override
    public BlockEdit changeBlock(BlockPos location)
    {
        if (location.getBlock(oldWorld) == Blocks.air)
        {
            return null;
        }
        return new BlockEdit(oldWorld, location).set(Blocks.air, 0, false, true);
    }

    @Override
    public boolean shouldPath(BlockPos location)
    {
        if (!ICBM.ANTIMATTER_BREAK_UNBREAKABLE && location.getHardness(oldWorld) < 0)
        {
            return false;
        }
        double distance = blockCenter.distance(location.xi() + 0.5, location.yi() + 0.5, location.zi() + 0.5);
        return distance <= size - 1 ||  distance <= size && oldWorld().rand.nextFloat() > 0.7;
    }

    @Override
    public void doEffectOther(boolean beforeBlocksPlaced)
    {
        if (!beforeBlocksPlaced)
        {
            //TODO wright own version of getEntitiesWithinAABB that takes a filter and cuboid(or Vector3 to Vector3)
            //TODO ensure that the entity is in line of sight
            //TODO ensure that the entity can be pathed by the explosive
            AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(x - size - 1, y - size - 1, z - size - 1, x + size + 1, y + size + 1, z + size + 1);
            List list = oldWorld.selectEntitiesWithinAABB(Entity.class, bounds, new EntityDistanceSelector(new Pos(x, y, z), size + 1, true));
            if (list != null && !list.isEmpty())
            {
                damageEntities(list, new DamageSource("antimatter").setExplosion().setDamageBypassesArmor(), 10);
            }
        }
    }

    @Override
    public void displayEffectForEdit(IWorldEdit blocks)
    {

    }

    @Override
    public void playAudioForEdit(IWorldEdit blocks)
    {

    }

    @Override
    public void doStartDisplay()
    {
        Engine.packetHandler.sendToAllAround(new PacketBlast(this, PacketBlast.BlastPacketType.PRE_BLAST_DISPLAY), this, 400);
    }

    @Override
    public void doEndDisplay()
    {
        Engine.packetHandler.sendToAllAround(new PacketBlast(this, PacketBlast.BlastPacketType.POST_BLAST_DISPLAY), this, 400);
    }
}
