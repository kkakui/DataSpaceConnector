/*
 *  Copyright (c) 2020 - 2022 Microsoft Corporation
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

package org.eclipse.dataspaceconnector.dataplane.selector.store.sql;

import org.eclipse.dataspaceconnector.dataplane.selector.store.DataPlaneInstanceStore;
import org.eclipse.dataspaceconnector.dataplane.selector.store.sql.schema.DataPlaneInstanceStatements;
import org.eclipse.dataspaceconnector.dataplane.selector.store.sql.schema.postgres.PostgresDataPlaneInstanceStatements;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.EdcSetting;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Extension;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provider;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provides;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transaction.TransactionContext;
import org.eclipse.dataspaceconnector.spi.transaction.datasource.DataSourceRegistry;

/**
 * Extensions that expose an implementation of {@link DataPlaneInstanceStore} that uses SQL as backend storage
 */
@Provides(DataPlaneInstanceStore.class)
@Extension(value = SqlDataPlaneInstanceStoreExtension.NAME)
public class SqlDataPlaneInstanceStoreExtension implements ServiceExtension {
    public static final String NAME = "Sql Data Plane Instance Store";
    
    @EdcSetting(value = "Name of the datasource to use for accessing data plane instances")
    private static final String DATASOURCE_SETTING_NAME = "edc.datasource.dataplaneinstance.name";
    private static final String DEFAULT_DATASOURCE_NAME = "dataplaneinstance";
    @Inject
    private DataSourceRegistry dataSourceRegistry;

    @Inject
    private TransactionContext transactionContext;

    @Inject(required = false)
    private DataPlaneInstanceStatements statements;

    @Override
    public String name() {
        return NAME;
    }


    @Provider
    public DataPlaneInstanceStore dataPlaneInstanceStore(ServiceExtensionContext context) {
        return new SqlDataPlaneInstanceStore(dataSourceRegistry, getDataSourceName(context), transactionContext, statements, context.getTypeManager().getMapper());
    }

    /**
     * returns an externally-provided sql statement dialect, or postgres as a default
     */
    private DataPlaneInstanceStatements getStatementImpl() {
        return statements != null ? statements : new PostgresDataPlaneInstanceStatements();
    }

    private String getDataSourceName(ServiceExtensionContext context) {
        return context.getConfig().getString(DATASOURCE_SETTING_NAME, DEFAULT_DATASOURCE_NAME);
    }
}