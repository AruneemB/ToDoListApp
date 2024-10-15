import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TodoListApp extends JFrame
{
    private DefaultListModel<Task> todoListModel;
    private JList<Task> todoList;
    private JTextField newTaskField;
    private String abelardo;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> statusComboBox;
    private JButton editButton;

    private static final String DATA_FILE = "todo_list.dat";

    public TodoListApp()
    {
        //Set up the frame
        setTitle("To-Do List");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //Create the components
        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);
        todoList.setCellRenderer(new TaskRenderer());
        newTaskField = new JTextField(15);
        categoryComboBox = new JComboBox<>(new String[]{"School", "Personal", "Others"});
        statusComboBox = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed"});
        JButton addButton = new JButton("Add Task");
        JButton removeButton = new JButton("Remove Task");
        editButton = new JButton("Edit Task");

        //Load tasks from file
        loadTasks();

        //Set up the layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel listPanel = new JPanel(new BorderLayout(10, 10));
        listPanel.setBorder(BorderFactory.createTitledBorder("Tasks"));
        listPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Task Details"));

        JPanel topInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topInputPanel.add(new JLabel("Task:"));
        topInputPanel.add(newTaskField);
        topInputPanel.add(new JLabel("Category:"));
        topInputPanel.add(categoryComboBox);

        JPanel bottomInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomInputPanel.add(new JLabel("Status:"));
        bottomInputPanel.add(statusComboBox);
        bottomInputPanel.add(addButton);
        bottomInputPanel.add(editButton);
        bottomInputPanel.add(removeButton);

        inputPanel.add(topInputPanel);
        inputPanel.add(bottomInputPanel);

        mainPanel.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);

        //Add button actions
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String taskDescription = newTaskField.getText().trim();
                if (!taskDescription.isEmpty()) {
                    String category = (String) categoryComboBox.getSelectedItem();
                    String status = (String) statusComboBox.getSelectedItem();
                    Task task = new Task(taskDescription, category, status);
                    todoListModel.addElement(task);
                    newTaskField.setText("");
                    saveTasks();
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = todoList.getSelectedIndex();
                if (selectedIndex != -1) {
                    todoListModel.remove(selectedIndex);
                    saveTasks();
                }
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = todoList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String taskDescription = newTaskField.getText().trim();
                    if (!taskDescription.isEmpty()) {
                        String category = (String) categoryComboBox.getSelectedItem();
                        String status = (String) statusComboBox.getSelectedItem();
                        Task task = todoListModel.get(selectedIndex);
                        task.setDescription(taskDescription);
                        task.setCategory(category);
                        task.setStatus(status);
                        todoList.repaint();
                        newTaskField.setText("");
                        saveTasks();
                    }
                }
            }
        });

        todoList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && todoList.getSelectedIndex() != -1) {
                    Task task = todoList.getSelectedValue();
                    newTaskField.setText(task.getDescription());
                    categoryComboBox.setSelectedItem(task.getCategory());
                    statusComboBox.setSelectedItem(task.getStatus());
                }
            }
        });
    }

    private void loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            List<Task> tasks = (List<Task>) ois.readObject();
            for (Task task : tasks) {
                todoListModel.addElement(task);
            }
        } catch (IOException | ClassNotFoundException e) {
            // Ignore if the file does not exist or cannot be read
        }
    }

    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            List<Task> tasks = new ArrayList<>();
            for (int i = 0; i < todoListModel.getSize(); i++) {
                tasks.add(todoListModel.get(i));
            }
            oos.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TodoListApp().setVisible(true);
            }
        });
    }
}

class Task implements Serializable {
    private String description;
    private String category;
    private String status;

    public Task(String description, String category, String status) {
        this.description = description;
        this.category = category;
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return description + " (" + category + ") - " + status;
    }
}

class TaskRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Task) {
            Task task = (Task) value;
            label.setText(task.toString());
            switch (task.getStatus()) {
                case "Not Started":
                    label.setForeground(Color.RED);
                    break;
                case "In Progress":
                    label.setForeground(Color.ORANGE);
                    break;
                case "Completed":
                    Color darkGreen = new Color(2, 196, 88);
                    label.setForeground(darkGreen);
                    break;
            }
        }
        return label;
    }
}
