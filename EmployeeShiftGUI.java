import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

// Shift types
enum Shift {
    MORNING, EVENING, NIGHT
}

// Employee class 1 
class Employee {
    int id;
    String name;
    Shift shift;

    Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Output formatting: one aligned table row (shows "Not assigned" instead of "null")
    public String toString() {
        return String.format("%-6d %-22s %-12s", id, name,
                (shift == null) ? "Not assigned" : shift.toString());
    }
}

// Attendance class 2
// Check-in and check-out are stored as simple hour numbers (0 to 24)
// Example: 9.5 means 9:30 AM
class Attendance {
    int employeeId;
    String name;
    Shift shift;
    double checkIn;
    double checkOut;

    Attendance(Employee e, double checkIn) {
        employeeId = e.id;
        name = e.name;
        shift = e.shift;
        this.checkIn = checkIn;
        this.checkOut = -1; // -1 means not checked out yet
    }

    // Simple subtraction, no Duration class needed
    double workedHours() {
        if (checkOut == -1) return 0;
        return checkOut - checkIn;
    }

    // Column header that matches the row layout in toString()
    static String header() {
        return String.format("%-6s %-16s %-9s %-10s %-10s %6s",
                "ID", "NAME", "SHIFT", "IN", "OUT", "HOURS");
    }

    // Output formatting: one aligned table row
    public String toString() {
        String outText = (checkOut == -1) ? "Not yet" : TimeUtil.format(checkOut);
        return String.format("%-6d %-16s %-9s %-10s %-10s %6s",
                employeeId, name, shift,
                TimeUtil.format(checkIn), outText,
                String.format("%.2f", workedHours()));
    }
}

// Helper class to convert between "9:30 AM" text and decimal hours (9.5)
class TimeUtil {

    // Converts text like "9:30 AM" or "5:45 PM" into decimal hours (e.g. 9.5)
    static double parse(String input) {
        input = input.trim().toUpperCase();

        String period = input.substring(input.length() - 2); // "AM" or "PM"
        String timePart = input.substring(0, input.length() - 2).trim();

        String[] parts = timePart.split(":");
        int hour = Integer.parseInt(parts[0].trim());
        int minute = Integer.parseInt(parts[1].trim());

        if (hour < 1 || hour > 12 || minute < 0 || minute > 59)
            throw new NumberFormatException("Invalid time");

        if (period.equals("AM")) {
            if (hour == 12) hour = 0; // 12 AM = midnight = 0
        } else if (period.equals("PM")) {
            if (hour != 12) hour = hour + 12; // convert to 24-hour, but keep 12 PM as 12
        } else {
            throw new NumberFormatException("Must end in AM or PM");
        }

        return hour + (minute / 60.0);
    }

    // Converts decimal hours (e.g. 9.5) back into readable text (e.g. "9:30 AM")
    static String format(double time) {
        int hour24 = (int) time;
        int minute = (int) Math.round((time - hour24) * 60);

        String period = (hour24 < 12) ? "AM" : "PM";
        int hour12 = hour24 % 12;
        if (hour12 == 0) hour12 = 12;

        return String.format("%d:%02d %s", hour12, minute, period);
    }
}

// Main management class 3 
class EmployeeManager {
    ArrayList<Employee> employees = new ArrayList<>();
    LinkedList<Attendance> records = new LinkedList<>();
    HashMap<Shift, String> shiftTimings = new HashMap<>();
    TreeMap<Integer, String> report = new TreeMap<>();

    EmployeeManager() {
        shiftTimings.put(Shift.MORNING, "6 AM - 2 PM");
        shiftTimings.put(Shift.EVENING, "2 PM - 10 PM");
        shiftTimings.put(Shift.NIGHT, "10 PM - 6 AM");
    }

    // Output helper: title followed by a divider line
    static String title(String text) {
        return "\u25A0 " + text + "\n" + "\u2550".repeat(62) + "\n";
    }

    // Output helper: thin divider line
    static String divider() {
        return "\u2500".repeat(62) + "\n";
    }

    Employee findEmployee(int id) {
        for (Employee e : employees) {
            if (e.id == id) return e;
        }
        return null;
    }

