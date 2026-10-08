package pe.edu.upc.agroleak.production;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.http.MediaType;
import pe.edu.upc.agroleak.iam.domain.model.*;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;
import pe.edu.upc.agroleak.iam.infrastructure.security.JwtService;
import pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository;
import pe.edu.upc.agroleak.monitoring.infrastructure.SensorReadingRepository;
import pe.edu.upc.agroleak.monitoring.domain.model.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BackendProductionIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
    @Autowired UserRepository users; @Autowired JwtService jwt;
    @Autowired SensorReadingRepository readings; @Autowired DeviceRepository devices;
    String user() { return jwt.issue(users.save(new User("Demo","Test",UUID.randomUUID()+"@example.com","unused-test-hash",Role.FARMER))); }
    JsonNode call(MockHttpServletRequestBuilder request,String token,Object body,int expected) throws Exception {
        request.header("Authorization","Bearer "+token);
        if(body!=null) request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body));
        String result=mvc.perform(request).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
        return result.isEmpty() ? mapper.nullNode() : mapper.readTree(result);
    }
    String sector(String token) throws Exception {
        String farm=call(post("/api/v1/farms"),token,Map.of("name","Test","location","Lima","areaHectares",10),201).path("id").asText();
        String field=call(post("/api/v1/farms/"+farm+"/fields"),token,Map.of("name","Field","areaHectares",5),201).path("id").asText();
        return call(post("/api/v1/fields/"+field+"/sectors"),token,Map.of("name","Sector","areaHectares",2,"status","ACTIVE"),201).path("id").asText();
    }
    String device(String token,String sector) throws Exception {
        return call(post("/api/v1/devices"),token,Map.of("name","Gateway","location","Lima","deviceType","GATEWAY","sectorId",sector),201).path("id").asText();
    }
    void reading(String token,String device,String type,double value,Instant at) throws Exception {
        String unit=type.startsWith("FLOW") ? "L/min" : type.equals("PRESSURE") ? "bar" : "%";
        call(post("/api/v1/monitoring/readings"),token,Map.of("deviceId",device,"sensorType",type,"value",value,"unit",unit,"recordedAt",at.toString()),201);
    }
    @Test void historicalFiltersLatestAndSummaryAreScopedAndValidated() throws Exception {
        String a=user(),b=user(),d=device(a,sector(a));Instant now=Instant.now().minusSeconds(1);
        reading(a,d,"FLOW_IN",20,now.minusSeconds(60));reading(a,d,"FLOW_IN",30,now);
        var history=call(get("/api/v1/monitoring/readings").param("deviceId",d).param("sensorType","FLOW_IN").param("limit","1"),a,null,200);
        assertThat(history.size()).isEqualTo(1);assertThat(history.get(0).path("value").asDouble()).isEqualTo(30);
        var summary=call(get("/api/v1/monitoring/devices/"+d+"/summary"),a,null,200).path("flowIn");
        assertThat(summary.path("average").asDouble()).isEqualTo(25);assertThat(summary.path("min").asDouble()).isEqualTo(20);
        assertThat(summary.path("max").asDouble()).isEqualTo(30);assertThat(summary.path("samples").asLong()).isEqualTo(2);
        assertThat(call(get("/api/v1/monitoring/devices/"+d+"/latest"),a,null,200).path("value").asDouble()).isEqualTo(30);
        call(get("/api/v1/readings/device/"+d+"/latest"),a,null,200);
        call(get("/api/v1/monitoring/devices/"+d+"/latest-by-type"),a,null,200);
        call(get("/api/v1/monitoring/readings").param("deviceId",d),b,null,403);
        assertThat(call(get("/api/v1/monitoring/readings"),b,null,200).size()).isZero();
        call(get("/api/v1/monitoring/readings").param("from",now.toString()).param("to",now.minusSeconds(1).toString()),a,null,400);
        call(get("/api/v1/monitoring/readings").param("limit","0"),a,null,400);
        call(post("/api/v1/readings"),a,Map.of("deviceId",d,"sensorType","FLOW_IN","unit","L/min"),400);
        call(post("/api/v1/readings"),a,Map.of("deviceId",d,"sensorType","SOIL_MOISTURE","value",101,"unit","%"),400);
        call(post("/api/v1/readings"),a,Map.of("deviceId",d,"sensorType","PRESSURE","value",2,"unit","psi"),400);
    }
    @Test void alertsAcknowledgeResolveAndSuppressDuplicateOpenIncidents() throws Exception {
        String a=user(),b=user(),d=device(a,sector(a));Instant now=Instant.now().minusSeconds(1);
        reading(a,d,"FLOW_IN",25,now);reading(a,d,"FLOW_OUT",17,now);
        String id=call(get("/api/v1/alerts").param("deviceId",d).param("type","LEAK").param("severity","HIGH"),a,null,200).get(0).path("id").asText();
        call(patch("/api/v1/alerts/"+id+"/acknowledge"),b,null,403);
        var ack=call(patch("/api/v1/alerts/"+id+"/acknowledge"),a,null,200);
        assertThat(ack.path("status").asText()).isEqualTo("ACKNOWLEDGED");assertThat(ack.path("acknowledgedBy").asText()).isNotBlank();
        assertThat(call(patch("/api/v1/alerts/"+id+"/acknowledge"),a,null,200).path("acknowledgedAt")).isEqualTo(ack.path("acknowledgedAt"));
        reading(a,d,"FLOW_OUT",16,now.plusMillis(1));
        assertThat(call(get("/api/v1/alerts").param("deviceId",d),a,null,200).size()).isEqualTo(1);
        assertThat(call(get("/api/v1/analytics/dashboard"),a,null,200).path("activeAlerts").asInt()).isEqualTo(1);
        var resolved=call(patch("/api/v1/alerts/"+id+"/resolve"),a,null,200);
        assertThat(resolved.path("status").asText()).isEqualTo("RESOLVED");assertThat(resolved.path("resolvedBy").asText()).isNotBlank();
        assertThat(call(patch("/api/v1/alerts/"+id+"/resolve"),a,null,200).path("resolvedAt")).isEqualTo(resolved.path("resolvedAt"));
        call(patch("/api/v1/alerts/"+id+"/acknowledge"),a,null,409);
        reading(a,d,"FLOW_OUT",15,now.plusMillis(2));
        assertThat(call(get("/api/v1/alerts").param("deviceId",d),a,null,200).size()).isEqualTo(2);
        assertThat(call(get("/api/v1/alerts"),b,null,200).size()).isZero();
    }
    @Test void manualValveFlowAndModesPreventUnsupportedActuation() throws Exception {
        String a=user(),b=user(),d=device(a,sector(a));
        assertThat(call(get("/api/v1/valves/"+d+"/mode"),a,null,200).path("mode").asText()).isEqualTo("MANUAL");
        for(String mode:List.of("MONITOR_ONLY","AUTO_SAFE")) {
            call(put("/api/v1/valves/"+d+"/mode"),a,Map.of("mode",mode),200);
            call(post("/api/v1/valves/"+d+"/commands"),a,Map.of("action","OPEN"),409);
        }
        call(put("/api/v1/valves/"+d+"/mode"),a,Map.of("mode","MANUAL"),200);
        String id=call(post("/api/v1/valves/"+d+"/commands"),a,Map.of("action","OPEN"),201).path("id").asText();
        call(post("/api/v1/valves/"+d+"/commands"),a,Map.of("action","CLOSE"),409);
        call(put("/api/v1/valves/"+d+"/mode"),a,Map.of("mode","MONITOR_ONLY"),409);
        call(patch("/api/v1/valves/commands/"+id+"/confirm"),b,Map.of("success",true),403);
        assertThat(call(patch("/api/v1/valves/commands/"+id+"/confirm"),a,Map.of("success",true),200).path("status").asText()).isEqualTo("CONFIRMED");
        call(patch("/api/v1/valves/commands/"+id+"/confirm"),a,Map.of("success",true),409);
        String failed=call(post("/api/v1/valves/"+d+"/commands"),a,Map.of("action","CLOSE"),201).path("id").asText();
        assertThat(call(patch("/api/v1/valves/commands/"+failed+"/confirm"),a,Map.of("success",false),200).path("status").asText()).isEqualTo("FAILED");
    }
    @Test void pestObservationsSupportLegacyAndSectorOnlyInputAndGenerateScopedAlerts() throws Exception {
        String a=user(),b=user(),s=sector(a),d=device(a,s);
        var pest=call(post("/api/v1/pests/observations"),a,Map.of("sectorId",s,"count",7,"confidence",0.9,"pestType","APHID","imageUrl","https://example.com/pests.jpg"),201);
        assertThat(pest.path("count").asInt()).isEqualTo(7);assertThat(pest.path("detectedAt").asText()).isNotBlank();assertThat(pest.path("deviceId").isNull()).isTrue();
        assertThat(call(get("/api/v1/alerts").param("type","PEST_DETECTED"),a,null,200).size()).isEqualTo(1);
        call(post("/api/v1/pest-observations"),a,Map.of("deviceId",d,"pestCount",2,"confidence",0.8),201);
        assertThat(call(get("/api/v1/analytics/dashboard"),a,null,200).path("pestsDetectedToday").asInt()).isEqualTo(9);
        assertThat(call(get("/api/v1/analytics/dashboard"),b,null,200).path("pestsDetectedToday").asInt()).isZero();
        assertThat(call(get("/api/v1/pests/observations"),b,null,200).size()).isZero();
        call(post("/api/v1/pests/observations"),b,Map.of("sectorId",s,"count",10,"confidence",0.9),403);
        call(post("/api/v1/pests/observations"),a,Map.of("count",1,"confidence",0.8),400);
        call(post("/api/v1/pests/observations"),a,Map.of("sectorId",s,"count",1,"confidence",1.5),400);
    }
    @Test void analyticsIntegratesLitersWithoutExtrapolatingAcrossLongGaps() throws Exception {
        String a=user(),b=user(),d=device(a,sector(a));
        Instant start=Instant.now().atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        // Yesterday's samples expire before today: they cannot inflate today's totals.
        var device=devices.findById(UUID.fromString(d)).orElseThrow();
        readings.save(new SensorReading(device,SensorType.FLOW_IN,60,"L/min",start.minusSeconds(600)));
        readings.save(new SensorReading(device,SensorType.FLOW_OUT,50,"L/min",start.minusSeconds(600)));
        var dash=call(get("/api/v1/analytics/dashboard"),a,null,200);
        assertThat(dash.path("waterUsageToday").asDouble()).isZero();
        assertThat(dash.path("volumeUnit").asText()).isEqualTo("L");
        assertThat(call(get("/api/v1/analytics/charts").param("deviceId",d),a,null,200).path("hourly").size()).isEqualTo(24);
        call(get("/api/v1/analytics/charts").param("deviceId",d),b,null,403);
        assertThat(call(get("/api/v1/analytics/dashboard"),b,null,200).path("offlineDevices").asLong()).isZero();
    }
}
