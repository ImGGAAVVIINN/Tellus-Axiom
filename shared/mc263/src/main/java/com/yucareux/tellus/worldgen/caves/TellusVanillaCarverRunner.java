package com.yucareux.tellus.worldgen.caves;

import com.yucareux.tellus.compat.MinecraftVersionCompat;
import com.yucareux.tellus.worldgen.UndergroundStructureExclusion;
import java.util.List;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.Aquifer.FluidPicker;
import net.minecraft.world.level.levelgen.Aquifer.FluidStatus;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;

public final class TellusVanillaCarverRunner {
    private static final int CARVER_RADIUS_CHUNKS = 8;

    private final BiomeSource biomeSource;

    private final NoiseBasedChunkGenerator carvingContextGenerator;

    private final NoiseGeneratorSettings contextNoiseSettings;
    private final TellusConfiguredCarvers configuredCarvers;
    private final int chunkMinY;
    private final TellusVanillaNoiseCaveSampler noiseCaveSampler;

    public TellusVanillaCarverRunner(
       BiomeSource biomeSource,
       Registry<Block> blockRegistry,
       Holder<NoiseGeneratorSettings> vanillaNoiseSettings,
       Holder<NoiseGeneratorSettings> noiseSettings,
       int tellusMinY,
       int tellusHeight,
       int undergroundDepth
    ) {
       this.biomeSource = Objects.requireNonNull(biomeSource, "biomeSource");
       Objects.requireNonNull(blockRegistry, "blockRegistry");
       Holder<NoiseGeneratorSettings> contextSettings = Objects.requireNonNull(noiseSettings, "noiseSettings");
       this.chunkMinY = tellusMinY;
       this.contextNoiseSettings = Objects.requireNonNull((NoiseGeneratorSettings)contextSettings.value(), "contextNoiseSettings");
       this.carvingContextGenerator = Objects.requireNonNull(new NoiseBasedChunkGenerator(this.biomeSource, contextSettings), "carvingContextGenerator");
       this.configuredCarvers = TellusConfiguredCarvers.create(blockRegistry, tellusMinY, tellusHeight, undergroundDepth);
       this.noiseCaveSampler = new TellusVanillaNoiseCaveSampler(
          Objects.requireNonNull(vanillaNoiseSettings, "vanillaNoiseSettings").value()
       );
    }

    public void applyCarvers(
       WorldGenRegion level,
       long worldSeed,
       BiomeManager biomeManager,
       StructureManager structures,
       ChunkAccess chunk,
       int tellusSeaLevel,
       boolean applyCaves,
       boolean cavesReachSurface,
       boolean applyOreVeins,
       boolean applyGeologicalStonePatches,
       int[] surfaceYByColumn,
       IntBinaryOperator surfaceHeightSampler,
       int[] floodGuardYByColumn,
       int[] generationFloorYByColumn,
       List<UndergroundStructureExclusion.Box> structureExclusions
    ) {
       StructureManager safeStructures = Objects.requireNonNull(structures, "structures");
       IntBinaryOperator safeSurfaceHeightSampler = Objects.requireNonNull(surfaceHeightSampler, "surfaceHeightSampler");
       NoiseGeneratorSettings safeNoiseSettings = Objects.requireNonNull(this.contextNoiseSettings, "contextNoiseSettings");
       NoiseBasedChunkGenerator safeCarvingContextGenerator = Objects.requireNonNull(this.carvingContextGenerator, "carvingContextGenerator");
       RegistryAccess registryAccess = level.registryAccess();
       RandomState safeRandomState = this.noiseCaveSampler.randomStateFor(registryAccess, worldSeed);
       this.noiseCaveSampler.apply(
          registryAccess,
          worldSeed,
          chunk,
          this.chunkMinY,
          tellusSeaLevel,
          applyCaves,
          cavesReachSurface,
          applyOreVeins,
          applyGeologicalStonePatches,
          surfaceYByColumn,
          safeSurfaceHeightSampler,
          floodGuardYByColumn,
          generationFloorYByColumn,
          structureExclusions,
          (target, pos, state, fluid) -> {
             target.setBlockState(pos, state);
             if (fluid && target instanceof ProtoChunk protoChunk) {
                MinecraftVersionCompat.markPosForPostProcessing(protoChunk, pos);
             }
          }
       );
       if (!applyCaves) {
          return;
       }
       BiomeManager carvedBiomeManager = biomeManager.withDifferentSource(
          this.biomeSource.createResolver(
             safeRandomState.createClimateSampler(SamplerContext.builder().build())
          )
       );
       WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
       ChunkPos targetPos = chunk.getPos();
       FluidPicker fluidPicker = Objects.requireNonNull(createFluidPicker(this.chunkMinY + 8, tellusSeaLevel), "fluidPicker");
       NoiseSettings safeNoise = safeNoiseSettings.noiseSettings();
       try (NoiseChunk noiseChunk = new NoiseChunk(
          safeRandomState,
          Beardifier.forStructuresInChunk(safeStructures, targetPos),
          safeNoiseSettings,
          fluidPicker,
          Blender.empty(),
          new DensityVolume(
             16,
             safeNoise.height(),
             16,
             targetPos.getMinBlockX(),
             safeNoise.minY(),
             targetPos.getMinBlockZ()
          )
       )) {
       WorldGenerationContext worldGenerationContext = new WorldGenerationContext(
          safeCarvingContextGenerator, chunk.getHeightAccessorForGeneration()
       );
       CarverOutput carvingOutput = Objects.requireNonNull(
          getCarvingOutput(
             chunk,
             cavesReachSurface,
             surfaceYByColumn,
             floodGuardYByColumn,
             generationFloorYByColumn,
             structureExclusions
          ),
          "carvingOutput"
       );

       for (int offsetX = -CARVER_RADIUS_CHUNKS; offsetX <= CARVER_RADIUS_CHUNKS; offsetX++) {
          for (int offsetZ = -CARVER_RADIUS_CHUNKS; offsetZ <= CARVER_RADIUS_CHUNKS; offsetZ++) {
             ChunkPos sourcePos = new ChunkPos(
                MinecraftVersionCompat.chunkX(targetPos) + offsetX,
                MinecraftVersionCompat.chunkZ(targetPos) + offsetZ
             );
             int sourceSurfaceY = safeSurfaceHeightSampler.applyAsInt(sourcePos.getMinBlockX() + 8, sourcePos.getMinBlockZ() + 8);
             List<WorldCarver> sourceCarvers = this.configuredCarvers.orderedCarvers(sourceSurfaceY);

             for (int carverIndex = 0; carverIndex < sourceCarvers.size(); carverIndex++) {
                WorldCarver carver = sourceCarvers.get(carverIndex);
                random.setLargeFeatureSeed(
                   worldSeed + carverIndex,
                   MinecraftVersionCompat.chunkX(sourcePos),
                   MinecraftVersionCompat.chunkZ(sourcePos)
                );
                carver.carve(
                   worldGenerationContext,
                   random,
                   sourcePos,
                   targetPos,
                   carvingOutput
                );
             }
          }
       }
       }
    }


