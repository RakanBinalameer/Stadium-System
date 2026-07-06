import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SeatPanel extends JPanel {
    JTextField txtSeatNum, txtRowNum;
    JComboBox<String> comboSection;
    JTable table;
    DefaultTableModel model;

    public SeatPanel() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Seat Details"));

        txtSeatNum = new JTextField(); txtRowNum = new JTextField();
        comboSection = new JComboBox<>();

        formPanel.add(new JLabel("Seat Number (PK):")); formPanel.add(txtSeatNum);
        formPanel.add(new JLabel("Row Number:")); formPanel.add(txtRowNum);
        formPanel.add(new JLabel("Section (PK/FK):")); formPanel.add(comboSection);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search (Seat#)");
        JButton btnLoad = new JButton("Refresh");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete);
        btnPanel.add(btnSearch); btnPanel.add(btnLoad);

        model = new DefaultTableModel(new Object[]{"Seat Num", "Row", "Sec ID", "Stadium ID"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel top = new JPanel(new BorderLayout());
        top.add(formPanel, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> { loadData(); loadSections(); });

        btnSearch.addActionListener(e -> {
            String search = txtSeatNum.getText().trim();
            if (search.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter seat# to search"); return; }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "SELECT * FROM Seat WHERE Seat_Number=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(search));
                ResultSet rs = pst.executeQuery();
                model.setRowCount(0);
                while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)});
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnAdd.addActionListener(e -> {
            if (txtSeatNum.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seat Number is required!");
                return;
            }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "INSERT INTO Seat (Seat_Number, Row_Number, Seating_Section_Section_ID, Seating_Section_STADIUM_Stadium_ID) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);

                String sel = (String) comboSection.getSelectedItem();
                int secId = Integer.parseInt(sel.split(" - ")[0]);
                int stadId = Integer.parseInt(sel.substring(sel.indexOf("StadID: ") + 8, sel.indexOf("]")));

                pst.setInt(1, Integer.parseInt(txtSeatNum.getText()));
                DBUtils.setOptionalInt(pst, 2, txtRowNum.getText());
                pst.setInt(3, secId);
                pst.setInt(4, stadId);
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtSeatNum.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "UPDATE Seat SET Row_Number=? WHERE Seat_Number=? AND Seating_Section_Section_ID=? AND Seating_Section_STADIUM_Stadium_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);

                String sel = (String) comboSection.getSelectedItem();
                int secId = Integer.parseInt(sel.split(" - ")[0]);
                int stadId = Integer.parseInt(sel.substring(sel.indexOf("StadID: ") + 8, sel.indexOf("]")));

                DBUtils.setOptionalInt(pst, 1, txtRowNum.getText());
                pst.setInt(2, Integer.parseInt(txtSeatNum.getText()));
                pst.setInt(3, secId);
                pst.setInt(4, stadId);
                pst.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Seat Updated!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnDelete.addActionListener(e -> {
            try (Connection conn = DBUtils.getConnection()) {
                if (txtSeatNum.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a Seat Number to delete.");
                    return;
                }

                int seatNum = Integer.parseInt(txtSeatNum.getText());

                String sel = (String) comboSection.getSelectedItem();
                if (sel == null) {
                    JOptionPane.showMessageDialog(this, "Please select a Section.");
                    return;
                }

                int secId = Integer.parseInt(sel.split(" - ")[0]);
                int stadId = Integer.parseInt(sel.substring(sel.indexOf("StadID: ") + 8, sel.indexOf("]")));

                String sql = "DELETE FROM Seat WHERE Seat_Number=? AND Seating_Section_Section_ID=? AND Seating_Section_STADIUM_Stadium_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, seatNum); pst.setInt(2, secId); pst.setInt(3, stadId);
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtSeatNum.setText(model.getValueAt(row, 0).toString());
                    txtRowNum.setText(model.getValueAt(row, 1) != null ? model.getValueAt(row, 1).toString() : "");
                }
            }
        });

        loadData(); loadSections();
    }

    private void loadSections() {
        comboSection.removeAllItems();
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT Section_ID, Section_Name, STADIUM_Stadium_ID FROM Seating_Section");
            while (rs.next()) {
                comboSection.addItem(rs.getInt(1) + " - " + rs.getString(2) + " [StadID: " + rs.getInt(3) + "]");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadData() {
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Seat");
            model.setRowCount(0);
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)});
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
