package salon.catalog.infrastructure.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Encja JPA specyfikacji pojazdu (konfiguracja, UC-KON-01). */
@Entity
@Table(name = "vehicle_specifications")
public class VehicleSpecificationJpaEntity {

    @Id
    public String id;
    public String catalogId;
    public String state;
    public BigDecimal totalPrice;
    public String currency;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "specification_options", joinColumns = @JoinColumn(name = "specification_id"))
    @Column(name = "option_code")
    public List<String> options = new ArrayList<>();

    public VehicleSpecificationJpaEntity() {
    }
}
