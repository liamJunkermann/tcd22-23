import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client extends Node {
    InetSocketAddress dstAddress;
    boolean registered;

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
                    System.out.println("Received test packet. Feature may not be implemented yet");
                    break;
                case REGACK:
                    registered = true;
                    System.out.println("Successfully registered client with ingress server");
                    break;
                default:
                    System.out.println("Unexpected packet " + packet.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() throws Exception {
        // Test send packet
        byte[] regData = new byte[CONTROL_HEADER_LENGTH];
        regData[TYPE_POS] = 4;
        DatagramPacket regPacket = new DatagramPacket(regData, regData.length, dstAddress);
        socket.send(regPacket);
        System.out
                .println("Sent reg packet with data");
        System.out.println("File Retrieval Protocol starting...");
        System.out.println(dstAddress.getHostName() + ":" + dstAddress.getPort());
        Scanner scanner = new Scanner(System.in);
        boolean finished = false;

        while (!finished) {
            if (registered) {
                System.out.print("To request a file enter the filename: ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("exit")) {
                    finished = true;
                } else {
                    // Request file
                    System.out.println("Requesting file " + input);
                    byte[] data = makeDataByteArray(input);
                    data[TYPE_POS] = 0;
                    DatagramPacket fileRequestPacket = new DatagramPacket(data, data.length, dstAddress);
                    socket.send(fileRequestPacket);
                }
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
