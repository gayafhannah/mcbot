package mcbot;

import java.io.*;
import java.util.*;
import java.util.AbstractList.*;
import java.util.ArrayList.*;

import mcbot.Client;

public class Pathfinder {
    public Client client;
    public HashMap<Long, Node> nodes = new HashMap<Long, Node>();
    private ArrayList closed = new ArrayList();
    private ArrayList<Node> open = new ArrayList<Node>();
    private int sX, sY, sZ; // Starting XYZ
    private int gX, gY, gZ; // Goal XYZ
    private Node cN;
    public LinkedList<Node> path;
    private int maxDepth;
    private int maxSearchDistance = 100; // Max distance of 100

    public Pathfinder(Client _client) {
        client = _client;
    }

    public boolean getPath(int _gX, int _gY, int _gZ) {
        gX = _gX;
        gY = _gY;
        gZ = _gZ;
        sX = (int)Math.floor(client.playerX);
        sY = (int)Math.floor(client.playerY);
        sZ = (int)Math.floor(client.playerZ);

        nodes.clear();
        nodes.put(xyz(gX, gY, gZ), new Node(null, gX, gY, gZ, 0, 0, 0));
        cN = new Node(null, sX, sY, sZ, 0, heuristic(sX, sY, sZ), 0);
        closed.clear();
        open.clear();
        open.add(cN);

        maxDepth = 0;
        while ((open.size() != 0) && (maxDepth < maxSearchDistance)) {
            cN = open.get(0);
            //cN = open.removeFirst();
            if (cN == nodes.get(xyz(gX, gY, gZ))) {  // If reached target node
                System.out.println("Reached goal");
                path = buildPath(cN);
                return true;
            }
            /*if (cN.parent == null) {
                System.out.println("No path found");
                break;
            }*/
            open.remove(cN);
       //     open.removeFirst();
            closed.add(cN);
            //System.out.printf("At: %d %d %d : %d\n", cN.x, cN.y, cN.z, open.size());

            for (int x=-1;x<2;x++) {
                for (int y=-1;y<2;y++) {
                    for (int z=-1;z<2;z++) {
                        if ((x==0) && (z==0) && (y==0)) {continue;}
                        if (((x!=0) || (z!=0)) && (y!=0)) {continue;}
                        int nX = x+cN.x;
                        int nY = y+cN.y;
                        int nZ = z+cN.z;

                        if (cN.y==nY) {
                            if ((x!=0) && (z!=0)) { // Checks for diagonal movement
                                if (!canFit(nX, nY, cN.z)) {continue;}
                                if (!canFit(cN.x, nY, nZ)) {continue;}
                            }
                            if (!canFit(nX, nY, nZ)) {continue;} // Standard check
                            if (!hasWalkableFloor(cN.x, cN.y, cN.z)) { // To stop from walking on air
                                if (cN.y>cN.parent.y) {
                                    if (!hasWalkableFloor(cN.parent.x, cN.parent.y, cN.parent.z)) {continue;}
                                } else {continue;}
                            }
                        } else if (cN.y>nY) { // Go down
                        //System.out.printf("%d %d %d\n", nX, nY, nZ);
                            if (!canFit(nX, nY, nZ)) {continue;}
                            //if (hasWalkableFloor(nX, nY, nZ)) {continue;}
                        } else { // Go up
                            if (!hasWalkableFloor(cN.x, cN.y, cN.z)) {continue;}
                            if (!canFit(nX, nY, nZ)) {continue;}
                        }

                        if (true) {
                            double nSCost = cN.cost + getCost(nX, nY, nZ);
                            Node neighbour = nodes.get(xyz(nX, nY, nZ));
                            if (neighbour==null) { // Create node if not exists
                                neighbour = new Node(cN, nX, nY, nZ, Double.POSITIVE_INFINITY, heuristic(nX, nY, nZ), 0);
                                nodes.put(xyz(nX, nY, nZ), neighbour); // Fix depth value at end
                            }
                            //System.out.println(nodes.size());
                            //System.out.printf("Valid: %d %d %d %f / %x\n", nX, nY, nZ, nSCost,xyz(nX,nY,nZ));
                            if (nSCost < neighbour.cost) {
                                //System.out.println("Better cost found");
                                if (open.contains(neighbour)) {open.remove(neighbour);}
                                if (closed.contains(neighbour)) {closed.remove(neighbour);}
                            }
                            if (!open.contains(neighbour) && !closed.contains(neighbour)) {
                                neighbour.cost = nSCost;
                                neighbour.depth = cN.depth+1;
                                neighbour.h = heuristic(nX, nY, nZ);
                                neighbour.parent = cN;
                                maxDepth = Math.max(maxDepth, neighbour.depth);
                                open.add(neighbour);
                                //System.out.printf("Adding: %d %d %d , %d\n", nX, nY, nZ, open.size());
                                //System.out.println(neighbour);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Failed to find path.");
        path = null;
        return false;
    }

    private LinkedList buildPath(Node cN) {
        LinkedList path = new LinkedList();
        while (cN != null) {
            //System.out.printf("%d %d %d\n", cN.x, cN.y, cN.z);
            path.add(cN);
            cN = cN.parent;
        }
        return path;
    }

    public void doPath() throws IOException, InterruptedException {
        System.out.println("Path:");
        //for (Node n : path) {
        client.moveInterrupted = false;
        for (int i=path.size()-1;i!=0;i--) {
            if (client.moveInterrupted) {break;}
            Node n = path.get(i);
            System.out.printf("%d %d %d\n", n.x, n.y, n.z);
            Serverbound.playerPosition(client, n.x+0.5, n.y, n.z+0.5);
            Thread.sleep(200);
        }
    }

    private boolean validPosition(int x, int y, int z) { // Checks if position is valid for player to stand on
        if (client.chunks.getBlock(x,y+1,z)!=0) {return false;} // Head is air
        if (client.chunks.getBlock(x,y,z)!=0) {return false;} // Feet is air
        if (client.chunks.getBlock(x,y-1,z)==0) {return false;} // Block below is NOT air
        return true;
    }
    private boolean canFit(int x, int y, int z) { // Check if player can exist in these blocks
        if (client.chunks.getBlock(x,y+1,z)!=0) {return false;} // Head is air
        if (client.chunks.getBlock(x,y,z)!=0) {return false;} // Feet is air
        return true;
    }
    private boolean hasWalkableFloor(int x, int y, int z) {
        if (client.chunks.getBlock(x,y-1,z)==0) {return false;} // Block below is NOT air
        return true;
    }

    private double heuristic(int nX, int nY, int nZ) { // Distance between node and end
        return Math.sqrt(Math.abs((double)Math.pow(gX-nX,2) + (double)Math.pow(gY-nY,2) + (double)Math.pow(gZ-nZ,2)));
    }

    private double getCost(int nX, int nY, int nZ) { // Cost to get here
        double cost = Math.sqrt(Math.abs((double)Math.pow(gX-nX,2) + (double)Math.pow(gY-nY,2) + (double)Math.pow(gZ-nZ,2)));
        // Maybe add weird offsets and stuff if other conditions met
        return cost;
    }

    private long xyz(long x, long y, long z) {
        return ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    class Node {
        public Node parent;
        public int x,y,z;
        public double cost;
        public double h;
        public double f;
        public int depth;
        public Node(Node _parent, int _x, int _y, int _z, double _cost, double _h, int _depth) {
            //System.out.printf("%d %d %d : %f %f : %f\n",_x,_y,_z,_cost,_h, _cost+_h);
            parent = _parent;
            x = _x;
            y = _y;
            z = _z;
            cost = _cost;
            h = _h;
            f = cost+h;
            depth = _depth;
        }
    }
}
