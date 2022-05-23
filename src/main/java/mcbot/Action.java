package mcbot;

import mcbot.Client;

import java.io.*;
import java.util.*;

public class Action {
    public static void eatFood(Client client) throws IOException, InterruptedException {
        Serverbound.useItem(client);
        Thread.sleep(2000);
        Serverbound.playerDigging(client, 5, 0, 0, 0); // Release Bow/Finish eating
    }

    public static void shootBow(Client client, boolean useOffsets, double x, double y, double z) throws IOException, InterruptedException {
        final double centreOffset = 0.5; // Offset to aim for centre of block
        final double playerEyeHeightOffset = 1.62; // Offset for arrow coming out of player's eyes rather than true Y pos
        if (useOffsets) {
            x+=centreOffset;
            y+=centreOffset - playerEyeHeightOffset;
            z+=centreOffset;
        }
        double dx = x-client.playerX;
        double dy = y-client.playerY;
        double dz = z-client.playerZ;
        double h = Math.sqrt(dx*dx + dz*dz);
        Double pitchR = shootBowGetPitch(h, dy, 3, 0.01, 0.05); // 3 is velocity, 0.01 is drag, 0.05 is gravity
        if (pitchR==null) {Serverbound.chatMessage(client, "Cannot shoot target");return;}
        double pitch = Math.toDegrees(pitchR);
        float yaw = (float)(360 - Math.toDegrees(Math.atan2(x-client.playerX,z-client.playerZ))) % (float)360;
        Serverbound.useItem(client);
        Thread.sleep(50);
        Serverbound.playerRotation(client, yaw, (float)-pitch);
        Thread.sleep(1100);
        Serverbound.playerDigging(client, 5, 0, 0, 0);
    }
    private static Double shootBowGetPitch(double tx, double ty, double v, double d, double g) { // Used purely for shootBow
        // If it's near the asymptotes, just return a vertical angle
        if (tx < ty * 0.001) {
            return ty>0 ? Math.PI/2.0 : -Math.PI/2.0;
        }

        double md = 1.0-d;
        double log_md = Math.log(md);
        double g_d = g/d; // This is terminal velocity
        double theta = Math.atan2(ty, tx);
        double prev_abs_ydif = Double.POSITIVE_INFINITY;

        // 20 iterations max, although it usually converges in 3 iterations
        for (int i=0; i<20; i++) {
            //System.out.println(i);
            double cost = Math.cos(theta);
            double sint = Math.sin(theta);
            double tant = sint/cost;
            double vx = v * cost;
            double vy = v * sint;
            double y = tx*(g_d+vy)/vx - g_d*Math.log(1-d*tx/vx)/log_md;
            double ydif = y-ty;
            double abs_ydif = Math.abs(ydif);

            // If it's getting farther away, there's probably no solution
            if (abs_ydif>prev_abs_ydif) {
                return null;
            }
            else if (abs_ydif < 0.0001) {
                return theta;
            }

            double dy_dtheta = tx + g*tx*tant / ((-d*tx+v*cost)*log_md) + g*tx*tant/(d*v*cost) + tx*tant*tant;
            theta -= ydif/dy_dtheta;
            prev_abs_ydif = abs_ydif;
        }

        // If exceeded max iterations, return null
        return null;
    }

    public static void digBlock(Client client, int x, int y, int z) throws IOException, InterruptedException {
        Serverbound.playerDigging(client, 0, x, y, z); // TODO Add timeout testing
        Thread.sleep(1500);
        Serverbound.playerDigging(client, 2, x, y, z);
    }

    //public static boolean moveDelta(Client client, int dX, int dY, int dZ) {moveDelta(client, dX, dY, dZ, 0.2);}
    //public static boolean moveDelta(Client client, int dX, int dY, int dZ, float speed) throws IOException, InterruptedException { // Move, speed is per tick
    //    client.moveInterrupted = false;
    //    for (int i=0;i<)
    //}
}
