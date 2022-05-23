package mcbot;

import mcbot.Utilities;
import mcbot.Client;
import mcbot.Entity;
import mcbot.Inventory;

import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.util.regex.*;

public class Clientbound {
    // Handshake mode
    public static void loginFail(Client client, ByteArrayInputStream data) {
        byte[] d = data.readAllBytes();
        for (int i=0;i<d.length;i++) {
            System.out.print((char)d[i]);
        }
        client.alive = false;
    }

    public static void loginSuccess(Client client) {
        System.out.printf("<%s> Joined Server!\n", client.username);
        client.mode = 1;
    }

    public static void setCompression(Client client, ByteArrayInputStream data) throws IOException {
        int compression = Utilities.readVarInt(data);
        System.out.printf("<%s> Compression threshold set to: %d\n", client.username, compression);
        client.compression = compression;
    }

    //Normal mode

    public static void spawnEntity(Client client, ByteArrayInputStream data) throws IOException { // 0x00 0x02
        int id = Utilities.readVarInt(data);
        data.skip(16);
        int type = Utilities.readVarInt(data);
        byte[] x = new byte[8];
        byte[] y = new byte[8];
        byte[] z = new byte[8];
        data.read(x, 0, 8);
        data.read(y, 0, 8);
        data.read(z, 0, 8);
        double xx = ByteBuffer.wrap(x).getDouble();
        double yy = ByteBuffer.wrap(y).getDouble();
        double zz = ByteBuffer.wrap(z).getDouble();

        if (client.entities.containsKey(id) == false) {
            Entity e = new Entity();
            e.id = id;
            e.type = type;
            e.x = xx;
            e.y = yy;
            e.z = zz;
            client.entities.put(id, e);
        }
    }

    public static void spawnPlayer(Client client, ByteArrayInputStream data) throws IOException { // 0x04
        int id = Utilities.readVarInt(data);
        data.skip(16);
        byte[] x = new byte[8];
        byte[] y = new byte[8];
        byte[] z = new byte[8];
        data.read(x, 0, 8);
        data.read(y, 0, 8);
        data.read(z, 0, 8);
        double xx = ByteBuffer.wrap(x).getDouble();
        double yy = ByteBuffer.wrap(y).getDouble();
        double zz = ByteBuffer.wrap(z).getDouble();

        if (client.entities.containsKey(id) == false) {
            Entity e = new Entity();
            e.id = id;
            e.type = 111; // Player type
            e.x = xx;
            e.y = yy;
            e.z = zz;
            client.entities.put(id, e);
        }
    }

    public static void blockChange(Client client, ByteArrayInputStream data) throws IOException { // 0x0C TODO
        byte[] locationBytes = new byte[8];
        data.read(locationBytes, 0, 8);
        long location = ByteBuffer.wrap(locationBytes).getLong();
        int x = (int)(location >> 38);
        int y = (int)(location & 0xFFF); if (y>=1<<11) {y-=1<<12;}
        int z = (int)(location >> 12) & 0x3FFFFFF;
        int blockId = Utilities.readVarInt(data);
        client.chunks.setBlock(blockId, x, y, z);
        //System.out.printf("ID: %d X:%d Y:%d Z:%d\n", blockId, x, y, z);
    }

    public static void chatMessage(Client client, ByteArrayInputStream data) throws IOException { // 0x0F
        String cMsg = Utilities.readString(data);
        if (Pattern.matches("^\\{\"translate\":\"chat\\.type\\.text\",\"with\":\\[\\{\"text\":\".*\"},\\{\"text\":\".*\"}]}",cMsg)) { // If normal chat message with nothing special at all
            String s = cMsg.substring(cMsg.indexOf("{\"text\":\"")+9, cMsg.indexOf("\"},{\"text"));
            String m = cMsg.substring(cMsg.lastIndexOf("{\"text\":\"")+9, cMsg.lastIndexOf("\"}]}"));
            String message = String.format("[%s] %s\n", s, m);
            if (s.equals("Shoe_Eater")) {
                String[] j = m.split(" ", 2);
                client.workerJobs.add(j);
            }
            //System.out.println(message);
        }
    }

