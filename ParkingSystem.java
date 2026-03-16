import java.util.*;
import java.time.*;

// Queue Class
class MyQueue {
    int[] data;
    int front, rear, count, capacity;

    MyQueue(int capacity) {
        this.capacity = capacity;
        data = new int[capacity];
        front = 0;
        rear = -1;
        count = 0;
    }

    boolean isEmpty() {
        return count == 0;
    }

    boolean isFull() {
        return count == capacity;
    }

    void add(int value) {
        if (isFull()) return;
        rear = (rear + 1) % capacity;
        data[rear] = value;
        count++;
    }

    int poll() {
        if (isEmpty()) return -1;
        int val = data[front];
        front = (front + 1) % capacity;
        count--;
        return val;
    }

    int size() {
        return count;
    }

    void clear() {
        front = 0;
        rear = -1;
        count = 0;
    }
}

// Hash Table with Double Hashing
class MyHashTable<K, V> {

    private static class Entry<K, V> {
        K key;
        V value;
        boolean deleted;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.deleted = false;
        }
    }

    private final int SIZE = 200;
    private Entry<K, V>[] table;
    private int count;

    @SuppressWarnings("unchecked")
    MyHashTable() {
        table = new Entry[SIZE];
        count = 0;
    }

    private int hash1(K key) {
        return Math.abs(key.hashCode()) % SIZE;
    }

    private int hash2(K key) {
        return 199 - (Math.abs(key.hashCode()) % 199);
    }

    void put(K key, V value) {
        int h1 = hash1(key);
        int h2 = hash2(key);

        for (int i = 0; i < SIZE; i++) {
            int pos = (h1 + i * h2) % SIZE;
            Entry<K, V> e = table[pos];

            if (e == null || e.deleted || e.key.equals(key)) {
                table[pos] = new Entry<>(key, value);
                count++;
                return;
            }
        }
    }

    V get(K key) {
        int h1 = hash1(key);
        int h2 = hash2(key);

        for (int i = 0; i < SIZE; i++) {
            int pos = (h1 + i * h2) % SIZE;
            Entry<K, V> e = table[pos];

            if (e == null) return null;
            if (!e.deleted && e.key.equals(key)) return e.value;
        }
        return null;
    }

    boolean containsKey(K key) {
        return get(key) != null;
    }

    V remove(K key) {
        int h1 = hash1(key);
        int h2 = hash2(key);

        for (int i = 0; i < SIZE; i++) {
            int pos = (h1 + i * h2) % SIZE;
            Entry<K, V> e = table[pos];

            if (e == null) return null;

            if (!e.deleted && e.key.equals(key)) {
                e.deleted = true;
                count--;
                return e.value;
            }
        }
        return null;
    }

    boolean isEmpty() {
        return count == 0;
    }

    void clear() {
        Arrays.fill(table, null);
        count = 0;
    }

    int size() {
        return count;
    }

    Iterable<K> keySet() {
        List<K> keys = new ArrayList<>();
        for (Entry<K, V> e : table) {
            if (e != null && !e.deleted) keys.add(e.key);
        }
        return keys;
    }

    Iterable<V> values() {
        List<V> vals = new ArrayList<>();
        for (Entry<K, V> e : table) {
            if (e != null && !e.deleted) vals.add(e.value);
        }
        return vals;
    }
}

// Vehicle Class
class Vehicle {
    String vehicleNo;
    String ownerName;
    String type;
    int slotNo;
    LocalDateTime entryTime;
    LocalDateTime exitTime;
    double fee;

    Vehicle(String vehicleNo, String ownerName, String type, int slotNo) {
        this.vehicleNo = vehicleNo;
        this.ownerName = ownerName;
        this.type = type;
        this.slotNo = slotNo;
        this.entryTime = LocalDateTime.now();
        this.fee = 0;
    }

    void setExitTime() {
        this.exitTime = LocalDateTime.now();
    }

    long getDurationMinutes() {
        return Duration.between(entryTime, exitTime).toMinutes();
    }
}

// Parking System
public class ParkingSystem {

    static final int TWO_WHEELER_CAPACITY = 100;
    static final int FOUR_WHEELER_CAPACITY = 100;

