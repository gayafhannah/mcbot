package mcbot;

import java.io.*;
import java.util.*;

import mcbot.Client;

public class Pathfinder {
    public Client client;
    public HashMap<Long, Node> nodes = new HashMap<Long, Node>();
    private int sX, sY, sZ; // Starting XYZ
    private int gX, gY, gZ; // Goal XYZ
    private Node cN;

    public Pathfinder(Client _client) {
        client = _client;
    }

    public void pathTo(int _gX, int _gY, int _gZ) {
        gX = _gX;
        gY = _gY;
        gZ = _gZ;
        sX = (int)Math.floor(client.playerX);
        sY = (int)Math.floor(client.playerY);
        sZ = (int)Math.floor(client.playerZ);
        cN = new Node(null, sX, sY, sZ, 1, heuristic(sX, sY, sZ));
        nodes.put(xyzToLocation(sX, sY, sZ), cN);
        boolean stop = false;
        while (!stop) {
            System.out.printf("Currently at Node: %d %d %d\n", cN.x, cN.y, cN.z);
            if ((cN.x==gX) && (cN.y==gY) && (cN.z==gZ)) {break;}
            calculateNeighbours();
            chooseNextNode();
        }
        System.out.println("DONE");
    }

    private void chooseNextNode() {
        Node bestNode = cN;
        System.out.printf("pls %f\n",cN.f);
        if (nodes.get(xyzToLocation(cN.x+1, cN.y, cN.z)).cost<=bestNode.f) {bestNode = nodes.get(xyzToLocation(cN.x+1, cN.y, cN.z));}
        if (nodes.get(xyzToLocation(cN.x-1, cN.y, cN.z)).cost<=bestNode.f) {bestNode = nodes.get(xyzToLocation(cN.x-1, cN.y, cN.z));}
        if (nodes.get(xyzToLocation(cN.x, cN.y, cN.z+1)).cost<=bestNode.f) {bestNode = nodes.get(xyzToLocation(cN.x, cN.y, cN.z+1));}
        if (nodes.get(xyzToLocation(cN.x, cN.y, cN.z-1)).cost<=bestNode.f) {bestNode = nodes.get(xyzToLocation(cN.x, cN.y, cN.z-1));}
        if (nodes.get(xyzToLocation(cN.x+1, cN.y, cN.z+1)).cost<=bestNode.f) {bestNode = nodes.get(xyzToLocation(cN.x+1, cN.y, cN.z+1));}
        if (nodes.get(xyzToLocation(cN.x+1, cN.y, cN.z-1)).cost<=bestNode.f) {bestNode = nodes.get(xyzToLocation(cN.x+1, cN.y, cN.z-1));}
        if (nodes.get(xyzToLocation(cN.x-1, cN.y, cN.z+1)).cost<=bestNode.f) {bestNode = nodes.get(xyzToLocation(cN.x-1, cN.y, cN.z+1));}
        if (nodes.get(xyzToLocation(cN.x-1, cN.y, cN.z-1)).cost<=bestNode.f) {bestNode = nodes.get(xyzToLocation(cN.x-1, cN.y, cN.z-1));}
        if (bestNode == cN) {
            cN.f+=1;
            cN = cN.parent;
        } else {cN = bestNode;}
    }

    private void calculateNeighbours() {
        calculateNodeAxis(cN.x+1, cN.y, cN.z);
        calculateNodeAxis(cN.x-1, cN.y, cN.z);
        calculateNodeAxis(cN.x, cN.y, cN.z+1);
        calculateNodeAxis(cN.x, cN.y, cN.z-1);
        calculateNodeDiag(cN.x+1, cN.y, cN.z+1);
        calculateNodeDiag(cN.x-1, cN.y, cN.z+1);
        calculateNodeDiag(cN.x+1, cN.y, cN.z-1);
        calculateNodeDiag(cN.x-1, cN.y, cN.z-1);
    }

    private void calculateNodeAxis(int nX, int nY, int nZ) {
        double cost = move(nX, nY, nZ);
        double h = heuristic(nX, nY, nZ);
        //System.out.println(h);
        if (nodes.get(xyzToLocation(nX,nY,nZ))!=null) {return;}
        nodes.put(xyzToLocation(nX, nY, nZ), new Node(cN, nX, nY, nZ, cost, h));
    }
    private void calculateNodeDiag(int nX, int nY, int nZ) {
        boolean notPossible = false;
        double cost = move(nX, nY, nZ);
        if (nX>cN.x) {notPossible |= !validPosition(cN.x+1,cN.y,cN.z);}
        if (nX<cN.x) {notPossible |= !validPosition(cN.x-1,cN.y,cN.z);}
        if (nZ>cN.z) {notPossible |= !validPosition(cN.x,cN.y,cN.z+1);}
        if (nZ<cN.z) {notPossible |= !validPosition(cN.x,cN.y,cN.z-1);}
        if (notPossible) {cost = Double.POSITIVE_INFINITY;}
        double h = heuristic(nX, nY, nZ);
        if (nodes.get(xyzToLocation(nX,nY,nZ))!=null) {return;}
        nodes.put(xyzToLocation(nX, nY, nZ), new Node(cN, nX, nY, nZ, cost, h));
    }

    private boolean validPosition(int x, int y, int z) { // Checks if position is valid for player to stand on
        if (client.chunks.getBlock(x,y+1,z)!=0) {return false;} // Head is air
        if (client.chunks.getBlock(x,y,z)!=0) {return false;} // Feet is air
        if (client.chunks.getBlock(x,y-1,z)==0) {return false;} // Block below is NOT air
        return true;
    }

    private double heuristic(int nX, int nY, int nZ) { // Distance between node and end
        return Math.sqrt(Math.abs((double)Math.pow(gX-nX,2) + (double)Math.pow(gY-nY,2) + (double)Math.pow(gZ-nZ,2)));
    }

    private double move(int nX, int nY, int nZ) { // Cost to get here
        if (!validPosition(nX, nY, nZ)) {return Double.POSITIVE_INFINITY;}
        double cost = cN.cost;
        if ((cN.x!=nX) && (cN.z!=nZ)) { // Moving diagonally
        cost += Math.sqrt(2);
        } else { // Not moving diagonally
        cost += 1;
        }
        return cost-0.1;
    }

    private long xyzToLocation(int x, int y, int z) {
        return ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    class Node {
        public Node parent;
        public int x,y,z;
        public double cost;
        public double h;
        public double f;
        public Node(Node _parent, int _x, int _y, int _z, double _cost, double _h) {
            System.out.printf("%d %d %d : %f %f : %f\n",_x,_y,_z,_cost,_h, _cost+_h);
            parent = _parent;
            x = _x;
            y = _y;
            z = _z;
            cost = _cost;
            h = _h;
            f = cost+h;
        }
    }
}
