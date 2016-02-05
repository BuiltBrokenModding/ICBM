package com.builtbroken.icbm.content.crafting.station;

import com.builtbroken.icbm.content.crafting.station.small.TileSmallMissileWorkstation;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/6/2015.
 */
public class ItemBlockMissileStation extends ItemBlock
{
    public ItemBlockMissileStation(Block p_i45328_1_)
    {
        super(p_i45328_1_);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        boolean placed = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
        if (placed)
        {
            world.setBlockMetadataWithNotify(x, y, z, side, 3);
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileSmallMissileWorkstation)
            {
                switch (dir)
                {
                    case UP:
                    case DOWN:
                        ((TileSmallMissileWorkstation) tile).rotation = ForgeDirection.SOUTH;
                        break;
                    case EAST:
                    case WEST:
                    case NORTH:
                    case SOUTH:
                        ((TileSmallMissileWorkstation) tile).rotation = ForgeDirection.UP;
                        break;
                }
            }
            world.markBlockForUpdate(x, y, z);
        }
        return placed;
    }
}
