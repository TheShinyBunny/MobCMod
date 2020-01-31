package com.shinybunny.mobc;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class MobCRenderer extends LivingEntityRenderer<MobCEntity,MobCModel> {
    private static final Identifier TEXTURE = new Identifier("mobc:mob_texture.png");

    public MobCRenderer(EntityRenderDispatcher entityRenderDispatcher_1, MobCModel entityModel_1) {
        super(entityRenderDispatcher_1, entityModel_1, 0f);
        addFeature(new TongueFeature(this));
    }

    @Override
    protected void renderLabel(MobCEntity entity_1, double double_1, double double_2, double double_3, String string_1, double double_4) {
        if (entity_1.isCustomNameVisible() && entity_1.hasCustomName()) {
            super.renderLabel(entity_1, double_1, double_2, double_3, string_1, double_4);
        }
    }

    @Override
    protected void render(MobCEntity livingEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6) {
        GlStateManager.pushMatrix();
        if (livingEntity_1.getStopCrawlingTime() > 0) {
            float percent = MathHelper.clamp(livingEntity_1.getStopCrawlingTime() / 20f + (float_6 * 3),0,1);
            GlStateManager.translatef(0,2.5f * percent,0);
        } else if (livingEntity_1.isCrawling()) {
            GlStateManager.translatef(0,2.5f,0);
        } else if (livingEntity_1.getStartCrawlingTime() > 0) {
            float percent = MathHelper.clamp(1 - livingEntity_1.getStartCrawlingTime() / 20f + (float_6 * 3),0,1);
            GlStateManager.translatef(0,2.5f * percent,0);
        }
        super.render(livingEntity_1, float_1, float_2, float_3, float_4, float_5, float_6);
        GlStateManager.popMatrix();
    }

    @Override
    protected Identifier getTexture(MobCEntity var1) {
        return TEXTURE;
    }

    public static class TongueFeature extends FeatureRenderer<MobCEntity,MobCModel> {

        private static final Identifier TONGUE_TEXTURE = new Identifier("mobc", "tongue.png");
        private final MobCModel.Tongue tongue;

        public TongueFeature(FeatureRendererContext<MobCEntity, MobCModel> featureRendererContext_1) {
            super(featureRendererContext_1);
            this.tongue = new MobCModel.Tongue();
        }

        @Override
        public void render(MobCEntity mob, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
            if (mob.tongueTime > 0 && !mob.isCrawling() && !mob.isChangingCrawlStatus()) {
                float percent = mob.tongueTime < 40 ? mob.tongueTime / 40f : 1 - (mob.tongueTime - 40) / 20f;
                tongue.setPercent(percent);
                bindTexture(TONGUE_TEXTURE);
                GlStateManager.translatef(0,-2.2f,0);
                tongue.render(mob, var2, var3, var5, var6, var7, var8);
                mob.tongueTime--;
            }
        }

        @Override
        public boolean hasHurtOverlay() {
            return false;
        }
    }
}
