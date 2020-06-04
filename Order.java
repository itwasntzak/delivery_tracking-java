import java.util.ArrayList;
import java.util.Calendar;
import java.io.File;
import java.net.URI;
import java.nio.channels.WritableByteChannel;

public class Order {
    // initialize attributes for this class
    int id;
    Delivery parent;
    Calendar endTime = Calendar.getInstance();
    Tip tip;
    double milesTraveled;

    //  list of all local files
    File idFile;
    File orderInfoFile;
    File tipFile;
    File milesFile;
    File endtimeFile;

    File startIndicator;

    // constructor for new orders
    public Order(Delivery delivery) {
        this.parent = delivery;
        this.idFile = new File(parent.directory.getAbsolutePath(), "order_id.txt");
        this.tipFile = new File(parent.directory.getAbsolutePath(), "tip.txt");
        this.milesFile = new File(parent.directory.getAbsolutePath(), "order_miles_traveled.txt");
        this.endtimeFile = new File(parent.directory.getAbsolutePath(), "order_end_time.txt");
        this.startIndicator = new File(parent.directory.getAbsolutePath(), "order");
    }

    // constructor to load completed orders
    public Order(int id, Delivery delivery) {
        this.id = id;
        this.parent = delivery;
        this.orderInfoFile = new File(parent.directory.getAbsolutePath(), Integer.toString(id) + ".txt");
        this.tipFile = new File(parent.directory.getAbsolutePath(), "tip.txt");
        this.milesFile = new File(parent.directory.getAbsolutePath(), "order_miles_traveled.txt");
        this.endtimeFile = new File(parent.directory.getAbsolutePath(), "order_end_time.txt");
    }

    // method to consolidate data from indavidule files to one file
    public void consolidate() {
        this.orderInfoFile = new File(this.parent.directory.getAbsolutePath(), Integer.toString(id) + ".txt");
        String data = String.format("%s,%s,%s,%s,%s",
            this.tip.card, this.tip.cash, this.tip.unknown,
            this.milesTraveled, Utility.pyTimeFormat.format(this.endTime.getTime()));
        Utility.writeData(this.orderInfoFile, data);
        this.updateIdsFile();
        // delete temporary files after consolidating the data to one file
        this.idFile.delete();
        this.tipFile.delete();
        this.milesFile.delete();
        this.endtimeFile.delete();
        this.startIndicator.delete();
    }

    // method to load order data after already consolidating to one file
    public void load() {
        ArrayList<String> tempList = Utility.commaScanner(this.orderInfoFile);
        this.tip = new Tip(
            Double.parseDouble(tempList.get(0)),
            Double.parseDouble(tempList.get(1)),
            Double.parseDouble(tempList.get(2)));        
        this.milesTraveled = Double.parseDouble(tempList.get(3));
        this.endTime.setTime(Utility.parsePyTime(tempList.get(4)));
    }

    // method to start entering data for an order
    public void start() {
        Utility.writeData(new File(this.parent.directory.getAbsolutePath(), "order"), "");
        inputIdNumber();
        inputTip();
        inputMilesTraveled();
        this.endTime = Calendar.getInstance();
        Utility.writeData(this.endtimeFile, Utility.pyTimeFormat.format(this.endTime.getTime()));
        consolidate();
        String timeTaken = Utility.timeTaken(this.parent.startTime, this.endTime);
        System.out.println("\nThis order was completed in:\t" + timeTaken);
    }

    // method to update/create the order ids file for the delivery
    private void updateIdsFile() {
        if (this.parent.orderIdsFile.exists()) {
            Utility.appendData(this.parent.orderIdsFile, Integer.toString(this.id), ",");
        } else { Utility.writeData(this.parent.orderIdsFile, Integer.toString(this.id)); }
    }

