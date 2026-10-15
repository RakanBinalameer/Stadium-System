import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Visual theme for the Stadium System (pure Swing, no extra libraries).
 * It only changes how the existing screens look: colours, fonts, spacing, buttons, tables and tabs.
 * The panels' database code is not touched.
 */
public final class Theme {
    private Theme() {}

    // ---- palette ----
    static final Color PITCH      = new Color(0x0E6B4A);
    static final Color PITCH_DARK = new Color(0x0A4B34);
    static final Color NAVY       = new Color(0x0F1F2E);
    static final Color GOLD       = new Color(0xF2B134);
    static final Color BG         = new Color(0xF2F5F4);
    static final Color CARD       = Color.WHITE;
    static final Color LINE       = new Color(0xD7E0DC);
    static final Color TEXT       = new Color(0x1B2A33);
    static final Color MUTED      = new Color(0x5F7078);
    static final Color BLUE       = new Color(0x2F5D8C);
    static final Color DANGER     = new Color(0xB8392E);
    static final Color ZEBRA      = new Color(0xF6F9F8);
    static final Color SELECT     = new Color(0xCFEBDD);

    static final String FONT = pickFont("Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "Arial", Font.SANS_SERIF);
    static Font font(int style, float size) { return new Font(FONT, style, 1).deriveFont(style, size); }

    private static String pickFont(String... names) {
        java.util.Set<String> have = new java.util.HashSet<>(java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String n : names) if (have.contains(n) || n.equals(Font.SANS_SERIF)) return n;
        return Font.SANS_SERIF;
    }

    private static Graphics2D g2(Graphics gr) {
        Graphics2D g2 = (Graphics2D) gr.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    // ---- global look and feel ----
    public static void install() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", font(Font.BOLD, 13));
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("Panel.background", CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.font", font(Font.BOLD, 13));
        UIManager.put("ToolTip.font", font(Font.PLAIN, 12));
    }

    // ---- header banner ----
    public static JComponent header(String title, String subtitle) {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics gr) {
                Graphics2D g = g2(gr);
                int w = getWidth(), h = getHeight();
                g.setPaint(new GradientPaint(0, 0, NAVY, w, h, PITCH_DARK));
                g.fillRect(0, 0, w, h);
                // pitch markings on the right: halfway line + centre circle
                g.setStroke(new BasicStroke(2f));
                g.setColor(new Color(255, 255, 255, 38));
                int r = h - 26, cx = w - 120, cy = h / 2;
                g.drawLine(cx, 0, cx, h);
                g.drawOval(cx - r / 2, cy - r / 2, r, r);
                g.fillOval(cx - 3, cy - 3, 6, 6);
                g.drawRect(w - 330, -2, 120, 36);
                g.drawRect(w - 330, h - 34, 120, 36);
                // gold accent line at the bottom
                g.setColor(GOLD);
                g.fillRect(0, h - 4, w, 4);
                // title
                g.setColor(Color.WHITE);
                g.setFont(font(Font.BOLD, 26));
                g.drawString(title, 28, 42);
                g.setColor(new Color(255, 255, 255, 190));
                g.setFont(font(Font.PLAIN, 13));
                g.drawString(subtitle, 29, 66);
                g.dispose();
            }
        };
        p.setPreferredSize(new Dimension(100, 88));
        return p;
    }

