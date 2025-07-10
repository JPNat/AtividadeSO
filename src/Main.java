import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Main {

    public static int numeroAleatorioEntre(int min, int max) {
        double f = Math.random() / Math.nextDown(1.0);
        return (int) (min * (1.0 - f) + max * f);
    }

    public static void main(String[] args) {
        ExecutorService produtores = Executors.newSingleThreadExecutor();
        ExecutorService consumidores = Executors.newFixedThreadPool(2);

        Vector<String> lista = new Vector<>();

        for (int i = 0; i < 10; i++) {
            produtores.submit(() -> {
                String elemento = "Elemento " + Math.random();
                try {
                    lista.add(elemento);
                    System.out.println("Thread " + Thread.currentThread().threadId() + " adicionou o " + elemento);
                } catch (Exception e) {
                    System.err.println("Thread " + Thread.currentThread().threadId() + " não conseguiu adicionar o " + elemento);
                }
            });

            consumidores.submit(() -> {
                if (!lista.isEmpty()) { // Verifica se há elementos antes de tentar remover
                    int indice = numeroAleatorioEntre(0, lista.size() - 1);
                    try {
                        String elementoRemovido = lista.remove(indice);
                        System.out.println("Thread " + Thread.currentThread().threadId() + " removeu o " + elementoRemovido);
                    } catch (Exception e) {
                        System.err.println("Thread " + Thread.currentThread().threadId() + " não conseguiu remover o elemento de índice " + indice);
                    }
                }
            });
        }

        produtores.shutdown();
        consumidores.shutdown();
    }
}
