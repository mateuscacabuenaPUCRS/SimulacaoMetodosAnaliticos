// Arquivo: Fila.java

public class Fila {
    int servidores;
    int capacidade;
    double minArrival, maxArrival;
    double minService, maxService;
    int clientes;
    int ocupados;
    int perdas;
    double[] tempos;
    double[] nextServiceEnd;
    double ultimoTempo;

    public Fila(int servidores, int capacidade, double minArrival, double maxArrival, double minService,
            double maxService) {
        this.servidores = servidores;
        this.capacidade = capacidade;
        this.minArrival = minArrival;
        this.maxArrival = maxArrival;
        this.minService = minService;
        this.maxService = maxService;
        this.clientes = 0;
        this.ocupados = 0;
        this.perdas = 0;
        int maxSize = Math.min(servidores + capacidade + 1, 1000);
        this.tempos = new double[maxSize];
        this.nextServiceEnd = new double[servidores];
        for (int i = 0; i < servidores; i++)
            nextServiceEnd[i] = Double.POSITIVE_INFINITY;
        this.ultimoTempo = 0.0;
    }

    public boolean podeEntrar() {
        return (clientes < servidores + capacidade);
    }

    public void in() {
        clientes++;
    }

    public void perder() {
        perdas++;
    }

    public boolean temServidorDisponivel() {
        return ocupados < servidores;
    }

    public void iniciarServico(double tempoAtual, double duracao) {
        for (int i = 0; i < servidores; i++) {
            if (nextServiceEnd[i] == Double.POSITIVE_INFINITY) {
                nextServiceEnd[i] = tempoAtual + duracao;
                ocupados++;
                break;
            }
        }
    }

    public int proximoServidorFinalizando() {
        double menor = Double.POSITIVE_INFINITY;
        int indice = -1;
        for (int i = 0; i < servidores; i++) {
            if (nextServiceEnd[i] < menor) {
                menor = nextServiceEnd[i];
                indice = i;
            }
        }
        return indice;
    }

    public void finalizarServico(int servidor) {
        clientes--;
        ocupados--;
        nextServiceEnd[servidor] = Double.POSITIVE_INFINITY;
        if (clientes >= servidores)
            iniciarServicoDireto(servidor);
    }

    private void iniciarServicoDireto(int servidor) {
        nextServiceEnd[servidor] = Double.POSITIVE_INFINITY;
        ocupados++;
    }

    public void atualizarTempoEstado(double tempoAtual) {
        int estado = clientes;
        double delta = tempoAtual - ultimoTempo;
        if (estado < tempos.length)
            tempos[estado] += delta;
        ultimoTempo = tempoAtual;
    }
}
