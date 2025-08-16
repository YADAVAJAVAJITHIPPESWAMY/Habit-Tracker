import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class HabitTracker implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SAVE_FILE = "habit_data_gui.dat";

    private final LinkedHashMap<String, Habit> habits = new LinkedHashMap<>();
    private final Set<LocalDate> wordOfTheDayShown = new HashSet<>();
    private int totalScore = 0;
    private int level = 1;
    private int nextTitleMilestone = 100;

    private JFrame frame;
    private DefaultListModel<String> habitListModel;

    private static final String[] QUOTES = {
            "Small steps every day lead to big results.",
            "Discipline is the bridge between goals and achievement.",
            "Your future is built by what you do today, not tomorrow.",
            "Consistency beats motivation every time.",
            "One day or day one. You decide."
    };

    private static final String[] DIALOGUES = {
            "🚀 'Don’t underestimate the power of a common man!' – Chennai Express",
            "🔥 'With great power comes great responsibility.' – Spider-Man",
            "⚡ 'Why so serious?' – The Dark Knight",
            "💪 'I’ll be back.' – Terminator",
            "✨ 'May the Force be with you.' – Star Wars",
            "🦁 'Hakuna Matata!' – The Lion King",
            "🎯 'Get busy living, or get busy dying.' – The Shawshank Redemption",
            "🌟 'It’s not who I am underneath, but what I do that defines me.' – Batman Begins",
            "❤️ 'Love you 3000.' – Avengers: Endgame",
            "🏆 'Winners never quit, and quitters never win.'"
    };

    private static final String[][] VOCAB = {
            {"Serendipity", "Finding something good without looking for it"},
            {"Ephemeral", "Lasting for a very short time"},
            {"Resilience", "Ability to recover quickly from difficulties"},
            {"Euphoria", "A feeling of intense happiness"},
            {"Meticulous", "Showing great attention to detail"},
            {"Gratitude", "The quality of being thankful"},
            {"Tenacity", "Ability to keep going despite challenges"},
            {"Clarity", "The quality of being clear and easy to understand"},
            {"Fortitude", "Courage in pain or adversity"},
            {"Harmony", "A pleasing arrangement or peaceful state"}
    };

    private static final String[] JOKES = {
            "😂 Why don’t programmers like nature? Too many bugs!",
            "🤣 Parallel lines have so much in common… It’s a shame they’ll never meet.",
            "😅 Why did the scarecrow win an award? Because he was outstanding in his field!",
            "🫠 Debugging: Removing the needles from the haystack.",
            "😜 I told my computer I needed a break, and it froze!"
    };

    private static final String[][] RIDDLES = {
            {"I speak without a mouth and hear without ears. What am I?", "An echo"},
            {"What has keys but can’t open locks?", "A piano"},
            {"The more you take, the more you leave behind. What is it?", "Footsteps"},
            {"What has to be broken before you can use it?", "An egg"},
            {"I’m tall when I’m young and short when I’m old. What am I?", "A candle"}
    };

    private static final String[] TITLES = {
            "🌞 Early Bird", "🔥 Streak Master", "💎 Discipline Royalty",
            "🎯 Focus Guru", "🏆 Consistency Champion", "🌟 Productivity Ninja"
    };

    private static final Random RNG = new Random();

    // ====== Main ======
    public static void main(String[] args) {
        HabitTracker tracker = load();
        tracker.createGUI();
    }

    // ====== GUI Creation ======
    private void createGUI() {
        frame = new JFrame("Personal Habit Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLayout(new BorderLayout());

        // Habit List
        habitListModel = new DefaultListModel<>();
        habits.keySet().forEach(habitListModel::addElement);
        JList<String> habitList = new JList<>(habitListModel);
        JScrollPane scrollPane = new JScrollPane(habitList);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        JButton addButton = new JButton("Add Habit");
        JButton logButton = new JButton("Log Progress");
        JButton reportButton = new JButton("Weekly Report");
        JButton deleteButton = new JButton("Delete Habit");
        JButton exitButton = new JButton("Save & Exit");

        buttonPanel.add(addButton);
        buttonPanel.add(logButton);
        buttonPanel.add(reportButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(exitButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        addButton.addActionListener(e -> addHabit());
        logButton.addActionListener(e -> logProgress());
        reportButton.addActionListener(e -> weeklyReport());
        deleteButton.addActionListener(e -> deleteHabit());
        exitButton.addActionListener(e -> { save(); frame.dispose(); });

        frame.setVisible(true);
    }

    // ====== Habit Operations ======
    private void addHabit() {
        String name = JOptionPane.showInputDialog(frame, "Enter habit name:");
        if (name != null && !name.isEmpty() && !habits.containsKey(name)) {
            Habit h = new Habit(name);
            habits.put(name, h);
            habitListModel.addElement(name);
        }
    }

    private void logProgress() {
        if (habits.isEmpty()) { JOptionPane.showMessageDialog(frame, "No habits to log."); return; }
        LocalDate today = LocalDate.now();
        for (Habit h : habits.values()) {
            if (h.hasLogForDate(today)) continue; // Skip if already logged

            int res = JOptionPane.showConfirmDialog(frame, "Did you complete '" + h.name + "' today?", "Log Progress", JOptionPane.YES_NO_OPTION);
            boolean done = (res == JOptionPane.YES_OPTION);
            h.addLogForDate(today, done);

            if (done) {
                totalScore += h.pointsPerDone;
                randomQuote();
                checkAwards(h);
            } else {
                totalScore -= 5;
            }
            updateLevel();
        }
        showWordOfTheDay(); // Daily word on login
    }

    private void weeklyReport() {
        if (habits.isEmpty()) { JOptionPane.showMessageDialog(frame, "No habits to report."); return; }
        StringBuilder sb = new StringBuilder("----- Weekly Report -----\n");
        Habit champion = null;
        LocalDate today = LocalDate.now();

        for (Habit h : habits.values()) {
            int last7 = h.countLastNDays(7, today);
            sb.append(h.name).append(" -> ").append(last7).append("/7 days | Streak: ").append(h.streak).append("\n");
            if (champion == null || last7 > champion.countLastNDays(7, today)) champion = h;
        }
        sb.append("Champion Habit: ").append(champion != null ? champion.name : "N/A").append("\n");
        sb.append("Total Score: ").append(totalScore).append(" | Level: ").append(level);
        JOptionPane.showMessageDialog(frame, sb.toString());
    }

    // ====== Delete Habit ======
    private void deleteHabit() {
        if (habits.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No habits to delete.");
            return;
        }

        String habitToDelete = (String) JOptionPane.showInputDialog(
                frame,
                "Select habit to delete:",
                "Delete Habit",
                JOptionPane.PLAIN_MESSAGE,
                null,
                habits.keySet().toArray(),
                null
        );

        if (habitToDelete != null && habits.containsKey(habitToDelete)) {
            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to delete '" + habitToDelete + "'?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                habits.remove(habitToDelete);
                habitListModel.removeElement(habitToDelete);
                JOptionPane.showMessageDialog(frame, "Habit '" + habitToDelete + "' deleted.");
            }
        }
    }

    // ====== Awards / Dialogues / Jokes / Word ======
    private void checkAwards(Habit h) {
        if (h.streak == 3) awardDialogue();
        if (h.totalCompletions > 0 && h.totalCompletions % 10 == 0) awardJoke();
        if (h.totalCompletions > 0 && h.totalCompletions % 15 == 0) awardRiddle();
        if (totalScore >= nextTitleMilestone) awardTitle();
    }

    private void awardDialogue() { JOptionPane.showMessageDialog(frame, "🎬 Award: " + pick(DIALOGUES)); }
    private void awardJoke() { JOptionPane.showMessageDialog(frame, pick(JOKES)); }
    private void awardRiddle() {
        String[] r = pick(RIDDLES);
        String guess = JOptionPane.showInputDialog(frame, "🧠 Riddle: " + r[0] + "\nYour guess (or cancel to see answer):");
        JOptionPane.showMessageDialog(frame, "Answer: " + r[1]);
    }
    private void awardTitle() { JOptionPane.showMessageDialog(frame, "🏅 Title Unlocked: " + pick(TITLES)); nextTitleMilestone += 100; }
    private void randomQuote() { JOptionPane.showMessageDialog(frame, "💡 " + pick(QUOTES)); }

    private void showWordOfTheDay() {
        LocalDate today = LocalDate.now();
        if (!wordOfTheDayShown.contains(today)) {
            String[] w = pick(VOCAB);
            JOptionPane.showMessageDialog(frame, "📖 Word of the Day: " + w[0] + " → " + w[1]);
            wordOfTheDayShown.add(today);
        }
    }

    private void updateLevel() {
        if (totalScore >= 500) level = 5;
        else if (totalScore >= 300) level = 4;
        else if (totalScore >= 200) level = 3;
        else if (totalScore >= 100) level = 2;
        else level = 1;
    }

    private static String pick(String[] arr) { return arr[RNG.nextInt(arr.length)]; }
    private static String[] pick(String[][] arr) { return arr[RNG.nextInt(arr.length)]; }

    // ====== Persistence ======
    private void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(this);
        } catch (IOException e) { System.err.println("Failed to save: " + e.getMessage()); }
    }

    private static HabitTracker load() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            Object o = in.readObject();
            if (o instanceof HabitTracker) return (HabitTracker) o;
        } catch (Exception ignored) {}
        return new HabitTracker();
    }

    // ====== Habit Class ======
    private static class Habit implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, category = "Lifestyle", difficulty = "Easy";
        int streak = 0, bestStreak = 0, totalCompletions = 0, pointsPerDone = 10;
        Map<LocalDate, Boolean> logs = new HashMap<>();

        Habit(String name) { this.name = name; }

        void addLogForDate(LocalDate date, boolean done) {
            logs.put(date, done);
            if (done) { streak++; totalCompletions++; bestStreak = Math.max(bestStreak, streak); }
            else streak = 0;
        }

        boolean hasLogForDate(LocalDate date) { return logs.getOrDefault(date, false); }

        int countLastNDays(int n, LocalDate today) {
            int count = 0;
            for (int i = 0; i < n; i++) if (logs.getOrDefault(today.minusDays(i), false)) count++;
            return count;
        }
    }
}
