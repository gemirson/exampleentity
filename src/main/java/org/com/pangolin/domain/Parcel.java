package org.com.pangolin.domain;

import org.com.pangolin.domain.core.*;
import org.com.pangolin.domain.core.entidade.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Parcel extends Entity<String,ParcelId> implements Comparable<Parcel>{

    private final BigDecimal amount;
    private final LocalDate dueDate;
    private final double rate;
    private final ValidationResult validationResult;

    protected Parcel(ParcelId id, BigDecimal amount,
                     LocalDate dueDate, double rate,
                     ValidationResult validationResult) {
        super(Objects.requireNonNull(id));
        this.amount = Objects.requireNonNull(amount);
        this.dueDate = Objects.requireNonNull(dueDate);
        this.rate = rate;
        this.validationResult = Objects.requireNonNull(validationResult);
    }

    public static Either<ValidationResult, Parcel> create(ParcelId id,
                                                          BigDecimal amount,
                                                          LocalDate dueDate,
                                                          double rate) {
        Objects.requireNonNull(id, WalletConstants.PARCELS_ID_NOT_NULL_MESSAGE);

        ValidationResult result = validate(amount, dueDate, rate);
        return result.isValid()
                ? Either.right(new Parcel(id, amount, dueDate, rate, result))
                : Either.left(result);
    }

    private static ValidationResult validate(BigDecimal amount,
                                             LocalDate dueDate,
                                             double rate) {
        Map<String, List<ValidationResult.ValidationError>> errors = new LinkedHashMap<>();

        validateAmount(amount, errors);
        validateDueDate(dueDate, errors);
        validateRate(rate, errors);

        return errors.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(errors);
    }

    private static  void validateDueDate(LocalDate contracted_parcel,Map<String, List<ValidationResult.ValidationError>> errors){
        if(contracted_parcel == null){
            addError(errors, new ValidationResult.ValidationError.Builder("PARCEL_INVALID")
                                 .withMessageKey( WalletConstants.PARCELS_DATA_NOT_NULL_MESSAGE)
                                 .build());
        }

    }
    private static  void validateAmount(BigDecimal amount,Map<String, List<ValidationResult.ValidationError>> errors){
        if(amount == null){
            addError(errors, new ValidationResult.ValidationError.Builder("PARCEL_INVALID")
                                  .withMessageKey(WalletConstants.PARCELS_AMOUNT_NOT_ZERO_MESSAGE)
                                  .build());
        }

    }


    private static void validateRate(double rate, Map<String, List<ValidationResult.ValidationError>> errors) {
        if (rate <= 0 || rate > 100) {
            addError(errors, new ValidationResult.ValidationError.Builder("PARCEL_INVALID")
                             .withMessageKey(WalletConstants.PARCELS_RATE_NOT_ZERO_MESSAGE)
                             .build());
        }
    }

    private static void addError(Map<String, List<ValidationResult.ValidationError>> errors,
                                 ValidationResult.ValidationError message) {
        errors.computeIfAbsent(WalletConstants.PARCELS_KEY, k -> new ArrayList<>()).add(message);
    }

    public ValidationResult validate() {
        return validationResult;
    }

    /**
     * Compares this parcel with another parcel for ordering.
     *
     * <p>The comparison is based on:
     * <ol>
     *   <li>Due date (chronological order)</li>
     *   <li>Parcel amount (ascending order)</li>
     *   <li>Interest rate (ascending order)</li>
     * </ol>
     *
     * <p><b>Usage Examples:</b></p>
     *
     * <pre>{@code
     * // Creating sample parcels
     * List<Parcel> parcels = Arrays.asList(
     *     new Parcel(
     *         new ParcelId("PARCEL-001"),
     *         new BigDecimal("1500.00"),
     *         LocalDate.of(2023, 12, 15),
     *         2.5,
     *         ValidationResult.valid()
     *     ),
     *     new Parcel(
     *         new ParcelId("PARCEL-002"),
     *         new BigDecimal("1200.00"),
     *         LocalDate.of(2023, 11, 20),
     *         3.0,
     *         ValidationResult.valid()
     *     )
     * );
     *
     * // Sorting using Collections.sort()
     * Collections.sort(parcels); // Orders by date, then amount, then rate
     *
     * // Sorting using Streams
     * List<Parcel> sortedParcels = parcels.stream()
     *     .sorted()
     *     .collect(Collectors.toList());
     *
     * // Sorting in reverse order
     * List<Parcel> reverseSorted = parcels.stream()
     *     .sorted(Comparator.reverseOrder())
     *     .collect(Collectors.toList());
     * }</pre>
     *
     * @param other the other parcel to compare to (must not be null)
     * @return a negative integer, zero, or a positive integer as this parcel
     *         is less than, equal to, or greater than the specified parcel
     * @throws NullPointerException if the other parcel is null
     *
     * @see java.lang.Comparable
     * @see java.util.Collections#sort(List)
     * @see java.util.stream.Stream#sorted()
     */
    @Override
    public int compareTo(Parcel parcel) {
        Objects.requireNonNull(parcel, "Cannot compare with null Parcel");

        // 1. Comparação por data de vencimento (cronológica)
        int dateComparison = this.dueDate.compareTo(parcel.dueDate);
        if (dateComparison != 0) {
            return dateComparison;
        }

        // 2. Comparação por valor (ascendente)
        int amountComparison = this.amount.compareTo(parcel.amount);
        if (amountComparison != 0) {
            return amountComparison;
        }

        // 3. Comparação por taxa (ascendente)
        return Double.compare(this.rate, parcel.rate);
    }



    /**
     * Calculates the present value of the installment using a discount rate
     *
     * @param discountRate Discount rate (in decimal, e.g., 0.05 for 5%)
     * @param contractDate Reference date for calculation
     * @return Present value of the installment
     * @throws IllegalArgumentException If rate is negative or due date is before contract date
     */
    public BigDecimal calculatePresentValue(BigDecimal discountRate, LocalDate contractDate) {

        if (discountRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount rate cannot be negative");
        }

        if (dueDate.isBefore(contractDate)) {
            throw new IllegalArgumentException("Due date cannot be before contract date");
        }

        long days = ChronoUnit.DAYS.between(contractDate, dueDate);

        // Convert to years using business year convention (360 days)
        BigDecimal period = BigDecimal.valueOf(days)
                .divide(BigDecimal.valueOf(360), 10, RoundingMode.HALF_EVEN);

        // Present value formula: PV = FV / (1 + rate)^period
        BigDecimal denominator = BigDecimal.ONE.add(discountRate)
                .pow(period.intValue());
        BigDecimal presentValue = amount.divide(denominator, 2, RoundingMode.HALF_EVEN);

        return presentValue;
    }
    @Override
    public boolean equals(Object o) {
        if(this == o) return  true;
        if (o == null || getClass() != o.getClass()) return false;
        Parcel parcel = (Parcel) o;
        return getId().equals(parcel.getId());

    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public String toString() {

      /**  return new StringJoiner(", ", Parcel.class.getSimpleName() + "[", "]")
                .add(""\id\":" + getId().toString())
                .add(""\amount\":" + amount)
                .add(""\dueDate\":" + dueDate)
                .add(""\rate\":" + rate)
                .add(""\valid\":" + validationResult.isValid())
                .toString();*/
      return  "";
    }
}
