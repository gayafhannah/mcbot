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

    public static Inventory.Slot readSlot(Inventory inventory, ByteArrayInputStream inputStream) throws IOException {
        Inventory.Slot slot = inventory.newSlot();
        slot.hasItem = (inputStream.read()!=0);
        if (slot.hasItem) {
            slot.itemId = readVarInt(inputStream);
            slot.itemCount = (byte)inputStream.read();
            if (inputStream.read()!=0) {
            //Cry
            }
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
}
