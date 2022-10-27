import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client extends Node {
    InetSocketAddress dstAddress;
    private boolean registered;
    private boolean blocked;

    private Scanner scanner;

    private ByteArrayOutputStream outputStream;

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

    private void sendFileAck(int foundLast, DatagramSocket socket, byte srcIdx) throws IOException {
        // File Response Ack Payload
        byte[] ackPacketData = new byte[2];
        ackPacketData[0] = (byte) (foundLast >> 8);
        ackPacketData[1] = (byte) (foundLast);

        byte[] payloadData = generatePacketData(FILERESACK, srcIdx, ackPacketData);
        DatagramPacket ackPacket = new DatagramPacket(payloadData, payloadData.length, dstAddress);
        socket.send(ackPacket);
        System.out.println("Sent ack: Sequence Number = " + foundLast);
    }

    boolean flag;
    int sequenceNumber = 0;
    int foundLast = 0;

    /**
     * Handle File Response
     * <p>
     * File may be coming in multiple packets, so we need to return acknowledgement
     * as well as print what has been sent
     * 
     * @param packet
     * @throws Exception
     */
    private void handleFileRes(DatagramPacket packet) throws Exception {
        byte[] message = getPayloadData(packet);
        byte[] fileByteArray = new byte[1021];

        // Retrieve sequence number
        sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
        // Check if we reached last datagram (end of file)
        flag = (message[2] & 0xff) == 1;
        System.out.println("flag: " + flag);

        // If sequence number is the last seen + 1, then it is correct
        // We get the data from the message and write the ack that it has been received
        // correctly
        if (sequenceNumber == (foundLast + 1)) {

            // set the last sequence number to be the one we just received
            foundLast = sequenceNumber;

            // Retrieve data from message
            System.arraycopy(message, 3, fileByteArray, 0, 1021);

            // Write the retrieved data to the output stream buffer and print received data
            // sequence number
            outputStream.write(fileByteArray);

            System.out.println("Received: Sequence number:" + foundLast);

            // Send acknowledgement
            sendFileAck(foundLast, socket, packet.getData()[SRC_POS]);
        } else {
            System.out.println("Expected sequence number: " + (foundLast + 1) + " but received " + sequenceNumber
                    + ". DISCARDING");
            // Re send the acknowledgement
            sendFileAck(foundLast, socket, packet.getData()[SRC_POS]);
        }
        // Check for last datagram
        if (flag) {
            System.out.println("Finished receiving File");
            System.out.println(outputStream.toString());
            outputStream.flush();
            blocked = false;
            return;
        }
    }

    /**
     * Client onReceipt expecting FWDFileResponse
     */
    public void onReceipt(DatagramPacket packet) {
        try {
            byte[] data = packet.getData();

            switch (data[TYPE_POS]) {
                case FWDFILERES:
                    handleFileRes(packet);
                    break;
                case ERRPKT:
                    System.out.println(
                            "\nAn error occurred within the Ingress or Worker\nMake sure you've entered a valid file and ensure you have setup the system properly");
                    blocked = false;
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
            blocked = true;

            outputStream = new ByteArrayOutputStream();

            sequenceNumber = 0;
            foundLast = 0;
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
            if (registered && !blocked) {
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
