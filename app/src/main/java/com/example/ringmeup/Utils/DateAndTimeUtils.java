package com.example.ringmeup.Utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DateAndTimeUtils {

    public static String convertToDateWordFormat(String date){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);

        // Format the date to another format if needed
        return localDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

    }

    public String getTimeWithAMAndPM(){
      return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
    }
    public static String getDateWithWordFormat(){
        return new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
    }
    public static String getDateId(){
        return  new SimpleDateFormat("MMddyyyy", Locale.getDefault()).format(new Date());
    }


    public static String parseDateToDateId(LocalDate date){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");
        return date.format(formatter);
    }

    public static String getTimeForLineChart(String time){
        String TIME = "";

        if (time.charAt(0) == '0' && time.charAt(1) == '0')
            TIME = "0";
        else if (time.charAt(0) == '0' && time.charAt(1) == '1')
            TIME = "1";
        else if (time.charAt(0) == '0' && time.charAt(1) == '2')
            TIME = "2";
        else if (time.charAt(0) == '0' && time.charAt(1) == '3')
            TIME = "3";
        else if (time.charAt(0) == '0' && time.charAt(1) == '4')
            TIME = "4";
        else if (time.charAt(0) == '0' && time.charAt(1) == '5')
            TIME = "5";
        else if (time.charAt(0) == '0' && time.charAt(1) == '6')
            TIME = "6";
        else if (time.charAt(0) == '0' && time.charAt(1) == '7')
            TIME = "7";
        else if (time.charAt(0) == '0' && time.charAt(1) == '8')
            TIME = "8";
        else if (time.charAt(0) == '0' && time.charAt(1) == '9')
            TIME = "9";
        else if (time.charAt(0) == '1' && time.charAt(1) == '0')
            TIME = "10";
        else if (time.charAt(0) == '1' && time.charAt(1) == '1')
            TIME = "11";
        else if (time.charAt(0) == '1' && time.charAt(1) == '2')
            TIME = "12";
        else if (time.charAt(0) == '1' && time.charAt(1) == '3')
            TIME = "13";
        else if (time.charAt(0) == '1' && time.charAt(1) == '4')
            TIME = "14";
        else if (time.charAt(0) == '1' && time.charAt(1) == '5')
            TIME = "15";
        else if (time.charAt(0) == '1' && time.charAt(1) == '6')
            TIME = "16";
        else if (time.charAt(0) == '1' && time.charAt(1) == '7')
            TIME = "17";
        else if (time.charAt(0) == '1' && time.charAt(1) == '8')
            TIME = "18";
        else if (time.charAt(0) == '1' && time.charAt(1) == '9')
            TIME = "19";
        else if (time.charAt(0) == '2' && time.charAt(1) == '0')
            TIME = "20";
        else if (time.charAt(0) == '2' && time.charAt(1) == '1')
            TIME = "21";
        else if (time.charAt(0) == '2' && time.charAt(1) == '2')
            TIME = "22";
        else if (time.charAt(0) == '2' && time.charAt(1) == '3')
            TIME = "23";
        else if (time.charAt(0) == '2' && time.charAt(1) == '4')
            TIME = "24";

        return  TIME;

    }

    public static LocalDate getLocalDate(String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            // Parse the date string into a LocalDate object
            LocalDate localDate = LocalDate.parse(dateString, formatter);

            return localDate;

        } catch (Exception e) {
            // Handle parsing errors
            return  null;
        }
    }

    public static LocalDate getLocalDateUsingDateId(String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");

        try {
            // Parse the date string into a LocalDate object
            LocalDate localDate = LocalDate.parse(dateString, formatter);

            return localDate;

        } catch (Exception e) {
            // Handle parsing errors
            return  null;
        }
    }
    public static String getTime24HrsFormat(){
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
    public static String getTime24HrsFormatAndDate(){
        return new SimpleDateFormat("MMMM dd yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
    public static String getTimeId(){
        return new SimpleDateFormat("HHmmss", Locale.getDefault()).format(new Date());
    }

    public static long getRemainingDays(String date){

        // Define the date format to match "MMMM d, yyyy" (e.g., September 30, 2024)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        // Parse the date string into a LocalDate
        LocalDate targetDate = LocalDate.parse(date, formatter);

        // Get the current date
        LocalDate today = LocalDate.now();

        // Calculate the remaining days
        long remainingDays = ChronoUnit.DAYS.between(today, targetDate);

        return remainingDays;
    }




}
