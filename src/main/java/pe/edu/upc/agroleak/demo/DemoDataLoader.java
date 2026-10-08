package pe.edu.upc.agroleak.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.edu.upc.agroleak.iam.application.AuthService;
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
@Profile("!prod")
@ConditionalOnProperty(prefix="agroleak",name="demo-data-enabled",havingValue="true")
public class DemoDataLoader implements CommandLineRunner {
    private static final Logger log=LoggerFactory.getLogger(DemoDataLoader.class);
    private final AuthService auth; private final UserRepository users; private final FarmService farms;
    private final DeviceService devices; private final ReadingService readings;
    private final PestObservationService pests; private final ValveService valves;
    private final String email;private final String password;
    public DemoDataLoader(AuthService auth,UserRepository users,FarmService farms,DeviceService devices,
            ReadingService readings,PestObservationService pests,ValveService valves,
            @Value("${agroleak.demo.email:demo@agroleak.local}") String email,
            @Value("${agroleak.demo.password:}") String password) {
        this.auth=auth;this.users=users;this.farms=farms;this.devices=devices;this.readings=readings;
        this.pests=pests;this.valves=valves;this.email=email;this.password=password;
    }
    @Override @Transactional
    public void run(String... args) {
        var user=users.findByEmail(email).orElseGet(() -> {
            if(password.length()<8) throw new IllegalArgumentException("DEMO_PASSWORD requiere al menos 8 caracteres");
            return auth.register("Demo","AgroLeak",email,password);
        });
        if(!user.isActive()) return;
        var previous=SecurityContextHolder.getContext();
        var context=SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getId().toString(),null,
                List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()))));
        SecurityContextHolder.setContext(context);
        try {
            if(farms.listFarms().stream().anyMatch(f -> f.getOwnerId().equals(user.getId()) && f.getName().equals("AgroLeak Demo"))) return;
            var farm=farms.createFarm("AgroLeak Demo","Ica, Peru",BigDecimal.TEN);
            var field=farms.createField(farm.getId(),"Parcela Demo",BigDecimal.TEN,"Demostracion academica");
            var north=farms.createSector(field.getId(),"Norte",new BigDecimal("5"),SectorStatus.ACTIVE);
            var south=farms.createSector(field.getId(),"Sur",new BigDecimal("5"),SectorStatus.ACTIVE);
            farms.createCrop(north.getId(),"Tomate","Cherry",LocalDate.now().minusDays(30),CropStatus.GROWING);
            var gateway=device("Demo Gateway",DeviceType.GATEWAY,north.getId());
            device("Demo Flow Sensor",DeviceType.FLOW_SENSOR,north.getId());
            device("Demo Pressure Sensor",DeviceType.PRESSURE_SENSOR,north.getId());
            device("Demo Soil Sensor",DeviceType.SOIL_MOISTURE_SENSOR,south.getId());
            var valve=device("Demo Valve",DeviceType.VALVE,north.getId());
            var camera=device("Demo Camera",DeviceType.CAMERA,south.getId());
            // Gateway represents the single metering stream; sensor registrations
            // do not duplicate its flow samples in volume totals.
            Instant end=Instant.now().minusSeconds(1);
            for(int i=12;i>=0;i--) {
                Instant at=end.minusSeconds(i*300L);
                readings.recordSnapshot(gateway.getId(),java.util.Map.of(SensorType.FLOW_IN,25.0,SensorType.FLOW_OUT,24.2,
                        SensorType.PRESSURE,2.2,SensorType.SOIL_MOISTURE,48.0),at);
            }
            pests.create(camera.getId(),2,0.82,null,end,south.getId(),"APHID");
            var command=valves.request(valve.getId(),ValveAction.CLOSE);
            valves.confirm(command.getId(),true);
            log.info("Demo initialized: user={}, farmId={}, scenarioDeviceId={}",email,farm.getId(),gateway.getId());
        } finally { SecurityContextHolder.setContext(previous); }
    }
    private Device device(String name,DeviceType type,java.util.UUID sector) {
        var d=devices.create(name,"Ica",type,DeviceStatus.OFFLINE,95,"demo-1.0",LocalDate.now(),sector);
        devices.markSeen(d.getId());return d;
    }
}
