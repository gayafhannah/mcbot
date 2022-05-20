package mcbot;

import mcbot.Client;
import mcbot.Utilities;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;

public class Serverbound {
    // Handshaking Packets
    public static void handshake(Client client) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x00, outputStream);   //Packet ID 0x00
        Utilities.writeVarInt(client.protocol, outputStream);    //Protocol Number TODO Replace this
        Utilities.writeString(client.addr, outputStream); //Send Server IP
        outputStream.write(ByteBuffer.allocate(2).putShort(client.port).array());    //Send Server Port
        Utilities.writeVarInt(2, outputStream); //Mode to login (1 - Status, 2 - Login)
        //Utilities.writeVarInt(2, outputStream); //Mode to login (1 - Status, 2 - Login) //DO NOT ASK WHY I HAVE TO DO THIS TWICE!!!
        client.SendPacket(outputStream.toByteArray());
    }

    public static void login_start(Client client) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x00, outputStream); //Packet ID 0x00
        Utilities.writeString(client.username, outputStream); //Player username
        client.SendPacket(outputStream.toByteArray());
    }

    // Normal Packets
    public static void teleportConfirm(Client client, int tID) throws IOException { // 0x00
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x00, outputStream);
        Utilities.writeVarInt(tID, outputStream);
        client.SendPacket(outputStream.toByteArray());
    }

    public static void chatMessage(Client client, String message) throws IOException { // 0x03
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x03, outputStream); //Packet ID: 0x00
        Utilities.writeString(message, outputStream); //Chat message
        client.SendPacket(outputStream.toByteArray());
    }

    public static void clientStatus(Client client) throws IOException { // 0x04
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x04, outputStream);
        Utilities.writeVarInt(0, outputStream);
        client.SendPacket(outputStream.toByteArray());
    }

    public static void closeWindow(Client client, byte id) throws IOException { // 0x09
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x09, outputStream);
        outputStream.write(id);
        client.SendPacket(outputStream.toByteArray());
    }

    public static void interactEntity(Client client, int id, int type, boolean sneaking) throws IOException { // 0x0D
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x0D, outputStream);
        Utilities.writeVarInt(id, outputStream);
        Utilities.writeVarInt(type, outputStream); // 0 interact, 1 attack
        outputStream.write(sneaking?1:0);
        client.SendPacket(outputStream.toByteArray());
    }

    public static void keepalive(Client client, byte[] id) throws IOException { // 0x0F
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x0F, outputStream);
        outputStream.write(id);
        client.SendPacket(outputStream.toByteArray());
    }

    public static void playerPosition(Client client, double x, double y, double z) throws IOException { // 0x11
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x11, outputStream);
        client.playerX = x;
        client.playerY = y;
        client.playerZ = z;
        outputStream.write(Utilities.doubleToByteArray(x));
        outputStream.write(Utilities.doubleToByteArray(y));
        outputStream.write(Utilities.doubleToByteArray(z));
        outputStream.write(0x01); // On Ground
        client.SendPacket(outputStream.toByteArray());
    }

    public static void playerRotation(Client client, float yaw, float pitch) throws IOException { // 0x13
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x13, outputStream);
        outputStream.write(Utilities.floatToByteArray(yaw));
        outputStream.write(Utilities.floatToByteArray(pitch));
        outputStream.write(0x01); // On Ground
        client.SendPacket(outputStream.toByteArray());
    }

    public static void useItem(Client client, int blockX, int blockY, int blockZ) throws IOException { // 0x2E
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x2E, outputStream);
        Utilities.writeVarInt(0, outputStream); // Assume Main hand (0 main, 1 offhand)
        outputStream.writeBytes(Utilities.coordsToPosition(blockX, blockY, blockZ));
        Utilities.writeVarInt(1, outputStream); // Assume top of block
        outputStream.write(Utilities.floatToByteArray((float)0.0)); // Cursor position on block
        outputStream.write(Utilities.floatToByteArray((float)1.0)); // Cursor position on block
        outputStream.write(Utilities.floatToByteArray((float)0.0)); // Cursor position on block
        outputStream.write(0x00); // Player isnt inside block
        client.SendPacket(outputStream.toByteArray());
    }
}
