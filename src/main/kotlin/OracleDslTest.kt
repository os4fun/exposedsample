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

class Greeter(val greeting: String) {
    // call instance of objects as if they are function
    operator fun invoke(name: String) {
        println("$greeting, $name")
    }
}

data class Issue (
    val id: String, val project: String, val type:String, val priority: String, val description: String
)

class ImportantIssuesPredicate(val project: String)
    : (Issue) -> Boolean {
    override fun invoke(issue: Issue): Boolean {
        return issue.project == project && issue.isImportant()
    }

    private fun Issue.isImportant(): Boolean {
        return type == "Bug" && (priority == "Major" || priority == "Critical")
    }
}

class DependencyHandler {
    fun compile(coordination: String) {
        println("Added dependencies on $coordination")
    }

    operator fun invoke(body: DependencyHandler.() -> Unit  ) {
        body()
    }
}


fun main(args: Array<String>) {
    val bavarianGreeter = Greeter("Servus")
    bavarianGreeter("Dmitry")

   val i1 = Issue("IDEA-154446", "IDEA", "Bug", "Major", "Save settings failed")
   val i2 = Issue("KT-12183", "Kotlin", "Feature", "Normal", "Intention: convert")

    val predicate = ImportantIssuesPredicate("IDEA")
    for (issue in listOf(i1, i2).filter(predicate)) {
        println(issue.id)
    }

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