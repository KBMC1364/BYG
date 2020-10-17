package corgiaoc.byg.common.world.feature.overworld.volcano;

import com.mojang.serialization.Codec;
import corgiaoc.byg.common.world.feature.config.SimpleBlockProviderConfig;
import corgiaoc.byg.util.noise.fastnoise.FastNoise;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;

public class VolcanoFeature extends Feature<SimpleBlockProviderConfig> {

    FastNoise perlin = null;

    public VolcanoFeature(Codec<SimpleBlockProviderConfig> codec) {
        super(codec);
    }

    @Override
    public boolean func_241855_a(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, SimpleBlockProviderConfig config) {
        setSeed(world.getSeed());


        if (world.getBlockState(pos.down()).getMaterial() == Material.AIR || world.getBlockState(pos.down()).getMaterial() == Material.WATER || world.getBlockState(pos.down()).getMaterial() == Material.LAVA || world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, pos.getX(), pos.getZ()) < 4)
            return false;


        BlockPos.Mutable mutable = new BlockPos.Mutable();

        double baseRadius = 4;
        int lavaLeakage = 2;
        int volcanoConeSize = 45;
        int volcanoStartHeight = volcanoConeSize - 5;

        for (double x = -volcanoConeSize; x <= volcanoConeSize; x++) {
            for (double y = -volcanoConeSize; y <= -1; y++) {
                for (double z = -volcanoConeSize; z <= volcanoConeSize; z++) {
                    mutable.setPos(pos).move((int)x, (int)y + volcanoStartHeight, (int)z);
                    double noise = perlin.GetNoise(mutable.getX(), mutable.getY(), mutable.getZ()) * 12;
                    double scaledNoise = (noise / 11) * (-(y * baseRadius) / ((x * x) + (z * z)));

                    if (scaledNoise - lavaLeakage >= 0.5) {
                        if (mutable.getY() <= pos.getY() + (volcanoStartHeight - 11)) {
                            world.setBlockState(mutable, Blocks.LAVA.getDefaultState(), 2);
                            world.getPendingFluidTicks().scheduleTick(mutable, Fluids.LAVA, 0);
                        }
                    }
                    else if (scaledNoise >= 0.5) {
                        if (world.getBlockState(mutable).getMaterial() == Material.AIR) {
                            world.setBlockState(mutable, config.getBlockProvider().getBlockState(rand, mutable), 2);
                        }
                    }
                }
            }
        }
        return true;
    }


    public void setSeed(long seed) {
        if (perlin == null) {
            perlin = new FastNoise((int) seed);
            perlin.SetNoiseType(FastNoise.NoiseType.Perlin);
            perlin.SetFrequency(0.2F);
        }
    }
}
