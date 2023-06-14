import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatCliente extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextArea tablon;
	private JTextField txtMensaje;
	private JButton btnEnviar;
	private DataInputStream in;
	private DataOutputStream out;
	private Socket cliente;
	private JTextField txtNombre;
	private JPanel panelNick;
	private JTextArea tvNombre;
	private JButton btnGuardar;
	private String nick;

	public ChatCliente() {
		super("Cliente");
		nick = "Anonimo";
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 600);

		JPanel panelPrincipal = new JPanel();
		panelPrincipal.setLayout(new BorderLayout());
		getContentPane().add(panelPrincipal);

		tablon = new JTextArea();
		tablon.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(tablon);
		panelPrincipal.add(scrollPane, BorderLayout.CENTER);

		panelNick = new JPanel();
		scrollPane.setColumnHeaderView(panelNick);

		tvNombre = new JTextArea();
		tvNombre.setText("Introduce tu Nick:");
		panelNick.add(tvNombre);

		txtNombre = new JTextField();
		panelNick.add(txtNombre);
		txtNombre.setColumns(10);

		btnGuardar = new JButton("Guardar");
		panelNick.add(btnGuardar);

		JPanel panelEntrada = new JPanel();
		panelEntrada.setLayout(new BorderLayout());
		panelPrincipal.add(panelEntrada, BorderLayout.SOUTH);

		txtMensaje = new JTextField();
		panelEntrada.add(txtMensaje, BorderLayout.CENTER);

		btnEnviar = new JButton("Enviar");
		panelEntrada.add(btnEnviar, BorderLayout.EAST);
		btnEnviar.setEnabled(false);

		txtNombre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					nick = txtNombre.getText();
					out.writeUTF("/nick " + nick);
					txtNombre.setEnabled(false);
					btnGuardar.setEnabled(false);
					btnEnviar.setEnabled(true);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		txtMensaje.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarMensaje();
			}
		});
		btnEnviar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarMensaje();
			}
		});
		btnGuardar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					nick = txtNombre.getText();
					out.writeUTF("/nick " + nick);
					txtNombre.setEnabled(false);
					btnGuardar.setEnabled(false);
					btnEnviar.setEnabled(true);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		setVisible(true);

		try {
			cliente = new Socket("localhost", 1234);
			in = new DataInputStream(cliente.getInputStream());
			out = new DataOutputStream(cliente.getOutputStream());
			new Thread(new Runnable() {
				public void run() {
					try {
						String mensaje;
						while ((mensaje = in.readUTF()) != null) {
							tablon.append(mensaje);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void enviarMensaje() {
		String mensaje = txtMensaje.getText();
		String mensajeFinal = nick + ": " + mensaje;
		txtMensaje.setText("");
		tablon.append(hora() + mensajeFinal + "\n");

		try {
			out.writeUTF(mensajeFinal);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public String hora() {
		LocalTime horaActual = LocalTime.now();
		DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm");
		String horaFormateada = horaActual.format(formato);
		return horaFormateada + " ";
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getNick() {
		return nick;
	}

	public static void main(String[] args) {
		new ChatCliente();
	}
}