    public static void closeWindow(Client client, ByteArrayInputStream data) throws IOException { // 0x13
        int id = (byte)data.read();
        client.inventories.remove(id);
        System.out.printf("C%d",id);
    }

    public static void windowItems(Client client, ByteArrayInputStream data) throws IOException, InterruptedException { // 0x14
        int wId = (byte)data.read();
        client.stateId = Utilities.readVarInt(data);
        int arraySize = Utilities.readVarInt(data);
        Inventory inventory = client.inventories.get(wId);
        if ((inventory==null)&&(wId==0)) {inventory = new Inventory(); client.inventories.put(0,new Inventory(-1));}
        Inventory.Slot slot;
        for (int i=0;i<arraySize;i++) {
            slot = Utilities.readSlot(inventory, data);
            inventory.slots.put(i,slot);
        }
    }

    public static void updateSlot(Client client, ByteArrayInputStream data) throws IOException, InterruptedException { // 0x16
        int wId = (byte)data.read();
        client.stateId = Utilities.readVarInt(data);
        byte[] s = new byte[2];
        data.read(s, 0, 2);
        int slotId = ByteBuffer.wrap(s).getShort();
        Inventory inventory = client.inventories.get(wId);
        if ((inventory==null)&&(wId==0)) {inventory = new Inventory(); client.inventories.put(0,new Inventory(-1));}
        Inventory.Slot slot = Utilities.readSlot(inventory, data);
        inventory.slots.put(slotId, slot);
    }

    public static void unloadChunk(Client client, ByteArrayInputStream data) throws IOException { // 0x1D
        byte[] chunkXBytes = new byte[4];
        byte[] chunkZBytes = new byte[4];
        data.read(chunkXBytes, 0, 4);
        data.read(chunkZBytes, 0, 4);
        int chunkX = ByteBuffer.wrap(chunkXBytes).getInt();
        int chunkZ = ByteBuffer.wrap(chunkZBytes).getInt();
        client.chunks.delChunk(chunkX, chunkZ);
    }

    public static void keepalive(Client client, ByteArrayInputStream data) throws IOException { // 0x21
        byte[] id = new byte[8];
        data.read(id, 0, 8);
        //System.out.printf("<%s> Recieved Keepalive.\n", client.username);
        Serverbound.keepalive(client, id);
    }

