package net.shadowmage.ancientwarfare.automation.tile.worksite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.shadowmage.ancientwarfare.core.crafting.AWCraftingManager;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.inventory.InventoryBasic;
import net.shadowmage.ancientwarfare.core.item.ItemResearchBook;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockPosition;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

public class TileAutoCrafting extends TileWorksiteBase implements IInventory, IWorkSite, ISidedInventory, IInteractableTile
{

public InventoryBasic bookSlot;
public InventoryBasic outputInventory;
public InventoryBasic resourceInventory;
public InventoryBasic outputSlot;//the templated output slot, non-pullable
public InventoryCrafting craftMatrix;//the 3x3 recipe template/matrix

int[] outputSlotIndices;
int[] resourceSlotIndices;
ItemStack[] matrixShadow = new ItemStack[9];//shadow copy of input matrix

boolean hasResourcesForNext;//set from onInventoryChanged() to check if there are enough resources in input inventory to craft the next item
boolean shouldUpdateInventory;

public TileAutoCrafting()
  {
  Container dummy = new Container()
    {
    @Override
    public boolean canInteractWith(EntityPlayer var1)
      {
      return true;
      }
    @Override
    public void onCraftMatrixChanged(IInventory par1iInventory)
      {
      onLayoutMatrixChanged(par1iInventory);
      }
    };
  craftMatrix = new InventoryCrafting(dummy, 3, 3);
  resourceInventory = new InventoryBasic(27)
    {
    @Override
    public void markDirty()
      {
      onInventoryUpdated();
      super.markDirty();
      }
    @Override
    public void setInventorySlotContents(int var1, ItemStack var2)
      {      
      super.setInventorySlotContents(var1, var2);
      if(worldObj!=null && !worldObj.isRemote)
        {
        onInventoryUpdated();
        }
      }
    };
  outputInventory = new InventoryBasic(9)
    {
    @Override
    public void markDirty()
      {
      onInventoryUpdated();
      super.markDirty();
      }
    };
  outputSlot = new InventoryBasic(1)
    {
    @Override
    public void markDirty()
      {
      onInventoryUpdated();
      super.markDirty();
      }
    };
  bookSlot = new InventoryBasic(1)
    {
    @Override
    public void markDirty()
      {
      onInventoryUpdated();
      super.markDirty();
      }
    };
  resourceSlotIndices = new int[18];
  for(int i = 0; i < 18; i++)
    {
    resourceSlotIndices[i] = i;
    }
  outputSlotIndices = new int[9];
  for(int i = 0, k = 18; i<9; i++, k++)
    {
    outputSlotIndices[i] = k;
    }
  }

private void onInventoryUpdated()
  {
  this.hasResourcesForNext = false;
  this.shouldUpdateInventory = true;
  this.markDirty();
  }

private void countResources()
  {
  ArrayList<ItemStack> compactedCraft = new ArrayList<ItemStack>();
  ItemStack stack1, stack2;
  boolean found;
  for(int i = 0;i < 9; i++)
    {
    stack1 = craftMatrix.getStackInSlot(i);
    if(stack1==null){continue;}
    found = false;
    for(ItemStack stack3 : compactedCraft)
      {
      if(InventoryTools.doItemStacksMatch(stack1, stack3))
        {
        stack3.stackSize++;
        found = true;
        break;
        }
      }
    if(!found)
      {
      stack2 = stack1.copy();
      stack2.stackSize = 1;
      compactedCraft.add(stack2);
      }
    }
  found = true;
  for(ItemStack stack3 : compactedCraft)
    {
    if(InventoryTools.getCountOf(resourceInventory, -1, stack3)<stack3.stackSize)
      {
      found = false;
      break;
      }
    }  
  if(found)
    {
    hasResourcesForNext = true;
    }
  }

public String getCrafterName()
  {
  return ItemResearchBook.getResearcherName(bookSlot.getStackInSlot(0));
  }

public final void setOwningPlayer(String name)
  {
  this.owningPlayer = name;
  }

public boolean tryCraftItem()
  {
  if(hasResourcesForNext && outputSlot.getStackInSlot(0)!=null && canHoldResult())
    {
    craftItem();
    return true;
    }
  return false; 
  }

private void craftItem()
  {
  ItemStack stack = this.outputSlot.getStackInSlot(0).copy();
  useResources();
  stack = InventoryTools.mergeItemStack(outputInventory, stack, -1);
  if(stack!=null)
    {
    inventoryOverflow.add(stack);
    }  
  countResources();
  }

private boolean canHoldResult()
  {
  ItemStack out = outputSlot.getStackInSlot(0);
  if(out==null){return false;}
  ItemStack slotStack;
  int availCount = 0;
  for(int i = 0; i< outputInventory.getSizeInventory(); i++)
    {
    slotStack = outputInventory.getStackInSlot(i);
    if(slotStack==null){return true;}
    if(InventoryTools.doItemStacksMatch(slotStack, out))
      {
      availCount+=slotStack.getMaxStackSize()-slotStack.stackSize;
      }
    if(availCount>=out.stackSize){return true;}
    }  
  return false;
  }

private void useResources()
  {
  ItemStack stack1;
  for(int i = 0;i < 9; i++)
    {
    stack1 = craftMatrix.getStackInSlot(i);
    if(stack1==null){continue;}
    InventoryTools.removeItems(resourceInventory, -1, stack1, 1);
    }
  }

@Override
public WorkType getWorkType()
  {
  return WorkType.CRAFTING;
  }

@Override
public BlockPosition getWorkBoundsMin()
  {
  return null;
  }

@Override
public BlockPosition getWorkBoundsMax()
  {
  return null;
  }

@Override
public boolean hasWorkBounds()
  {
  return false;
  }

@Override
public void readFromNBT(NBTTagCompound tag)
  {
  super.readFromNBT(tag);
  this.bookSlot.readFromNBT(tag.getCompoundTag("bookSlot"));
  this.resourceInventory.readFromNBT(tag.getCompoundTag("resourceInventory"));
  this.outputInventory.readFromNBT(tag.getCompoundTag("outputInventory"));
  this.outputSlot.readFromNBT(tag.getCompoundTag("outputSlot"));
  InventoryTools.readInventoryFromNBT(craftMatrix, tag.getCompoundTag("craftMatrix"));
  hasResourcesForNext = tag.getBoolean("hasResourcesForNext");
  shouldUpdateInventory = tag.getBoolean("shouldUpdateInventory");
  onLayoutMatrixChanged(craftMatrix);
  }

@Override
public void writeToNBT(NBTTagCompound tag)
  {
  super.writeToNBT(tag);    
  tag.setTag("bookSlot", bookSlot.writeToNBT(new NBTTagCompound()));  
  tag.setTag("resourceInventory", resourceInventory.writeToNBT(new NBTTagCompound()));
  tag.setTag("outputInventory", outputInventory.writeToNBT(new NBTTagCompound()));
  tag.setTag("outputSlot", outputSlot.writeToNBT(new NBTTagCompound()));
  tag.setTag("craftMatrix", InventoryTools.writeInventoryToNBT(craftMatrix, new NBTTagCompound()));  
  tag.setBoolean("hasResourcesForNext", hasResourcesForNext);
  tag.setBoolean("shouldUpdateInventory", shouldUpdateInventory);
  }

/***************************************INVENTORY METHODS************************************************/
private void onLayoutMatrixChanged(IInventory matrix)
  {
  this.outputSlot.setInventorySlotContents(0, AWCraftingManager.INSTANCE.findMatchingRecipe(craftMatrix, worldObj, getCrafterName()));
  this.onInventoryUpdated();
  }

@Override
public int getSizeInventory()
  {
  return 18+9;
  }

@Override
public ItemStack getStackInSlot(int slotIndex)
  {
  if(slotIndex>=18)
    {
    slotIndex-=18;
    return outputInventory.getStackInSlot(slotIndex);
    }
  return resourceInventory.getStackInSlot(slotIndex);
  }

@Override
public ItemStack decrStackSize(int slot, int amount)
  {
  if(slot>=18)
    {
    slot-=18;
    return outputInventory.decrStackSize(slot, amount);
    }
  return resourceInventory.decrStackSize(slot, amount);
  }

@Override
public ItemStack getStackInSlotOnClosing(int var1)
  {
  if(var1>=18)
    {
    var1-=18;
    return outputInventory.getStackInSlotOnClosing(var1);
    }
  return resourceInventory.getStackInSlotOnClosing(var1);
  }

@Override
public void setInventorySlotContents(int var1, ItemStack var2)
  {
  if(var1>=18)
    {
    var1-=18;
    outputInventory.setInventorySlotContents(var1, var2);
    return;
    }
  resourceInventory.setInventorySlotContents(var1, var2);
  }

@Override
public String getInventoryName()
  {
  return "aw.autocrafting";
  }

@Override
public boolean hasCustomInventoryName()
  {
  return false;
  }

@Override
public int getInventoryStackLimit()
  {
  return 64;
  }

@Override
public void markDirty()
  {
  super.markDirty();
  }

@Override
public boolean isUseableByPlayer(EntityPlayer var1)
  {
  return false;
  }

@Override
public void openInventory()
  {
  
  }

@Override
public void closeInventory()
  {
  
  }

@Override
public boolean isItemValidForSlot(int var1, ItemStack var2)
  {
  return true;
  }

@Override
public int[] getAccessibleSlotsFromSide(int side)
  {
  ForgeDirection d = ForgeDirection.getOrientation(side);  
  if(d==ForgeDirection.UP)
    {
    return resourceSlotIndices;
    }
  else if(d==ForgeDirection.DOWN)
    {
    return outputSlotIndices;
    }
  return new int[0];
  }

@Override
public boolean canInsertItem(int slot, ItemStack var2, int side)
  {
  ForgeDirection d = ForgeDirection.getOrientation(side);
  if(d==ForgeDirection.UP)
    {
    return true;//top, insert-only
    }
  return false;
  }

@Override
public boolean canExtractItem(int slot, ItemStack var2, int side)
  {
  ForgeDirection d = ForgeDirection.getOrientation(side);
  if(d==ForgeDirection.DOWN)
    {
    return true;//bottom, extract only
    }
  return false;
  }

@Override
public boolean onBlockClicked(EntityPlayer player)
  {
  if(!player.worldObj.isRemote)
    {
    //TODO validate team status?
    NetworkHandler.INSTANCE.openGui(player, NetworkHandler.GUI_WORKSITE_AUTO_CRAFT, xCoord, yCoord, zCoord);
    }
  return true;
  }

@Override
public void setBounds(BlockPosition p1, BlockPosition p2)
  {
  
  }

@Override
protected boolean processWork()
  {
  return tryCraftItem();
  }

@Override
protected boolean hasWorksiteWork()
  {
  return hasResourcesForNext && outputSlot.getStackInSlot(0)!=null;
  }

@Override
protected void updateOverflowInventory()
  {
  List<ItemStack> notMerged = new ArrayList<ItemStack>();
  Iterator<ItemStack> it = inventoryOverflow.iterator();
  ItemStack stack;
  while(it.hasNext() && (stack=it.next())!=null)
    {
    it.remove();
    stack = InventoryTools.mergeItemStack(resourceInventory, stack, -1);
    if(stack!=null)
      {
      notMerged.add(stack);
      }      
    }
  if(!notMerged.isEmpty())
    {
    inventoryOverflow.addAll(notMerged);    
    }
  }

@Override
protected void updateWorksite()
  {
  worldObj.theProfiler.startSection("CraftingInventoryCheck");
  if(shouldUpdateInventory)
    {
    hasResourcesForNext = false;
    countResources();
    shouldUpdateInventory = false;
    }
  worldObj.theProfiler.endSection();
  }

@Override
public boolean shouldRenderInPass(int pass)
  {
  return pass==0;
  }

@Override
public int getBoundsMaxWidth(){return 0;}

@Override
public int getBoundsMaxHeight(){return 0;}

@Override
public void setWorkBoundsMax(BlockPosition max){}//NOOP

@Override
public void setWorkBoundsMin(BlockPosition min){}//NOOP

@Override
public void onBoundsAdjusted(){}//NOOP

@Override
public boolean userAdjustableBlocks(){return false;}

}
