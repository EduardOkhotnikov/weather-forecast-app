package com.weatherapp.model;

public class Day {
    private double maxtemp_c;
    private double mintemp_c;
    private double avghumidity;
    private double maxwind_kph;

    public double getMaxtemp_c() { return maxtemp_c; }
    public void setMaxtemp_c(double maxtemp_c) { this.maxtemp_c = maxtemp_c; }

    public double getMintemp_c() { return mintemp_c; }
    public void setMintemp_c(double mintemp_c) { this.mintemp_c = mintemp_c; }

    public double getAvghumidity() { return avghumidity; }
    public void setAvghumidity(double avghumidity) { this.avghumidity = avghumidity; }

    public double getMaxwind_kph() { return maxwind_kph; }
    public void setMaxwind_kph(double maxwind_kph) { this.maxwind_kph = maxwind_kph; }
}

