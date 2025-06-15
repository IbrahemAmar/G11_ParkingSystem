package adminGui;

import bpark_common.ClientRequest;
import client.ClientController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import utils.SceneNavigator;

import java.util.ArrayList;
import java.util.List;

public class AdminReportsController {

    private ClientController client;

    @FXML private Label labelTotalHours;
    @FXML private PieChart parkingTimePieChart;
    @FXML private BarChart<String, Number> subscribersBarChart;
    @FXML private Button btnBack;

    public void setClient(ClientController client) {
        this.client = client;
        client.setAdminReportsController(this);

        requestMonthlyParkingTime();
        requestMonthlySubscriberReport();
    }

    private void requestMonthlyParkingTime() {
        client.sendObjectToServer(new ClientRequest("get_monthly_parking_time", null));
    }

    private void requestMonthlySubscriberReport() {
        client.sendObjectToServer(new ClientRequest("get_monthly_subscriber_report", null));
    }

    @FXML
    private void handleBack(ActionEvent event) {
        AdminMainMenuController controller = SceneNavigator.navigateToAndGetController(
            event, "/adminGui/AdminMainMenu.fxml", "Admin Dashboard"
        );
        if (controller != null) controller.setClient(client);
    }

    /**
     * Update the PieChart with parking time data.
     */
    public void setParkingTimeData(int normal, int extended, int delayed) {
        int total = normal + extended + delayed;

        Platform.runLater(() -> {
            String monthName = java.time.LocalDate.now().getMonth().name();
            String formattedMonth = monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase();

            labelTotalHours.setText(
                String.format("📅 Monthly Report for %s – Total Parking Time: %d hours", formattedMonth, total)
            );

            parkingTimePieChart.getData().clear();

            PieChart.Data normalData = new PieChart.Data("Normal (" + normal + "h)", normal);
            PieChart.Data extendedData = new PieChart.Data("Extended (" + extended + "h)", extended);
            PieChart.Data delayedData = new PieChart.Data("Delayed (" + delayed + "h)", delayed);

            parkingTimePieChart.getData().addAll(normalData, extendedData, delayedData);

            for (PieChart.Data data : parkingTimePieChart.getData()) {
                Tooltip tooltip = new Tooltip(data.getName());
                Tooltip.install(data.getNode(), tooltip);
            }
        });
    }

    /**
     * Update the BarChart with daily subscriber counts.
     */
    public void setSubscribersPerDayData(List<Integer> dailyCounts) {
        Platform.runLater(() -> {
            subscribersBarChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Subscribers");

            for (int day = 1; day <= 30; day++) {
                int count = (day <= dailyCounts.size()) ? dailyCounts.get(day - 1) : 0;
                series.getData().add(new XYChart.Data<>(String.valueOf(day), count));
            }

            subscribersBarChart.getData().add(series);
        });
    }


}
