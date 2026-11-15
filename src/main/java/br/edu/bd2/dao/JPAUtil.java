package br.edu.bd2.dao;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/** Fabrica unica de EntityManager (a EntityManagerFactory e cara e thread-safe). */
public final class JPAUtil {

    public static final String UNIDADE = "farmaciaPU";

    private static EntityManagerFactory emf;

    private JPAUtil() {
    }

    public static synchronized EntityManagerFactory getFactory() {
        if (emf == null) {
            Map<String, Object> props = new HashMap<>();
            // mvn exec:java -Dbd2.showSql=true  -> mostra o SQL do Hibernate (util na demonstracao)
            if (Boolean.getBoolean("bd2.showSql")) {
                props.put("hibernate.show_sql", "true");
            }
            // Permite trocar o banco sem editar o XML: -Dbd2.url=... -Dbd2.user=... -Dbd2.password=...
            copiar(props, "bd2.url", "javax.persistence.jdbc.url");
            copiar(props, "bd2.user", "javax.persistence.jdbc.user");
            copiar(props, "bd2.password", "javax.persistence.jdbc.password");
            emf = Persistence.createEntityManagerFactory(UNIDADE, props);
        }
        return emf;
    }

    private static void copiar(Map<String, Object> props, String sysProp, String jpaProp) {
        String valor = System.getProperty(sysProp);
        if (valor != null) {
            props.put(jpaProp, valor);
        }
    }

    public static EntityManager criarEntityManager() {
        return getFactory().createEntityManager();
    }

    public static synchronized void fechar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
