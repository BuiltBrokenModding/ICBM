package com.builtbroken.icbm.content.missile.tile;

import com.builtbroken.icbm.ICBM;
import com.builtbroken.icbm.api.ICBM_API;
import com.builtbroken.icbm.api.blast.IBlastTileMissile;
import com.builtbroken.icbm.api.blast.IExHandlerTileMissile;
import com.builtbroken.icbm.api.missile.IMissileItem;
import com.builtbroken.icbm.api.missile.ITileMissile;
import com.builtbroken.icbm.api.modules.IMissile;
import com.builtbroken.icbm.content.missile.client.RenderMissile;
import com.builtbroken.icbm.content.missile.entity.EntityMissile;
import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.api.explosive.IExplosiveHandler;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.framework.explosive.ExplosiveRegistry;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.render.RenderUtility;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.TileEnt;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/30/2016.
 */
public class TileCrashedMissile extends TileEnt implements IPacketIDReceiver, ITileMissile
{
    public static int DECAY_TICKS = 20 * 60 * 60; //1 hour
    /** List of blocks to mimic as part of the model */
    public static List<Block> blocksToMimic = new ArrayList();

    public static String[] renderKeys = new String[]{"missile.crashed", "missile", "entity"};

    static
    {
        blocksToMimic.add(Blocks.tallgrass);
        blocksToMimic.add(Blocks.snow_layer);
        blocksToMimic.add(Blocks.cake);
        blocksToMimic.add(ICBM_API.blockCake);
        blocksToMimic.add(Blocks.fire);
        blocksToMimic.add(Blocks.ice);
        blocksToMimic.add(Blocks.glass);
    }

    /** Missile object that defines render and blast information */
    public IMissile missile;

    private ForgeDirection attachedSide;
    /** Render rotation yaw of the entity */
    private float yaw = 0;
    /** Render rotation pitch of the entity */
    private float pitch = -90;
    /** Slightly random offset to improve render randomization */
    private Pos posOffset = new Pos();

    /** Currently blast running in the missile */
    private IBlastTileMissile blast;
    /** Called to do the blast reguardless of what the blast code is */
    private boolean doBlast = false;
    /** Cause to use if triggering the blast */
    private TriggerCause cause;

    /** Amount of time to run the engine */
    private int ticksForEngine;
    /** Amount of time to generate smoke */
    private int ticksForSmoke;

    private int ticksAlive = 0;

    private Pos misislePos = toPos().add(0.5);

    private Block block;
    private int meta;


    //TODO add engine flames for a few seconds after landing
    //TODO generate smoke for a few mins after landing

    public TileCrashedMissile()
    {
        super("missile", Material.iron);
        this.hardness = 10f;
        this.resistance = 10f;
        this.itemBlock = ItemBlockMissile.class;
        this.renderNormalBlock = false;
        this.renderTileEntity = true;
        this.isOpaque = false;
        this.bounds = new Cube(0.3, 0, 0.3, .7, .7, .7);
    }

    @Override
    public Tile newTile()
    {
        return new TileCrashedMissile();
    }

    @Override
    public ItemStack toItemStack()
    {
        return missile != null ? missile.toStack() : null;
    }

