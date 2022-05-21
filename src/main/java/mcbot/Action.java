package mcbot;

import mcbot.Client;

import java.io.*;
import java.util.*;

public class Action {
    public void eatFood(Client client) {
        Serverbound.useItem(client);
        Thread.sleep(2000);
        Serverbound.playerDigging(client, 5, 0, 0, 0); // Release Bow/Finish eating
    }

    public void shootBow(Client client, double x, double y, double z) {
        Serverbound.useItem(client);
        //Calculate angle and trejectory and Rotate player TODO
        Thread.sleep(1.15);
        Serverbound.playerDigging(client, 5, 0, 0, 0);
    }
}
