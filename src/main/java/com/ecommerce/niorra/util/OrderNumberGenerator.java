package com.ecommerce.niorra.util;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {

    public String generate() {

        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String random = UUID.randomUUID()
                .toString()
                .substring(0,6)
                .toUpperCase();

        return "NIO-" + date + "-" + random;
    }

}