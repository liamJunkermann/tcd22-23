import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client extends Node {
    InetSocketAddress dstAddress;
    boolean registered;

    private Scanner scanner;

    Client() {
        try {
            dstAddress = new InetSocketAddress("ingress", PORT);
            socket = new DatagramSocket(PORT);
            registered = false;
            listener.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Client onReceipt expecting FWDFileResponse
     */
    public void onReceipt(DatagramPacket packet) {
        try {
            byte[] data = packet.getData();

            switch (data[TYPE_POS]) {
                case TESTPKT:
                    System.out.println("\nReceived test packet. Feature may not be implemented yet");
                    break;
                case REGACK:
                    registered = true;
                    System.out.println("\nSuccessfully registered client with ingress server");
                    break;
                default:
                    System.out.println("\nUnexpected packet " + packet.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean handleInput() throws Exception {
        System.out.print("To request a file enter the filename: ");
        String input = scanner.nextLine();

        if (input.equalsIgnoreCase("exit")) {
            return true;
        } else {
            // Request file
            System.out.println("Requesting file " + input);
            byte[] data = makeDataByteArray(input);
            data[TYPE_POS] = 0;
            DatagramPacket fileRequestPacket = new DatagramPacket(data, data.length, dstAddress);
            socket.send(fileRequestPacket);
            return false;
        }
    }

    public synchronized void start() throws Exception {
        // Instantiate Scanner
        scanner = new Scanner(System.in);
        // Test send packet
        byte[] regData = new byte[CONTROL_HEADER_LENGTH];
        regData[TYPE_POS] = REGCLIENT;
        DatagramPacket regPacket = new DatagramPacket(regData, regData.length, dstAddress);
        socket.send(regPacket);
        System.out
                .println("Sent reg packet with data");
        System.out.println("File Retrieval Protocol starting...");
        System.out.println(dstAddress.getHostName() + ":" + dstAddress.getPort());
        boolean finished = false;

        while (!finished) {
            if (registered) {
                finished = handleInput();
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        try {
            (new Client()).start();
            System.out.println("Client completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
