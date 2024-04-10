import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Scanner;

public class MulticastUDP{
    @SuppressWarnings("deprecation")
    
    public static void main(String args[]){
        try {
            int port = 8080;
            InetAddress group = InetAddress.getByName("224.0.0.0");
            MulticastSocket socket = new MulticastSocket(port);
            socket.joinGroup(group);

            Scanner scannner = new Scanner(System.in);
            System.out.print("Envie un mensaje al grupo: ");
            String message = scannner.nextLine();

            byte[] messageBytes = message.getBytes();
            DatagramPacket mensajeSalida = new DatagramPacket(messageBytes, messageBytes.length, group, port);
            socket.send(mensajeSalida);

            byte[] buffer = new byte[1024];
            String line;

            while(true){
                DatagramPacket mensajeEntrada = new DatagramPacket(buffer, buffer.length);
                socket.receive(mensajeEntrada);

                line = new String(mensajeEntrada.getData(), 0, mensajeEntrada.getLength());
                System.out.println("Recibido: " + line);

                if(line.equalsIgnoreCase("Adios")){
                    socket.leaveGroup(group);
                    break;
                }
            }

            scannner.close();
            socket.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}





