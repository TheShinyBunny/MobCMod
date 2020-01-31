package com.shinybunny.mobc.ai;

import com.shinybunny.mobc.MobCEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class EatEntityGoal extends Goal {

    private MobCEntity mobC;
    private int biteCooldown;

    public EatEntityGoal(MobCEntity mobC) {
        this.mobC = mobC;
    }

    @Override
    public boolean canStart() {
        return mobC.eatCooldown <= 0 && !getEdibleEntities().isEmpty() && !mobC.isCrawling();
    }

    @Override
    public void start() {
        List<Entity> inMouth = getEdibleEntities();
        mobC.setEatingEntity(inMouth.get(0));
    }

    protected List<Entity> getEdibleEntities() {
        Vec3d looking = mobC.getRotationVector();
        return mobC.world.getEntities(mobC,mobC.getBoundingBox().offset(looking.x,0,looking.z).expand(0.2),e->{
            return (e instanceof MobEntity && !(e instanceof MobCEntity)) || (e instanceof PlayerEntity && !((PlayerEntity) e).abilities.invulnerable);
        });
    }

    @Override
    public boolean shouldContinue() {
        return mobC.hasPassenger(mobC.eatingEntity);
    }

    @Override
    public void tick() {
        biteCooldown--;
        if (biteCooldown == 10) {
            mobC.world.sendEntityStatus(mobC,MobCEntity.BITE_STATUS);
        }
        if (biteCooldown == 5) {
            mobC.playBiteSound();
        }
        if (biteCooldown <= 0) {
            mobC.eatingEntity.damage(new EntityDamageSource("mobc.bite",mobC),mobC.getRand().nextInt(5) + 4);
            biteCooldown = 40;
        }
    }

    @Override
    public void stop() {
        mobC.eatingEntity = null;
        mobC.eatCooldown = 120;
    }
}
