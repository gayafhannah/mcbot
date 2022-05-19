package mcbot;

import mcbot.Client;
import mcbot.Utilities;
import java.io.*;

public class Serverbound {
    // Handshaking Packets
    public static void handshake(Client client) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Utilities.writeVarInt(0x00, outputStream);   //Packet ID 0x00
        Utilities.writeVarInt(client.protocol, outputStream);    //Protocol Number TODO Replace this
        Utilities.writeString(client.addr, outputStream); //Send Server IP
        outputStream.write(client.port);    //Send Server Port
        Utilities.writeVarInt(2, outputStream); //Mode to login (1 - Status, 2 - Login)
        Utilities.writeVarInt(2, outputStream); //Mode to login (1 - Status, 2 - Login) //DO NOT ASK WHY I HAVE TO DO THIS TWICE!!!
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
}
