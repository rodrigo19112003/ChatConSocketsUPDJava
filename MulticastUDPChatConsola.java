import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Scanner;

public class MulticastUDPChatConsola {
    static boolean finished = false;
    static String name;
    @SuppressWarnings("deprecation")
    
    public static void main(String args[]){
        try {
            int port = 8080;
            InetAddress group = InetAddress.getByName("224.0.0.0");
            MulticastSocket socket = new MulticastSocket(port);
            socket.joinGroup(group);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Ingresa tu nombre:");
            name = scanner.nextLine();

            Thread thread = new Thread(new HiloLectura(socket, group, port));
            thread.start();

            System.out.println("Puede comenzar a escribir mensajes en el grupo...\n");

            byte[] buffer = new byte[1024];
            String line;

            while(true){
                line = scanner.nextLine();
                if(line.equalsIgnoreCase("Adios")){
                    finished = true;

                    line = name + ": Ha termiando la conexión";
                    buffer = line.getBytes();
                    DatagramPacket outMessage = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(outMessage);

                    socket.leaveGroup(group);
                    socket.close();
                    break;
                }

                line = name + ": " + line;
                buffer = line.getBytes();
                DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                socket.send(datagram);
            }
            scanner.close();

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}

class HiloLectura implements Runnable{
    private MulticastSocket socket;
    private InetAddress grupo;
    private int port;

    HiloLectura(MulticastSocket socket, InetAddress grupo, int port){
        this.socket = socket;
        this.grupo = grupo;
        this.port = port;
    }

    @Override
    public void run(){
        byte[] buffer = new byte[1024];
        String line;

        while(!MulticastUDPChatConsola.finished){
            try {
                DatagramPacket inputMessage = new DatagramPacket(buffer, buffer.length, grupo, port);
                socket.receive(inputMessage);
    
                line = new String(buffer, 0, inputMessage.getLength());
    
                if(!line.startsWith(MulticastUDPChatConsola.name)){
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Comunicacion y socket cerrados");
            }
        }
    }
}



