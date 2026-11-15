package br.edu.bd2.dao;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Gera nomes fisicos em snake_case (ItemVenda -> item_venda, dataVenda -> data_venda),
 * o que deixa o DDL legivel e facilita escrever o ETL em SQL puro (sem aspas).
 */
public class SnakeCaseNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private static final long serialVersionUID = 1L;

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return converter(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return converter(name);
    }

    private Identifier converter(Identifier name) {
        if (name == null) {
            return null;
        }
        String snake = name.getText().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
        return Identifier.toIdentifier(snake, name.isQuoted());
    }
}
