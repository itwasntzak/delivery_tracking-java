import java.util.ArrayList;
import java.util.Calendar;
import java.io.File;
import java.io.WriteAbortedException;

public class ExtraStop {
    int id;
    Shift shift;
    Delivery delivery;
    Calendar startTime = Calendar.getInstance();
    Calendar endTime = Calendar.getInstance();
    String location;
    String reason;
    double milesTraveled;

    // list of all local files
    File locationFile;
    File reasonFile;
    File milesFile;
    File startTimeFile;
    File endTimeFile;
    File extraStopIdFile;
    File extraStopIdsFile;
    File infoFile;

    File startIndicator;

    // constructor for new extra stop taken while on a delivery
    public ExtraStop(Delivery delivery) {
        this.delivery = delivery;
        this.locationFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_location.txt");
        this.reasonFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_reason.txt");
        this.milesFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_miles.txt");
        this.startTimeFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_start_time.txt");
        this.endTimeFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_end_time.txt");
        this.extraStopIdFile = new File(delivery.shift.directory.getAbsolutePath(), "extra_stop_id_number.txt");
        this.extraStopIdsFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_ids.txt");
        if (!this.extraStopIdFile.exists()) { this.id = 0; }
        else { this.id = Integer.parseInt(Utility.readLine(this.extraStopIdFile)) + 1; }
        this.infoFile = new File(delivery.directory.getAbsolutePath(), Integer.toString(this.id) + ".txt");
        this.startIndicator = new File(delivery.directory.getAbsolutePath(), "extra_stop");
    }

    // constructor for new extra stop taken separate from a delivery
    public ExtraStop(Shift shift) {
        this.shift = shift;
        this.locationFile = new File(shift.directory.getAbsolutePath(), "extra_stop_location.txt");
        this.reasonFile = new File(shift.directory.getAbsolutePath(), "extra_stop_reason.txt");
        this.milesFile = new File(shift.directory.getAbsolutePath(), "extra_stop_miles.txt");
        this.startTimeFile = new File(shift.directory.getAbsolutePath(), "extra_stop_start_time.txt");
        this.endTimeFile = new File(shift.directory.getAbsolutePath(), "extra_stop_end_time.txt");
        this.extraStopIdFile = new File(shift.directory.getAbsolutePath(), "extra_stop_id_number.txt");
        this.extraStopIdsFile = new File(shift.directory.getAbsolutePath(), "extra_stop_ids.txt");
        if (!this.extraStopIdFile.exists()) { this.id = 0; }
        else { this.id = Integer.parseInt(Utility.readLine(this.extraStopIdFile)) + 1; }
        this.infoFile = new File(shift.directory.getAbsolutePath(), Integer.toString(this.id) + ".txt");
        this.startIndicator = new File(shift.directory.getAbsolutePath(), "extra_stop");
    }

    // constructor for loading extra stop taken while on a delivery
    public ExtraStop(int id, Delivery delivery) {
        this.id = id;
        this.delivery = delivery;
        this.locationFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_location.txt");
        this.reasonFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_reason.txt");
        this.milesFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_miles.txt");
        this.startTimeFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_start_time.txt");
        this.endTimeFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_end_time.txt");
        this.extraStopIdFile = new File(delivery.shift.directory.getAbsolutePath(), "extra_stop_id_number.txt");
        this.extraStopIdsFile = new File(delivery.directory.getAbsolutePath(), "extra_stop_ids.txt");
        this.infoFile = new File(delivery.directory.getAbsolutePath(), Integer.toString(id) + ".txt");
        this.startIndicator = new File(delivery.directory.getAbsolutePath(), "extra_stop");
    }

    // constructor for loading extra stop taken separate from a delivery
    public ExtraStop(int id, Shift shift) {
        this.id = id;
        this.shift = shift;
        this.locationFile = new File(shift.directory.getAbsolutePath(), "extra_stop_location.txt");
        this.reasonFile = new File(shift.directory.getAbsolutePath(), "extra_stop_reason.txt");
        this.milesFile = new File(shift.directory.getAbsolutePath(), "extra_stop_miles.txt");
        this.startTimeFile = new File(shift.directory.getAbsolutePath(), "extra_stop_start_time.txt");
        this.endTimeFile = new File(shift.directory.getAbsolutePath(), "extra_stop_end_time.txt");
        this.extraStopIdFile = new File(shift.directory.getAbsolutePath(), "extra_stop_id_number.txt");
        this.extraStopIdsFile = new File(shift.directory.getAbsolutePath(), "extra_stop_ids.txt");
        this.infoFile = new File(shift.directory.getAbsolutePath(), Integer.toString(id) + ".txt");
        this.startIndicator = new File(shift.directory.getAbsolutePath(), "extra_stop");
    }

    // method to consolidate data from indavidule files to one file
    public void consolidate() {
        String data;
        try {
            Shift shift = this.shift;
            data = String.format("%s\n%s\n%s\n%s\n%s",
                this.location, this.reason, this.milesTraveled,
                Utility.pyTimeFormat.format(this.startTime.getTime()),
                Utility.pyTimeFormat.format(this.endTime.getTime()));
                Utility.writeData(this.infoFile, data);
        } catch (NullPointerException e) {
            data = String.format("%s\n%s\n%s\n%s",
                this.location, this.reason, this.milesTraveled,
                Utility.pyTimeFormat.format(this.endTime.getTime()));
                Utility.writeData(this.infoFile, data);
        }
        this.locationFile.delete();
        this.reasonFile.delete();
        this.milesFile.delete();
        this.endTimeFile.delete();
        if (this.startTimeFile.exists()) { this.startTimeFile.delete(); }
        updateIdsFile();
        updateIdNumber();
        this.startIndicator.delete();
    }

