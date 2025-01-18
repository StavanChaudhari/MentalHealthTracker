module healthtracker.mentalhealthtracker {
    requires javafx.controls;
    requires javafx.fxml;


    opens healthtracker.mentalhealthtracker to javafx.fxml;
    exports healthtracker.mentalhealthtracker;
}