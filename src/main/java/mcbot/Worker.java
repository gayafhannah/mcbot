package mcbot;

import java.io.*;
import java.util.*;

import mcbot.Client;

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

    private void testJob() throws IOException, InterruptedException{
        float yaw,pitch;
        for (int i=0;i<360;i+=15) {
            yaw = (float)i;
            pitch = (float)0;
            Serverbound.playerRotation(client, yaw, pitch);
            Thread.sleep(50);
        }
        for (int i=0;i<20;i++) {
            System.out.printf("a:%.1f :%.1f\n",client.playerX, client.playerX + 0.10);
            Serverbound.playerPosition(client, client.playerX + 0.20, client.playerY, client.playerZ);
            Thread.sleep(50);
        }
    }
}