    private static CarverOutput getCarvingOutput(
       ChunkAccess chunk,
       boolean cavesReachSurface,
       int[] surfaceYByColumn,
       int[] floodGuardYByColumn,
       int[] generationFloorYByColumn,
       List<UndergroundStructureExclusion.Box> structureExclusions
    ) {
       CarvingMask baseMask = new CarvingMask(chunk.getMinY(), chunk.getMinY() + chunk.getHeight() - 1);

       if ((cavesReachSurface || surfaceYByColumn == null)
          && floodGuardYByColumn == null
          && generationFloorYByColumn == null
          && (structureExclusions == null || structureExclusions.isEmpty())) {
          return Objects.requireNonNull(baseMask, "baseMask");
       } else {
          int chunkMinX = chunk.getPos().getMinBlockX();
          int chunkMinZ = chunk.getPos().getMinBlockZ();
          return new CarverOutput() {
             @Override
             public void carve(int x, int y, int z) {
                int localX = x & 15;
                int localZ = z & 15;
                int index = chunkIndex(localX, localZ);
                if (!cavesReachSurface && surfaceYByColumn != null && y >= surfaceYByColumn[index] - 4) {
                   return;
                }
                if (floodGuardYByColumn != null && y >= floodGuardYByColumn[index]) {
                   return;
                }
                if (generationFloorYByColumn != null && y <= generationFloorYByColumn[index]) {
                   return;
                }
                if (UndergroundStructureExclusion.blocksCarving(
                   structureExclusions, chunkMinX + localX, y, chunkMinZ + localZ
                )) {
                   return;
                }
                baseMask.carve(x, y, z);
             }

             @Override
             public int minY() {
                return baseMask.minY();
             }

             @Override
             public int maxY() {
                return baseMask.maxY();
             }
          };
       }
    }


    private static FluidPicker createFluidPicker(int lavaLevel, int seaLevel) {
       FluidStatus lava = new FluidStatus(lavaLevel, Blocks.LAVA.defaultBlockState());
       FluidStatus water = new FluidStatus(seaLevel, Blocks.WATER.defaultBlockState());
       return Objects.requireNonNull((x, y, z) -> y < lavaLevel ? lava : water, "fluidPicker");
    }

    private static int chunkIndex(int localX, int localZ) {
       return localZ << 4 | localX;
    }
}
