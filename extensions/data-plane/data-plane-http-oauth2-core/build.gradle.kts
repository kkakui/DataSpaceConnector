/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */
plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(project(":spi:data-plane:data-plane-http-spi"))
    api(project(":spi:common:oauth2-spi"))
    api(project(":spi:common:jwt-signer-spi"))
    implementation(project(":core:common:lib:token-lib"))
    implementation(project(":spi:common:keys-spi"))

    testImplementation(project(":core:common:junit"))
    testImplementation(libs.restAssured)
    testImplementation(libs.mockserver.netty)
}


