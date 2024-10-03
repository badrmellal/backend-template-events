package backend.event_management_system.constant;

import java.util.List;
import java.util.Map;

public class FeesCalculator {
    private static final double STRIPE_FEE_PERCENTAGE = 0.029;
    private static final double SINGLE_PUBLISHER_COMMISSION_PERCENTAGE = 0.05;
    private static final double ORGANIZATION_COMMISSION_PERCENTAGE = 0.04;
    private static final double WISE_FEE_PERCENTAGE = 0.015;

    private static final Map<String, Double> STRIPE_FIXED_FEES = Map.of(
            "EGP", 3.00,
            "KES", 30.00,
            "ZAR", 1.50,
            "GHS", 0.30,
            "TZS", 700.00,
            "MAD", 3.00,
            "XOF", 100.00,
            "UGX", 1000.00,
            "ZMW", 3.00
    );

    public static class FeeCalculationResult {
        public double subtotal;
        public double stripeFee;
        public double commission;
        public double totalToCharge;
        public double publisherAmount;
        public double wiseFee;
        public double finalPublisherAmount;
    }

    public static FeeCalculationResult calculateFeesAndCommission(
            double price, int quantity, boolean isOrganization, String currencyCode) {

        double stripeFixedFee = STRIPE_FIXED_FEES.getOrDefault(currencyCode, 0.30);

        double subtotal = price * quantity;

        // Stripe fees for the total ticket price
        double stripeFee = (subtotal * STRIPE_FEE_PERCENTAGE) + (stripeFixedFee * quantity);

        // Commission for the total ticket sales
        double commission = subtotal * (isOrganization ? ORGANIZATION_COMMISSION_PERCENTAGE : SINGLE_PUBLISHER_COMMISSION_PERCENTAGE);

        // Total amount to charge the customer
        double totalToCharge = subtotal + stripeFee + commission;

        // Amount that would be sent to the publisher before Wise fee
        double publisherAmount = subtotal - commission;

        // Wise fee
        double wiseFee = publisherAmount * WISE_FEE_PERCENTAGE;

        // Final amount to be sent to the publisher
        double finalPublisherAmount = publisherAmount - wiseFee;

        FeeCalculationResult result = new FeeCalculationResult();
        result.subtotal = subtotal;
        result.stripeFee = stripeFee;
        result.commission = commission;
        result.totalToCharge = totalToCharge;
        result.publisherAmount = publisherAmount;
        result.wiseFee = wiseFee;
        result.finalPublisherAmount = finalPublisherAmount;

        return result;
    }

}
