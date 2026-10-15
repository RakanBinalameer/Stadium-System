import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.Random;
import java.util.function.IntConsumer;

/**
 * Home page: an illustrated stadium banner, live counters from the database and the next event.
 * It only reads (SELECT COUNT / next event); it never changes data.
 */
public class HomePanel extends JPanel {
    private static final String[][] STATS = {
            {"Stadiums", "SELECT COUNT(*) FROM STADIUM"},
            {"Events", "SELECT COUNT(*) FROM Event"},
            {"Fans", "SELECT COUNT(*) FROM Fan"},
            {"Sponsors", "SELECT COUNT(*) FROM Sponser"},
            {"Seats", "SELECT COUNT(*) FROM Seat"},
            {"Tickets", "SELECT COUNT(*) FROM Ticket"},
    };
    private static final int[] TARGET_TAB = {1, 2, 3, 4, 7, 8};   // tab to open when a card is clicked
    private static final Color[] ACCENT = {Theme.PITCH, Theme.BLUE, new Color(0x7A4FA3), Theme.GOLD, new Color(0x2A8C8C), Theme.DANGER};

    private final StatCard[] cards = new StatCard[STATS.length];
    private final JLabel nextTitle = new JLabel("No upcoming events");
    private final JLabel nextInfo = new JLabel(" ");

