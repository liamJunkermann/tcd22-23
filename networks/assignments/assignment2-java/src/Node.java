import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

public abstract class Node {
    static final int PACKETSIZE = 65000;
    static final int PORT = 54321;
    static final int CONTROL_HEADER_LENGTH = 2; // Fixed length of the control header

    // Header info idx
    static final int TYPE_POS = 0; // Position of type within header
    static final int LEN_POS = 1; // Position of destination length

    // Packet Types
    static final byte HELLO = 0;
    static final byte PACKET_IN = 1;
    static final byte FWD_MOD = 2;
    static final byte NETWORK_ID = 3;

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

    /**
     * Make byte array from string message
     * 
     * @param message
     * @return
     */
    protected byte[] makeDataByteArray(String message) {
        byte[] buffer = message.getBytes();
        byte[] data = new byte[CONTROL_HEADER_LENGTH + buffer.length];
        System.arraycopy(buffer, 0, data, CONTROL_HEADER_LENGTH, buffer.length);
        return data;
    }

    /**
     * Get Message from packet
     * 
     * @param data   packet data
     * @param packet the actual packet
     * @return The message as a string
     */
    protected String getMessage(byte[] data, DatagramPacket packet) {
        int dstLength = data[LEN_POS];
        byte[] buffer = new byte[packet.getLength() - CONTROL_HEADER_LENGTH - dstLength];
        System.arraycopy(data, CONTROL_HEADER_LENGTH + dstLength, buffer, 0, buffer.length);
        String message = new String(buffer);
        return message;
    }

    /**
     * Get the destination from the endnode packet
     * 
     * @param packet
     * @return
     */
    protected String getDestination(DatagramPacket packet) {
        byte[] data = packet.getData();
        int dstLength = data[LEN_POS];

        byte[] buffer = new byte[dstLength];
        System.arraycopy(data, CONTROL_HEADER_LENGTH, buffer, 0, buffer.length);
        String destination = new String(buffer);
        return destination;
    }

    class Listener extends Thread {

        /* lets listener know that socket is initialised */
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
