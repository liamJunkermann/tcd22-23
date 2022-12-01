import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Controller extends Node {
    // Table indexes
    static final int ROUTER = 0;
    static final int ROUTER_OUT = 1;
    static final int DEST_ADDR = 2;

    String[][] preconfigInfo = {
            // Destination, Router, Out

            // Router 1
            { "R1", "E1", "lab" },
            { "R1", "R2", "home" },
            { "R1", "R3", "server" },
            { "R1", "R5", "trinity" },

            // Router 2
            { "R2", "R1", "lab" },
            { "R2", "E2", "home" },
            { "R2", "R5", "server" },
            { "R2", "R4", "trinity" },

            // Router 3
            { "R3", "R1", "lab" },
            { "R3", "R5", "home" },
            { "R3", "E3", "server" },
            { "R3", "R4", "trinity" },

            // Router 4
            { "R4", "R5", "lab" },
            { "R4", "R2", "home" },
            { "R4", "R3", "server" },
            { "R4", "E4", "trinity" },

            // Router 5
            { "R5", "R1", "lab" },
            { "R5", "R2", "home" },
            { "R5", "R3", "server" },
            { "R5", "R4", "trinity" },
    };

    Controller() {
        try {
            socket = new DatagramSocket(PORT);
            listener.go();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public synchronized void onReceipt(DatagramPacket packet) {
        try {
            byte[] data = packet.getData();
            String source = packet.getAddress().getHostName().substring(0, 2);
            switch (data[TYPE_POS]) {
                case HELLO:
                    System.out.println("Received hello from router " + source);
                    registerElement(source);
                    break;
                case PACKET_IN:
                    System.out.println("Received packet with unknown next hop from " + source);
                    if (!lookUpDestination(packet)) {
                        System.out.println("Destination does not exist - packet has been dropped");
                    }
                    break;
                default:
                    System.out.println("Received unexpected packet" + packet.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendForwardingTable(String router) throws IOException {
        ArrayList<String> table = new ArrayList<String>();
        for (int i = 0; i < preconfigInfo.length; i++) {
            if (router.equals(preconfigInfo[i][ROUTER])) {
                table.add(preconfigInfo[i][ROUTER_OUT]);
                table.add(preconfigInfo[i][DEST_ADDR]);
            }
        }

        String tableString = String.join(", ", table);

        byte[] buffer = tableString.getBytes();
        byte[] data = new byte[buffer.length + 1];
        System.arraycopy(buffer, 0, data, 1, buffer.length);
        data[TYPE_POS] = FWD_MOD;

        InetSocketAddress routerAddr = new InetSocketAddress(router, PORT);
        DatagramPacket packet = new DatagramPacket(data, data.length, routerAddr);
        socket.send(packet);

        System.out.println("Updated forwarding table has been sent to " + router);
    }

    // Register network element, i.e send routers forwarding table info
    public synchronized void registerElement(String container) throws IOException {
        sendForwardingTable(container);
    }

    private void printPreconfigInfo() {
        String format = "%-3s %3s %-3s %3s %-7s %n";

        System.out.printf(format, "R#", "|", "OUT", "|", "DEST");
        for (int i = 0; i < preconfigInfo.length; i++) {
            System.out.printf(format, preconfigInfo[i][ROUTER], "|",
                    preconfigInfo[i][ROUTER_OUT], "|", preconfigInfo[i][DEST_ADDR]);
        }
    }

    private Boolean lookUpDestination(DatagramPacket packet) {
        System.out.println("Searching for the destination...");

        String destination = getDestination(packet);

        for (int i = 0; i < preconfigInfo.length; i++) {
            if (destination.equals(preconfigInfo[i][DEST_ADDR])) {
                System.out.println("Destination found in preconfig table");
                System.out.println("Updating forwarding tables...");
                return true;
            }
        }
        return false;
    }

    public synchronized void start() throws Exception {
        System.out.println("Controller program starting...");
        System.out.println("The current view of the network is: ");
        printPreconfigInfo();
        while (true) {
            this.wait();
        }
    }

    public static void main(String[] args) {
        try {
            (new Controller()).start();
            System.out.println("Controller program completed.");
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}