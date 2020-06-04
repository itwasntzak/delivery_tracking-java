import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.math.RoundingMode;

public class Utility {
    static SimpleDateFormat pyTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    static Date tempDate;
    static ArrayList<String> tempList;

    public static void appendData(File file, String data, String separatorType) {
        try {
            FileWriter textWriter = new FileWriter(file, true);
            textWriter.write(separatorType + data);
            textWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeData(File file, String data) {
        try {
            FileWriter textWriter = new FileWriter(file);
            textWriter.write(data);
            textWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readLine(File file) {
        try {
            Scanner tempScanner = new Scanner(file);
            String data = tempScanner.nextLine();
            tempScanner.close();
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "ERROR: Nothing was found in the file.";
    }

    public static void enterToContinue() {
        System.out.println("\nPress enter to continue!");
        Scanner userInput = new Scanner(System.in);
        while (userInput.hasNextLine()) {
            String userResponse = userInput.nextLine();
            if (!userResponse.equals("")) {
                System.out.println("\nPlease press enter to continue!");
            } else {
                break;
            }
        }
    }

    public static double inputDouble(String prompt) {
        System.out.println(prompt);
        Scanner userInput = new Scanner(System.in);
        while (!userInput.hasNextDouble()) {
            System.out.println("\nThat is not a valid input.");
            System.out.println(prompt);
            userInput.next();
        }
        return userInput.nextDouble();
    }

    public static int inputInt(String prompt) {
        System.out.println(prompt);
        Scanner userInput = new Scanner(System.in);
        while (!userInput.hasNextInt()) {
            System.out.println("\nThat is not a valid input.");
            System.out.println(prompt);
            userInput.next();
        }
        return userInput.nextInt();
    }

    public static String getInput(String prompt, String regex) {
        System.out.println(prompt);
        Scanner userInput = new Scanner(System.in);
        while (!userInput.hasNext(regex)) {
            System.out.println("\nThat is not a valid input.");
            System.out.println(prompt);
            userInput.next();
        }
        return userInput.next();
    }

    public static boolean userConfirmation(String data) {
        Scanner userInput = new Scanner(System.in);
        System.out.println("\n" + data + "\nIs this correct? [Y/N]");
        while (!userInput.hasNext("[yYnN]{1,1}")) {
            System.out.println("\nThat is not a valid input.");
            System.out.println("\n" + data + "\nIs this correct? [Y/N]");
            userInput.next();
        }
        if (userInput.hasNext("[yY]{1,1}")) { return true; } else { return false; }
    }

    public static Date parsePyTime(String data) {
        try {
            tempDate = pyTimeFormat.parse(data);
            return tempDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tempDate;
    }

    public static ArrayList<String> commaScanner(File file) {
        tempList = new ArrayList<>();
        try {
            Scanner idListScanner = new Scanner(file).useDelimiter(",");
            while (idListScanner.hasNext()) { tempList.add(idListScanner.next()); }
            idListScanner.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        return tempList;
    }

    public static ArrayList<String> commaScanner(String string) {
        tempList = new ArrayList<>();
        Scanner idListScanner = new Scanner(string).useDelimiter(",");
        while (idListScanner.hasNext()) {
            tempList.add(idListScanner.next());
        }
        return tempList;
    }

    public static ArrayList<String> lineScanner(File file) {
        tempList = new ArrayList<>();
        try {
            Scanner idListScanner = new Scanner(file);
            while (idListScanner.hasNextLine()) {
                tempList.add(idListScanner.nextLine());
            }
            idListScanner.close();
            return tempList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return tempList;
    }

    public static ArrayList<Integer> parseIdList(File file) {
        ArrayList<Integer> intList = new ArrayList<>();
        for (String id: commaScanner(file)) {
            intList.add(Integer.parseInt(id));
        }
        return intList;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
    
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String timeTaken(Calendar startTime, Calendar endTime) {
        long timeDifference = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        int millaseconds = (int) timeDifference % 1000;
        int seconds = (int) timeDifference / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        int hours;
        if (minutes > 60) {
            hours = minutes / 60;
            minutes = minutes % 60;
        } else { hours = 0; }
        return String.format("%s:%s:%s.%s", hours, minutes, seconds, millaseconds);
    }

    public static boolean deleteDirectory(File file) {
        File[] allContents = file.listFiles();
        if (allContents != null) {
            for (File subFile: allContents) {
                deleteDirectory(subFile);
            }
        }
        return file.delete();
    }

    public static Delivery driving(Delivery delivery, String destination, String prompt) {
        File startIndicator = new File(delivery.directory.getAbsolutePath(), "driving-" + destination);
        if (!startIndicator.exists()) { Utility.writeData(startIndicator, ""); }
        prompt = prompt + "\nC: To complete\nE: For extra stop\nT: See current time\nQ: Quit program";
        while (true) {
            String waitForUser = getInput(prompt, "[cCeEtTqQ]{1,1}");;
            if (waitForUser.matches("[cC]{1,1}")) { startIndicator.delete(); break; }
            else if (waitForUser.matches("[eE]{1,1}")) {
                ExtraStop extraStop = new ExtraStop(delivery);
                extraStop.start();
                delivery.extraStopIds.add(extraStop.id);
                delivery.extraStops.add(extraStop);
            } else if (waitForUser.matches("[tT]{1,1}")) {
                System.out.println(
                    "\nThe current time on this delivery is:\t" +
                    timeTaken(delivery.startTime, Calendar.getInstance()));
            } else if (waitForUser.matches("[qQ]{1,1}")) { System.exit(0); }
        }
        return delivery;
    }
}