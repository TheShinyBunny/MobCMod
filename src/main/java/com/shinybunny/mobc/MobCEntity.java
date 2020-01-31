package com.shinybunny.mobc;

import com.shinybunny.mobc.ai.CureEnchantedItems;
import com.shinybunny.mobc.ai.EatEntityGoal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.EntityTrackerUpdateS2CPacket;
import net.minecraft.command.arguments.EntityAnchorArgumentType;
import net.minecraft.command.arguments.LookingPosArgument;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.omg.PortableServer.THREAD_POLICY_ID;

import java.util.Map;

public class MobCEntity extends HostileEntity {

    private static final byte START_CRAWLING_STATUS = 100;
    private static final byte STOP_CRAWLING_STATUS = 101;
    public static final byte BITE_STATUS = 102;
    private static final byte TONGUE_STATUS = 103;
    private static final EntityDimensions CRAWLING_DIMENSIONS = EntityDimensions.fixed(1.5f,0.25f);
    private int startCrawlingTime;
    private int stopCrawlingTime;
    public Entity eatingEntity;
    public int eatCooldown;
    public int tongueTime;
    public static final TrackedData<Boolean> CRAWLING = DataTracker.registerData(MobCEntity.class,TrackedDataHandlerRegistry.BOOLEAN);
    public int bitingTime;
    private TargetPlayer targetPlayerGoal;

