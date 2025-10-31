package hakan.rentacar;

import hakan.rentacar.exceptions.BusinessException;
import hakan.rentacar.exceptions.ProblemDetails;
import hakan.rentacar.exceptions.ValidationProblemDetails;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@SpringBootApplication
@RestControllerAdvice
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableScheduling
public class RentACarApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentACarApplication.class, args);
    }
    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ProblemDetails handleBusinessException(BusinessException businessException){
        ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setMessage(businessException.getMessage());
        return problemDetails;
    }
    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ProblemDetails handleValidationException(MethodArgumentNotValidException methodArgumentNotValidException){
        ValidationProblemDetails validationProblemDetails = new ValidationProblemDetails();
        validationProblemDetails.setMessage("VALIDATION.EXCEPTION");
        validationProblemDetails.setValidationErrors(new HashMap<String,String>());

        for (FieldError fieldError: methodArgumentNotValidException.getBindingResult().getFieldErrors()) {
            validationProblemDetails.getValidationErrors().put(fieldError.getField(),fieldError.getDefaultMessage());
        }

        return validationProblemDetails;
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ProblemDetails handleRuntimeException(RuntimeException runtimeException){
        ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setMessage(runtimeException.getMessage());
        return problemDetails;
    }
}
