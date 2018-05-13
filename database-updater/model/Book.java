package model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Books")
public class Book {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  @Column(length = 13)
  private BigInteger ISBN;
  @Column(length = 100)
  private String title;
  @Column(length = 400)
  private String cover;

  @OneToMany(mappedBy = "book")
  public Set<BookAuthor> booksAuthors;

  public Book() {}

  public Book(BigInteger ISBN, String title) {
    this.ISBN = ISBN;
    this.title = title;
  }

  public BigInteger getISBN() {
    return ISBN;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setCover(String cover) {
    this.cover = cover;
  }

  public String getCover() {
    return cover;
  }

  public int getId() {
    return id;
  }

  public String[] getFields() {
    return new String[]{Integer.toString(id), ISBN.toString(), title, cover};
  }
}
