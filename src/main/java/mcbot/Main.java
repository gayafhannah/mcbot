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
    //private static String defaultAddress = "nyaaa.daz.cat";
    private static short defaultPort = 25565;
    private static String[] usernames = {"Nya"};//,"Cummies"};//, "UwU", "Awoo", "Blaaa", "stbhjgr", "b1", "b2", "b3", "b4"};

    public static void main(String[] args) throws IOException, DataFormatException, InterruptedException {
        System.out.println("Minecraft Bot thing lmao!");
        System.out.println("--------");

        String addr;
        short port = defaultPort;

        switch(args.length) {
            case 0:
                System.out.println("Wrong number of arguments!");
                System.out.println("./main <ip> [usernames]");
                return;
            case 1:
                System.out.println("One argument, Using default usernames");
                addr = args[0];
                break;
            default:
                System.out.println("Multiple arguments, Using custom usernames.");
                addr = args[0];
                usernames = Arrays.copyOfRange(args, 1, args.length);
        }

        System.out.println("-Server-");
        System.out.printf("Addr: %s\n", addr);
        System.out.printf("Port: %s\n", port);
        System.out.println("-Usernames-");
        for (String u : usernames) {System.out.printf("%s\n", u);}
        System.out.println("");
        System.out.println("-Starting Clients-");


        ArrayList<Client> clients = new ArrayList<Client>();
        for (String u : usernames) {clients.add(new Client(addr, port, u));}
        for (Client c : clients) {
            //System.out.printf("Starting %s\n",c.username);
            c.start();
            Thread.sleep(4500);
        }

        System.out.println("Clients Started. Starting monitor loop.");
        System.out.println("");
        boolean running = true;
        while (running) {
            System.out.println("------------------------------------------");
            for (Client c : clients) {
                System.out.printf("(%s) H:%.1f X:%.1f Y:%.1f Z:%.1f\n",c.username,c.playerHealth,c.playerX,c.playerY,c.playerZ);
            }
            running = false;
            Client tmp = null;
            for (Client c : clients) {if (c.isAlive()) {running = true;} else {tmp = c;}}//running |= c.isAlive();}
            clients.remove(tmp);
            Thread.sleep(1000);
        }
    }
}
