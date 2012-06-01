/*
 * All content copyright (c) 2003-2012 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */

package org.quartz.management;

import org.quartz.management.service.EntityResourceFactory;
import org.quartz.management.service.SamplerRepositoryService;
import org.terracotta.management.ServiceLocator;
import org.terracotta.management.resource.services.validator.RequestValidator;

/**
 * @author brandony
 */
public class EmbeddedQuartzServiceLocator extends ServiceLocator implements EntityResourceFactory.Locator, SamplerRepositoryService.Locator {
    private final EntityResourceFactory entityRsrcFactory;
    private final SamplerRepositoryService samplerRepositoryService;

    public static EmbeddedQuartzServiceLocator locator() {
        return (EmbeddedQuartzServiceLocator) ServiceLocator.locator();
    }

    public EmbeddedQuartzServiceLocator(RequestValidator requestValidator, EntityResourceFactory entityRsrcFactory,
            SamplerRepositoryService samplerRepositoryService) {
        super(requestValidator);
        this.entityRsrcFactory = entityRsrcFactory;
        this.samplerRepositoryService = samplerRepositoryService;
    }

    @Override
    public EntityResourceFactory locateEntityResourceFactory() {
        return entityRsrcFactory;
    }

    @Override
    public SamplerRepositoryService locateSamplerRepositoryService() {
        return samplerRepositoryService;
    }

}
