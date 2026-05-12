package tr.edu.duzce.mf.bm.cloudstorage.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QdrantInitializer {

    private static final Logger logger = LoggerFactory.getLogger(QdrantInitializer.class);
    private static final String COLLECTION_NAME = "cloud_storage";
    private static final int VECTOR_SIZE = 3072;

    @Autowired
    private QdrantClient qdrantClient;

    @PostConstruct
    public void initCollection() {
        try {
            boolean exists = qdrantClient.collectionExistsAsync(COLLECTION_NAME).get();

            if (!exists) {
                qdrantClient.createCollectionAsync(
                        COLLECTION_NAME,
                        VectorParams.newBuilder()
                                .setSize(VECTOR_SIZE)
                                .setDistance(Distance.Cosine)
                                .build()
                ).get();
                logger.info("Qdrant collection oluşturuldu: {}", COLLECTION_NAME);
            } else {
                logger.info("Qdrant collection zaten mevcut: {}", COLLECTION_NAME);
            }
        } catch (Exception e) {
            logger.error("Qdrant bağlantısı kurulamadı. AI özellikleri çalışmayabilir: {}", e.getMessage());
        }
    }
}