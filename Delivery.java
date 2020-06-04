import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.io.File;


public class Delivery {
    // initialize attributes for this class
    int id;
    Shift shift;
    Calendar startTime = Calendar.getInstance();
    Calendar endTime = Calendar.getInstance();
    int orderQuantity;
    ArrayList<Integer> orderIds = new ArrayList<>();
    ArrayList<Order> orders = new ArrayList<>();
    ArrayList<Integer> extraStopIds = new ArrayList<>();
    ArrayList<ExtraStop> extraStops = new ArrayList<>();
    double milesTraveled;
    int averageSpeed;

    //  list of all local files
    File directory;
    File orderQuantityFile;
    File milesTraveledFile;
    File averageSpeedFile;
    File startTimeFile;
    File endTimeFile;
    File orderIdsFile;
    File extraStopIdsFile;
    File deliveryInfoFile;

    // constructor for new deliveries
    public Delivery(Shift parent) {
        this.id = parent.deliveries.size();
        this.shift = parent;
        this.directory = new File(parent.directory.getAbsolutePath(), Integer.toString(id));
        this.orderQuantityFile = new File(directory, "order_quantity.txt");
        this.milesTraveledFile = new File(directory, "delivery_miles_traveled.txt");
        this.averageSpeedFile = new File(directory, "delivery_average_speed.txt");
        this.startTimeFile = new File(directory, "delivery_start_time.txt");
        this.endTimeFile = new File(directory, "delivery_end_time.txt");
        this.orderIdsFile = new File(directory, "order_ids.txt");
        this.extraStopIdsFile = new File(directory, "extra_stop_ids.txt");
        this.deliveryInfoFile = new File(directory, "delivery_info.txt");
    }

    // constructor for loading already completed deliveries
    public Delivery(int id, Shift parent) {
        this.id = id;
        this.shift = parent;
        this.directory = new File(parent.directory.getAbsolutePath(), Integer.toString(id));
        this.orderQuantityFile = new File(directory, "order_quantity.txt");
        this.milesTraveledFile = new File(directory, "delivery_miles_traveled.txt");
        this.averageSpeedFile = new File(directory, "delivery_average_speed.txt");
        this.startTimeFile = new File(directory, "delivery_start_time.txt");
        this.endTimeFile = new File(directory, "delivery_end_time.txt");
        this.orderIdsFile = new File(directory, "order_ids.txt");
        this.extraStopIdsFile = new File(directory, "extra_stop_ids.txt");
        this.deliveryInfoFile = new File(directory, "delivery_info.txt");
    }

    // methods for delivery tracking
    // method to consolidate all data from indavidual files to one file
    private void consolidate() {
        String data = String.format("%s,%s,%s,%s",
            this.milesTraveled, this.averageSpeed,
            Utility.pyTimeFormat.format(this.startTime.getTime()),
            Utility.pyTimeFormat.format(this.endTime.getTime()));
        Utility.writeData(this.deliveryInfoFile, data);
        // delete temporary files after consolidating the data to one file
        this.orderQuantityFile.delete();
        this.milesTraveledFile.delete();
        this.averageSpeedFile.delete();
        this.startTimeFile.delete();
        this.endTimeFile.delete();
        this.updateIdsFile();
    }