    /**
     * Called to place this tile into the world from a missile entity
     *
     * @param missile
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public static void placeFromMissile(EntityMissile missile, World world, int x, int y, int z, Pos posOffset)
    {
        //TODO if block is a fluid place an entity version of this tile instead, fixed fluid renderer
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        while (y < 255 && !blocksToMimic.contains(block) && !(block.isAir(world, x, y, z) || block.isReplaceable(world, x, y, z)))
        {
            y += 1;
            block = world.getBlock(x, y, z);
            meta = world.getBlockMetadata(x, y, z);
        }
        if (block == Blocks.lava || block == Blocks.flowing_lava)
        {
            //Missile is destroyed
            return;
        }
        if (block instanceof IFluidBlock)
        {
            Fluid fluid = ((IFluidBlock) block).getFluid();
            if (fluid != null && fluid.getTemperature(world, x, y, z) > 1000)
            {
                //Missile is destroyed
                return;
            }
        }
        if (world.setBlock(x, y, z, ICBM_API.blockCrashMissile))
        {
            ICBM.INSTANCE.logger().info(String.format("Placed missile %d@dim %dx %dy %dz", world.provider.dimensionId, x, y, z));

            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileCrashedMissile)
            {
                ((TileCrashedMissile) tile).posOffset = posOffset;
                if (blocksToMimic.contains(block))
                {
                    ((TileCrashedMissile) tile).block = block;
                    ((TileCrashedMissile) tile).meta = meta;
                    switch (missile.sideTile)
                    {
                        case 0:
                            ((TileCrashedMissile) tile).posOffset.sub(0, block.getBlockBoundsMinY(), 0);
                            break;
                        case 1:
                            ((TileCrashedMissile) tile).posOffset.add(0, block.getBlockBoundsMaxY(), 0);
                            break;
                    }
                }
                if (missile.getMissile() != null)
                {
                    ((TileCrashedMissile) tile).missile = missile.getMissile();
                }
                ((TileCrashedMissile) tile).yaw = missile.rotationYaw;
                ((TileCrashedMissile) tile).pitch = missile.rotationPitch;
                ((TileCrashedMissile) tile).cause = new TriggerCause.TriggerCauseEntity(missile);
                ((TileCrashedMissile) tile).attachedSide = ForgeDirection.getOrientation(missile.sideTile);
            }
        }
        else if (Engine.runningAsDev)
        {
            ICBM.INSTANCE.logger().error(String.format("TileCrashedMissile#placeFromMissile() Failed to place missile %d@dim %dx %dy %dz", world.provider.dimensionId, x, y, z));
        }
        missile.setDead();
    }

    @Override
    public void firstTick()
    {
        super.firstTick();
        misislePos = misislePos.add(posOffset);
    }

    @Override
    public void update()
    {
        super.update();
        if (isServer())
        {
            //TODO implement gravity
            //TODO check if block inside moves
            //TODO allow to be pushed a little bit
            //TODO on punched by entity push a little
            if (blast == null && ticks % 20 == 0)
            {
                if (missile != null && missile.getWarhead() != null && missile.getWarhead().getExplosiveStack() != null)
                {
                    IExplosiveHandler handler = ExplosiveRegistry.get(missile.getWarhead().getExplosiveStack());

                    if (doBlast || handler instanceof IExHandlerTileMissile && ((IExHandlerTileMissile) handler).doesSpawnMissileTile(missile, null))
                    {
                        IWorldChangeAction action = handler.createBlastForTrigger(oldWorld(), xi() + 0.5, yi() + 0.5, zi() + 0.5, cause, missile.getWarhead().getExplosiveSize(), missile.getWarhead().getAdditionalExplosiveData());
                        if (action != null)
                        {
                            if (action instanceof IBlastTileMissile)
                            {
                                blast = (IBlastTileMissile) action;
                            }
                            else
                            {
                                missile.getWarhead().trigger(cause, oldWorld(), xi() + 0.5, yi() + 0.5, zi() + 0.5);
                            }
                        }
                    }
                }
            }

            //Tick the explosive
            if (blast != null)
            {
                blast.tickBlast(this, missile);
                if (blast.isCompleted())
                {
                    blast = null;
                    missile.getWarhead().setExplosiveStack(null);
                }
            }
            else
            {
                ticksAlive++;
                if (ticksAlive > DECAY_TICKS)
                {
                    oldWorld().setBlockToAir(xi(), yi(), zi());
                    return;
                }
            }

            if (missile != null && missile.getEngine() != null)
            {

            }
            //TODO if block we are in is not solid fall
            //TODO if block is no full sized offset render pos and collision box
            if (ticks % 5 == 0)
            {
                if (attachedSide != null)
                {
                    Pos pos = toPos().add(attachedSide.getOpposite());
                    if (pos.isAirBlock(worldObj))
                    {
                        //attemptToFall();
                    }
                }
                else
                {
                    //attemptToFall();
                }
            }
        }
    }

    /**
     * Used to cause the block to drop a bit
     */
    protected void attemptToFall()
    {
        Pos pos = new Pos(xCoord, yCoord - 1, zCoord);
        if (pos.isAirBlock(worldObj))
        {
            EntityMissile missile = new EntityMissile(oldWorld());
            missile.setPosition(x() + 0.5, y() + 0.5, z() + 0.5);
            missile.setMissile(this.missile);
            missile.rotationYaw = yaw;
            missile.rotationPitch = pitch;
            toPos().setBlockToAir(oldWorld());
        }
        else
        {
            attachedSide = ForgeDirection.UP;
        }
    }

