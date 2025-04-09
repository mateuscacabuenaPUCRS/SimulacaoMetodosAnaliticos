public class Tandem {
    private long seed;
    private static final long a = 1664525;
    private static final long c = 1013904223;
    private static final long m = (long) Math.pow(2, 32);

    private static final int CHEGADA_EXTERNA_F1 = 1;
    private static final int SAIDA_F1 = 2;
    private static final int SAIDA_F2 = 3;

    private static double tempoAtual = 0.0;
    private static double proximaChegadaExterna = 1.5;
    private static long numerosAleatoriosUsados = 0;
    private static final long MAX_ALEATORIOS = 100000;

    private static Tandem gerador;

    private static Fila fila1;
    private static Fila fila2;

    public Tandem(long seed) {
        this.seed = seed;
    }

    public double nextRandom() {
        if (numerosAleatoriosUsados >= MAX_ALEATORIOS) {
            return -1.0;
        }
        numerosAleatoriosUsados++;
        seed = (a * seed + c) % m;
        return (double) seed / m;
    }

    private static double gerarTempoUniforme(double min, double max) {
        double randomNum = gerador.nextRandom();
        if (randomNum < 0)
            return Double.POSITIVE_INFINITY;
        return min + (max - min) * randomNum;
    }


    private static int NextEvent() {
        double tempoProxChegadaExt = proximaChegadaExterna;
        double tempoProxSaidaF1 = fila1.getNextServiceEndTime();
        double tempoProxSaidaF2 = fila2.getNextServiceEndTime();

        double minTempo = tempoProxChegadaExt;
        int tipoEvento = CHEGADA_EXTERNA_F1;

        if (tempoProxSaidaF1 < minTempo) {
            minTempo = tempoProxSaidaF1;
            tipoEvento = SAIDA_F1;
        }
        if (tempoProxSaidaF2 < minTempo) {
            minTempo = tempoProxSaidaF2;
            tipoEvento = SAIDA_F2;
        }

        if (minTempo == Double.POSITIVE_INFINITY) {
            return -1;
        }

        tempoAtual = minTempo;
        return tipoEvento;
    }

    private static void processarChegadaExternaF1() {
        proximaChegadaExterna = tempoAtual + gerarTempoUniforme(fila1.minArrival, fila1.maxArrival);

        if (fila1.status() < fila1.capacity()) {
            fila1.in(); // Cliente entra na fila 1
            System.out.printf("Tempo %.4f: CHEGADA_F1 - Clientes F1: %d | Servidores Ocupados F1: %d%n",
                    tempoAtual, fila1.status(), fila1.serversBusy);

            if (fila1.serversBusy < fila1.servers()) {
                int servidorOcioso = fila1.findIdleServer();
                if (servidorOcioso != -1) {
                    fila1.serversBusy++;
                    double tempoServico = gerarTempoUniforme(fila1.minService, fila1.maxService);
                    fila1.nextServiceEnd[servidorOcioso] = tempoAtual + tempoServico;
                    System.out.printf("            -> Iniciou serviço F1 (Servidor %d) - Término: %.4f%n",
                            servidorOcioso, fila1.nextServiceEnd[servidorOcioso]);
                } else {
                    System.out.println("WARN: Inconsistência em servidor ocioso F1!");
                }
            }
        } else {
            fila1.loss();
            System.out.printf("Tempo %.4f: PERDA_F1 - Fila 1 cheia (Clientes: %d)%n",
                    tempoAtual, fila1.status());
        }
    }

    private static void processarSaidaF1() {
        int servidorQueTerminou = fila1.findServerFinishingAt(tempoAtual);
        if (servidorQueTerminou == -1) {
            System.out.println("WARN: Evento SAIDA_F1 sem servidor correspondente!");
            return;
        }

        fila1.out();
        fila1.serversBusy--;
        fila1.nextServiceEnd[servidorQueTerminou] = Double.POSITIVE_INFINITY;
        System.out.printf("Tempo %.4f: SAIDA_F1 (Servidor %d) - Clientes F1: %d | Servidores Ocupados F1: %d%n",
                tempoAtual, servidorQueTerminou, fila1.status(), fila1.serversBusy);

        if (fila1.status() >= fila1.servers()) {
            fila1.serversBusy++;
            double tempoServico = gerarTempoUniforme(fila1.minService, fila1.maxService);
            fila1.nextServiceEnd[servidorQueTerminou] = tempoAtual + tempoServico;
            System.out.printf("            -> Iniciou serviço F1 (Servidor %d) - Término: %.4f%n",
                    servidorQueTerminou, fila1.nextServiceEnd[servidorQueTerminou]);
        }

        if (fila2.status() < fila2.capacity()) {
            fila2.in();
            System.out.printf("            -> CHEGADA_F2 - Clientes F2: %d | Servidores Ocupados F2: %d%n",
                    fila2.status(), fila2.serversBusy);

            if (fila2.serversBusy < fila2.servers()) {
                int servidorOciosoF2 = fila2.findIdleServer();
                if (servidorOciosoF2 != -1) {
                    fila2.serversBusy++;
                    double tempoServicoF2 = gerarTempoUniforme(fila2.minService, fila2.maxService);
                    fila2.nextServiceEnd[servidorOciosoF2] = tempoAtual + tempoServicoF2;
                    System.out.printf("                -> Iniciou serviço F2 (Servidor %d) - Término: %.4f%n",
                            servidorOciosoF2, fila2.nextServiceEnd[servidorOciosoF2]);
                } else {
                    System.out.println("WARN: Inconsistência em servidor ocioso F2!");
                }
            }
        } else {
            fila2.loss();
            System.out.printf("            -> PERDA_F2 - Fila 2 cheia (Clientes: %d)%n",
                    fila2.status());
        }
    }

    private static void processarSaidaF2() {
        int servidorQueTerminou = fila2.findServerFinishingAt(tempoAtual);
        if (servidorQueTerminou == -1) {
            System.out.println("WARN: Evento SAIDA_F2 sem servidor correspondente!");
            return;
        }

        fila2.out();
        fila2.serversBusy--;
        fila2.nextServiceEnd[servidorQueTerminou] = Double.POSITIVE_INFINITY;
        System.out.printf("Tempo %.4f: SAIDA_F2 (Servidor %d) - Clientes F2: %d | Servidores Ocupados F2: %d%n",
                tempoAtual, servidorQueTerminou, fila2.status(), fila2.serversBusy);

        if (fila2.status() >= fila2.servers()) {
            fila2.serversBusy++;
            double tempoServico = gerarTempoUniforme(fila2.minService, fila2.maxService);
            fila2.nextServiceEnd[servidorQueTerminou] = tempoAtual + tempoServico;
            System.out.printf("            -> Iniciou serviço F2 (Servidor %d) - Término: %.4f%n",
                    servidorQueTerminou, fila2.nextServiceEnd[servidorQueTerminou]);
        }
    }

    private static void inicializarSimulacaoTandem(long seedValue) {
        tempoAtual = 0.0;
        proximaChegadaExterna = 1.5; // Conforme especificado
        numerosAleatoriosUsados = 0;

        gerador = new Tandem(seedValue);

        fila1 = new Fila(2, 3, 1.0, 4.0, 3.0, 4.0);

        fila2 = new Fila(1, 5, 0, 0, 2.0, 3.0); // min/max Arrival não usados aqui

        fila1.lastStateChangeTime = 0.0;
        fila2.lastStateChangeTime = 0.0;
    }

    private static void simularTandem() {
        System.out.println("--- Iniciando Simulação Tandem ---");
        System.out.printf("Fila 1: G/G/%d/%d | Chegada Externa [%.1f..%.1f] | Serviço [%.1f..%.1f]%n",
                fila1.servers(), fila1.capacity(), fila1.minArrival, fila1.maxArrival, fila1.minService,
                fila1.maxService);
        System.out.printf("Fila 2: G/G/%d/%d | Chegada Interna (Saída F1) | Serviço [%.1f..%.1f]%n",
                fila2.servers(), fila2.capacity(), fila2.minService, fila2.maxService);
        System.out.println("Condição de parada: " + MAX_ALEATORIOS + " números aleatórios usados.");
        System.out.println("Primeira chegada externa em t = " + proximaChegadaExterna);
        System.out.println("----------------------------------------");

        // Loop principal da simulação
        while (numerosAleatoriosUsados < MAX_ALEATORIOS) {
            int tipoEvento = NextEvent();

            if (tipoEvento == -1) { 
                System.out.println(
                        "WARN: Nenhum evento futuro agendado ou limite de aleatórios atingido. Saindo do loop.");
                break;
            }

            fila1.updateStateTimes(tempoAtual);
            fila2.updateStateTimes(tempoAtual);

            switch (tipoEvento) {
                case CHEGADA_EXTERNA_F1:
                    processarChegadaExternaF1();
                    break;
                case SAIDA_F1:
                    processarSaidaF1();
                    break;
                case SAIDA_F2:
                    processarSaidaF2();
                    break;
                default:
                    System.out.println("ERRO: Evento desconhecido: " + tipoEvento);
                    break;
            }
            if (numerosAleatoriosUsados >= MAX_ALEATORIOS) {
                System.out.println("INFO: Limite de " + MAX_ALEATORIOS + " números aleatórios atingido.");
                break;
            }
        }

        fila1.updateStateTimes(tempoAtual);
        fila2.updateStateTimes(tempoAtual);

        imprimirResultadosTandem();
    }

    private static void imprimirResultadosTandem() {
        System.out.println("--- Resultados da Simulação Tandem ---");
        System.out.printf("Tempo total simulado: %.4f%n", tempoAtual);
        System.out.printf("Números aleatórios usados: %d%n", numerosAleatoriosUsados);

        System.out.println("--- Fila 1 --- (G/G/" + fila1.servers() + "/" + fila1.capacity() + ")");
        System.out.printf("Clientes perdidos (Fila 1 cheia): %d%n", fila1.loss);
        System.out.println("Distribuição de Estados (Fila 1):");
        System.out.println("Estado | Tempo Acumulado | Probabilidade (%)");
        System.out.println("-------|-----------------|------------------");
        for (int i = 0; i <= fila1.capacity(); i++) {
            double prob = (tempoAtual > 0) ? (fila1.times[i] / tempoAtual) * 100 : 0;
            System.out.printf("  %d    | %.4f        | %.2f%%%n", i, fila1.times[i], prob);
        }

        System.out.println("--- Fila 2 --- (G/G/" + fila2.servers() + "/" + fila2.capacity() + ")");
        System.out.printf("Clientes perdidos (Fila 2 cheia): %d%n", fila2.loss);
        System.out.println("Distribuição de Estados (Fila 2):");
        System.out.println("Estado | Tempo Acumulado | Probabilidade (%)");
        System.out.println("-------|-----------------|------------------");
        for (int i = 0; i <= fila2.capacity(); i++) {
            double prob = (tempoAtual > 0) ? (fila2.times[i] / tempoAtual) * 100 : 0;
            System.out.printf("  %d    | %.4f        | %.2f%%%n", i, fila2.times[i], prob);
        }
        System.out.println("----------------------------------------");
    }

    public static void main(String[] args) {
        inicializarSimulacaoTandem(System.currentTimeMillis());
        simularTandem();
    }

}
