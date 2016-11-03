package com.builtbroken.icbm.content.rail.entity;

import com.builtbroken.icbm.ICBM;
import com.builtbroken.icbm.content.crafting.missile.casing.Missile;
import com.builtbroken.icbm.content.rail.BlockRail;
import com.builtbroken.icbm.content.rail.IMissileRail;
import com.builtbroken.mc.lib.helper.MathUtility;
import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.entity.EntityBase;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/29/2016.
 */
public class EntityCart extends EntityBase
{
    public static final int TYPE_DATA_ID = 23;
    //Thing we are carrying
    private Missile cargo;

    /** Side of the cart that the rail exists */
    public ForgeDirection railSide;
    /** Direction the cart is facing */
    public ForgeDirection facingDirection = ForgeDirection.NORTH;

    /** Length of the cart */
    public float length = 3;
    /** Toggle to invalidate the collision box and have it reset next tick */
    private boolean invalidBox = false;

    public EntityCart(World world)
    {
        super(world);
        height = 0.7f;
        width = .95f;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(TYPE_DATA_ID, 0);
    }

    /**
     * Sets the type of the cart, should
     * never be run after the cart is created
     *
     * @param cartType - type of the cart
     */
    public void setType(final CartTypes cartType)
    {
        if (cartType == null)
        {
            this.dataWatcher.updateObject(TYPE_DATA_ID, 0);
        }
        else
        {
            this.dataWatcher.updateObject(TYPE_DATA_ID, cartType.ordinal());
        }
        this.width = getType().width;
        this.length = getType().length;
        invalidBox = true;
    }

    /**
     * Gets the type of cart
     *
     * @return cart type
     */
    public CartTypes getType()
    {
        return CartTypes.values()[this.dataWatcher.getWatchableObjectInt(TYPE_DATA_ID)];
    }

