# Dining Philosophers (Java)

Team Members: Sanjana Nalla, Tejaswi Paladugu

## How to run

```bash
javac DiningPhilosophers.java
java DiningPhilosophers            # defaults to 5 philosophers
java DiningPhilosophers 7          # optional: specify philosopher count (>=2)
```

## Design rationale

- **Philosophers**: each philosopher is a `Thread` (see `Philosopher` runnable). A small fixed number of meals (3) keeps the simulation finite and visible.
- **Table/Forks**: forks are an array of fair `ReentrantLock` instances. Fair locks reduce the chance a philosopher repeatedly loses the race to the same neighbor.
- **Butler/Seats**: a fair `Semaphore` with `N-1` permits ("butler" pattern) limits how many philosophers may try to sit at once, breaking one condition for circular wait.
- **Spaghetti/Eating**: eating is simulated with a bounded random sleep once both forks are acquired; log statements show when forks are picked up, meals start, and forks are released.
- **Thinking**: thinking is another bounded random sleep with logs so you can see alternation between thinking and eating phases.

## Deadlock and starvation considerations

- **Deadlock avoidance**:
	- The butler semaphore (`N-1` permits) prevents all philosophers from holding exactly one fork simultaneously.
	- Forks are always acquired in a **total order** (`min` then `max` fork index), eliminating circular wait.
- **Starvation mitigation**:
	- Both the semaphore and fork locks use fairness (`true`), so waiting philosophers are queued FIFO.
	- Bounded work (finite meals, bounded think/eat times) keeps the system moving; a waiting philosopher eventually reaches the front of each queue.
- **Residual risk**: with fair primitives and total ordering, starvation is highly improbable in this finite simulation. In theory, starvation can still occur in unbounded runs on a preemptive scheduler, but the chosen patterns make it effectively negligible for the assignment scenario.

## Judicious output

The program prints each philosopher's actions: thinking, picking up each fork, eating with meal number, and releasing forks. This makes it easy to trace interleavings and verify that forks are always released and reused without deadlock.
