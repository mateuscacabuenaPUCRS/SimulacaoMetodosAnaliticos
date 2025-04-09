public class Fila {
    int server;
    int capacity;
    double minArrival;
    double maxArrival;
    double minService;
    double maxService;
    int customers;
    int serversBusy;
    int loss;
    double[] times;
    double[] nextServiceEnd;
    double lastStateChangeTime;

    public Fila(int server, int capacity, double minArrival, double maxArrival, double minService, double maxService) {
        this.server = server;
        this.capacity = capacity;
        this.minArrival = minArrival;
        this.maxArrival = maxArrival;
        this.minService = minService;
        this.maxService = maxService;
        this.customers = 0;
        this.serversBusy = 0;
        this.loss = 0;
        this.times = new double[capacity + 1];
        this.nextServiceEnd = new double[server];
        for (int i = 0; i < server; i++) {
            this.nextServiceEnd[i] = Double.POSITIVE_INFINITY;
        }
        this.lastStateChangeTime = 0.0;
        for (int i = 0; i <= capacity; i++) {
            this.times[i] = 0.0;
        }
    }

    public void updateStateTimes(double currentTime) {
        double timeInState = currentTime - this.lastStateChangeTime;
        if (this.customers >= 0 && this.customers <= this.capacity) {
            this.times[this.customers] += timeInState;
        }
        this.lastStateChangeTime = currentTime;
    }

    public double getNextServiceEndTime() {
        double minTime = Double.POSITIVE_INFINITY;
        for (double endTime : nextServiceEnd) {
            if (endTime < minTime) {
                minTime = endTime;
            }
        }
        return minTime;
    }

    public int findIdleServer() {
        for (int i = 0; i < server; i++) {
            if (nextServiceEnd[i] == Double.POSITIVE_INFINITY) {
                return i;
            }
        }
        return -1;
    }

    public int findServerFinishingAt(double time) {
        for (int i = 0; i < server; i++) {
            if (Math.abs(nextServiceEnd[i] - time) < 1e-9) {
                return i;
            }
        }
        return -1;
    }

    public int status() {
        return this.customers;
    }

    public int capacity() {
        return this.capacity;
    }

    public int servers() {
        return this.server;
    }

    public void loss() {
        this.loss++;
    }

    public void in() {
        this.customers++;
    }

    public void out() {
        this.customers--;
    }
}