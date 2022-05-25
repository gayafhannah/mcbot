package mcbot;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import mcbot.Utilities;

public class Chunks {
    public ConcurrentHashMap<Long, Chunk> chunkMap = new ConcurrentHashMap<Long, Chunk>();
    public Client client;

    public Chunks(Client _client) {client = _client;}

    class Chunk {
        public ConcurrentHashMap<Long, Integer> blockMap = new ConcurrentHashMap<Long, Integer>();
    }
    /*class Block { // Not needed, might use later if additional storage is needed per block
        public int id;
    }*/

    public Chunk newChunk(int chunkX, int chunkZ) {
        long chunkXZ = ((long)chunkX << 32) | ((long)chunkZ << 32 >>> 32);
        Chunk chunk = new Chunk();
        chunkMap.put(chunkXZ, chunk);
        return chunk;
    }

    public void delChunk(int chunkX, int chunkZ) {
        long chunkXZ = ((long)chunkX << 32) | ((long)chunkZ << 32 >>> 32);
        chunkMap.remove(chunkXZ);
    }

    public void setBlock(int blockId, int blockX, int blockY, int blockZ) {
        // Get ChunkXZ from Global BlockXZ
        int chunkX = (int)Math.floor((float)blockX/16);
        int chunkZ = (int)Math.floor((float)blockZ/16);
        long chunkXZ = ((long)chunkX << 32) | ((long)chunkZ << 32 >>> 32);
        // Get Local BlockXYZ from Global BlockXYZ
        //blockX = blockX%16;
        //blockY = blockY%16;
        //blockZ = blockZ%16;
        long blockXYZ = (blockX&0xF) << 16 | (blockY&0xFFF) << 4 | (blockZ&0xF);
        Chunk chunk = chunkMap.get(chunkXZ);
        if (chunk==null) {chunk = newChunk(chunkX,chunkZ);}
        if (blockId==0) {chunk.blockMap.remove(blockXYZ); return;}
        chunk.blockMap.put(blockXYZ, blockId);
    }

    public int getBlock(int blockX, int blockY, int blockZ) {
        // Get ChunkXZ from Global BlockXZ
        int chunkX = (int)Math.floor((float)blockX/16);
        int chunkZ = (int)Math.floor((float)blockZ/16);
        long chunkXZ = ((long)chunkX << 32) | ((long)chunkZ << 32 >>> 32);
        // Get Local BlockXYZ from Global BlockXYZ
        //blockX = blockX%16;
        //blockY = blockY%16;
        //blockZ = blockZ%16;
        //int blockXYZ = ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF);
        long blockXYZ = (blockX&0xF) << 16 | (blockY&0xFFF) << 4 | (blockZ&0xF);
        Chunk chunk = chunkMap.get(chunkXZ);
        if (chunk==null) {return 0;}
        Integer block = chunk.blockMap.get(blockXYZ);
        if (block==null) {return 0;}
        return block;
    }
}
