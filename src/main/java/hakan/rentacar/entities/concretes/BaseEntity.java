package hakan.rentacar.entities.concretes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"created_date,uptade_date"},allowGetters = true)
public class BaseEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="created_by",updatable = false)
    @CreatedBy
    private String createdBy;
    @Column(name ="created_date",updatable = false)
    @CreatedDate
    private Date createdDate;

    @Column(name ="uptade_By")
    @LastModifiedBy
    @Nullable
    private String uptadeBy;
    @Column(name ="uptade_date")
    @LastModifiedDate
    @Nullable
    private Date uptadeDate;

    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date date;
}
