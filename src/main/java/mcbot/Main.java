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

        String[] usernames = {"Cummies"};//, "UwU", "Awoo", "Blaaa", "stbhjgr", "b1", "b2", "b3", "b4"};

        ArrayList<Client> clients = new ArrayList<Client>();
        for (String u : usernames) {clients.add(new Client(addr, port, u));}
        for (Client c : clients) {
            System.out.printf("Starting %s\n",c.username);
            c.start();
            Thread.sleep(4500);
        }

        System.out.println("Clients Started. Starting monitor loop in 3s");
        boolean running = true;
        while (running) {
            System.out.println("-----------------");
            for (Client c : clients) {
                System.out.printf("(%s) H:%.1f X:%.1f Y:%.1f Z:%.1f\n",c.username,c.playerHealth,c.playerX,c.playerY,c.playerZ);
            }
            running = false;
            Client tmp = null;
            for (Client c : clients) {if (c.isAlive()) {running = true;} else {tmp = c;}}//running |= c.isAlive();}
            clients.remove(tmp);
            Thread.sleep(10000);
        }
    }
}
