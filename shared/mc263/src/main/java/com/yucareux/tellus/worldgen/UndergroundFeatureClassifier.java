package com.yucareux.tellus.worldgen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

/**
 * Separates decorative host-rock patches from mineable ore features. Vanilla
 * registers both groups under {@code ore_*} IDs, so path matching alone cannot
 * implement independent settings.
 */
public final class UndergroundFeatureClassifier {
    private UndergroundFeatureClassifier() {
    }

    public static Kind classify(PlacedFeature feature) {
       if (!(feature.feature().value() instanceof OreFeature oreFeature)) {
          return Kind.OTHER;
       }

       List<BlockReplacement> replacements = oreFeature.targetStates();
       boolean geologicalStone = !replacements.isEmpty()
          && replacements.stream().allMatch(replacement -> isGeologicalStone(replacement.state().getBlock()));
       return geologicalStone ? Kind.GEOLOGICAL_STONE : Kind.MINEABLE_ORE;
    }

    public static boolean isGeologicalStone(Block block) {
       return block == Blocks.GRANITE
          || block == Blocks.DIORITE
          || block == Blocks.ANDESITE
          || block == Blocks.TUFF;
    }

    public enum Kind {
       OTHER,
       MINEABLE_ORE,
       GEOLOGICAL_STONE
    }
}
