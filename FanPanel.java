import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FanPanel extends JPanel {
    JTextField txtId, txtFname, txtLname, txtPhone;
    JTable table;
    DefaultTableModel model;

    public FanPanel() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Fan Details"));

        txtId = new JTextField(); txtFname = new JTextField();
        txtLname = new JTextField(); txtPhone = new JTextField();

        formPanel.add(new JLabel("Fan ID (PK):")); formPanel.add(txtId);
        formPanel.add(new JLabel("First Name:")); formPanel.add(txtFname);
        formPanel.add(new JLabel("Last Name:")); formPanel.add(txtLname);
        formPanel.add(new JLabel("Phone:")); formPanel.add(txtPhone);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search (Phone)");
        JButton btnLoad = new JButton("Refresh");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete);
        btnPanel.add(btnSearch); btnPanel.add(btnLoad);

        model = new DefaultTableModel(new Object[]{"ID", "First Name", "Last Name", "Phone"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel top = new JPanel(new BorderLayout());
        top.add(formPanel, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> loadData());

        btnSearch.addActionListener(e -> {
            String search = txtPhone.getText().trim();
            if (search.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter phone to search"); return; }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "SELECT * FROM Fan WHERE Phone_Number LIKE ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, "%" + search + "%");
                ResultSet rs = pst.executeQuery();
                model.setRowCount(0);
                while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnAdd.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fan ID is required!");
                return;
            }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "INSERT INTO Fan (Fan_ID, Fan_Fname, Fan_Lname, Phone_Number) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtId.getText()));
                DBUtils.setOptionalString(pst, 2, txtFname.getText());
                DBUtils.setOptionalString(pst, 3, txtLname.getText());
                DBUtils.setOptionalString(pst, 4, txtPhone.getText());
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "UPDATE Fan SET Fan_Fname=?, Fan_Lname=?, Phone_Number=? WHERE Fan_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                DBUtils.setOptionalString(pst, 1, txtFname.getText());
                DBUtils.setOptionalString(pst, 2, txtLname.getText());
                DBUtils.setOptionalString(pst, 3, txtPhone.getText());
                pst.setInt(4, Integer.parseInt(txtId.getText()));
                pst.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Fan Updated!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnDelete.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                PreparedStatement pst = conn.prepareStatement("DELETE FROM Fan WHERE Fan_ID=?");
                pst.setInt(1, Integer.parseInt(txtId.getText()));
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtId.setText(model.getValueAt(row, 0).toString());
                    txtFname.setText(model.getValueAt(row, 1) != null ? model.getValueAt(row, 1).toString() : "");
                    txtLname.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                    txtPhone.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                }
            }
        });
        loadData();
    }

    private void loadData() {
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Fan");
            model.setRowCount(0);
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
