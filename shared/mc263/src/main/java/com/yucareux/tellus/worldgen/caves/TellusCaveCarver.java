package com.yucareux.tellus.worldgen.caves;

import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public final class TellusCaveCarver {
   private final WorldCarver carver;

   public TellusCaveCarver(CaveWorldCarver carver) {
      this.carver = carver;
   }

   WorldCarver carver() {
      return this.carver;
   }
}