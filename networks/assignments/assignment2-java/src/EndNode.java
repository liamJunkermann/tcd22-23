import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

public class EndNode extends Node {
    EndNode() {
        try {
            socket = new DatagramSocket(PORT);
            listener.go();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public synchronized void onReceipt(DatagramPacket packet) {
        byte[] data = packet.getData();
        String message = getMessage(data, packet);
        System.out.println("Received message: " + message);
    }

    public synchronized void sendMessage(Scanner scanner) throws IOException {
        // Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the destination you want to send a message to: ");
        String destination = scanner.nextLine();

        System.out.println("Enter the message you want to send to " + destination + ":");
        String message = scanner.nextLine();

        String stringData = destination + message;

        byte[] data = makeDataByteArray(stringData);
        data[TYPE_POS] = NETWORK_ID;
        data[LEN_POS] = (byte) destination.length();

        InetSocketAddress dstAddress = new InetSocketAddress("R1", PORT);
        DatagramPacket packet = new DatagramPacket(data, data.length, dstAddress);
        socket.send(packet);

        System.out.println("Message " + message + " sent to " + destination);

    }

    private synchronized void start() throws IOException, InterruptedException {
        System.out.println("EndNode program starting...");

        Scanner scanner = new Scanner(System.in);
        boolean finished = false;

        while (!finished) {
            System.out.println("Do you want to send or receive a message?");
            System.out.println("Enter SEND or WAIT: ");

            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("SEND")) {
                sendMessage(scanner);
            } else if (choice.equalsIgnoreCase("WAIT")) {
                System.out.println("Waiting for messages...");
                // this.wait();
            } else if (choice.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye ;(");
                finished = true;
            } else {
                System.out.println("Invalid input.");
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        try {
            (new EndNode()).start();
            System.out.println("EndNode program completed.");
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}
