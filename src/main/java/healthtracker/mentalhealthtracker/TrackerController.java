package healthtracker.mentalhealthtracker;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TrackerController {
    private final Map<LocalDate, EntryData> userData = new TreeMap<>();
    private final String[] MOOD_TYPES = {"Happy", "Sad", "Angry", "Calm", "Anxious", "Energetic"};
    @FXML
    private TextArea finalVerdict;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextArea journalArea;
    @FXML
    private HBox moodButtonsBox;
    @FXML
    private Slider moodSlider;
    @FXML
    private Spinner<Integer> screenTimeSpinner;
    @FXML
    private Spinner<Integer> sleepTimeSpinner;
    @FXML
    private LineChart<String, Number> moodChart;
    @FXML
    private Button showPreviousEntriesButton;
    @FXML
    private TextArea nutritionSuggestion;
    @FXML
    private TextArea workoutSuggestion;
    @FXML
    private TextArea mentalHealthInfo;
    @FXML
    private Button showScreenTimeStatsButton;
    @FXML
    private Button showSleepTimeStatsButton;
    private String currentUser;
    private ToggleGroup moodToggleGroup;

    private static TextArea getTextArea(Map.Entry<LocalDate, EntryData> entry) {
        TextArea entryArea = new TextArea();
        entryArea.setEditable(false);
        entryArea.setWrapText(true);
        EntryData data = entry.getValue();
        String entryText = String.format(
                "Date: %s\nMood: %s (%d/10)\nMental Health Index: %.1f/10\nScreen Time: %d hours\nSleep Time: %d hours\nJournal Entry: %s",
                entry.getKey(), data.moodType, data.moodRating, data.mentalHealthIndex, data.screenTime,
                data.sleepTime, data.entry);
        entryArea.setText(entryText);
        return entryArea;
    }

    private Map<String, Double> calculateWeeklyAverages(
            java.util.function.Function<EntryData, Integer> valueExtractor) {
        return userData.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> {
                            LocalDate date = entry.getKey();
                            return date.getYear() + "-W" + String.format("%02d",
                                    date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()));
                        },
                        TreeMap::new,
                        Collectors.averagingDouble(entry -> valueExtractor.apply(entry.getValue()))));
    }

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());
        setupMoodButtons();
        setupSpinners();
        setupChartStyle();
        setupMentalHealthInfo();
    }

    private void setupMentalHealthInfo() {
        mentalHealthInfo.setText(
                """
                        Understanding Your Mental Health
                        =====================================
                        The Mental Health Index (0-10) is a comprehensive measure of your daily mental well-being. It combines multiple
                        factors to give you a holistic view of your mental health status.
                        Index Components:
                        ----------------
                        • Mood Rating (40%)
                          Your daily emotional state serves as the primary indicator of your mental well-being.
                          The higher your mood rating, the better your mental health score.
                        • Sleep Quality (30%)
                          Research shows that 7-9 hours of sleep is optimal for mental health.
                          - Less than 7 hours: May impact cognitive function and emotional regulation
                          - 7-9 hours: Ideal range for mental restoration
                          - Over 9 hours: Might indicate other health concerns
                        • Screen Time (30%)
                          Managing screen time is crucial for mental wellness.
                          - Under 4 hours: Optimal for mental health
                          - Over 4 hours: May contribute to stress and anxiety
                        Mood Type Adjustments:
                        --------------------
                        Your overall index is fine-tuned based on your mood type:
                        • Happy/Calm: +2 points
                          Positive emotions contribute to better mental health
                        • Energetic: +1 point
                          Active engagement with life is beneficial
                        • Neutral: No adjustment
                          A baseline emotional state
                        • Sad/Anxious: -1 point
                          These emotions may indicate need for self-care
                        • Angry: -2 points
                          Intense negative emotions warrant attention
                        Why Tracking Matters:
                        ------------------
                        Regular monitoring of your mental health helps you:
                        1. Identify patterns and triggers affecting your well-being
                        2. Recognize early warning signs of stress or burnout
                        3. Make informed decisions about lifestyle changes
                        4. Monitor the effectiveness of self-care practices
                        5. Track progress over time
                        Important Note:
                        -------------
                        This tool is designed for self-reflection and personal growth. It should not replace
                        professional medical advice. If you're experiencing persistent mental health challenges,
                        please reach out to a qualified mental health professional.
                        Remember: Your mental health journey is unique, and it's okay to seek help when needed.
                        """);
        mentalHealthInfo.setStyle("""
                -fx-control-inner-background: #f8f9fa;
                -fx-font-size: 13px;
                -fx-border-color: #dee2e6;
                -fx-border-radius: 5px;
                -fx-padding: 15px;
                -fx-font-family: 'Arial';
                """);
    }

    private void setupChartStyle() {
        CategoryAxis xAxis = (CategoryAxis) moodChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) moodChart.getYAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Value");
        xAxis.setTickLabelRotation(45);
        moodChart.setAnimated(false);
        moodChart.setCreateSymbols(true);
        moodChart.setStyle("""
                -fx-padding: 10;
                -fx-background-color: #f5f5f5;
                """);
        moodChart.lookup(".chart-plot-background").setStyle(
                "-fx-background-color: white;");
        xAxis.setStyle("""
                -fx-tick-label-fill: #424242;
                -fx-tick-label-font-size: 10px;
                """);
        yAxis.setStyle("""
                -fx-tick-label-fill: #424242;
                -fx-tick-label-font-size: 10px;
                """);
    }

    @SuppressWarnings("unused")
    private void setupMoodButtons() {
        moodToggleGroup = new ToggleGroup();
        for (String moodType : MOOD_TYPES) {
            ToggleButton moodButton = new ToggleButton(moodType);
            moodButton.setToggleGroup(moodToggleGroup);
            String baseStyle = """
                    -fx-padding: 8 15;
                    -fx-background-radius: 5;
                    -fx-background-color: white;
                    -fx-border-color: #cccccc;
                    -fx-border-radius: 5;
                    -fx-cursor: hand;
                    -fx-text-fill: black;
                    """;
            String hoverStyle = """
                    -fx-padding: 8 15;
                    -fx-background-radius: 5;
                    -fx-background-color: #f0f0f0;
                    -fx-border-color: #cccccc;
                    -fx-border-radius: 5;
                    -fx-cursor: hand;
                    -fx-text-fill: black;
                    """;
            String selectedStyle = """
                    -fx-padding: 8 15;
                    -fx-background-radius: 5;
                    -fx-background-color: #2196f3;
                    -fx-border-color: #cccccc;
                    -fx-border-radius: 5;
                    -fx-cursor: hand;
                    -fx-text-fill: white;
                    """;
            moodButton.setStyle(baseStyle);
            moodButton.setOnMouseEntered(e -> {
                if (!moodButton.isSelected()) {
                    moodButton.setStyle(hoverStyle);
                }
            });
            moodButton.setOnMouseExited(e -> {
                if (!moodButton.isSelected()) {
                    moodButton.setStyle(baseStyle);
                }
            });
            moodButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    moodButton.setStyle(selectedStyle);
                } else {
                    moodButton.setStyle(baseStyle);
                }
            });
            moodButtonsBox.getChildren().add(moodButton);
        }
    }

    private void setupSpinners() {
        screenTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 24, 0));
        sleepTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 24, 8));
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @FXML
    private void saveEntry() {
        LocalDate date = datePicker.getValue();
        ToggleButton selectedMoodButton = (ToggleButton) moodToggleGroup.getSelectedToggle();
        String moodType = selectedMoodButton != null ? selectedMoodButton.getText() : "Not specified";
        int moodRating = (int) moodSlider.getValue();
        int screenTime = screenTimeSpinner.getValue();
        int sleepTime = sleepTimeSpinner.getValue();
        String entry = journalArea.getText();
        if (screenTime > 24 || sleepTime > 24) {
            showAlert("Please enter valid input");
            return;
        }
        EntryData entryData = new EntryData(moodType, moodRating, screenTime, sleepTime, entry);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentUser + "_entries.txt", true))) {
            writer.write(String.format("%s,%s,%d,%d,%d,%s,%.2f",
                    date, moodType, moodRating, screenTime, sleepTime,
                    entry.replace("\n", "\\n"), entryData.mentalHealthIndex));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        userData.put(date, entryData);
        showAlert("Entry saved successfully!");
        finalVerdict.setText(giveFinalVerdict(entryData));
        nutritionSuggestion.setText(suggestNutrition(entryData));
        workoutSuggestion.setText(suggestWorkout(entryData));
        updateMoodChart();
    }

    public void loadUserData() {
        userData.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(currentUser + "_entries.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 7);
                LocalDate date = LocalDate.parse(parts[0]);
                String moodType = parts[1];
                int moodRating = Integer.parseInt(parts[2]);
                int screenTime = Integer.parseInt(parts[3]);
                int sleepTime = Integer.parseInt(parts[4]);
                String entry = parts[5].replace("\\n", "\n");
                EntryData entryData = new EntryData(moodType, moodRating, screenTime, sleepTime, entry);
                userData.put(date, entryData);
            }
        } catch (IOException e) {
            // File might not exist yet, which is fine for new users
        }
        updateMoodChart();
    }

    @SuppressWarnings("unchecked")
    private void updateMoodChart() {
        moodChart.getData().clear();
        CategoryAxis xAxis = (CategoryAxis) moodChart.getXAxis();
        xAxis.getCategories().clear();
        XYChart.Series<String, Number> moodRatingSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> screenTimeSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> sleepTimeSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> mentalHealthIndexSeries = new XYChart.Series<>();
        moodRatingSeries.setName("Mood Rating");
        screenTimeSeries.setName("Screen Time");
        sleepTimeSeries.setName("Sleep Time");
        mentalHealthIndexSeries.setName("Mental Health Index");
        if (userData.isEmpty()) {
            return;
        }
        List<LocalDate> dateList = new ArrayList<>(userData.keySet());
        Collections.sort(dateList);
        List<String> formattedDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        for (LocalDate date : dateList) {
            formattedDates.add(formatter.format(date));
        }
        xAxis.setCategories(FXCollections.observableArrayList(formattedDates));
        for (int i = 0; i < dateList.size(); i++) {
            LocalDate date = dateList.get(i);
            String dateStr = formattedDates.get(i);
            EntryData data = userData.get(date);
            moodRatingSeries.getData().add(new XYChart.Data<>(dateStr, data.moodRating));
            screenTimeSeries.getData().add(new XYChart.Data<>(dateStr, data.screenTime));
            sleepTimeSeries.getData().add(new XYChart.Data<>(dateStr, data.sleepTime));
            mentalHealthIndexSeries.getData().add(new XYChart.Data<>(dateStr, data.mentalHealthIndex));
        }
        moodChart.getData().addAll(moodRatingSeries, screenTimeSeries, sleepTimeSeries, mentalHealthIndexSeries);
        String[] colors = {"#2196f3", "#4caf50", "#ff9800", "#9c27b0"};
        Platform.runLater(() -> {
            for (int i = 0; i < moodChart.getData().size(); i++) {
                XYChart.Series<String, Number> series = moodChart.getData().get(i);
                String color = colors[i];
                if (series.getNode() != null) {
                    series.getNode().setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
                }
                Node legendSymbol = moodChart.lookup(".chart-legend-item-symbol.series" + i);
                if (legendSymbol != null) {
                    legendSymbol.setStyle("-fx-background-color: " + color + ";");
                }
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle(
                                "-fx-background-color: " + color + ", white;" +
                                        "-fx-background-insets: 0, 2;" +
                                        "-fx-background-radius: 5px;" +
                                        "-fx-padding: 5px;");
                    }
                }
            }
        });
    }

    @FXML
    private void showScreenTimeStats() {
        Stage stage = new Stage();
        stage.setTitle("Screen Time Statistics");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Average Screen Time by Week");
        xAxis.setLabel("Week");
        yAxis.setLabel("Hours");
        Map<String, Double> weeklyAverages = calculateWeeklyAverages(entry -> entry.screenTime);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Screen Time");
        TextArea statsText = weekAverage(chart, weeklyAverages, series);
        DoubleSummaryStatistics stats = userData.values().stream()
                .mapToDouble(data -> data.screenTime)
                .summaryStatistics();
        statsText.setText(String.format("""
                        Overall Screen Time Statistics:
                        Average: %.2f hours
                        Minimum: %.2f hours
                        Maximum: %.2f hours
                        Number of entries: %d
                        Number of weeks: %d""",
                stats.getAverage(), stats.getMin(), stats.getMax(),
                stats.getCount(), weeklyAverages.size()));
        VBox layout = new VBox(10);
        layout.getChildren().addAll(chart, statsText);
        Scene scene = new Scene(layout, 600, 500);
        stage.setScene(scene);
        stage.show();
    }

    private TextArea weekAverage(LineChart<String, Number> chart, Map<String, Double> weeklyAverages, XYChart.Series<String, Number> series) {
        weeklyAverages.forEach((week, avg) -> {
            String weekLabel = formatWeekLabel(week);
            series.getData().add(new XYChart.Data<>(weekLabel, avg));
        });
        chart.getData().add(series);
        chart.setAnimated(false);
        TextArea statsText = new TextArea();
        statsText.setEditable(false);
        statsText.setPrefRowCount(3);
        statsText.setWrapText(true);
        return statsText;
    }

    @FXML
    private void showSleepTimeStats() {
        Stage stage = new Stage();
        stage.setTitle("Sleep Time Statistics");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Average Sleep Time by Week");
        xAxis.setLabel("Week");
        yAxis.setLabel("Hours");
        Map<String, Double> weeklyAverages = calculateWeeklyAverages(entry -> entry.sleepTime);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Sleep Time");
        TextArea statsText = weekAverage(chart, weeklyAverages, series);
        DoubleSummaryStatistics stats = userData.values().stream()
                .mapToDouble(data -> data.sleepTime)
                .summaryStatistics();
        statsText.setText(String.format("""
                        Overall Sleep Time Statistics:
                        Average: %.2f hours
                        Minimum: %.2f hours
                        Maximum: %.2f hours
                        Number of entries: %d
                        Number of weeks: %d""",
                stats.getAverage(), stats.getMin(), stats.getMax(),
                stats.getCount(), weeklyAverages.size()));
        VBox layout = new VBox(10);
        layout.getChildren().addAll(chart, statsText);
        Scene scene = new Scene(layout, 600, 500);
        stage.setScene(scene);
        stage.show();
    }

    private String formatWeekLabel(String weekYear) {
        String[] parts = weekYear.split("-W");
        return String.format("Week %s\n%s", parts[1], parts[0]);
    }

    private void showAlert(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(s);
        alert.showAndWait();
    }

    private String giveFinalVerdict(EntryData entryData) {
        StringBuilder verdict = new StringBuilder("Mental Health Assessment:\n\n");
        List<String> recommendations = new ArrayList<>();
        if (entryData.sleepTime < 7) {
            recommendations.add("""
                    Your sleep duration is below the recommended 7-9 hours. Consider:
                    • Setting a consistent bedtime routine
                    • Creating a dark, quiet sleep environment
                    • Avoiding screens 1-2 hours before bed
                    • Using relaxation techniques like deep breathing before sleep""");
        } else if (entryData.sleepTime > 9) {
            recommendations.add(
                    """
                            While getting enough sleep is important, sleeping more than 9 hours regularly might indicate:
                            • Potential depression or underlying health issues
                            • Poor sleep quality
                            Consider:
                            • Maintaining a consistent wake-up time
                            • Getting exposure to natural light in the morning
                            • Consulting a healthcare provider if oversleeping persists""");
        }
        switch (entryData.moodType) {
            case "Angry" -> recommendations.add("""
                    To manage anger effectively:
                    • Practice the 5-5-5 breathing technique (inhale 5s, hold 5s, exhale 5s)
                    • Step away from triggering situations when possible
                    • Express feelings through writing or talking to someone trusted
                    • Try progressive muscle relaxation""");
            case "Anxious" -> recommendations.add("""
                    To reduce anxiety:
                    • Practice grounding techniques (name 5 things you can see, 4 you can touch, etc.)
                    • Limit caffeine and sugar intake
                    • Try anxiety-reducing apps or guided meditations
                    • Break large tasks into smaller, manageable steps""");
            case "Sad" -> recommendations.add("""
                    To improve your mood:
                    • Reach out to friends or family for support
                    • Engage in activities you usually enjoy, even if you don't feel like it
                    • Spend time in nature or get some sunlight
                    • Consider journaling about your feelings""");
        }
        if (entryData.screenTime > 4) {
            recommendations.add("""
                    Your screen time is higher than recommended. Try:
                    • Using the 20-20-20 rule (every 20 minutes, look 20 feet away for 20 seconds)
                    • Setting specific screen-free times during the day
                    • Using apps to monitor and limit screen time
                    • Finding offline alternatives for entertainment""");
        }
        if (entryData.moodRating < 5) {
            recommendations.add("""
                    To improve your low mood:
                    • Set small, achievable goals for the day
                    • Practice self-compassion and avoid self-criticism
                    • Consider scheduling an appointment with a mental health professional
                    • Try mood-tracking to identify patterns and triggers""");
        }
        if (recommendations.isEmpty()) {
            verdict.append("""
                    Your mental health appears to be in good shape! To maintain this:
                    • Continue your current healthy habits
                    • Stay connected with your support system
                    • Monitor any changes in your mood or sleep patterns
                    • Practice preventive self-care""");
        } else {
            verdict.append("Here are personalized suggestions to improve your well-being:\n\n");
            for (int i = 0; i < recommendations.size(); i++) {
                verdict.append(i + 1).append(". ").append(recommendations.get(i)).append("\n\n");
            }
        }
        return verdict.toString().trim();
    }

    private String suggestNutrition(EntryData entryData) {
        StringBuilder suggestion = new StringBuilder("Nutrition Suggestions:\n\n");
        suggestion.append("""
                General Guidelines:
                • Stay hydrated (aim for 8 glasses of water daily)
                • Include a variety of colorful fruits and vegetables
                • Choose whole grains over refined grains
                
                """);
        switch (entryData.moodType) {
            case "Sad" -> suggestion.append("""
                    For improving mood:
                    • Increase omega-3 rich foods (salmon, walnuts, flaxseeds)
                    • Add vitamin D sources (fatty fish, eggs, fortified foods)
                    • Include B-vitamin rich foods (leafy greens, legumes)
                    • Dark chocolate (70%+ cocoa) can help boost mood
                    
                    """);
            case "Anxious" -> suggestion.append("""
                    For reducing anxiety:
                    • Include magnesium-rich foods (spinach, almonds, avocados)
                    • Add foods high in L-theanine (green tea, mushrooms)
                    • Choose complex carbs (oats, quinoa, sweet potatoes)
                    • Limit caffeine and processed sugars
                    
                    """);
            case "Angry" -> suggestion.append("""
                    For mood stability:
                    • Include foods rich in vitamin B6 (bananas, chickpeas)
                    • Add tryptophan sources (turkey, eggs, cheese)
                    • Choose calming herbs (chamomile, lavender tea)
                    • Avoid stimulants and processed foods
                    
                    """);
        }
        if (entryData.sleepTime < 7) {
            suggestion.append("""
                    For better sleep:
                    • Include foods with natural melatonin (cherries, kiwis)
                    • Add magnesium-rich foods (pumpkin seeds, bananas)
                    • Consider calming teas (chamomile, valerian root)
                    • Avoid heavy meals 2-3 hours before bedtime
                    
                    """);
        }
        if (entryData.screenTime > 4) {
            suggestion.append("""
                    For eye health:
                    • Increase foods rich in vitamin A (carrots, sweet potatoes)
                    • Add foods high in lutein (spinach, kale)
                    • Include omega-3 fatty acids for eye health
                    • Stay hydrated to prevent eye strain
                    
                    """);
        }
        return suggestion.toString().trim();
    }

    private String suggestWorkout(EntryData entryData) {
        StringBuilder suggestion = new StringBuilder("Exercise Recommendations:\n\n");
        suggestion.append("""
                General Guidelines:
                • Aim for 20 minutes of moderate activity per day
                • Include both cardio and strength training
                • Always warm up and cool down properly
                
                """);
        switch (entryData.moodType) {
            case "Anxious" -> suggestion.append("""
                    For anxiety relief:
                    • Try slow-paced yoga (suggestions: Child's pose, Cat-Cow, Forward Fold)
                    • Practice mindful walking for 15-20 minutes
                    • Do gentle stretching routines
                    • Consider tai chi or qigong
                    
                    """);
            case "Sad" -> suggestion.append("""
                    For mood elevation:
                    • Start with 10-minute walk, gradually increase duration
                    • Try rhythmic exercises like swimming or cycling
                    • Join group exercise classes for social interaction
                    • Dance to your favorite music
                    
                    """);
            case "Angry" -> suggestion.append("""
                    For stress relief:
                    • High-intensity exercises like boxing or running
                    • Strength training with proper form
                    • Outdoor activities for fresh air
                    • End workouts with calming stretches
                    
                    """);
            case "Energetic" -> suggestion.append("""
                    To channel energy:
                    • Try H.I.I.T. workouts (30 seconds work, 30 seconds rest)
                    • Consider sports like tennis or basketball
                    • Challenge yourself with new workout routines
                    • Mix cardio with strength training
                    
                    """);
        }
        if (entryData.sleepTime < 7) {
            suggestion.append("""
                    For better sleep:
                    • Exercise earlier in the day, not close to bedtime
                    • Try evening stretching or gentle yoga
                    • Practice relaxation exercises
                    • Include walking after meals
                    
                    """);
        }
        if (entryData.screenTime > 4) {
            suggestion.append("""
                    To reduce screen time:
                    • Take movement breaks every hour
                    • Do desk exercises (neck rolls, shoulder shrugs)
                    • Try standing or walking meetings
                    • Use exercise as screen breaks
                    
                    """);
        }
        suggestion.append("""
                Remember:
                • Listen to your body and adjust intensity as needed
                • Stay hydrated before, during, and after exercise
                • Consider working with a fitness professional for proper form
                • Celebrate small improvements and be consistent""");
        return suggestion.toString().trim();
    }

    @FXML
    private void showPreviousEntries() {
        VBox entriesBox = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(entriesBox);
        scrollPane.setFitToWidth(true);
        for (Map.Entry<LocalDate, EntryData> entry : userData.entrySet()) {
            TextArea entryArea = getTextArea(entry);
            entriesBox.getChildren().add(entryArea);
        }
        Stage stage = new Stage();
        stage.setTitle("Previous Entries");
        stage.setScene(new Scene(scrollPane, 400, 600));
        stage.show();
    }

    private static class EntryData {
        String moodType;
        int moodRating;
        int screenTime;
        int sleepTime;
        double mentalHealthIndex;
        String entry;

        EntryData(String moodType, int moodRating, int screenTime, int sleepTime, String entry) {
            this.moodType = moodType;
            this.moodRating = moodRating;
            this.screenTime = screenTime;
            this.sleepTime = sleepTime;
            this.entry = entry;
            this.mentalHealthIndex = calculateMentalHealthIndex();
        }

        private double calculateMentalHealthIndex() {
            double moodScore = moodRating / 10.0;
            double sleepScore;
            if (sleepTime >= 7 && sleepTime <= 9) {
                sleepScore = 1.0;
            } else if (sleepTime < 7) {
                sleepScore = sleepTime / 7.0;
            } else {
                sleepScore = 1.0 - ((sleepTime - 9) / 15.0);
            }
            double finalIndex = getFinalIndex(moodScore, sleepScore);
            return finalIndex * 10;
        }

        private double getFinalIndex(double moodScore, double sleepScore) {
            double screenTimeScore = screenTime <= 4 ? 1.0 : Math.max(0, 1.0 - ((screenTime - 4) / 20.0));
            double moodTypeImpact = switch (moodType) {
                case "Happy", "Calm" -> 0.2;
                case "Energetic" -> 0.1;
                case "Sad", "Anxious" -> -0.1;
                case "Angry" -> -0.2;
                default -> 0.0;
            };
            double baseIndex = (moodScore * 0.4) + (sleepScore * 0.3) + (screenTimeScore * 0.3);
            return Math.max(0, Math.min(1, baseIndex + moodTypeImpact));
        }
    }
}