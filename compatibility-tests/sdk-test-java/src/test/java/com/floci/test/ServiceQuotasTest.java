package com.floci.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.servicequotas.ServiceQuotasClient;
import software.amazon.awssdk.services.servicequotas.model.GetServiceQuotaRequest;
import software.amazon.awssdk.services.servicequotas.model.ListServiceQuotasRequest;
import software.amazon.awssdk.services.servicequotas.model.NoSuchResourceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceQuotasTest {

    private static ServiceQuotasClient serviceQuotas;

    @BeforeAll
    static void setUp() {
        serviceQuotas = TestFixtures.serviceQuotasClient();
    }

    @AfterAll
    static void tearDown() {
        if (serviceQuotas != null) {
            serviceQuotas.close();
        }
    }

    @Test
    void getNatGatewayQuotaDecodesThroughSdk() {
        var quota = serviceQuotas.getServiceQuota(GetServiceQuotaRequest.builder()
                .serviceCode("ec2")
                .quotaCode("L-FE5A380F")
                .build()).quota();

        assertThat(quota.quotaCode()).isEqualTo("L-FE5A380F");
        assertThat(quota.value()).isEqualTo(5.0);
        assertThat(quota.unit()).isEqualTo("None");
    }

    @Test
    void listEc2QuotasReturnsSupportedQuotaCodes() {
        var quotas = serviceQuotas.listServiceQuotas(ListServiceQuotasRequest.builder()
                .serviceCode("ec2")
                .build()).quotas();

        assertThat(quotas).extracting(quota -> quota.quotaCode())
                .contains("L-0263D0A3", "L-5F53652F", "L-FE5A380F");
    }

    @Test
    void unknownQuotaUsesTypedAwsException() {
        assertThatThrownBy(() ->
                serviceQuotas.getServiceQuota(GetServiceQuotaRequest.builder()
                        .serviceCode("ec2")
                        .quotaCode("L-unknown")
                        .build()))
                .isInstanceOf(NoSuchResourceException.class)
                .hasMessageContaining("The specified resource does not exist.");
    }
}