    static MyQueue free2WSlots = new MyQueue(TWO_WHEELER_CAPACITY);
    static MyQueue free4WSlots = new MyQueue(FOUR_WHEELER_CAPACITY);

    static MyHashTable<String, Vehicle> parkedVehicles = new MyHashTable<>();
    static MyHashTable<String, Double> monthlyRevenue = new MyHashTable<>();

    static ArrayList<Vehicle> history = new ArrayList<>();

    static double totalRevenue = 0;
    static Scanner sc = new Scanner(System.in);

    static {
        for (int i = 1; i <= TWO_WHEELER_CAPACITY; i++)
            free2WSlots.add(i);

        for (int i = 1; i <= FOUR_WHEELER_CAPACITY; i++)
            free4WSlots.add(i);
    }

    static void parkVehicle() {

        System.out.print("Enter Vehicle Number: ");
        String no = sc.next();

        if (parkedVehicles.containsKey(no)) {
            System.out.println("Vehicle already parked!");
            return;
        }

        System.out.print("Enter Owner Name: ");
        String owner = sc.next();

        System.out.print("Enter Type (2W/4W): ");
        String type = sc.next().toUpperCase();

        if (type.equals("2W")) {

            if (free2WSlots.isEmpty()) {
                System.out.println("No 2W slots available!");
                return;
            }

            int slot = free2WSlots.poll();
            Vehicle v = new Vehicle(no, owner, type, slot);
            parkedVehicles.put(no, v);

            System.out.println("2W Vehicle parked at slot " + slot);

        } else if (type.equals("4W")) {

            if (free4WSlots.isEmpty()) {
                System.out.println("No 4W slots available!");
                return;
            }

            int slot = free4WSlots.poll();
            Vehicle v = new Vehicle(no, owner, type, slot);
            parkedVehicles.put(no, v);

            System.out.println("4W Vehicle parked at slot " + slot);

        } else {
            System.out.println("Invalid vehicle type!");
        }
    }

    static void removeVehicle() {

        System.out.print("Enter Vehicle Number to remove: ");
        String no = sc.next();

        if (!parkedVehicles.containsKey(no)) {
            System.out.println("Vehicle not found!");
            return;
        }

        Vehicle v = parkedVehicles.remove(no);
        v.setExitTime();

        long mins = v.getDurationMinutes();
        if (mins == 0) mins = 1;

        if (v.type.equals("2W")) {
            v.fee = mins * 1;
            free2WSlots.add(v.slotNo);
        } else {
            v.fee = mins * 2;
            free4WSlots.add(v.slotNo);
        }

        totalRevenue += v.fee;

        String month = YearMonth.now().toString();
        Double old = monthlyRevenue.get(month);

        if (old == null) old = 0.0;

        monthlyRevenue.put(month, old + v.fee);

        history.add(v);

        System.out.println("Vehicle removed. Fee: ₹" + v.fee);
    }

    static void searchVehicle() {

        System.out.print("Enter Vehicle Number: ");
        String no = sc.next();

        if (parkedVehicles.containsKey(no)) {
            Vehicle v = parkedVehicles.get(no);
            System.out.println("Found → Owner: " + v.ownerName + ", Type: " + v.type + ", Slot: " + v.slotNo);
        } else {
            System.out.println("Vehicle not found!");
        }
    }

    static void showAllVehicles() {

        if (parkedVehicles.isEmpty()) {
            System.out.println("No vehicles parked!");
            return;
        }

        System.out.println("Vehicle No | Owner | Type | Slot");

        for (Vehicle v : parkedVehicles.values()) {
            System.out.println(v.vehicleNo + " | " + v.ownerName + " | " + v.type + " | " + v.slotNo);
        }
    }

    static void showAvailableSlots() {

        System.out.println("Available 2W Slots: " + free2WSlots.size());
        System.out.println("Available 4W Slots: " + free4WSlots.size());
    }

    static void showTotalRevenue() {
        System.out.println("Total Revenue: ₹" + totalRevenue);
    }

    static void showMonthlyRevenue() {

        System.out.println("---- Monthly Revenue ----");

        for (String m : monthlyRevenue.keySet()) {
            System.out.println(m + " → ₹" + monthlyRevenue.get(m));
        }
    }

