import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Clientes {
    private final Queue<String> filaDeClientes = new LinkedList<>();
    private Integer idUltimoCliente = 0;
    private final Integer tamanhoMaximoDaFila;

    public Clientes(int tamanhoMaximoDaFila) {
        this.tamanhoMaximoDaFila = tamanhoMaximoDaFila;
    }

    public synchronized void novoCliente(long tempoDeChegada) {
        String cliente = "Cliente " + idUltimoCliente;
        idUltimoCliente++;
        if (filaDeClientes.size() < tamanhoMaximoDaFila) {
            filaDeClientes.add(cliente);
            if (filaDeClientes.size() == 1) {
                notify();
            }
            System.out.println(cliente + " chegou à fila após " + tempoDeChegada / 1000 + " s");
        } else {
            System.out.println("Fila cheia, " + cliente + " não adicionado.");
        }
    }

    public synchronized String removeCliente() {
        while (filaDeClientes.isEmpty()) {
            System.out.println("Fila vazia, nenhum cliente para remover. Barbeiro " + Thread.currentThread().threadId() + " aguardando.");
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return filaDeClientes.remove();
    }
}

public class BarbeiroDorminhoco {
    public static long tempoAleatorio(long min, long max) {
        double f = Math.random() / Math.nextDown(1.0);
        return (long) (min * (1.0 - f) + max * f);
    }

    public static void main(String[] args) {
        Clientes clientes = new Clientes(10);

        ExecutorService barbeiros = Executors.newFixedThreadPool(2);
        ExecutorService clientesThread = Executors.newSingleThreadExecutor();

        for (int i = 0; i < 10; i++) {
            barbeiros.execute(() -> {
                long tempoDeCorte = tempoAleatorio(5000, 10000);
                String cliente = null;
                try {
                    Thread.sleep(tempoDeCorte);
                    cliente = clientes.removeCliente();
                } catch (InterruptedException e) {
                    System.err.println("Barbeiro interrompido durante o corte.");
                    Thread.currentThread().interrupt();
                }
                System.out.println("Barbeiro " + Thread.currentThread().threadId() + " cortou o cabelo do " + cliente + " em " + tempoDeCorte / 1000 + " s");
            });

            clientesThread.execute(() -> {
                long tempoDeChegada = tempoAleatorio(4000, 6000);

                try {
                    Thread.sleep(tempoDeChegada);
                    clientes.novoCliente(tempoDeChegada);
                } catch (InterruptedException e) {
                    System.err.println("Cliente interrompido ao entrar na fila.");
                    Thread.currentThread().interrupt();
                }
            });
        }

        barbeiros.shutdown();
        clientesThread.shutdown();
    }
}