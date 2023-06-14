import java.util.concurrent.locks.ReentrantLock;

public class Historial {
	String historial;
	ReentrantLock lock = new ReentrantLock();

	public Historial(String historial) {
		super();
		this.historial = historial;
	}

	public Historial() {

	}

	public String getHistorial() {
		return historial;
	}

	public void setHistorial(String historial) {
		this.historial = historial;
	}

	public void anadirHistorial(String mensaje) {
		lock.lock();
		try {
			historial = historial.concat("\n" + mensaje);
		} finally {
			lock.unlock();
		}

	}

}
