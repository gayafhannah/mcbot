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

    public static void spawnEntity(Client client, HashMap<Integer, Entity> entities, ByteArrayInputStream data) throws IOException { // 0x00 0x02
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

        if (entities.containsKey(id) == false) {
            Entity e = new Entity();
            e.id = id;
            e.type = type;
            e.x = xx;
            e.y = yy;
            e.z = zz;
            entities.put(id, e);
        }
    }

    public static void spawnPlayer(Client client, HashMap<Integer, Entity> entities, ByteArrayInputStream data) throws IOException { // 0x04
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

        if (entities.containsKey(id) == false) {
            Entity e = new Entity();
            e.id = id;
            e.type = 111; // Player type
            e.x = xx;
            e.y = yy;
            e.z = zz;
            entities.put(id, e);
        }
    }

    public static void chatMessage(Client client, ByteArrayInputStream data) throws IOException { // 0x0F
        String cMsg = Utilities.readString(data);
        if (Pattern.matches("^\\{\"translate\":\"chat\\.type\\.text\",\"with\":\\[\\{\"text\":\".*\"},\\{\"text\":\".*\"}]}",cMsg)) { // If normal chat message with nothing special at all
            String s = cMsg.substring(cMsg.indexOf("{\"text\":\"")+9, cMsg.indexOf("\"},{\"text"));
            String m = cMsg.substring(cMsg.lastIndexOf("{\"text\":\"")+9, cMsg.lastIndexOf("\"}]}"));
            String message = String.format("[%s] %s\n", s, m);
            if (s.equals("Shoe_Eater")) {
                String[] j;
                switch (m) {
                    case "test":
                        j = new String[] {"test",m};
                        client.workerJobs.add(j);
                        break;
                    case "gay":
                        j = new String[] {"gay",m};
                        client.workerJobs.add(j);
                        break;
                    case "use":
                        j = new String[] {"use",m};
                        client.workerJobs.add(j);
                        break;
                    case "dig":
                        j = new String[] {"dig",m};
                        client.workerJobs.add(j);
                        break;
                    default:
                        Serverbound.chatMessage(client, "Invalid command");
                }
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

    public static void keepalive(Client client, ByteArrayInputStream data) throws IOException { // 0x21
        byte[] id = new byte[8];
        data.read(id, 0, 8);
        //System.out.printf("<%s> Recieved Keepalive.\n", client.username);
        Serverbound.keepalive(client, id);
    }

    public static void entityPos(Client client, HashMap<Integer, Entity> entities, ByteArrayInputStream data) throws IOException { // 0x29 0x2A
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
        Entity e = entities.get(id);
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

    public static void destroyEntity(Client client, HashMap<Integer, Entity> entities, ByteArrayInputStream data) throws IOException { // 0x3A
        int count = Utilities.readVarInt(data); // Gets number of entities in array to destroy
        for (int i=0;i<count;i++) {
            int id = Utilities.readVarInt(data); // Id of entity to destroy (remove from hashmap)
            if (entities.get(id)!=null) { // Check if entity actually exists in hashmap
                entities.remove(id); // Remove from hashmap
            }
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

    public static void entityTeleport(Client client, HashMap<Integer, Entity> entities, ByteArrayInputStream data) throws IOException { // 0x62
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
        Entity e = entities.get(id);
        if (e==null) {return;} // If entity does not exist in HashMap, do not update
        e.x = xx;
        e.y = yy;
        e.z = zz;
    }
}
