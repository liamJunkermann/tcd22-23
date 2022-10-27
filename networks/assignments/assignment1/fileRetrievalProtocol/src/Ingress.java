import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Ingress extends Node {
    private ArrayList<InetSocketAddress> workerMap;
    private ArrayList<InetSocketAddress> clientMap;
    private int count = 0;

    Ingress() {
        try {
            this.socket = new DatagramSocket(PORT);
            listener.go();
            System.out.println(
                    "[Ingress] Instantiated new node");
        } catch (Exception e) {
            e.printStackTrace();
        }
        workerMap = new ArrayList<InetSocketAddress>();
        clientMap = new ArrayList<InetSocketAddress>();
    }

    private synchronized void registerClient(DatagramPacket regPacket) throws Exception {
        System.out.println("Registering new client " + regPacket.getSocketAddress().toString());
        clientMap.add((InetSocketAddress) regPacket.getSocketAddress());

        // Send reg ack
        byte[] ackData = new byte[2];
        ackData[TYPE_POS] = REGACK;
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, regPacket.getSocketAddress());
        socket.send(ackPacket);
        System.out.println("Sent RegAck Packet");
    }

    private synchronized void registerWorker(DatagramPacket packet) throws Exception {
        System.out.println("Registering new worker " + packet.getSocketAddress().toString());
        workerMap.add((InetSocketAddress) packet.getSocketAddress());

        // Send reg ack
        byte[] ackData = new byte[2];
        ackData[TYPE_POS] = REGACK;
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, packet.getSocketAddress());
        socket.send(ackPacket);
        System.out.println("Sent RegAck Packet");
    }

    /**
     * handle file response from worker to client
     * 
     * @param packet
     * @throws Exception
     */
    private synchronized void handleFileResponse(DatagramPacket packet) throws Exception {
        byte[] receivedData = packet.getData();
        byte[] buffer = getPayloadData(packet);

        InetSocketAddress target = clientMap.get(receivedData[SRC_POS]);

        InetSocketAddress srcAddr = (InetSocketAddress) packet.getSocketAddress();
        int workerId = workerMap.indexOf(srcAddr);

        byte[] fwdData = new byte[CONTROL_HEADER_LENGTH + buffer.length];
        System.arraycopy(buffer, 0, fwdData, CONTROL_HEADER_LENGTH, buffer.length);
        fwdData[TYPE_POS] = FWDFILERES;
        fwdData[SRC_POS] = (byte) workerId;
        DatagramPacket forwardPacket = new DatagramPacket(fwdData, fwdData.length, target);
        socket.send(forwardPacket);
    }

    /**
     * handle file ack from client to worker
     * 
     * @param packet
     * @throws Exception
     */
    private synchronized void handleFileAck(DatagramPacket packet) throws Exception {
        byte[] receivedData = packet.getData();
        byte[] buffer = getPayloadData(packet);

        System.out.println("forwarding data of length " + buffer.length);

        InetSocketAddress target = workerMap.get(receivedData[SRC_POS]);

        byte[] fwdData = new byte[CONTROL_HEADER_LENGTH + buffer.length];
        System.arraycopy(buffer, 0, fwdData, CONTROL_HEADER_LENGTH, buffer.length);
        fwdData[TYPE_POS] = FILERESACK;
        fwdData[SRC_POS] = (byte) clientMap.indexOf((InetSocketAddress) packet.getSocketAddress());
        DatagramPacket forwardPacket = new DatagramPacket(fwdData, fwdData.length, target);
        socket.send(forwardPacket);
    }

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

        if (workerMap.size() <= 0) {
            System.out.println("Worker map has 0 items");
            return;
        }

        InetSocketAddress target = workerMap.get(count % workerMap.size());
        count++;
        System.out.println("Temp Counter " + count);

        int clientId;
        if (clientMap.contains(srcAddr)) {
            clientId = clientMap.indexOf(srcAddr);
        } else {
            clientMap.add(srcAddr);
            clientId = clientMap.indexOf(srcAddr);
        }

        // Create new datagram with response address + filename to retrieve
        byte[] fwdData = new byte[CONTROL_HEADER_LENGTH + buffer.length];
        System.arraycopy(buffer, 0, fwdData, CONTROL_HEADER_LENGTH, buffer.length);
        fwdData[TYPE_POS] = FWDFILEREQ;
        fwdData[SRC_POS] = (byte) clientId;

        // Sending newly built data
        DatagramPacket forwardPacket = new DatagramPacket(fwdData, fwdData.length,
                target);
        socket.send(forwardPacket);
    }

    private synchronized void handleWorkerError(DatagramPacket packet) throws Exception {
        byte[] errData = new byte[CONTROL_HEADER_LENGTH];
        errData[TYPE_POS] = ERRPKT;

        DatagramPacket errPacket = new DatagramPacket(errData, errData.length,
                clientMap.get(packet.getData()[SRC_POS]));
        socket.send(errPacket);
    }

    public void onReceipt(DatagramPacket packet) {
        try {
            byte[] data = packet.getData();
            switch (data[TYPE_POS]) {
                case FILEREQ:
                    System.out.println("Received file request");
                    sendFileRequest(packet);
                    break;
                case FILERES:
                    System.out.println("Recieved File response.");
                    handleFileResponse(packet);
                    break;
                case REGCLIENT:
                    registerClient(packet);
                    break;
                case REGWORKER:
                    registerWorker(packet);
                    break;
                case FILERESACK:
                    handleFileAck(packet);
                    break;
                case ERRPKT:
                    System.out.println("Worker errored");
                    if (workerMap.indexOf(packet.getSocketAddress()) != -1) {
                        handleWorkerError(packet);
                        break;
                    }
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
