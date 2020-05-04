package com.shinybunny.mobc.ai;

import com.shinybunny.mobc.MobCEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.ViewableWorld;

import java.util.Map;

public class CureEnchantedItems extends MoveToTargetPosGoal {

    private ItemEntity target;

    public CureEnchantedItems(MobCEntity mobC, int distance) {
        super(mobC,1,distance,15);
    }

    @Override
    protected boolean isTargetPos(ViewableWorld var1, BlockPos var2) {
        return !mob.world.getEntities(ItemEntity.class,new Box(var2), item->isCursed(item.getStack())).isEmpty();
    }

    @Override
    public void tick() {
        targetPos = target.getBlockPos();
        super.tick();
        if (hasReached()) {
            ((MobCEntity)mob).blessItem(target);
        }
    }

    @Override
    public double getDesiredSquaredDistanceToTarget() {
        return 4.0;
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && !target.removed;
    }

    @Override
    protected boolean findTargetPos() {
        boolean found = super.findTargetPos();
        if (found) {
            target = mob.world.getEntities(ItemEntity.class,new Box(targetPos),item->isCursed(item.getStack())).get(0);
        }
        return found;
    }

    private boolean isCursed(ItemStack stack) {
        return EnchantmentHelper.hasBindingCurse(stack) || EnchantmentHelper.hasVanishingCurse(stack);
    }
}
