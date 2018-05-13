package model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Books_Authors")
public class BookAuthor {
  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "books_id")
  private Book book;
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "authors_id")
  private Author author;
  @Column(name = "num")
  private int num;

  public BookAuthor() {}

  public BookAuthor(Book book, Author author, int num) {
    this.book = book;
    this.author = author;
    this.num = num;
  }

  public String[] getFields() {
    return new String[]{Integer.toString(id), Integer.toString(book.getId()),
        Integer.toString(author.getId()), Integer.toString(num)};
  }

}
