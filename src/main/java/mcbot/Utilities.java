package mcbot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import mcbot.Inventory;


public class Utilities {
    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    public static int readVarInt(ByteArrayInputStream inputStream) throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;
        while (true) {
            currentByte = (byte)inputStream.read();
            value |= (currentByte & SEGMENT_BITS) << position;
            if ((currentByte & CONTINUE_BIT) == 0) break;
                position += 7;
            if (position >= 32) throw new RuntimeException("VarInt is too big");
    }
    return value;
    }

    public static long readVarLong(ByteArrayInputStream inputStream) throws IOException {
        long value = 0;
        int position = 0;
        byte currentByte;
        while (true) {
            currentByte = (byte)inputStream.read();
            value |= (currentByte & SEGMENT_BITS) << position;
            if ((currentByte & CONTINUE_BIT) == 0) break;
            position += 7;
            if (position >= 64) throw new RuntimeException("VarLong is too big");
        }
        return value;
    }

    public static void writeVarInt(int value, ByteArrayOutputStream outputStream) throws IOException {
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                outputStream.write(value);
                return;
            }
            outputStream.write((value & SEGMENT_BITS) | CONTINUE_BIT);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }
    }

    public static void writeVarLong(long value, ByteArrayOutputStream outputStream) throws IOException {
        while (true) {
            if ((value & ~((long) SEGMENT_BITS)) == 0) {
                outputStream.write((byte)value); //Testing casting to BYTE
                return;
            }
            outputStream.write(((byte)value & SEGMENT_BITS) | CONTINUE_BIT); //Test casting to BYTE
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }
    }

    public static void writeString(String s, ByteArrayOutputStream outputStream) throws IOException {
        int length = s.length();
        writeVarInt(length,outputStream);
        outputStream.write(s.getBytes("UTF-8"));
    }

    public static String readString(ByteArrayInputStream inputStream) throws IOException {
        int length = readVarInt(inputStream);
        byte[] bytes = new byte[length];
        inputStream.read(bytes, 0, length);
        String s = new String(bytes, "UTF-8");
        return s;
    }

    public static byte[] floatToByteArray(float value) {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putFloat(value);
        return bytes;
    }

    public static byte[] doubleToByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static Inventory.Slot readSlot(Inventory inventory, ByteArrayInputStream inputStream) throws IOException, InterruptedException {
        Inventory.Slot slot = inventory.newSlot();
        slot.hasItem = (inputStream.read()!=0);
        if (slot.hasItem) {
            slot.itemId = readVarInt(inputStream);
            slot.itemCount = (byte)inputStream.read();
            ignoreNBT(inputStream);
        }
        return slot;
    }

    public static byte[] coordsToPosition(long x, long y, long z) {
        long v = ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF);
        //System.out.printf("I X: %d Y: %d Z: %d\n",x,y,z);
        //System.out.printf("O X: %d Y: %d Z: %d\n",(v>>38)&0x3FFFFFF,v&0xFFF,(v>>12)&0x3FFFFFF);
