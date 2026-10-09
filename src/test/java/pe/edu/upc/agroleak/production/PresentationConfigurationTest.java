package pe.edu.upc.agroleak.production;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.agroleak.alerts.application.AlertService;
import pe.edu.upc.agroleak.demo.PresentationDataLoader;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.farm.application.FarmService;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;
import pe.edu.upc.agroleak.irrigation.application.ValveService;
import pe.edu.upc.agroleak.monitoring.application.ReadingService;
import pe.edu.upc.agroleak.pests.application.PestObservationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PresentationConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withInitializer(new org.springframework.boot.test.context.ConfigDataApplicationContextInitializer())
            .withUserConfiguration(PresentationDataLoader.class)
            .withPropertyValues("spring.profiles.active=prod", "agroleak.demo-data-enabled=false")
            .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class))
            .withBean(AlertService.class, () -> mock(AlertService.class))
            .withBean(UserRepository.class, () -> mock(UserRepository.class))
            .withBean(FarmService.class, () -> mock(FarmService.class))
            .withBean(DeviceService.class, () -> mock(DeviceService.class))
            .withBean(ReadingService.class, () -> mock(ReadingService.class))
            .withBean(PestObservationService.class, () -> mock(PestObservationService.class))
            .withBean(ValveService.class, () -> mock(ValveService.class));

    @Test void productionCanCreateMasterWhileLegacyDemoIsDisabled() {
        context.withPropertyValues("agroleak.presentation-data-enabled=true").run(c -> {
            assertThat(c.getEnvironment().getActiveProfiles()).contains("prod");
            assertThat(c).hasSingleBean(PresentationDataLoader.class);
        });
    }

    @Test void productionDoesNotCreatePublicAdminByDefault() {
        context.run(c -> assertThat(c).doesNotHaveBean(PresentationDataLoader.class));
    }

    @Test void presentationCanBeDisabledIndependently() {
        context.withPropertyValues("agroleak.presentation-data-enabled=false")
                .run(c -> assertThat(c).doesNotHaveBean(PresentationDataLoader.class));
    }
}