    // 1. Register employee.    4 
    String addEmployee(int id, String name) {
        if (id <= 0 || name.trim().isEmpty())
            return "Enter a valid ID and name.";

        if (findEmployee(id) != null)
            return "Employee ID already exists.";

        employees.add(new Employee(id, name.trim()));
        return "Employee registered successfully.";
    }

    // Update employee name
    String updateEmployee(int id, String name) {
        Employee e = findEmployee(id);
        if (e == null) return "Employee not found.";
        if (name.trim().isEmpty()) return "Enter a valid name.";

        e.name = name.trim();
        return "Employee updated successfully.";
    }

    // Delete employee
    String deleteEmployee(int id) {
        Employee e = findEmployee(id);
        if (e == null) return "Employee not found.";

        employees.remove(e);
        return "Employee deleted successfully.";
    }

    // 2. Assign shift.   5 
    String assignShift(int id, Shift shift) {
        Employee e = findEmployee(id);
        if (e == null) return "Employee not found.";

        e.shift = shift;
        return "Shift assigned successfully.";
    }

    // 3. Check in (user enters the hour, e.g. 9.5 for 9:30 AM).  6 
    String checkIn(int id, double time) {
        Employee e = findEmployee(id);
        if (e == null) return "Employee not found.";
        if (e.shift == null) return "Assign a shift first.";

        for (Attendance a : records) {
            if (a.employeeId == id && a.checkOut == -1)
                return "Employee is already checked in.";
        }

        records.add(new Attendance(e, time));
        return "Check-in recorded.";
    }

    // 3. Check out. 7
    String checkOut(int id, double time) {
        for (Attendance a : records) {
            if (a.employeeId == id && a.checkOut == -1) {
                a.checkOut = time;
                return "Check-out recorded.";
            }
        }
        return "No active check-in found.";
    }

    // 4. Calculate worked hours          8  time utils
    String calculateHours(int id) {
        StringBuilder result = new StringBuilder();
        int count = 1;
        for (Attendance a : records) {
            if (a.employeeId == id) {
                result.append(String.format("Record %-4d %8s hours\n",
                        count++, String.format("%.2f", a.workedHours())));
            }
        }
        return result.length() == 0 ? "No records found."
                : title("WORKED HOURS - Employee ID " + id) + result;
    }

    // 5. Detect irregularities   9 
    // Each shift has an expected start hour. If check-in is more than
    // 0.2 hours (about 10 mins) late, or worked hours < 8, flag it.
    String irregularities() {
        StringBuilder result = new StringBuilder();

        for (Attendance a : records) {
            if (a.shift == null) continue;

            double expectedStart;
            if (a.shift == Shift.MORNING) expectedStart = 6;
            else if (a.shift == Shift.EVENING) expectedStart = 14;
            else expectedStart = 22;

            if (a.checkIn > expectedStart + 0.2) {
                result.append("  *  ").append(a.name).append(" checked in late.\n");
            }

            if (a.checkOut != -1 && a.workedHours() < 8) {
                result.append("  *  ").append(a.name).append(" worked less than 8 hours.\n");
            }
        }
        return result.length() == 0 ? "No irregularities found."
                : title("IRREGULARITIES") + result;
    }

    // 6. Search attendance by employee ID.     
    String searchByEmployee(int id) {
        StringBuilder result = new StringBuilder();
        for (Attendance a : records) {
            if (a.employeeId == id) result.append(a).append("\n");
        }
        return result.length() == 0 ? "No records found."
                : title("ATTENDANCE - Employee ID " + id)
                + Attendance.header() + "\n" + divider() + result;
    }

    // 6. Search attendance by shift
    String searchByShift(Shift shift) {
        StringBuilder result = new StringBuilder();
        for (Attendance a : records) {
            if (a.shift == shift) result.append(a).append("\n");
        }
        return result.length() == 0 ? "No records found."
                : title("ATTENDANCE - " + shift + " Shift")
                + Attendance.header() + "\n" + divider() + result;
    }

    // 7. Generate report

