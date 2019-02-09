package net.minecraft.inventory;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.MathHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceExtractEvent;

public class SlotFurnaceOutput extends Slot
{
    private final EntityPlayer player;
    private int removeCount;

    public SlotFurnaceOutput(EntityPlayer player, IInventory inventoryIn, int slotIndex, int xPosition, int yPosition)
    {
        super(inventoryIn, slotIndex, xPosition, yPosition);
        this.player = player;
    }

    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }

    public ItemStack decrStackSize(int amount)
    {
        if (this.getHasStack())
        {
            this.removeCount += Math.min(amount, this.getStack().getCount());
        }

        return super.decrStackSize(amount);
    }

    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
    {
        this.onCrafting(stack);
        super.onTake(thePlayer, stack);
        return stack;
    }

    protected void onCrafting(ItemStack stack, int amount)
    {
        this.removeCount += amount;
        this.onCrafting(stack);
    }

    protected void onCrafting(ItemStack stack)
    {
        stack.onCrafting(this.player.world, this.player, this.removeCount);

        if (!this.player.world.isRemote)
        {
            int i = this.removeCount;
            float f = FurnaceRecipes.instance().getSmeltingExperience(stack);

            if (f == 0.0F)
            {
                i = 0;
            }
            else if (f < 1.0F)
            {
                int j = MathHelper.floor((float)i * f);

                if (j < MathHelper.ceil((float)i * f) && Math.random() < (double)((float)i * f - (float)j))
                {
                    ++j;
                }

                i = j;
            }

            Player player = (Player) this.player.getBukkitEntity();
            TileEntityFurnace furnace = ((TileEntityFurnace) this.inventory);
            org.bukkit.block.Block block = this.player.world.getWorld().getBlockAt(furnace.getPos().getX(), furnace.getPos().getY(), furnace.getPos().getZ());

            if (removeCount != 0) {
                FurnaceExtractEvent event = new FurnaceExtractEvent(player, block, org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(stack.getItem()), removeCount, i);
                this.player.world.getServer().getPluginManager().callEvent(event);
                i = event.getExpToDrop();
            }

            while (i > 0)
            {
                int k = EntityXPOrb.getXPSplit(i);
                i -= k;
                this.player.world.spawnEntity(new EntityXPOrb(this.player.world, this.player.posX, this.player.posY + 0.5D, this.player.posZ + 0.5D, k));
            }
        }

        this.removeCount = 0;
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerSmeltedEvent(player, stack);
    }
}