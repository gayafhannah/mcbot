package mcbot;

import mcbot.Utilities;
//import java.io.OutputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.FileOutputStream;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class Main {
    //Default Server addr/port to use when no arguments used
    private static String defaultAddress = "nyaaa.daz.cat";
    private static int defaultPort = 25565;
    private static String defaultUsername = "cunt";

    public static void main(String[] args) throws IOException, DataFormatException {
        System.out.println("Minecraft Bot thing lmao!");
        System.out.println("--------");

        String addr = defaultAddress;
        int port = defaultPort;
        String name = defaultUsername;

        switch(args.length) {
            case 0:
                System.out.println("No arguments, Assuming defaults");
                break;
            case 1:
                System.out.println("One argument, Setting new IP");
                addr = args[0];
                break;
            case 2:
                System.out.println("Two arguments, Setting new IP+Username");
                addr = args[0];
                name = args[1];
                break;
            default:
                System.out.println("Too many arguments!");
        }

        System.out.println("--------");
        System.out.println("Addr: " + addr);
        System.out.println("Port: " + port);
        System.out.println("Name: " + name);
        System.out.println("--------");

        /* Test Utilities.writeVarLong() because its weird and i had to cast stuff lol
        OutputStream f = new FileOutputStream("test.bin");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        Utilities.writeVarInt(129,b);
        b.writeTo(f); */

        Client client1 = new Client();
        client1.Connect(addr,port,name);
    }
}