    @Override
    public Iterable<Cube> getCollisionBoxes(Cube intersect, Entity entity)
    {
        List<Cube> boxes = new ArrayList<>();
        boxes.add(getCollisionBounds());
        if (block != null)
        {
            AxisAlignedBB bb = block.getCollisionBoundingBoxFromPool(oldWorld(), xi(), yi(), zi());
            if (bb != null)
            {
                boxes.add(new Cube(bb).subtract(xi(), yi(), zi()));
            }
            else
            {
                Cube cube = new Cube(block.getBlockBoundsMinX(), block.getBlockBoundsMinY(), block.getBlockBoundsMinZ(), block.getBlockBoundsMaxX(), block.getBlockBoundsMaxY(), block.getBlockBoundsMaxZ());
                if (cube.isValid())
                {
                    //Not sure why but BB can go null
                    boxes.add(cube);
                }
            }
        }
        return boxes;
    }

    @Override
    public boolean onPlayerActivated(EntityPlayer player, int side, Pos hit)
    {
        //Removed wrench support
        return onPlayerRightClick(player, side, hit);
    }

    @Override
    protected boolean onPlayerRightClick(EntityPlayer player, int side, Pos hit)
    {
        if (block != null)
        {
            //TODO do collisions check for block,
        }
        if (isServer())
        {
            if (missile != null)
            {
                ItemStack stack = missile.toStack();

                if (player.inventory.addItemStackToInventory(stack))
                {
                    //stack size is only one so no need to do checks
                    toPos().setBlock(oldWorld(), block != null ? block : Blocks.air, block != null ? meta : 0);
                    player.inventoryContainer.detectAndSendChanges();
                }
            }
        }
        return true;
    }

    @Override
    public boolean removeByPlayer(EntityPlayer player, boolean willHarvest)
    {
        //TODO chance to blow up missile
        if (missile != null && willHarvest)
        {
            ItemStack stack = missile.toStack();
            InventoryUtility.dropItemStack(oldWorld(), x() + 0.5, y() + 0.5, z() + 0.5, stack, 10, 0);
        }
        if (block != null)
        {
            return oldWorld().setBlock(xi(), yi(), zi(), block, meta, 3);
        }
        return oldWorld().setBlockToAir(xi(), yi(), zi());
    }

    @Override
    public void onCollide(Entity entity)
    {
        if (isServer() && entity != null && entity.worldObj != null && missile != null && blast == null)
        {
            if (oldWorld().rand.nextFloat() <= 0.3 && missile.getWarhead() != null)
            {
                missile.getWarhead().trigger(new TriggerCause.TriggerCauseEntity(entity), oldWorld(), x() + 0.5, y() + 0.5, z() + 0.5);
            }
            else
            {
                //TODO push missile around
            }
        }
    }

    @Override
    public boolean onPlayerLeftClick(EntityPlayer player)
    {
        if (oldWorld().rand.nextFloat() <= 0.3 && missile.getWarhead() != null && blast == null)
        {
            missile.getWarhead().trigger(new TriggerCause.TriggerCauseEntity(player), oldWorld(), x() + 0.5, y() + 0.5, z() + 0.5);
        }
        else
        {
            //TODO push missile around
        }
        return false;
    }


