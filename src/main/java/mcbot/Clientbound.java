package mcbot;

import mcbot.Utilities;
import mcbot.Client;
import mcbot.Entity;

import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;

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
        System.out.print(Utilities.parseChat(Utilities.readString(data))); // TODO
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

    public static void playerPosLook(Client client, ByteArrayInputStream data) throws IOException { // 0x38
        //Get the data
        byte[] x = new byte[8];
        byte[] y = new byte[8];
        byte[] z = new byte[8];
        byte flags;
        data.read(x,0,8);
        data.read(y,0,8);
        data.read(z,0,8);
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
        System.out.printf("<%s> X: %.1f Y: %.1f Z: %.1f\n", client.username, xx, yy, zz);
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
}