    @Override
    public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        invalidBox = true;
    }

    @Override
    protected void setRotation(float yaw, float pitch)
    {
        this.rotationYaw = yaw % 360.0F;
        this.rotationPitch = pitch % 360.0F;
        invalidBox = true;

        float yaw2 = (float) MathUtility.clampAngleTo180(this.rotationYaw);
        if (yaw2 >= -45 && yaw2 <= 45)
        {
            facingDirection = ForgeDirection.NORTH;
        }
        else if (yaw2 <= -135 || yaw2 >= 135)
        {
            facingDirection = ForgeDirection.SOUTH;
        }
        else if (yaw2 >= 45 && yaw2 <= 135)
        {
            facingDirection = ForgeDirection.EAST;
        }
        else if (yaw2 >= -135 && yaw2 <= -45)
        {
            facingDirection = ForgeDirection.WEST;
        }
    }

    /**
     * Updates the collision box to match it's rotation
     */
    protected void validateBoundBox()
    {
        invalidBox = false;

        float halfWidth = this.width / 2.0F;
        float halfLength = this.length / 2.0F;
        float yaw = (float) Math.abs(MathUtility.clampAngleTo180(this.rotationYaw));
        if (yaw >= 45 && yaw <= 135)
        {
            halfWidth = this.length / 2.0F;
            halfLength = this.width / 2.0F;
        }
        this.boundingBox.setBounds(
                posX - (double) halfWidth,
                posY - (double) this.yOffset + (double) this.ySize,
                posZ - (double) halfLength,

                posX + (double) halfWidth,
                posY - (double) this.yOffset + (double) this.ySize + this.height,
                posZ + (double) halfLength);
    }


    @Override
    protected void setSize(float p_70105_1_, float p_70105_2_)
    {
        //Empty to prevent issues
    }

    @Override
    public void onUpdate()
    {
        this.prevDistanceWalkedModified = this.distanceWalkedModified;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;

        final CartTypes type = getType();

        //Checks if the size changed, No change should result in zero
        if (Math.abs(type.length - length - type.width - width) > 0.1)
        {
            length = type.length;
            width = type.width;
            invalidBox = true;
        }

        //Updates the collision box
        if (invalidBox)
        {
            validateBoundBox();
        }

        //Grab rail side if null
        if (railSide == null)
        {
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
            {
                Pos pos = new Pos(this).add(side);
                Block block = pos.getBlock(worldObj);
                final TileEntity tile = pos.getTileEntity(worldObj);
                if (!(block instanceof BlockRail || tile instanceof IMissileRail))
                {
                    railSide = side;
                    break;
                }
            }
        }

        Pos pos = new Pos(this).floor();
        Block block = pos.getBlock(worldObj);
        if (block == null)
        {
            pos = pos.add(railSide);
            block = pos.getBlock(worldObj);
        }
        final TileEntity tile = pos.getTileEntity(worldObj);

        if (!worldObj.isRemote)
        {
            //Kills entity if it falls out of the world, should never happen
            if (this.posY < -64.0D)
            {
                this.kill();
                return;
            }
            //Breaks entity if its not on a track
            else if (!(block instanceof BlockRail || tile instanceof IMissileRail))
            {
                destroyCart();
                return;
            }

            //Moves the entity
            if (motionX != 0 || motionY != 0 || motionZ != 0)
            {
                moveEntity(motionX, motionY, motionZ);
            }

            if (isAirBorne)
            {
                motionY -= (9.8 / 20);
                motionY = Math.max(-2, motionY);
            }
        }
        //Updates the rail and cart position
        if (tile instanceof IMissileRail)
        {
            ((IMissileRail) tile).tickRailFromCart(this);
        }
        else
        {
            final int meta = pos.getBlockMetadata(worldObj);
            BlockRail.RailDirections railType = BlockRail.RailDirections.get(meta);
            recenterCartOnRail(railType.side, railType.facing, block.getBlockBoundsMaxY());
        }

        //Handles pushing entities out of the way
        doCollisionLogic();
    }

    /**
     * Clamps the movement of the cart and position data so the
     * cart stays on the rail.
     *
     * @param side       - side the rail is attached to
     * @param facing     - direction the movement is going into
     * @param railHeight - height of the rail the cart is on
     */
    public void recenterCartOnRail(ForgeDirection side, ForgeDirection facing, double railHeight)
    {
        if (side == ForgeDirection.UP)
        {
            if (facing == ForgeDirection.NORTH || facing == ForgeDirection.SOUTH)
            {
                motionX = 0;
                motionY = 0;
                posY = ((int) posY) + railHeight;
            }
            else if (facing == ForgeDirection.EAST || facing == ForgeDirection.WEST)
            {
                motionZ = 0;
                motionY = 0;
                posY = ((int) posY) + railHeight;
            }
        }
        //TODO fix as this will not work with the current rail logic
        else if (side == ForgeDirection.DOWN)
        {
            if (facing == ForgeDirection.NORTH || facing == ForgeDirection.SOUTH)
            {
                motionX = 0;
                motionY = 0;
                posY = ((int) posY) + 1 - railHeight;
            }
            else if (facing == ForgeDirection.EAST || facing == ForgeDirection.WEST)
            {
                motionZ = 0;
                motionY = 0;
                posY = ((int) posY) + 1 - railHeight;
            }
        }
    }

    protected void doCollisionLogic()
    {
        AxisAlignedBB box = boundingBox.expand(0.2D, 0.0D, 0.2D);

        List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, box);

        if (list != null && !list.isEmpty())
        {
            for (Object aList : list)
            {
                Entity entity = (Entity) aList;

                if (entity != this.riddenByEntity && entity.canBePushed())
                {
                    entity.applyEntityCollision(this);
                }
            }
        }
    }

    @Override
    public void applyEntityCollision(Entity entity)
    {

    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0;
    }

    public void destroyCart()
    {
        InventoryUtility.dropItemStack(new Location(this), new ItemStack(ICBM.itemMissileCart, 1, getType().ordinal()));
        setDead();
    }

    @Override
    public void moveEntity(double byX, double byY, double byZ)
    {
        this.worldObj.theProfiler.startSection("move");
        this.ySize *= 0.4F;

        //Store previous movement values
        final double prevMoveX = byX;
        final double prevMoveY = byY;
        final double prevMoveZ = byZ;

        //Get collision boxes
        //TODO adjust by attached side
        List collisionBoxes = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.getOffsetBoundingBox(0, .1, 0).addCoord(byX, byY, byZ));

        //Calculate offset for y collision
        for (Object collisionBoxe : collisionBoxes)
        {
            byY = ((AxisAlignedBB) collisionBoxe).calculateYOffset(this.boundingBox, byY);
        }
        this.boundingBox.offset(0.0D, byY, 0.0D);

        //Calculate offset for x collision
        for (Object collisionBoxe : collisionBoxes)
        {
            byX = ((AxisAlignedBB) collisionBoxe).calculateXOffset(this.boundingBox, byX);
        }
        this.boundingBox.offset(byX, 0.0D, 0.0D);

        //Calculate offset for z collision
        for (Object collisionBoxe : collisionBoxes)
        {
            byZ = ((AxisAlignedBB) collisionBoxe).calculateZOffset(this.boundingBox, byZ);
        }
        this.boundingBox.offset(0.0D, 0.0D, byZ);

        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("rest");

        this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
        this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
        this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;

        this.isCollidedHorizontally = prevMoveX != byX || prevMoveZ != byZ;
        this.isCollidedVertically = prevMoveY != byY;

        this.onGround = prevMoveY != byY && prevMoveY < 0.0D;
        this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;

        if (prevMoveX != byX)
        {
            this.motionX = 0.0D;
        }

        if (prevMoveY != byY)
        {
            this.motionY = 0.0D;
        }

        if (prevMoveZ != byZ)
        {
            this.motionZ = 0.0D;
        }

        this.worldObj.theProfiler.endSection();
    }

    @Override
    public void onEntityUpdate()
    {
        //Empty to ignore default entity code
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        setType(CartTypes.values()[nbt.getInteger("cartType")]);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("cartType", getType().ordinal());
    }

    public Missile getCargo()
    {
        return cargo;
    }

    public void setCargo(Missile cargo)
    {
        this.cargo = cargo;
    }
}