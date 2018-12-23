import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.dao.*

object DaoUsers : IntIdTable() {
    val name = varchar("name", 50).index()
    val city = reference("city", DaoCities)
    val age = integer("age")
}

object DaoCities: IntIdTable() {
    val name = varchar("name", 50)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(DaoUsers)

    var name by DaoUsers.name
    var city by City referencedOn DaoUsers.city
    var age by DaoUsers.age
}

class City(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<City>(DaoCities)

    var name by DaoCities.name
    val users by User referrersOn DaoUsers.city
}

fun salute() = println("Salute")

fun main(args: Array<String>) {
    run(::salute)

    val list = listOf("args: ", *args)
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

    transaction {
//        logger.addLogger(StdOutSqlLogger())

        create (DaoCities, DaoUsers)

        val stPete = City.new {
            name = "St. Petersburg"
        }

        val munich = City.new {
            name = "Munich"
        }

        User.new {
            name = "a"
            city = stPete
            age = 5
        }

        User.new {
            name = "b"
            city = stPete
            age = 27
        }

        User.new {
            name = "c"
            city = munich
            age = 42
        }

        println("DaoCities: ${City.all().joinToString {it.name}}")
        println("DaoUsers in ${stPete.name}: ${stPete.users.joinToString {it.name}}")
        println("Adults: ${User.find { DaoUsers.age greaterEq 18 }.joinToString {it.name}}")
    }
}