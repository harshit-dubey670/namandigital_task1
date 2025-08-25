package NamanDigital;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TaskManagerSwing extends JFrame {
    private DefaultTableModel tableModel;
    private JTable taskTable;
    private List<Task> tasks;
    private Timer reminderTimer;

    public TaskManagerSwing() {
        super("Task Manager - Your Personal Productivity Hub");
        tasks = new ArrayList<>();
        initializeUI();
        startReminderService();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);

        // Create main layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create header
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // Create task table
        mainPanel.add(createTaskTable(), BorderLayout.CENTER);

        // Create footer with buttons
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(new Color(76, 175, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("TASK MANAGER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.white);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        return headerPanel;
    }

    private JScrollPane createTaskTable() {
        // Create table model with columns
        String[] columnNames = {"Task", "Description", "Due Date", "Priority", "Status", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // Only status and actions columns are editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Boolean.class;
                return String.class;
            }
        };

        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(40);
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        // Custom renderer for priority column
        taskTable.getColumnModel().getColumn(3).setCellRenderer(new PriorityRenderer());

        // Custom renderer for actions column
        taskTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        taskTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(taskTable);
        return scrollPane;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footerPanel.setBackground(new Color(0,0,0));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addButton = createStyledButton("Add Task", new Color(0, 0, 128));
        JButton editButton = createStyledButton("Edit Task", new Color(33, 150, 243));
        JButton deleteButton = createStyledButton("Delete Task", new Color(255, 68, 68));
        JButton clearButton = createStyledButton("Clear Completed", new Color(102, 102, 102));

        addButton.addActionListener(e -> showAddTaskDialog());
        editButton.addActionListener(e -> editSelectedTask());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        clearButton.addActionListener(e -> clearCompletedTasks());

        footerPanel.add(addButton);
        footerPanel.add(editButton);
        footerPanel.add(deleteButton);
        footerPanel.add(clearButton);

        return footerPanel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.blue);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void showAddTaskDialog() {
        JDialog dialog = new JDialog(this, "Create New Task", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField titleField = new JTextField();
        JTextArea descriptionArea = new JTextArea(3, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        JSpinner dueDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dueDateSpinner, "MM/dd/yyyy");
        dueDateSpinner.setEditor(dateEditor);
        dueDateSpinner.setValue(new java.util.Date());

        String[] priorities = {"High", "Medium", "Low"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setSelectedItem("Medium");

        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionScroll);
        formPanel.add(new JLabel("Due Date:"));
        formPanel.add(dueDateSpinner);
        formPanel.add(new JLabel("Priority:"));
        formPanel.add(priorityCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            java.util.Date dueDate = (java.util.Date) dueDateSpinner.getValue();
            String priority = (String) priorityCombo.getSelectedItem();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a task title.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Task task = new Task(title, description, dueDate, priority);
            tasks.add(task);
            addTaskToTable(task);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addTaskToTable(Task task) {
        Object[] rowData = {
                task.getTitle(),
                task.getDescription(),
                task.getFormattedDueDate(),
                task.getPriority(),
                task.isCompleted(),
                "Edit/Delete"
        };
        tableModel.addRow(rowData);
    }

    private void editSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = tasks.get(selectedRow);
        showEditTaskDialog(task, selectedRow);
    }

    private void showEditTaskDialog(Task task, int rowIndex) {
        JDialog dialog = new JDialog(this, "Edit Task", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField titleField = new JTextField(task.getTitle());
        JTextArea descriptionArea = new JTextArea(task.getDescription(), 3, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        JSpinner dueDateSpinner = new JSpinner(new SpinnerDateModel(task.getDueDate(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dueDateSpinner, "MM/dd/yyyy");
        dueDateSpinner.setEditor(dateEditor);

        String[] priorities = {"High", "Medium", "Low"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setSelectedItem(task.getPriority());

        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionScroll);
        formPanel.add(new JLabel("Due Date:"));
        formPanel.add(dueDateSpinner);
        formPanel.add(new JLabel("Priority:"));
        formPanel.add(priorityCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            task.setTitle(titleField.getText().trim());
            task.setDescription(descriptionArea.getText().trim());
            task.setDueDate((java.util.Date) dueDateSpinner.getValue());
            task.setPriority((String) priorityCombo.getSelectedItem());

            // Update table row
            tableModel.setValueAt(task.getTitle(), rowIndex, 0);
            tableModel.setValueAt(task.getDescription(), rowIndex, 1);
            tableModel.setValueAt(task.getFormattedDueDate(), rowIndex, 2);
            tableModel.setValueAt(task.getPriority(), rowIndex, 3);

            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete: " + tasks.get(selectedRow).getTitle() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            tasks.remove(selectedRow);
            tableModel.removeRow(selectedRow);
        }
    }

    private void clearCompletedTasks() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove all completed tasks?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            for (int i = tasks.size() - 1; i >= 0; i--) {
                if (tasks.get(i).isCompleted()) {
                    tasks.remove(i);
                    tableModel.removeRow(i);
                }
            }
        }
    }

    private void startReminderService() {
        reminderTimer = new Timer(true);
        reminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < tasks.size(); i++) {
                        Task task = tasks.get(i);
                        if (task.isDueSoon() && !task.isCompleted() && !task.isNotified()) {
                            showReminder(task);
                            task.setNotified(true);
                        }
                    }
                });
            }
        }, 0, 60000); // Check every minute
    }

    private void showReminder(Task task) {
        JOptionPane.showMessageDialog(this,
                String.format("Task Reminder!\n\nTask: %s\nDue: %s\nPriority: %s",
                        task.getTitle(),
                        task.getFormattedDueDate(),
                        task.getPriority()),
                "Task Due Soon",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Custom renderer for priority column
    class PriorityRenderer extends JLabel implements TableCellRenderer {
        public PriorityRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String priority = (String) value;
            setText(priority);

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                switch (priority) {
                    case "High":
                        setBackground(new Color(255, 68, 68));
                        setForeground(Color.WHITE);
                        break;
                    case "Medium":
                        setBackground(new Color(255, 187, 51));
                        setForeground(Color.BLACK);
                        break;
                    case "Low":
                        setBackground(new Color(0, 200, 81));
                        setForeground(Color.WHITE);
                        break;
                    default:
                        setBackground(table.getBackground());
                        setForeground(table.getForeground());
                }
            }

            setFont(getFont().deriveFont(Font.BOLD));
            return this;
        }
    }

    // Button renderer and editor for actions column
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton editButton;
        private JButton deleteButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");

            editButton.setFont(new Font("Arial", Font.PLAIN, 12));
            deleteButton.setFont(new Font("Arial", Font.PLAIN, 12));

            add(editButton);
            add(deleteButton);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JPanel panel;
        protected JButton editButton;
        protected JButton deleteButton;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");

            editButton.addActionListener(e -> {
                fireEditingStopped();
                Task task = tasks.get(currentRow);
                showEditTaskDialog(task, currentRow);
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                int confirm = JOptionPane.showConfirmDialog(TaskManagerSwing.this,
                        "Are you sure you want to delete: " + tasks.get(currentRow).getTitle() + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    tasks.remove(currentRow);
                    tableModel.removeRow(currentRow);
                }
            });

            panel.add(editButton);
            panel.add(deleteButton);
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            return panel;
        }

        public Object getCellEditorValue() {
            return "Edit/Delete";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            TaskManagerSwing app = new TaskManagerSwing();
            app.setVisible(true);
        });
    }
}

class Task {
    private String title;
    private String description;
    private java.util.Date dueDate;
    private String priority;
    private boolean completed;
    private boolean notified;

    public Task(String title, String description, java.util.Date dueDate, String priority) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
        this.notified = false;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public java.util.Date getDueDate() { return dueDate; }
    public void setDueDate(java.util.Date dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }

    public String getFormattedDueDate() {
        return new java.text.SimpleDateFormat("MMM dd, yyyy").format(dueDate);
    }

    public boolean isOverdue() {
        return dueDate.before(new java.util.Date()) && !completed;
    }

    public boolean isDueSoon() {
        long diff = dueDate.getTime() - System.currentTimeMillis();
        return diff <= 24 * 60 * 60 * 1000 && diff >= 0 && !completed; // Within 24 hours
    }

    @Override
    public String toString() {
        return title;
    }
}
