import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

public abstract class Node {
    static final int PACKETSIZE = 65000;

    static final int PORT = 50000;

    static final int CONTROL_HEADER_LENGTH = 2; // Fixed length of the control header
    static final int TYPE_POS = 0; // Position of type within header
    static final int SRC_POS = 1; // Position of source index (used only for forwarding functions)

    // Packet Types
    /**
     * File request packet from Client to Ingress
     * <p>
     * Packet data includes just requested filename.
     */
    static final byte FILEREQ = 0;
    static final byte FWDFILEREQ = 1;
    static final byte FILERES = 2;
    static final byte TESTPKT = 3;
    /**
     * Below two register client to ingress. This is done to pass client address to
     * clientMap
     */
    static final byte REGCLIENT = 4;
    static final byte REGWORKER = 5;
    static final byte REGACK = 6;
    /**
     * Forwarded File Response packet from Ingress to Client
     * <p>
     * Packet data simply includes file data.
     */
    static final byte FWDFILERES = 7;

    DatagramSocket socket;
    Listener listener;
    CountDownLatch latch;

    Node() {
        latch = new CountDownLatch(1);
        listener = new Listener();
        listener.setDaemon(true);
        listener.start();
        System.out.println("[Node] Instantiated node");
    }

    public abstract void onReceipt(DatagramPacket packet);

    protected byte[] makeDataByteArray(String message) {
        byte[] buffer = message.getBytes();
        byte[] data = new byte[CONTROL_HEADER_LENGTH + buffer.length];
        System.arraycopy(buffer, 0, data, CONTROL_HEADER_LENGTH, buffer.length);
        return data;
    }

    /**
     * Get String Data from data and packet combo
     * 
     * @param data   The result of packet.getData()
     * @param packet The entire packet
     * @return The packet string data
     */
    protected String getStringData(byte[] data, DatagramPacket packet) {
        byte[] buffer = new byte[packet.getLength() - CONTROL_HEADER_LENGTH];
        System.arraycopy(data, CONTROL_HEADER_LENGTH, buffer, 0, buffer.length);
        String string = new String(buffer);
        return string;
    }

    class Listener extends Thread {

        /* lets listener know that socket is initilalised */
        public void go() {
            latch.countDown();
            // System.out.println("[Listener] Ran go, latch counted down");
        }

        public void run() {
            try {
                // System.out.println("[Node.Listener] Run()");
                latch.await();
                while (true) {
                    DatagramPacket packet = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
                    socket.receive(packet);
                    // System.out.println("[Node] received packet from " +
                    // packet.getAddress().getHostName());
                    onReceipt(packet);
                }
            } catch (Exception e) {
                if (!(e instanceof SocketException))
                    e.printStackTrace();
            }
        }
    }

}
