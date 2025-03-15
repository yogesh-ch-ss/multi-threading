import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution implements CommandRunner {

    private HashMap<Long, Thread> runningCalculations = new HashMap<>();
    private HashMap<Long, SlowCalculator> calculationObjects = new HashMap<>();
    private Set<Long> finishedCalculations = new HashSet<>();
    private Set<Long> cancelledCalculations = new HashSet<>();
    private HashMap<Long, List<Long>> afterHashMap = new HashMap<>();

    public Solution() {
        // Empty constructor.
        // Your Solution class should have a public constructor taking no parameters.
    }

    @Override
    public String runCommand(String command) {
        /*
         * The runCommand method of this class will be passed a string, corresponding to
         * a command the user entered.
         * It should perform the relevant command (see below), and return the specified
         * output string.
         */

        String[] commandFull = command.split(" ");
        String commandString = commandFull[0];

        if (commandString.equals("start")) {
            // start N
            Long n = Long.parseLong(commandFull[1]);
            return commandStart(n);

        } else if (commandString.equals("cancel")) {
            // cancel N
            Long n = Long.parseLong(commandFull[1]);
            return commandCancel(n);

        } else if (commandString.equals("running")) {
            // running
            return commandRunning();

        } else if (commandString.equals("get")) {
            // get N
            Long n = Long.parseLong(commandFull[1]);
            return commandGet(n);

        } else if (commandString.equals("after")) {
            // after N M
            Long n = Long.parseLong(commandFull[1]);
            Long m = Long.parseLong(commandFull[2]);
            return commandAfter(n, m);

        } else if (commandString.equals("finish")) {
            // finish
            return commandFinish();

        } else if (commandString.equals("abort")) {
            // abort
            return commandAbort();

        } else {
            return "Invalid Command";
        }

    }

    private synchronized String commandStart(Long n) {
        /*
         * start calculating with input N , by calling
         * SlowCalculator.run on a new thread; imme-
         * diately return the message “started N ”
         */

        // do not start if the same calucation is already running or completed
        if (this.runningCalculations.containsKey(n)) {
            return "calculation is already running";
        }
        if (this.finishedCalculations.contains(n)) {
            return "calculation is already finished";
        }

        SlowCalculator slowCalculator = new SlowCalculator(n);

        // defining the thread and what it does
        Thread thread = new Thread(() -> {
            slowCalculator.run();
            // once the thread finishes, it calls finishThread(n),
            // which starts the "after n m"
            this.finshThread(n);
        });

        this.runningCalculations.put(n, thread);
        this.calculationObjects.put(n, slowCalculator);

        thread.start();

        return "started " + n;

    }

    private synchronized String commandCancel(Long n) {
        /*
         * immediately cancel the calculation with input N
         * that is currently running or pending with after (do
         * nothing if it already completed or if it was never
         * requested); when it has stopped (which should be
         * within 0.1s) return message “cancelled N ”
         */

        // cancelling a non running calculation
        if (!this.runningCalculations.containsKey(n)) {
            // scan if n is pending in after hashmap
            for (List<Long> afterValues : this.afterHashMap.values()) {
                if (afterValues.contains(n)) {
                    afterValues.remove(n);
                }
            }

            if (this.afterHashMap.containsKey(n)) {
                // start threads after n even if n is not running currently
                this.startThreadsAfterN(n);
                this.cancelledCalculations.add(n);
                return "removed from after";
            }

            //
            return "cancelled non-existent calculation";
        }

        Thread thread = this.runningCalculations.get(n);
        thread.interrupt();

        try {
            thread.join(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        this.runningCalculations.remove(n);
        this.cancelledCalculations.add(n);

        // starts the threads that must start after n
        this.startThreadsAfterN(n);

        return "cancelled " + n;

    }

    private synchronized String commandRunning() {
        /*
         * return a message indicating the total number
         * of calculations currently running (i.e. exclud-
         * ing those already completed/cancelled), and
         * their inputs N (in any order), in the form
         * “3 calculations running: 83476 1000 176544”.
         * If no calculations are running, return the string “no
         * calculations running”.
         */

        try {
            if (this.runningCalculations.isEmpty()) {
                return "no calculations running";
            } else {

                if (this.runningCalculations.size() == 0) {
                    return "no calculations running";
                }

                String output = this.runningCalculations.size() + " calculations running:";
                for (Long calculation : this.runningCalculations.keySet()) {
                    output += " " + calculation;
                }

                return output;
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

        return null;

    }

    private synchronized String commandGet(Long n) {
        /*
         * if the calculation for N is finished, return message
         * “result is M ” where M is the integer result; if
         * the calculation is not yet finished, return message
         * “calculating”. If the calculation was started but
         * already cancelled, return message “cancelled”. If
         * the calculation is scheduled with after but not yet
         * started, return message “waiting”.
         */

        if (this.finishedCalculations.contains(n)) {
            // calculation done
            return "result is " + calculationObjects.get(n).getResult();
        } else if (this.cancelledCalculations.contains(n)) {
            // cancelled calculation
            return "cancelled";
        } else if (this.runningCalculations.containsKey(n) && this.runningCalculations.get(n).isAlive()) {
            // calculation not yet finished
            return "calculating";
        } else {
            // scheduled with after, but not yet started
            return "waiting";
        }
    }

    private synchronized String commandAfter(Long n, Long m) {
        /*
         * schedule the calculation for M to start when that for
         * N finishes (or is cancelled). Return the message “M
         * will start after N ” immediately (without wait-
         * ing for either calculation). The calculation for M
         * should not appear in running until it is actually
         * running (i.e. N has completed). If N has already
         * finished, M should start immediately. If a circular
         * dependency would arise (i.e. two calculations wait-
         * ing for each other, hence neither would start), then
         * after should not schedule M , but instead return the
         * message “circular dependency N... M ” where
         * ... is replaced by the numbers of all calculations
         * scheduled after M (or after another that is itself af-
         * ter M, recursively), and which N is itself scheduled
         * after (these can be listed in any order)
         */

        // circular dependency
        if (this.isCircular(n, m)) {
            String circularOutputString = "circular dependency";
            List<Long> circularPath = this.getCircularPath(n, m);

            for (Long p : circularPath) {
                circularOutputString += " " + p;
            }
            return circularOutputString;
        }

        if (this.finishedCalculations.contains(n) || this.cancelledCalculations.contains(n)) {
            // if N has already finished or cancelled, M should start immediately.
            return commandStart(m);
        }

        {
            // if N is still running, M should start after N
            afterHashMap.computeIfAbsent(n, k -> new ArrayList<>()).add(m);
            // ...
            return m + " will start after " + n;
        }

    }

    private synchronized String commandFinish() {
        /*
         * wait for all calculations previously requested by the
         * user (including those scheduled with after) to fin-
         * ish, and then after they are all completed, return
         * message “finished”
         */

        while (!this.runningCalculations.isEmpty() || !this.afterHashMap.isEmpty()) {
            try {
                wait(5000);
                System.out.println("...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return "finished";
    }

    private synchronized String commandAbort() {
        /*
         * immediately stop all running calculations (and dis-
         * card any scheduled using after), and then when
         * they are stopped (which should be within 0.1s) re-
         * turn message “aborted”
         */

        for (Thread thread : this.runningCalculations.values()) {
            thread.interrupt();
            try {
                thread.join(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.runningCalculations.clear();
        this.afterHashMap.clear();
        return "aborted";
    }

    // HELPER METHODS

    private void finshThread(Long n) {
        /*
         * after finishing the thread,
         * it removes the thread from runningCalculations
         * initiates the threads that need to start after n
         */
        this.runningCalculations.remove(n);
        this.finishedCalculations.add(n);
        this.startThreadsAfterN(n);
    }

    private void startThreadsAfterN(Long n) {
        if (this.afterHashMap.containsKey(n)) {
            List<Long> nextThreads = this.afterHashMap.get(n);
            this.afterHashMap.remove(n);
            for (Long m : nextThreads) {
                commandStart(m);
            }
        }
    }

    private boolean isCircular(Long n, long m) {
        Set<Long> visited = new HashSet<>();
        return this.isCircularHelper(n, m, visited);
    }

    private boolean isCircularHelper(Long n, Long m, Set<Long> visited) {
        // implements dfs

        if (this.afterHashMap.containsKey(m) && this.afterHashMap.get(m).contains(n)) {
            // if m is in the hash map and the value of n is present on the key m,
            // it is cyclic dependent
            return true;
        }

        if (!this.afterHashMap.containsKey(m) || visited.contains(m)) {
            // if m is not in the hash map or if m has been visited already
            return false;
        }

        visited.add(m);

        for (Long v : this.afterHashMap.get(m)) {
            // Check by dfs by traversing to every value v in the key of m
            if (this.isCircularHelper(n, v, visited)) {
                return true;
            }
        }

        return false;
    }

    private List<Long> getCircularPath(Long n, Long m) {
        List<Long> path = new ArrayList<>();
        path.add(n);
        this.getCircularPathHelper(n, m, path);
        return path;
    }

    private void getCircularPathHelper(Long n, Long m, List<Long> path) {
        // System.out.println(Arrays.toString(path.toArray()));

        // base condition
        if (n == m) {
            return;
        }

        // find the key of the value n
        Long key = null;
        for (Long k : this.afterHashMap.keySet()) {
            if (this.afterHashMap.get(k).contains(n)) {
                key = k;
                break;
            }
        }

        if (key != null)
            path.add(key);

        this.getCircularPathHelper(key, m, path);
        return;
    }

}
