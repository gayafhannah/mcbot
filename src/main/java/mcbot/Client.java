package mcbot;

import mcbot.Serverbound;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.zip.*;

public class Client {
    public DataOutputStream dOut;
    public DataInputStream dIn;
    public String addr;
    public int port;
    public String username;
    public int protocol = 758;

    public int compression=-1; // -1 Means no compression, any other value is compression threshold
    public int mode = 0; // 0 Means Handshake, 1 Means Normal
    public boolean alive = true;

    public double playerX, playerY, playerZ;

    public void Connect(String _addr, int _port, String _username) throws IOException, DataFormatException {
        addr = _addr;
        port = _port;
        username = _username;
        System.out.printf("Connecting to %s:%d as %s\n", addr, port, username);
        //Create initial socket connection + datasttreams
        Socket sock = new Socket(addr,port);
        dOut = new DataOutputStream(sock.getOutputStream());
        dIn = new DataInputStream(sock.getInputStream());
        //Start handshake
        System.out.println("Starting handshake");
        Serverbound.handshake(this);
        Serverbound.login_start(this);
        Listen();
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

    private void Listen() throws IOException, DataFormatException {
        ByteArrayInputStream data;
        int id;
        while (alive) {
            data = new ByteArrayInputStream(RecievePacket());
            id = Utilities.readVarInt(data);
            System.out.println("-----");
            System.out.printf("Packet ID: %x\n", id);

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
                        System.out.println("Unknown packet during Handshake");
                }
            } else { // If upgraded to Normal mode
                switch (id) {
                    case 0x00: // Spawn Entity
                        break;
                    case 0x0E: // Difficulty
                        break;
                    case 0x0F: // Chat Message
                        Clientbound.chatMessage(this, data);
                        break;
                    case 0x12: // Declare commands
                        break;
                    case 0x18: // Plugin Message
                        break;
                    case 0x1B: // Entity action
                        break;
                    case 0x26: // Joined Game
                        Serverbound.chatMessage(this, "Hello cunts!");
                        break;
                    case 0x32: // Player Abilities
                        break;
                    case 0x38: // Player Position and Look
                        Clientbound.playerPosLook(this, data); //Must reply with Teleport Confirm
                        break;
                    case 0x39: // Unlock recipies
                        break;
                    case 0x48: // Currently selected hotbar slot
                        break;
                    case 0x66: // Declare crafting recipies
                        break;
                    case 0x67: // Declare tags
                        break;
                    default:
                        System.out.println("Unknown packet");
                        alive = false;
                        break;
                }
            }
        }
    }
}
