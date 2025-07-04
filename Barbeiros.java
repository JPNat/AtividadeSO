import java.util.concurrent.ThreadLocalRandom;




// O que devemos fazer?


// Criar a classes Barbeiros

    // ele pega um cliente da fila
    // corta o cabelo em um tempo aleatório de 5s a 10s
    // exclui esse cliente

// Criar pool

    // número RunnableTask: 2
    // número máximo da fila: 10
    


public class Barbeiros implements Runnable{
    

    public void getCliente(){

        int clienteAtendido = filaClientes.poll();

        System.out.println("Atendendo: " + clienteAtendido);

        try {

            int time = ThreadLocalRandom.current().nextInt(5, 10);
            Thread.sleep(time * 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    
    }

    
    private int num_barbeiro;

    public Barbeiros(int num_barbeiro){

        this.num_barbeiro = num_barbeiro;
    }

    @Override
    public void run() {

        getCliente;
        System.out.println("Barbeiro" + Thread.currentThread().getName());

    }

}   

