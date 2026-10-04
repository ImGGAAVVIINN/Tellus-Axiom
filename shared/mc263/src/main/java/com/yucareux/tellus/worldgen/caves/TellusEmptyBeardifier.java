package com.yucareux.tellus.worldgen.caves;

import com.yucareux.tellus.compat.MinecraftVersionCompat;
import net.minecraft.world.level.levelgen.Beardifier;

final class TellusEmptyBeardifier {
   private TellusEmptyBeardifier() {
   }

   static Beardifier instance() {
      return MinecraftVersionCompat.emptyBeardifier();
   }
}