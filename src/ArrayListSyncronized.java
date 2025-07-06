import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArrayListSyncronized<T> {
    private Object[] array;
    private int tamanho = 0;
    private int capacidade = 0;

    public ArrayListSyncronized() {
    }


    public T get(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice fora dos limites: " + indice);
        }
        return (T) array[indice];
    }

    public synchronized void adicionar(T elemento) {
        if (capacidade == 0) {
            capacidade = 1;
            array = new Object[capacidade];
        } else {
            if (tamanho == capacidade) {
                capacidade *= 2;
                Object[] novoArray = new Object[capacidade];
                System.arraycopy(this.array, 0, novoArray, 0, tamanho);
            }
        }

        array[tamanho] = elemento;
        tamanho++;

        if (tamanho == 1) {
            notify();
        }
    }

    public synchronized T remover(int indice) {
        while (tamanho == 0)
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice fora dos limites: " + indice);
        }

        T elementoRemovido = (T) array[indice];
        System.arraycopy(this.array, indice + 1, this.array, indice, tamanho - indice - 1);
        tamanho--;

        return elementoRemovido;
    }

    public int size() {
        return this.tamanho;
    }

}

class Main {

    public static int numeroAleatorioEntre(int min, int max) {

        double f = Math.random() / Math.nextDown(1.0);
        return (int) (min * (1.0 - f) + max * f);
    }

    public static void main(String[] args) {
        ExecutorService produtores = Executors.newSingleThreadExecutor();
        ExecutorService consumidores = Executors.newFixedThreadPool(2);

        ArrayListSyncronized<String> lista = new ArrayListSyncronized<>();

        for (int i = 0; i < 10; i++) {
            produtores.submit(() -> {
                String elemento = "Elemento " + Math.random();
                try {
                    lista.adicionar(elemento);
                    System.out.println("Thread " + Thread.currentThread().threadId() + " adicionou o " + elemento);
                } catch (Exception e) {
                    System.err.println("Thread " + Thread.currentThread().threadId() + " não conseguiu adicionar o " + elemento);
                }
            });

            consumidores.submit(() -> {
                int indice = numeroAleatorioEntre(0, lista.size() - 1);
                try {
                    String elementoRemovido = lista.remover(indice);
                    System.out.println("Thread " + Thread.currentThread().threadId() + " removeu o " + elementoRemovido);
                } catch (Exception e) {
                    System.err.println("Thread " + Thread.currentThread().threadId() + " não conseguiu remover o elemento de índice " + indice);
                }
            });
        }

        produtores.shutdown();
        consumidores.shutdown();
    }
}