    // ---- recursive styling ----
    /** Style every component below (and including) c. Call once after the panels are built. */
    public static void style(Component c) {
        if (c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty("themed"))) return;   // pages that style themselves
        if (c instanceof JTabbedPane) styleTabs((JTabbedPane) c);
        else if (c instanceof JButton) styleButton((JButton) c);
        else if (c instanceof JTextField) styleField((JTextField) c);
        else if (c instanceof JComboBox) styleCombo((JComboBox<?>) c);
        else if (c instanceof JLabel) styleLabel((JLabel) c);
        else if (c instanceof JScrollPane) styleScroll((JScrollPane) c);
        else if (c instanceof JTable) styleTable((JTable) c);
        else if (c instanceof JPanel) stylePanel((JPanel) c);
        if (c instanceof Container) for (Component k : ((Container) c).getComponents()) style(k);
    }

    private static void stylePanel(JPanel p) {
        Border b = p.getBorder();
        if (b instanceof TitledBorder) {                              // the "Details" form -> a white card
            TitledBorder tb = (TitledBorder) b;
            p.setBorder(new CardBorder(tb.getTitle()));
            p.setOpaque(false);
            convertFormLayout(p);
        } else {
            p.setOpaque(false);
            if (p.getLayout() instanceof FlowLayout) {                 // the row of buttons
                FlowLayout fl = (FlowLayout) p.getLayout();
                fl.setAlignment(FlowLayout.LEFT); fl.setHgap(10); fl.setVgap(0);
                p.setBorder(new EmptyBorder(14, 2, 14, 0));
            } else if (p.getParent() instanceof JTabbedPane || p.getParent() == null) {
                p.setOpaque(true); p.setBackground(BG);
                p.setBorder(new EmptyBorder(18, 22, 18, 22));
            }
        }
    }

    /** GridLayout(n, 2) forms become: fixed label column, fixed-width field column, free space on the right. */
    private static void convertFormLayout(JPanel p) {
        if (!(p.getLayout() instanceof GridLayout) || ((GridLayout) p.getLayout()).getColumns() != 2) return;
        Component[] cs = p.getComponents();
        p.removeAll();
        p.setLayout(new GridBagLayout());
        for (int i = 0; i + 1 < cs.length; i += 2) {
            GridBagConstraints l = new GridBagConstraints();
            l.gridx = 0; l.gridy = i / 2; l.anchor = GridBagConstraints.WEST; l.insets = new Insets(6, 0, 6, 18);
            l.ipadx = 0;
            GridBagConstraints f = new GridBagConstraints();
            f.gridx = 1; f.gridy = i / 2; f.fill = GridBagConstraints.HORIZONTAL; f.insets = new Insets(6, 0, 6, 0);
            if (cs[i] instanceof JComponent) ((JComponent) cs[i]).setPreferredSize(new Dimension(190, 22));
            if (cs[i + 1] instanceof JComponent) ((JComponent) cs[i + 1]).setPreferredSize(new Dimension(420, 36));
            p.add(cs[i], l); p.add(cs[i + 1], f);
        }
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 2; filler.gridy = 0; filler.weightx = 1; filler.fill = GridBagConstraints.HORIZONTAL;
        p.add(Box.createHorizontalGlue(), filler);
    }

    private static void styleLabel(JLabel l) {
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(MUTED);
    }

    private static void styleField(JTextField t) {
        t.setFont(font(Font.PLAIN, 14));
        t.setForeground(TEXT);
        t.setBackground(Color.WHITE);
        t.setCaretColor(PITCH);
        t.setSelectionColor(SELECT);
        t.setSelectedTextColor(TEXT);
        t.setBorder(new FieldBorder(false));
        t.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { t.setBorder(new FieldBorder(true)); }
            @Override public void focusLost(FocusEvent e)   { t.setBorder(new FieldBorder(false)); }
        });
    }

    private static void styleCombo(JComboBox<?> c) {
        c.setUI(new FlatComboUI());
        c.setFont(font(Font.PLAIN, 14));
        c.setForeground(TEXT);
        c.setBackground(Color.WHITE);
        c.setBorder(new FieldBorder(false));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object v, int i, boolean sel, boolean foc) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, v, i, sel, foc);
                l.setBorder(new EmptyBorder(6, 10, 6, 10));
                l.setFont(font(Font.PLAIN, 14));
                l.setForeground(TEXT);
                l.setBackground(sel ? SELECT : Color.WHITE);
                return l;
            }
        });
    }

    private static void styleButton(JButton b) {
        String t = b.getText() == null ? "" : b.getText().toLowerCase();
        Color base, fg = Color.WHITE;
        boolean outline = false;
        if (t.startsWith("insert") || t.startsWith("link")) base = PITCH;
        else if (t.startsWith("update")) base = BLUE;
        else if (t.startsWith("delete") || t.startsWith("remove")) base = DANGER;
        else if (t.startsWith("search")) { base = GOLD; fg = NAVY; }
        else { base = Color.WHITE; fg = TEXT; outline = true; }              // Refresh / Load All
        b.putClientProperty("base", base);
        b.putClientProperty("outline", outline);
        b.setUI(new ButtonUi());
        b.setForeground(fg);
        b.setFont(font(Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(Math.max(104, b.getPreferredSize().width + 18), 38));
    }

    private static void styleScroll(JScrollPane s) {
        s.setBorder(new RoundedLine(LINE, 12));
        s.getViewport().setBackground(Color.WHITE);
        s.setOpaque(false);
        s.getVerticalScrollBar().setUI(new ThinScrollUi());
        s.getHorizontalScrollBar().setUI(new ThinScrollUi());
        s.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        s.getHorizontalScrollBar().setPreferredSize(new Dimension(10, 10));
    }

    private static void styleTable(JTable t) {
        t.setRowHeight(34);
        t.setFont(font(Font.PLAIN, 14));
        t.setForeground(TEXT);
        t.setBackground(Color.WHITE);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xEAF0ED));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setDefaultRenderer(Object.class, new ZebraRenderer());
        t.setDefaultRenderer(Number.class, new ZebraRenderer());
        t.setDefaultRenderer(Integer.class, new ZebraRenderer());
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 38));
        h.setDefaultRenderer(new HeaderRenderer());
    }

    private static void styleTabs(JTabbedPane tp) {
        tp.setUI(new TabsUi());
        tp.setFont(font(Font.BOLD, 14));
        tp.setOpaque(true);
        tp.setBackground(BG);
        tp.setForeground(MUTED);
        tp.setBorder(BorderFactory.createEmptyBorder());
    }

    // ---- renderers ----
    static final class ZebraRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setForeground(TEXT);
            setBackground(sel ? SELECT : (r % 2 == 0 ? Color.WHITE : ZEBRA));
            return this;
        }
    }

    static final class HeaderRenderer implements TableCellRenderer {
        private final JLabel l = new JLabel();
        HeaderRenderer() { l.setOpaque(true); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            l.setText(v == null ? "" : v.toString().toUpperCase());
            l.setFont(font(Font.BOLD, 12));
            l.setForeground(Color.WHITE);
            l.setBackground(NAVY);
            l.setBorder(new EmptyBorder(0, 14, 0, 14));
            return l;
        }
    }

    // ---- borders ----
    /** White rounded card with the form title inside, top-left. */
    static final class CardBorder extends AbstractBorder {
        private final String title;
        CardBorder(String title) { this.title = title == null ? "" : title; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(50, 26, 18, 26); }
        @Override public void paintBorder(Component c, Graphics gr, int x, int y, int w, int h) {
            Graphics2D g = g2(gr);
            RoundRectangle2D card = new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, 16, 16);
            g.setColor(new Color(0, 0, 0, 12)); g.fill(new RoundRectangle2D.Float(x + 1, y + 3, w - 2, h - 2, 16, 16));
            g.setColor(Color.WHITE); g.fill(card);
            g.setColor(LINE); g.setStroke(new BasicStroke(1f)); g.draw(card);
            g.setColor(PITCH); g.fillRoundRect(x + 26, y + 20, 5, 20, 3, 3);
            g.setFont(font(Font.BOLD, 16)); g.setColor(TEXT);
            g.drawString(title, x + 40, y + 36);
            g.dispose();
        }
    }

    static final class FieldBorder extends AbstractBorder {
        private final boolean focused;
        FieldBorder(boolean focused) { this.focused = focused; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(7, 12, 7, 12); }
        @Override public void paintBorder(Component c, Graphics gr, int x, int y, int w, int h) {
            Graphics2D g = g2(gr);
            g.setColor(focused ? PITCH : new Color(0xC5D1CC));
            g.setStroke(new BasicStroke(focused ? 2f : 1f));
            g.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, 10, 10));
            g.dispose();
        }
    }

    static final class RoundedLine extends AbstractBorder {
        private final Color color; private final int arc;
        RoundedLine(Color color, int arc) { this.color = color; this.arc = arc; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(2, 2, 2, 2); }
        @Override public void paintBorder(Component c, Graphics gr, int x, int y, int w, int h) {
            Graphics2D g = g2(gr);
            g.setColor(color);
            g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, arc, arc));
            g.dispose();
        }
    }

    // ---- component UIs ----
    static final class ButtonUi extends BasicButtonUI {
        @Override public void paint(Graphics gr, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel m = b.getModel();
            Color base = (Color) b.getClientProperty("base");
            boolean outline = Boolean.TRUE.equals(b.getClientProperty("outline"));
            Graphics2D g = g2(gr);
            int w = c.getWidth(), h = c.getHeight();
            Color fill = base;
            if (m.isPressed()) fill = outline ? new Color(0xE3EBE8) : base.darker();
            else if (m.isRollover()) fill = outline ? new Color(0xF0F5F3) : brighten(base);
            g.setColor(fill);
            g.fillRoundRect(0, 0, w, h, 12, 12);
            if (outline) { g.setColor(new Color(0xC5D1CC)); g.drawRoundRect(0, 0, w - 1, h - 1, 12, 12); }
            g.setFont(b.getFont()); g.setColor(b.getForeground());
            FontMetrics fm = g.getFontMetrics();
            String s = b.getText();
            g.drawString(s, (w - fm.stringWidth(s)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
        @Override public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setOpaque(false); b.setBorderPainted(false); b.setContentAreaFilled(false); b.setRolloverEnabled(true);
        }
        private static Color brighten(Color c) {
            return new Color(Math.min(255, c.getRed() + 18), Math.min(255, c.getGreen() + 18), Math.min(255, c.getBlue() + 18));
        }
    }

    static final class FlatComboUI extends BasicComboBoxUI {
        @Override protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override protected void paintComponent(Graphics gr) {
                    Graphics2D g = g2(gr);
                    int cx = getWidth() / 2, cy = getHeight() / 2;
                    g.setColor(MUTED); g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(cx - 5, cy - 2, cx, cy + 3); g.drawLine(cx, cy + 3, cx + 5, cy - 2);
                    g.dispose();
                }
            };
            b.setBorder(BorderFactory.createEmptyBorder()); b.setContentAreaFilled(false); b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(34, 30));
            return b;
        }
        @Override public void paintCurrentValueBackground(Graphics gr, Rectangle bounds, boolean hasFocus) {}
        @Override protected javax.swing.plaf.basic.ComboPopup createPopup() {
            javax.swing.plaf.basic.BasicComboPopup p = new javax.swing.plaf.basic.BasicComboPopup(comboBox);
            p.setBorder(new RoundedLine(LINE, 6));
            return p;
        }
    }

    static final class ThinScrollUi extends BasicScrollBarUI {
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        @Override protected void paintTrack(Graphics gr, JComponent c, Rectangle r) {}
        @Override protected void paintThumb(Graphics gr, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g = g2(gr);
            g.setColor(new Color(0xB5C4BE));
            g.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g.dispose();
        }
    }

    static final class TabsUi extends BasicTabbedPaneUI {
        @Override protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(11, 18, 11, 18);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(10, 22, 0, 22);
            contentBorderInsets = new Insets(0, 0, 0, 0);
        }
        @Override protected void paintTabBackground(Graphics gr, int p, int i, int x, int y, int w, int h, boolean sel) {
            Graphics2D g = g2(gr);
            g.setColor(sel ? PITCH : BG);
            if (sel) g.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 12, 12);
            g.dispose();
        }
        @Override protected void paintTabBorder(Graphics gr, int p, int i, int x, int y, int w, int h, boolean sel) {}
        @Override protected void paintFocusIndicator(Graphics gr, int p, Rectangle[] r, int i, Rectangle a, Rectangle b, boolean sel) {}
        @Override protected void paintContentBorder(Graphics gr, int p, int sel) {}
        @Override protected void paintText(Graphics gr, int p, Font font, FontMetrics fm, int i, String title, Rectangle tr, boolean sel) {
            Graphics2D g = g2(gr);
            g.setFont(font); g.setColor(sel ? Color.WHITE : MUTED);
            g.drawString(title, tr.x, tr.y + fm.getAscent());
            g.dispose();
        }
        @Override protected int calculateTabHeight(int p, int i, int fh) { return fh + 22; }
        @Override protected int getTabLabelShiftY(int p, int i, boolean sel) { return 0; }
        @Override protected int getTabLabelShiftX(int p, int i, boolean sel) { return 0; }
        @Override public void paint(Graphics gr, JComponent c) {
            gr.setColor(BG); gr.fillRect(0, 0, c.getWidth(), c.getHeight());
            super.paint(gr, c);
        }
    }
}
