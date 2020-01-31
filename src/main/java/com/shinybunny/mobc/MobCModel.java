//Made with Blockbench
//Paste this code into your mod.

package com.shinybunny.mobc;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;

public class MobCModel extends EntityModel<MobCEntity> {
	private final Cuboid leftFrontLeg;
	private final Cuboid rightFrontLeg;
	private final Cuboid leftBackLeg;
	private final Cuboid rightBackLeg;
	private final Cuboid body;
	private final Cuboid mouth_top;
	private final Cuboid mouth_bottom;

	public MobCModel() {
		textureWidth = 256;
		textureHeight = 256;

		leftFrontLeg = new Cuboid(this);
		leftFrontLeg.setRotationPoint(13.0F, 11.0F, -6.0F);
		leftFrontLeg.boxes.add(new Box(leftFrontLeg, 0, 85, -4.0F, 0.0F, -4.0F, 8, 13, 8, 0.0F, false));

		rightFrontLeg = new Cuboid(this);
		rightFrontLeg.setRotationPoint(-13.0F, 11.0F, -6.0F);
		rightFrontLeg.boxes.add(new Box(rightFrontLeg, 0, 64, -4.0F, 0.0F, -4.0F, 8, 13, 8, 0.0F, false));

		leftBackLeg = new Cuboid(this);
		leftBackLeg.setRotationPoint(13.0F, 11.0F, 12.0F);
		leftBackLeg.boxes.add(new Box(leftBackLeg, 0, 0, -4.0F, 0.0F, -4.0F, 8, 13, 8, 0.0F, false));

		rightBackLeg = new Cuboid(this);
		rightBackLeg.setRotationPoint(-13.0F, 11.0F, 12.0F);
		rightBackLeg.boxes.add(new Box(rightBackLeg, 0, 21, -4.0F, 0.0F, -4.0F, 8, 13, 8, 0.0F, false));

		body = new Cuboid(this);
		body.setRotationPoint(0.0F, 4.0F, 0.0F);
		body.boxes.add(new Box(body, 0, 128, -16.0F, 0.0F, -9.0F, 32, 10, 24, 0.0F, false));

		mouth_top = new Cuboid(this);
		mouth_top.setRotationPoint(0.0F, -12.0F, 24.0F);
		mouth_top.boxes.add(new Box(mouth_top, 0, 0, -24.0F, -16.0F, 0.0F, 48, 16, 48, 0.0F, false));

		mouth_bottom = new Cuboid(this);
		mouth_bottom.setRotationPoint(0.0F, -12.0F, 24.0F);
		mouth_bottom.boxes.add(new Box(mouth_bottom, 0, 64, -24.0F, 0.0F, 0.0F, 48, 16, 48, 0.0F, false));
	}

	@Override
	public void render(MobCEntity entity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6) {
		this.leftFrontLeg.pitch = MathHelper.cos(float_1 * 0.6662F) * 0.4f * float_2;
		this.leftBackLeg.pitch = MathHelper.cos(float_1 * 0.6662F + 3.1415927F) * 0.4f * float_2;
		this.rightFrontLeg.pitch = MathHelper.cos(float_1 * 0.6662F + 3.1415927F) * 0.4f * float_2;
		this.rightBackLeg.pitch = MathHelper.cos(float_1 * 0.6662F) * 0.4f * float_2;
		leftFrontLeg.render(float_6);
		rightFrontLeg.render(float_6);
		leftBackLeg.render(float_6);
		rightBackLeg.render(float_6);
		body.render(float_6);
		if (entity_1.getStartCrawlingTime() > 0) {
			int time = entity_1.getStartCrawlingTime();
			float percentDone = time <= 10 ? time / 20f : 1 - time / 20f;
			float easeInOut = easeInOut(percentDone);
			float eased = easeInOut * -60;
			mouth_top.pitch = eased * 0.017453292F;
			mouth_bottom.pitch = eased * 0.017453292F;
		} else if (entity_1.isCrawling()) {
			mouth_top.pitch = 0;
			mouth_bottom.pitch = 0;
		} else {
			if (entity_1.bitingTime > 0) {
				entity_1.bitingTime--;
				float percent = entity_1.bitingTime < 25 ? (entity_1.bitingTime / 50f) : (1 - entity_1.bitingTime / 50f);
				mouth_top.pitch = Math.max(0, 0.017453292F * 30 * percent);
				mouth_bottom.pitch = Math.min(0, 0.017453292F * -30 * percent);
			} else {
				mouth_top.pitch = Math.max(0, MathHelper.cos(float_1 * 0.6662F) * 0.2F * float_2);
				mouth_bottom.pitch = Math.min(0, MathHelper.cos(float_1 * 0.6662F) * -0.2F * float_2);
			}
		}
		mouth_top.yaw = 180 * 0.017453292F;
		mouth_bottom.yaw = 180 * 0.017453292F;
		mouth_top.render(float_6);
		mouth_bottom.render(float_6);
	}

	private float easeInOut(float percentDone) {
		if (percentDone < 0.5) {
			return 2.0f * (percentDone * percentDone);
		}
		percentDone -= 0.5;
		return 2.0f * percentDone * (1f - percentDone) + 0.5f;
	}

	public static class Tongue extends EntityModel<MobCEntity> {

		private final Cuboid main;
		private final Cuboid roll;
		private float percent;

		public Tongue() {
			textureWidth = 128;
			textureHeight = 128;

			main = new Cuboid(this);
			main.setRotationPoint(0.0F, 24.0F, 8.0F);
			main.boxes.add(new Box(main, 0, 0, -4.0F, -3.0F, -29.0F, 8, 3, 29, 0.0F, false));

			roll = new Cuboid(this);
			roll.setRotationPoint(0.0F, 24.0F, 8.0F);
			roll.boxes.add(new Box(roll, 0, 32, -4.0F, -4.0F, -37.0F, 8, 4, 8, 0.0F, false));
		}

		@Override
		public void render(MobCEntity entity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(1,1,1 + percent);
			main.render(float_6);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			//GlStateManager.translatef(0,0,percent * 2);
			GlStateManager.scalef(1,1,1 + percent);
			roll.render(float_6);
			GlStateManager.popMatrix();
		}

		public void setPercent(float percent) {
			this.percent = percent;
		}
	}
}