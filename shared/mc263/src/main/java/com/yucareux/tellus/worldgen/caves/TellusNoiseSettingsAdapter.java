package com.yucareux.tellus.worldgen.caves;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;

public final class TellusNoiseSettingsAdapter {
    private TellusNoiseSettingsAdapter() {
    }

    public static Holder<NoiseGeneratorSettings> adaptToTellusHeight(Holder<NoiseGeneratorSettings> baseHolder, int tellusMinY, int tellusHeight, int seaLevel) {
       NoiseGeneratorSettings base = (NoiseGeneratorSettings)baseHolder.value();
       NoiseSettings adaptedNoise = NoiseSettings.create(tellusMinY, tellusHeight);
       int maxY = tellusMinY + tellusHeight - 1;
       int adaptedSeaLevel = Math.max(tellusMinY, Math.min(maxY, seaLevel));
       NoiseGeneratorSettings adapted = new NoiseGeneratorSettings(
          adaptedNoise,
          base.defaultBlock(),
          base.defaultFluid(),
          base.noiseRouter(),
          base.materialRule(),
          base.spawnTarget(),
          adaptedSeaLevel,
          base.disableMobGeneration(),
          base.aquifers(),
          base.useLegacyRandomSource(),
          base.debugFunctions()
       );
       return Holder.direct(adapted);
    }
}