/*       x = v>>38;
        y = v&0xFFF;
        z = (v>>12)&0x3fff;
        if (x>=(1<<25)) {x-=1<<26;}
        if (y>=(1<<11)) {y-=1<<12;}
        if (z>=(1<<25)) {z-=1<<26;}*/
        return ByteBuffer.allocate(8).putLong(v).array();
 //       return ByteBuffer.allocate(8).putLong(((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF)).array();
    }

    public static void ignoreNBT(ByteArrayInputStream inputStream) throws IOException, InterruptedException {
        byte tag = (byte)inputStream.read();
        //System.out.println("e"+tag);
        if (tag==(byte)0x0A) { //NBT Starts with TAG_Compound
            byte[] nameLengthBytes = new byte[2];
            inputStream.read(nameLengthBytes, 0, 2);
            short nameLength = ByteBuffer.wrap(nameLengthBytes).getShort();
            inputStream.skip(nameLength);
            ignoreNBTCompound(inputStream);
            return;
        } else {
            return; //NBT Starts with TAG_End
        }
    }
    private static void ignoreNBTCompound(ByteArrayInputStream inputStream) throws IOException, InterruptedException {
        byte[] nameLengthBytes = new byte[2];
        int nameLength;
        byte tag = 0x0A;
        do {
            //Thread.sleep(100);
            tag = (byte)inputStream.read();
            //System.out.println("c"+tag);
            if (tag==(byte)0) {return;}
            nameLengthBytes = new byte[2];
            inputStream.read(nameLengthBytes, 0, 2);
            nameLength = ByteBuffer.wrap(nameLengthBytes).getShort();
            inputStream.skip(nameLength);
            switch (tag) {
                case 0x01: // TAG_Byte
                    ignoreNBTByte(inputStream);
                    break;
                case 0x02: // TAG_Short
                    ignoreNBTShort(inputStream);
                    break;
                case 0x03: // TAG_Int
                    ignoreNBTInt(inputStream);
                    break;
                case 0x04: // TAG_Long
                    ignoreNBTLong(inputStream);
                    break;
                case 0x05: // TAG_Float
                    ignoreNBTFloat(inputStream);
                    break;
                case 0x06: // TAG_Double
                    ignoreNBTDouble(inputStream);
                    break;
                case 0x07: // TAG_Byte_Array
                    ignoreNBTByteArray(inputStream);
                    break;
                case 0x08: // TAG_String
                    ignoreNBTString(inputStream);
                    break;
                case 0x09: // TAG_List
                    ignoreNBTList(inputStream);
                    break;
                case 0x0A: // TAG_Compound
                    ignoreNBTCompound(inputStream);
                    break;
                case 0x0B: // TAG_Int_Array
                    ignoreNBTIntArray(inputStream);
                    break;
                case 0x0C: // TAG_Long_Array
                    ignoreNBTLongArray(inputStream);
                    break;
                default:
                    break;
            }
        } while (tag!=(byte)0);
    }
    public static void ignoreNBTByte(ByteArrayInputStream inputStream) throws IOException {inputStream.skip(1);}
    public static void ignoreNBTShort(ByteArrayInputStream inputStream) throws IOException {inputStream.skip(2);}
    public static void ignoreNBTInt(ByteArrayInputStream inputStream) throws IOException {inputStream.skip(4);}
    public static void ignoreNBTLong(ByteArrayInputStream inputStream) throws IOException {inputStream.skip(8);}
    public static void ignoreNBTFloat(ByteArrayInputStream inputStream) throws IOException {inputStream.skip(4);}
    public static void ignoreNBTDouble(ByteArrayInputStream inputStream) throws IOException {inputStream.skip(8);}
    public static void ignoreNBTByteArray(ByteArrayInputStream inputStream) throws IOException {
        byte[] nameLengthBytes = new byte[4];
        inputStream.read(nameLengthBytes, 0, 4);
        int nameLength = ByteBuffer.wrap(nameLengthBytes).getInt();
        inputStream.skip(nameLength);
    }
    public static void ignoreNBTString(ByteArrayInputStream inputStream) throws IOException {
        byte[] nameLengthBytes = new byte[2];
        inputStream.read(nameLengthBytes, 0, 2);
        short nameLength = ByteBuffer.wrap(nameLengthBytes).getShort();
        inputStream.skip(nameLength);
        //System.out.println("s"+nameLength);
    }
    public static void ignoreNBTList(ByteArrayInputStream inputStream) throws IOException, InterruptedException {
        byte tag = (byte)inputStream.read(); // was .skip(1)
        byte[] nameLengthBytes = new byte[4];
        inputStream.read(nameLengthBytes, 0, 4);
        int nameLength = ByteBuffer.wrap(nameLengthBytes).getInt();
        //System.out.println(tag+"a"+nameLength);
        for (int i=0;i<nameLength;i++) {
            //System.out.println("l"+tag);
            switch (tag) {
                case 0x01: // TAG_Byte
                    ignoreNBTByte(inputStream);
                    break;
                case 0x02: // TAG_Short
                    ignoreNBTShort(inputStream);
                    break;
                case 0x03: // TAG_Int
                    ignoreNBTInt(inputStream);
                    break;
                case 0x04: // TAG_Long
                    ignoreNBTLong(inputStream);
                    break;
                case 0x05: // TAG_Float
                    ignoreNBTFloat(inputStream);
                    break;
                case 0x06: // TAG_Double
                    ignoreNBTDouble(inputStream);
                    break;
                case 0x07: // TAG_Byte_Array
                    ignoreNBTByteArray(inputStream);
                    break;
                case 0x08: // TAG_String
                    ignoreNBTString(inputStream);
                    break;
                case 0x09: // TAG_List
                    ignoreNBTList(inputStream);
                    break;
                case 0x0A: // TAG_Compound
                    ignoreNBTCompound(inputStream);
                    break;
                case 0x0B: // TAG_Int_Array
                    ignoreNBTIntArray(inputStream);
                    break;
                case 0x0C: // TAG_Long_Array
                    ignoreNBTLongArray(inputStream);
                    break;
                default:
                    break;
            }
        }
    }
    public static void ignoreNBTIntArray(ByteArrayInputStream inputStream) throws IOException {
        byte[] nameLengthBytes = new byte[4];
        inputStream.read(nameLengthBytes, 0, 4);
        int nameLength = ByteBuffer.wrap(nameLengthBytes).getInt();
        inputStream.skip(nameLength*4);
    }
    public static void ignoreNBTLongArray(ByteArrayInputStream inputStream) throws IOException {
        byte[] nameLengthBytes = new byte[4];
        inputStream.read(nameLengthBytes, 0, 4);
        int nameLength = ByteBuffer.wrap(nameLengthBytes).getInt();
        inputStream.skip(nameLength*8);
    }
}
