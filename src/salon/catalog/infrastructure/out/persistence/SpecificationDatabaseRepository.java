package salon.catalog.infrastructure.out.persistence;

import salon.catalog.application.port.out.VehicleSpecificationRepository;
import salon.catalog.application.domain.model.specification.SpecificationId;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SpecificationDatabaseRepository implements VehicleSpecificationRepository {

    private static final String COLLECTION = "vehicle_specification";

    private final DbAdapter dbAdapter;
    private final VehicleSpecificationPersistenceMapper mapper;

    public SpecificationDatabaseRepository(DbAdapter dbAdapter,
                                           VehicleSpecificationPersistenceMapper mapper) {
        this.dbAdapter = dbAdapter;
        this.mapper = mapper;
    }

    @Override
    public void save(VehicleSpecification specification) {
        dbAdapter.upsert(COLLECTION, specification.id().toString(), mapper.toRecord(specification));
    }

    @Override
    public Optional<VehicleSpecification> findById(SpecificationId id) {
        return dbAdapter.findById(COLLECTION, id.toString())
                .map(r -> mapper.toDomain((VehicleSpecificationRecord) r));
    }
}