    @Override
    public void onDestroyedByExplosion(Explosion ex)
    {
        //TODO attempt to set off warhead
        if (missile != null && missile.getWarhead() != null && blast == null)
        {
            missile.getWarhead().trigger(new TriggerCause.TriggerCauseExplosion(ex), oldWorld(), x() + 0.5, y() + 0.5, z() + 0.5);
        }
    }

    @Override
    public void onFillRain()
    {
        //TODO decrease smoke timer faster
        //TODO make pop noise like car engine cooling down
        //TODO make metal rain noise
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("missile"))
        {
            ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("missile"));
            if (stack != null)
            {
                missile = stack.getItem() instanceof IMissileItem ? ((IMissileItem) stack.getItem()).toMissile(stack) : null;
            }
        }
        yaw = nbt.getFloat("yaw");
        pitch = nbt.getFloat("pitch");
        if (nbt.hasKey("offset"))
        {
            posOffset = new Pos(nbt.getCompoundTag("offset"));
        }
        if (nbt.hasKey("block"))
        {
            block = Block.getBlockFromName(nbt.getString("block"));
            meta = nbt.getInteger("blockMeta");
        }
        ticksAlive = nbt.getInteger("ticksAlive");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (missile != null)
        {
            ItemStack stack = missile.toStack();
            nbt.setTag("missile", stack.writeToNBT(new NBTTagCompound()));
        }
        nbt.setFloat("yaw", yaw);
        nbt.setFloat("pitch", pitch);
        if (posOffset != null)
        {
            nbt.setTag("offset", posOffset.toNBT());
        }
        if (block != null)
        {
            nbt.setString("block", Block.blockRegistry.getNameForObject(block));
            nbt.setInteger("blockMeta", meta);
        }
        nbt.setInteger("ticksAlive", ticksAlive);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        ItemStack stack = ByteBufUtils.readItemStack(buf);
        missile = stack.getItem() instanceof IMissileItem ? ((IMissileItem) stack.getItem()).toMissile(stack) : null;
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        posOffset = new Pos(buf);
        if (buf.readBoolean())
        {
            block = Block.getBlockFromName(ByteBufUtils.readUTF8String(buf));
            meta = buf.readShort();
        }
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        if (missile != null)
        {
            ByteBufUtils.writeItemStack(buf, missile.toStack());
        }
        else
        {
            ByteBufUtils.writeItemStack(buf, new ItemStack(Items.stone_axe)); //random sync data, set the missile to null
        }
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        posOffset.writeByteBuf(buf);
        buf.writeBoolean(block != null);
        if (block != null)
        {
            ByteBufUtils.writeUTF8String(buf, Block.blockRegistry.getNameForObject(block));
            buf.writeShort(meta);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float deltaFrame, int pass)
    {
        GL11.glPushMatrix();
        if (missile != null)
        {
            GL11.glTranslated(pos.x() + 0.5, pos.y() + (float) (missile.getHeight() / 2.0) - (float) (missile.getHeight() / 3.0), pos.z() + 0.5);
            if (block != null)
            {
                GL11.glTranslated(0, block.getBlockBoundsMaxY(), 0);
            }
            if (posOffset != null)
            {
                GL11.glTranslated(posOffset.x(), posOffset.y(), posOffset.z());
            }
            RenderMissile.renderMissile(missile, yaw, pitch, renderKeys);
        }
        GL11.glPopMatrix();
        if (block != null)
        {
            GL11.glPushMatrix();
            GL11.glTranslated(pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5);
            RenderUtility.renderInventoryBlock(RenderUtility.getBlockRenderer(), block, meta);
            GL11.glPopMatrix();
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(int metadata, int fortune)
    {
        ArrayList<ItemStack> drops = new ArrayList();
        if (missile != null)
        {
            ItemStack m = missile.toStack();
            if (m != null && m.getItem() != null)
            {
                //TODO implement dropping scrap parts instead of full missile
                drops.add(m);
            }
        }
        return drops;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon()
    {
        return Blocks.iron_block.getIcon(0, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int meta, int side)
    {
        return Blocks.iron_block.getIcon(0, 0);
    }

    public void setTextureName(String value)
    {
        textureName = value;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
    }
}