    // method to load extra stop data after already consolidating to one file
    public void load() {
        ArrayList<String> tempList = Utility.lineScanner(this.infoFile);
        this.location = tempList.get(0);
        this.reason = tempList.get(1);
        this.milesTraveled = Double.parseDouble(tempList.get(2));
        try {
            this.startTime.setTime(Utility.parsePyTime(tempList.get(3)));
            this.endTime.setTime(Utility.parsePyTime(tempList.get(4)));
        } catch (NullPointerException e) {
            this.endTime.setTime(Utility.parsePyTime(tempList.get(3)));
        }
    }

     // method to start entering data for an extra stop
    public void start() {
        Utility.writeData(startIndicator, "");
        try {
            Shift shift = this.shift;
            this.startTime = Calendar.getInstance();
            Utility.writeData(this.startTimeFile, Utility.pyTimeFormat.format(this.startTime.getTime()));
        } catch (NullPointerException e) { ; }
        inputLocation();
        inputReason();
        inputMilesTraveled();
        this.endTime = Calendar.getInstance();
        Utility.writeData(this.endTimeFile, Utility.pyTimeFormat.format(this.endTime.getTime()));
        consolidate();
        String timeTaken;
        try {
            timeTaken = Utility.timeTaken(this.startTime, this.endTime);
            System.out.println("This extra stop was completed in:\t" + timeTaken);
        } catch (NullPointerException e) {
            timeTaken = Utility.timeTaken(this.delivery.startTime, this.endTime);
            System.out.println("This extra stop was completed in:\t" + timeTaken);
        }
    }

    // method to update a file that tells the program how many extra stops and the ids a parent has
    private void updateIdsFile() {
        if (this.extraStopIdsFile.exists()) {
            Utility.appendData(this.extraStopIdsFile, Integer.toString(this.id), ",");
        } else { Utility.writeData(this.extraStopIdsFile, Integer.toString(this.id)); }
    }

    // method that updates a file that keeps track "global" extra stop id
    private void updateIdNumber() {
        Utility.writeData(this.extraStopIdFile, Integer.toString(this.id));
    }

    // methods for inputting data
    private void inputLocation() {
        String prompt = "\nExtra stop location:";
        this.location = Utility.getInput(prompt, "[a-zA-Z]+");
        while (!Utility.userConfirmation(this.location)) {
            this.location = Utility.getInput(prompt, "[a-zA-Z]+");
        }
        Utility.writeData(this.locationFile, this.location);
    }

    private void inputReason() {
        String prompt = "\nReason for extra stop?";
        this.reason = Utility.getInput(prompt, "[a-zA-Z]+");
        while (!Utility.userConfirmation(this.reason)) {
            this.reason = Utility.getInput(prompt, "[a-zA-Z]+");
        }
        Utility.writeData(this.reasonFile, this.reason);
    }

    private void inputMilesTraveled() {
        String prompt = "\nExtra stop miles traveled:\t#.#";
        this.milesTraveled = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation(Double.toString(this.milesTraveled) + " miles")) {
            this.milesTraveled = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.milesFile, Double.toString(this.milesTraveled));
    }

    //  method to load extra stop data while a extra stop is in progress
    public void loadCurrent() {
        try {
            Shift shift = this.shift;
            if (this.startTimeFile.exists()) {
                this.startTime.setTime(Utility.parsePyTime(Utility.readLine(this.startTimeFile)));
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (this.locationFile.exists()) { this.location = Utility.readLine(this.locationFile); }
        if (this.reasonFile.exists()) { this.reason = Utility.readLine(this.reasonFile); }

        if (this.milesFile.exists()) {
            this.milesTraveled = Double.parseDouble(Utility.readLine(this.milesFile));
        }

        if (this.endTimeFile.exists()) {
            this.endTime.setTime(Utility.parsePyTime(Utility.readLine(this.endTimeFile)));
        }
        resume();
    }

    // method to continue entering data for an extra stop
    private void resume() {
        try {
            Shift shift = this.shift;
            if (!this.startTimeFile.exists()) {
                this.startTime = Calendar.getInstance();
                Utility.writeData(
                    this.startTimeFile, Utility.pyTimeFormat.format(this.startTime.getTime()));
            }
        } catch (NullPointerException e) { ; }

        if (!this.locationFile.exists()) { inputLocation();; }
        if (!this.reasonFile.exists()) { inputReason(); }
        if (!this.milesFile.exists()) { inputMilesTraveled(); }

        if (!this.endTimeFile.exists()) {
            this.endTime = Calendar.getInstance();
            Utility.writeData(this.endTimeFile, Utility.pyTimeFormat.format(this.endTime.getTime()));
        }
        consolidate();
        String timeTaken;
        try {
            timeTaken = Utility.timeTaken(this.startTime, this.endTime);
            System.out.println("This extra stop was completed in:\t" + timeTaken);
        } catch (NullPointerException e) {
            timeTaken = Utility.timeTaken(this.delivery.startTime, this.endTime);
            System.out.println("This extra stop was completed in:\t" + timeTaken);
        }
    }

    public void saveChanges() {
        String data;
        try {
            Shift shift = this.shift;
            data = String.format("%s\n%s\n%s\n%s\n%s",
                this.location, this.reason, this.milesTraveled,
                Utility.pyTimeFormat.format(this.startTime),
                Utility.pyTimeFormat.format(this.endTime));
            Utility.writeData(this.infoFile, data);
        } catch (NullPointerException e) {
            data = String.format("%s\n%s\n%s\n%s",
                this.location, this.reason, this.milesTraveled,
                Utility.pyTimeFormat.format(this.endTime));
            Utility.writeData(this.infoFile, data);
        }
    }

}
// todo: need to write function that allows the user to change data