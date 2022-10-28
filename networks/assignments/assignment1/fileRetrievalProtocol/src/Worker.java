import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class Worker extends Node {
    private InetSocketAddress dstAddress = new InetSocketAddress("ingress", PORT);
    private boolean registered;
    private String workerName;

    // variables for sending large files
    int sequenceNumber = 0;
    boolean flag;
    int ackSequence = 0;
    byte[] fileByteArray;
    DatagramPacket lastPacket;
    boolean ackRec;

    Worker() {
        try {
            socket = new DatagramSocket(PORT);
            listener.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Worker(String workerName) {
        try {
            socket = new DatagramSocket(PORT);
            this.workerName = workerName;
            listener.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] fileToByteArray(File file) throws Exception {
        if (file.exists()) {
            System.out.println("File " + file.getAbsolutePath() + " exists");
            int fileLen = (int) file.length();
            byte[] buffer = new byte[fileLen];
            FileInputStream in = new FileInputStream(file);
            int bytes_read = 0, n;
            do { // loop until we've read it all
                n = in.read(buffer, bytes_read, fileLen - bytes_read);
                bytes_read += n;
            } while ((bytes_read < fileLen) && (n != -1));
            in.close();
            return buffer;
        } else {
            throw new Exception("File " + file.getAbsolutePath() + " does not exist, sending error");
        }
    }

    // private void handleFileSend(DatagramPacket packet) throws Exception {
    // // create packet to send based on sequence no
    // sequenceNumber += 1;

    // byte[] message = new byte[1024];
    // message[0] = (byte) (sequenceNumber >> 8);
    // message[1] = (byte) (sequenceNumber);

    // if (((sequenceNumber * 1021) + 1021) >= fileByteArray.length) {
    // flag = true;
    // message[2] = (byte) 1;
    // } else {
    // flag = false;
    // message[2] = (byte) 0;
    // }

    // if (!flag) {
    // System.arraycopy(fileByteArray, sequenceNumber * 1021, message, 3, 1021);
    // } else {
    // // If last datagram
    // System.arraycopy(fileByteArray, sequenceNumber * 1021, message, 3,
    // fileByteArray.length - (sequenceNumber * 1021));
    // }

    // byte[] packetData = generatePacketData(FILERES, packet.getData()[SRC_POS],
    // message);
    // lastPacket = new DatagramPacket(packetData, packetData.length, dstAddress);
    // socket.send(lastPacket);
    // System.out.println("Sent: Sequence number = " + sequenceNumber);

    // }

    // private void handleFileAck(DatagramPacket packet) throws Exception {
    // byte[] ack = getPayloadData(packet);
    // ackSequence = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff); // determine ack
    // sequence no
    // ackRec = true;

    // if((ackSequence == sequenceNumber) && ackRec) {
    // System.out.println("Ack received: Sequence no = "+ ackSequence);
    // handleFileSend(packet);
    // } else {
    // // Package not received, resending it
    // System.out.println("Resending: Sequence no = "+ sequenceNumber);
    // }
    // }

    private void sendFile(DatagramSocket socket, byte[] fileByteArray, byte srcIdx) throws Exception {
        System.out.println("Sending file");
        int sequenceNumber = 0; // For order
        boolean flag; // To see if we got to the end of the file
        int ackSequence = 0; // To see if the datagram was received correctly

        for (int i = 0; i < fileByteArray.length; i = i + 1021) {
            sequenceNumber += 1;

            // Create message (this is wrapped by our packets)
            byte[] message = new byte[1024]; // First two bytes of the data are for control (datagram integrity and
                                             // order)
            message[0] = (byte) (sequenceNumber >> 8);
            message[1] = (byte) (sequenceNumber);

            if ((i + 1021) >= fileByteArray.length) { // Have we reached the end of file?
                flag = true;
                message[2] = (byte) (1); // We reached the end of the file (last datagram to be send)
            } else {
                flag = false;
                message[2] = (byte) (0); // We haven't reached the end of the file, still sending datagrams
            }

            if (!flag) {
                System.arraycopy(fileByteArray, i, message, 3, 1021);
            } else { // If it is the last datagram
                System.arraycopy(fileByteArray, i, message, 3, fileByteArray.length - i);
            }

            byte[] packetData = generatePacketData(FILERES, srcIdx, message);
            DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length, dstAddress);
            socket.send(sendPacket); // Sending the data
            System.out.println("Sent: Sequence number = " + sequenceNumber);

            boolean ackRec; // Was the datagram received?

            while (true) {
                byte[] wrappedAck = new byte[4]; // Create another packet for datagram acknowledgement
                DatagramPacket ackpack = new DatagramPacket(wrappedAck, wrappedAck.length);

                try {
                    socket.receive(ackpack);// Waiting for the server to send the ack
                    byte[] ack = new byte[2];
                    System.arraycopy(wrappedAck, 2, ack, 0, ack.length);
                    ackSequence = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff); // Figuring the sequence number
                    ackRec = true; // We received the ack
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timed out waiting for ack");
                    ackRec = false; // We did not receive an ack
                }

                // If the package was received correctly next packet can be sent
                if ((ackSequence == sequenceNumber) && (ackRec)) {
                    System.out.println("Ack received: Sequence Number = " + ackSequence);
                    break;
                } // Package was not received, so we resend it
                else {
                    socket.send(sendPacket);
                    System.out.println("Resending: Sequence Number = " + sequenceNumber);
                }
            }
        }
    }

    private void handleGetFile(DatagramPacket packet) {
        if (registered) {
            byte[] packetData = packet.getData();
            String filename = getStringData(packetData, packet);
            System.out.println("Forwarded File Request recieved for file " + filename);
            try {
                File file = new File("../files/" + filename);
                byte[] buffer = fileToByteArray(file);

                sendFile(socket, buffer, packetData[SRC_POS]);

                System.out.println("Sent File Resp Packet(s)");
            } catch (Exception e) {
                handleError(packet);
                e.printStackTrace();
            }
        } else {
            System.err.println(
                    "Worker node not registered, restart to try to re-register with ingress. \nMake sure to start the ingress server first");
        }
    }

    private void handleError(DatagramPacket packet) {
        byte[] errData = new byte[CONTROL_HEADER_LENGTH];
        errData[TYPE_POS] = ERRPKT;
        errData[SRC_POS] = packet.getData()[SRC_POS];

        DatagramPacket errPacket = new DatagramPacket(errData, errData.length, dstAddress);
        try {
            socket.send(errPacket);
        } catch (IOException e) {
            System.err.println("Something has gone quite badly, restart the whole server");
            e.printStackTrace();
        }
    }

    public void onReceipt(DatagramPacket packet) {
        try {
            byte[] data = packet.getData();

            switch (data[TYPE_POS]) {
                case FWDFILEREQ:
                    handleGetFile(packet);
                    break;
                case REGACK:
                    registered = true;
                    System.out.println("Successfully registered worker with ingress server");
                    break;
                default:
                    System.out.println("Unexpected packet " + packet.toString());
            }
        } catch (Exception e) {
            handleError(packet);
            e.printStackTrace();
        }
    }

    public synchronized void start() throws Exception {
        System.out.println("Starting worker " + workerName + " program...");
        wait(300);

        byte[] regData = new byte[CONTROL_HEADER_LENGTH];
        regData[TYPE_POS] = REGWORKER;
        DatagramPacket regPacket = new DatagramPacket(regData, regData.length, dstAddress);
        socket.send(regPacket);
        System.out
                .println("Sent reg packet with data");
        System.out.println("File Retrieval Protocol starting...");
        System.out.println(dstAddress.getHostName() + ":" + dstAddress.getPort());

        while (true) {
            this.wait();
        }
    }

    public static void main(String[] args) {
        try {
            Worker worker;
            if (args.length >= 0) {
                worker = new Worker(args[0]);
            } else {
                worker = new Worker("worker x");
            }
            worker.start();
            System.out.println("Completed Worker Program");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
