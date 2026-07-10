package io.github.hectorvent.floci.services.servicequotas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.hectorvent.floci.core.common.AwsException;
import io.github.hectorvent.floci.core.common.RegionResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class ServiceQuotasJsonHandler {

    private final ObjectMapper mapper;
    private final RegionResolver regionResolver;

    @Inject
    public ServiceQuotasJsonHandler(ObjectMapper mapper, RegionResolver regionResolver) {
        this.mapper = mapper;
        this.regionResolver = regionResolver;
    }

    public Response handle(String action, JsonNode request, String region) {
        return switch (action) {
            case "GetServiceQuota", "GetAWSDefaultServiceQuota" -> getQuota(request, region);
            case "ListServiceQuotas", "ListAWSDefaultServiceQuotas" -> listQuotas(request, region);
            case "ListServices" -> listServices();
            default -> throw new AwsException("UnsupportedOperationException",
                    "Operation " + action + " is not supported.", 400);
        };
    }

    private Response getQuota(JsonNode request, String region) {
        String serviceCode = required(request, "ServiceCode");
        String quotaCode = required(request, "QuotaCode");
        ServiceQuotaCatalog.QuotaDefinition quota = findQuota(serviceCode, quotaCode);
        ObjectNode response = mapper.createObjectNode();
        response.set("Quota", quotaNode(serviceCode, quota, region));
        return Response.ok(response).build();
    }

    private Response listQuotas(JsonNode request, String region) {
        String serviceCode = required(request, "ServiceCode");
        List<ServiceQuotaCatalog.QuotaDefinition> quotas = ServiceQuotaCatalog.forService(serviceCode);
        if (quotas.isEmpty()) {
            throw noSuchResource();
        }
        ObjectNode response = mapper.createObjectNode();
        ArrayNode values = response.putArray("Quotas");
        for (ServiceQuotaCatalog.QuotaDefinition quota : quotas) {
            values.add(quotaNode(serviceCode, quota, region));
        }
        return Response.ok(response).build();
    }

    private Response listServices() {
        ObjectNode response = mapper.createObjectNode();
        ObjectNode service = response.putArray("Services").addObject();
        service.put("ServiceCode", ServiceQuotaCatalog.EC2_SERVICE_CODE);
        service.put("ServiceName", ServiceQuotaCatalog.EC2_SERVICE_NAME);
        return Response.ok(response).build();
    }

    private ObjectNode quotaNode(String serviceCode,
                                 ServiceQuotaCatalog.QuotaDefinition quota,
                                 String region) {
        ObjectNode node = mapper.createObjectNode();
        node.put("Arn", "arn:aws:servicequotas:" + region + ":" + regionResolver.getAccountId()
                + ":" + serviceCode + "/" + quota.code());
        node.put("GlobalQuota", false);
        node.put("QuotaCode", quota.code());
        node.put("QuotaName", quota.name());
        node.put("ServiceCode", serviceCode);
        node.put("ServiceName", ServiceQuotaCatalog.EC2_SERVICE_NAME);
        node.put("Unit", "None");
        node.put("Value", quota.value());
        node.put("Adjustable", quota.adjustable());
        return node;
    }

    private ServiceQuotaCatalog.QuotaDefinition findQuota(String serviceCode, String quotaCode) {
        return ServiceQuotaCatalog.find(serviceCode, quotaCode).orElseThrow(ServiceQuotasJsonHandler::noSuchResource);
    }

    private static String required(JsonNode request, String field) {
        String value = request == null ? null : request.path(field).asText(null);
        if (value == null || value.isBlank()) {
            throw new AwsException("ValidationException",
                    "Value null at '" + lowerCamel(field) + "' failed to satisfy constraint: Member must not be null",
                    400);
        }
        return value;
    }

    private static String lowerCamel(String field) {
        return Character.toLowerCase(field.charAt(0)) + field.substring(1);
    }

    private static AwsException noSuchResource() {
        return new AwsException("NoSuchResourceException", "The specified resource does not exist.", 400);
    }
}
