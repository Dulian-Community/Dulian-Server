package dulian.dulian.global.common

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.format.DateTimeFormatter

class DateFormatValidator : ConstraintValidator<DateFormat, String> {

    private lateinit var format: String

    override fun initialize(constraintAnnotation: DateFormat) {
        this.format = constraintAnnotation.format
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?
    ): Boolean {
        if (value == null) return true

        return try {
            val formatter = DateTimeFormatter.ofPattern(format)
            formatter.parse(value)
            true
        } catch (e: Exception) {
            false
        }
    }
}