import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BancoDeDados {
    private final Map<Integer, String> dados;
    private final Semaphore leituras;
    private final ReadWriteLock trava = new ReentrantReadWriteLock(true);
    private final Lock travaDeEscrita = trava.writeLock();
    private final Lock travaDeLeitura = trava.readLock();
    private int id = 0;

    public BancoDeDados(int numeroMaximoDeLeiturasSimultaneas) {
        this.dados = new HashMap<>();
        this.leituras = new Semaphore(numeroMaximoDeLeiturasSimultaneas);
    }

    public void insert(String valor) {
        travaDeEscrita.lock();
        this.dados.put(id, valor);
        id++;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Inserido: \"" + valor + "\" com ID: \"" + (id - 1) + "\" pelo Thread: " + Thread.currentThread().threadId());
        travaDeEscrita.unlock();
    }

    public String read(int id) {
        if (!leituras.tryAcquire()) {
            System.out.println("Limite de leituras simultaneas atingido, Thread " + Thread.currentThread().threadId() + " estÃ¡ aguardando...");
            try {
                leituras.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        travaDeLeitura.lock();
        String valor = this.dados.get(id);
        if (valor == null) {
            System.out.println("Nenhuma linha encontrada");
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Lido: \"" + valor + "\" com ID: \"" + id + "\" pelo Thread: " + Thread.currentThread().threadId());
        travaDeLeitura.unlock();

        leituras.release();
        return valor;
    }

    public void update(int id, String valor) {
        travaDeEscrita.lock();
        if (this.dados.containsKey(id)) {
            this.dados.put(id, valor);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Atualizado: \"" + valor + "\" com ID: \"" + id + "\" pelo Thread: " + Thread.currentThread().threadId());
        } else {
            System.out.println("Nenhuma linha atualizada");
        }
        travaDeEscrita.unlock();
    }

    public void delete(int id) {
        travaDeEscrita.lock();
        if (this.dados.containsKey(id)) {
            this.dados.remove(id);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Removido ID: \"" + id + "\" pelo Thread: " + Thread.currentThread().threadId());
        } else {
            System.out.println("Nenhuma linha deletada");
        }
        travaDeEscrita.unlock();
    }

    public int maiorId() {
        return id;
    }

    public void mostrarDados() {
        System.out.println("Dados no Banco de Dados:");
        travaDeLeitura.lock();
        for (Map.Entry<Integer, String> entry : dados.entrySet()) {
            System.out.println("ID: " + entry.getKey() + ", Valor: " + entry.getValue());
        }
        travaDeLeitura.unlock();
    }
}

class TesteBancoDeDados {
    public static int numeroAleatorioEntre(int min, int max) {

        double f = Math.random() / Math.nextDown(1.0);
        return (int) (min * (1.0 - f) + max * f);
    }

    static void criarRegistros(BancoDeDados bancoDeDados) {
        bancoDeDados.insert("Original");
    }

    static void lerRegistros(BancoDeDados bancoDeDados) {
        bancoDeDados.read(numeroAleatorioEntre(0, bancoDeDados.maiorId() - 1));
    }

    static void atualizarRegistros(BancoDeDados bancoDeDados) {
        bancoDeDados.update(numeroAleatorioEntre(0, bancoDeDados.maiorId() - 1), "Modificado");
    }

    static void deletarRegistros(BancoDeDados bancoDeDados) {
        bancoDeDados.delete(numeroAleatorioEntre(0, bancoDeDados.maiorId() - 1));
    }

    public static void main(String[] args) {
        ExecutorService usuarios = Executors.newFixedThreadPool(20);

        BancoDeDados bancoDeDados = new BancoDeDados(10);

        for (int i = 0; i < 25; i++) {
            criarRegistros(bancoDeDados);
        }

        for (int i = 0; i < 100; i++) {
            usuarios.submit(() -> {
                int operacao = numeroAleatorioEntre(0, 1000) % 8;
                switch (operacao) {
                    case 0 -> criarRegistros(bancoDeDados);
                    case 1 -> atualizarRegistros(bancoDeDados);
                    case 2 -> deletarRegistros(bancoDeDados);
                    default -> lerRegistros(bancoDeDados);
                }
            });
        }

        usuarios.shutdown();
        try {
            if (usuarios.awaitTermination(5, java.util.concurrent.TimeUnit.MINUTES)) {
                bancoDeDados.mostrarDados();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}