import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MulticastUDPChatGrafico extends JFrame {
    private static final List<ChatFrame> chatFrames = new ArrayList<>();
    @SuppressWarnings("deprecation")
    
    public static void main(String[] args) {
        String name = JOptionPane.showInputDialog(null, "Ingresa tu nombre");
        try {
            int port = 8080;
            InetAddress group = InetAddress.getByName("224.0.0.0");
            MulticastSocket socket = new MulticastSocket(port);
            socket.joinGroup(group);

            if (name != null) {
                ChatFrame chatFrame = new ChatFrame(name, socket, group, port);
                Thread thread = new Thread(chatFrame);
                thread.start();
                chatFrame.setVisible(true);
                chatFrames.add(chatFrame);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void removeChatFrame(ChatFrame chatFrame) {
        chatFrames.remove(chatFrame);
        if (chatFrames.isEmpty()) {
            System.exit(0);
        }
    }
}

class ChatFrame extends JFrame implements Runnable {
    private JTextArea chatArea;
    private JTextField messageField;
    private JLabel nameLabel;
    private JLabel messageLabel;
    private JButton sendButton;
    private String name;
    private MulticastSocket socket;
    private InetAddress group;
    private int port;

    public ChatFrame(String name, MulticastSocket socket, InetAddress group, int port) {
        this.name = name;
        this.socket = socket;
        this.group = group;
        this.port = port;
        setTitle("Multicast UDP Chat - " + name);
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        addComponentsToFrame();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    String exitMessage = name + " ha terminado la conexion";
                    byte[] exitMessageBytes = exitMessage.getBytes();
                    DatagramPacket exitPacket = new DatagramPacket(exitMessageBytes, exitMessageBytes.length, group, port);
                    socket.send(exitPacket);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                MulticastUDPChatGrafico.removeChatFrame(ChatFrame.this);
            }
        });
    }

    private void initComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setMargin(new Insets(15, 15, 15, 15));

        nameLabel = new JLabel("Usuario: " + name);
        messageLabel = new JLabel("Escribir mensaje:");
        messageField = new JTextField(30);
        sendButton = new JButton("Enviar");

        sendButton.addActionListener(e -> sendMessage());

        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
    }

    private void addComponentsToFrame() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());

        JPanel nameUserPanel = new JPanel();
        nameUserPanel.add(nameLabel);

        userPanel.add(nameUserPanel, BorderLayout.NORTH);

        JPanel messagePanel = new JPanel();
        messagePanel.add(messageLabel);
        messagePanel.add(messageField);
        messagePanel.add(sendButton);
        userPanel.add(messagePanel, BorderLayout.CENTER);

        contentPane.add(userPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            chatArea.append("Tu: " + message + "\n");
            try {
                String fullMessage = name + ": " + message;
                byte[] messageBytes = fullMessage.getBytes();
                DatagramPacket outMessage = new DatagramPacket(messageBytes, messageBytes.length, group, port);
                socket.send(outMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            messageField.setText("");
        }
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            String line;

            while (true) {
                DatagramPacket inputMessage = new DatagramPacket(buffer, buffer.length);
                socket.receive(inputMessage);

                line = new String(buffer, 0, inputMessage.getLength());

                if(!line.startsWith(name)){
                    chatArea.append(line + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Comunicacion y socket cerrados");
        }
    }
}