    static void showHistory() {

        if (history.isEmpty()) {
            System.out.println("No history!");
            return;
        }

        System.out.println("Vehicle No | Type | Fee | Duration(min)");

        for (Vehicle v : history) {
            System.out.println(v.vehicleNo + " | " + v.type + " | ₹" + v.fee + " | " + v.getDurationMinutes());
        }
    }

    static void findLongestParked() {

        if (parkedVehicles.isEmpty()) {
            System.out.println("No vehicles parked!");
            return;
        }

        Vehicle oldest = null;

        for (Vehicle v : parkedVehicles.values()) {
            if (oldest == null || v.entryTime.isBefore(oldest.entryTime)) {
                oldest = v;
            }
        }

        System.out.println("Longest parked: " + oldest.vehicleNo + " since " + oldest.entryTime);
    }

    static void showTotalCount() {
        System.out.println("Total Vehicles Parked: " + parkedVehicles.size());
    }

    static void showSlotUsage() {

        System.out.println("2W Used: " + (TWO_WHEELER_CAPACITY - free2WSlots.size()));
        System.out.println("4W Used: " + (FOUR_WHEELER_CAPACITY - free4WSlots.size()));
    }

    static void resetSystem() {

        parkedVehicles.clear();
        history.clear();
        free2WSlots.clear();
        free4WSlots.clear();
        totalRevenue = 0;
        monthlyRevenue.clear();

        for (int i = 1; i <= TWO_WHEELER_CAPACITY; i++)
            free2WSlots.add(i);

        for (int i = 1; i <= FOUR_WHEELER_CAPACITY; i++)
            free4WSlots.add(i);

        System.out.println("System reset complete!");
    }

    static void showDuration() {

        System.out.print("Enter Vehicle Number: ");
        String no = sc.next();

        if (!parkedVehicles.containsKey(no)) {
            System.out.println("Vehicle not found!");
            return;
        }

        Vehicle v = parkedVehicles.get(no);

        long mins = Duration.between(v.entryTime, LocalDateTime.now()).toMinutes();

        System.out.println(v.vehicleNo + " parked for " + mins + " minutes.");
    }

    static void showVehiclesByType() {

        System.out.print("Enter type (2W/4W): ");
        String t = sc.next().toUpperCase();

        boolean found = false;

        for (Vehicle v : parkedVehicles.values()) {

            if (v.type.equals(t)) {
                System.out.println(v.vehicleNo + " | Owner: " + v.ownerName + " | Slot: " + v.slotNo);
                found = true;
            }
        }

        if (!found)
            System.out.println("No vehicles of type " + t + "!");
    }

    static void exitSystem() {
        System.out.println("Exiting... Thank you!");
    }

    static void menu() {

        System.out.println("\n===== PARKING MANAGEMENT SYSTEM =====");

        System.out.println("1. Park Vehicle");
        System.out.println("2. Remove Vehicle");
        System.out.println("3. Search Vehicle");
        System.out.println("4. Show All Vehicles");
        System.out.println("5. Show Available Slots");
        System.out.println("6. Total Revenue");
        System.out.println("7. Monthly Revenue");
        System.out.println("8. Vehicle History");
        System.out.println("9. Longest Parked");
        System.out.println("10. Total Count");
        System.out.println("11. Slot Usage");
        System.out.println("12. Reset System");
        System.out.println("13. Show Duration");
        System.out.println("14. Vehicles by Type");
        System.out.println("15. Exit");

        System.out.print("Enter your choice: ");
    }

    public static void main(String[] args) {

        int choice;

        do {

            menu();
            choice = sc.nextInt();

            switch (choice) {

                case 1: parkVehicle(); break;
                case 2: removeVehicle(); break;
                case 3: searchVehicle(); break;
                case 4: showAllVehicles(); break;
                case 5: showAvailableSlots(); break;
                case 6: showTotalRevenue(); break;
                case 7: showMonthlyRevenue(); break;
                case 8: showHistory(); break;
                case 9: findLongestParked(); break;
                case 10: showTotalCount(); break;
                case 11: showSlotUsage(); break;
                case 12: resetSystem(); break;
                case 13: showDuration(); break;
                case 14: showVehiclesByType(); break;
                case 15: exitSystem(); break;
                default: System.out.println("Invalid choice!");
            }

        } while (choice != 15);
    }
}