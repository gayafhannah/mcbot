package mcbot;

import java.io.*;
import java.util.*;

import mcbot.Client;
import mcbot.Inventory;
import mcbot.Entity;

public class Worker extends Thread {
    Client client;
    public Worker(Client _client) {
        client = _client;
    }

    public void run() {
        try {
            String[] job;
            while (client.mode==0) {Thread.sleep(1000);}
            Serverbound.chatMessage(client, "Worker thread started on "+client.username);
            while (client.alive) {
                job = client.workerJobs.poll();
                if (job!=null) {
                    Serverbound.chatMessage(client, "Doing Job: " + job[0]);
                    switch (job[0]) {
                        case "test":
                            testJob();
                            break;
                        case "gay":
                            gayJob();
                            break;
                        case "use":
                            useJob();
                            break;
                        default:
                            Serverbound.chatMessage(client, "Invalid Job: " + job[0]);
                    }
                    Serverbound.chatMessage(client, "Done Job");
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("aejkrgn "+e);
        }
        System.out.println("Stopped");
    }

    private void testJob() throws IOException, InterruptedException {
        float yaw,pitch;
        for (int i=0;i<360;i+=15) {
            yaw = (float)i;
            pitch = (float)0;
            Serverbound.playerRotation(client, yaw, pitch);
            Thread.sleep(50);
        }
        for (int i : client.inventories.keySet()) {
            Inventory j = client.inventories.get(i);
            for (int k : j.slots.keySet()) {
                Inventory.Slot s = j.slots.get(k);
                if (s.hasItem) {
                    System.out.printf("Window: %d Slot: %d Item ID: %d Item Count: %d\n", i, k, s.itemId, s.itemCount);
                }
            }
        }
        Serverbound.playerBlockPlacement(client, 8, -60, 537);
        for (int i=0;i<20;i++) {
            //System.out.printf("a:%.1f :%.1f\n",client.playerX, client.playerX + 0.10);
            Serverbound.playerPosition(client, client.playerX + 0.20, client.playerY, client.playerZ);
            Thread.sleep(50);
        }
    }

    private void gayJob() throws IOException, InterruptedException {
        for (Entity e : client.entities.values()) {
            if ((e.type==111)||(e.type==67)) {
                double y = client.playerY;
                Serverbound.playerPosition(client, client.playerX, y+0.1, client.playerZ);
                Thread.sleep(200);
                Serverbound.playerPosition(client, client.playerX, y+0.5, client.playerZ);
                Thread.sleep(200);
                Serverbound.interactEntity(client, e.id, 1, false);
                Serverbound.playerPosition(client, client.playerX, y, client.playerZ);
                Thread.sleep(200);
            }
        }
    }

    private void useJob() throws IOException, InterruptedException {
        Serverbound.useItem(client);
        Thread.sleep(2000);
        Serverbound.playerDigging(client, 5, 0, 0, 0); // Release Bow/Finish eating
    }
}