    public static void chunkLightData(Client client, ByteArrayInputStream data) throws IOException, InterruptedException { // 0x22 TODO
        byte[] chunkXBytes = new byte[4];
        byte[] chunkZBytes = new byte[4];
        data.read(chunkXBytes, 0, 4);
        data.read(chunkZBytes, 0, 4);
        int chunkX = ByteBuffer.wrap(chunkXBytes).getInt();
        int chunkZ = ByteBuffer.wrap(chunkZBytes).getInt();
        Utilities.ignoreNBT(data); // Ignore heightmaps NBT
        int dataStructureSize = Utilities.readVarInt(data); // Size of byte array for chunk data structure, rename later lol
        // Start array of Chunk Section (bottom to top)
        byte[] blockCountBytes;
        short blockCount;
        byte bitsPerEntry;
        byte[] blockBytes;
        long blockLong;
        int blocksArrayLength;
        int blockId;
        for (int chunkY=-4;chunkY<16;chunkY++) {
            blockCountBytes = new byte[2];
            data.read(blockCountBytes, 0, 2);
            blockCount = ByteBuffer.wrap(blockCountBytes).getShort();
            int k = 0;
            // Enter block states palleted container
            bitsPerEntry = (byte)data.read();
            if (bitsPerEntry==0) { // Single Valued
                blockId = Utilities.readVarInt(data);
                // all blocks are of this blockId
                //System.out.printf("Chunk %d %d %d is all ID:%d\n",chunkX, chunkY, chunkZ, blockId);
                for (int x=0;x<16;x++) {
                    for (int y=0;y<16;y++) {
                        for (int z=0;z<16;z++) {
                            client.chunks.setBlock(blockId, (chunkX*16)+x, (chunkY*16)+y, (chunkZ*16)+z);
                        }
                    }
                }
            } else if (bitsPerEntry <= 8) { // Indirect
                if (bitsPerEntry <= 4) {bitsPerEntry = 4;};
                int paletteLength = Utilities.readVarInt(data);
                int[] palette = new int[paletteLength];
                for (int i=0;i<paletteLength;i++) {
                    palette[i] = Utilities.readVarInt(data);
                }
                blocksArrayLength = Utilities.readVarInt(data);
                //TODO TODO TODO Read Data Array
//                System.out.printf("PX:%d CY:%d CZ:%d\n",chunkX, chunkY, chunkZ);
                for (int i=0;i<blocksArrayLength;i++) {
                    blockBytes = new byte[8];
                    data.read(blockBytes, 0, 8);
                    blockLong = ByteBuffer.wrap(blockBytes).getLong();
                    for (int j=0;j<(int)(64/bitsPerEntry);j++) {
                        blockId = palette[(int)(blockLong << (64-((j*bitsPerEntry)+bitsPerEntry)) >>> (64-bitsPerEntry))];
                        client.chunks.setBlock(blockId, (chunkX*16)+(k%16)/*Local X*/, (chunkY*16)+(((k/16)/16)%16)/*Local Y*/, (chunkZ*16)+((k/16)%16)/*Local Z*/);
                        //System.out.printf("X:%d Y:%d Z:%d ID:%d\n",(chunkX*16)+(k%16), (chunkY*16)+((k/(16)/16)%16), (chunkZ*16)+((k/16)%16), blockId);
                        k++;
                    }
                }
            } else { // Direct
                //TODO TODO TODO Read Data Array
                blocksArrayLength = Utilities.readVarInt(data);
//                System.out.printf("DX:%d CY:%d CZ:%d\n",chunkX, chunkY, chunkZ);
                for (int i=0;i<blocksArrayLength;i++) {
                    blockBytes = new byte[8];
                    data.read(blockBytes, 0, 8);
                    blockLong = ByteBuffer.wrap(blockBytes).getLong();
                    for (int j=0;j<(int)(64/bitsPerEntry);j++) {
                        blockId = (int)(blockLong << (64-((j*bitsPerEntry)+bitsPerEntry)) >>> (64-bitsPerEntry));
                        client.chunks.setBlock(blockId, (chunkX*16)+(k%16)/*Local X*/, (chunkY*16)+(((k/16/16))%16)/*Local Y*/, (chunkZ*16)+((k/16)%16)/*Local Z*/);
                        //System.out.printf("X:%d Y:%d Z:%d ID:%d\n",(chunkX*16)+(k%16), chunkY*16, (chunkZ*16)+((k/16)%16), blockId);
                        k++;
                    }
                }
            }
            //Start of Biomes palleted Container
            bitsPerEntry = (byte)data.read();
            if (bitsPerEntry==0) {
                Utilities.readVarInt(data);
            } else if (bitsPerEntry<=8) { // Read and ignore palette
                int paletteLength = Utilities.readVarInt(data);
                for (int i=0;i<paletteLength;i++) {Utilities.readVarInt(data);}
            }
            int arrayLength = Utilities.readVarInt(data);
            data.skip(arrayLength*8);
        }
        //System.out.println("ENDCHUNK");
    }

    public static void entityPos(Client client, ByteArrayInputStream data) throws IOException { // 0x29 0x2A
        int id = Utilities.readVarInt(data);
        byte[] x = new byte[2];
        byte[] y = new byte[2];
        byte[] z = new byte[2];
        data.read(x, 0, 2);
        data.read(y, 0, 2);
        data.read(z, 0, 2);
        double xx = (double)ByteBuffer.wrap(x).getShort() / (double)4096;
        double yy = (double)ByteBuffer.wrap(y).getShort() / (double)4096;
        double zz = (double)ByteBuffer.wrap(z).getShort() / (double)4096;
        Entity e = client.entities.get(id);
        if (e==null) {return;} // If entity does not exist in HashMap, do not update
        e.x += xx;
        e.y += yy;
        e.z += zz;
    }

    public static void windowOpen(Client client, ByteArrayInputStream data) throws IOException { // 0x2E
        int wId = Utilities.readVarInt(data);
        int wType = Utilities.readVarInt(data);
        if (client.inventories.containsKey(wId)==false) {
            client.inventories.put(wId, new Inventory(wType));
        }
    }

