import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ChatServer extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextArea tablon;
	private JTextField txtMensaje;
	private ServerSocket servidor;
	private JButton btnEnviar, btnHistorial;
	private static Historial historial;
	private List<gestorClientes> clientes = Collections.synchronizedList(new ArrayList<gestorClientes>());

	public ChatServer() throws Exception {
		super("Servidor");
		historial = new Historial("inicio:\n");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 600);

		JPanel panelPrincipal = new JPanel();
		panelPrincipal.setLayout(new BorderLayout());
		add(panelPrincipal);

		tablon = new JTextArea();
		tablon.setEditable(false);
		panelPrincipal.add(new JScrollPane(tablon), BorderLayout.CENTER);

		JPanel panelEntrada = new JPanel();
		panelEntrada.setLayout(new BorderLayout());
		panelPrincipal.add(panelEntrada, BorderLayout.SOUTH);

		txtMensaje = new JTextField();
		panelEntrada.add(txtMensaje, BorderLayout.CENTER);

		btnEnviar = new JButton("Enviar");
		panelEntrada.add(btnEnviar, BorderLayout.EAST);

		btnEnviar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String mensaje = txtMensaje.getText();
				txtMensaje.setText("");
				enviarMensaje(mensaje);
			}
		});

		btnHistorial = new JButton("Historial");
		panelEntrada.add(btnHistorial, BorderLayout.WEST);
		btnHistorial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(historial.getHistorial());
			}
		});
		txtMensaje.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mensaje = txtMensaje.getText();
				txtMensaje.setText("");
				enviarMensaje(mensaje);
			}
		});

		setVisible(true);

		servidor = new ServerSocket(1234);
		tablon.append(hora() + "Servidor iniciado en el puerto 1234\n");

		while (true) {
			Socket cliente = servidor.accept();
			// tablon.append("Cliente conectado desde " +
			// cliente.getInetAddress().getHostAddress() + "\n");

			gestorClientes gestor = new gestorClientes(cliente);
			clientes.add(gestor);
			gestor.start();
		}
	}

	private void enviarMensaje(String mensaje) {
		String mensajeFinal = hora() + "Servidor: " + mensaje + "\n";
		tablon.append(mensajeFinal);
		historial.anadirHistorial(mensajeFinal);

		for (gestorClientes cliente : clientes) {
			cliente.enviarMensaje(mensajeFinal);
		}
	}

	private class gestorClientes extends Thread {
		private Socket cliente;
		private DataInputStream in;
		private DataOutputStream out;
		private Historial historial;
		private String nick;

		public gestorClientes(Socket cliente) throws Exception {
			this.cliente = cliente;
			in = new DataInputStream(cliente.getInputStream());
			out = new DataOutputStream(cliente.getOutputStream());
			historial = ChatServer.historial;
			this.nick = "Anónimo";
		}

		public void enviarMensaje(String mensaje) {
			try {
				out.writeUTF(mensaje);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				String recibo = in.readUTF();
				if (recibo.startsWith("/nick"))
					nick = recibo.substring(6);
				for (gestorClientes cliente : clientes) {
					cliente.out.writeUTF("Bienvenido al chat, " + nick + "! \n");
				}

				tablon.append("Cliente conectado desde " + cliente.getInetAddress().getHostAddress() + " con el nick: "
						+ nick + "\n");
				historial.anadirHistorial("Cliente conectado desde " + cliente.getInetAddress().getHostAddress()
						+ " con el nick: " + nick + "\n");

				while (true) {
					String mensaje = in.readUTF();
					String mensajeFinal = mensaje;
					tablon.append(hora() + " " + mensajeFinal + "\n");
					historial.anadirHistorial(hora() + " " + mensajeFinal + "\n");
					for (gestorClientes cliente : clientes) {
						if (cliente != this) {
							cliente.enviarMensaje(hora() + " " + mensajeFinal + "\n");
						}
					}
				}
			} catch (Exception e) {
				tablon.append(hora() + "Cliente desconectado: " + nick + "\n");
				historial.anadirHistorial(hora() + "Cliente desconectado: " + nick + "\n");
				clientes.remove(this);
			}
		}
	}

	public String hora() {
		LocalTime horaActual = LocalTime.now();
		DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm");
		String horaFormateada = horaActual.format(formato);
		return horaFormateada + " ";
	}

	public static void main(String[] args) throws Exception {
		new ChatServer();
	}
}
