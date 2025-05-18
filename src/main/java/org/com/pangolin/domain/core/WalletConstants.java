package org.com.pangolin.domain.core;

import java.math.BigDecimal;

public class WalletConstants {

    /**
     * Implement all the constants related to the validation rules
     */
    public  static final BigDecimal BALANCE_MARGIN_MAX = BigDecimal.valueOf(0.15);
    public  static final BigDecimal BALANCE_MARGIN_MIN = BigDecimal.valueOf(0.15);
    public  static final BigDecimal MAX_ALLOWED_BALANCE = new BigDecimal("1000000.00");
    public  static final BigDecimal MIN_ALLOWED_BALANCE = new BigDecimal("10.00");
    public  static final BigDecimal MIN_ABSOLUTE_VALUE = BigDecimal.ZERO;
    public  static final int MAX_DECIMAL_PLACES = 2;



    /**
     * Implement all the constants related to the validation message
     */
    public  static  final String BALANCE_NOT_NULL_MESSAGE = "\"The balance is mandatory.\"" ;
    public  static  final String BALANCE_MARGIN_MAX_MESSAGE = "\"The balance above the maximum %s allowed limit.\"" ;
    public  static  final String BALANCE_MARGIN_MIN_MESSAGE = "\"The balance below the minimum %s allowed limit.\"" ;
    public  static  final String PARCELS_DATA_NOT_NULL_MESSAGE = "\"The contracted data is mandatory.\"" ;
    public  static  final String PARCELS_AMOUNT_NOT_ZERO_MESSAGE = "\"The contracted amount is mandatory.\"" ;
    public  static  final String PARCELS_RATE_NOT_ZERO_MESSAGE = "\"Rate must be between 0 and 100\"" ;
    public  static  final String PARCELS_ID_NOT_NULL_MESSAGE = "\"ID cannot be null\"" ;

    /**
     * Implement all the constants related to the validation keys
     */
    public  static  final  String BALANCE_KEY = "balance";
    public  static  final  String PARCELS_KEY = "parcels";
}
