package pe.edu.upc.agroleak.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.agroleak.iam.domain.model.User;
import pe.edu.upc.agroleak.iam.domain.model.Role;
import pe.edu.upc.agroleak.alerts.application.AlertService;
import pe.edu.upc.agroleak.alerts.domain.model.*;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;
import pe.edu.upc.agroleak.farm.application.FarmService;
import pe.edu.upc.agroleak.farm.domain.model.*;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.*;
import pe.edu.upc.agroleak.monitoring.application.ReadingService;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorType;
import pe.edu.upc.agroleak.pests.application.PestObservationService;
import pe.edu.upc.agroleak.irrigation.application.ValveService;
import pe.edu.upc.agroleak.irrigation.domain.model.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Component
@ConditionalOnProperty(prefix="agroleak",name="presentation-data-enabled",havingValue="true")
public class PresentationDataLoader implements CommandLineRunner {
    private static final Logger log=LoggerFactory.getLogger(PresentationDataLoader.class);
    private final PasswordEncoder encoder; private final AlertService alerts; private final UserRepository users; private final FarmService farms;
    private final DeviceService devices; private final ReadingService readings;
    private final PestObservationService pests; private final ValveService valves;
    public static final String EMAIL = "maestro@agroleak.local";
    public PresentationDataLoader(PasswordEncoder encoder, AlertService alerts, UserRepository users,
            FarmService farms, DeviceService devices, ReadingService readings,
            PestObservationService pests, ValveService valves) {
        this.encoder=encoder;this.alerts=alerts;this.users=users;this.farms=farms;
        this.devices=devices;this.readings=readings;this.pests=pests;this.valves=valves;
    }
    @Override @Transactional
    public void run(String... args) {
        // Never promote or reset an existing account, even if its email matches.
        var user=users.findByEmail(EMAIL).orElseGet(() -> users.save(
                new User("Maestro", "AgroLeak", EMAIL, encoder.encode("123456789"), Role.ADMIN)));
        if(!user.isActive() || user.getRole()!=Role.ADMIN) return;
        var previous=SecurityContextHolder.getContext();
        var context=SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getId().toString(),null,
                List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()))));
        SecurityContextHolder.setContext(context);
        try {
            if(farms.listFarms().stream().anyMatch(f -> f.getOwnerId().equals(user.getId()) && f.getName().equals("AgroLeak Presentacion"))) return;
            var farm=farms.createFarm("AgroLeak Presentacion","Ica, Peru",BigDecimal.TEN);
            var field=farms.createField(farm.getId(),"Parcela de produccion",BigDecimal.TEN,"Datos simulados para presentacion");
            var north=farms.createSector(field.getId(),"Norte",new BigDecimal("5"),SectorStatus.ACTIVE);
            var south=farms.createSector(field.getId(),"Sur",new BigDecimal("5"),SectorStatus.ACTIVE);
            farms.createCrop(north.getId(),"Tomate","Cherry",LocalDate.now().minusDays(30),CropStatus.GROWING);
            farms.createCrop(south.getId(),"Pimiento","California",LocalDate.now().minusDays(45),CropStatus.GROWING);
            var gateway=device("Presentacion Gateway",DeviceType.GATEWAY,north.getId());
            device("Presentacion Flow Sensor",DeviceType.FLOW_SENSOR,north.getId());
            device("Presentacion Pressure Sensor",DeviceType.PRESSURE_SENSOR,north.getId());
            device("Presentacion Soil Sensor",DeviceType.SOIL_MOISTURE_SENSOR,south.getId());
            var valve=device("Presentacion Valve",DeviceType.VALVE,north.getId());
            var camera=device("Presentacion Camera",DeviceType.CAMERA,south.getId());
            // Gateway represents the single metering stream; sensor registrations
            // do not duplicate its flow samples in volume totals.
            Instant end=Instant.now().minusSeconds(1);
            // Seven days at five-minute intervals keep volume estimates coherent.
            for(int i=7*24*12;i>=0;i--) {
                Instant at=end.minusSeconds(i*300L);
                double flow=25.0+3.0*Math.sin(i*Math.PI/144);
                boolean leak=i>=24 && i<=30;
                readings.recordSnapshot(gateway.getId(),java.util.Map.of(
                        SensorType.FLOW_IN,flow,SensorType.FLOW_OUT,leak ? flow*0.65 : flow*0.97,
                        SensorType.PRESSURE,2.2+0.2*Math.sin(i*Math.PI/72),
                        SensorType.SOIL_MOISTURE,48.0+6.0*Math.cos(i*Math.PI/144)),at);
            }
            alerts.findByDevice(gateway.getId(),AlertStatus.ACTIVE).forEach(a -> {
                alerts.acknowledge(a.getId());
                alerts.resolve(a.getId());
            });
            for(int day=6;day>=0;day--) {
                pests.create(camera.getId(),day==0 ? 8 : 2,0.88,null,
                        end.minusSeconds(day*86400L),south.getId(),"APHID");
            }
            var inspection=alerts.createIfNotActive(gateway,AlertType.LOW_PRESSURE,AlertSeverity.MEDIUM,
                    "Ejemplo simulado: inspeccion de presion pendiente");
            alerts.acknowledge(inspection.getId());
            for(int cycle=0;cycle<3;cycle++) {
                var open=valves.request(valve.getId(),ValveAction.OPEN);
                valves.confirm(open.getId(),true);
                var close=valves.request(valve.getId(),ValveAction.CLOSE);
                valves.confirm(close.getId(),true);
            }
            log.info("Presentacion initialized: user={}, farmId={}, scenarioDeviceId={}",EMAIL,farm.getId(),gateway.getId());
        } finally { SecurityContextHolder.setContext(previous); }
    }
    private Device device(String name,DeviceType type,java.util.UUID sector) {
        var d=devices.create(name,"Ica",type,DeviceStatus.OFFLINE,95,"demo-1.0",LocalDate.now(),sector);
        devices.markSeen(d.getId());return d;
    }
}
