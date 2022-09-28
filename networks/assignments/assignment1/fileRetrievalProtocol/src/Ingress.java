import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Ingress extends Node {
    // private ArrayList<InetSocketAddress> workerMap;
    private ArrayList<InetSocketAddress> clientMap;

    Ingress() {
        try {
            this.socket = new DatagramSocket(PORT);
            listener.go();
            System.out.println(
                    "[Ingress] Instantiated new node");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // workerMap = new ArrayList<InetSocketAddress>();
        clientMap = new ArrayList<InetSocketAddress>();
    }

    private synchronized void registerClient(DatagramPacket regPacket) throws Exception {
        System.out.println("Registering new client " + regPacket.getSocketAddress().toString());
        clientMap.add((InetSocketAddress) regPacket.getSocketAddress());

        // Send reg ack
        byte[] ackData = new byte[2];
        ackData[TYPE_POS] = 5;
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, regPacket.getSocketAddress());
        socket.send(ackPacket);
        System.out.println("Sent RegAck Packet");
    }

    // private int count = 0;

    /**
     * handles request for file
     */
    private synchronized void sendFileRequest(DatagramPacket rPacket) throws Exception {
        byte[] receivedData = rPacket.getData();
        byte[] buffer = new byte[rPacket.getLength() - CONTROL_HEADER_LENGTH];
        System.arraycopy(receivedData, CONTROL_HEADER_LENGTH, buffer, 0, buffer.length);
        String filename = new String(buffer);
        InetSocketAddress srcAddr = (InetSocketAddress) rPacket.getSocketAddress();
        System.out.println(srcAddr.toString() + " requested " + filename);

        byte[] tempData = makeDataByteArray("received, not implemented yet");
        tempData[TYPE_POS] = 3;

        DatagramPacket tempResp = new DatagramPacket(tempData, tempData.length, srcAddr);
        socket.send(tempResp);
        // // Select target worker
        // InetSocketAddress target = workerMap.get(count % workerMap.size());
        // count++;

        // int clientId;
        // if (clientMap.contains(srcAddr)) {
        // clientId = clientMap.indexOf(srcAddr);
        // } else {
        // clientMap.add(srcAddr);
        // clientId = clientMap.indexOf(srcAddr);
        // }

        // // Create new datagram with response address + filename to retrieve
        // byte[] fwdData = new byte[CONTROL_HEADER_LENGTH + buffer.length];
        // System.arraycopy(buffer, 0, fwdData, CONTROL_HEADER_LENGTH, buffer.length);
        // fwdData[TYPE_POS] = FWDFILEREQ;
        // fwdData[SRC_POS] = (byte) clientId;

        // // Sending newly built data
        // DatagramPacket forwardPacket = new DatagramPacket(fwdData, fwdData.length,
        // target);
        // socket.send(forwardPacket);
    }

    public void onReceipt(DatagramPacket packet) {
        try {
            byte[] data = packet.getData();
            switch (data[TYPE_POS]) {
                case FILEREQ:
                    System.out.println("Received file request");
                    sendFileRequest(packet);
                    break;
                case TESTPKT:
                    System.out.println("Received test packet. Feature may not be implemented yet");
                    break;
                case REGCLIENT:
                    registerClient(packet);
                    break;
                default:
                    System.out.println("Received unexpected packet" + packet.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() throws Exception {
        System.out.println("Starting ingress program...");
        while (true) {
            this.wait();
        }
    }

    public static void main(String[] args) {
        try {
            (new Ingress()).start();
            System.out.println("Completed Ingress Program.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
