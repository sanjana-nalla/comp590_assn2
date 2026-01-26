import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {
    private static final int PHILOSOPHER_COUNT = 5;
    private static final int MEALS_PER_PHILOSOPHER = 3;
    private static final int THINK_TIME_MS = 500; // upper bound for thinking
    private static final int EAT_TIME_MS = 400;   // upper bound for eating

    private final ReentrantLock[] forks;
    private final Semaphore seats;
    private final Random random = new Random();

    public DiningPhilosophers(int philosopherCount) {
        forks = new ReentrantLock[philosopherCount];
        for (int i = 0; i < philosopherCount; i++) {
            // Fair locks reduce the chance of one philosopher repeatedly losing the race.
            forks[i] = new ReentrantLock(true);
        }
        // Butler pattern: at most N-1 philosophers seated concurrently prevents circular wait.
        seats = new Semaphore(philosopherCount - 1, true);
    }

    private static void log(int id, String message) {
        System.out.printf("Philosopher %d: %s%n", id, message);
    }

    private void sleepRandom(int maxMillis) {
        try {
            Thread.sleep(50 + random.nextInt(Math.max(1, maxMillis - 49)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private class Philosopher implements Runnable {
        private final int id;
        private final int leftFork;
        private final int rightFork;

        Philosopher(int id) {
            this.id = id;
            this.leftFork = id;
            this.rightFork = (id + 1) % forks.length;
        }

        @Override
        public void run() {
            for (int meal = 1; meal <= MEALS_PER_PHILOSOPHER && !Thread.currentThread().isInterrupted(); meal++) {
                think(meal);
                dine(meal);
            }
            log(id, "done for the day");
        }

        private void think(int meal) {
            log(id, "thinking before meal " + meal);
            sleepRandom(THINK_TIME_MS);
        }

        private void dine(int meal) {
            seats.acquireUninterruptibly();
            try {
                // Total order on fork acquisition eliminates circular wait.
                int first = Math.min(leftFork, rightFork);
                int second = Math.max(leftFork, rightFork);

                forks[first].lock();
                log(id, "picked up fork " + first);
                forks[second].lock();
                log(id, "picked up fork " + second + "; eating meal " + meal);

                sleepRandom(EAT_TIME_MS);
            } finally {
                forks[leftFork].unlock();
                forks[rightFork].unlock();
                seats.release();
                log(id, "finished meal " + meal + "; released forks");
            }
        }
    }

    public void startDinner() {
        Thread[] philosophers = new Thread[PHILOSOPHER_COUNT];
        for (int i = 0; i < PHILOSOPHER_COUNT; i++) {
            philosophers[i] = new Thread(new Philosopher(i), "Philosopher-" + i);
            philosophers[i].start();
        }
        for (Thread t : philosophers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        int count = PHILOSOPHER_COUNT;
        if (args.length == 1) {
            try {
                count = Math.max(2, Integer.parseInt(args[0]));
            } catch (NumberFormatException ignored) {
                System.err.println("Invalid philosopher count, defaulting to " + PHILOSOPHER_COUNT);
            }
        }
        DiningPhilosophers table = new DiningPhilosophers(count);
        table.startDinner();
    }
}
