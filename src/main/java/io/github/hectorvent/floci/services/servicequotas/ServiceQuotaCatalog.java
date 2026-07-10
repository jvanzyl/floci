package io.github.hectorvent.floci.services.servicequotas;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ServiceQuotaCatalog {

    public static final String EC2_SERVICE_CODE = "ec2";
    public static final String EC2_SERVICE_NAME = "Amazon Elastic Compute Cloud (Amazon EC2)";
    public static final String EIP_PER_REGION = "L-0263D0A3";
    public static final String EIP_PER_PUBLIC_NAT_GATEWAY = "L-5F53652F";
    public static final String NAT_GATEWAYS_PER_AVAILABILITY_ZONE = "L-FE5A380F";

    private static final Map<String, QuotaDefinition> EC2_QUOTAS = Map.of(
            EIP_PER_REGION, new QuotaDefinition(
                    EIP_PER_REGION, "Elastic IP addresses per Region", 5, true),
            EIP_PER_PUBLIC_NAT_GATEWAY, new QuotaDefinition(
                    EIP_PER_PUBLIC_NAT_GATEWAY, "Elastic IP addresses per public NAT gateway", 2, true),
            NAT_GATEWAYS_PER_AVAILABILITY_ZONE, new QuotaDefinition(
                    NAT_GATEWAYS_PER_AVAILABILITY_ZONE, "NAT gateways per Availability Zone", 5, true));

    private ServiceQuotaCatalog() {}

    public static List<QuotaDefinition> forService(String serviceCode) {
        if (!EC2_SERVICE_CODE.equals(serviceCode)) {
            return List.of();
        }
        return List.copyOf(EC2_QUOTAS.values());
    }

    public static Optional<QuotaDefinition> find(String serviceCode, String quotaCode) {
        if (!EC2_SERVICE_CODE.equals(serviceCode)) {
            return Optional.empty();
        }
        return Optional.ofNullable(EC2_QUOTAS.get(quotaCode));
    }

    public record QuotaDefinition(String code, String name, double value, boolean adjustable) {}
}