    // method to load delivery data after already consolidating to one file
    public void load() {
        ArrayList<String> tempList = Utility.commaScanner(this.deliveryInfoFile);
        this.milesTraveled = Double.parseDouble(tempList.get(0));
        this.averageSpeed = Integer.parseInt(tempList.get(1));
        this.startTime.setTime(Utility.parsePyTime(tempList.get(2)));
        this.endTime.setTime(Utility.parsePyTime(tempList.get(3)));

        if (this.orderIdsFile.exists()) {
            this.orderIds = Utility.parseIdList(this.orderIdsFile);
            for (Integer id: this.orderIds) {
                Order order = new Order(id, this);
                order.load();
                this.orders.add(order);
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
    }

    // method to start new delivery
    public void start() {
        this.directory.mkdir();
        this.startTime = Calendar.getInstance();
        inputOrderQuantity();
        for (int completed = 0; completed < this.orderQuantity; completed++) {
            Utility.driving(this, "address", "\nDriving to address...");
            Order order = new Order(this);
            order.start();
            this.orderIds.add(order.id);
            this.orders.add(order);
        }
        Utility.driving(this, "store", "\nDriving back to the store...");
        inputMilesTraveled();
        inputAverageSpeed();
        this.endTime = Calendar.getInstance();
        Utility.writeData(this.endTimeFile, Utility.pyTimeFormat.format(this.endTime.getTime()));
        consolidate();
        String timeTaken = Utility.timeTaken(this.startTime, this.endTime);
        System.out.println("\nThis delivery was completed in:\t" + timeTaken);
    }

    // method to update the file that tells the program deliveries exist
    private void updateIdsFile() {
        if (this.shift.deliveryIdsFile.exists()) {
            Utility.appendData(this.shift.deliveryIdsFile, Integer.toString(this.id), ",");
        } else { Utility.writeData(this.shift.deliveryIdsFile, Integer.toString(this.id)); }
    }

    // methods for inputting data
    private void inputAverageSpeed() {
        String prompt = "\nEnter the average speed for this delivery:\t##";
        this.averageSpeed = Utility.inputInt(prompt);
        while (!Utility.userConfirmation(Integer.toString(this.averageSpeed) + " mph")) {
            this.averageSpeed = Utility.inputInt(prompt);
        }
        Utility.writeData(this.averageSpeedFile, Integer.toString(this.averageSpeed));
    }

    private void inputMilesTraveled() {
        String prompt = "\nDelivery miles traveled:\t#.#";
        this.milesTraveled = Utility.inputDouble(prompt);
        while (!Utility.userConfirmation(Double.toString(this.milesTraveled) + " miles")) {
            this.milesTraveled = Utility.inputDouble(prompt);
        }
        Utility.writeData(this.milesTraveledFile, Double.toString(this.milesTraveled));
    }

    private void inputOrderQuantity() {
        String prompt = "\nNumber of orders?";
        this.orderQuantity = Utility.inputInt(prompt);
        while (!Utility.userConfirmation(Integer.toString(this.orderQuantity))) {
            this.orderQuantity = Utility.inputInt(prompt);
        }
        Utility.writeData(this.orderQuantityFile, Integer.toString(this.orderQuantity));
    }

    // methods for continuing tracking if program ends
    // method to load delivery data while a delivery is in progress
    public void loadCurrent() {
        // todo: in android, this method will be not needed. *THIS MIGHT BE WRONG
        //       data should be saved to the local disk by indavidual activiteis
        if (this.startTimeFile.exists()) {
            this.startTime.setTime(Utility.parsePyTime(Utility.readLine(this.startTimeFile)));
        } else  {
            // todo: in android this will be handled by getinstance or widget.TimePicker
            this.startTime = Calendar.getInstance();
            Utility.writeData(
                this.startTimeFile,
                Utility.pyTimeFormat.format(this.startTime.getTime()));
        }
        this.id = this.shift.deliveries.size();
        if (this.orderQuantityFile.exists()) {
            this.orderQuantity = Integer.parseInt(Utility.readLine(this.orderQuantityFile));
        } else { inputOrderQuantity(); }

        if (this.orderIdsFile.exists()) {
            this.orderIds = Utility.parseIdList(this.orderIdsFile);
            for (int id: this.orderIds) {
                Order order = new Order(id, this);
                order.load();
                this.orders.add(order);
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

        if (this.milesTraveledFile.exists()) {
            this.milesTraveled = Double.parseDouble(Utility.readLine(this.milesTraveledFile));
        }
        if (this.averageSpeedFile.exists()) {
            this.averageSpeed = Integer.parseInt(Utility.readLine(this.averageSpeedFile));
        }
        if (this.endTimeFile.exists()) {
            this.endTime.setTime(Utility.parsePyTime(Utility.readLine(this.endTimeFile)));
        }
        resume();
    }

    // method to comlete entering data for started delivery
    private void resume() {
        // check if an extra stop has been started
        ExtraStop extraStop = new ExtraStop(this);
        if (extraStop.startIndicator.exists()) {
            extraStop.loadCurrent();
            this.extraStopIds.add(extraStop.id);
            this.extraStops.add(extraStop);
        }
        // check if any order data has started to be entered
        Order order = new Order(this);
        if (order.startIndicator.exists()) {
            order.loadCurrent();
            this.orderIds.add(order.id);
            this.orders.add(order);
        }
        // check if user is in the middle of driving to address
        File drivingIndicator = new File(this.directory.getAbsolutePath(), "driving-address");
        if (drivingIndicator.exists()) {
            Utility.driving(this, "address", "\nDriving to address...");
            order = new Order(this);
            order.start();
            this.orderIds.add(order.id);
            this.orders.add(order);
        }
        while (this.orderQuantity > this.orders.size()) {
            Utility.driving(this, "address", "\nDriving to address...");
            order = new Order(this);
            order.start();
            this.orderIds.add(order.id);
            this.orders.add(order);
        }

        Utility.driving(this, "store", "\nDriving back to the store...");
        if (!this.milesTraveledFile.exists()) { inputMilesTraveled(); }
        if (!this.averageSpeedFile.exists()) { inputAverageSpeed(); }
        if (!this.endTimeFile.exists()) { this.endTime = Calendar.getInstance(); }
        consolidate();
        String timeTaken = Utility.timeTaken(this.startTime, this.endTime);
        System.out.println("\nThis delivery was completed in:\t" + timeTaken);
    }

    public void saveChanges() {
        String data = String.format("%s,%s,%s,%s",
            this.milesTraveled, this.averageSpeed,
            Utility.pyTimeFormat.format(this.startTime),
            Utility.pyTimeFormat.format(this.endTime));
        Utility.writeData(this.deliveryInfoFile, data);
    }

}
// todo: need to write function that allows the user to change data