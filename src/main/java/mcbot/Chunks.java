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
    class Block {
        public int id;
    }

    public Chunk newChunk(int chunkX, int chunkZ) {
        long chunkXZ = ((long)chunkX << 32) | ((long)chunkZ << 32 >>> 32);
        //System.out.printf("CX: %x\nCZ: %x\nCXZ: %x\n",chunkX,chunkZ,chunkXZ);
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
        int chunkX = blockX/16;
        int chunkZ = blockZ/16;
        long chunkXZ = ((long)chunkX << 32) | ((long)chunkZ << 32 >>> 32);
        // Get Local BlockXYZ from Global BlockXYZ
        blockX = blockX%16;
        blockY = blockY%16;
        blockZ = blockZ%16;
        long blockXYZ = (blockX&0xF) << 8 | (blockY&0xF) << 4 | (blockZ&0xF);
        //System.out.printf("X:%x Y:%x Z:%x XYZ:%x\n",blockX,blockY,blockZ,blockXYZ);
        Chunk chunk = chunkMap.get(chunkXZ);
        if (chunk==null) {chunk = newChunk(chunkX,chunkZ);}
        chunk.blockMap.put(blockXYZ, blockId);
    }

    public int getBlock(int blockX, int blockY, int blockZ) {
        // Get ChunkXZ from Global BlockXZ
        int chunkX = blockX/16;
        int chunkZ = blockZ/16;
        long chunkXZ = ((long)chunkX << 32) | ((long)chunkZ << 32 >>> 32);
        // Get Local BlockXYZ from Global BlockXYZ
        blockX = blockX%16;
        blockY = blockY%16;
        blockZ = blockZ%16;
        //int blockXYZ = ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF);
        long blockXYZ = (blockX&0xF) << 8 | (blockY&0xF) << 4 | (blockZ&0xF);
        Chunk chunk = chunkMap.get(chunkXZ);
        if (chunk==null) {return 0;}
        return chunk.blockMap.get(blockXYZ);
    }
}
