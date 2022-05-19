package mcbot;

import mcbot.Utilities;
import mcbot.Entity;
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

    public static void main(String[] args) throws IOException, DataFormatException, InterruptedException {
        System.out.println("Minecraft Bot thing lmao!");
        System.out.println("--------");

        ArrayList<Entity> entities = new ArrayList<Entity>();
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

        Client client1 = new Client(entities, addr, port, name);
        client1.start();
        Thread.sleep(5000);
        Client client2 = new Client(entities, addr, port, "anel-lol");
        client2.start();

        Thread.sleep(5000);
        for (Entity e : entities) {
            if (e.typeString().equals("Player")) {
            System.out.println("---------");
            System.out.println(e.id);
            System.out.println(e.typeString());
            System.out.println(e.x);
            System.out.println(e.y);
            System.out.println(e.z);}
        }

        System.out.println("Clients Started");
        client1.join();
        client2.join();
    }
}
