package com.yucareux.tellus.worldgen.caves;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.junit.jupiter.api.Test;

class TellusEmptyBeardifierTest {
   @Test
   void projectedVanillaCaveFieldIsIndependentOfRetargetedStructures() {
      SamplerContext context = SamplerContext.builder().build();

      assertEquals(0.0F, TellusEmptyBeardifier.instance().sampleValue(context, 32, 2048, -48));
   }
}