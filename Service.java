import java.util.*;
import java.text.SimpleDateFormat;


enum RoomType {
    STANDARD, JUNIOR, SUITE
}

class Room {
    int roomNumber;
    RoomType type;
    int pricePerNight;

    public Room(int roomNumber, RoomType type, int pricePerNight) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
    }

    @Override
    public String toString() {
        return "Room{" +
                "number=" + roomNumber +
                ", type=" + type +
                ", pricePerNight=" + pricePerNight +
                '}';
    }
}

class User {
    int id;
    int balance;

    public User(int id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", balance=" + balance +
                '}';
    }
}

class Booking {
    User user;
    Room room;
    Date checkIn;
    Date checkOut;
    int totalCost;

    public Booking(User user, Room room, Date checkIn, Date checkOut, int totalCost) {
        this.user = new User(user.id, user.balance); // snapshot of user state
        this.room = new Room(room.roomNumber, room.type, room.pricePerNight); // snapshot of room state
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalCost = totalCost;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "user=" + user +
                ", room=" + room +
                ", checkIn=" + checkIn +
                ", checkOut=" + checkOut +
                ", totalCost=" + totalCost +
                '}';
    }
}

public class Service {

    ArrayList<Room> rooms = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();
    ArrayList<Booking> bookings = new ArrayList<>();

    void setRoom(int roomNumber, RoomType roomType, int roomPricePerNight) {
        for (Room r : rooms) {
            if (r.roomNumber == roomNumber) {
                return; // Don't override, respect bookings
            }
        }
        rooms.add(new Room(roomNumber, roomType, roomPricePerNight));
    }

    void setUser(int userId, int balance) {
        for (User u : users) {
            if (u.id == userId) return;
        }
        users.add(new User(userId, balance));
    }

    void bookRoom(int userId, int roomNumber, Date checkIn, Date checkOut) {
        if (!checkIn.before(checkOut)) {
            System.out.println("Invalid booking dates.");
            return;
        }

        User user = null;
        for (User u : users) {
            if (u.id == userId) user = u;
        }
        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        Room room = null;
        for (Room r : rooms) {
            if (r.roomNumber == roomNumber) room = r;
        }
        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        // Check if room is available
        for (Booking b : bookings) {
            if (b.room.roomNumber == roomNumber &&
                    !(checkOut.compareTo(b.checkIn) <= 0 || checkIn.compareTo(b.checkOut) >= 0)) {
                System.out.println("Room not available.");
                return;
            }
        }

        long nights = (checkOut.getTime() - checkIn.getTime()) / (1000 * 60 * 60 * 24);
        int totalCost = (int) nights * room.pricePerNight;

        if (user.balance < totalCost) {
            System.out.println("Insufficient balance.");
            return;
        }

        user.balance -= totalCost;
        bookings.add(new Booking(user, room, checkIn, checkOut, totalCost));
        System.out.println("Booking successful.");
    }

    void printAll() {
        System.out.println("\nAll Rooms (Latest to Oldest):");
        ListIterator<Room> roomIter = rooms.listIterator(rooms.size());
        while (roomIter.hasPrevious()) {
            System.out.println(roomIter.previous());
        }

        System.out.println("\nAll Bookings (Latest to Oldest):");
        ListIterator<Booking> bookIter = bookings.listIterator(bookings.size());
        while (bookIter.hasPrevious()) {
            System.out.println(bookIter.previous());
        }
    }

    void printAllUsers() {
        System.out.println("\nAll Users (Latest to Oldest):");
        ListIterator<User> userIter = users.listIterator(users.size());
        while (userIter.hasPrevious()) {
            System.out.println(userIter.previous());
        }
    }

    // For testing
    public static void main(String[] args) throws Exception {
        Service service = new Service();

        service.setRoom(1, RoomType.STANDARD, 1000);
        service.setRoom(2, RoomType.JUNIOR, 2000);
        service.setRoom(3, RoomType.SUITE, 3000);

        service.setUser(1, 5000);
        service.setUser(2, 10000);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        service.bookRoom(1, 2, sdf.parse("30/06/2026"), sdf.parse("07/07/2026")); // should fail: not enough balance
        service.bookRoom(1, 2, sdf.parse("07/07/2026"), sdf.parse("30/06/2026")); // invalid dates
        service.bookRoom(1, 1, sdf.parse("07/07/2026"), sdf.parse("08/07/2026")); // 1 night: 1000
        service.bookRoom(2, 1, sdf.parse("07/07/2026"), sdf.parse("09/07/2026")); // overlaps → fail
        service.bookRoom(2, 3, sdf.parse("07/07/2026"), sdf.parse("08/07/2026")); // 1 night: 3000

        service.setRoom(1, RoomType.SUITE, 10000); // should not impact existing bookings

        service.printAll();
        service.printAllUsers();
    }
}
