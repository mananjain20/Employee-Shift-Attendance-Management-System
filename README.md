# Employee Shift & Attendance Management System

A Java Swing desktop application for managing employees working in multiple shifts. 

## Features

- **Employee management (CRUD):** register, view, update and delete employees
- **Shift scheduling:** assign Morning, Evening or Night shifts and view shift timings
- **Attendance recording:** check-in and check-out using normal time input (e.g. `9:30 AM`)
- **Worked hours calculation** for each attendance record
- **Irregularity detection:** flags late check-ins and shifts shorter than 8 hours
- **Search:** attendance by employee ID or by shift
- **Shift report:** total worked hours per employee, sorted by employee ID
- **Input validation and exception handling** for IDs, names and time formats

## Shift Timings

| Shift   | Timing        |
|---------|---------------|
| Morning | 6 AM - 2 PM   |
| Evening | 2 PM - 10 PM  |
| Night   | 10 PM - 6 AM  |

## Java Concepts Used

| Concept | Where |
|---|---|
| Classes, Objects, Constructors | `Employee`, `Attendance`, `EmployeeManager`, `TimeUtil` |
| Enum | `Shift` (MORNING, EVENING, NIGHT) |
| ArrayList | Stores employees |
| LinkedList | Stores attendance history |
| HashMap | Shift to timing details |
| TreeMap | Sorted shift report |
| StringBuilder | Report and table generation |
| Swing | `JFrame`, `JButton`, `JTextArea`, `JOptionPane` |
| Inheritance | `EmployeeShiftGUI extends JFrame` |
| Exception handling and validation | `NumberFormatException`, ID/time/shift checks |

## How to Run

Requires **JDK 8 or later**.

```bash
javac EmployeeShiftGUI.java
java EmployeeShiftGUI
```

## How to Use

1. Click **Register Employee** and enter an ID and name.
2. Click **Assign Shift** to give the employee a shift (required before check-in).
3. Use **Check In** and **Check Out** with times like `6:00 AM` and `2:00 PM`.
4. Use **Worked Hours**, **Irregularities**, **Search** and **Shift Report** to review data.


## Limitations and Future Work

- Data is stored in memory only and is lost when the app closes (file or database storage can be added)
- Attendance has no calendar date
- Sorting by hours worked and shift type can be added
- Night shifts crossing midnight need an adjustment to the hours calculation

## Author

Manan Jain 