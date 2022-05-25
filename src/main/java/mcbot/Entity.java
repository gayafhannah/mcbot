package mcbot;

public class Entity {
    public int id;
    public int type;
    public double x, y, z;

    public String typeString() {
        switch (type) {
            case 1:
                return "Armor Stand";
            case 2:
                return "Arrow";
            case 3:
                return "Axolotl";
            case 4:
                return "Bat";
            case 5:
                return "Bee";
            case 7:
                return "Boat";
            case 8:
                return "Cat";
            case 10:
                return "Chicken";
            case 12:
                return "Cow";
            case 13:
                return "Creeper";
            case 15:
                return "Donkey";
            case 21:
                return "Enderman";
            case 25:
                return "XP Orb";
            case 27:
                return "Falling Block";
            case 29:
                return "Fox";
            case 37:
                return "Horse";
            case 40:
                return "Iron Golem";
            case 41:
                return "Item";
            case 42:
                return "Item Frame";
            case 46:
                return "Llama";
            case 64:
                return "Pig";
            case 67:
                return "Pillager";
            case 69:
                return "Primed TNT";
            case 71:
                return "Rabbit";
            case 74:
                return "Sheep";
            case 78:
                return "Skeleton";
            case 80:
                return "Slime";
            case 85:
                return "Spider";
            case 86:
                return "Squid";
            case 94:
                return "Trader Llama";
            case 98:
                return "Villager";
            case 100:
                return "Wandering Trader";
            case 107:
                return "Zombie";
            case 111:
                return "Player";
            default:
                return String.valueOf(type);
        }
    }
}
