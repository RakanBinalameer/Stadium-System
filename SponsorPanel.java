import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SponsorPanel extends JPanel {
    JTextField txtId, txtName;
    JTable table;
    DefaultTableModel model;

    public SponsorPanel() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Sponsor Details"));
        txtId = new JTextField(); txtName = new JTextField();
        formPanel.add(new JLabel("Sponsor ID (PK):")); formPanel.add(txtId);
        formPanel.add(new JLabel("Name:")); formPanel.add(txtName);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search (Name)");
        JButton btnLoad = new JButton("Refresh");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete);
        btnPanel.add(btnSearch); btnPanel.add(btnLoad);

        model = new DefaultTableModel(new Object[]{"ID", "Name"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel top = new JPanel(new BorderLayout());
        top.add(formPanel, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> loadData());

        btnSearch.addActionListener(e -> {
            String search = txtName.getText().trim();
            if (search.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter name to search"); return; }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "SELECT * FROM Sponser WHERE Name LIKE ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, "%" + search + "%");
                ResultSet rs = pst.executeQuery();
                model.setRowCount(0);
                while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2)});
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnAdd.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sponsor ID is required!");
                return;
            }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "INSERT INTO Sponser (Sponser_ID, Name) VALUES (?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtId.getText()));
                DBUtils.setOptionalString(pst, 2, txtName.getText());
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "UPDATE Sponser SET Name=? WHERE Sponser_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                DBUtils.setOptionalString(pst, 1, txtName.getText());
                pst.setInt(2, Integer.parseInt(txtId.getText()));
                pst.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Sponsor Updated!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnDelete.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                PreparedStatement pst = conn.prepareStatement("DELETE FROM Sponser WHERE Sponser_ID=?");
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
                    txtName.setText(model.getValueAt(row, 1) != null ? model.getValueAt(row, 1).toString() : "");
                }
            }
        });
        loadData();
    }

    private void loadData() {
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Sponser");
            model.setRowCount(0);
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2)});
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
