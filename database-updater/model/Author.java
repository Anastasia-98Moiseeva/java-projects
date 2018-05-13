package model;

//import com.sun.istack.internal.NotNull;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Authors")
public class Author {
  public Author() {}
  public Author(String name) {
    this.name = name;
  }

  @Id
  @Column(name= "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  @Column(length = 50)
  //@NotNull
  private String name;
  @OneToMany(mappedBy = "author")
  public Set<BookAuthor> userGroups;

  public int getId() {
    return id;
  }

  public String[] getFields() {
    return new String[]{Integer.toString(id), name};
  }
}
