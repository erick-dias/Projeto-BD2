package br.edu.bd2.app;

import br.edu.bd2.dao.JPAUtil;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Persistence;

/**
 * Gera o script DDL do OLTP em sql/01_ddl_oltp.sql a partir das entidades
 * (JPA 2.1 schema generation, executado pelo Hibernate).
 * mvn compile exec:java -Dexec.mainClass=br.edu.bd2.app.ExportarDDL
 */
public class ExportarDDL {

    public static void main(String[] args) {
        String destino = args.length > 0 ? args[0] : "sql/01_ddl_oltp.sql";
        Map<String, Object> props = new HashMap<>();
        props.put("javax.persistence.schema-generation.scripts.action", "create");
        props.put("javax.persistence.schema-generation.scripts.create-target", destino);
        props.put("javax.persistence.schema-generation.database.action", "none");
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.hbm2ddl.delimiter", ";");
        // gera o script sem precisar abrir conexao com o banco
        props.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        Persistence.generateSchema(JPAUtil.UNIDADE, props);
        System.out.println("DDL gerado em " + destino);
        System.exit(0);
    }
}
