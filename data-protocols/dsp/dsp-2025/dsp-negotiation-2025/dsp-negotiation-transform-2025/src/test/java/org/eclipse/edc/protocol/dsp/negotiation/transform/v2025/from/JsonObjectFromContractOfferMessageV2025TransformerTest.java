/*
 *  Copyright (c) 2025 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package org.eclipse.edc.protocol.dsp.negotiation.transform.v2025.from;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.controlplane.contract.spi.types.negotiation.ContractOfferMessage;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.transform.spi.ProblemBuilder;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.protocol.dsp.spi.type.Dsp2025Constants.DSP_NAMESPACE_V_2025_1;
import static org.eclipse.edc.protocol.dsp.spi.type.DspNegotiationPropertyAndTypeNames.DSPACE_PROPERTY_OFFER_TERM;
import static org.eclipse.edc.protocol.dsp.spi.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_OFFER_MESSAGE_TERM;
import static org.eclipse.edc.protocol.dsp.spi.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_CALLBACK_ADDRESS_TERM;
import static org.eclipse.edc.protocol.dsp.spi.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_CONSUMER_PID_TERM;
import static org.eclipse.edc.protocol.dsp.spi.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_PROVIDER_PID_TERM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonObjectFromContractOfferMessageV2025TransformerTest {

    private static final String MESSAGE_ID = "messageId";
    private static final String CALLBACK_ADDRESS = "https://test.com";
    private static final String PROTOCOL = "DSP";
    private static final String CONTRACT_OFFER_ID = "contractId";

    private final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(Map.of());
    private final TransformerContext context = mock();

    private JsonObjectFromContractOfferMessageV2025Transformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new JsonObjectFromContractOfferMessageV2025Transformer(jsonFactory, DSP_NAMESPACE_V_2025_1);
    }

    @Test
    void transform_shouldReturnJsonObject_whenValidMessage_withConsumerPid() {
        var message = ContractOfferMessage.Builder.newInstance()
                .id(MESSAGE_ID)
                .callbackAddress(CALLBACK_ADDRESS)
                .providerPid("providerPid")
                .consumerPid("consumerPid")
                .protocol(PROTOCOL)
                .contractOffer(contractOffer())
                .build();
        var policyJson = jsonFactory.createObjectBuilder().build();

        when(context.transform(any(Policy.class), eq(JsonObject.class))).thenReturn(policyJson);

        var result = transformer.transform(message, context);

        assertThat(result).isNotNull();
        assertThat(result.getJsonString(ID).getString()).isNotEmpty();
        assertThat(result.getJsonString(TYPE).getString()).isEqualTo(toIri(DSPACE_TYPE_CONTRACT_OFFER_MESSAGE_TERM));
        assertThat(result.getJsonString(toIri(DSPACE_PROPERTY_CALLBACK_ADDRESS_TERM))).isNull();
        assertThat(result.getJsonObject(toIri(DSPACE_PROPERTY_OFFER_TERM))).isNotNull();
        assertThat(result.getJsonObject(toIri(DSPACE_PROPERTY_OFFER_TERM)).getJsonString(ID).getString()).isEqualTo(CONTRACT_OFFER_ID);
        assertThat(result.getJsonObject(toIri(DSPACE_PROPERTY_PROVIDER_PID_TERM)).getString(ID)).isEqualTo("providerPid");
        assertThat(result.getJsonObject(toIri(DSPACE_PROPERTY_CONSUMER_PID_TERM)).getString(ID)).isEqualTo("consumerPid");

        verify(context, never()).reportProblem(anyString());
    }

    @Test
    void transform_shouldReturnJsonObject_whenValidMessage_withCallback() {
        var message = ContractOfferMessage.Builder.newInstance()
                .id(MESSAGE_ID)
                .callbackAddress(CALLBACK_ADDRESS)
                .providerPid("providerPid")
                .protocol(PROTOCOL)
                .contractOffer(contractOffer())
                .build();
        var policyJson = jsonFactory.createObjectBuilder().build();

        when(context.transform(any(Policy.class), eq(JsonObject.class))).thenReturn(policyJson);

        var result = transformer.transform(message, context);

        assertThat(result).isNotNull();
        assertThat(result.getJsonString(ID).getString()).isNotEmpty();
        assertThat(result.getJsonString(TYPE).getString()).isEqualTo(toIri(DSPACE_TYPE_CONTRACT_OFFER_MESSAGE_TERM));
        assertThat(result.getJsonString(toIri(DSPACE_PROPERTY_CALLBACK_ADDRESS_TERM)).getString()).isEqualTo(CALLBACK_ADDRESS);
        assertThat(result.getJsonObject(toIri(DSPACE_PROPERTY_OFFER_TERM))).isNotNull();
        assertThat(result.getJsonObject(toIri(DSPACE_PROPERTY_OFFER_TERM)).getJsonString(ID).getString()).isEqualTo(CONTRACT_OFFER_ID);
        assertThat(result.getJsonObject(toIri(DSPACE_PROPERTY_PROVIDER_PID_TERM)).getString(ID)).isEqualTo("providerPid");
        assertThat(result.getJsonObject(toIri(DSPACE_PROPERTY_CONSUMER_PID_TERM))).isNull();

        verify(context, never()).reportProblem(anyString());
    }

    @Test
    void transform_shouldReportProblem_whenPolicyTransformationFails() {
        var message = ContractOfferMessage.Builder.newInstance()
                .callbackAddress("callbackAddress")
                .id(MESSAGE_ID)
                .processId("processId")
                .providerPid("providerPid")
                .protocol(PROTOCOL)
                .contractOffer(contractOffer())
                .build();

        when(context.transform(any(Policy.class), eq(JsonObject.class))).thenReturn(null);
        when(context.problem()).thenReturn(new ProblemBuilder(context));

        var result = transformer.transform(message, context);

        assertThat(result).isNull();
        verify(context, times(1)).reportProblem(any());
    }

    private ContractOffer contractOffer() {
        return ContractOffer.Builder.newInstance()
                .id(CONTRACT_OFFER_ID)
                .assetId("assetId")
                .policy(Policy.Builder.newInstance().build())
                .build();
    }

    private String toIri(String term) {
        return DSP_NAMESPACE_V_2025_1.toIri(term);
    }

}
