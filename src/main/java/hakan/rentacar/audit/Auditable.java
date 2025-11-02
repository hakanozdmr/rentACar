package hakan.rentacar.audit;

import hakan.rentacar.entities.concretes.AuditLog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audited.
 * Can be used on service methods to automatically log audit information.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * The entity name being operated on (e.g., "Car", "Customer", "Reservation")
     */
    String entity() default "";
    
    /**
     * The action being performed on the entity
     */
    AuditLog.ActionType action() default AuditLog.ActionType.READ;
    
    /**
     * Additional information to include in the audit log
     */
    String description() default "";
}




