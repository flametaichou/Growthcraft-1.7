package growthcraft.cellar.block;

import growthcraft.core.block.IDroppableBlock;
import growthcraft.core.block.IRotatableBlock;
import growthcraft.core.block.IWrenchable;
import growthcraft.core.utils.BlockFlags;
import growthcraft.core.utils.ItemUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Base class for Cellar machines and the like
 */
public abstract class BlockCellarContainer extends BlockContainer implements IDroppableBlock, IRotatableBlock, IWrenchable
{
	public BlockCellarContainer(Material material)
	{
		super(material);
	}

	/**
	 * Drops the block as an item and replaces it with air
	 *
	 * @param world - world to drop in
	 * @param x - x Coord
	 * @param y - y Coord
	 * @param z - z Coord
	 */
	public void fellBlockAsItem(World world, int x, int y, int z)
	{
		this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
		world.setBlockToAir(x, y, z);
	}

	/* IRotatableBlock */
	public boolean isRotatable(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		return false;
	}

	public void doRotateBlock(World world, int x, int y, int z, ForgeDirection side)
	{
		final int meta = world.getBlockMetadata(x, y, z);
		final ForgeDirection current = ForgeDirection.getOrientation(meta);
		ForgeDirection newDirection = current;
		if (current == side)
		{
			switch (current)
			{
				case UP:
					newDirection = ForgeDirection.NORTH;
					break;
				case DOWN:
					newDirection = ForgeDirection.SOUTH;
					break;
				case NORTH:
				case EAST:
					newDirection = ForgeDirection.UP;
					break;
				case SOUTH:
				case WEST:
					newDirection = ForgeDirection.DOWN;
					break;
				default:
					// some invalid state
					break;
			}
		}
		else
		{
			switch (current)
			{
				case UP:
					newDirection = ForgeDirection.DOWN;
					break;
				case DOWN:
					newDirection = ForgeDirection.UP;
					break;
				case WEST:
					newDirection = ForgeDirection.SOUTH;
					break;
				case EAST:
					newDirection = ForgeDirection.NORTH;
					break;
				case NORTH:
					newDirection = ForgeDirection.WEST;
					break;
				case SOUTH:
					newDirection = ForgeDirection.EAST;
					break;
				default:
					// yet another invalid state
					break;
			}
		}
		if (newDirection != current)
		{
			world.setBlockMetadataWithNotify(x, y, z, newDirection.ordinal(), BlockFlags.UPDATE_CLIENT);
		}
	}

	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection side)
	{
		if (isRotatable(world, x, y, z, side))
		{
			doRotateBlock(world, x, y, z, side);
			world.markBlockForUpdate(x, y, z);
			return true;
		}
		return false;
	}

	public boolean wrenchBlock(World world, int x, int y, int z, EntityPlayer player, ItemStack wrench)
	{
		if (player != null)
		{
			if (ItemUtils.canWrench(wrench, player, x, y, z))
			{
				if (player.isSneaking())
				{
					fellBlockAsItem(world, x, y, z);
					ItemUtils.wrenchUsed(wrench, player, x, y, z);
					return true;
				}
			}
		}
		return false;
	}

	public boolean tryWrenchItem(EntityPlayer player, World world, int x, int y, int z)
	{
		if (player == null) return false;
		final ItemStack is = player.inventory.getCurrentItem();
		return wrenchBlock(world, x, y, z, player, is);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6)
	{
		final TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof IInventory)
		{
			final IInventory inventory = (IInventory)te;

			if (inventory != null)
			{
				for (int index = 0; index < inventory.getSizeInventory(); ++index)
				{
					final ItemStack stack = inventory.getStackInSlot(index);

					if (stack != null)
					{
						final float f = world.rand.nextFloat() * 0.8F + 0.1F;
						final float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
						final float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

						while (stack.stackSize > 0)
						{
							int k1 = world.rand.nextInt(21) + 10;

							if (k1 > stack.stackSize)
							{
								k1 = stack.stackSize;
							}

							stack.stackSize -= k1;
							final EntityItem entityitem = new EntityItem(world, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(stack.getItem(), k1, stack.getItemDamage()));

							if (stack.hasTagCompound())
							{
								entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
							}

							final float f3 = 0.05F;
							entityitem.motionX = world.rand.nextGaussian() * (double)f3;
							entityitem.motionY = world.rand.nextGaussian() * (double)(f3 + 0.2F);
							entityitem.motionZ = world.rand.nextGaussian() * (double)f3;
							world.spawnEntityInWorld(entityitem);
						}
					}
				}

				world.func_147453_f(x, y, z, block);
			}
		}
		super.breakBlock(world, x, y, z, block, par6);
	}
}
