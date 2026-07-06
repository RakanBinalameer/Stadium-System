import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SeatingSectionPanel extends JPanel {
    JTextField txtSecId, txtName, txtCap, txtPrice;
    JComboBox<String> comboStadium;
    JTable table;
    DefaultTableModel model;

    public SeatingSectionPanel() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Section Details"));

        txtSecId = new JTextField(); txtName = new JTextField();
        txtCap = new JTextField(); txtPrice = new JTextField();
        comboStadium = new JComboBox<>();

        formPanel.add(new JLabel("Section ID (PK):")); formPanel.add(txtSecId);
        formPanel.add(new JLabel("Stadium (PK):")); formPanel.add(comboStadium);
        formPanel.add(new JLabel("Name:")); formPanel.add(txtName);
        formPanel.add(new JLabel("Capacity:")); formPanel.add(txtCap);
        formPanel.add(new JLabel("Price:")); formPanel.add(txtPrice);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search (Name)");
        JButton btnLoad = new JButton("Refresh");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete);
        btnPanel.add(btnSearch); btnPanel.add(btnLoad);

        model = new DefaultTableModel(new Object[]{"Sec ID", "Stadium ID", "Name", "Capacity", "Price"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel top = new JPanel(new BorderLayout());
        top.add(formPanel, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> { loadData(); loadStadiums(); });

        btnSearch.addActionListener(e -> {
            String search = txtName.getText().trim();
            if (search.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter name to search"); return; }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "SELECT * FROM Seating_Section WHERE Section_Name LIKE ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, "%" + search + "%");
                ResultSet rs = pst.executeQuery();
                model.setRowCount(0);
                while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getInt(5), rs.getString(2), rs.getInt(3), rs.getInt(4)});
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnAdd.addActionListener(e -> {
            if (txtSecId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Section ID is required!");
                return;
            }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "INSERT INTO Seating_Section (Section_ID, STADIUM_Stadium_ID, Section_Name, Capacity, Section_Price) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtSecId.getText()));
                pst.setInt(2, DBUtils.getSelectedId(comboStadium));
                DBUtils.setOptionalString(pst, 3, txtName.getText());
                DBUtils.setOptionalInt(pst, 4, txtCap.getText());
                DBUtils.setOptionalInt(pst, 5, txtPrice.getText());
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtSecId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "UPDATE Seating_Section SET Section_Name=?, Capacity=?, Section_Price=? WHERE Section_ID=? AND STADIUM_Stadium_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                DBUtils.setOptionalString(pst, 1, txtName.getText());
                DBUtils.setOptionalInt(pst, 2, txtCap.getText());
                DBUtils.setOptionalInt(pst, 3, txtPrice.getText());
                pst.setInt(4, Integer.parseInt(txtSecId.getText()));
                pst.setInt(5, DBUtils.getSelectedId(comboStadium));
                pst.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Section Updated!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnDelete.addActionListener(e -> {
            try (Connection conn = DBUtils.getConnection()) {
                int row = table.getSelectedRow();
                if (row == -1) { JOptionPane.showMessageDialog(this, "Select a row to delete"); return; }
                int secId = (int) model.getValueAt(row, 0);
                int stadId = (int) model.getValueAt(row, 1);

                PreparedStatement pst = conn.prepareStatement("DELETE FROM Seating_Section WHERE Section_ID=? AND STADIUM_Stadium_ID=?");
                pst.setInt(1, secId);
                pst.setInt(2, stadId);
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtSecId.setText(model.getValueAt(row, 0).toString());
                    txtName.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                    txtCap.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                    txtPrice.setText(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");
                }
            }
        });

        loadData(); loadStadiums();
    }

    private void loadStadiums() {
        comboStadium.removeAllItems();
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT Stadium_ID, Name FROM STADIUM");
            while (rs.next()) comboStadium.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadData() {
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Seating_Section");
            model.setRowCount(0);
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getInt(5), rs.getString(2), rs.getInt(3), rs.getInt(4)});
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
