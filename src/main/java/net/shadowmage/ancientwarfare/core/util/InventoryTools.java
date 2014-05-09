package net.shadowmage.ancientwarfare.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;
import net.shadowmage.ancientwarfare.core.config.AWLog;
import net.shadowmage.ancientwarfare.core.util.InventoryTools.ComparatorItemStack.StackSortType;

public class InventoryTools
{

/**
 * Attempt to merge stack into inventory via the given side, or general all-sides merge if side <0<br>
 * Resorts to default general merge if inventory is not a sided inventory.<br>
 * Double-pass merging.  First pass attempts to merge with partial stacks.  Second pass will place
 * into empty slots if available.
 * @param inventory the inventory to merge into, must not be null
 * @param stack the stack to merge, must not be null
 * @param side or <0 for none
 * @return any remaining un-merged item, or null if completely merged
 */
public static ItemStack mergeItemStack(IInventory inventory, ItemStack stack, int side)
  {
  if(side>0 && inventory instanceof ISidedInventory)
    {    
    int[] slotIndices = ((ISidedInventory)inventory).getAccessibleSlotsFromSide(side);
    if(slotIndices==null){return null;}
    int index;
    int toMove;
    ItemStack slotStack;
    for(int i = 0; i <slotIndices.length; i++)
      {
      toMove = stack.stackSize;
      index = slotIndices[i];
      slotStack = inventory.getStackInSlot(index);
      if(doItemStacksMatch(stack, slotStack))
        {
        if(toMove > slotStack.getMaxStackSize() - slotStack.stackSize)
          {
          toMove = slotStack.getMaxStackSize() - slotStack.stackSize;
          }
        stack.stackSize-=toMove;
        slotStack.stackSize+=toMove;
        inventory.setInventorySlotContents(index, slotStack);
        inventory.markDirty();
        }      
      if(stack.stackSize<=0)//merged stack fully;
        {
        return null;
        }
      }
    if(stack.stackSize>0)
      {
      for(int i = 0; i <slotIndices.length; i++)
        {        
        index = slotIndices[i];
        slotStack = inventory.getStackInSlot(index);
        if(slotStack==null)
          {
          inventory.setInventorySlotContents(index, stack);
          inventory.markDirty();
          return null;//successful merge
          }
        }
      }
    else
      {
      return null;//successful merge
      }
    }
  else
    {
    int index;
    int toMove;
    ItemStack slotStack;
    for(int i = 0; i <inventory.getSizeInventory(); i++)
      {
      toMove = stack.stackSize;
      index = i;
      slotStack = inventory.getStackInSlot(index);
      if(doItemStacksMatch(stack, slotStack))
        {
        if(toMove > slotStack.getMaxStackSize() - slotStack.stackSize)
          {
          toMove = slotStack.getMaxStackSize() - slotStack.stackSize;
          }
        stack.stackSize-=toMove;
        slotStack.stackSize+=toMove;
        inventory.setInventorySlotContents(index, slotStack);
        inventory.markDirty();
        }      
      if(stack.stackSize<=0)//merged stack fully;
        {
        return null;
        }
      }
    if(stack.stackSize>0)
      {
      for(int i = 0; i <inventory.getSizeInventory(); i++)
        {        
        index = i;
        slotStack = inventory.getStackInSlot(index);
        if(slotStack==null)
          {
          inventory.setInventorySlotContents(index, stack);
          inventory.markDirty();
          return null;//successful merge
          }
        }
      }
    else
      {
      return null;//successful merge
      }
    }
  return stack;//partial or unsuccessful merge
  }

/**
 * Attempts to remove filter * quantity from inventory.  Returns removed item in return stack, or null if
 * no items were removed.<br>
 * Will only remove and return up to filter.getMaxStackSize() items, regardless of how many are requested.
 * @param inventory
 * @param side
 * @param toRemove
 * @return the removed item.
 */
public static ItemStack removeItems(IInventory inventory, int side, ItemStack filter, int quantity)
  {  
  if(quantity>filter.getMaxStackSize())
    {
    quantity = filter.getMaxStackSize();
    }
  ItemStack returnStack = null;
  if(side>0 && inventory instanceof ISidedInventory)
    {
    int[] slotIndices = ((ISidedInventory)inventory).getAccessibleSlotsFromSide(side);
    if(slotIndices==null){return null;}
    int index;
    int toMove;
    ItemStack slotStack;
    for(int i = 0; i <slotIndices.length; i++)
      {  
      
      index = slotIndices[i];
      slotStack = inventory.getStackInSlot(index);
      if(!doItemStacksMatch(slotStack, filter)){continue;}
      if(returnStack==null)
        {
        returnStack = new ItemStack(filter.getItem());
        returnStack.stackSize = 0;
        }
      toMove = slotStack.stackSize;
      if(toMove>quantity){toMove = quantity;}
      if(toMove + returnStack.stackSize> returnStack.getMaxStackSize()){toMove = returnStack.getMaxStackSize() - returnStack.stackSize;}
      
      returnStack.stackSize+=toMove;
      slotStack.stackSize-=toMove;
      quantity-=toMove;
      if(slotStack.stackSize<=0)
        {
        inventory.setInventorySlotContents(index, null);        
        }
      inventory.markDirty();
      if(quantity<=0)
        {
        break;
        }
      }
    }
  else
    {
    int index;
    int toMove;
    ItemStack slotStack;
    for(int i = 0; i <inventory.getSizeInventory(); i++)
      {        
      index = i;
      slotStack = inventory.getStackInSlot(index);
      if(!doItemStacksMatch(slotStack, filter)){continue;}
      if(returnStack==null)
        {
        returnStack = new ItemStack(filter.getItem());
        returnStack.stackSize = 0;
        }
      toMove = slotStack.stackSize;
      if(toMove>quantity){toMove = quantity;}
      if(toMove + returnStack.stackSize> returnStack.getMaxStackSize()){toMove = returnStack.getMaxStackSize() - returnStack.stackSize;}
      
      returnStack.stackSize+=toMove;
      slotStack.stackSize-=toMove;
      quantity-=toMove;
      if(slotStack.stackSize<=0)
        {
        inventory.setInventorySlotContents(index, null);        
        }
      inventory.markDirty();
      if(quantity<=0)
        {
        break;
        }
      }
    }  
  return returnStack;
  }

/**
 * return a count of how many slots in an inventory contain a certain item stack (any size)
 * @param inv
 * @param side
 * @param filter
 * @return
 */
public static int getNumOfSlotsContaining(IInventory inv, int side, ItemStack filter)
  {
  if(inv.getSizeInventory()<=0){return 0;}
  int count = 0;
  if(side>0 && inv instanceof ISidedInventory)
    {
    int[] slotIndices = ((ISidedInventory) inv).getAccessibleSlotsFromSide(side);
    if(slotIndices==null || slotIndices.length==0){return 0;}
    ItemStack stack;
    for(int i = 0; i < slotIndices.length; i++)
      {
      stack = inv.getStackInSlot(slotIndices[i]);
      if(stack==null){continue;}
      else if(doItemStacksMatch(filter, stack))
        {
        count++;
        }
      }
    }
  else
    {
    ItemStack stack;
    for(int i = 0; i < inv.getSizeInventory(); i++)
      {
      stack = inv.getStackInSlot(i);
      if(stack==null){continue;}
      else if(doItemStacksMatch(filter, stack))
        {
        count ++;
        }
      }
    }
  return count;
  }

/**
 * return the found count of the input item stack (checks item/meta/tag, ignores qty)<br>
 * if inv is not a sided inventory, or input side < 0, counts from entire inventory<br>
 * otherwise only returns the item count from the input side
 * @param inv
 * @param side
 * @param filter
 * @return
 */
public static int getCountOf(IInventory inv, int side, ItemStack filter)
  {
  if(inv.getSizeInventory()<=0){return 0;}
  int count = 0;
  if(side>0 && inv instanceof ISidedInventory)
    {
    int[] slotIndices = ((ISidedInventory) inv).getAccessibleSlotsFromSide(side);
    if(slotIndices==null || slotIndices.length==0){return 0;}
    ItemStack stack;
    for(int i = 0; i < slotIndices.length; i++)
      {
      stack = inv.getStackInSlot(slotIndices[i]);
      if(stack==null){continue;}
      else if(doItemStacksMatch(filter, stack))
        {
        count += stack.stackSize;
        }
      }
    }
  else
    {
    ItemStack stack;
    for(int i = 0; i < inv.getSizeInventory(); i++)
      {
      stack = inv.getStackInSlot(i);
      if(stack==null){continue;}
      else if(doItemStacksMatch(filter, stack))
        {
        count += stack.stackSize;
        }
      }
    }
  return count;
  }

/**
 * validates that stacks are the same item / damage / tag, ignores quantity
 * @param stack1
 * @param stack2
 * @return
 */
public static boolean doItemStacksMatch(ItemStack stack1, ItemStack stack2)
  {
  if(stack1==null){return stack2==null;}
  if(stack2==null){return stack1==null;}
  if(stack1.getItem()==stack2.getItem() && stack1.getItemDamage()==stack2.getItemDamage() && ItemStack.areItemStackTagsEqual(stack1, stack2))
    {
    return true;
    }
  return false;
  }

/**
 * 
 * @param stack1
 * @param stack2
 * @param matchDamage
 * @param matchNBT
 * @param useOreDictionary -- NOTE: this setting overrides damage/nbt match if set to true, and uses oredict id comparison
 * @return
 */
public static boolean doItemStacksMatch(ItemStack stack1, ItemStack stack2, boolean matchDamage, boolean matchNBT, boolean useOreDictionary)
  {
  if(stack1==null){return stack2==null;}
  if(stack2==null){return stack1==null;}
  if(matchDamage && matchNBT && !useOreDictionary)
    {
    return doItemStacksMatch(stack1, stack2);
    }
  if(stack1.getItem()==stack2.getItem())
    {
    if(useOreDictionary)
      {
      int id = OreDictionary.getOreID(stack1);
      int id2 = OreDictionary.getOreID(stack2);
      return id>0 && id2>0 && id==id2;
      }
    if(matchDamage && stack1.getItemDamage()!=stack2.getItemDamage())
      {
      return false;
      }
    if(matchNBT && !ItemStack.areItemStackTagsEqual(stack1, stack2))
      {
      return false;
      }
    return true;
    }
  return false;
  }

/**
 * drops the input itemstack into the world at the input position;
 * @param world
 * @param item
 * @param x
 * @param y
 * @param z
 */
public static void dropItemInWorld(World world, ItemStack item, double x, double y, double z)
  {
  if(item==null || world==null || world.isRemote)
    {
    return;
    }
  EntityItem entityToSpawn;
  x += world.rand.nextFloat() * 0.6f - 0.3f;
  y += world.rand.nextFloat() * 0.6f + 1 - 0.3f;
  z += world.rand.nextFloat() * 0.6f - 0.3f;
  entityToSpawn = new EntityItem(world, x, y, z, item);
  entityToSpawn.setPosition(x, y, z);
  world.spawnEntityInWorld(entityToSpawn);      
  }

public static void dropInventoryInWorld(World world, IInventory localInventory, double x, double y, double z)
  {
  if(world.isRemote)
    {
    return;
    }
  if (localInventory != null)
    {
    ItemStack stack;
    for(int i = 0; i < localInventory.getSizeInventory(); i++)
      {      
      stack = localInventory.getStackInSlotOnClosing(i);      
      if(stack==null)
        {
        continue;
        }
      dropItemInWorld(world, stack, x, y, z);      
      }
    }
  }

/**
 * Writes out the input inventory to the input nbt-tag.<br>
 * The written out inventory is suitable for reading back using
 * {@link #InventoryTools.readInventoryFromNBT(IInventory, NBTTagCompound)}
 * @param inventory
 * @param tag
 */
public static void writeInventoryToNBT(IInventory inventory, NBTTagCompound tag)
  {
  NBTTagList itemList = new NBTTagList();
  NBTTagCompound itemTag;  
  ItemStack item;
  for(int i = 0; i < inventory.getSizeInventory(); i++)
    {
    item = inventory.getStackInSlot(i);
    if(item==null){continue;}
    itemTag = new NBTTagCompound();
    item.writeToNBT(itemTag);
    itemTag.setShort("slot", (short)i);
    itemList.appendTag(itemTag);
    }  
  tag.setTag("itemList", itemList);
  }

/**
 * Reads an inventory contents into the input inventory from the given nbt-tag.<br>
 * Should only be passed nbt-tags / inventories that have been saved using
 *  {@link #InventoryTools.writeInventoryToNBT(IInventory, NBTTagCompound)} 
 * @param inventory
 * @param tag
 */
public static void readInventoryFromNBT(IInventory inventory, NBTTagCompound tag)
  {
  NBTTagList itemList = tag.getTagList("itemList", Constants.NBT.TAG_COMPOUND);  
  NBTTagCompound itemTag;  
  ItemStack item;
  int slot;
  for(int i = 0; i < itemList.tagCount(); i++)
    {
    itemTag = itemList.getCompoundTagAt(i);
    slot = itemTag.getShort("slot");
    item = ItemStack.loadItemStackFromNBT(itemTag);
    inventory.setInventorySlotContents(slot, item);
    }
  }

/**
 * Compacts an input item-stack list.<br>
 * Any partial stacks in the input list will be merged into max-stack-size stacks.<br>
 * The output list will be filled with the results of the merge, and will contain as few stacks as possible.<br>
 * This particular method is on average 2x faster than compactStackList2, and also uses less memory.
 * @param in
 * @param out
 */
public static void compactStackList(List<ItemStack> in, List<ItemStack> out)
  {
  Map<ItemStackHashWrap, Integer> map = new HashMap<ItemStackHashWrap, Integer>();  
  ItemStackHashWrap wrap;
  int count;
  for(ItemStack stack : in)
    {
    count = 0;
    wrap = new ItemStackHashWrap(stack);
    if(map.containsKey(wrap))
      {
      count = map.get(wrap);      
      }
    count+=stack.stackSize;
    map.put(wrap, count);
    }
  int qty;
  ItemStack outStack;
  for(ItemStackHashWrap wrap1 : map.keySet())
    {
    qty = map.get(wrap1);
    while(qty>0)
      {
      outStack = wrap1.createItemStack();
      outStack.stackSize = qty>outStack.getMaxStackSize() ? outStack.getMaxStackSize() : qty;
      qty-=outStack.stackSize;
      out.add(outStack);
      }
    }  
  }

/**
 * Compacts an input item-stack list.<br>
 * Any partial stacks in the input list will be merged into max-stack-size stacks.<br>
 * The output list will be filled with the results of the merge, and will contain as few stacks as possible.<br>
 * This particular method is on average 1/2 as fast as compactStackList, and also uses more memory.
 * @param in
 * @param out
 */
public static void compactStackList2(List<ItemStack> in, List<ItemStack> out)
  {
//  ItemStack inStack;
  int transfer = 0;
  int tmax;
  ItemStack copy;
  for(ItemStack inStack : in)
    {
    tmax = inStack.stackSize;    
    transfer = 0;
    for(ItemStack outStack : out)
      {
      if(!InventoryTools.doItemStacksMatch(inStack, outStack) || outStack.stackSize>=outStack.getMaxStackSize()){continue;}
      transfer = outStack.getMaxStackSize() - outStack.stackSize;
      if(transfer>tmax){transfer=tmax;}
      outStack.stackSize+=transfer;
      tmax-=transfer;
      if(tmax<=0){break;}
      }
    if(tmax>0)
      {
      copy = inStack.copy();
      copy.stackSize = tmax;
      out.add(copy);
      }
    }
  }

public static void compactStackList3(List<ItemStack> in, List<ItemStack> out)
  {
  ItemQuantityMap map = new ItemQuantityMap();
  for(ItemStack stack : in)
    {
    map.addItemStack(stack, stack.stackSize);
    }
  map.getItems(out);
  }

public static void itemCompactTest()
  {
  List<ItemStack> toCompact = new ArrayList<ItemStack>();
  for(int i = 0; i < 10; i++)
    {
    toCompact.add(new ItemStack(Blocks.cobblestone,64));
    toCompact.add(new ItemStack(Blocks.dirt,64));  
    toCompact.add(new ItemStack(Blocks.grass,64));  
    toCompact.add(new ItemStack(Items.apple,1));    
    }
  for(int i = 0; i < 20; i++)
    {
    toCompact.add(new ItemStack(Items.apple,64));
    toCompact.add(new ItemStack(Items.apple,1));    
    }
  for(int i = 0; i < 10; i++)
    {
    toCompact.add(new ItemStack(Items.fish,1));
    toCompact.add(new ItemStack(Items.apple,1));    
    }
  
  ComparatorItemStack comparator = new ComparatorItemStack(StackSortType.UNLOCALIZED_NAME,1);
  Collections.sort(toCompact, comparator);
  List<ItemStack> result = new ArrayList<ItemStack>();

  
  int runs = 100000;  
  
  for(int i = 0; i < 10; i++)
    {
    test1(toCompact, result, runs);
    }  
  for(int i = 0; i < 10; i++)
    {
    test2(toCompact, result, runs);
    }  
  for(int i = 0; i < 10; i++)
    {
    test3(toCompact, result, runs);
    }  
  }

private static void test1(List<ItemStack> toCompact, List<ItemStack> result, int runs)
  {
  int s1;
  long t1, t2, t3, m1, m2, m3;
  float tf1, tf2;
  s1 = 0; 
  t3 = 0;  
  m3 = 0; 
  
  System.gc();
  for(int i = 0; i < runs; i++)
    {
    m1 = Runtime.getRuntime().freeMemory();    
    t1 = System.nanoTime();
    compactStackList(toCompact, result);
    t2 = System.nanoTime();
    m2 = Runtime.getRuntime().freeMemory();
    m3+=m1-m2;
    t3+=t2-t1;
    s1+=result.size();    
    if(i<runs-1)
      {
      result.clear();      
      }
    }  
  tf1 = ((float)t3/(float)runs)/1000000.f;
  tf2 = (float)t3/1000000.f;
  AWLog.logDebug("Compact method 1, time for "+runs+" runs: "+t3+"ns (" + tf2+"ms)"+" time per run avg: "+(t3/runs)+"ns ("+tf1+"ms)" + "mem use: "+m3 + " per run: "+(m3/runs));
  AWLog.logDebug("Compacted list: "+s1+":"+result.size()); 
  result.clear();
  }

private static void test2(List<ItemStack> toCompact, List<ItemStack> result, int runs)
  {
  int s1;
  long t1, t2, t3, m1, m2, m3;
  float tf1, tf2;
  s1 = 0; 
  t3 = 0;  
  m3 = 0; 
  
  System.gc();
  for(int i = 0; i < runs; i++)
    {
    m1 = Runtime.getRuntime().freeMemory();    
    t1 = System.nanoTime();
    compactStackList2(toCompact, result);
    t2 = System.nanoTime();
    m2 = Runtime.getRuntime().freeMemory();
    m3+=m1-m2;
    t3+=t2-t1;
    s1+=result.size();    
    if(i<runs-1)
      {
      result.clear();      
      }
    }  
  tf1 = ((float)t3/(float)runs)/1000000.f;
  tf2 = (float)t3/1000000.f;
  AWLog.logDebug("Compact method 2, time for "+runs+" runs: "+t3+"ns (" + tf2+"ms)"+" time per run avg: "+(t3/runs)+"ns ("+tf1+"ms)" + "mem use: "+m3 + " per run: "+(m3/runs));
  AWLog.logDebug("Compacted list: "+s1+":"+result.size()); 
  result.clear();
  }

private static void test3(List<ItemStack> toCompact, List<ItemStack> result, int runs)
  {
  int s1;
  long t1, t2, t3, m1, m2, m3;
  float tf1, tf2;
  s1 = 0; 
  t3 = 0;  
  m3 = 0; 
  
  System.gc();
  for(int i = 0; i < runs; i++)
    {
    m1 = Runtime.getRuntime().freeMemory();    
    t1 = System.nanoTime();
    compactStackList3(toCompact, result);
    t2 = System.nanoTime();
    m2 = Runtime.getRuntime().freeMemory();
    m3+=m1-m2;
    t3+=t2-t1;
    s1+=result.size();    
    if(i<runs-1)
      {
      result.clear();      
      }
    }  
  tf1 = ((float)t3/(float)runs)/1000000.f;
  tf2 = (float)t3/1000000.f;
  AWLog.logDebug("Compact method 3, time for "+runs+" runs: "+t3+"ns (" + tf2+"ms)"+" time per run avg: "+(t3/runs)+"ns ("+tf1+"ms)" + "mem use: "+m3 + " per run: "+(m3/runs));
  AWLog.logDebug("Compacted list: "+s1+":"+result.size()); 
  result.clear();
  }

/**
 * Item-stack comparator.  Configurable in constructor to sort by localized or unlocalized name, as well as
 * sort-order (regular or reverse).
 * @author Shadowmage
 */
public static final class ComparatorItemStack implements Comparator<ItemStack>
{

public static enum StackSortType
{
DISPLAY_NAME,
UNLOCALIZED_NAME,
}

private final int sortOrder;
private final StackSortType sortType;

/**
 * 
 * @param type
 * @param order 1 for normal, -1 for reverse
 */
public ComparatorItemStack(StackSortType type, int order)
  {
  this.sortOrder = order<-1 ? -1 : order>1 ? 1 : order==0? 1 : order;
  this.sortType = type;
  }

@Override
public int compare(ItemStack o1, ItemStack o2)
  {
  String name1, name2;
  switch(sortType)
  {
  case DISPLAY_NAME:
    {
    name1 = o1.getDisplayName();
    name2 = o2.getDisplayName();
    }
  break;
  
  case UNLOCALIZED_NAME:
    {
    name1 = o1.getUnlocalizedName();
    name2 = o2.getUnlocalizedName();    
    }
  break;
  
  default:
    {
    name1 = o1.getDisplayName();
    name2 = o2.getDisplayName();
    }
  break;
  }
  
  int nc = name1.compareTo(name2);
  if(nc==0)//if they have the same name, compare damage/tags
    {
    if(o1.getItemDamage()!=o2.getItemDamage())
      {
      nc = o1.getItemDamage()-o2.getItemDamage();
      nc = nc<0? -1 : nc>0 ? 1 : nc;
      }
    else
      {
      if(o1.hasTagCompound() && o2.hasTagCompound())
        {
        nc = o1.hashCode() - o2.hashCode();
        nc = nc<0? -1 : nc>0 ? 1 : nc;
        }
      else if(o1.hasTagCompound())
        {
        nc=1;
        }
      else if(o1.hasTagCompound())
        {
        nc=-1;
        }
      }
    }
  return nc * sortOrder;
  }
}

/**
 * Utility class for easy storage of quantities of items.  Can quickly iterate over an inventory and get
 * the complete counts of all items in the inventory.<br>
 * Supports adding items / quantities, and removing items / quantities.<br>
 * Supports querying item-quantity by item.<br>
 * Supports retrieving a list of item-stacks representing the most compact representation of this maps contents (stacks limited by stack-size, may be multiple stacks per item)<br>
 * Get / quantity query operations return 0 if no matching item / entry is found.<br>
 * @author Shadowmage
 */
public static final class ItemQuantityMap
{
Map<ItemStackHashWrap, ItemCount> map = new HashMap<ItemStackHashWrap, ItemCount>();

public void put(ItemStackHashWrap wrap, int count)
  {
  if(map.containsKey(wrap))
    {
    map.get(wrap).count = count;
    }
  else
    {
    ItemCount c = new ItemCount();
    c.count = count;
    map.put(wrap, c);
    }
  }

public int get(ItemStackHashWrap wrap)
  {
  if(map.containsKey(wrap))
    {
    return map.get(wrap).count;
    }
  return 0;
  }

public void addItemStack(ItemStack item, int count)
  {
  ItemStackHashWrap wrap = new ItemStackHashWrap(item);
  if(!map.containsKey(wrap))
    {
    map.put(wrap, new ItemCount());
    }
  map.get(wrap).count+=count;
  }

public void removeItem(ItemStack item, int count)
  {
  ItemStackHashWrap wrap = new ItemStackHashWrap(item);
  if(map.containsKey(wrap))
    {
    ItemCount itemCount = map.get(wrap);
    itemCount.count-=count;
    if(itemCount.count<=0)
      {
      map.remove(wrap);
      itemCount.count=0;
      }
    }
  }

public void remove(ItemStackHashWrap wrap)
  {
  map.remove(wrap);
  }

public void clear()
  {
  this.map.clear();
  }

public int get(ItemStack item)
  {
  ItemStackHashWrap wrap = new ItemStackHashWrap(item);
  if(!map.containsKey(wrap))
    {
    return 0;
    }
  return map.get(wrap).count;
  }

public Set<ItemStackHashWrap> keySet()
  {
  return map.keySet();
  }

public boolean contains(ItemStackHashWrap wrap)
  {
  return map.containsKey(wrap);
  }

@Override
public String toString()
  {
  String out = "Item Quantity Map:";
  for(ItemStackHashWrap wrap : map.keySet())
    {
    out = out +"\n   "+ wrap.item.getUnlocalizedName();
    out = out + " : "+map.get(wrap).count;
    }  
  return out;
  }

/**
 * Return the most compact set of item-stacks that can represent the contents of this map.<br>
 * May return multiple stacks of the same item if the quantity contained is > maxStackSize.<br> * 
 * @param items will be filled with the item-stacks from this map, must not be NULL
 */
public void getItems(List<ItemStack> items)
  {
  ItemStack outStack;
  int qty;
  for(ItemStackHashWrap wrap1 : map.keySet())
    {
    qty = map.get(wrap1).count;
    while(qty>0)
      {
      outStack = wrap1.createItemStack();
      outStack.stackSize = qty>outStack.getMaxStackSize() ? outStack.getMaxStackSize() : qty;
      qty-=outStack.stackSize;
      items.add(outStack);
      }
    } 
  }

/**
 * used by ItemQuantityMap for tracking item quantities for a given ItemStackHashWrap
 * @author Shadowmage
 *
 */
private static final class ItemCount
{
private int count;
}
}

/**
 * Wraps an item stack with a hashable object.<br>
 * Uses item, item damage, and nbt-tag for hash-code.<br>
 * Ignores quantity.<br>
 * Immutable.
 * @author Shadowmage
 */
public static final class ItemStackHashWrap
{
private final Item item;
private final int damage;
private final NBTTagCompound tag;

/**
 * @param item MUST NOT BE NULL
 */
public ItemStackHashWrap(ItemStack item)
  {
  if(item==null){throw new IllegalArgumentException("Stack may not be null");}
  this.item = item.getItem();
  if(this.item==null){throw new IllegalArgumentException("Item may not be null");}
  this.damage = item.getItemDamage();
  if(item.hasTagCompound())
    {
    this.tag = (NBTTagCompound) item.getTagCompound().copy();    
    }
  else
    {
    this.tag = null;
    }
  }

public Item getItem()
  {
  return item;
  }

public int getDamage()
  {
  return damage;
  }

public NBTTagCompound getTag()
  {
  return (NBTTagCompound) (tag==null? null : tag.copy());
  }

/**
 * internal constructor used for copying/cloning
 * @param item
 * @param damage
 * @param tag
 */
private ItemStackHashWrap(Item item, int damage, NBTTagCompound tag)
  {
  if(item==null){throw new IllegalArgumentException("Item may not be null");}
  this.item = item;
  this.damage = damage;
  this.tag = (NBTTagCompound) (tag==null ? null : tag.copy());
  }

@Override
public int hashCode()
  {
  int hash = 1;
  if(item!=null){hash = 31*hash + item.hashCode();}
  hash = 31*hash + damage;
//  hash = 31*hash + stack.stackSize;//noop for implementation, only care about identity, not quantity
  if(tag!=null){hash = 31*hash + tag.hashCode();}
  return hash;
  }

@Override
public boolean equals(Object obj)
  {
  if(obj==null){return false;}
  if(obj.getClass()!=this.getClass()){return false;}
  ItemStackHashWrap wrap = (ItemStackHashWrap)obj;  
  boolean tagsMatch = false;
  if(tag==null){tagsMatch = wrap.tag==null;}
  else if(wrap.tag==null){tagsMatch=tag==null;}
  else{tagsMatch = tag.equals(wrap.tag);}  
  return item==wrap.item && damage==wrap.damage && tagsMatch;  
  }

@Override
public String toString()
  {
  return "StackHashWrap: "+item.getUnlocalizedName()+"@"+damage;
  }

public ItemStackHashWrap copy()
  {
  return new ItemStackHashWrap(item, damage, tag);
  }

public ItemStack createItemStack()
  {
  ItemStack stack = new ItemStack(item,1,damage);
  if(tag!=null){stack.stackTagCompound = (NBTTagCompound)tag.copy();}
  return stack;
  }

}

}
