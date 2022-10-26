import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    private void handleGetFile(DatagramPacket packet) {
        if (registered) {

            byte[] packetData = packet.getData();
            String filename = getStringData(packetData, packet);
            System.out.println("Forwarded File Request recieved for file " + filename);
            try {
                File file = new File("../files/" + filename);
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
                    // Creating response packet
                    byte[] fileResp = new byte[CONTROL_HEADER_LENGTH + fileLen];
                    // Set Response Packet to be file response
                    fileResp[TYPE_POS] = FILERES;
                    // Set forwarding idx
                    fileResp[SRC_POS] = packetData[SRC_POS];
                    // Copying file content to response
                    System.arraycopy(buffer, 0, fileResp, CONTROL_HEADER_LENGTH, fileLen);

                    DatagramPacket fileRespPacket = new DatagramPacket(fileResp, fileResp.length, dstAddress);
                    socket.send(fileRespPacket);

                    System.out.println("Sent File Resp Packet");
                } else {
                    System.out.println(
                            "File " + file.getAbsolutePath() + " does not exist, sending error");
                    handleError(packet);

                }
            } catch (Exception e) {
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