    String generateReport() {
        report.clear();

        for (Employee e : employees) {
            double total = 0;
            for (Attendance a : records) {
                if (a.employeeId == e.id) total += a.workedHours();
            }
            report.put(e.id, String.format("%-22s %-12s %8s", e.name,
                    (e.shift == null) ? "Not assigned" : e.shift.toString(),
                    String.format("%.2f", total)));
        }

        StringBuilder result = new StringBuilder(title("EMPLOYEE SHIFT REPORT"));
        result.append(String.format("%-6s %-22s %-12s %8s\n",
                "ID", "NAME", "SHIFT", "HOURS"));
        result.append(divider());
        for (Integer id : report.keySet()) {
            result.append(String.format("%-6d ", id))
                  .append(report.get(id)).append("\n");
        }
        return result.toString();
    }

    String viewEmployees() {
        StringBuilder result = new StringBuilder();
        for (Employee e : employees) result.append(e).append("\n");
        return result.length() == 0 ? "No employees registered."
                : title("REGISTERED EMPLOYEES")
                + String.format("%-6s %-22s %-12s\n", "ID", "NAME", "SHIFT")
                + divider() + result;
    }

    String viewAttendance() {
        StringBuilder result = new StringBuilder();
        for (Attendance a : records) result.append(a).append("\n");
        return result.length() == 0 ? "No attendance records."
                : title("ATTENDANCE RECORDS")
                + Attendance.header() + "\n" + divider() + result;
    }

    String viewShifts() {
        return title("SHIFT TIMINGS") +
                String.format("%-10s %s\n", "MORNING", shiftTimings.get(Shift.MORNING)) +
                String.format("%-10s %s\n", "EVENING", shiftTimings.get(Shift.EVENING)) +
                String.format("%-10s %s", "NIGHT", shiftTimings.get(Shift.NIGHT));
    }
}

// Swing GUI
public class EmployeeShiftGUI extends JFrame {  // inheritance 
    EmployeeManager manager = new EmployeeManager();
    JTextArea output = new JTextArea();

    EmployeeShiftGUI() {
        setTitle("Employee Shift & Attendance System");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(243, 244, 246));

        JLabel heading = new JLabel(
                "<html><center>Employee Shift & Attendance Management"
                + "<br><span style='font-size:11px; font-weight:normal'>"
                + "Register staff, assign shifts and track attendance</span></center></html>",
                JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(Color.WHITE);
        heading.setOpaque(true);
        heading.setBackground(new Color(44, 62, 80));
        heading.setBorder(new EmptyBorder(18, 10, 18, 10));
        add(heading, BorderLayout.NORTH);

        // Monospaced font keeps table columns aligned; margin gives breathing room
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        output.setMargin(new Insets(18, 22, 18, 22));
        output.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(new CompoundBorder(
                new EmptyBorder(12, 12, 6, 12),
                new TitledBorder(new LineBorder(new Color(208, 213, 220), 1, true),
                        "  Output  ", TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 13), new Color(90, 100, 115))));
        add(scroll, BorderLayout.CENTER);
        show("Welcome! Choose an option below to get started.");

        JPanel buttons = new JPanel(new GridLayout(5, 3, 12, 12));
        buttons.setBorder(new EmptyBorder(10, 14, 14, 14));
        buttons.setBackground(new Color(243, 244, 246));

        addButton(buttons, "Register Employee", this::registerEmployee);
        addButton(buttons, "View Employees",
                () -> show(manager.viewEmployees()));
        addButton(buttons, "Update Employee", this::updateEmployee);
        addButton(buttons, "Delete Employee", () -> {
            Integer id = getId();
            if (id != null) show(manager.deleteEmployee(id));
        });
        addButton(buttons, "Assign Shift", this::assignShift);
        addButton(buttons, "View Shifts",
                () -> show(manager.viewShifts()));
        addButton(buttons, "Check In", this::checkIn);
        addButton(buttons, "Check Out", this::checkOut);
        addButton(buttons, "Worked Hours", () -> {
            Integer id = getId();
            if (id != null) show(manager.calculateHours(id));
        });
        addButton(buttons, "Irregularities",
                () -> show(manager.irregularities()));
        addButton(buttons, "Search by Employee", () -> {
            Integer id = getId();
            if (id != null) show(manager.searchByEmployee(id));
        });
        addButton(buttons, "Search by Shift", this::searchShift);
        addButton(buttons, "View Attendance",
                () -> show(manager.viewAttendance()));
        addButton(buttons, "Shift Report",
                () -> show(manager.generateReport()));

