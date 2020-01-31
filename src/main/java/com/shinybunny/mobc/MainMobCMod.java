package com.shinybunny.mobc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biomes.v1.FabricBiomes;
import net.fabricmc.fabric.api.biomes.v1.OverworldBiomes;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.impl.registry.FabricRegistryClientInit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.DesertBiome;

public class MainMobCMod implements ModInitializer, ClientModInitializer {

    public static final EntityType<MobCEntity> MOB_C = Registry.register(Registry.ENTITY_TYPE,new Identifier("mobc","mob_c"),FabricEntityTypeBuilder.create(EntityCategory.MONSTER, MobCEntity::new).size(EntityDimensions.changing(1.5f,2.5f)).trackable(128,3).build());
    private static final Identifier biteSoundId = new Identifier("mobc:bite");
    public static final SoundEvent BITE_SOUND = new SoundEvent(biteSoundId);
    public static final Item SPAWN_EGG = Registry.register(Registry.ITEM,new Identifier("mobc:mob_c_spawn_egg"),new SpawnEggItem(MOB_C, 0xf0c92c, 0xd00000,new Item.Settings().group(ItemGroup.MISC)));

    @Override
    public void onInitialize() {
        Registry.register(Registry.SOUND_EVENT,biteSoundId,BITE_SOUND);
        for (Biome b : Registry.BIOME) {
            if (b.getCategory() == Biome.Category.DESERT) {
                b.getEntitySpawnList(EntityCategory.MONSTER).add(new Biome.SpawnEntry(MOB_C, 10, 1, 1));
            }
        }
    }

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(MobCEntity.class, (entityRenderDispatcher, context) -> new MobCRenderer(entityRenderDispatcher,new MobCModel()));
    }
}
