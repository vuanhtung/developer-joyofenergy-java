package uk.tw.energy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final MeterReadingService meterReadingService;
    private final PricePlanService pricePlanService;
    private final AccountService accountService;

    public UserController(MeterReadingService meterReadingService,
                          PricePlanService pricePlanService,
                          AccountService accountService) {
        this.meterReadingService = meterReadingService;
        this.pricePlanService = pricePlanService;
        this.accountService = accountService;
    }

    @GetMapping("/{smartMeterId}/cost")
    public ResponseEntity<BigDecimal> getCost(@PathVariable String smartMeterId) {
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
        if (pricePlanId == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<List<ElectricityReading>> readings = meterReadingService.getReadings(smartMeterId);
        if (!readings.isPresent() || readings.get().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Instant threshold = Instant.now().minus(Duration.ofDays(7));
        List<ElectricityReading> effectiveReadings = readings.get().stream()
                .filter(r -> !r.getTime().isBefore(threshold))
                .collect(Collectors.toList());

        Optional<PricePlan> pricePlan = pricePlanService.getPricePlan(pricePlanId);
        if (!pricePlan.isPresent()) {
            return ResponseEntity.internalServerError().build();
        }

        BigDecimal cost = pricePlanService.calculateCost(effectiveReadings, pricePlan.get());

        return ResponseEntity.ok(cost);
    }
}
