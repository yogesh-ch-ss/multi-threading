import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Solution implements CommandRunner {

    private HashMap<Long, Thread> runningCalculations = new HashMap<>();
    private HashMap<Long, SlowCalculator> calculationObjects = new HashMap<>();

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

        } else if (commandString.equals("finish")) {
            // finish

        } else if (commandString.equals("abort")) {
            // abort

        } else {
            return "Invalid Command";
        }

        return "-";

    }

    private String commandStart(Long n) {
        /*
         * start calculating with input N , by calling
         * SlowCalculator.run on a new thread; imme-
         * diately return the message “started N ”
         */

        SlowCalculator slowCalculator = new SlowCalculator(n);
        Thread thread = new Thread(slowCalculator);
        this.calculationObjects.put(n, slowCalculator);
        this.runningCalculations.put(n, thread);
        thread.start();

        return "started " + n;

    }

    private String commandCancel(Long n) {
        /*
         * immediately cancel the calculation with input N
         * that is currently running or pending with after (do
         * nothing if it already completed or if it was never
         * requested); when it has stopped (which should be
         * within 0.1s) return message “cancelled N ”
         */

        Thread thread = runningCalculations.get(n);
        thread.interrupt();
        runningCalculations.remove(n);

        return "cancelled " + n;

    }

    private String commandRunning() {
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

                // check if the calculations are completed.
                // if so, remove from runningCalculations.

                Iterator<Map.Entry<Long, Thread>> iterator = this.runningCalculations.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<Long, Thread> entry = iterator.next();
                    if (!entry.getValue().isAlive()) {
                        // thread has finished
                        iterator.remove();
                    }
                }

                if (this.runningCalculations.size() == 0) {
                    return "no calculations running";
                }

                String output = this.runningCalculations.size() + " calculations running:";

                for (Long calculation : this.runningCalculations.keySet()) {
                    output += " " + Long.toString(calculation);
                }

                return output;
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

        return null;

    }

    private String commandGet(Long n) {
        /*
         * if the calculation for N is finished, return message
         * “result is M ” where M is the integer result; if
         * the calculation is not yet finished, return message
         * “calculating”. If the calculation was started but
         * already cancelled, return message “cancelled”. If
         * the calculation is scheduled with after but not yet
         * started, return message “waiting”.
         */

        if (runningCalculations.get(n) == null && calculationObjects.get(n).getResult() == -1) {
            // cancelled calculation
            return "cancelled";
        } else if (runningCalculations.get(n) == null && calculationObjects.get(n).getResult() != -1) {
            // calculation done,
            return "result is " + Integer.toString(calculationObjects.get(n).getResult());
        } else if (runningCalculations.get(n).isAlive() && calculationObjects.get(n).getResult() == -1) {
            // calculation not yet finished
            return "calculating";
        } else {
            return null;
        }
    }

    private String commandAfter(Long n, Long m) {
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
         * scheduled after N (or after another that is itself af-
         * ter N, recursively), and which M is itself scheduled
         * after (these can be listed in any order)
         */
        return null;

    }

    private String commandFinish() {
        /*
         * wait for all calculations previously requested by the
         * user (including those scheduled with after) to fin-
         * ish, and then after they are all completed, return
         * message “finished”
         */
        return null;
    }

    private String commandAbort() {
        /*
         * immediately stop all running calculations (and dis-
         * card any scheduled using after), and then when
         * they are stopped (which should be within 0.1s) re-
         * turn message “aborted”
         */
        return null;
    }

}