    public MobCEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.stepHeight = 1.5f;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(CRAWLING,false);
    }

    @Override
    protected Identifier getLootTableId() {
        return super.getLootTableId();
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeContainer().get(EntityAttributes.FOLLOW_RANGE).setBaseValue(32.0f);
        this.getAttributeContainer().get(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.3f);
        this.getAttributeContainer().get(EntityAttributes.MAX_HEALTH).setBaseValue(40f);
    }

    @Override
    public boolean tryAttack(Entity entity_1) {
        if (entity_1 == eatingEntity) return false;
        if (super.tryAttack(entity_1)) {
            world.sendEntityStatus(this,TONGUE_STATUS);
            return true;
        }
        return false;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new CureEnchantedItems(this, 32));
        this.goalSelector.add(4, new AttackGoal(this,1,true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.add(2, new RevengeGoal(this));
        this.goalSelector.add(4, new EatEntityGoal(this));
        this.targetSelector.add(3, targetPlayerGoal = new TargetPlayer());
    }

    public void setEatingEntity(Entity entity) {
        this.eatingEntity = entity;
        entity.startRiding(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isClient) {
            eatCooldown--;
            if (isCrawling() && targetPlayerGoal.getTarget() == null) {
                stopCrawling();
            }
        }
        if (startCrawlingTime > 0) {
            startCrawlingTime--;
            if (startCrawlingTime == 0) {
                setCrawling(true);
            }
        }
        if (stopCrawlingTime > 0) {
            stopCrawlingTime--;
            if (stopCrawlingTime == 0) {
                setCrawling(false);
            }
        }
    }

    @Override
    public int method_5986() {
        return 1;
    }

    private static LookingPosArgument leftParticlePos = new LookingPosArgument(1.5,0,0);
    private static LookingPosArgument rightParticlePos = new LookingPosArgument(-1.5,0,0);

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (isCrawling() && !world.isClient) {
            Vec3d left = leftParticlePos.toAbsolutePos(this.getCommandSource());
            Vec3d right = rightParticlePos.toAbsolutePos(this.getCommandSource());
            ((ServerWorld)world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK,world.getBlockState(getBlockPos().down())),right.x,right.y,right.z,20,0.2,0.2,0.2,0.1);
            ((ServerWorld)world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK,world.getBlockState(getBlockPos().down())),left.x,left.y,left.z,20,0.2,0.2,0.2,0.1);
        }
    }

    public void blessItem(ItemEntity item) {
        ItemStack stack = item.getStack();
        Map<Enchantment,Integer> enchs = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment,Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            if (e.getKey().isCursed()) {
                enchs.remove(e.getKey());
            }
        }
        EnchantmentHelper.set(enchs,stack);
        item.setStack(stack);

        if (!world.isClient) {
            ((ServerWorld)world).getServer().getPlayerManager().sendToAll(new EntityTrackerUpdateS2CPacket(item.getEntityId(),item.getDataTracker(),true));
            ((ServerWorld)world).spawnParticles(ParticleTypes.ENCHANT,item.x,item.y + 2,item.z,30,0.2,0.5,0.2,0.2);
            ((ServerWorld)world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, item.x + 0.5,item.y + 0.2, item.z,20,0,0,0.3,0.05);
            ((ServerWorld)world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, item.x - 0.5,item.y + 0.2, item.z,20,0,0,0.3,0.05);
            ((ServerWorld)world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, item.x,item.y + 0.2, item.z - 0.5,20,0.3,0,0,0.05);
            ((ServerWorld)world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, item.x,item.y + 0.2, item.z + 0.5,20,0.3,0,0,0.05);
        }
    }

    public boolean isCrawling() {
        return dataTracker.get(CRAWLING);
    }

    public void setCrawling(boolean crawling) {
        dataTracker.set(CRAWLING,crawling);
        calculateDimensions();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose entityPose_1) {
        return isCrawling() ? CRAWLING_DIMENSIONS : super.getDimensions(entityPose_1);
    }

    public void startCrawling() {
        if (!isCrawling() && startCrawlingTime == 0) {
            world.sendEntityStatus(this, START_CRAWLING_STATUS);
            stopCrawlingTime = 0;
            startCrawlingTime = 20;
        }
    }

    public void stopCrawling() {
        if (isCrawling() && stopCrawlingTime == 0) {
            world.sendEntityStatus(this, STOP_CRAWLING_STATUS);
            stopCrawlingTime = 20;
            startCrawlingTime = 0;
        }
    }

    @Override
    public void handleStatus(byte byte_1) {
        if (byte_1 == START_CRAWLING_STATUS) {
            System.out.println("starting to crawl in client");
            stopCrawlingTime = 0;
            startCrawlingTime = 20;
            return;
        }
        if (byte_1 == STOP_CRAWLING_STATUS) {
            startCrawlingTime = 0;
            stopCrawlingTime = 20;
            return;
        }
        if (byte_1 == BITE_STATUS) {
            this.bitingTime = 50;
        }
        if (byte_1 == TONGUE_STATUS) {
            this.tongueTime = 60;
        }
        super.handleStatus(byte_1);
    }

    @Override
    public double getMountedHeightOffset() {
        return 1.5;
    }

    public int getStartCrawlingTime() {
        return startCrawlingTime;
    }

    public int getStopCrawlingTime() {
        return stopCrawlingTime;
    }

    public boolean isChangingCrawlStatus() {
        return startCrawlingTime > 0 || stopCrawlingTime > 0;
    }

    public void playBiteSound() {
        this.playSound(MainMobCMod.BITE_SOUND,1.0f,0.8f);
    }

    public class TargetPlayer extends FollowTargetGoal<PlayerEntity> {

        private int crawlCooldown;

        public TargetPlayer() {
            super(MobCEntity.this, PlayerEntity.class, true,true);
        }

        @Override
        public void start() {
            super.start();
            if (distanceTo(targetEntity) > 8) {
                startCrawling();
                crawlCooldown = 100;
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (crawlCooldown > 0) {
                crawlCooldown--;
            }
            if (targetEntity == null) {
                stopCrawling();
            }
            if (distanceTo(targetEntity) < 8) {
                stopCrawling();
                crawlCooldown = 0;
            } else {
                startCrawling();
                crawlCooldown = 100;
            }
        }

        @Override
        public void stop() {
            super.stop();
            if (crawlCooldown == 0 || targetEntity == null) {
                stopCrawling();
            }
        }

        public LivingEntity getTarget() {
            return targetEntity;
        }
    }

    public static class AttackGoal extends MeleeAttackGoal {

        public AttackGoal(MobEntityWithAi mobEntityWithAi_1, double double_1, boolean boolean_1) {
            super(mobEntityWithAi_1, double_1, boolean_1);
        }

        @Override
        public void start() {
            super.start();
            if (this.mob.getNavigation().getCurrentPath() != null) {
                System.out.println("starting to move to " + this.mob.getNavigation().getCurrentPath().method_48());
            }
        }

        @Override
        protected double getSquaredMaxAttackDistance(LivingEntity livingEntity_1) {
            return 4;
        }
    }

}
