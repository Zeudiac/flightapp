package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dk.cphbusiness.utils.Utils;
import lombok.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        FlightReader flightReader = new FlightReader();
        try {
            List<DTOs.FlightDTO> flightList = flightReader.getFlightsFromFile("flights.json");
            List<DTOs.FlightInfo> flightInfoList = flightReader.getFlightInfoDetails(flightList);

            flightInfoList.forEach(f->{
                System.out.println("\n"+f);
                System.out.println((Duration.between(f.getDeparture(),f.getArrival())).toMinutes());
            });
                List <DTOs.FlightInfo> filteredList = flightInfoList.stream().filter(flightInfo -> flightInfo.getAirline() != null)
                        .collect(Collectors.toList());

                Map<String, Double> averageDurationByAirline = filteredList.stream()
                        .collect(Collectors.groupingBy(DTOs.FlightInfo::getAirline,
                                Collectors.averagingDouble(flightInfo -> flightInfo.getDuration().toMinutes())));

                averageDurationByAirline.forEach((airline, avgDuration) ->
                        System.out.println("Airline: " + airline + ", Average Flight Duration: " + avgDuration + " minutes"));

            flightReader.calculateTotalFlightTimeForAirline(flightList, "Royal Jordanian");


        } catch (IOException e) {
            e.printStackTrace();
        }



    }


//    public List<FlightDTO> jsonFromFile(String fileName) throws IOException {
//        List<FlightDTO> flights = getObjectMapper().readValue(Paths.get(fileName).toFile(), List.class);
//        return flights;
//    }


    public List<DTOs.FlightInfo> getFlightInfoDetails(List<DTOs.FlightDTO> flightList) {
        List<DTOs.FlightInfo> flightInfoList = flightList.stream().map(flight -> {
            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
            DTOs.FlightInfo flightInfo = DTOs.FlightInfo.builder()
                    .name(flight.getFlight().getNumber())
                    .iata(flight.getFlight().getIata())
                    .airline(flight.getAirline().getName())
                    .duration(duration)
                    .departure(flight.getDeparture().getScheduled().toLocalDateTime())
                    .arrival(flight.getArrival().getScheduled().toLocalDateTime())
                    .origin(flight.getDeparture().getAirport())
                    .destination(flight.getArrival().getAirport())
                    .build();

            return flightInfo;
        }).toList();
        return flightInfoList;
    }

    public List<DTOs.FlightDTO> getFlightsFromFile(String filename) throws IOException {
        DTOs.FlightDTO[] flights = new Utils().getObjectMapper().readValue(Paths.get(filename).toFile(), DTOs.FlightDTO[].class);

        List<DTOs.FlightDTO> flightList = Arrays.stream(flights).toList();
        return flightList;
    }

    // New method to calculate the total flight time for a specific airline
    public Duration calculateTotalFlightTimeForAirline(List<DTOs.FlightDTO> flightList, String airlineName) {
        Duration totalFlightTime = flightList.stream()
                .filter(flight -> flight.getAirline() != null
                        && flight.getAirline().getName() != null
                        && flight.getAirline().getName().equalsIgnoreCase(airlineName))
                .map(flight -> Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled()))
                .reduce(Duration::plus)
                .orElse(Duration.ZERO); // Return zero if there are no matching flights

        System.out.println("Total flight time for " + airlineName + ": "
                + totalFlightTime.toHoursPart() + " hours "
                + totalFlightTime.toMinutesPart() + " minutes");

        return totalFlightTime;
    }
}