    public HomePanel(IntConsumer openTab) {
        putClientProperty("themed", Boolean.TRUE);        // Theme.style() leaves this page alone
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        add(new Hero(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(26, 28, 20, 28));

        JLabel h = new JLabel("Overview");
        h.setFont(Theme.font(Font.BOLD, 18)); h.setForeground(Theme.TEXT);
        h.setAlignmentX(LEFT_ALIGNMENT);
        body.add(h);
        body.add(Box.createVerticalStrut(14));

        JPanel grid = new JPanel(new GridLayout(1, STATS.length, 16, 0));
        grid.setOpaque(false); grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        for (int i = 0; i < STATS.length; i++) {
            final int idx = i;
            cards[i] = new StatCard(STATS[i][0], ACCENT[i], () -> openTab.accept(TARGET_TAB[idx]));
            grid.add(cards[i]);
        }
        body.add(grid);
        body.add(Box.createVerticalStrut(22));

        JPanel next = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics gr) {
                Graphics2D g = (Graphics2D) gr.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE); g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.setColor(Theme.LINE); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.setColor(Theme.GOLD); g.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
                g.dispose();
            }
        };
        next.setOpaque(false); next.setAlignmentX(LEFT_ALIGNMENT);
        next.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        next.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 20));
        JLabel cap = new JLabel("NEXT EVENT");
        cap.setFont(Theme.font(Font.BOLD, 11)); cap.setForeground(Theme.MUTED);
        nextTitle.setFont(Theme.font(Font.BOLD, 20)); nextTitle.setForeground(Theme.TEXT);
        nextInfo.setFont(Theme.font(Font.PLAIN, 14)); nextInfo.setForeground(Theme.MUTED);
        JPanel text = new JPanel(new GridLayout(3, 1, 0, 2)); text.setOpaque(false);
        text.add(cap); text.add(nextTitle); text.add(nextInfo);
        next.add(text, BorderLayout.CENTER);
        body.add(next);
        body.add(Box.createVerticalStrut(26));

        JLabel qa = new JLabel("Quick actions");
        qa.setFont(Theme.font(Font.BOLD, 18)); qa.setForeground(Theme.TEXT); qa.setAlignmentX(LEFT_ALIGNMENT);
        body.add(qa);
        body.add(Box.createVerticalStrut(14));
        JPanel actions = new JPanel(new GridLayout(1, 4, 16, 0));
        actions.setOpaque(false); actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        String[][] A = {{"Add a stadium", "Register a new venue"}, {"Schedule an event", "Pick a stadium, date and time"},
                {"Register a fan", "Add a fan to the system"}, {"Issue a ticket", "Event, fan and seat"}};
        int[] at = {1, 2, 3, 8};
        for (int i = 0; i < A.length; i++) { final int t = at[i]; actions.add(new ActionTile(A[i][0], A[i][1], () -> openTab.accept(t))); }
        body.add(actions);
        body.add(Box.createVerticalGlue());

        add(body, BorderLayout.CENTER);
        refresh();
    }

    /** Re-read the counters and the next event. Called when the Home tab is shown. */
    public void refresh() {
        try (Connection conn = DBUtils.getConnection(); Statement st = conn.createStatement()) {
            for (int i = 0; i < STATS.length; i++) {
                try (ResultSet rs = st.executeQuery(STATS[i][1])) { rs.next(); cards[i].setValue(String.valueOf(rs.getInt(1))); }
            }
            String sql = "SELECT e.Event_Name, e.Event_Date, e.Event_Time, s.Name FROM Event e "
                    + "LEFT JOIN STADIUM s ON s.Stadium_ID = e.STADIUM_Stadium_ID "
                    + "WHERE e.Event_Date >= CURDATE() ORDER BY e.Event_Date, e.Event_Time LIMIT 1";
            try (ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    nextTitle.setText(rs.getString(1));
                    String stadium = rs.getString(4);
                    Time t = rs.getTime(3);
                    nextInfo.setText(rs.getDate(2) + (t != null ? "  ·  " + t.toString().substring(0, 5) : "")
                            + (stadium != null ? "  ·  " + stadium : ""));
                } else { nextTitle.setText("No upcoming events"); nextInfo.setText("Add one from the Events tab."); }
            }
        } catch (Exception ex) {
            for (StatCard c : cards) c.setValue("–");
            nextTitle.setText("Database not reachable"); nextInfo.setText("Check that MySQL is running.");
        }
    }

    // ------------------------------------------------------------------ stat card
    static final class StatCard extends JPanel {
        private final String label; private final Color accent; private String value = "–"; private boolean hover;
        StatCard(String label, Color accent, Runnable onClick) {
            this.label = label; this.accent = accent;
            setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            });
        }
        void setValue(String v) { value = v; repaint(); }
        @Override public Dimension getPreferredSize() { return new Dimension(150, 112); }
        @Override protected void paintComponent(Graphics gr) {
            Graphics2D g = (Graphics2D) gr.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), y0 = hover ? 0 : 3;
            g.setColor(new Color(0, 0, 0, hover ? 26 : 12)); g.fillRoundRect(1, y0 + 3, w - 3, h - 6, 16, 16);
            g.setColor(Color.WHITE); g.fillRoundRect(0, y0, w - 2, h - 6, 16, 16);
            g.setColor(Theme.LINE); g.drawRoundRect(0, y0, w - 2, h - 6, 16, 16);
            g.setColor(accent); g.fillRoundRect(18, y0 + 14, 28, 5, 4, 4);
            g.setColor(Theme.TEXT); g.setFont(Theme.font(Font.BOLD, 30));
            g.drawString(value, 18, y0 + 56);
            g.setColor(Theme.MUTED); g.setFont(Theme.font(Font.BOLD, 12));
            g.drawString(label.toUpperCase(), 18, y0 + 75);
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ quick action tile
    static final class ActionTile extends JPanel {
        private final String title, sub; private boolean hover;
        ActionTile(String title, String sub, Runnable onClick) {
            this.title = title; this.sub = sub;
            setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            });
        }
        @Override public Dimension getPreferredSize() { return new Dimension(150, 84); }
        @Override protected void paintComponent(Graphics gr) {
            Graphics2D g = (Graphics2D) gr.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(hover ? Theme.PITCH : Theme.PITCH_DARK); g.fillRoundRect(0, 0, w - 2, h - 2, 16, 16);
            g.setColor(Color.WHITE); g.setFont(Theme.font(Font.BOLD, 15)); g.drawString(title, 18, 34);
            g.setColor(new Color(255, 255, 255, 190)); g.setFont(Theme.font(Font.PLAIN, 12)); g.drawString(sub, 18, 56);
            g.setColor(Theme.GOLD); g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(w - 40, h / 2 - 4, w - 24, h / 2 - 4); g.drawLine(w - 30, h / 2 - 10, w - 24, h / 2 - 4); g.drawLine(w - 30, h / 2 + 2, w - 24, h / 2 - 4);
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ illustrated banner
    static final class Hero extends JPanel {
        private BufferedImage cache;
        Hero() { setPreferredSize(new Dimension(100, 284)); setOpaque(true); }

        @Override protected void paintComponent(Graphics gr) {
            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            if (cache == null || cache.getWidth() != w || cache.getHeight() != h) {
                cache = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = cache.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                scene(g, w, h);
                g.dispose();
            }
            gr.drawImage(cache, 0, 0, null);
        }

        private void scene(Graphics2D g, int w, int h) {
            // sky: night blue to a warm horizon
            g.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 0.55f, 0.82f, 1f},
                    new Color[]{new Color(0x07131F), new Color(0x0F2F45), new Color(0x1E6B5E), new Color(0xF2B134)}));
            g.fillRect(0, 0, w, h);
            Random rnd = new Random(7);
            g.setColor(new Color(255, 255, 255, 150));
            for (int i = 0; i < 70; i++) { int sx = rnd.nextInt(w), sy = rnd.nextInt((int) (h * 0.45)); int s = rnd.nextInt(3) == 0 ? 2 : 1; g.fillOval(sx, sy, s, s); }

            int horizon = (int) (h * 0.60);
            int cx = (int) (w * 0.72);                       // illustration centre (text lives on the left)
            double rx = Math.min(w * 0.28, 420), ry = h * 0.34;

            // stadium bowl: back wall + three seating tiers (upper half of ellipses)
            g.setColor(new Color(0x0A1722));
            g.fill(new Ellipse2D.Double(cx - rx - 14, horizon - ry - 26, 2 * rx + 28, 2 * ry + 52));
            Color[] tier = {new Color(0x16384A), new Color(0x1F5163), new Color(0x2B6F7C)};
            for (int t = 0; t < 3; t++) {
                double k = 1.0 - t * 0.17;
                Shape bowl = new Ellipse2D.Double(cx - rx * k, horizon - ry * k - 6, 2 * rx * k, 2 * ry * k + 12);
                g.setColor(tier[t]); g.fill(bowl);
                g.setColor(new Color(255, 255, 255, 26)); g.setStroke(new BasicStroke(1.2f)); g.draw(bowl);
            }
            // crowd: little coloured dots on the tiers
            Color[] fans = {new Color(0xF2B134), new Color(0xE9EEF0), new Color(0x3FBF8A), new Color(0xD9534F), new Color(0x7FB3FF)};
            Shape clip = g.getClip();
            g.setClip(new Rectangle(0, 0, w, horizon));
            for (int i = 0; i < 1100; i++) {
                double a = Math.PI + rnd.nextDouble() * Math.PI;
                double k = 0.60 + rnd.nextDouble() * 0.38;
                int px = (int) (cx + Math.cos(a) * rx * k), py = (int) (horizon - 6 + Math.sin(a) * ry * k);
                Color c = fans[rnd.nextInt(fans.length)];
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 170));
                g.fillOval(px, py, 3, 3);
            }
            g.setClip(clip);

            // light beams from the towers (drawn before the pitch so the grass sits on top)
            int[] towers = {(int) (cx - rx - 40), (int) (cx + rx + 40)};
            for (int tx : towers) {
                int top = (int) (horizon - ry - 70);
                g.setPaint(new GradientPaint(tx, top, new Color(255, 244, 200, 70), cx, horizon, new Color(255, 244, 200, 0)));
                Path2D beam = new Path2D.Double();
                beam.moveTo(tx - 10, top); beam.lineTo(tx + 10, top);
                beam.lineTo(cx + (tx < cx ? 150 : -150) , horizon + 20); beam.lineTo(cx - (tx < cx ? 150 : -150), horizon + 20);
                beam.closePath(); g.fill(beam);
            }

            // pitch in perspective with mowing stripes
            int top = horizon, bottom = h;
            double topHalf = rx * 0.78, botHalf = rx * 1.55;
            int stripes = 9;
            for (int i = 0; i < stripes; i++) {
                double y1 = top + (bottom - top) * Math.pow(i / (double) stripes, 1.25);
                double y2 = top + (bottom - top) * Math.pow((i + 1) / (double) stripes, 1.25);
                double h1 = topHalf + (botHalf - topHalf) * ((y1 - top) / (bottom - top));
                double h2 = topHalf + (botHalf - topHalf) * ((y2 - top) / (bottom - top));
                Path2D p = new Path2D.Double();
                p.moveTo(cx - h1, y1); p.lineTo(cx + h1, y1); p.lineTo(cx + h2, y2); p.lineTo(cx - h2, y2); p.closePath();
                g.setColor(i % 2 == 0 ? new Color(0x137A4F) : new Color(0x0E6B4A));
                g.fill(p);
            }
            // pitch lines
            g.setColor(new Color(255, 255, 255, 190)); g.setStroke(new BasicStroke(2f));
            double midY = top + (bottom - top) * 0.46;
            double midHalf = topHalf + (botHalf - topHalf) * 0.46;
            g.draw(new Line2D.Double(cx - midHalf * 0.98, midY, cx + midHalf * 0.98, midY));                    // halfway line
            double cr = midHalf * 0.22;
            g.draw(new Ellipse2D.Double(cx - cr, midY - cr * 0.34, 2 * cr, cr * 0.68));                          // centre circle
            g.fill(new Ellipse2D.Double(cx - 3, midY - 2, 6, 4));
            Path2D box = new Path2D.Double();                                                                      // far penalty box
            box.moveTo(cx - topHalf * 0.42, top); box.lineTo(cx - topHalf * 0.36, top + (bottom - top) * 0.10);
            box.lineTo(cx + topHalf * 0.36, top + (bottom - top) * 0.10); box.lineTo(cx + topHalf * 0.42, top);
            g.draw(box);
            // ball with a soft shadow
            int bx = (int) (cx - midHalf * 0.30), by = (int) (top + (bottom - top) * 0.70);
            g.setColor(new Color(0, 0, 0, 70)); g.fill(new Ellipse2D.Double(bx - 12, by + 14, 30, 8));
            g.setColor(Color.WHITE); g.fill(new Ellipse2D.Double(bx - 12, by - 12, 26, 26));
            g.setColor(new Color(0x1B2A33)); g.setStroke(new BasicStroke(1.2f)); g.draw(new Ellipse2D.Double(bx - 12, by - 12, 26, 26));
            Path2D pent = new Path2D.Double();
            for (int i = 0; i < 5; i++) { double a = -Math.PI / 2 + i * 2 * Math.PI / 5; double px = bx + 1 + Math.cos(a) * 6, py = by + 1 + Math.sin(a) * 6; if (i == 0) pent.moveTo(px, py); else pent.lineTo(px, py); }
            pent.closePath(); g.fill(pent);

            // floodlight towers: pole, lamp panel, glow
            for (int tx : towers) {
                int topY = (int) (horizon - ry - 70);
                g.setColor(new Color(0x0A1722)); g.fillRect(tx - 3, topY, 6, horizon - topY + 10);
                g.setPaint(new RadialGradientPaint(new Point2D.Double(tx, topY), 70f, new float[]{0f, 1f},
                        new Color[]{new Color(255, 240, 190, 140), new Color(255, 240, 190, 0)}));
                g.fill(new Ellipse2D.Double(tx - 70, topY - 70, 140, 140));
                g.setColor(new Color(0x142636)); g.fillRoundRect(tx - 22, topY - 16, 44, 30, 6, 6);
                g.setColor(new Color(0xFFF1C4));
                for (int r = 0; r < 3; r++) for (int c = 0; c < 4; c++) g.fillRect(tx - 18 + c * 10, topY - 12 + r * 9, 7, 6);
            }

            // text on the left
            g.setColor(Color.WHITE);
            g.setFont(Theme.font(Font.BOLD, 32));
            g.drawString("Welcome back", 36, 104);
            g.setFont(Theme.font(Font.BOLD, 32)); g.setColor(Theme.GOLD);
            g.drawString("Stadium Management", 36, 146);
            g.setColor(new Color(255, 255, 255, 215)); g.setFont(Theme.font(Font.PLAIN, 15));
            g.drawString("Stadiums, events, fans, sponsors, seating and tickets", 38, 178);
            g.drawString("in one place.", 38, 200);
            g.setColor(Theme.GOLD); g.fillRect(0, h - 4, w, 4);
        }
    }
}
