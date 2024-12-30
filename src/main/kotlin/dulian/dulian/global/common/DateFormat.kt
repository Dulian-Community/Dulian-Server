package dulian.dulian.global.common

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateFormatValidator::class])
annotation class DateFormat(
    val format: String = "yyyy-MM-dd",
    val message: String = "날짜 형식이 올바르지 않습니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
