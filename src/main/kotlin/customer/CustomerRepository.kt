package red.cliff.observability.customer

import org.springframework.data.repository.ListCrudRepository

interface CustomerRepository : ListCrudRepository<Customer, Long>
