package com.gradekeeper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.gradekeeper.models.Class;
import com.gradekeeper.models.Grade;

public class GradeKeeperUI extends JFrame {
    private final SupabaseClient db;
    
    private JList<String> classList;
    private DefaultListModel<String> classListModel;
    private Map<String, Integer> classIdMap;
    
    private JTable gradeTable;
    private DefaultTableModel gradeTableModel;
    
    private JTextField classNameField;
    private JTextField assignmentField;
    private JTextField scoreField;
    private JTextField maxPointsField;
    
    private JLabel statusLabel;
    private int currentClassId;

    // Colors for custom styling
    private final Color HEADER_COLOR = new Color(79, 129, 189);
    private final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private final Color WARNING_COLOR = new Color(245, 124, 0);
    private final Color ERROR_COLOR = new Color(198, 40, 40);

    public GradeKeeperUI() {
        db = SupabaseClient.getInstance();
        classIdMap = new HashMap<>();
        currentClassId = -1;
        
        setTitle("Grade Keeper");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        createUI();
        refreshClassList();
    }

    private void createUI() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top Panel - Add Class
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(HEADER_COLOR, 1),
            "Add New Class",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            HEADER_COLOR
        ));
        
        JLabel classNameLabel = new JLabel("Class Name:");
        classNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topPanel.add(classNameLabel);
        
        classNameField = new JTextField(25);
        classNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topPanel.add(classNameField);
        
        JButton addClassBtn = createStyledButton("Add Class", SUCCESS_COLOR);
        addClassBtn.addActionListener(e -> addClass());
        topPanel.add(addClassBtn);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center Panel - Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(320);
        splitPane.setDividerSize(8);
        
        // Left Panel - Class List
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(HEADER_COLOR, 1),
            "Your Classes",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            HEADER_COLOR
        ));
        
        classListModel = new DefaultListModel<>();
        classList = new JList<>(classListModel);
        classList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        classList.setSelectionBackground(new Color(79, 129, 189, 80));
        classList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onClassSelected();
            }
        });
        leftPanel.add(new JScrollPane(classList), BorderLayout.CENTER);
        
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton deleteClassBtn = createStyledButton("Delete Class", ERROR_COLOR);
        deleteClassBtn.addActionListener(e -> deleteClass());
        leftButtonPanel.add(deleteClassBtn);
        
        JButton avgBtn = createStyledButton("Calculate Average", WARNING_COLOR);
        avgBtn.addActionListener(e -> showAverage());
        leftButtonPanel.add(avgBtn);
        
        leftPanel.add(leftButtonPanel, BorderLayout.SOUTH);
        
        // Right Panel - Grades
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(HEADER_COLOR, 1),
            "Grades",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            HEADER_COLOR
        ));
        
        // Add Grade Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel assignmentLabel = new JLabel("Assignment:");
        assignmentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(assignmentLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        assignmentField = new JTextField(20);
        assignmentField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(assignmentField, gbc);
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel scoreLabel = new JLabel("Score:");
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(scoreLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        scoreField = new JTextField(10);
        scoreField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(scoreField, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel maxPointsLabel = new JLabel("Max Points:");
        maxPointsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(maxPointsLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        maxPointsField = new JTextField(10);
        maxPointsField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(maxPointsField, gbc);
        
        // Row 3
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.CENTER;
        JButton addGradeBtn = createStyledButton("Add Grade", SUCCESS_COLOR);
        addGradeBtn.addActionListener(e -> addGrade());
        formPanel.add(addGradeBtn, gbc);
        
        rightPanel.add(formPanel, BorderLayout.NORTH);
        
        // Grade Table
        gradeTableModel = new DefaultTableModel(new String[]{"Assignment", "Score", "Max", "%"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        gradeTable = new JTable(gradeTableModel);
        gradeTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gradeTable.setRowHeight(30);
        gradeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        gradeTable.getTableHeader().setBackground(HEADER_COLOR);
        gradeTable.getTableHeader().setForeground(Color.WHITE);
        
        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i <= 3; i++) {
            gradeTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        rightPanel.add(new JScrollPane(gradeTable), BorderLayout.CENTER);
        
        // Delete Grade Button
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton deleteGradeBtn = createStyledButton("Delete Selected Grade", ERROR_COLOR);
        deleteGradeBtn.addActionListener(e -> deleteGrade());
        rightButtonPanel.add(deleteGradeBtn);
        rightPanel.add(rightButtonPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);
        
        // Status Bar
        statusLabel = new JLabel("Ready. Add a class to start!");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void refreshClassList() {
        classListModel.clear();
        classIdMap.clear();
        List<Class> classes = db.getClasses();
        for (Class c : classes) {
            classListModel.addElement(c.getName());
            classIdMap.put(c.getName(), c.getId());
        }
    }

    private void onClassSelected() {
        String selected = classList.getSelectedValue();
        if (selected != null) {
            currentClassId = classIdMap.get(selected);
            statusLabel.setText("Selected: " + selected);
            refreshGradeTable();
        }
    }

    private void refreshGradeTable() {
        gradeTableModel.setRowCount(0);
        if (currentClassId == -1) return;
        
        List<Grade> grades = db.getGrades(currentClassId);
        for (Grade g : grades) {
            gradeTableModel.addRow(new Object[]{
                g.getAssignment(),
                g.getScore(),
                g.getMaxPoints(),
                String.format("%.1f%%", g.getPercentage())
            });
        }
    }

    private void addClass() {
        String name = classNameField.getText().trim();
        if (name.isEmpty()) {
            showWarning("Please enter a class name.");
            return;
        }
        
        if (db.addClass(name)) {
            classNameField.setText("");
            refreshClassList();
            statusLabel.setText("Added class: " + name);
            showInfo("Class '" + name + "' added!");
        } else {
            showError("Error adding class. It may already exist.");
        }
    }

    private void deleteClass() {
        if (currentClassId == -1) {
            showWarning("Please select a class to delete.");
            return;
        }
        
        String className = classList.getSelectedValue();
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Delete class '" + className + "' and ALL its grades?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (db.deleteClass(currentClassId)) {
                currentClassId = -1;
                refreshClassList();
                refreshGradeTable();
                statusLabel.setText("Deleted class: " + className);
            }
        }
    }

    private void addGrade() {
        if (currentClassId == -1) {
            showWarning("Please select a class first.");
            return;
        }
        
        String assignment = assignmentField.getText().trim();
        String scoreStr = scoreField.getText().trim();
        String maxStr = maxPointsField.getText().trim();
        
        if (assignment.isEmpty()) {
            showWarning("Please enter an assignment name.");
            return;
        }
        
        try {
            double score = Double.parseDouble(scoreStr);
            double maxPoints = Double.parseDouble(maxStr);
            
            if (maxPoints <= 0) {
                showWarning("Max points must be greater than 0.");
                return;
            }
            if (score < 0 || score > maxPoints) {
                showWarning("Score must be between 0 and " + maxPoints + ".");
                return;
            }
            
            if (db.addGrade(currentClassId, assignment, score, maxPoints)) {
                assignmentField.setText("");
                scoreField.setText("");
                maxPointsField.setText("");
                refreshGradeTable();
                statusLabel.setText("Added grade: " + assignment);
                showInfo("Grade added successfully!");
            }
        } catch (NumberFormatException e) {
            showWarning("Please enter valid numbers for score and max points.");
        }
    }

    private void deleteGrade() {
        int selectedRow = gradeTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a grade to delete.");
            return;
        }
        
        String assignment = (String) gradeTableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Delete grade '" + assignment + "'?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            List<Grade> grades = db.getGrades(currentClassId);
            Grade selectedGrade = grades.get(selectedRow);
            if (db.deleteGrade(selectedGrade.getId())) {
                refreshGradeTable();
                statusLabel.setText("Grade deleted.");
            }
        }
    }

    private void showAverage() {
        if (currentClassId == -1) {
            showWarning("Please select a class first.");
            return;
        }
        
        Double avg = db.calculateAverage(currentClassId);
        String className = classList.getSelectedValue();
        
        if (avg == null) {
            showInfo("No grades recorded for " + className + " yet.");
        } else {
            String numericGrade = db.getNumericGrade(avg);
            String description = db.getGradeDescription(numericGrade);
            
            String gradeEmoji;
            if (numericGrade.equals("1.0") || numericGrade.equals("1.25")) gradeEmoji = "[Excellent]";
            else if (numericGrade.equals("1.5") || numericGrade.equals("1.75")) gradeEmoji = "[Very Good]";
            else if (numericGrade.equals("2.0") || numericGrade.equals("2.25")) gradeEmoji = "[Good]";
            else if (numericGrade.equals("2.5") || numericGrade.equals("2.75")) gradeEmoji = "[Satisfactory]";
            else if (numericGrade.equals("3.0")) gradeEmoji = "[Passing]";
            else gradeEmoji = "[Failed]";
            
            JOptionPane.showMessageDialog(this, 
                String.format("%s %s\n\nAverage: %.1f%%\nGrade: %s (%s)", 
                    gradeEmoji, className, avg, numericGrade, description),
                "Class Average", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}