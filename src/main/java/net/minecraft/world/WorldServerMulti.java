package net.minecraft.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class WorldServerMulti extends WorldServer {
    private final WorldServer delegate;
    private IBorderListener borderListener;

    // CraftBukkit start - Add WorldInfo, Environment and ChunkGenerator arguments
    public WorldServerMulti(MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId, WorldServer delegate, Profiler profilerIn, WorldInfo worldData, org.bukkit.World.Environment env, org.bukkit.generator.ChunkGenerator gen, String worldName) {
        super(server, saveHandlerIn, worldData, dimensionId, profilerIn, env, gen, worldName);
        this.delegate = delegate;
        /* CraftBukkit start
        this.borderListener = new IBorderListener()
        {
            public void onSizeChanged(WorldBorder border, double newSize)
            {
                WorldServerMulti.this.getWorldBorder().setTransition(newSize);
            }
            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time)
            {
                WorldServerMulti.this.getWorldBorder().setTransition(oldSize, newSize, time);
            }
            public void onCenterChanged(WorldBorder border, double x, double z)
            {
                WorldServerMulti.this.getWorldBorder().setCenter(x, z);
            }
            public void onWarningTimeChanged(WorldBorder border, int newTime)
            {
                WorldServerMulti.this.getWorldBorder().setWarningTime(newTime);
            }
            public void onWarningDistanceChanged(WorldBorder border, int newDistance)
            {
                WorldServerMulti.this.getWorldBorder().setWarningDistance(newDistance);
            }
            public void onDamageAmountChanged(WorldBorder border, double newAmount)
            {
                WorldServerMulti.this.getWorldBorder().setDamageAmount(newAmount);
            }
            public void onDamageBufferChanged(WorldBorder border, double newSize)
            {
                WorldServerMulti.this.getWorldBorder().setDamageBuffer(newSize);
            }
        };
        this.delegate.getWorldBorder().addListener(this.borderListener);
        // CraftBukkit end */
    }

    public WorldServerMulti(MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId, WorldServer delegate, Profiler profilerIn) {
        super(server, saveHandlerIn, new DerivedWorldInfo(delegate.getWorldInfo()), dimensionId, profilerIn);
        this.delegate = delegate;
        this.borderListener = new IBorderListener() {
            public void onSizeChanged(WorldBorder border, double newSize) {
                WorldServerMulti.this.getWorldBorder().setTransition(newSize);
            }

            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time) {
                WorldServerMulti.this.getWorldBorder().setTransition(oldSize, newSize, time);
            }

            public void onCenterChanged(WorldBorder border, double x, double z) {
                WorldServerMulti.this.getWorldBorder().setCenter(x, z);
            }

            public void onWarningTimeChanged(WorldBorder border, int newTime) {
                WorldServerMulti.this.getWorldBorder().setWarningTime(newTime);
            }

            public void onWarningDistanceChanged(WorldBorder border, int newDistance) {
                WorldServerMulti.this.getWorldBorder().setWarningDistance(newDistance);
            }

            public void onDamageAmountChanged(WorldBorder border, double newAmount) {
                WorldServerMulti.this.getWorldBorder().setDamageAmount(newAmount);
            }

            public void onDamageBufferChanged(WorldBorder border, double newSize) {
                WorldServerMulti.this.getWorldBorder().setDamageBuffer(newSize);
            }
        };
        this.delegate.getWorldBorder().addListener(this.borderListener);
    }

    /*
        Disable this method in favour of WorldServer#saveLevel(), so level.dat can be saved for each dimension
        separately, as it is required by CraftBukkit mechanic: CraftBukkit changes world directories hierarchy
        to separate each world in its own directory, instead of locating them in the world directory via inner
        directories. Thus, it makes each directory(each world) having its own level.dat. Some plugins, such as
        MultiVerse, rely on this mechanic, saving some information to level.dat while unloading worlds. By
        disabling this method we return this behaviour back, so level.dat is created for each world now.
     */
//    protected void saveLevel() throws MinecraftException
//    {
//        this.perWorldStorage.saveAllData();
//    }

    public World init() {
        this.mapStorage = this.delegate.getMapStorage();
        this.worldScoreboard = this.delegate.getScoreboard();
        this.lootTable = this.delegate.getLootTableManager();
        this.advancementManager = this.delegate.getAdvancementManager();
        String s = VillageCollection.fileNameForProvider(this.provider);
        VillageCollection villagecollection = (VillageCollection) this.perWorldStorage.getOrLoadData(VillageCollection.class, s);

        if (villagecollection == null) {
            this.villageCollection = new VillageCollection(this);
            this.perWorldStorage.setData(s, this.villageCollection);
        } else {
            this.villageCollection = villagecollection;
            this.villageCollection.setWorldsForAll(this);
        }

        this.initCapabilities();
        // return this;
        return super.init(); // CraftBukkit
    }


    @Override
    public void flush() {
        super.flush();
        this.delegate.getWorldBorder().removeListener(this.borderListener); // Unlink ourselves, to prevent world leak.
    }

    public void saveAdditionalData() {
        this.provider.onWorldSave();
    }
}