import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection.TRANSACTION_READ_COMMITTED

object Countries : Table() {
    val id = varchar("country_id", 2).primaryKey() // Column<String>
    val name = varchar("country_name", length = 50) // Column<String>
    val regionId = integer("region_id") references Regions.id // Column<Int?>
}

object Regions : Table() {
    val id = integer("region_id").autoIncrement().primaryKey() // Column<Int>
    val name = varchar("region_name", 50) // Column<String>
}

fun main(args: Array<String>) {
   // Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
    Database.connect("jdbc:oracle:thin:@localhost:1521:orcl", driver = "oracle.jdbc.driver.OracleDriver", user = "HR", password = "hr4oracle")

    transaction(TRANSACTION_READ_COMMITTED, 1) {
        SchemaUtils.create(Countries, Regions)

        for (country in Countries.selectAll()) {
            println("${country[Countries.id]}: ${country[Countries.name]}")
        }

        println("Manual join:")
        (Countries innerJoin Regions).slice(Countries.id, Countries.name, Regions.name).
                select { (Regions.name.eq("Europe") or Regions.id.eq(4)) and
                        (Countries.name.eq("China") or Countries.id.eq("IL")) and
                        (Countries.regionId.eq(Regions.id))}.forEach {
            println("${it[Countries.name]} belongs to ${it[Regions.name]}")
        }
    }
}