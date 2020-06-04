import java.util.ArrayList;
import java.util.Calendar;
import java.io.File;

public class Split {
    Calendar startTime = Calendar.getInstance();
    Calendar endTime = Calendar.getInstance();
    double milesTraveled;

    // list of all local files
    File milesFile;
    File startTimeFile;
    File endTimeFile;
    File infoFile;

    public Split(File parentDirectory) {
        this.milesFile = new File(parentDirectory.getAbsolutePath(), "split_miles_traveled.txt");
        this.startTimeFile = new File(parentDirectory.getAbsolutePath(), "split_start_time.txt");
        this.endTimeFile = new File(parentDirectory.getAbsolutePath(), "split_end_time.txt");
        this.infoFile = new File(parentDirectory.getAbsolutePath(), "split_info.txt");
    }

    private void consolidate() {
        String data = String.format("%s,%s,%s",
            this.milesTraveled,
            Utility.pyTimeFormat.format(this.startTime.getTime()),
            Utility.pyTimeFormat.format(this.endTime.getTime()));
        Utility.writeData(this.infoFile, data);
        this.milesFile.delete();
        this.startTimeFile.delete();
        this.endTimeFile.delete();
    }

    public void end() {
        String prompt = "Are you sure you want to complete this split?\t[Y/N]";
        String userResponse = Utility.getInput(prompt, "[yYnN]{1,1}");
        if (userResponse.matches("[yY]{1,1}")) {
            this.startTime.setTime(Utility.parsePyTime(Utility.readLine(this.startTimeFile)));
            if (this.milesFile.exists()) {
                this.milesTraveled = Double.parseDouble(Utility.readLine(this.milesFile));
            } else { inputMilesTraveled(); }
            if (this.endTimeFile.exists()) {
                this.endTime.setTime(Utility.parsePyTime(Utility.readLine(this.endTimeFile)));
            } else {
                this.endTime = Calendar.getInstance();
                Utility.writeData(this.endTimeFile, Utility.pyTimeFormat.format(this.endTime.getTime()));
            }
            consolidate();
        }
    }

    public void load() {
        ArrayList<String> tempList = Utility.commaScanner(this.infoFile);
        this.milesTraveled = Double.parseDouble(tempList.get(0));
        this.startTime.setTime(Utility.parsePyTime(tempList.get(1)));
        this.endTime.setTime(Utility.parsePyTime(tempList.get(2)));
    }

    public void start() {
        String prompt = "\nAre you sure you want to start a split?\nY: yes\nN: no";
        String userResponse = Utility.getInput(prompt, "[yYnN]{1,1}");
        if (userResponse.matches("[yY]{1,1}")) {
            this.startTime = Calendar.getInstance();
            Utility.writeData(
                this.startTimeFile,
                Utility.pyTimeFormat.format(this.startTime.getTime()));
            System.out.println("\nSplit has been started!");
            Utility.enterToContinue();
            System.exit(0);
        } else { ; }
    }

    private void inputMilesTraveled() {
        String prompt = "\nTotal miles traveled for split:\t#.#";
        this.milesTraveled = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation(Double.toString(this.milesTraveled) + " miles")) {
            this.milesTraveled = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.milesFile, Double.toString(this.milesTraveled));
    }

    public void saveChanges() {
        String data = String.format("%s,%s,%s",
            this.milesTraveled,
            Utility.pyTimeFormat.format(this.startTime),
            Utility.pyTimeFormat.format(this.endTime));
        Utility.writeData(this.infoFile, data);
    }

}