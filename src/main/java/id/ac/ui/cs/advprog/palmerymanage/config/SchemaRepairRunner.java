package id.ac.ui.cs.advprog.palmerymanage.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("pengiriman")) {
            return;
        }

        addColumnIfMissing("admin_approval_status", "varchar(30) not null default 'PENDING'");
        addColumnIfMissing("mandor_approval_status", "varchar(20) not null default 'PENDING'");
        addColumnIfMissing("accepted_kg_by_admin", "integer");
        addColumnIfMissing("recognized_kg", "integer");
    }

    private boolean tableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(*) from information_schema.tables where table_name = ?",
                    Integer.class,
                    tableName
            );
            return count != null && count > 0;
        } catch (RuntimeException exception) {
            log.debug("Skipping schema repair; table metadata unavailable", exception);
            return false;
        }
    }

    private void addColumnIfMissing(String columnName, String definition) {
        if (columnExists(columnName)) {
            return;
        }

        jdbcTemplate.execute("alter table pengiriman add column " + columnName + " " + definition);
        log.info("Added missing pengiriman.{} column", columnName);
    }

    private boolean columnExists(String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = 'pengiriman' and column_name = ?",
                Integer.class,
                columnName
        );
        return count != null && count > 0;
    }
}
