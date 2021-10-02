package cursedflames.cubeloader.chunkloading;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cursedflames.cubeloader.CubeLoader;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.UsernameCache;

//TODO fix chunk unload on singleplayer dimension change
public class ChunkloaderManager extends WorldSavedData {
	public static final String DATA_NAME = CubeLoader.MODID+"_ChunkloaderManager";

	private final Map<UUID, PlayerChunkloaders> loaders = new HashMap<>();
//	private Map<UUID, String> playerNames = new HashMap<>();

	public ChunkloaderManager() {
		super(DATA_NAME);
	}

	// Apparently this constructor is required?
	public ChunkloaderManager(String s) {
		super(s);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		this.loaders.clear();

		if (!tag.hasKey("chunkloaders"))
			return;
		NBTTagCompound tag1 = tag.getCompoundTag("chunkloaders");
		tag1.getKeySet().forEach(key -> this.loaders.put(UUID.fromString(key), new PlayerChunkloaders().loadChunkloadersNBT((NBTTagList) tag1.getTag(key))));
	}

	// TODO this will probably break if loaders is modified concurrently
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		NBTTagCompound tag1 = new NBTTagCompound();
		this.loaders.forEach((uuid, chunkloaders) -> tag1.setTag(uuid.toString(), chunkloaders.getChunkloadersNBT()));
		tag.setTag("chunkloaders", tag1);
		return tag;
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	public static ChunkloaderManager getInstance(World world) {
		if (world==null)
			return null;

		ChunkloaderManager instance = (ChunkloaderManager) world.getPerWorldStorage().getOrLoadData(ChunkloaderManager.class, DATA_NAME);
		if (instance == null) {
			instance = new ChunkloaderManager();
			world.getPerWorldStorage().setData(DATA_NAME, instance);
		}

		return instance;
	}

	public void tick() {
	}

	public void reloadChunkloaders() {
		CubeLoader.logger.info("Reloading chunkloaders...");
		loaders.values().forEach(playerChunkloaders -> playerChunkloaders.reloadChunkloaders());
	}

	public PlayerChunkloaders getPlayerChunkloaders(UUID id) {
		PlayerChunkloaders playerLoaders = loaders.get(id);
		if (playerLoaders==null) {
			playerLoaders = new PlayerChunkloaders();
			loaders.put(id, playerLoaders);
		}
		return playerLoaders;
	}

	public String getPlayerName(UUID id) {
		return UsernameCache.getLastKnownUsername(id);// playerNames.get(id);
	}

//	public void setPlayerName(UUID id, String name) {
//		playerNames.put(id, name);
//		markDirty();
//	}
}
