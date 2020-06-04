import java.util.Calendar;

public class DeliveryTracking {

    public static void main(String[] args) {
        Calendar today = Calendar.getInstance();
        Shift shift = new Shift(Shift.idFormat.format(today.getTime()));
        if (!Shift.shiftsDirectory.exists()) { Shift.shiftsDirectory.mkdir(); }
        if (shift.shiftInfoFile.exists()) { shift.completed(); }
        if (!shift.directory.exists()) { shift.start(); } else { shift.loadCurrent(); shift.resume(); }
        if (shift.split.startTimeFile.exists()) { shift.split.end(); } else { menu(shift); }
    }

    private static void menu(Shift shift) {
        String prompt = "\nWhat would you like to do?\n" +
            "D: Start delivery\n" +
            "E: Start an extra stop\n" +
            "C: Enter carry out tip\n" +
            "S: Start split\n" +
            "X: End shift\n" +
            "I: Information on shift\n" +
            "Q: Quit program";
        String pattern = "[dDeEcCsSxXiIqQ]{1,1}";
        String userChoice = Utility.getInput(prompt, pattern);
        while (userChoice.matches(pattern)) {
            if (userChoice.matches("[dD]{1,1}")) { shift.delivery(); }
            else if (userChoice.matches("[eE]{1,1}")) { shift.extraStop(); }
            else if (userChoice.matches("[cC]{1,1}")) { shift.carryOutTip(); }
            else if (userChoice.matches("[sS]{1,1}")) { shift.split.start(); }
            else if (userChoice.matches("[xX]{1,1}")) { shift.end(); }
            // todo: need to rewrite function to view shift statistics/info
            else if (userChoice.matches("[iI]{1,1}")) { ; }
            else if (userChoice.matches("[qQ]{1,1}")) { System.exit(0); }
            userChoice = Utility.getInput(prompt, pattern);
        }
    }

}
// cnsd: adding a method in all classes that return the py time string of times