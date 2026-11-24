package com.example.easygraph.data.model;
/** Weather forecast for one day (the node publishes 7 of them). */
public class ForecastDay {

    private final String date;          // e.g. "2026-11-17"
    private final double maxTemp;       // °C
    private final double minTemp;       // °C
    private final int rainProbability;  // 0-100 (%)
    private final int weatherCode;      // weather condition code

    public ForecastDay(String date, double maxTemp, double minTemp,
                       int rainProbability, int weatherCode) {
        this.date = date;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.rainProbability = rainProbability;
        this.weatherCode = weatherCode;
    }

    public String getDate() { return date; }
    public double getMaxTemp() { return maxTemp; }
    public double getMinTemp() { return minTemp; }
    public int getRainProbability() { return rainProbability; }
    public int getWeatherCode() { return weatherCode; }
}
