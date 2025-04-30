// Arquivo: RedeFilas.java
// implementação de uma rede de filas usando simulação de eventos discretos
// Trabalho 1 - Simulação e Métodos Analíticos

import java.util.*;

class Roteamento {
    int destino; // -1 significa saída do sistema
    double probabilidade;

    Roteamento(int destino, double probabilidade) {
        this.destino = destino;
        this.probabilidade = probabilidade;
    }
}

public class RedeFilas {
    private static final long MAX_ALEATORIOS = 100_000;
    private static long usados = 0;
    private static double tempoAtual = 0.0;
    private static double proximaChegadaExterna = 1.5;

    private static long seed;
    private static final long a = 1664525, c = 1013904223, m = (1L << 32);
    private static List<Fila> filas = new ArrayList<>();
    private static Map<Integer, List<Roteamento>> roteamentos = new HashMap<>();

    private static double nextRandom() {
        if (usados++ >= MAX_ALEATORIOS)
            return -1.0;
        seed = (a * seed + c) % m;
        return (double) seed / m;
    }

    private static double uniforme(double min, double max) {
        double r = nextRandom();
        if (r < 0)
            return Double.POSITIVE_INFINITY;
        return min + (max - min) * r;
    }

    private static void chegadaExterna() {
        proximaChegadaExterna = tempoAtual + uniforme(2, 4); // chegada entre 2..4
        Fila f1 = filas.get(0);
        if (f1.podeEntrar()) {
            f1.in();
            if (f1.temServidorDisponivel())
                f1.iniciarServico(tempoAtual, uniforme(1, 2));
        } else {
            f1.perder();
        }
    }

    private static void saida(int indiceFila, int indiceServidor) {
        Fila fila = filas.get(indiceFila);
        fila.finalizarServico(indiceServidor);

        double r = nextRandom();
        for (Roteamento rota : roteamentos.getOrDefault(indiceFila, new ArrayList<>())) {
            if (r <= rota.probabilidade) {
                if (rota.destino == -1)
                    return; // saiu do sistema
                Fila destino = filas.get(rota.destino);
                if (destino.podeEntrar()) {
                    destino.in();
                    if (destino.temServidorDisponivel())
                        destino.iniciarServico(tempoAtual, uniforme(destino.minService, destino.maxService));
                } else {
                    destino.perder();
                }
                break;
            }
            r -= rota.probabilidade;
        }
    }

    private static void simular() {
        while (usados < MAX_ALEATORIOS) {
            int proxFila = -1, proxServidor = -1;
            double menorTempo = proximaChegadaExterna;
            boolean isChegada = true;

            for (int i = 0; i < filas.size(); i++) {
                int servidor = filas.get(i).proximoServidorFinalizando();
                double fim = (servidor != -1) ? filas.get(i).nextServiceEnd[servidor] : Double.POSITIVE_INFINITY;
                if (fim < menorTempo) {
                    menorTempo = fim;
                    proxFila = i;
                    proxServidor = servidor;
                    isChegada = false;
                }
            }

            if (menorTempo == Double.POSITIVE_INFINITY)
                break;
            tempoAtual = menorTempo;

            for (Fila f : filas)
                f.atualizarTempoEstado(tempoAtual);

            if (isChegada) chegadaExterna();
            else saida(proxFila, proxServidor);
        }

        for (Fila f : filas)
            f.atualizarTempoEstado(tempoAtual);
        imprimirResultados();
    }

    private static void imprimirResultados() {
        System.out.printf("\nTempo total de simulacao: %.2f\n\n", tempoAtual);
        for (int i = 0; i < filas.size(); i++) {
            Fila f = filas.get(i);
            System.out.printf("Fila %d (G/G/%d/%d):\n", i + 1, f.servidores, f.capacidade);
            System.out.printf("Perdas: %d\n", f.perdas);
            System.out.println("Estado | Tempo | Probabilidade (%)");
            for (int j = 0; j < f.tempos.length; j++) {
                double p = f.tempos[j] / tempoAtual * 100;
                System.out.printf(" %2d     | %.4f | %.2f%%%n", j, f.tempos[j], p);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        seed = System.currentTimeMillis();

        // cria as filas do sistema
        filas.add(new Fila(1, 10, 2, 4, 1, 2)); // Fila 1 - um servidor, capacidade 10
        filas.add(new Fila(2, 5, 0, 0, 4, 8)); // Fila 2 - dois servidores, capacidade 5
        filas.add(new Fila(2, 10, 0, 0, 5, 15)); // Fila 3 - dois servidores, capacidade 10

        // configura o roteamento entre as filas
        roteamentos.put(0, Arrays.asList(
                new Roteamento(1, 0.5), // 50% vai pra fila 2
                new Roteamento(2, 0.5)) // 50% vai pra fila 3
        );
        roteamentos.put(1, List.of(new Roteamento(-1, 1.0))); // Fila 2 para fora
        roteamentos.put(2, List.of(new Roteamento(-1, 1.0))); // Fila 3 para fora

        simular();
    }
}
