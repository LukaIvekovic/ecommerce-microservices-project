import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrics")
public class MetricsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String protocol;
    private Long orderLatency;
    private Long paymentLatency;
    private Long shippingLatency;
    private Long totalLatency;
    private Integer compensations;
    private String orderStatus;
    private String paymentStatus;
    private String shippingStatus;

    private LocalDateTime createdAt = LocalDateTime.now();

}