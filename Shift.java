import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class Shift {
    final static SimpleDateFormat idFormat = new SimpleDateFormat("yyyy-MM-dd");

    // initialize attributes for this class
    Calendar id = Calendar.getInstance();
    Calendar startTime = Calendar.getInstance();
    Calendar endTime = Calendar.getInstance();
    ArrayList<Integer> deliveryIds = new ArrayList<>();
    ArrayList<Delivery> deliveries = new ArrayList<>();
    ArrayList<Integer> extraStopIds = new ArrayList<>();
    ArrayList<ExtraStop> extraStops = new ArrayList<>();
    ArrayList<Tip> carryOutTips = new ArrayList<>();
    double totalMiles;
    double fuelEconomy;
    double mileagePaid;
    double deviceUsagePaid;
    double extraTipsClaimed;
    double totalHours;
    Split split;

    //  list of all local files
    final static File shiftIdsFile = new File("shift_ids.txt");
    final static File shiftsDirectory = new File("shifts");
    File directory;
    File deliveryIdsFile;
    File extraStopIdsFile;
    File totalMilesFile;
    File fuelEconomyFile;
    File mileagePaidFile;
    File deviceUsagePaidFile;
    File extraTipsClaimedFile;
    File totalHoursFile;
    File startTimeFile;
    File endTimeFile;
    File carryOutTipsFile;
    File shiftInfoFile;
    File splitInfoFile;

    File endIndicator;

    // constructor for this class
    public Shift(String date) {
        try {
            this.id.setTime(idFormat.parse(date));
        } catch (ParseException e) { e.printStackTrace(); }
        //  list of all local files
        this.directory = new File(shiftsDirectory, idFormat.format(this.id.getTime()));
        this.deliveryIdsFile = new File(directory.getAbsolutePath(), "delivery_ids.txt");
        this.extraStopIdsFile = new File(directory.getAbsolutePath(), "extra_stop_ids.txt");
        this.totalMilesFile = new File(directory.getAbsolutePath(), "total_miles_traveled.txt");
        this.fuelEconomyFile = new File(directory.getAbsolutePath(), "fuel_economy.txt");
        this.mileagePaidFile = new File(directory.getAbsolutePath(), "mileage_paid.txt");
        this.deviceUsagePaidFile = new File(directory.getAbsolutePath(), "device_usage_paid.txt");
        this.extraTipsClaimedFile = new File(directory.getAbsolutePath(), "extra_tips_claimed.txt");
        this.totalHoursFile = new File(directory.getAbsolutePath(), "total_hours.txt");
        this.startTimeFile = new File(directory.getAbsolutePath(), "shift_start_time.txt");
        this.endTimeFile = new File(directory.getAbsolutePath(), "shift_end_time.txt");
        this.carryOutTipsFile = new File(directory.getAbsolutePath(), "carry_out_tips.txt");
        this.shiftInfoFile = new File(directory.getAbsolutePath(), "shift_info.txt");
        this.splitInfoFile = new File(directory.getAbsolutePath(), "split_info.txt");
        this.split = new Split(directory);
        this.endIndicator = new File(this.directory.getAbsolutePath(), "end_shift");
    }

    // methods for basic shift tracking
    public void carryOutTip() {
        while (Utility.userConfirmation("You would like to enter a carry out tip.")) {
            Tip tip;
            String prompt = "\nWas there a split tip?\t[Y/N]";
            String userResponse = Utility.getInput(prompt, "[yYnN]{1,1}");
            if (userResponse.matches("[yY]{1,1}")) {
                double cardAmount = Utility.inputDouble("\nEnter card tip amount:\t$#.##");
                while (!Utility.userConfirmation("\n$" + Double.toString(cardAmount))) {
                    cardAmount = Utility.inputDouble("\nEnter card tip amount:\t$#.##");
                }
                double cashAmount = Utility.inputDouble("\nEnter cash tip amount:\t$#.##");
                while (!Utility.userConfirmation("\n$" + Double.toString(cashAmount))) {
                    cashAmount = Utility.inputDouble("\nEnter cash tip amount:\t$#.##");
                }
                tip = new Tip(cardAmount, cashAmount);
            } else {
                double amount = Utility.inputDouble("\nEnter tip amount:\t$#.##");
                while (!Utility.userConfirmation("\n$" + Double.toString(amount))) {
                    amount = Utility.inputDouble("\nEnter tip amount:\t$#.##");
                }
                String type = Utility.getInput("\nSelect a tip type:\n1. Card\n2. Cash", "1|2{1,1}");
                String check;
                if (type.matches("[1]{1,1}")) { check = "Card"; } else { check = "Cash"; }
                while (!Utility.userConfirmation(check)) {
                    type = Utility.getInput("\nSelect a tip type:\n1. Card\n2. Cash", "1|2{1,1}");
                    if (type.matches("[1]{1,1}")) { check = "Card"; } else { check = "Cash"; }
                }
                if (type.matches("[1]{1,1}")) { tip = new Tip(amount, Tip.typeCard); }
                else { tip = new Tip(amount, Tip.typeCash); }
            }
            this.carryOutTips.add(tip);
            tip.save(this.carryOutTipsFile);
            break;
        }
    }

    // consolidate all data stored in separate files to one file
    private void consolidate() {
        String data = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
            this.totalMiles, this.fuelEconomy, this.mileagePaid,
            this.deviceUsagePaid, this.extraTipsClaimed, this.totalHours,
            Utility.pyTimeFormat.format(this.startTime.getTime()),
            Utility.pyTimeFormat.format(this.endTime.getTime()));
        Utility.writeData(this.shiftInfoFile, data);
        // delete temporary files after consolidating the data to one file
        this.totalMilesFile.delete();
        this.fuelEconomyFile.delete();
        this.mileagePaidFile.delete();
        this.deviceUsagePaidFile.delete();
        this.extraTipsClaimedFile.delete();
        this.totalHoursFile.delete();
        this.startTimeFile.delete();
        this.endTimeFile.delete();
        updateIdsFile();
    }

    public void delivery() {
        Delivery delivery = new Delivery(this);
        delivery.start();
        this.deliveryIds.add(delivery.id);
        this.deliveries.add(delivery);
    }

    // method to start entering data for the completion of a shift
    public void end() {
        System.out.println("\nAre you sure you would like to end today's shift? [y/n]");
        Scanner userInput = new Scanner(System.in);
        while (!userInput.hasNext("[yYnN]{1,1}")) {
            System.out.println("\nPlease enter [y/n]\n");
            userInput.next();
        }
        // Scanner userResponse = new Scanner(userInput.next());
        if (userInput.hasNext("[yY]{1,1}")) {
            // create file to indatce to program that user started ending the shift
            Utility.writeData(this.endIndicator, "");
            this.endTime = Calendar.getInstance();
            Utility.writeData(this.endTimeFile, Utility.pyTimeFormat.format(this.endTime.getTime()));
            inputTotalMiles(); inputFuelEconomy();
            inputMilagePaid(); inputDeviceUsagePaid();
            inputTotalHours(); inputExtraTipsClaimed();
            consolidate();
            // remove file to indicate to program all data has been entered
            this.endIndicator.delete();
            System.out.println("\n\nShift has been ended!");
            Utility.enterToContinue();
            System.exit(0);
        } else { ; }
    }

    public void extraStop() {
        ExtraStop extraStop = new ExtraStop(this);
        extraStop.start();
        this.extraStopIds.add(extraStop.id);
        this.extraStops.add(extraStop);
    }

    // method to load shift data after already consolidating to one file
    public void load() {
        ArrayList<String> tempList = Utility.commaScanner(this.shiftInfoFile);
        this.totalMiles = Double.parseDouble(tempList.get(0));
        this.fuelEconomy = Double.parseDouble(tempList.get(1));
        this.mileagePaid = Double.parseDouble(tempList.get(2));
        this.deviceUsagePaid = Double.parseDouble(tempList.get(3));
        this.extraTipsClaimed = Double.parseDouble(tempList.get(4));
        this.totalHours = Double.parseDouble(tempList.get(5));
        this.startTime.setTime(Utility.parsePyTime(tempList.get(6)));
        this.endTime.setTime(Utility.parsePyTime(tempList.get(7)));

        if (this.deliveryIdsFile.exists()) {
            this.deliveryIds = Utility.parseIdList(this.deliveryIdsFile);
            for (Integer id: this.deliveryIds) {
                Delivery delivery = new Delivery(id, this);
                delivery.load();
                this.deliveries.add(delivery);
            }
        }

        if (this.extraStopIdsFile.exists()) {
            this.extraStopIds = Utility.parseIdList(this.extraStopIdsFile);
            for (Integer id: this.extraStopIds) {
                ExtraStop extraStop = new ExtraStop(id, this);
                extraStop.load();
                this.extraStops.add(extraStop);
            }
        }

        if (this.carryOutTipsFile.exists()) {
            for (String item: Utility.lineScanner(this.carryOutTipsFile)) {
                tempList = Utility.commaScanner(item);
                Tip tip = new Tip(
                    Double.parseDouble(tempList.get(0)),
                    Double.parseDouble(tempList.get(1)),
                    Double.parseDouble(tempList.get(2)));
                this.carryOutTips.add(tip);
            }
        }

        if (splitInfoFile.exists()) {
            split.load();
        }
    }

    // method to create the shift directory and save the start time
    public void start() {
        this.directory.mkdir();
        this.startTime = Calendar.getInstance();
        Utility.writeData(this.startTimeFile, Utility.pyTimeFormat.format(this.startTime.getTime()));
        System.out.println("\nShift has been started!");
        Utility.enterToContinue();
        System.exit(0);
    }

    // method to update the ids file so the program knows the shift exists
    private void updateIdsFile() {
        if (shiftIdsFile.exists()) {
            Utility.appendData(shiftIdsFile, idFormat.format(this.id.getTime()), ",");
        } else {
            Utility.writeData(shiftIdsFile, idFormat.format(this.id.getTime()));
        }
    }

    // methods for inputting data
    private void inputDeviceUsagePaid() {
        String prompt = "\nAmount of device usage paid:\t$#.##";
        this.deviceUsagePaid = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation("$" + Double.toString(this.deviceUsagePaid))) {
            this.deviceUsagePaid = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.deviceUsagePaidFile, Double.toString(this.deviceUsagePaid));
    }

    private void inputExtraTipsClaimed() {
        String prompt = "\nExtra tips claimed for shift:\t$#.##";
        this.extraTipsClaimed = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation("$" + Double.toString(this.extraTipsClaimed))) {
            this.extraTipsClaimed = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.extraTipsClaimedFile, Double.toString(this.extraTipsClaimed));
    }

    private void inputFuelEconomy() {
        String prompt = "\nEnter fuel economy:\t##.#";
        this.fuelEconomy = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation(Double.toString(this.fuelEconomy) + " mpg")) {
            this.fuelEconomy = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.fuelEconomyFile, Double.toString(this.fuelEconomy));
    }

    private void inputMilagePaid() {
        String prompt = "\nAmount of mileage paid:\t$#.##";
        this.mileagePaid = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation("$" + Double.toString(this.mileagePaid))) {
            this.mileagePaid = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.mileagePaidFile, Double.toString(this.mileagePaid));
    }

    private void inputTotalHours() {
        String prompt = "\nEnter total hours worked:\t#.##";
        this.totalHours = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation(Double.toString(this.totalHours) + "hours")) {
            this.totalHours = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.totalHoursFile, Double.toString(this.totalHours));
    }

    private void inputTotalMiles() {
        String prompt = "\nTotal miles traveled for this shift:\t#.#";
        this.totalMiles = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation(Double.toString(this.totalMiles) + " miles")) {
            this.totalMiles = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.totalMilesFile, Double.toString(this.totalMiles));
    }

    // methods for continuing tracking if program ends
    // method to load shift data while a shift is in progress
    public void loadCurrent() {
        // todo: in android, this method will be not needed. *THIS MIGHT BE WRONG
        //       data should be saved to the local disk by indavidual activiteis
        if (this.startTimeFile.exists()) {
            this.startTime.setTime(Utility.parsePyTime(Utility.readLine(this.startTimeFile)));
        } else {
            // todo: in android this will be handled by getinstance or widget.TimePicker
            this.startTime = Calendar.getInstance();
            Utility.writeData(
                this.startTimeFile,
                Utility.pyTimeFormat.format(this.startTime.getTime()));
        }

        if (this.deliveryIdsFile.exists()) {
            this.deliveryIds = Utility.parseIdList(this.deliveryIdsFile);
            for (Integer id: this.deliveryIds) {
                Delivery delivery = new Delivery(id, this);
                delivery.load();
                this.deliveries.add(delivery);
            }
        }

        if (this.extraStopIdsFile.exists()) {
            this.extraStopIds = Utility.parseIdList(this.extraStopIdsFile);
            for (int id: this.extraStopIds) {
                ExtraStop extraStop = new ExtraStop(id, this);
                extraStop.load();
                this.extraStops.add(extraStop);
            }
        }

        if (this.carryOutTipsFile.exists()) {
            for (String item: Utility.lineScanner(this.carryOutTipsFile)) {
                ArrayList<String> tempList = Utility.commaScanner(item);
                Tip tip = new Tip(
                    Double.parseDouble(tempList.get(0)),
                    Double.parseDouble(tempList.get(1)),
                    Double.parseDouble(tempList.get(2)));
                this.carryOutTips.add(tip);
            }
        }

        if (this.splitInfoFile.exists()) {
            split.load();
        }
    }

    // method to load end of shift data when end of shift has started
    private void loadEnd() {
        if (this.endTimeFile.exists()) {
            this.endTime.setTime(Utility.parsePyTime(Utility.readLine(this.endTimeFile)));
        }
        if (this.totalMilesFile.exists()) {
            this.totalMiles = Double.parseDouble(Utility.readLine(this.totalMilesFile));
        }
        if (this.fuelEconomyFile.exists()) {
            this.fuelEconomy = Double.parseDouble(Utility.readLine(this.fuelEconomyFile));
        }
        if (this.mileagePaidFile.exists()) {
            this.mileagePaid = Double.parseDouble(Utility.readLine(this.mileagePaidFile));
        }
        if (this.deviceUsagePaidFile.exists()) {
            this.deviceUsagePaid = Double.parseDouble(Utility.readLine(this.deviceUsagePaidFile));
        }
        if (this.totalHoursFile.exists()) {
            this.totalHours = Double.parseDouble(Utility.readLine(this.totalHoursFile));
        }
        if (this.extraTipsClaimedFile.exists()) {
            this.extraTipsClaimed = Double.parseDouble(Utility.readLine(this.extraTipsClaimedFile));
        }
    }

    // method to check if extra stop, delivery, or end shift has been started
    public void resume() {
        // check if an extra stop has been started
        ExtraStop extraStop = new ExtraStop(this);
        if (extraStop.startIndicator.exists()) {
            extraStop.loadCurrent();
            this.extraStopIds.add(extraStop.id);
            this.extraStops.add(extraStop);
        }
        // check if a delivery has been started
        Delivery delivery = new Delivery(this);
        if (delivery.directory.exists()) {
            delivery.loadCurrent();
            this.deliveryIds.add(delivery.id);
            this.deliveries.add(delivery);
        }
        // check if end shift has been started
        else if (this.endIndicator.exists()) {
            loadEnd();
            resumeEnd();
        }
    }

    // method to finish entering end of shift data
    private void resumeEnd() {
        if (!this.endTimeFile.exists()) {
            this.endTime = Calendar.getInstance();
            Utility.writeData(
                this.endTimeFile,
                Utility.pyTimeFormat.format(this.endTime.getTime()));
        }
        if (!this.totalMilesFile.exists()) { inputTotalMiles(); }
        if (!this.fuelEconomyFile.exists()) { inputFuelEconomy(); }
        if (!this.mileagePaidFile.exists()) { inputMilagePaid(); }
        if (!this.deviceUsagePaidFile.exists()) { inputDeviceUsagePaid(); }
        if (!this.totalHoursFile.exists()) { inputTotalHours(); }
        if (!this.extraTipsClaimedFile.exists()) { inputExtraTipsClaimed(); }
        consolidate();
        // remove file to indicate to program all data has been entered
        this.endIndicator.delete();
        System.out.println("\n\nShift has been ended!");
        Utility.enterToContinue();
        System.exit(0);
    }

    // methods for when current day's shift has already been completed
    public void completed() {
        String prompt = "\nYou have already completed a shift for today.\n" +
                        "Please select an option:\n" +
                        "R. Resume today\'s shift\n" +
                        "O. Overwrite shift\n" +
                        "Q. Quit program";
        String userChoise = Utility.getInput(prompt, "[rRoOqQ]{1,1}");
        if (userChoise.matches("[rR]{1,1}")) { resumeShift(); }
        else if (userChoise.matches("[oO]{1,1}")) { overwrite(); }
        else if (userChoise.matches("[qQ]{1,1}")) { System.exit(0); }
    }

    private void overwrite() {
        String prompt = "\nAre you sure you want to overwrite the completed shift?\n[Y/N]";
        String userChoise = Utility.getInput(prompt, "[yY]{1,1}");
        if (userChoise.matches("[yY]{1,1}")) {
            Utility.deleteDirectory(this.directory);
            this.directory.mkdir();
            this.startTime = Calendar.getInstance();
            Utility.writeData(
                this.startTimeFile,
                Utility.pyTimeFormat.format(this.startTime.getTime()));
            removeIdFromFile();
            System.out.println("\nShift has been overwriten!\n");
            Utility.enterToContinue();
            System.exit(0);
        } else { ; }
    }

    private void resumeShift() {
        String prompt = "\nAre you sure you want to resume the completed shift?\n[Y/N]";
        String userChoice  = Utility.getInput(prompt, "[yYnN]{1,1}");
        if (userChoice.matches("[yY]{1,1}")) {
            String[] shiftData = Utility.readLine(this.shiftInfoFile).split(",");
            this.startTime.setTime(Utility.parsePyTime(shiftData[7]));
            Utility.writeData(
                this.startTimeFile, Utility.pyTimeFormat.format(this.startTime.getTime()));
            this.shiftInfoFile.delete();
            removeIdFromFile();
        } else { ; }
    }

    private void removeIdFromFile() {
        ArrayList<String> shiftIds = Utility.commaScanner(shiftIdsFile);
        ArrayList<String> shifts = new ArrayList<>();
        for (String id: shiftIds) {
            if (!id.matches(idFormat.format(this.id.getTime()))) {
                shifts.add(id);
            }
        }
        String idList = "";
        int commaNumber = shifts.size() - 1;
        try {
            for (int base = 0; base < commaNumber; base++) {
                idList += shifts.get(base) + ",";
            }
            idList += shifts.get(commaNumber + 1);
        } catch (IndexOutOfBoundsException e) { shiftIdsFile.delete(); }
        Utility.writeData(shiftIdsFile, idList);
    }

    // method to save chagnes to an already completed shift
    public void saveChanges() {
        String data = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
            this.totalMiles, this.fuelEconomy, this.mileagePaid,
            this.deviceUsagePaid, this.extraTipsClaimed, this.totalHours,
            Utility.pyTimeFormat.format(this.startTime.getTime()),
            Utility.pyTimeFormat.format(this.endTime.getTime()));
        Utility.writeData(this.shiftInfoFile, data);
    }

}
// todo: need to write function that allows the user to change data