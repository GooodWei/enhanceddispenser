package com.godwei.behaviors;

import com.godwei.Utlis.ColoredItems;
import com.godwei.Utlis.ItemType;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PaintEntityBehavior extends ItemDispenserBehavior {


    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        Box box = new Box(pos);
        ServerWorld world = pointer.getWorld();
        List<SheepEntity> sheep = world.getEntitiesByClass(SheepEntity.class, box,
                Objects::nonNull);
        //Deprecated because cost too much time
        //List<ItemEntity> itemEntities = world.getEntitiesByClass(ItemEntity.class, box, Objects::nonNull);
        //Map<ItemType, List<ItemEntity>> typeListMap = getTypeItemEntityList(itemEntities);
        Map<ItemType, List<ItemEntity>> typeListMap = getTypeItemEntityList(world, box);
        DyeItem dye = (DyeItem) stack.getItem();
        DyeColor color = dye.getColor();
        AtomicBoolean isEmpty = new AtomicBoolean(true);
        typeListMap.forEach(
                (k, v) -> {
                    if (!v.isEmpty()) {
                        isEmpty.set(false);
                    }
                }
        );
        if (sheep.isEmpty() && isEmpty.get()) {
            return super.dispenseSilently(pointer, stack);
        }
        sheep.forEach(
                i -> {
                    if (stack.getCount() >= 1) {
                        i.setColor(color);
                        stack.decrement(1);
                    }
                }
        );
        PaintItemEntities(typeListMap, color, stack);
        return stack;
    }

    private static void PaintItemEntities(Map<ItemType, List<ItemEntity>> map, DyeColor color, ItemStack dyes) {
        for (ItemType type : map.keySet()) {
            switch (type) {
                case WOOL -> {
                    List<ItemEntity> itemEntities = map.get(type);
                    itemEntities.forEach(
                            ie -> {
                                if (dyes.getCount() >= 1) {
                                    ie.setStack(new ItemStack(ColoredItems.getWoolFromColor().get(color), ie.getStack().getCount()));
                                    dyes.decrement(1);
                                }
                            }
                    );
                }
                case CONCRETE_POWDER -> {
                    List<ItemEntity> itemEntities = map.get(type);
                    itemEntities.forEach(
                            ie -> {
                                if (dyes.getCount() >= 1) {
                                    ie.setStack(new ItemStack(ColoredItems.getConcretePowderFromColor().get(color), ie.getStack().getCount()));
                                    dyes.decrement(1);
                                }
                            }
                    );
                }
                case GLASS -> {
                    List<ItemEntity> itemEntities = map.get(type);
                    itemEntities.forEach(
                            ie -> {
                                if (dyes.getCount() >= 1) {
                                    ie.setStack(new ItemStack(ColoredItems.getGlassFromColor().get(color), ie.getStack().getCount()));
                                    dyes.decrement(1);
                                }
                            }
                    );
                }
            }

        }
    }

    private static Map<ItemType, List<ItemEntity>> getTypeItemEntityList(@NotNull List<ItemEntity> entities) {
        Map<ItemType, List<ItemEntity>> map = new HashMap<>();
        for (ItemType type : ItemType.STAINABLE_ITEM_TYPE_LIST) {
            List<Item> typeItemList = type.getItemList();
            List<ItemEntity> itemEntities = new ArrayList<>();
            entities.forEach(
                    entity -> {
                        if (typeItemList.contains(entity.getStack().getItem())) {
                            itemEntities.add(entity);
                        }
                    }
            );
            map.put(type, itemEntities);
        }
        return map;
    }

    private static Map<ItemType, List<ItemEntity>> getTypeItemEntityList(@NotNull ServerWorld world, @NotNull Box area) {
        Map<ItemType, List<ItemEntity>> map = new HashMap<>();
        for (ItemType type : ItemType.STAINABLE_ITEM_TYPE_LIST) {
            List<Entity> entities = world.getOtherEntities(null, area,
                    ie -> {
                        if (ie instanceof ItemEntity itemEntity) {
                            return type.getItemList().contains(itemEntity.getStack().getItem());
                        }
                        return false;
                    });
            List<ItemEntity> collect = entities.stream().map(i -> (ItemEntity) i).collect(Collectors.toList());
            map.put(type, collect);
        }

        return map;
    }
}
