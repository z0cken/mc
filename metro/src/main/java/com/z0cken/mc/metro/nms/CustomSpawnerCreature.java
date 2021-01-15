/*package com.z0cken.mc.metro.nms;

import com.z0cken.mc.metro.PCS_Metro;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.craftbukkit.v1_13_R2.util.LongHash;
import org.bukkit.craftbukkit.v1_13_R2.util.LongHashSet;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("Duplicates")
public class CustomSpawnerCreature {

    private static final Logger log = PCS_Metro.getInstance().getLogger();

    //b (equals to 289)
    private static final int sevenTeenSquared = (int) Math.pow(17.0D, 2.0D);
    //c
    private final LongHashSet chunks = new LongHashSet();

    public int a(WorldServer worldserver, boolean spawnMonsters, boolean spawnAnimals, boolean fourHundredthTick) {
        if (!spawnMonsters && !spawnAnimals) {
            return 0;
        } else {
            this.chunks.clear();

            //i
            int chunkCount = 0;

            for (EntityHuman entityhuman : worldserver.players) {
                if (!entityhuman.isSpectator()) {

                    //b0
                    byte mobSpawnRange = worldserver.spigotConfig.mobSpawnRange;

                    mobSpawnRange = mobSpawnRange > worldserver.spigotConfig.viewDistance ? (byte) worldserver.spigotConfig.viewDistance : mobSpawnRange;
                    mobSpawnRange = mobSpawnRange > 8 ? 8 : mobSpawnRange;

                    //il
                    for (int x = -mobSpawnRange; x <= mobSpawnRange; ++x) {
                        //k
                        for (int z = -mobSpawnRange; z <= mobSpawnRange; ++z) {
                            boolean flag4 = x == -mobSpawnRange || x == mobSpawnRange || z == -mobSpawnRange || z == mobSpawnRange;
                            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(x + MathHelper.floor(entityhuman.locX / 16.0D), z + MathHelper.floor(entityhuman.locZ / 16.0D));
                            long chunkCoords = LongHash.toLong(chunkcoordintpair.x, chunkcoordintpair.z);

                            if (!this.chunks.contains(chunkCoords)) {
                                ++chunkCount;
                                if (!flag4 && worldserver.getWorldBorder().isInBounds(chunkcoordintpair)) {
                                    PlayerChunk playerchunk = worldserver.getPlayerChunkMap().getChunk(chunkcoordintpair.x, chunkcoordintpair.z);

                                    if (playerchunk != null && playerchunk.e()) {
                                        this.chunks.add(chunkCoords);
                                    }
                                }
                            }
                        } //for
                    } //for
                }
            } //for

            int j1 = 0;

            for (EnumCreatureType enumcreaturetype : EnumCreatureType.values()) {
                int limit = enumcreaturetype.b();

                switch (enumcreaturetype) {
                    case MONSTER:
                        limit = worldserver.getWorld().getMonsterSpawnLimit();
                        break;
                    case CREATURE:
                        limit = worldserver.getWorld().getAnimalSpawnLimit();
                        break;
                    case AMBIENT:
                        limit = worldserver.getWorld().getAmbientSpawnLimit();
                        break;
                    case WATER_CREATURE:
                        limit = worldserver.getWorld().getWaterAnimalSpawnLimit();
                }

                if (limit != 0 && (!enumcreaturetype.c() || spawnAnimals) && (enumcreaturetype.c() || spawnMonsters) && (!enumcreaturetype.d() || fourHundredthTick)) {

                    //k
                    int maxAmount = limit * chunkCount / sevenTeenSquared;

                    //l1
                    int currentAmount = worldserver.a(enumcreaturetype.a(), maxAmount);

                    if (currentAmount <= maxAmount) {
                        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
                        Iterator chunkIterator = this.chunks.iterator();

                        label159:
                        while (chunkIterator.hasNext()) {
                            long key = (Long) chunkIterator.next();
                            //blockposition1
                            BlockPosition randomPosition = getRandomPosition(worldserver, LongHash.msw(key), LongHash.lsw(key));

                            //i2
                            int randomX = randomPosition.getX();
                            //j2
                            int randomY = randomPosition.getY();
                            //k2
                            int randomZ = randomPosition.getZ();

                            //If block doesn't completely block vision
                            if (!worldserver.getType(randomPosition).isOccluding()) {
                                int l2 = 0;

                                //i3
                                for (int i = 0; i < 3; ++i) {

                                    //j3
                                    int x = randomX;
                                    //k3
                                    int y = randomY;
                                    //l3
                                    int z = randomZ;

                                    BiomeBase.BiomeMeta biomebase_biomemeta = null;
                                    GroupDataEntity groupdataentity = null;

                                    //i4 = Random number from 1 to 4
                                    //MathHelper.f(double) rounds up
                                    int r = MathHelper.f(Math.random() * 4.0D);
                                    int j4 = 0;

                                    //k4
                                    for (int j = 0; j < r; ++j) {
                                        x += worldserver.random.nextInt(6) - worldserver.random.nextInt(6);
                                        y += worldserver.random.nextInt(1) - worldserver.random.nextInt(1);
                                        z += worldserver.random.nextInt(6) - worldserver.random.nextInt(6);
                                        blockposition_mutableblockposition.c(x, y, z);

                                        //f
                                        float x1 = (float) x + 0.5F;
                                        //f1
                                        float z1 = (float) z + 0.5F;

                                        //entityhuman1
                                        EntityHuman entityHuman = worldserver.a((double) x1, (double) z1, -1.0D);

                                        if (entityHuman != null) {
                                            //d0
                                            double distanceSquared = entityHuman.d((double) x1, (double) y, (double) z1);

                                            //sqrt(576) = 24
                                            if (distanceSquared > 576.0D && worldserver.getSpawn().distanceSquared((double) x1, (double) y, (double) z1) >= 576.0D) {
                                                if (biomebase_biomemeta == null) {
                                                    biomebase_biomemeta = worldserver.a(enumcreaturetype, blockposition_mutableblockposition);
                                                    if (biomebase_biomemeta == null) {
                                                        break;
                                                    }

                                                    r = biomebase_biomemeta.c + worldserver.random.nextInt(1 + biomebase_biomemeta.d - biomebase_biomemeta.c);
                                                }

                                                if (worldserver.a(enumcreaturetype, biomebase_biomemeta, blockposition_mutableblockposition)) {
                                                    EntityPositionTypes.Surface entitypositiontypes_surface = EntityPositionTypes.a(biomebase_biomemeta.b);

                                                    if (entitypositiontypes_surface != null && a(entitypositiontypes_surface, worldserver, blockposition_mutableblockposition, biomebase_biomemeta.b)) {
                                                        EntityInsentient entityinsentient;

                                                        try {
                                                            entityinsentient = biomebase_biomemeta.b.a((World) worldserver);
                                                        } catch (Exception exception) {
                                                            log.log(Level.WARNING, "Failed to create mob", exception);
                                                            return j1;
                                                        }

                                                        entityinsentient.setPositionRotation((double) x1, (double) y, (double) z1, worldserver.random.nextFloat() * 360.0F, 0.0F);

                                                        //sqrt(16384) = 128
                                                        if ((distanceSquared <= 16384.0D || !entityinsentient.isTypeNotPersistent()) && entityinsentient.a(worldserver, false) && entityinsentient.a(worldserver)) {
                                                            groupdataentity = entityinsentient.prepare(worldserver.getDamageScaler(new BlockPosition(entityinsentient)), groupdataentity, null);

                                                            //if hitbox in applicable liquid? complicated.
                                                            if (entityinsentient.a(worldserver)) {
                                                                if (worldserver.addEntity(entityinsentient, CreatureSpawnEvent.SpawnReason.NATURAL)) {
                                                                    ++l2;
                                                                    ++j4;
                                                                }
                                                            } else {
                                                                entityinsentient.die();
                                                            }

                                                            //If saturated jump to next chunk
                                                            if (l2 >= entityinsentient.dg()) {
                                                                continue label159;
                                                            }

                                                            //Only for tropical fish - arbitrary value
                                                            if (entityinsentient.c(404)) {
                                                                break;
                                                            }
                                                        }

                                                        j1 += l2;
                                                    } //if mob can spawn on this surface??
                                                } //if mob can spawn in this biome
                                            } //if player and world-spawn further away than 24
                                        } //if player found
                                    } //for random number of 1-4 times (select block with random offset +/- 6|1|6)
                                } //for 3 times
                            } //if random block's type is non occluding
                        } //while (iterate all selected chunks)
                    } //if current amount of entity type < maximum amount
                }
            } //for each creature type

            return j1;
        }
    }

    private static BlockPosition getRandomPosition(World world, int i, int j) {
        Chunk chunk = world.getChunkAt(i, j);
        int x = i * 16 + world.random.nextInt(16);
        int z = j * 16 + world.random.nextInt(16);
        int maxHeight = chunk.a(HeightMap.Type.LIGHT_BLOCKING, x, z) + 1;
        int y = world.random.nextInt(maxHeight + 1);

        return new BlockPosition(x, y, z);
    }

    public static boolean a(IBlockData iblockdata, Fluid fluid) {
        return !iblockdata.k() && (!iblockdata.isPowerSource() && (fluid.e() && !iblockdata.a(TagsBlock.RAILS)));
    }

    //Possibly determining if mob can spawn in this position
    public static boolean a(EntityPositionTypes.Surface entitypositiontypes_surface, IWorldReader iworldreader, BlockPosition blockposition, @Nullable EntityTypes entitytypes) {
        if (entitytypes != null && iworldreader.getWorldBorder().a(blockposition)) {
            IBlockData iblockdata = iworldreader.getType(blockposition);
            Fluid fluid = iworldreader.getFluid(blockposition);

            switch (EntityPositionTypes.Surface.values()[entitypositiontypes_surface.ordinal()]) {
                case ON_GROUND:
                default:
                    IBlockData iblockdata1 = iworldreader.getType(blockposition.down());

                    if (!iblockdata1.q() && (entitytypes == null || !EntityPositionTypes.a(entitytypes, iblockdata1))) {
                        return false;
                    } else {
                        Block block = iblockdata1.getBlock();
                        boolean flag = block != Blocks.BEDROCK && block != Blocks.BARRIER;

                        return flag && a(iblockdata, fluid) && a(iworldreader.getType(blockposition.up()), iworldreader.getFluid(blockposition.up()));
                    }
                case IN_WATER:
                    return fluid.a(TagsFluid.WATER) && iworldreader.getFluid(blockposition.down()).a(TagsFluid.WATER) && !iworldreader.getType(blockposition.up()).isOccluding();
            }
        } else {
            return false;
        }
    }

    //Exclusively used on generating chunks
    public static void a(GeneratorAccess generatoraccess, BiomeBase biomebase, int i, int j, Random random) {
        List list = biomebase.getMobs(EnumCreatureType.CREATURE);

        if (!list.isEmpty()) {
            int k = i << 4;
            int l = j << 4;

            while (random.nextFloat() < biomebase.e()) {
                BiomeBase.BiomeMeta biomebase_biomemeta = (BiomeBase.BiomeMeta) WeightedRandom.a(random, list);
                int i1 = biomebase_biomemeta.c + random.nextInt(1 + biomebase_biomemeta.d - biomebase_biomemeta.c);
                GroupDataEntity groupdataentity = null;
                int j1 = k + random.nextInt(16);
                int k1 = l + random.nextInt(16);
                int l1 = j1;
                int i2 = k1;

                for (int j2 = 0; j2 < i1; ++j2) {
                    boolean flag = false;

                    for (int k2 = 0; !flag && k2 < 4; ++k2) {
                        BlockPosition blockposition = a(generatoraccess, biomebase_biomemeta.b, j1, k1);

                        if (a(EntityPositionTypes.Surface.ON_GROUND, generatoraccess, blockposition, biomebase_biomemeta.b)) {
                            EntityInsentient entityinsentient;

                            try {
                                entityinsentient = biomebase_biomemeta.b.a(generatoraccess.getMinecraftWorld());
                            } catch (Exception exception) {
                                log.log(Level.WARNING, "Failed to create mob", exception);
                                continue;
                            }

                            double d0 = MathHelper.a((double) j1, (double) k + (double) entityinsentient.width, (double) k + 16.0D - (double) entityinsentient.width);
                            double d1 = MathHelper.a((double) k1, (double) l + (double) entityinsentient.width, (double) l + 16.0D - (double) entityinsentient.width);

                            entityinsentient.setPositionRotation(d0, (double) blockposition.getY(), d1, random.nextFloat() * 360.0F, 0.0F);
                            if (entityinsentient.a(generatoraccess, false) && entityinsentient.a(generatoraccess)) {
                                groupdataentity = entityinsentient.prepare(generatoraccess.getDamageScaler(new BlockPosition(entityinsentient)), groupdataentity, null);
                                generatoraccess.addEntity(entityinsentient, CreatureSpawnEvent.SpawnReason.CHUNK_GEN);
                                flag = true;
                            }
                        }

                        j1 += random.nextInt(5) - random.nextInt(5);

                        for (k1 += random.nextInt(5) - random.nextInt(5); j1 < k || j1 >= k + 16 || k1 < l || k1 >= l + 16; k1 = i2 + random.nextInt(5) - random.nextInt(5)) {
                            j1 = l1 + random.nextInt(5) - random.nextInt(5);
                        }
                    }
                }
            }
        }

    }

    private static BlockPosition a(GeneratorAccess generatoraccess, @Nullable EntityTypes entitytypes, int i, int j) {
        BlockPosition blockposition = new BlockPosition(i, generatoraccess.a(EntityPositionTypes.b(entitytypes), i, j), j);
        BlockPosition blockposition1 = blockposition.down();

        return generatoraccess.getType(blockposition1).a(generatoraccess, blockposition1, PathMode.LAND) ? blockposition1 : blockposition;
    }
}*/
