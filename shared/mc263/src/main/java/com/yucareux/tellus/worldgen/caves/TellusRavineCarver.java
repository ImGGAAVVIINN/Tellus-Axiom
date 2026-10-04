package com.yucareux.tellus.worldgen.caves;

import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public final class TellusRavineCarver {
   private final WorldCarver carver;

   public TellusRavineCarver(CanyonWorldCarver carver) {
      this.carver = carver;
   }

   WorldCarver carver() {
      return this.carver;
   }
}