        add(buttons, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void addButton(JPanel panel, String label, Runnable action) {
        Color normal = new Color(58, 92, 130);   // one steel-blue colour for all buttons
        Color hover = new Color(41, 128, 185);

        JButton button = new JButton(label);
        button.setFont(new Font("Arial", Font.PLAIN, 15));
        button.setPreferredSize(new Dimension(200, 46));
        button.setForeground(Color.WHITE);
        button.setBackground(normal);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(new Color(44, 70, 100), 1, true));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hover); }
            public void mouseExited(MouseEvent e) { button.setBackground(normal); }
        });
        button.addActionListener(e -> action.run());
        panel.add(button);
    }

    void show(String message) {
        if (message.contains("\n")) {
            // Tables and lists: normal monospaced text
            output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
            output.setForeground(new Color(40, 45, 55));
            output.setText(message);
        } else {
            // One-line status messages: big, coloured, with an icon
            boolean success = message.contains("successfully")
                    || message.contains("recorded") || message.startsWith("Welcome");
            boolean error = message.contains("not found") || message.contains("Enter")
                    || message.contains("Please") || message.contains("already")
                    || message.contains("Assign") || message.contains("must")
                    || message.contains("No active");

            String icon = success ? "\u2714  " : (error ? "\u2716  " : "\u2139  ");
            Color color = success ? new Color(46, 125, 50)
                    : (error ? new Color(183, 28, 28) : new Color(69, 90, 100));

            output.setFont(new Font("Arial", Font.PLAIN, 19));
            output.setForeground(color);
            output.setText("\n" + icon + message);
        }
        output.setCaretPosition(0); // always show the top of the result
    }

    Integer getId() {
        String input = JOptionPane.showInputDialog(this, "Enter employee ID:");
        if (input == null) return null;
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            show("Please enter a numeric employee ID.");
            return null;
        }
    }

    Double getTime(String message) {
        String input = JOptionPane.showInputDialog(this, message);
        if (input == null) return null;
        try {
            return TimeUtil.parse(input);
        } catch (Exception e) {
            show("Please enter time like 9:30 AM or 5:45 PM.");
            return null;
        }
    }

    void registerEmployee() {
        JTextField id = new JTextField();
        JTextField name = new JTextField();
        Object[] fields = {"Employee ID:", id, "Employee Name:", name};

        int choice = JOptionPane.showConfirmDialog(
                this, fields, "Register Employee",
                JOptionPane.OK_CANCEL_OPTION);

        if (choice == JOptionPane.OK_OPTION) {
            try {
                show(manager.addEmployee(
                        Integer.parseInt(id.getText().trim()),
                        name.getText()));
            } catch (NumberFormatException e) {
                show("Employee ID must be a number.");
            }
        }
    }

    void updateEmployee() {
        Integer id = getId();
        if (id == null) return;

        String name = JOptionPane.showInputDialog(this, "Enter new name:");
        if (name != null) show(manager.updateEmployee(id, name));
    }

    void assignShift() {
        Integer id = getId();
        if (id == null) return;

        Shift shift = (Shift) JOptionPane.showInputDialog(
                this, "Choose shift:", "Assign Shift",
                JOptionPane.QUESTION_MESSAGE, null,
                Shift.values(), Shift.MORNING);

        if (shift != null) show(manager.assignShift(id, shift));
    }

    void checkIn() {
        Integer id = getId();
        if (id == null) return;

        Double time = getTime("Enter check-in time (e.g. 9:30 AM):");
        if (time != null) show(manager.checkIn(id, time));
    }

    void checkOut() {
        Integer id = getId();
        if (id == null) return;

        Double time = getTime("Enter check-out time (e.g. 5:30 PM):");
        if (time != null) show(manager.checkOut(id, time));
    }

    void searchShift() {
        Shift shift = (Shift) JOptionPane.showInputDialog(
                this, "Choose shift:", "Search Attendance",
                JOptionPane.QUESTION_MESSAGE, null,
                Shift.values(), Shift.MORNING);

        if (shift != null) show(manager.searchByShift(shift));
    }

    public static void main(String[] args) {
        // Cross-platform look so the button colours show on every system
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // ignore and use the default look
        }
        SwingUtilities.invokeLater(EmployeeShiftGUI::new);
    }
}