    // methods for inputting data
    private void inputMilesTraveled() {
        String prompt = "\nOrder miles traveled:\t#.#";
        this.milesTraveled = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation(Double.toString(this.milesTraveled) + " miles")) {
            this.milesTraveled = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.milesFile, Double.toString(this.milesTraveled));
    }

    private void inputIdNumber() {
        String prompt = "\nEnter order number:\t#-####";
        this.id = Utility.inputInt(prompt);
        while (!Utility.userConfirmation(Integer.toString(this.id))) {
            this.id = Utility.inputInt(prompt);
        }
        Utility.writeData(this.idFile, Integer.toString(this.id));
    }

    private void inputTip() {
        double noTip = 0.0;
        String prompt = "\nWas there a split tip?\t[Y/N]";
        String userResponse = Utility.getInput(prompt, "[yYnN]{1,1}");
        if (userResponse.matches("[yY]{1,1}")) {
            double cardAmount = Utility.inputDouble("\nEnter card tip amount:\t$#.##");
            while (!Utility.userConfirmation("$" + Double.toString(cardAmount) + " card tip")) {
                cardAmount = Utility.inputDouble("\nEnter card tip amount:\t$#.##");
            }

            double cashAmount = Utility.inputDouble("\nEnter cash tip amount:\t$#.##");
            while (!Utility.userConfirmation("$" + Double.toString(cashAmount) + " cash tip")) {
                cashAmount = Utility.inputDouble("\nEnter cash tip amount:\t$#.##");
            }

            this.tip = new Tip(cardAmount, cashAmount);

        } else {
            double amount = Utility.inputDouble("\nEnter tip amount:\t$#.##\n(Enter 0 for no tip)");
            while (!Utility.userConfirmation("$" + Double.toString(amount))) {
                amount = Utility.inputDouble("\nEnter tip amount:\t$#.##\n(Enter 0 for no tip)");
            }

            if (amount == noTip) { this.tip = new Tip(); } else {
                String type = Utility.getInput("\nSelect a tip type:\n1. Card\n2. Cash", "1|2{1,1}");
                String data;
                if (type.matches("[1]{1,1}")) { data = "Card"; } else { data = "Cash"; }
                while (!Utility.userConfirmation(data)) {
                    type = Utility.getInput("\nSelect a tip type:\n1. Card\n2. Cash", "1|2{1,1}");
                    if (type.matches("[1]{1,1}")) { data = "Card"; } else { data = "Cash"; }
                }

                if (type.matches("[1]{1,1}")) { this.tip = new Tip(amount, Tip.typeCard); }
                else { this.tip = new Tip(amount, Tip.typeCash); }
            }
        }
        this.tip.save(this.tipFile);
    }

    //  method to load order data while a order is in progress
    public void loadCurrent() {
        if (this.idFile.exists()) { this.id = Integer.parseInt(Utility.readLine(this.idFile)); }
        if (this.tipFile.exists()) {
            ArrayList<String> tempList = Utility.commaScanner(this.tipFile);
            ArrayList<Double> tipList = new ArrayList<>();
            for (String tip: tempList) {
                tipList.add(Double.parseDouble(tip));
            }
            tip = new Tip(tipList.get(0), tipList.get(1), tipList.get(2));
        }
        if (this.milesFile.exists()) {
            this.milesTraveled = Double.parseDouble(Utility.readLine(this.milesFile));
        }
        if (this.endtimeFile.exists()) {
            this.endTime.setTime(Utility.parsePyTime(Utility.readLine(this.endtimeFile)));
        }
        resume();
    }

    // method to comlete entering data for started order
    private void resume() {
        if (!this.idFile.exists()) { inputIdNumber(); }
        if (!this.tipFile.exists()) { inputTip(); }
        if (!this.milesFile.exists()) { inputMilesTraveled(); }
        if (!this.endtimeFile.exists()) {
            this.endTime = Calendar.getInstance();
            Utility.writeData(this.endtimeFile, Utility.pyTimeFormat.format(this.endTime.getTime()));
        }
        consolidate();
        String timeTaken = Utility.timeTaken(this.parent.startTime, this.endTime);
        System.out.println("\nThis order was completed in:\t" + timeTaken);
    }

    public void saveChanges() {
        String data = String.format("%s,%s,%s,%s,%s",
            this.tip.card, this.tip.cash, this.tip.unknown,
            this.milesTraveled, Utility.pyTimeFormat.format(this.endTime));
        Utility.writeData(this.orderInfoFile, data);
    }

}
// todo: need to write function that allows the user to change data