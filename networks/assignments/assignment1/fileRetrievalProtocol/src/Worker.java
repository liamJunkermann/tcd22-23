import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Worker extends Node {
    private InetSocketAddress dstAddress = new InetSocketAddress("ingress", PORT);
    private boolean registered;
    private String workerName;

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

    public void onReceipt(DatagramPacket packet) {
        try {
            byte[] data = packet.getData();

            switch (data[TYPE_POS]) {
                case FWDFILEREQ:
                    System.out.println("Forwarded File Request recieved");
                    break;
                case TESTPKT:
                    System.out.println("Recieved test packet. Feature may not be implemented yet");
                    break;
                case REGACK:
                    registered = true;
                    System.out.println("Successfully registered worker with ingress server");
                    break;
                default:
                    System.out.println("Unexpected packet " + packet.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() throws Exception {
        System.out.println("Starting worker " + workerName + " program...");

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
            (new Worker(args[0])).start();
            System.out.println("Completed Worker Program");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
