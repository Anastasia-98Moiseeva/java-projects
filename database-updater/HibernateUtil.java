import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import ru.mipt.java2017.hw3.model.Author;
import ru.mipt.java2017.hw3.model.Book;
import ru.mipt.java2017.hw3.model.BookAuthor;

public class HibernateUtil {
  private SessionFactory sessionFactory;
  HibernateUtil(String url) {
    Configuration configuration = new Configuration();
    configuration.addAnnotatedClass(Author.class);
    configuration.addAnnotatedClass(Book.class);
    configuration.addAnnotatedClass(BookAuthor.class);
    //configuration.setProperty("hibernate.connection.driver_class","org.postgresql.Driver");
    configuration.setProperty("hibernate.connection.url", url);
    configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLiteDialect");
    //configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL82Dialect");
    //configuration.setProperty("hibernate.hbm2ddl.auto", "update");
    configuration.setProperty("hibernate.show_sql", "true");

    StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
      .applySettings(configuration.getProperties());
    ServiceRegistry registry = builder.build();
    sessionFactory = configuration.buildSessionFactory(registry);
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }
}
