package uk.tw.energy;

import uk.tw.energy.domain.PricePlan;

import java.math.BigDecimal;

public final class TestData {

    private TestData() {
    }

    public static final String PRICE_PLAN_1_ID = "test-supplier";
    public static final String PRICE_PLAN_2_ID = "best-supplier";
    public static final String PRICE_PLAN_3_ID = "second-best-supplier";
    public static final PricePlan PRICE_PLAN_1 =
            new PricePlan(PRICE_PLAN_1_ID, null, BigDecimal.TEN, null);
    public static final PricePlan PRICE_PLAN_2 =
            new PricePlan(PRICE_PLAN_2_ID, null, BigDecimal.ONE, null);
    public static final PricePlan PRICE_PLAN_3 =
            new PricePlan(PRICE_PLAN_3_ID, null, BigDecimal.valueOf(2), null);

}
