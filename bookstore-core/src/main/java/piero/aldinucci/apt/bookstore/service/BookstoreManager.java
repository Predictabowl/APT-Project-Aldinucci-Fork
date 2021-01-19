package piero.aldinucci.apt.bookstore.service;

import java.util.List;

import piero.aldinucci.apt.bookstore.model.Author;
import piero.aldinucci.apt.bookstore.model.Book;

public interface BookstoreManager {
	public Author newAuthor(Author author);
	public Book newBook(Book book);
	public void deleteAuthor(long id);
	public void deleteBook(long id);
	public List<Author> getAllAuthors();
	public List<Book> getAllBooks();
	public void update(Author author);
	public void update(Book book);
}
