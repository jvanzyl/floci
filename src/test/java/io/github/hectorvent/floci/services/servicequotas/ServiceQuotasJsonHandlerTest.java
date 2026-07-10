package io.github.hectorvent.floci.services.servicequotas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.hectorvent.floci.core.common.AwsException;
import io.github.hectorvent.floci.core.common.RegionResolver;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceQuotasJsonHandlerTest {

    private static final String REGION = "us-east-1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ServiceQuotasJsonHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ServiceQuotasJsonHandler(MAPPER,
                new RegionResolver(REGION, "123456789012"));
    }

    @Test
    void getServiceQuotaReturnsAwsShapeAndValue() {
        ObjectNode request = MAPPER.createObjectNode()
                .put("ServiceCode", "ec2")
                .put("QuotaCode", ServiceQuotaCatalog.NAT_GATEWAYS_PER_AVAILABILITY_ZONE);

        JsonNode quota = ((ObjectNode) handler.handle("GetServiceQuota", request, REGION)
                .getEntity()).get("Quota");

        assertEquals("ec2", quota.get("ServiceCode").asText());
        assertEquals(ServiceQuotaCatalog.NAT_GATEWAYS_PER_AVAILABILITY_ZONE,
                quota.get("QuotaCode").asText());
        assertEquals(5, quota.get("Value").asInt());
        assertEquals("None", quota.get("Unit").asText());
        assertEquals("arn:aws:servicequotas:us-east-1:123456789012:ec2/"
                        + ServiceQuotaCatalog.NAT_GATEWAYS_PER_AVAILABILITY_ZONE,
                quota.get("Arn").asText());
    }

    @Test
    void listServiceQuotasReturnsAllSupportedEc2Quotas() {
        ObjectNode request = MAPPER.createObjectNode().put("ServiceCode", "ec2");

        JsonNode quotas = ((ObjectNode) handler.handle("ListServiceQuotas", request, REGION)
                .getEntity()).get("Quotas");

        assertEquals(3, quotas.size());
        assertTrue(quotas.findValuesAsText("QuotaCode")
                .contains(ServiceQuotaCatalog.EIP_PER_REGION));
        assertTrue(quotas.findValuesAsText("QuotaCode")
                .contains(ServiceQuotaCatalog.EIP_PER_PUBLIC_NAT_GATEWAY));
        assertTrue(quotas.findValuesAsText("QuotaCode")
                .contains(ServiceQuotaCatalog.NAT_GATEWAYS_PER_AVAILABILITY_ZONE));
    }

    @Test
    void listServicesIncludesEc2() {
        JsonNode services = ((ObjectNode) handler.handle("ListServices", MAPPER.createObjectNode(), REGION)
                .getEntity()).get("Services");

        assertEquals(1, services.size());
        assertEquals("ec2", services.get(0).get("ServiceCode").asText());
        assertEquals(ServiceQuotaCatalog.EC2_SERVICE_NAME,
                services.get(0).get("ServiceName").asText());
    }

    @Test
    void unknownQuotaUsesAwsErrorShape() {
        ObjectNode request = MAPPER.createObjectNode()
                .put("ServiceCode", "ec2")
                .put("QuotaCode", "L-unknown");

        AwsException error = assertThrows(AwsException.class,
                () -> handler.handle("GetServiceQuota", request, REGION));

        assertEquals("NoSuchResourceException", error.getErrorCode());
        assertEquals("The specified resource does not exist.", error.getMessage());
        assertEquals(400, error.getHttpStatus());
    }

    @Test
    void missingServiceCodeUsesAwsValidationError() {
        AwsException error = assertThrows(AwsException.class,
                () -> handler.handle("ListServiceQuotas", MAPPER.createObjectNode(), REGION));

        assertEquals("ValidationException", error.getErrorCode());
        assertEquals(400, error.getHttpStatus());
    }
}
