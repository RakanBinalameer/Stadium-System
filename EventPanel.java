import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EventPanel extends JPanel {
    JTextField txtEventId, txtName, txtDate, txtTime;
    JComboBox<String> comboStadium;
    JTable table;
    DefaultTableModel model;

    public EventPanel() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Event Details"));

        txtEventId = new JTextField(); txtName = new JTextField();
        txtDate = new JTextField(); txtTime = new JTextField();
        comboStadium = new JComboBox<>();

        formPanel.add(new JLabel("Event ID (PK):"));   formPanel.add(txtEventId);
        formPanel.add(new JLabel("Event Name:"));      formPanel.add(txtName);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):")); formPanel.add(txtDate);
        formPanel.add(new JLabel("Time (HH:MM:SS):"));   formPanel.add(txtTime);
        formPanel.add(new JLabel("Stadium (FK):"));      formPanel.add(comboStadium);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search (Name)");
        JButton btnRefresh = new JButton("Refresh");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete);
        btnPanel.add(btnSearch); btnPanel.add(btnRefresh);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{"ID", "Name", "Date", "Time", "Stadium ID"});
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> { loadEvents(); loadStadiums(); });

        btnSearch.addActionListener(e -> {
            String search = txtName.getText().trim();
            if (search.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter name to search"); return; }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "SELECT * FROM Event WHERE Event_Name LIKE ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, "%" + search + "%");
                ResultSet rs = pst.executeQuery();
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("Event_ID"), rs.getString("Event_Name"),
                        rs.getDate("Event_Date"), rs.getTime("Event_Time"),
                        rs.getInt("STADIUM_Stadium_ID")
                    });
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnAdd.addActionListener(e -> {
            if (txtEventId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Event ID is required!");
                return;
            }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "INSERT INTO Event (Event_ID, Event_Name, Event_Date, Event_Time, STADIUM_Stadium_ID) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtEventId.getText()));
                DBUtils.setOptionalString(pst, 2, txtName.getText());
                DBUtils.setOptionalString(pst, 3, txtDate.getText());
                DBUtils.setOptionalString(pst, 4, txtTime.getText());
                pst.setInt(5, DBUtils.getSelectedId(comboStadium));
                pst.executeUpdate();
                loadEvents();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtEventId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "UPDATE Event SET Event_Name=?, Event_Date=?, Event_Time=?, STADIUM_Stadium_ID=? WHERE Event_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                DBUtils.setOptionalString(pst, 1, txtName.getText());
                DBUtils.setOptionalString(pst, 2, txtDate.getText());
                DBUtils.setOptionalString(pst, 3, txtTime.getText());
                pst.setInt(4, DBUtils.getSelectedId(comboStadium));
                pst.setInt(5, Integer.parseInt(txtEventId.getText()));
                pst.executeUpdate();
                loadEvents();
                JOptionPane.showMessageDialog(this, "Event Updated!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnDelete.addActionListener(e -> {
            if (txtEventId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "DELETE FROM Event WHERE Event_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtEventId.getText()));
                pst.executeUpdate();
                loadEvents();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtEventId.setText(model.getValueAt(row, 0).toString());
                    txtName.setText(model.getValueAt(row, 1) != null ? model.getValueAt(row, 1).toString() : "");
                    txtDate.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                    txtTime.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                }
            }
        });
        loadEvents(); loadStadiums();
    }

    private void loadStadiums() {
        comboStadium.removeAllItems();
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT Stadium_ID, Name FROM STADIUM");
            while (rs.next()) comboStadium.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadEvents() {
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Event");
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("Event_ID"),
                    rs.getString("Event_Name"),
                    rs.getDate("Event_Date"),
                    rs.getTime("Event_Time"),
                    rs.getInt("STADIUM_Stadium_ID")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
