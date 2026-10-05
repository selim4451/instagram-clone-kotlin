package com.example.instagramclone.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object DatabaseFactory {

    lateinit var dataSource: DataSource
        private set

    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/instagram_clone"
            username = System.getenv("DB_USER") ?: "postgres"
            password = System.getenv("DB_PASSWORD")
                ?: error("DB_PASSWORD environment variable is not set")
            maximumPoolSize = 5
        }
        dataSource = HikariDataSource(config)
        createSchema()
    }

    private fun createSchema() {
        val sql = checkNotNull(javaClass.getResource("/schema.sql")) { "schema.sql not found" }.readText()
        dataSource.connection.use { connection ->
            connection.createStatement().use { it.execute(sql) }
        }
    }
}
