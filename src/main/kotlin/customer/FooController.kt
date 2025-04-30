package red.cliff.observability.customer

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
//@RequestMapping("/foo")
class FooController {

    @GetMapping("address")
    fun getPersons(): List<Address> = listOf(
        Address(street = "Eenruiter", number = 20, addition = null),
        Address(street = "Korhaanstraat", number = 64, addition = "A"),
    )
}

data class Address(
    val street: String,
    val number: Int,
    val addition: String?,
)
