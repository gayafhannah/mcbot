package mcbot;

public class Inventory {
    public byte windowId;
    public int numOfSlots;
    public int type;
    public HashMap<Integer, Slot> slots = new HashMap<Integer, Slot>;

    public int getSlotsFromType() { // Returns Special slots in window (chest slots, crafting slots, furnace slots)
                                    // next 27 slots after returned value are main inventory, 9 after that are hotbar
        switch (type) {
            case -1: // Internal use for main inventory
                return 9;
            case 2: // Chest
                return 27;
            case 5: // Large chest
                return 53;
            case 11: // Crafting Table
                return 10;
            case 13: // Furnace
                return 3;
        }
    }
}

class Slot {
    public boolean hasItem;
    public int itemId;
    public byte itemCount;
}
