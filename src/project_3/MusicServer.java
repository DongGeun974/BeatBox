package project_3;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class MusicServer {

    ArrayList clientOutputStreams;

    public static void main(String[] args) {
        System.out.println("황동근_2016250056");
        System.out.println("MusicServer Started!");
        new MusicServer().go();
    }

    public class ClientHandler implements Runnable {
        ObjectInputStream in;
        Socket sock;

        public ClientHandler(Socket clientSOcket) {
            try {
                sock = clientSOcket;
                in = new ObjectInputStream(sock.getInputStream());

            } catch (Exception ex) { ex.printStackTrace(); }
        }

        public void run() {
            Object o1;
            Object o2;
            try {
                while ((o1 = in.readObject()) != null) {
                    o2 = in.readObject();
                    System.out.println("read two object -- 황동근 서버");
                    tellEveryone(o1, o2);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }



    public void go() {
        clientOutputStreams = new ArrayList();
        try {
            ServerSocket serverSock = new ServerSocket(4242);
            while(true) {
                Socket clientSocket = serverSock.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(out);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.println("got a connection -- 황동근 서버");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void tellEveryone(Object one, Object two) {
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try {
                ObjectOutputStream out = (ObjectOutputStream) it.next();
                out.writeObject(one);
                out.writeObject(two);
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}