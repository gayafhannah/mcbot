package mcbot;

import mcbot.Serverbound;
import mcbot.Entity;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.zip.*;

public class Client extends Thread{
    public DataOutputStream dOut;
    public DataInputStream dIn;
    public String addr;
    public int port;
    public String username;
    public int protocol = 758;
    public HashMap<Integer, Entity> entities = new HashMap<Integer, Entity>();

    public int compression=-1; // -1 Means no compression, any other value is compression threshold
    public int mode = 0; // 0 Means Handshake, 1 Means Normal
    public boolean alive = true;

    public double playerX, playerY, playerZ;
    public float playerHealth;

    public Client(String _addr, int _port, String _username) {
        addr = _addr;
        port = _port;
        username = _username;
    }

    public void run() {// throws IOException, DataFormatException {
        System.out.printf("Connecting to %s:%d as %s\n", addr, port, username);
        try {
            //Create initial socket connection + datasttreams
            Socket sock = new Socket(addr,port);
            dOut = new DataOutputStream(sock.getOutputStream());
            dIn = new DataInputStream(sock.getInputStream());
            //Start handshake
            System.out.printf("<%s> Starting handshake\n", username);
            Serverbound.handshake(this);
            Serverbound.login_start(this);
            Listen();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    public void SendPacket(byte[] data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int length=data.length;
        if ((compression!=-1)&&(length>=compression)) { // Form compressed data packet if compression enabled and data length above threshold
            //Create temp output stream + compression stuff
            ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();
            byte[] compressed = new byte[0];
            Deflater compresser = new Deflater();
            //Put data into compresser/deflater
            compresser.setInput(data);
            compresser.finish();
            int compressed_length = compresser.deflate(compressed);    //Append length of comrpessed data to output stream
            Utilities.writeVarInt(compressed_length,tempOutputStream); //
            tempOutputStream.write(compressed); //Add compressed data to output stream
            length = tempOutputStream.size();
            data = tempOutputStream.toByteArray();
        } else { // Form regular data packet without compression
            if (compression != -1) {length+=1;}
            Utilities.writeVarInt(length,outputStream);
            if (compression != -1) {Utilities.writeVarInt(0,outputStream);}
            outputStream.write(data);
        }
        dOut.write(outputStream.toByteArray()); //Send the assembled data packet
    }

    public byte[] RecievePacket() throws IOException, DataFormatException {
        //Get length of packet and read n bytes into data array
        int length = getPacketLength();
        byte[] data = dIn.readNBytes(length);

        if (compression != -1) { // How to read if compression is enabled
            ByteArrayInputStream tempData = new ByteArrayInputStream(data.clone());
            int dataLength = Utilities.readVarInt(tempData);
            if (dataLength != 0) { // If packet is actually compressed
                int compressedLength = tempData.available();
                data = new byte[dataLength];
                Inflater decompresser = new Inflater();
                decompresser.setInput(tempData.readAllBytes(), 0, compressedLength);
                decompresser.inflate(data);
                decompresser.end();
            } else {
                data = tempData.readAllBytes();
            }
        }
        return data;
    }

    private int getPacketLength() throws IOException {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = dIn.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("Packet Length VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);
        return result;
    }

    public void log(String string) {
        System.out.printf("<%s> %s\n", username, string);
    }

    private void Listen() throws IOException, DataFormatException {
        ByteArrayInputStream data;
        int id;
        while (alive) {
            data = new ByteArrayInputStream(RecievePacket());
            id = Utilities.readVarInt(data);
            //System.out.println("-----");
            //System.out.printf("Packet ID: %2x\n", id);

            if (mode == 0) { // If still in Handshake Mode
                switch (id) {
                    case 0x00: // Login Fail
                        Clientbound.loginFail(this, data);
                        break;
                    case 0x02: // Login Success
                        Clientbound.loginSuccess(this);
                        //Serverbound.chatMessage(this, "Hello world!");
                        break;
                    case 0x03: // Set Compression
                        Clientbound.setCompression(this, data);
                        break;
                    default:
                        System.out.printf("<%s> Unknown packet during Handshake\n", username);
                }
            } else { // If upgraded to Normal mode
                switch (id) {
                    case 0x00: // Spawn Non-Living Entity
                        Clientbound.spawnEntity(this, entities, data);
                        break;
                    case 0x01: // Spawn XP Orb
                        break;
                    case 0x02: // Spawn Living Entity
                        Clientbound.spawnEntity(this, entities, data);
                        break;
                    case 0x03: // Spawn Painting
                        break;
                    case 0x04: // Spawn Another Player
                        Clientbound.spawnPlayer(this, entities, data);
                        break;
                    case 0x05: // Skulk Vibration
                        break;
                    case 0x06: // Entity Animation
                        break;
                    case 0x0A: // Set Block entity data
                        break;
                    case 0x0B: // Block action
                        break;
                    case 0x0C: // Query entity NBT
                        break;
                    case 0x0D: // Boss bar
                        break;
                    case 0x0E: // Difficulty
                        break;
                    case 0x0F: // Chat Message TODO
                        Clientbound.chatMessage(this, data);
                        break;
                    case 0x12: // Declare commands
                        break;
                    case 0x13: // Close Inventory Window TODO
                        break;
                    case 0x14: // Update inventory/chest contents TODO
                        break;
                    case 0x16: // Set inventory slot contents TODO
                        break;
                    case 0x18: // Plugin Message
                        break;
                    case 0x19: // Named Sound Effect
                        break;
                    case 0x1A: // Disconnect
                        System.out.printf("<%s> Server requested disconnect\n", username);
                        alive = false;
                        break;
                    case 0x1B: // Entity action
                        break;
                    case 0x1C: // Explosion
                        break;
                    case 0x1D: // Unload chunk
                        break;
                    case 0x1E: // Change game state
                        break;
                    case 0x20: // Initialize World Border
                        break;
                    case 0x21: // Keepalive
                        Clientbound.keepalive(this, data);
                        break;
                    case 0x22: // Chunk and Lighting Data
                        break;
                    case 0x23: // Particle/Sound Effect
                        break;
                    case 0x24: // Particle Effect
                        break;
                    case 0x25: // Update Light Level
                        break;
                    case 0x26: // Joined Game TODO Maybe send client status
                        Serverbound.chatMessage(this, "Hello cunts!");
                        Serverbound.chatMessage(this, "I am "+username);
                        break;
                    case 0x27: // Map Data
                        break;
                    case 0x29: // Update Entity Position
                        Clientbound.entityPos(this, entities, data);
                        break;
                    case 0x2A: // Update Entity Position and Rotation
                        Clientbound.entityPos(this, entities, data);
                        break;
                    case 0x2B: // Update Entity Rotation
                        break;
                    case 0x2C: // Vehicle Move
                        break;
                    case 0x32: // Player Abilities
                        break;
                    case 0x33: // End combat event
                        break;
                    case 0x34: // Enter combat event
                        break;
                    case 0x35: // Death event TODO
                        break;
                    case 0x36: // List of players TODO
                        break;
                    case 0x37: // Player Look
                        break;
                    case 0x38: // Player Position and Look
                        Clientbound.playerPosLook(this, data); //Must reply with Teleport Confirm
                        break;
                    case 0x39: // Unlock recipies
                        break;
                    case 0x3A: // Destroy Entity
                        Clientbound.destroyEntity(this, entities, data);
                        break;
                    case 0x3B: // Remove Entity Effect
                        break;
                    case 0x3C: // Resource pack Send
                        break;
                    case 0x3D: // Respawn event
                        break;
                    case 0x3E: // Update Entity Head Look
                        break;
                    case 0x3F: // Multiple blocks change in one tick
                        break;
                    case 0x48: // Currently selected hotbar slot TODO
                        break;
                    case 0x49: // Update View Position (Chunk XY)
                        break;
                    case 0x4A: // Set Viewdistance
                        break;
                    case 0x4B: // Update World Spawnpoint
                        break;
                    case 0x4C: // Display scoreboard
                        break;
                    case 0x4D: // Update Entity Metadata
                        break;
                    case 0x4E: // Attach Entity (leash)
                        break;
                    case 0x4F: // Entity Velocity
                        break;
                    case 0x50: // Entity Equiptment
                        break;
                    case 0x51: // Update XP Level
                        break;
                    case 0x52: // Update player health
                        Clientbound.updateHealth(this, data);
                        break;
                    case 0x54: // Set Passengers
                        break;
                    case 0x57: // Set Simulationdistance
                        break;
                    case 0x59: // Update Time
                        break;
                    case 0x5C: // Entity Sound effect
                        break;
                    case 0x5D: // Sound effect
                        break;
                    case 0x5E: // Stop Sound
                        break;
                    case 0x61: // Item Picked Up (By anyone)
                        break;
                    case 0x62: // Entity Teleport TODO
                        break;
                    case 0x63: // Player Advancements
                        break;
                    case 0x64: // Update Entity Properties
                        break;
                    case 0x65: // Entity Effect
                        break;
                    case 0x66: // Declare crafting recipies
                        break;
                    case 0x67: // Declare tags
                        break;
                    default:
                        System.out.printf("<%s> Unknown packet ID: %x", username, id);
                        alive = false;
                        break;
                }
            }
        }
    }
}