    public static void playerPosLook(Client client, ByteArrayInputStream data) throws IOException { // 0x38
        //Get the data
        byte[] x = new byte[8];
        byte[] y = new byte[8];
        byte[] z = new byte[8];
        byte flags;
        data.read(x, 0, 8);
        data.read(y, 0, 8);
        data.read(z, 0, 8);
        data.skip(4); // Skip yaw
        data.skip(4); // Skip pitch
        flags = (byte)data.read();
        int tID = Utilities.readVarInt(data);
        //Convert from byte[] to double and/or float
        double xx = ByteBuffer.wrap(x).getDouble();
        double yy = ByteBuffer.wrap(y).getDouble();
        double zz = ByteBuffer.wrap(z).getDouble();
        //Reposition player
        if ((flags & 0x01) != 0) {client.playerX += xx;} else {client.playerX = xx;}
        if ((flags & 0x02) != 0) {client.playerY += yy;} else {client.playerY = yy;}
        if ((flags & 0x04) != 0) {client.playerZ += zz;} else {client.playerZ = zz;}
        //Send response
        Serverbound.teleportConfirm(client, tID);
        //System.out.printf("<%s> X: %.1f Y: %.1f Z: %.1f\n", client.username, xx, yy, zz);
    }

    public static void destroyEntity(Client client, ByteArrayInputStream data) throws IOException { // 0x3A
        int count = Utilities.readVarInt(data); // Gets number of entities in array to destroy
        for (int i=0;i<count;i++) {
            int id = Utilities.readVarInt(data); // Id of entity to destroy (remove from hashmap)
            if (client.entities.get(id)!=null) { // Check if entity actually exists in hashmap
                client.entities.remove(id); // Remove from hashmap
            }
        }
    }

    public static void multiBlockChange(Client client, ByteArrayInputStream data) throws IOException { // 0x3F TODO
        byte[] locationBytes = new byte[8];
        data.read(locationBytes, 0, 8);
        long location = ByteBuffer.wrap(locationBytes).getLong();
        int chunkX = (int)(location >> 42);
        int chunkY = (int)(location << 44 >> 44);
        int chunkZ = (int)(location << 22 >> 42);
        data.skip(1); // Skip boolean that inverts light update packets think, idk, not useful
        int blockArraySize = Utilities.readVarInt(data);
        byte[] blockData;
        long blockLong;
        int blockId, localX, localY, localZ;
        for (int i=0;i<blockArraySize;i++) {
            blockLong = Utilities.readVarLong(data);
            blockId = (int)(blockLong >> 12);
            localX = (int)(blockLong >> 8) & 0xF;
            localY = (int)(blockLong) & 0xF;
            localZ = (int)(blockLong >> 4) & 0xF;
            client.chunks.setBlock(blockId, (chunkX*16)+localX, (chunkY*16)+localY, (chunkZ*16)+localZ);
            //System.out.printf("mID: %d X:%d Y:%d Z:%d\n", blockId, localX+(chunkX*16), localY+(chunkY*16), localZ+(chunkZ*16));
        }
    }

    public static void updateHealth(Client client, ByteArrayInputStream data) throws IOException { // 0x52
        byte[] health = new byte[4];
        data.read(health, 0, 4);
        client.playerHealth = ByteBuffer.wrap(health).getFloat();
        System.out.printf("<%s> Health: %.1f\n", client.username, client.playerHealth);
        if (client.playerHealth<=0.0) {
            Serverbound.clientStatus(client);
        }
    }

    public static void entityTeleport(Client client, ByteArrayInputStream data) throws IOException { // 0x62
        byte[] x = new byte[8];
        byte[] y = new byte[8];
        byte[] z = new byte[8];
        int id = Utilities.readVarInt(data);
        data.read(x, 0, 8);
        data.read(y, 0, 8);
        data.read(z, 0, 8);
        double xx = ByteBuffer.wrap(x).getDouble();
        double yy = ByteBuffer.wrap(y).getDouble();
        double zz = ByteBuffer.wrap(z).getDouble();
        Entity e = client.entities.get(id);
        if (e==null) {return;} // If entity does not exist in HashMap, do not update
        e.x = xx;
        e.y = yy;
        e.z = zz;
    }
}
