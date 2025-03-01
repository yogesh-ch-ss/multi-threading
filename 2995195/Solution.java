import java.util.ArrayList;
import java.util.List;

public class Solution implements CommandRunner {

    private List<Long> runningCalculations = new ArrayList<Long>();

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

        } else if (commandString.equals("running")) {
            // running
            commandrunning();

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
         * start N
         * start calculating with input N , by calling SlowCalculator.run on a new
         * thread; immediately return the message “started N ”
         */

        SlowCalculator slowCalculator = new SlowCalculator(n);
        Thread thread = new Thread(slowCalculator);
        thread.start();

        this.runningCalculations.add(n);

        return "started " + n;

    }

    public String commandCancel(Long n) {
        return null;
    }

    public String commandrunning() {
        if (this.runningCalculations.isEmpty()) {
            return "no calculations running";
        } else {
            String output = this.runningCalculations.size() + " calculations running:";

            for (Long calculation : this.runningCalculations) {
                output += " " + Long.toString(calculation);
            }

            return output;
        }

    }

}
