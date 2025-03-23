public class geradorPseudoaleatorio {
    private long seed;
    private static final long a = 1664525;
    private static final long c = 1013904223;
    private static final long m = (long) Math.pow(2, 32);

    // Constantes para tipo de evento
    private static final int tipo_chegada = 1;
    private static final int tipo_saida = 2;

    // Capacidade máxima da fila
    private static final int K = 10; // Capacidade máxima de 10 clientes

    // Variáveis da simulação
    private static int clientesNaFila = 0;
    private static double tempoAtual = 0.0;
    private static double proximaChegada = 0.0;
    private static double proximaSaida = Double.POSITIVE_INFINITY;
    private static double ultimaMudancaEstado = 0.0;
    private static double[] times = new double[K + 1]; // Array para armazenar tempo em cada estado

    // Parâmetros da simulação
    private static final double MEDIA_TEMPO_CHEGADA = 1.0; // média de 1 cliente por minuto
    private static final double MEDIA_TEMPO_ATENDIMENTO = 0.5; // média de 0.5 minutos por atendimento

    // Estatísticas
    private static int totalClientes = 0;
    private static double tempoTotalEspera = 0.0;

    private static geradorPseudoaleatorio gerador;

    public geradorPseudoaleatorio(long seed) {
        this.seed = seed;
    }

    public double nextRandom() {
        seed = (a * seed + c) % m;
        return (double) seed / m;
    }

    // Gera tempo exponencial usando o método da transformação inversa
    private static double gerarTempoExponencial(double media) {
        return -media * Math.log(1 - gerador.nextRandom());
    }

    // Atualiza o tempo acumulado no estado atual
    private static void atualizarTempoEstado() {
        double tempoNoEstado = tempoAtual - ultimaMudancaEstado;
        times[clientesNaFila] += tempoNoEstado;
        ultimaMudancaEstado = tempoAtual;
    }

    // Retorna o próximo evento da simulação
    private static int NextEvent() {
        if (proximaChegada <= proximaSaida) {
            tempoAtual = proximaChegada;
            return tipo_chegada;
        } else {
            tempoAtual = proximaSaida;
            return tipo_saida;
        }
    }

    // Processa evento de chegada
    private static void CHEGADA(int evento) {
        atualizarTempoEstado(); // Atualiza tempo no estado atual antes da mudança

        // Se a fila não estiver cheia, aceita novo cliente
        if (clientesNaFila < K) {
            // Agenda próxima chegada
            proximaChegada = tempoAtual + gerarTempoExponencial(MEDIA_TEMPO_CHEGADA);

            // Incrementa número de clientes na fila
            clientesNaFila++;
            totalClientes++;

            System.out.printf("Tempo %.2f: CHEGADA - Clientes na fila: %d%n", tempoAtual, clientesNaFila);

            // Se for o único cliente, agenda sua saída
            if (clientesNaFila == 1) {
                proximaSaida = tempoAtual + gerarTempoExponencial(MEDIA_TEMPO_ATENDIMENTO);
            }
        } else {
            // Fila cheia, cliente é rejeitado
            System.out.printf("Tempo %.2f: CHEGADA REJEITADA - Fila cheia%n", tempoAtual);
            proximaChegada = tempoAtual + gerarTempoExponencial(MEDIA_TEMPO_CHEGADA);
        }
    }

    // Processa evento de saída
    private static void SAIDA(int evento) {
        atualizarTempoEstado(); // Atualiza tempo no estado atual antes da mudança

        // Decrementa número de clientes na fila
        clientesNaFila--;

        System.out.printf("Tempo %.2f: SAIDA - Clientes na fila: %d%n", tempoAtual, clientesNaFila);

        // Se ainda houver clientes na fila, agenda próxima saída
        if (clientesNaFila > 0) {
            proximaSaida = tempoAtual + gerarTempoExponencial(MEDIA_TEMPO_ATENDIMENTO);
        } else {
            proximaSaida = Double.POSITIVE_INFINITY; // Não há mais saídas agendadas
        }
    }

    public static void main(String[] args) {
        int count = 10;
        int evento;

        // Inicializa gerador com seed baseada no tempo atual
        gerador = new geradorPseudoaleatorio(System.currentTimeMillis());

        // Agenda primeira chegada
        proximaChegada = gerarTempoExponencial(MEDIA_TEMPO_CHEGADA);

        System.out.println("Iniciando simulação com " + count + " eventos");
        System.out.println("Capacidade máxima da fila: " + K + " clientes");

        while (count > 0) {
            evento = NextEvent();

            if (evento == tipo_chegada) {
                CHEGADA(evento);
            } else if (evento == tipo_saida) {
                SAIDA(evento);
            }

            count--;

            // Mostra progresso a cada 10000 eventos
            if (count % 10000 == 0) {
                System.out.println("\nEventos restantes: " + count);
                System.out.printf("Tempo médio de espera até agora: %.2f minutos%n",
                        totalClientes > 0 ? tempoTotalEspera / totalClientes : 0);
                System.out.println("----------------------------------------");
            }
        }

        // Atualiza o tempo final no último estado
        atualizarTempoEstado();

        // Imprime estatísticas finais
        System.out.println("\nSimulação finalizada!");
        System.out.printf("Tempo total simulado: %.2f minutos%n", tempoAtual);
        System.out.printf("Total de clientes atendidos: %d%n", totalClientes);
        System.out.printf("Tempo médio de espera: %.2f minutos%n",
                totalClientes > 0 ? tempoTotalEspera / totalClientes : 0);

        // Imprime distribuição de probabilidade dos estados
        System.out.println("\nDistribuição de probabilidade dos estados da fila:");
        System.out.println("Estado : Tempo    (Probabilidade)");
        System.out.println("----------------------------------");
        for (int i = 0; i <= K; i++) {
            System.out.printf("%d: %.2f (%.2f%%)\n",
                    i, times[i], (times[i] / tempoAtual) * 100);
        }
    }
}
