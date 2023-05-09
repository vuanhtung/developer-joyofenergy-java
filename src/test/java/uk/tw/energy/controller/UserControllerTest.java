package uk.tw.energy.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.tw.energy.TestData.PRICE_PLAN_1;
import static uk.tw.energy.TestData.PRICE_PLAN_1_ID;
import static uk.tw.energy.TestData.PRICE_PLAN_2;
import static uk.tw.energy.TestData.PRICE_PLAN_2_ID;
import static uk.tw.energy.TestData.PRICE_PLAN_3;

public class UserControllerTest {

    public static final String METER_ID_1 = "meter-id-1";
    public static final String METER_ID_2 = "meter-id-2";

    private UserController userController;
    private MeterReadingService meterReadingService;
    private PricePlanService pricePlanService;
    private AccountService accountService;

    private ElectricityReading reading1;
    private ElectricityReading reading2;
    private ElectricityReading reading3;

    @BeforeEach
    public void setUp() {
        Map<String, List<ElectricityReading>> meterAssociatedReadings = new HashMap<>();
        meterAssociatedReadings.put(METER_ID_1, new ArrayList<>());

        reading1 = new ElectricityReading(
                Instant.now().minus(Duration.ofDays(7)).minusSeconds(1), BigDecimal.valueOf(100.0));
        reading2 = new ElectricityReading(Instant.now().minusSeconds(3600), BigDecimal.valueOf(15.0));
        reading3 = new ElectricityReading(Instant.now(), BigDecimal.valueOf(5.0));

        meterAssociatedReadings.put(METER_ID_2,
                Arrays.asList(reading1, reading2, reading3));
        this.meterReadingService = new MeterReadingService(meterAssociatedReadings);

        Map<String, String> smartMeterToPricePlanAccounts = new HashMap<>();
        smartMeterToPricePlanAccounts.put(METER_ID_1, PRICE_PLAN_1_ID);
        smartMeterToPricePlanAccounts.put(METER_ID_2, PRICE_PLAN_2_ID);
        this.accountService = new AccountService(smartMeterToPricePlanAccounts);

        List<PricePlan> pricePlans = Arrays.asList(PRICE_PLAN_1, PRICE_PLAN_2, PRICE_PLAN_3);
        this.pricePlanService = new PricePlanService(pricePlans, meterReadingService);

        this.userController = new UserController(meterReadingService, pricePlanService, accountService);
    }

    @Test
    public void givenMeterIdThatIsNotRecognisedShouldReturnNotFound() {
        assertThat(userController.getCost(UUID.randomUUID().toString()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void givenMeterIdThatHasNoReadingsShouldReturnNotFound() {
        assertThat(userController.getCost(METER_ID_1).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void givenMeterIdThatHasReadingsShouldReturnOK() {
        BigDecimal expected = pricePlanService.calculateCost(
                Arrays.asList(reading2, reading3), PRICE_PLAN_2);

        assertThat(userController.getCost(METER_ID_2).getBody())
                .isEqualTo(expected);
    }
}
