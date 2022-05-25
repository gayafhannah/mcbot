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
        int chunkX = (int)Math.floor((float)blockX/16);
        int chunkZ = (int)Math.floor((float)blockZ/16);
        long chunkXZ = ((long)chunkX << 32) | ((long)chunkZ << 32 >>> 32);
        // Get Local BlockXYZ from Global BlockXYZ
        //if (blockId==5866) {
        //    System.out.printf("X:%d Y:%d Z:%d ID:%d %d %d\n",blockX,blockY,blockZ,blockId, chunkX,chunkZ);}
        //if ((blockX==-7)&&(blockY==-60)&&(blockZ==537)) {
        //    System.out.printf("aaa:%d\n",blockId);
        //}
        //blockX = blockX%16;
        //blockY = blockY%16;
        //blockZ = blockZ%16;
        long blockXYZ = (blockX&0xF) << 16 | (blockY&0xFFF) << 4 | (blockZ&0xF);
        //if ((blockX==-7)&&(blockY==-60)&&(blockZ==537)) {
        //    System.out.printf("X:%x Y:%x Z:%x XYZ:%x ID:%d %d %d\n",blockX,blockY,blockZ,blockXYZ,blockId,chunkX,chunkZ);}
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
        //System.out.printf("%d %d %d %x\n",blockX,blockY,blockZ,blockXYZ);
        //System.out.printf("%x %x %x %x %d %d\n",blockX,blockY,blockZ,blockXYZ,chunkX,chunkZ);
        Chunk chunk = chunkMap.get(chunkXZ);
        if (chunk==null) {return 0;}
        Integer block = chunk.blockMap.get(blockXYZ);
        if (block==null) {return 0;}
        return block;
    }
}
