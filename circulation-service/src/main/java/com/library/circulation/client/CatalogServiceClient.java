package com.library.circulation.client;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.logging.Logger;

//STUB IMPLEMENTATION
//TODO: HTTP IMPLEMENTATION
@Component
public class CatalogServiceClient {

    private static final Logger log = (Logger) LoggerFactory.getLogger(CatalogServiceClient.class);

    public CopySummary getCopy(UUID copyId) {

        //Replace with RestClient Call to: http://catalog-dervice:8081/api/catalog/copies/{copyId}
        return new CopySummary(copyId, UUID.randomUUID(), "AVAILABLE");
    }

    //STUB IMPLEMENTATION
    //TODO: HTTP IMPLEMENTATION
    public void updateCopyStatus( UUID copyId, String newStatus) {
        //RestClient PATCH call
        log.info("STUB: would update copy " + copyId + " to status " + newStatus);
    }
}
