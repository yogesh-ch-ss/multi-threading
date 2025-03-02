import java.util.HashMap;

public class Solution implements CommandRunner {

    private HashMap<Long, Thread> runningCalculations = new HashMap<>();

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
            commandStart(n);

        } else if (commandString.equals("cancel")) {
            // cancel N
            Long n = Long.parseLong(commandFull[1]);
            commandCancel(n);

        } else if (commandString.equals("running")) {
            // running
            commandRunning();

        } else if (commandString.equals("get")) {
            // get N

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

    public String commandStart(Long n) {
        /*
         * start calculating with input N , by calling
         * SlowCalculator.run on a new thread; imme-
         * diately return the message “started N ”
         */

        SlowCalculator slowCalculator = new SlowCalculator(n);
        Thread thread = new Thread(slowCalculator);
        this.runningCalculations.put(n, thread);
        thread.start();

        return "started " + n;

    }

    public String commandCancel(Long n) {
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

    public String commandRunning() {
        /*
         * return a message indicating the total number
         * of calculations currently running (i.e. exclud-
         * ing those already completed/cancelled), and
         * their inputs N (in any order), in the form
         * “3 calculations running: 83476 1000 176544”.
         * If no calculations are running, return the string “no
         * calculations running”.
         */
        if (this.runningCalculations.isEmpty()) {
            return "no calculations running";
        } else {
            String output = this.runningCalculations.size() + " calculations running:";

            for (Long calculation : this.runningCalculations.keySet()) {
                output += " " + Long.toString(calculation);
            }

            return output;
        }

    }

    public String commandGet(Long n) {
        /*
         * if the calculation for N is finished, return message
         * “result is M ” where M is the integer result; if
         * the calculation is not yet finished, return message
         * “calculating”. If the calculation was started but
         * already cancelled, return message “cancelled”. If
         * the calculation is scheduled with after but not yet
         * started, return message “waiting”.
         */
        return null;

    }

    public String commandAfter(Long n, Long m) {
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

    public String commandFinish() {
        /*
         * wait for all calculations previously requested by the
         * user (including those scheduled with after) to fin-
         * ish, and then after they are all completed, return
         * message “finished”
         */
        return null;
    }

    public String commandAbort() {
        /*
         * immediately stop all running calculations (and dis-
         * card any scheduled using after), and then when
         * they are stopped (which should be within 0.1s) re-
         * turn message “aborted”
         */
        return null;
    }

}
